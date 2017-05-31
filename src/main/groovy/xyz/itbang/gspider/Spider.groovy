package xyz.itbang.gspider

import groovy.util.logging.Slf4j
import xyz.itbang.gspider.handler.Handler

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

/**
 * a groovy spider.
 * Created by yan on 2017/2/13.
 */
@Slf4j
class Spider{
    String crawlName = "GSpider"
    int maxRoundCount = 3
    int maxFetchCount = 100
    int maxThreadCount  = 3
    boolean includeOutSite = false
    Map<Integer, HashSet<String>> roundLinks = new HashMap<Integer, HashSet<String>>()
    ExecutorService service
    List<Pattern> includeRegexList = new ArrayList<>()
    List<Pattern> excludeRegexList = new ArrayList<>()
    List<Handler> handlerList = new ArrayList<>()
    Closure reviewPage
    Closure reviewCrawl

    void completeInit(){
        if (!service) service = Executors.newFixedThreadPool(maxThreadCount)
        log.info("Config : round $maxRoundCount ,maxFetch $maxFetchCount ,thread $maxThreadCount ,seeds ${getRoundLinkSet(1)} .")
    }

    void start(){
        Date start = new Date()
        crawlName = crawlName+"@${start.time}"
        log.info("$crawlName starting ...")


        completeInit()

        maxRoundCount.times {
            def round = it+1,links = getRoundLinkSet(round).value
            log.info("Start round ${round} ,total ${links.size()} ...")
            def tasks = links.collect{
                def link = it.toString()
                new Callable<Object>() {
                    @Override
                    Object call() {
                        Page page = new Page(crawlName,round,link)
                        try {
                            process(page)
                            page.endAt = new Date()
                            reviewPage?.call(page)
                        } catch (Exception e) {
                            page.markAsFailed()
                            e.printStackTrace()
                        } finally {
                            page.endAt = page.endAt ?: new Date() //避免被异常吞掉
                            log.debug("Process url ${page.url} over, use time ${(page.endAt.time - page.startAt.time)/1000} s")
                        }
                    }
                }
            }
            service.invokeAll(tasks)
        }
        service.shutdown()

        Date end = new Date()
        reviewCrawl?.call(this,start,end)

        log.info("Crawl over,fetch totle ${roundLinksTotal()} , total time ${(end.time - start.time)/1000} s .")
    }
    //process
    void process(Page page){
        log.debug("Process url ${page.url}")

        handlerList.each {
            if (it.matches(page.url)) it.handle(page)
        }

        parserLinks(page)
    }

    void parserLinks(Page page) {
        if (page.currentRound >= maxRoundCount) return
        if (roundLinksTotal() >= maxFetchCount) return

        log.debug("Parse links from ${page.url}")
        if (page.links) {
            page.links.each {
                it = reorganize(page, it)
            }
        } else {
            page.document.select("a[href]")*.attr('href').each {
                def href = it.toString()
                //这里根据规则过滤
                if (["javascript:", "mailto:","#"].find { href.contains(it) }) return
                def link = reorganize(page, href)
                if (!includeOutSite && !inSite(page,link)) return
                if (excludeRegexList && excludeRegexList.find { it.matcher(link).matches() }) return
                if (includeRegexList && !includeRegexList.find { it.matcher(link).matches() }) return

                page.links.add(link)
            }
        }
        log.debug("               And find ${page.links.size()} links , ${page.links}")

        page.links.each {
            String url = it.trim()
            if (!roundLinks.values().find { it.contains(url) } && roundLinksTotal() < maxFetchCount) {
                getRoundLinkSet(page.currentRound + 1).add(it)
            } else {
                log.debug("Because too mach or duplicate ,drop the link $it")
            }
        }
    }

    private Set<String> getRoundLinkSet(int i) {
        if (!roundLinks[i]) roundLinks.put(i, Collections.synchronizedSet(new HashSet()))
        return roundLinks[i]
    }

    int roundLinksTotal(){
        return roundLinks.values()*.size().sum()
    }

    private String reorganize(Page page, String url) {
        if (url.startsWith('http://') || url.startsWith('https://')) return url
        if (url.startsWith('//')) return "${page.uri.scheme}:$url"
        return "${page.host}/${!url.startsWith('/') ? url : url.substring(1)}"
    }
    //本站的子域名，要包含在内
    private boolean inSite(Page page, String url){
        def baseDomain = page.uri.host.split('\\.')[-2,-1].join('.')
        return url.contains(baseDomain)
    }


    static crawl(@DelegatesTo(SpiderConfig) Closure closure) {
        Spider spider = new Spider()

        SpiderConfig config = new SpiderConfig(spider)
        def code = closure.rehydrate(config, config, config)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        spider.start()
    }
}

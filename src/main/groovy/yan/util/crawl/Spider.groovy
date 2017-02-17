package yan.util.crawl

import groovy.util.logging.Slf4j
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
    int maxRoundCount = 3
    int maxFetchCount = 100
    int maxThreadCount  = 3
    boolean includeOutSite = false
    List<Pattern> includeRegexList = new ArrayList<>()
    List<Pattern> excludeRegexList = new ArrayList<>()
    Map<Pattern,Closure> handlers = new HashMap<>()
    Map<Integer,HashSet<String>> roundLinks = new HashMap<Integer,HashSet<String>>()
    Closure downloader,reviewPage
    ExecutorService service

    void completeInit(){
        if (!service) service = Executors.newFixedThreadPool(maxThreadCount)
        log.info("Config : round $maxRoundCount ,maxFetch $maxFetchCount ,thread $maxThreadCount ,seeds ${getRoundLinkSet(1)} .")
    }

    void start(){
        log.info("Spider starting ...")

        completeInit()

        maxRoundCount.times {
            def round = it+1,links = getRoundLinkSet(round).value
            log.info("Start round ${round} ,total ${links.size()} ...")
            def tasks = links.collect{
                def link = it.toString() //不知道为什么，如果没有 toString ，得到的是 char object。
                new Callable<Object>() {
                    @Override
                    Object call() {
                        Page page = new Page(url: link, round: round)
                        try {
                            process(page)
                        }catch (Exception e){
                            e.printStackTrace()
                        }

                        reviewPage.call(page)
                    }
                }
            }
            service.invokeAll(tasks)
        }
        service.shutdown()

        log.info("Crawl over,fetch totle ${roundLinks.values()*.size().sum()} .")
    }
    //process
    void process(Page page){
        log.debug("Process url ${page.url}")
        page.startAt = new Date()
        page.text = downloader ? downloader.call(page.url) : new URL(page.url).getText([connectTimeout:5000,readTimeout:5000])
        page.downloadedAt = new Date()

        handlers.each {
            if (it.key.matcher(page.url).matches()){
                it.value.call(page)
            }
        }

        parserLinks(page)

        page.endAt = new Date()
        log.debug("Process url ${page.url} over, use time ${(page.endAt.time - page.startAt.time)} ms")
    }

    void parserLinks(Page page) {
        if (page.round >= maxRoundCount) return
        if (roundLinks.values()*.size().sum() >= maxFetchCount) return

        log.debug("Parse links from ${page.url}")
        if (page.links){
            page.links.each {
                it = reorganize(page,it)
            }
        }else {
            page.html.body.'**'.findAll{it.name()=='a'}.each {
                //这里根据规则过滤
                def link = reorganize(page,(it.@href).toString())
                if (!includeOutSite && !link.startsWith(page.host)) return
                if (excludeRegexList && excludeRegexList.find { it.matcher(link).matches() }) return
                if (includeRegexList && !includeRegexList.find { it.matcher(link).matches() }) return

                page.links.add(link)
            }
        }
        log.debug("               And find ${page.links.size()} links , ${page.links}")

        page.links.each {
            String url = it.trim()
            if (["javascript:","mailto:","#"].find { url.contains(it) }) return
            if (!roundLinks.values().find{ it.contains(url)} && roundLinks.values()*.size().sum() < maxFetchCount){
                getRoundLinkSet(page.round+1).add(it)
            }else {
                log.debug("Because too mach or duplicate ,drop the link $it")
            }
        }
    }

    private Set<String> getRoundLinkSet(int i){
        if (!roundLinks[i]) roundLinks.put(i,Collections.synchronizedSet(new HashSet()))
        return roundLinks[i]
    }

    private String reorganize(Page page,String url){
        url.contains('://') ? url : "${page.host}/${!url.startsWith('/') ? url : url.substring(1)}"
    }

    static crawl(@DelegatesTo(SpiderConfig) Closure closure){
        Spider spider = new Spider()

        SpiderConfig config = new SpiderConfig(spider)
        def code = closure.rehydrate(config, config, config)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        spider.start()
    }
}

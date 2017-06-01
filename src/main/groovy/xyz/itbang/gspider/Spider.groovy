package xyz.itbang.gspider

import groovy.util.logging.Slf4j
import xyz.itbang.gspider.handler.Handler
import xyz.itbang.gspider.remote.HessianClientSpider
import xyz.itbang.gspider.remote.HessianServerScheduler
import xyz.itbang.gspider.scheduler.LocalScheduler
import xyz.itbang.gspider.scheduler.Scheduler
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

/**
 * a groovy spider.
 * Created by yan on 2017/2/13.
 */
@Slf4j
class Spider{
    static List<String> roles = ['alone','server','client']

    String crawlName = "GSpider"
    int maxRoundCount = 3
    int maxFetchCount = 100
    int maxThreadCount  = 3
    ExecutorService service
    boolean includeOutSite = false
    Map<Integer, HashSet<String>> roundLinks = new HashMap<Integer, HashSet<String>>()
    Scheduler scheduler
    List<Pattern> includeRegexList = new ArrayList<>()
    List<Pattern> excludeRegexList = new ArrayList<>()
    List<Handler> handlerList = new ArrayList<>()
    Closure reviewPage
    Closure reviewCrawl
    //分布式配置
    String role = 'alone' // alone 独立，server 服务端，client 客户端
    String serviceURL = "http://localhost:8080/service"

    //启动，独立或者服务端。
    void start(){
        Date start = new Date()
        crawlName = crawlName+"@${start.time}"
        log.info("$crawlName starting ...")

        //初始化调度器
        if (role == 'alone'){
            service = Executors.newFixedThreadPool(maxThreadCount)
            scheduler = new LocalScheduler(service,handlerList)
        }else if (role == 'server'){
            HessianServerScheduler hessianServerScheduler = new HessianServerScheduler()
            hessianServerScheduler.startService(serviceURL)
            scheduler = hessianServerScheduler
        }

        log.info("Config : round $maxRoundCount ,maxFetch $maxFetchCount ,thread $maxThreadCount ,seeds ${getRoundLinkSet(1)} .")

        maxRoundCount.times {
            int round = it+1
            Set<String> links = getRoundLinkSet(round).value
            log.info("Start round ${round} ,total ${links.size()} ...")

            def pages = scheduler.dealRoundLinks(crawlName,round,links)

            pages.each {
                try {
                    parserLinks(it)
                    reviewPage?.call(it)
                } catch (Exception e) {
                    e.printStackTrace()
                    it.markAsFailed()
                }
            }
        }

        service?.shutdown() //有些调度器不需要多线程，并未初始化之。

        Date end = new Date()
        reviewCrawl?.call(this,start,end)

        log.info("Crawl over,fetch totle ${roundLinksTotal()} , total time ${(end.time - start.time)/1000} s .")
    }

    void startClient(){
        log.info("Starting client spider,${maxThreadCount} thread ")

        HessianClientSpider clientSpider = new HessianClientSpider(serviceURL,handlerList)
        int idleCount = 0
        int maxIdleCount = 1
        int idleSleepTime = 3000
        maxThreadCount.times {
            def t = new Thread(new Runnable() {
                @Override
                void run() {
                    while (true){
                        try {
                            clientSpider.process()
                            idleCount = 0
                        }catch (Exception e){
                            log.warn("Exception : ${e.getMessage()} ,sleep ${idleSleepTime} ...")
                            sleep(idleSleepTime)
                            idleCount = idleCount+1
                        }
                    }
                }
            })
            t.start()
        }

        while (true){
            if (idleCount >= maxIdleCount*maxThreadCount){
                log.info("Client spider idle too long ,about ${(idleSleepTime*idleCount)/maxThreadCount} ms,it will shutdown now .")
                break
            }else {
                sleep(1000)
            }
        }
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

        if (spider.role in roles){
            if (spider.role == 'client'){
                spider.startClient()
            }else {
                spider.start()
            }
        } else {
            throw new Exception("Role ${spider.role} not in roles ${roles}")
        }
    }
}

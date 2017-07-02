package xyz.itbang.gspider

import groovy.util.logging.Slf4j
import xyz.itbang.gspider.handler.Handler
import xyz.itbang.gspider.scheduler.LocalScheduler
import xyz.itbang.gspider.scheduler.Scheduler
import xyz.itbang.gspider.scheduler.distribute.DistributeScheduler
import xyz.itbang.gspider.scheduler.distribute.HessianClient
import xyz.itbang.gspider.scheduler.distribute.HessianServer
import xyz.itbang.gspider.util.SpiderConfig
import java.util.regex.Pattern

/**
 * a groovy spider.
 * Created by yan on 2017/2/13.
 */
@Slf4j
class Spider{

    static List<String> roles = ['alone','server','client']

    String name = "GSpider"
    List<String> seeds = []
    int maxRoundCount = 3
    int maxFetchCount = 100
    int maxThreadCount  = 3
    int maxWaitingTime = 60 * 1000 //默认60s
    boolean includeOutSite = false
    List<Pattern> includeRegexList = new ArrayList<>()
    List<Pattern> excludeRegexList = new ArrayList<>()

    //处理器，回顾等
    List<Handler> handlerList = new ArrayList<>()
    Closure reviewPage
    Closure reviewCrawl

    //调度器及分布式配置
    Scheduler scheduler
    String role = 'alone' // alone 独立，server 服务端，client 客户端
    String serviceURL = "http://localhost:8088/service"
    int maxClientWaitingTime = 60*60*1000 //默认一小时

    //内部数据
    int round = 1 //当前轮
    List<String> _hosts = []
    Date createAt = new Date(),startAt,endAt

    //标识某一次抓取名称
    String getCrawlName(){
        "${name}@${createAt.time}"
    }

    //缓存的站点列表
    List<String> getHosts(){
        if (_hosts) return _hosts
        seeds.each {
            _hosts << new URI(it).host.split('\\.')[-2,-1].join('.')
        }
        return _hosts
    }

    //验证链接是否合规
    boolean validate(String link){
        //这里根据规则过滤
        if (["javascript:", "mailto:"].find { link.contains(it) }) return false
        if (!includeOutSite && !hosts.find {link.contains(it)}) return false
        if (excludeRegexList && excludeRegexList.find { it.matcher(link).matches() }) return false
        if (includeRegexList && !includeRegexList.find { it.matcher(link).matches() }) return false
        return true
    }

    //常规入口
    static crawl(@DelegatesTo(SpiderConfig) Closure closure) {
        Spider spider = new Spider()

        SpiderConfig config = new SpiderConfig(spider)
        def code = closure.rehydrate(config, config, config)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        //根据角色，启动相应的程序
        switch (spider.role){
            case 'alone':
                new LocalScheduler().ship(spider)
                break
            case 'server':
                new HessianServer(spider.serviceURL).start()
                new DistributeScheduler().ship(spider)
                break
            case 'client':
                new HessianClient().start(spider)
                break
            default:
                new Exception("Role ${spider.role} not in roles ${roles}")
        }
    }
}

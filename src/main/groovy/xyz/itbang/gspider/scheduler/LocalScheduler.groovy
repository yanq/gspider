package xyz.itbang.gspider.scheduler

import groovy.util.logging.Slf4j
import xyz.itbang.gspider.Page
import xyz.itbang.gspider.Spider
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * 本地调度器
 * Created by yan on 2017/5/31.
 */
@Slf4j
class LocalScheduler implements Scheduler {
    ExecutorService service
    Map<Integer, HashSet<String>> roundLinks = new HashMap<Integer, HashSet<String>>()

    @Override
    void ship(Spider spider) {
        //初始化
        getRoundLinkSet(1).addAll(spider.seeds)

        spider.startAt = new Date()
        log.info("Starting spider(${spider.role}), ${spider.crawlName} ...")
        log.info("Config : round ${spider.maxRoundCount} ,maxFetch ${spider.maxFetchCount} ,thread ${spider.maxThreadCount} ,seeds ${spider.seeds} .")

        spider.maxRoundCount.times {
            spider.round = it+1
            def s = new Date()
            log.info("Start round ${spider.round} ,total ${getRoundLinkSet(spider.round).size()} ...")

            dealRoundLinks(spider)

            log.info("Complete round ${spider.round} ,total time ${(new Date().time - s.time)/1000}s .")
        }

        service?.shutdown()

        spider.endAt = new Date()
        spider.reviewCrawl?.call(spider)

        log.info("Crawl over,fetch totle ${roundLinksTotal()} , total time ${(spider.endAt.time - spider.startAt.time)/1000}s .")
    }

    void dealRoundLinks(Spider spider) {
        service = service ?: Executors.newFixedThreadPool(spider.maxThreadCount)
        Set<String> links = getRoundLinkSet(spider.round).value

        LinkedList<String> todoList = new LinkedList<>(links)
        List<Future> doingList = new ArrayList<>()

        while (true) {
            doingList.removeAll {it.done}

            if (todoList.size() == 0 && doingList.size() == 0) break

            //处理列表避免过长，也避免过于频繁检测，休息10毫秒。
            if (doingList.size() > spider.maxThreadCount) {
                sleep(10)
                continue
            }


            def url = todoList.poll()
            if (url) {
                doingList << service.submit(buildCallable(spider,url.toString()))
                continue
            }

            //如果没有待处理的 url 了，等一会儿。
            sleep(10)
        }
    }

    //构建 Callable
    private buildCallable(Spider spider,String url) {
        new Callable<Page>() {
            @Override
            Page call() {
                Page page = new Page(spider.crawlName, spider.round, url)
                try {
                    spider.handlerList.each {
                        if (it.matches(page.url)) it.handle(page)
                    }
                    page.endAt = new Date()

                    spider.reviewPage?.call(page)
                    parserLinks(spider,page)
                } catch (Exception e) {
                    e.printStackTrace()
                    page.markAsFailed()
                } finally {
                    page.endAt = page.endAt ?: new Date()
                    log.debug("Process url ${page.url} , use time ${page.endAt.time - page.startAt.time} ms")
                }

                return page
            }
        }
    }


    void parserLinks(Spider spider,Page page) {
        if (page.noMoreLinks) return
        if (page.currentRound >= spider.maxRoundCount) return
        if (roundLinksTotal() >= spider.maxFetchCount) return

        log.debug("Parse links from ${page.url}")
        if (page.links) {
            page.links.each {
                it = page.reorganize(it)
            }
        } else {
            page.document.select("a[href]")*.attr('href').each {
                def href = page.reorganize(it.toString())
                if (spider.validate(href)) page.links.add(href)
            }
        }
        log.debug("               And find ${page.links.size()} links , ${page.links}")

        page.links.each {
            String url = it.trim()
            if (!roundLinks.values().find { it.contains(url) } && roundLinksTotal() < spider.maxFetchCount) {
                getRoundLinkSet(page.currentRound + 1).add(it)
            } else {
                log.debug("Because too mach or duplicate ,drop the link $it")
            }
        }
    }

    protected Set<String> getRoundLinkSet(int i) {
        if (!roundLinks[i]) roundLinks.put(i, Collections.synchronizedSet(new HashSet()))
        return roundLinks[i]
    }

    protected int roundLinksTotal(){
        return roundLinks.values()*.size().sum()
    }
}

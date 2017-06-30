package xyz.itbang.gspider.scheduler

import groovy.util.logging.Slf4j
import xyz.itbang.gspider.Page
import xyz.itbang.gspider.Spider
import xyz.itbang.gspider.util.TimedFuture

import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * 本地调度器
 * Created by yan on 2017/5/31.
 */
@Slf4j
class LocalScheduler implements Scheduler {
    Spider spider

    LocalScheduler(Spider spider) {
        this.spider = spider
        this.spider.service = Executors.newFixedThreadPool(this.spider.maxThreadCount)
    }

    @Override
    void dealRoundLinks(String crawlName, int round, Set<String> links) {
        LinkedList<String> todoList = new LinkedList<>(links)
        List<TimedFuture> doingList = new ArrayList<>()

        while (true) {
            //更新状态
            doingList.removeAll {
                if (it.done()) return true
                if (it.overTime()){
                    log.warn("Time out : $it.name")
                    return true
                }
            }

            if (todoList.size() == 0 && doingList.size() == 0) break

            if (doingList.size() > spider.maxThreadCount * 10) {
                log.info("Prosessing url size：${doingList.size()} ，waiting 1s  ... ")
                sleep(1000)
                continue
            }


            def url = todoList.poll()
            if (url) {
                def future = spider.service.submit(buildCallable(url.toString()))
                doingList << new TimedFuture(url.toString(), future, spider.maxWaitingTime)
                continue
            }

            //如果没有待处理的 url 了，等一会儿。
            sleep(100)
        }
    }

    //构建 Callable
    private buildCallable(String url) {
        new Callable<Integer>() {
            @Override
            Integer call() {
                Page page = new Page(spider.crawlName, spider.round, url)
                try {
                    spider.handlerList.each {
                        if (it.matches(page.url)) it.handle(page)
                    }
                    page.endAt = new Date()

                    spider.reviewPage?.call(page)
                    spider.parserLinks(page)

                    return 1
                } catch (Exception e) {
                    e.printStackTrace()
                    page.markAsFailed()
                } finally {
                    page.endAt = page.endAt ?: new Date()
                    log.debug("Process url ${page.url} , use time ${page.endAt.time - page.startAt.time} ms")
                }

                return 0
            }
        }
    }

    @Override
    void shutdown() {}
}

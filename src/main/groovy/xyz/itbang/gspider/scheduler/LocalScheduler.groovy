package xyz.itbang.gspider.scheduler

import groovy.util.logging.Slf4j
import xyz.itbang.gspider.Page
import xyz.itbang.gspider.Spider
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

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
        List<Future> doingList = new ArrayList<>()

        while (true) {
            doingList.removeAll {it.done()}

            if (todoList.size() == 0 && doingList.size() == 0) break

            //处理列表避免过长，也避免过于频繁检测，休息10毫秒。
            if (doingList.size() > spider.maxThreadCount) {
                sleep(10)
                continue
            }


            def url = todoList.poll()
            if (url) {
                doingList << spider.service.submit(buildCallable(url.toString()))
                continue
            }

            //如果没有待处理的 url 了，等一会儿。
            sleep(10)
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

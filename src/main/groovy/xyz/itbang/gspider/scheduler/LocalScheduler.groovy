package xyz.itbang.gspider.scheduler

import groovy.util.logging.Slf4j
import xyz.itbang.gspider.Page
import xyz.itbang.gspider.Spider
import xyz.itbang.gspider.handler.Handler
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 本地调度器
 * Created by yan on 2017/5/31.
 */
@Slf4j
class LocalScheduler implements Scheduler {
    Spider spider

    LocalScheduler(Spider spider){
        this.spider = spider
    }

    @Override
    void dealRoundLinks(String crawlName, int round, Set<String> links) {
        def tasks = links.collect{
            def link = it.toString()
            new Callable<Object>() {
                @Override
                Object call() {
                    Page page = new Page(spider.crawlName,round,link)
                    try {
                        spider.handlerList.each {
                            if (it.matches(page.url)) it.handle(page)
                        }

                        spider.reviewPage?.call(page)
                        spider.parserLinks(page)
                    } catch (Exception e) {
                        e.printStackTrace()
                        page.markAsFailed()
                    } finally {
                        page.endAt = new Date()
                        log.debug("Process url ${page.url} , use time ${page.endAt.time - page.startAt.time} ms")
                    }
                }
            }
        }
        spider.service.invokeAll(tasks)
    }

    @Override
    void shutdown() {}
}

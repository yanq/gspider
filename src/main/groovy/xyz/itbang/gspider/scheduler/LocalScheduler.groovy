package xyz.itbang.gspider.scheduler

import groovy.util.logging.Slf4j
import xyz.itbang.gspider.Page
import xyz.itbang.gspider.handler.Handler
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by yan on 2017/5/31.
 */
@Slf4j
class LocalScheduler implements Scheduler {
    ExecutorService service
    List<Handler> handlerList

    LocalScheduler(ExecutorService service,List<Handler> handlerList){
        this.service = service
        this.handlerList = handlerList
    }

    @Override
    List<Page> dealRoundLinks(String crawlName, int round, Set<String> links) {
        def results = []
        def tasks = links.collect{
            def link = it.toString()
            new Callable<Object>() {
                @Override
                Object call() {
                    Page page = new Page(crawlName,round,link)
                    try {
                        handlerList.each {
                            if (it.matches(page.url)) it.handle(page)
                        }
                    } catch (Exception e) {
                        e.printStackTrace()
                        page.markAsFailed()
                    } finally {
                        page.endAt = new Date()
                        log.debug("Process url ${page.url} over, use time ${(page.endAt.time - page.startAt.time)/1000} s")
                    }
                    results << page
                }
            }
        }
        service.invokeAll(tasks)
        return results
    }
}

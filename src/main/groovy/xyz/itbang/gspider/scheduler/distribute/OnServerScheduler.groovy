package xyz.itbang.gspider.scheduler.distribute

import groovy.util.logging.Slf4j
import xyz.itbang.gspider.Page
import xyz.itbang.gspider.Spider
import xyz.itbang.gspider.scheduler.AloneScheduler

/**
 * 服务端调度器
 * 部署于服务端的调度器，主要职责是把每一轮的抓取通过 DataCenter 委托给 HessianServer 来处理。
 * Created by yan on 2017/5/31.
 */
@Slf4j
class OnServerScheduler extends AloneScheduler {

    @Override
    void dealRoundLinks(Spider spider) {
        LinkedList<String> todoList = Collections.synchronizedCollection(new LinkedList<String>(getRoundLinkSet(spider.round)))
        LinkedList<Page> doingList = Collections.synchronizedCollection(new LinkedList<Page>())
        def count = todoList.size()

        DataCenter.addSpiderRound(spider,todoList,doingList)

        Date startWaiting
        while (true) {
            //收到 page 就处理
            if (doingList.size() > 0) {
                log.trace("Doing list size : ${doingList.size()},${startWaiting ?'clear waiting status,':''}process it ...")

                def page = doingList.poll()
                parserLinks(spider,page)
                page.endAt = new Date()
                log.info("Process url ${page.url} , download time ${page.downloadTime} ms , total time ${page.endAt.time - page.startAt.time} ms .")

                count--
                continue
            }

            //没有分发完，就等
            if (todoList.size() > 0){
                log.trace("Todo list size : ${todoList.size()},just sleep(10) and continue")
                sleep(10)
                continue
            }

            //完事儿就结束
            if (todoList.size() == 0 && count == 0) break

            //开始计时等
            if (startWaiting){
                if ((new Date().time - startWaiting.time) > spider.maxWaitingTime){
                    log.warn("Waiting time（${spider.maxWaitingTime}ms） is out，just break ~")
                    break
                }
            }else {
                startWaiting = new Date()
                log.trace("Start waiting to end this round")
            }
            sleep(100)
        }
    }


}

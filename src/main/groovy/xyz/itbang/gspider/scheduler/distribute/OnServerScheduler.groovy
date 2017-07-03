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
                def page = doingList.poll()
                parserLinks(spider,page)
                count--
                continue
            }

            //没有分发完，就等
            if (todoList.size() > 0){
                sleep(10)
                continue
            }

            //完事儿就结束
            if (todoList.size() == 0 && count == 0) break

            //开始计时等
            if (startWaiting){
                if ((new Date().time - startWaiting.time) > spider.maxWaitingTime) break
            }else {
                startWaiting = new Date()
            }
            sleep(10)
        }
    }


}

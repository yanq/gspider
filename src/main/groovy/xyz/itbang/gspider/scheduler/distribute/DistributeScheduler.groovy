package xyz.itbang.gspider.scheduler.distribute

import groovy.util.logging.Slf4j
import xyz.itbang.gspider.Page
import xyz.itbang.gspider.Spider
import xyz.itbang.gspider.scheduler.LocalScheduler

/**
 * 基于 Hessian 的调度器，server 端。
 * Created by yan on 2017/5/31.
 */
@Slf4j
class DistributeScheduler extends LocalScheduler {

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

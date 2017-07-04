package xyz.itbang.gspider.scheduler.distribute

import xyz.itbang.gspider.Page
import xyz.itbang.gspider.Spider

/**
 * 巢
 * 管理者蜘蛛及数据，在调度器和服务器之间传递数据
 * Created by yan on 2017/7/2.
 */
class DataCenter {
    static Map<Spider, LinkedList<String>> todo = [:]
    static Map<Spider, LinkedList<Page>> doing = [:]

    static Page get() {
        def e = todo.find { it.value.size() > 0 }
        def url = e?.value?.poll()
        if (url) return new Page(e.key.crawlName, e.key.round, url)
        return null
    }

    static void put(Page page) {
        def e = doing.find { it.key.crawlName == page.crawlName }
        if (e) {
            e.key.reviewPage?.call(page) //把尽可能多的工作放到这里，避免主线程吃紧
            e.value.offer(page)
        }
    }

    static void addSpiderRound(Spider spider, LinkedList<String> todoList, LinkedList<Page> doingList) {
        todo.put(spider, todoList)
        doing.put(spider, doingList)
    }
}

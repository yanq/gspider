package xyz.itbang.gspider

/**
 * Created by yan on 2017/2/16.
 */
class SpiderTest extends GroovyTestCase {
    void testReorganize() {
        Page page = new Page(url: "http://luofans.com:8080")
        Spider spider = new Spider()
        println spider.reorganize(page,'a')
        println spider.reorganize(page,"http://a.com")
    }
}

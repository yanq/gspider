package xyz.itbang.gspider

import xyz.itbang.gspider.util.Tools

/**
 * Created by yan on 2017/2/16.
 */
class SpiderTest extends GroovyTestCase {

    String luofans = "http://luofans.com:8080"
    String localHi = "http://localhost:8080/hi"

    //官方示例
    void testSpider() {
        def list = ["http://www.luofans.com/","http://www.luofans.com/audios",luofans]
        Spider.crawl {
            seeds list
            rounds 1
            handle { Page page ->
                println("Handle -> " + page.url)
                //Tools.waitFor("登录") //noway
                println("Title -> " + page.document.title())
            }
        }
    }

    void testReorganize() {
        Page page = new Page(url: "http://luofans.com:8080")
        Spider spider = new Spider()
        println spider.reorganize(page, 'a')
        println spider.reorganize(page, "http://a.com")
    }

    void testPageHTML() {
        Page page = new Page()
        page.text = "<p> a nice day.</p>"
        println(page.document)

        page.clearStatus()
        page.text = "<p> a bad day.<a> </p>"
        println(page.document)
    }

    void testJsoup() {
        Tools.setAcceptAllCookies() //ok

        Page page = new Page(url: localHi)
        println page.connection.userAgent("GSpider").get()
        println page.document
        println page.json

        Page pageLuofans = new Page(url: luofans)
        println pageLuofans.document.select('a')*.attr('href')
    }

    void testWaitFor(){
        //Tools.waitFor("Login,登录") //貌似测试运行时，无法从控制台读取数据，直接运行就可以。
    }

}

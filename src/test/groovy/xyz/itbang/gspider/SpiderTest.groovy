package xyz.itbang.gspider

import xyz.itbang.gspider.handler.AbstractHandler
import xyz.itbang.gspider.util.Tools

/**
 * Created by yan on 2017/2/16.
 */
class SpiderTest extends GroovyTestCase {

    String luofans = "http://luoyouzhijia.cn"
    String localHi = "http://localhost:8080/hi"

    //官方示例
    void testSimple() {
        def list = [luofans]
        Spider.crawl {
            seeds list
            thread 3
            rounds 3
            maxFetch 30

            handle { Page page ->
                println("Handle -> " + page.url)
                println("Title -> " + page.document.title())
            }
        }
    }

    //
    void testSpider() {
        def list = [luofans]
        Spider.crawl {
            //role 'server'
            name "爬"
            seeds list
            thread 1
            rounds 3
            maxFetch 3
            //maxWaiting 100
            include '.*audios/\\d.*'

            handle { Page page ->
                println("Handle -> " + page.url)
                println("Title -> " + page.document.title())
                //page.markNoMoreLinks()
            }

            handle('.*audio.*') { Page page ->
                println("Audio -> ${page.document.title()}")
            }

            //handlers DefaultHandler

//            review { Page page ->
//                println("Time -> ${page.endAt.time - page.startAt.time} ms")
//            }
//
//            reviewCrawl {Spider spider,Date startAt,Date endAt ->
//                println "Status : $spider.crawlName --- $startAt - $endAt"
//            }
        }
    }

    void testClient(){
        Spider.crawl {
            role 'client'
            handlers DefaultHandler
        }
    }

    void testReorganize() {
        Page page = new Page(url: "http://luofans.com:8080")
        println page.reorganize('a')
        println page.reorganize("http://a.com")
        println page.reorganize("https://a.com")
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

    void testWaitFor() {
        //Tools.waitFor("Login,登录") //貌似测试运行时，无法从控制台读取数据，直接运行就可以。
    }

    void testConfig(){
        Spider spider = new Spider()

        spider.config
                .seeds('http://127.0.0.1:8080/')
                .thread(1)
                .maxFetch(5)
                .handle { Page page -> println("Title :${page.document.title()}") }

        spider.start()
    }

    void testHost(){
        ['http://luoyouzhijia.cn/','http://localhost:8080/',"http://127.0.0.1:8080","http://192.168.0.100:8080"].each {
            def host = new URI(it).host
            println "Host : $host"
            println((host=='localhost' || host.matches("(\\d+\\.){3}\\d+")) ? host : host.split('\\.')[-2,-1].join('.'))
        }
    }

    class DefaultHandler extends AbstractHandler{
        @Override
        Page handlePage(Page page) {
            println "Info from default handler @　$page"
        }
    }
}

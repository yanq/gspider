package xyz.itbang.gspider

import xyz.itbang.gspider.util.Tools

/**
 * Created by yan on 2017/2/16.
 */
class SpiderTest extends GroovyTestCase {

    String luofans = "http://luofans.com:8080"
    String localHi = "http://localhost:8080/hi"

    void testReorganize() {
        Page page = new Page(url: "http://luofans.com:8080")
        Spider spider = new Spider()
        println spider.reorganize(page,'a')
        println spider.reorganize(page,"http://a.com")
    }

    void testSpider(){

        Spider.crawl {
            seeds "http://www.luofans.com/"
//            thread(1)
//            maxFetch(10)
//            seeds "http://localhost:8080/hi"
            //downloader new DefaultDownloader(["User-Agent":'A Groovy Spider'],null,false)

            handle{ Page page ->

//                page.downloader = new Downloader() {
//                    @Override
//                    String download(String url) {
//                        Jsoup.connect(url).execute().body()
//                    }
//                }

                println(page.url)
                println(page.document.title())
            }
        }
    }

    void testPageHTML(){
        Page page = new Page()
        page.text = "<p> a nice day.</p>"
        println(page.document)

        page.clearStatus()
        page.text = "<p> a bad day.<a> </p>"
        println(page.document)
    }

    void testJsoup(){
        //Tools.setAcceptAllCookies() //ok

//        Page page = new Page(url: localHi)
//        println page.connection.userAgent("GSpider").get()
//        println page.document
//        println page.json

        Page page = new Page(url: luofans)
        println page.document.select('a')*.attr('href')
    }

}

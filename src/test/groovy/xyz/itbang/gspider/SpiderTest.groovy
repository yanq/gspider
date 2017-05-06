package xyz.itbang.gspider

import xyz.itbang.gspider.download.DefaultDownloader

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

    void testSpider(){

        Spider.crawl {
            seeds "http://localhost:8080/hi"
            //downloader new DefaultDownloader(["User-Agent":'A Groovy Spider'],null,false)

            handle{ Page page ->
                println(page.text)
            }
        }
    }
}

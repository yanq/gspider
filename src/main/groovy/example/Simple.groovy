package yan.example

import xyz.itbang.gspider.Page
import xyz.itbang.gspider.Spider

/**
 * a simple example.
 * Created by yan on 2017/2/13.
 */

Spider.crawl {
    seeds "http://www.oschina.net/"
    rounds 2
    maxFetch 20
    defaultParameters 'connectTimeout':5000,'readTimeout':5000,requestProperties:['user-agent':'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.104 Safari/537.36 Core/1.53.2141.400 QQBrowser/9.5.10219.400']
    acceptCookies true

    handle{ Page page ->
        println("Handle -> "+page.url)
//        println("Result -> "+page.text)
        println("Title -> "+page.html.head.title)
    }
}
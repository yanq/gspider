package yan.example

import yan.util.crawl.Page
import yan.util.crawl.Spider

/**
 * a simple example.
 * Created by yan on 2017/2/13.
 */

Spider.crawl {
    seeds "http://www.luofans.com/"
    rounds 3
    maxFetch 20
    thread 1
    include ".*/audios/.*"
    defaultParameters 'connectTimeout':5000,'readTimeout':5000,requestProperties:['user-agent':'a groovy spider']
    defaultCharset 'utf-8'

    handle{ Page page ->
        println("Handle -> "+page.url)
        println("Title -> "+page.html.head.title)
    }
}
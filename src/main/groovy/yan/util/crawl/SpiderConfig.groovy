package yan.util.crawl

import java.util.regex.Pattern

/**
 * 配置蜘蛛的 DSL
 * Created by yan on 2017/2/15.
 */
class SpiderConfig {
    Spider spider

    SpiderConfig(Spider s){
        this.spider =s
    }

    /**
     * 定义种子页面
     * @param urls
     * @return
     */
    def seeds(String ...urls){
        spider.getRoundLinkSet(1).addAll(urls)
    }

    /**
     * 是否包含外部站点，默认不包含。
     * @param b
     * @return
     */
    def includeOutSite(boolean b){
        spider.includeOutSite = b
    }

    /**
     * 不包含的 URL 的模式 ；先于包含判断以排除。
     * @param pattern 格式为 Java 的正则表达式
     * @return
     */
    def exclude(String... pattern){
        pattern.each {
            spider.excludeRegexList.add(Pattern.compile(it))
        }
    }

    /**
     * 包含的 URL 的模式 ；如果定义了包含，就仅抓取符合规则的页面。
     * @param pattern 格式为 Java 的正则表达式
     * @return
     */
    def include(String... pattern){
        pattern.each {
            spider.includeRegexList.add(Pattern.compile(it))
        }
    }


    /**
     * 抓取轮数
     * 种子是第一轮，分析所得链接排期第二轮，依次类推。
     * @param r
     * @return
     */
    def rounds(int r){
        spider.maxRoundCount = r
    }

    /**
     * 最多抓取页面数量
     * @param m
     * @return
     */
    def maxFetch(int m){
        spider.maxFetchCount = m
    }

    /**
     * 开启的线程数
     * @param t
     * @return
     */
    def thread(int t){
        spider.maxThreadCount = t
    }

    /**
     * 设置处理器，默认处理所有页面。
     * @param closure
     * @return
     */
    def handle(Closure closure){
        handle(".*",closure)
    }

    /**
     * 设置处理器，对符合规则的页面处理。
     * @param pattern
     * @param closure
     * @return
     */
    def handle(String pattern,Closure closure){
        spider.handlers.put(Pattern.compile(pattern),closure)
    }

    /**
     * 自定义下载器，接收一个 url string，经过自定义处理，下载页面内容，并返回内容为一个 string 。
     * @param closure
     * @return
     */
    def download(Closure closure){
        spider.downloader = closure
    }

    /**
     * 回顾 Page
     * 闭包接收一个 Page 参数，这是处理结束后，最后一次对 page 处理。
     * 即便线程内因为异常无法正常处理完成，这里也会处理到。
     * @param closure
     * @return
     */
    def review(Closure closure){
        spider.reviewPage = closure
    }

    /**
     * 默认的解码字符集
     * 注意，这里只影响默认的 downloader ，自定义的 downloader 需自行处理
     * @param charset
     * @return
     */
    def defaultCharset(String charset){
        spider.defaultChaset = charset
    }

    /**
     * 参见 groovy jdk URL.getText(parameters) 文档
     * 注意，这里只影响默认的 downloader ，自定义的 downloader 需自行处理
     * @param map
     * @return
     */
    def defaultParameters(Map map){
        spider.defaultParameters << map
    }

    def acceptCookies(boolean accept){
        spider.acceptCookies = accept
    }
}

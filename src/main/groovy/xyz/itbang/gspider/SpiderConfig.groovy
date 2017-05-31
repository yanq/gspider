package xyz.itbang.gspider

import xyz.itbang.gspider.handler.AbstractHandler

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
     * 定义抓取的名称前缀，每次抓取会生成一个名称@时间的抓取名称。
     * @param name
     * @return
     */
    def name(String name){
        spider.crawlName = name
    }

    /**
     * 定义种子页面
     * @param urls
     * @return
     */
    def seeds(String ...urls){
        spider.getRoundLinkSet(1).addAll(urls)
    }

    def seeds(List urls){
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
        spider.handlerList.add(new AbstractHandler(pattern) {
            @Override
            Page handlePage(Page page) {
                closure.call(page)
                return page
            }
        })
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
     * 抓取完后，总体回顾一下
     * @param closure
     * @return
     */
    def reviewCrawl(Closure closure){
        spider.reviewCrawl = closure
    }
}

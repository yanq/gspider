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

    def seed(String ...urls){
        spider.getRoundLinkSet(1).addAll(urls)
    }
    def includeOutSite(boolean b){
        spider.includeOutSite = b
    }
    def include(String... pattern){
        pattern.each {
            spider.includeRegexList.add(Pattern.compile(it))
        }
    }
    def exclude(String... pattern){
        pattern.each {
            spider.excludeRegexList.add(Pattern.compile(it))
        }
    }
    def rounds(int r){
        spider.maxRoundCount = r
    }
    def maxFetch(int m){
        spider.maxFetchCount = m
    }
    def thread(int t){
        spider.maxThreadCount = t
    }
    def handle(Closure closure){
        handle(".*",closure)
    }
    def handle(String pattern,Closure closure){
        spider.handlers.put(Pattern.compile(pattern),closure)
    }
    def download(Closure closure){
        spider.downloader = closure
    }
    def review(Closure closure){
        spider.reviewPage = closure
    }
}

package xyz.itbang.gspider

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult
import xyz.itbang.gspider.download.DefaultDownloader
import xyz.itbang.gspider.download.Downloader

/**
 * Page 涵盖了一个链接在抓取和处理过程中的所有数据，用于多个过程中的共享，并提供了格式的转换。
 * Created by yan on 2017/2/13.
 */
@Slf4j
class Page {
    String url = ''
    String text = ''
    boolean fail = false
    List<String> links = new ArrayList<>()
    Map<String, Object> data = new HashMap<>()
    Date startAt, downloadEndAt, endAt
    //download
    int currentRound = 1
    Downloader downloader
    int failRetryCount = 1
    // for cache read only properties
    private URI _uri
    private GPathResult _html
    private Object _json

    String getText(){
        if (text) return text
        download()
        return text
    }

    String getHost(){
        "${uri.scheme}://${uri.host}${uri.port>0 ? ':'+uri.port : ''}"
    }

    URI getUri() {
        _uri ?: (_uri = new URI(url))
    }

    GPathResult getHtml(){
        if (!_html) {
            log.trace("Transform text to html for page ${url}")

            def parser = new org.ccil.cowan.tagsoup.Parser()
            parser.setFeature("http://xml.org/sax/features/namespaces", false)
            def slurper = new XmlSlurper(parser)
            try {
                _html = slurper.parseText(getText())
            }catch (Exception e){
                _html = slurper.parseText("")
                e.printStackTrace()
            }
        }
        return _html
    }

    Object getJson() {
        if (!_json){
            log.trace("Transform text to json for page ${url}")

            JsonSlurper jsonSlurper = new JsonSlurper()
            try {
                _json = jsonSlurper.parseText(getText())
            }catch (Exception e){
                _json = jsonSlurper.parseText('{}')
                e.printStackTrace()
            }
        }
        return _json
    }


    void markAsFailed() {
        fail = true
    }

    void clearStatus() {
        fail = false
        _uri = null
        _html = null
        _json = null
    }

    void download(){
        downloader = downloader ?: new DefaultDownloader()

        startAt = new Date()
        try {
            text = downloader.download(url)
        }catch (Exception e){
            e.printStackTrace()
            //重试，并设置状态
            for (int i = 0; i < failRetryCount; i++) {
                log.warn("Download fail,retry ${i+1}")
                clearStatus()
                try {
                    text = downloader.download(url)
                    break
                }catch (Exception ea){
                    ea.printStackTrace()
                    markAsFailed()
                    continue
                }
            }
        }
        downloadEndAt = new Date()
    }

    String toString() {
        "${currentRound} : $url"
    }
}

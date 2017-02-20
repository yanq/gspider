package xyz.itbang.gspider

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult

/**
 * Page 涵盖了一个链接在抓取和处理过程中的所有数据，用于多个过程中的共享，并提供了格式的转换。
 * Created by yan on 2017/2/13.
 */
@Slf4j
class Page {
    int round = 1
    String url = ''
    String text = ''
    boolean fail = false
    private URI _uri
    private GPathResult _html
    private Object _json
    List<String> links = new ArrayList<>()
    Map<String,Object> data = new HashMap<>()
    Date createAt = new Date()
    Date startAt,downloadedAt,endAt

    void markAsFailed(){
        fail = true
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
                _html = slurper.parseText(text)
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
                _json = jsonSlurper.parseText(text)
            }catch (Exception e){
                _json = jsonSlurper.parseText('{}')
                e.printStackTrace()
            }
        }
        return _json
    }

    String toString(){
        "${round} : $url"
    }
}

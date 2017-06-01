package xyz.itbang.gspider

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Page 涵盖了一个链接在抓取和处理过程中的所有数据，用于多个过程中的共享，并提供了格式的转换。
 * Created by yan on 2017/2/13.
 */
@Slf4j
class Page implements Serializable{
    String crawlName
    String url = ''
    String text = ''
    boolean fail = false
    List<String> links = new ArrayList<>()
    Map<String, Object> data = new HashMap<>()
    Date startAt,downloadStartAt, downloadEndAt, endAt
    long downloadTime //耗时，毫秒
    //download
    int currentRound = 1
    int failRetryCount = 1
    // for cache read only properties
    private transient Connection _connection
    private transient URI _uri
    private transient Document _document
    private transient Object _json
    private _id

    Page(){}

    Page(String crawlName,int currentRound,String url){
        this.crawlName = crawlName
        this.currentRound = currentRound
        this.url = url
        this.startAt = new Date()
    }

    String getId(){
        _id ?: (_id = UUID.randomUUID().toString())
    }

    String getText(){
        if (text) return text
        download()
        return text
    }

    String getHost(){
        "${uri.scheme}://${uri.host}${uri.port>0 ? ':'+uri.port : ''}"
    }

    Connection getConnection(){
        _connection ?: (_connection = Jsoup.connect(url))
    }

    URI getUri() {
        _uri ?: (_uri = new URI(url))
    }

    Document getDocument(){
        _document ?: (_document = Jsoup.parse(getText()))
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
        _document = null
        _json = null
    }

    void download(){
        startDownload()
        try {
            text = connection.execute().body()
        }catch (Exception e){
            e.printStackTrace()
            //重试，并设置状态
            for (int i = 0; i < failRetryCount; i++) {
                log.warn("Download fail,retry ${i+1}")
                clearStatus()
                try {
                    text = connection.execute().body()
                    break
                }catch (Exception ea){
                    ea.printStackTrace()
                    markAsFailed()
                    continue
                }
            }
        }
        overDownload()
    }

    void startDownload(){
        downloadStartAt = new Date()
    }
    void overDownload(){
        downloadEndAt = new Date()
        downloadTime = downloadEndAt.time - downloadStartAt.time
    }

    String toString() {
        "${currentRound} : $url"
    }
}

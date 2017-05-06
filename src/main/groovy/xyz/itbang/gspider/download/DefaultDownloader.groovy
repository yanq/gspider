package xyz.itbang.gspider.download

/**
 * default downloader implement.
 * Created by yan on 2017/5/6 0006.
 */
class DefaultDownloader implements Downloader {

    Map<String, Object> defaultParameters = [connectTimeout: 5000, readTimeout: 5000]
    String defaultCharset = "UTF-8"
    boolean acceptCookies = false

    DefaultDownloader() {
        this(null,null,false)
    }

    DefaultDownloader(Map headers, String charset, Boolean acceptCookies) {
        if (headers) this.defaultParameters << [requestProperties:headers]
        if (charset) this.defaultCharset = charset
        if (acceptCookies) setCookieHandler();
    }


    @Override
    String download(String url) {
        return new URL(url).getText(defaultParameters, defaultCharset)
    }

    private void setCookieHandler() {
        CookieManager cookieManager = new CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        cookieManager.setDefault(cookieManager)
    }
}

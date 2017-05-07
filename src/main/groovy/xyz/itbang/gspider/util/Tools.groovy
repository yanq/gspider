package xyz.itbang.gspider.util

/**
 * Created by yan on 2017/5/7 0007.
 */
class Tools {

    /**
     * 设置接受Cookie。
     * 注意：这里只影响 Java 平台的网络 API 及使用其的框架。如 HttpURLConnection，Jsoup。
     */
    static void setAcceptAllCookies(){
        CookieManager cookieManager = new CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        cookieManager.setDefault(cookieManager)
    }
}

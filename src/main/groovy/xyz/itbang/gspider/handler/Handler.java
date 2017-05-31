package xyz.itbang.gspider.handler;

import xyz.itbang.gspider.Page;

/**
 * 页面处理器接口，为特定的页面提供处理器。
 * 包括从下载，到分析数据等所有过程。
 * Created by yan on 2017/5/28.
 */
public interface Handler {
    String getPattern();
    Boolean matches(String url);
    Page handle(Page page);
}

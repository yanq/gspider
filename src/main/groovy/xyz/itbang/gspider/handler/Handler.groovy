package xyz.itbang.gspider.handler;

import xyz.itbang.gspider.Page;

/**
 * 处理器
 * Created by yan on 2017/5/28.
 */
public interface Handler {
    String getPattern();
    Boolean matches(String url);
    Page handle(Page page);
}

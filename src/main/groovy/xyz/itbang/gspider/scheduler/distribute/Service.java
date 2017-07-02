package xyz.itbang.gspider.scheduler.distribute;

import xyz.itbang.gspider.Page;

import java.util.Map;

/**
 * Created by yan on 2017/5/31.
 */
public interface Service {
    Page getTask(Map params);
    String postTask(Page page);
}

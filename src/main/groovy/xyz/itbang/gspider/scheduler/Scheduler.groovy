package xyz.itbang.gspider.scheduler;

import xyz.itbang.gspider.Page
import xyz.itbang.gspider.Spider;
import xyz.itbang.gspider.handler.Handler;

import java.util.List;
import java.util.Set;

/**
 * 调度器
 * Created by yan on 2017/5/31.
 */
public interface Scheduler {
    void ship(Spider spider);
}

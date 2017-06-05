package xyz.itbang.gspider.scheduler;

import xyz.itbang.gspider.Page;
import xyz.itbang.gspider.handler.Handler;

import java.util.List;
import java.util.Set;

/**
 * Created by yan on 2017/5/31.
 */
public interface Scheduler {
    List<Page> dealRoundLinks(String crawlName, int round, Set<String> links);
    void shutdown();
}

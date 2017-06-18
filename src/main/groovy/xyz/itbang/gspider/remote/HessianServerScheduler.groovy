package xyz.itbang.gspider.remote

import com.caucho.hessian.server.HessianServlet
import groovy.util.logging.Slf4j
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import xyz.itbang.gspider.Page
import xyz.itbang.gspider.Spider
import xyz.itbang.gspider.scheduler.Scheduler

/**
 * 基于 Hessian 的调度器，server 端。
 * Created by yan on 2017/5/31.
 */
@Slf4j
class HessianServerScheduler implements Scheduler {
    static Spider spider
    static LinkedList<String> toDealLinks
    static int remainCount
    Server server

    HessianServerScheduler(Spider spider) {
        this.spider = spider
        startService(spider.serviceURL)
    }

    @Override
    void dealRoundLinks(String crawlName, int round, Set<String> links) {
        toDealLinks = Collections.synchronizedCollection(new LinkedList<String>())
        remainCount = links.size()
        links.each {
            toDealLinks.add(it.toString())
        }

        //一直等，直到都被处理，在等一会儿，就结束
        while (true) {
            if (toDealLinks.size() > 0) {
                sleep(1000)
                continue
            }
            sleep(2000)
            if (remainCount < 1) {
                break
            }
            log.info("Waiting for last pages,just 10s ...")
            sleep(10000)
            break
        }

        log.info("Round ${round} done, links : ${links.size()}")
    }

    @Override
    void shutdown() {
        server.stop()
    }

    void startService(String url) {
        URI uri = new URI(url)
        server = new Server(uri.port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        server.setHandler(context);
        context.addServlet(HessianService.class, uri.path)
        server.start()
    }

    public static class HessianService extends HessianServlet implements Service {

        @Override
        Page getTask(Map params) {
            String link = toDealLinks.poll()
            link ? new Page(spider.crawlName, spider.round, link) : null
        }

        @Override
        String postTask(Page page) {
            page.endAt = new Date()
            remainCount --
            try {
                spider.reviewPage?.call(page)
                spider.parserLinks(page)
                return 'success'
            } catch (Exception e) {
                e.printStackTrace()
                return 'fail'
            }
        }
    }
}

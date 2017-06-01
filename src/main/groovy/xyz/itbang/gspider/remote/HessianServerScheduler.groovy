package xyz.itbang.gspider.remote

import com.caucho.hessian.server.HessianServlet
import groovy.util.logging.Slf4j
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import xyz.itbang.gspider.Page
import xyz.itbang.gspider.scheduler.Scheduler

/**
 * 基于 Hessian 的调度器，server 端。
 * Created by yan on 2017/5/31.
 */
@Slf4j
class HessianServerScheduler implements Scheduler {
    Server server
    static LinkedList<Page> toDealLinks
    static List<Page> toDealPages

    @Override
    List<Page> dealRoundLinks(String crawlName, int round, Set<String> links) {
        toDealLinks = Collections.synchronizedCollection(new LinkedList<Page>())
        toDealPages = Collections.synchronizedList(new ArrayList<Page>())

        links.each {
            toDealLinks.add(new Page(crawlName,round,it.toString()))
        }

        //一直等，直到都被处理，在等一会儿，就结束
        while (true){
            if (toDealLinks.size()>0){
                sleep(1000)
                continue
            }
            sleep(2000)
            if (toDealPages.size() >= links.size()) {
                break
            }
            log.info("Waiting for last pages,just 10s ...")
            sleep(10000)
            break
        }

        log.info("Deal round links ${links.size()},result pages ${toDealPages.size()}")
        return toDealPages
    }

    void startService(String url) {
        URI uri = new URI(url)
        server = new Server(uri.port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        server.setHandler(context);
        context.addServlet(HessianService.class, "/service")
        server.start()
    }

    public static class HessianService extends HessianServlet implements Service {

        @Override
        Page getTask(Map params) {
            return toDealLinks.poll()
        }

        @Override
        String postTask(Page page) {
            toDealPages.add(page)
            return 'success'
        }
    }
}

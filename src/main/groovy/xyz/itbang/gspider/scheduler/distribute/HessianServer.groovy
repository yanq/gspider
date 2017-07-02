package xyz.itbang.gspider.scheduler.distribute

import com.caucho.hessian.server.HessianServlet
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import xyz.itbang.gspider.Page

/**
 * 服务器，基于 Hessian。
 * Created by yan on 2017/7/2.
 */
class HessianServer {
    Server server
    String url

    HessianServer(String url){
        this.url = url
    }

    void shutdown() {
        server.stop()
    }

    void start() {
        URI uri = new URI(this.url)
        server = new Server(uri.port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        server.setHandler(context);
        context.addServlet(HessianService.class, uri.path)
        server.start()
    }

    public static class HessianService extends HessianServlet implements Service {

        @Override
        Page getTask(Map params) {
            return DataCenter.get()
        }

        @Override
        String postTask(Page page) {
            try {
                DataCenter.put(page)
                return 'success'
            } catch (Exception e) {
                e.printStackTrace()
                return 'fail'
            }
        }
    }
}

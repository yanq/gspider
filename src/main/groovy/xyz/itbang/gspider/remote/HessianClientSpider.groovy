package xyz.itbang.gspider.remote

import com.caucho.hessian.client.HessianProxyFactory
import groovy.util.logging.Slf4j
import xyz.itbang.gspider.Page
import xyz.itbang.gspider.handler.Handler

/**
 * Created by yan on 2017/5/31.
 */
@Slf4j
class HessianClientSpider {
    HessianProxyFactory factory = new HessianProxyFactory();
    String url = "http://localhost:8080/service"
    List<Handler> handlerList
    Service service

    HessianClientSpider(String url,List<Handler> handlerList){
        this.url = url
        this.handlerList = handlerList
    }

    void process(){
        service = (Service) factory.create(Service, url);

        Page page = service.getTask(null)

        if (page){
            log.info("Get page $page")

            handlerList.each {
                if (it.matches(page.url)) it.handle(page)
            }
            def result = service.postTask(page)

            log.info("Post task ${page} , $result")
        }else {
            throw new Exception("No page found.")
        }
    }
}

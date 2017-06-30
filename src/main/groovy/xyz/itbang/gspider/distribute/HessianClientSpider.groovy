package xyz.itbang.gspider.distribute

import com.caucho.hessian.client.HessianProxyFactory
import groovy.util.logging.Slf4j
import xyz.itbang.gspider.Page
import xyz.itbang.gspider.Spider

/**
 * Created by yan on 2017/5/31.
 */
@Slf4j
class HessianClientSpider {
    Spider spider
    HessianProxyFactory factory = new HessianProxyFactory();
    Service service

    HessianClientSpider(Spider spider){
        this.spider = spider
    }

    void process(){
        service = (Service) factory.create(Service, spider.serviceURL);

        Page page = service.getTask(null)

        if (page){
            log.info("Get task : $page")

            spider.handlerList.each {
                if (it.matches(page.url)) it.handle(page)
            }

            def result = service.postTask(page)
            log.info("Post task : ${page} , $result")
        }else {
            throw new Exception("No page found.")
        }
    }
}

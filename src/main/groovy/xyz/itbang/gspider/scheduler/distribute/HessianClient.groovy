package xyz.itbang.gspider.scheduler.distribute

import com.caucho.hessian.client.HessianProxyFactory
import groovy.util.logging.Slf4j
import xyz.itbang.gspider.Page
import xyz.itbang.gspider.Spider

/**
 * Created by yan on 2017/5/31.
 */
@Slf4j
class HessianClient {
    HessianProxyFactory factory = new HessianProxyFactory();
    Service service

    void start(Spider spider) {
        log.info("Starting hessian client for ${spider.name},${spider.maxThreadCount} thread ")

        spider.maxThreadCount.times {
            def t = new Thread(new Runnable() {
                @Override
                void run() {
                    Date startWaiting
                    int idleTime = 1000
                    while (true) {
                        try {
                            service = (Service) factory.create(Service, spider.serviceURL);

                            Page page = service.getTask(null)

                            if (page) {
                                log.info("Get task : $page ${startWaiting ? ', stop waiting status.' :''}")
                                startWaiting = null

                                spider.handlerList.each {
                                    if (it.matches(page.url)) it.handle(page)
                                }

                                def result = service.postTask(page)
                                log.info("Post task : ${page} , $result")
                            } else {
                                startWaiting = startWaiting ?: new Date()
                                log.info("Get no page todo,start waiting to exit ...")
                                sleep(idleTime)
                            }
                        } catch (Exception e) {
                            startWaiting = startWaiting ?: new Date()
                            log.warn("Exception : ${e.getMessage()},start waiting to exit ...")
                            e.printStackTrace()
                            sleep(idleTime)
                        }

                        //开始计时等
                        if (startWaiting){
                            def waiting = new Date().time - startWaiting.time
                            if (waiting > spider.maxClientWaitingTime){
                                log.warn("Time over, waited ${waiting/1000}s , exit ---")
                                break
                            }
                        }
                        sleep(100)
                    }
                }
            })
            t.start()
        }
    }
}

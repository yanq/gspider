//package xyz.itbang.gspider.scheduler.distribute
//
//import com.caucho.hessian.client.HessianProxyFactory
//import groovy.util.logging.Slf4j
//import xyz.itbang.gspider.Page
//import xyz.itbang.gspider.Spider
//
///**
// * Created by yan on 2017/5/31.
// */
//@Slf4j
//class HessianClientSpider {
//    Spider spider
//    HessianProxyFactory factory = new HessianProxyFactory();
//    Service service
//
//    HessianClientSpider(Spider spider){
//        this.spider = spider
//    }
//
//    void process(){
//        service = (Service) factory.create(Service, spider.serviceURL);
//
//        Page page = service.getTask(null)
//
//        if (page){
//            log.info("Get task : $page")
//
//            spider.handlerList.each {
//                if (it.matches(page.url)) it.handle(page)
//            }
//
//            def result = service.postTask(page)
//            log.info("Post task : ${page} , $result")
//        }else {
//            throw new Exception("No page found.")
//        }
//    }
//
//    void startClient(){
//        log.info("Starting client spider,${maxThreadCount} thread ")
//
//        HessianClientSpider clientSpider = new HessianClientSpider(this)
//        int idleCount = 0
//        int maxIdleCount = 10
//        int idleSleepTime = 3000
//        maxThreadCount.times {
//            def t = new Thread(new Runnable() {
//                @Override
//                void run() {
//                    while (true){
//                        try {
//                            clientSpider.process()
//                            idleCount = 0
//                        }catch (Exception e){
//                            log.warn("Exception : ${e.getMessage()} ,sleep ${idleSleepTime} ...")
//                            sleep(idleSleepTime)
//                            idleCount = idleCount+1
//                        }
//
//                        //println("idle count $idleCount")
//                        if (idleCount >= maxIdleCount*maxThreadCount){
//                            log.info("Client spider idle too long ,about ${(idleSleepTime*idleCount)/maxThreadCount} ms,it will shutdown now .")
//                            break
//                        }
//                    }
//                }
//            })
//            t.start()
//        }
//    }
//}

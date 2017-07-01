package xyz.itbang.gspider.util

import java.util.concurrent.Future

/**
 * 计时的 Future
 * Created by yan on 2017/6/30.
 */
class TimedFuture {
    String name
    Date createAt = new Date()
    int maxTime //最大时间，毫秒
    Future future

    TimedFuture(String name, Future future, int maxTime) {
        this.name = name
        this.future = future
        this.maxTime = maxTime
    }

    boolean done() {
        return future.done
    }

    boolean overTime() {
        (new Date().time - createAt.time) >= maxTime
    }

    boolean cancel(boolean mayInterruptIfRunning){
        future.cancel(mayInterruptIfRunning)
    }

    int time(){
        new Date().time - createAt.time
    }
}

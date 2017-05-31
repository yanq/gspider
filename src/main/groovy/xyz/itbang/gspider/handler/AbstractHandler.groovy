package xyz.itbang.gspider.handler

import xyz.itbang.gspider.Page

/**
 * 抽象处理器。实现模板方法。
 * Created by yan on 2017/5/28.
 */
abstract class AbstractHandler implements Handler {

    String pattern = '.*'

    AbstractHandler(String pattern){
        this.pattern = pattern
    }

    @Override
    String getPattern() {
        return pattern
    }

    /**
     * 匹配测试
     * @param url
     * @return
     */
    @Override
    Boolean matches(String url) {
        return url?.matches(getPattern()) //这里需要通过调用子类方法获取子类的 pattern.
    }

    @Override
    Page handle(Page page) {
        if (!matches(page.url)) {
            page.markAsFailed()
            page.data.put('failMessage', "Not support this url by ${this.class}")
            return page
        }

        handlePage(page)
        page.endAt = new Date()

        return page
    }

    abstract Page handlePage(Page page)
}

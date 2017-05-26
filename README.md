# GSpider

a Groovy Spider,make crawl easy.

一个基于 Groovy 语言的 Spider，让抓取更简单。

## Feature 特性
- easy to use,support crawl dsl. 简单易用，支持抓取DSL。
- easy to manipulating html,by transform to jsoup document or json or just text. 响应结果可转换为 jsoup document，json 等，方便处理，也可以直接处理原文。
- out of box of multithread,text clean,url filter and so on. 开箱即用的多线程，文本分析清洗，过滤URL等等。

## Example 示例
```
        Spider.crawl {
            seeds "http://www.luoyouzhijia.cn/"
            handle { Page page ->
                println("Handle -> " + page.url)
                println("Title -> " + page.document.title())
            }
        }
```
[more example 更多示例](https://github.com/yanq/gspider-example)
> **Warning 注意**
> 
> Do not fetch too much before change the seeds，just for test。
> 
> 别抓太多哈，这个网站可是我亲儿子，别整坏了。

## How to use  咋用
Gradle code
```
compile 'xyz.itbang:gspider:1.2'
```

## Something to say 吹点牛
If you can not read chinese，nothing important，just to have a cup of tea。someday，i'll say it again in English。

因故需要爬取点东西，考察了 Java 领域的几个爬虫工具或框架。不甚满意。

nutch，一个伟大的先驱，诞生了一批金光闪闪的小弟，自己却似乎没落了，比着官方文档，也跑不起来，文档很不全，且多年失修。

WebMagic，那哥们说要搞成 Java 界的教科书式爬虫，比肩 Scrapy ，良好的文档，受益匪浅。不过，我觉着某些设计太复杂了，封装太多就容易不太灵活，做的太多，就容易跨越界限。

拜过两位大神之后，想，还是自己搞一个吧。于是 GSpider 诞生了。

示例大家已经看过了，市面上恐怕没有更简单易用强大的爬虫了吧。托Groovy的福，闭包，DSL，GPath等，让一切变得简单强大。

整个项目耗时大概两周，了解行业知识以及写代码，整个项目大概200多行（不算注释），实现了两位前辈的主要功能，功能上更像 nutch 一点。

真搞出来还有点小兴奋，想整点82年的拉菲压压，也没有找到，谁有？送点？

吹完牛了，该谦虚谨慎地干活了。

欢迎大家点赞，fork，欢迎意见建议，我的微信 jackyanq，靠这个赚了钱的，欢迎打赏。


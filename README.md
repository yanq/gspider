# GSpider

a Java/Groovy Spider,make crawl easy.

一个基于 Java/Groovy 的 Spider，让抓取更简单。

## Feature 特性
- easy to use,support crawl DSL. 易用，支持抓取DSL。
- easy to manipulating html,by transform to jsoup document or json or just text. 操作网页简单，内置可用text, jsoup document,json 等格式。
- config like nutch. 像使用 nutch 一样。配置种子，线程数，抓取数，页面范围（包含和排除），处理器等，就可启动了。
- develop use java or groovy. 可用使用 Java 或者 Groovy 开发。当然也可用于任何JVM支持的语言。
- deploy alone，distribute，and support multi-spider。 可独立部署，分布式部署，并支持多爬虫。

## Example 示例
This is a Groovy script example. 这是一个 Groovy 脚本示例。
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
compile 'xyz.itbang:gspider:3.0.0'
```

## Something to say 吹点牛
If you can not read chinese，nothing important，just to have a cup of tea。someday，I'll say it again in English。

因故需要爬取点东西，考察了 Java 领域的几个爬虫工具或框架。不甚满意。

nutch，一个伟大的先驱，诞生了一批金光闪闪的小弟，自己却似乎没落了，比着官方文档，也跑不起来，文档很不全，且多年失修。

WebMagic，那哥们说要搞成 Java 界的教科书式爬虫，比肩 Scrapy ，良好的文档，受益匪浅。不过，我觉着某些设计太复杂了，封装太多就容易不太灵活，做的太多，就容易跨越界限。

拜过两位大神之后，想，还是自己搞一个吧。于是 GSpider 诞生了。

示例大家已经看过了，市面上恐怕没有更简单易用强大的爬虫了吧。托Groovy的福，闭包，DSL，GPath等，让一切变得简单强大。

整个项目耗时大概两周，了解行业知识以及写代码，整个项目大概200多行（不算注释），实现了两位前辈的主要功能，功能上更像 nutch 一点。

真搞出来还有点小兴奋，想整点82年的拉菲压压，也没有找到，谁有？送点？

吹完牛了，该谦虚谨慎地干活了。

欢迎大家点赞，fork，欢迎意见建议，我的微信 jackyanq，靠这个赚了钱的，欢迎打赏。


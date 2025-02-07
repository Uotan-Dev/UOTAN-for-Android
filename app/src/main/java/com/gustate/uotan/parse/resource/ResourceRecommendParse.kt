package com.gustate.uotan.parse.resource

import com.gustate.uotan.parse.home.FetchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URL

data class ResourceRecommendItem(
    val cover:  String,
    val topic: String,
    val title: String,
    val version: String,
    val updateTime: String,
    val author: String,
    val downloadCount: String,
    val price: String,
    val link: String
)

// 获取柚坛社区资源库推荐的类
class ResourceRecommendParse {

    // 伴生对象
    companion object {

        // 一个协程方法，用于获取分析柚坛社区的 Document, 返回上面那个数据类的 MutableList
        /*suspend fun fetchResourceRecommendData(): MutableList<ResourceRecommendItem> = withContext(Dispatchers.IO) {

            // 设置一个变量存储柚坛社区的网址
            val basicUrl = "https://www.uotan.cn/"

            try {

                // 获取资源库推荐的 Document
                val document = Jsoup.parse(URL("https://www.uotan.cn/resources/featured"),30000)

                /*
                 *  爬取资源库推荐
                 *  @JiaGuZhuangZhi (LOVE JIANGXUN)
                 *  感谢 Jsoup 项目
                 */

                val element = document.getElementsByClass("structItemContainer").first()
                val coverElements = element.getElementsByTag("img")


            } finally {

            }

            return

        }*/

    }

}

fun main() {
    fetchResourceRecommendData()
}

fun fetchResourceRecommendData() {

    val result = mutableListOf<ResourceRecommendItem>()

    // 设置一个变量存储柚坛社区的网址
    val basicUrl = "https://www.uotan.cn/"
    // 获取资源库推荐的 Document
    val document = Jsoup.parse(URL("https://www.uotan.cn/resources/featured"),30000)

    /*
     *  爬取资源库推荐
     *  @JiaGuZhuangZhi (LOVE JIANGXUN)
     *  感谢 Jsoup 项目
     */

    val element = document.getElementsByClass("structItemContainer").first()
    val coverElements = element!!.getElementsByTag("img")

    for (coverElement in coverElements) {

        //result.add(ResourceRecommendItem(cover = "basicUrl + coverElement.attr(\"src\")"))

    }

}
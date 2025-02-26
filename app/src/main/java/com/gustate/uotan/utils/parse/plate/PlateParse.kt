package com.gustate.uotan.utils.parse.plate

import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.IOException

data class PlateItem(
    val cover:  String,
    val title: String,
    val link: String
)

class PlateParse {

    // 伴生对象
    companion object {

        // 协程函数
        suspend fun fetchPlateData(content: String): MutableList<PlateItem> = withContext(Dispatchers.IO) {

            try {
                // 我的关注  小米手机  红米手机  爬取
                if (content.startsWith("/watched/")) {

                    // 获取关注板块的网页 document 文档
                    val document = Jsoup.connect(BASE_URL + content)
                        .userAgent(USER_AGENT)
                        .timeout(TIMEOUT_MS)
                        .cookies(Cookies)
                        .get()

                    val rootElement = document.getElementsByClass("block-body").first()

                    val itemElements = rootElement!!.select("div.node--forum")

                    return@withContext fetchContent(itemElements)

                } else if (content.startsWith("/categories/")) {

                    // 获取关注板块的网页 document 文档
                    val document = Jsoup.connect(BASE_URL + content)
                        .userAgent(USER_AGENT)
                        .timeout(TIMEOUT_MS)
                        .cookies(Cookies)
                        .get()

                    val rootElement = document.getElementsByClass("block-body").first()

                    val itemElements = rootElement!!.select("div.node--forum")

                    return@withContext fetchContent(itemElements)

                } else {

                    // 获取关注板块的网页 document 文档
                    val document = Jsoup.connect(BASE_URL + "/forums/")
                        .userAgent(USER_AGENT)
                        .timeout(TIMEOUT_MS)
                        .cookies(Cookies)
                        .get()

                    val rootElement = document.getElementsByClass("block block--category block--category" + content).first()

                    val bodyElement = rootElement!!.getElementsByClass("block-body").first()

                    val itemElements = bodyElement!!.getElementsByTag("div")

                    return@withContext fetchContent(itemElements)

                }
            } catch (e: HttpStatusException) {
                // 专门处理 HTTP 状态异常
                return@withContext mutableListOf<PlateItem>()
            } catch (e: IOException) {
                // 处理网络异常
                return@withContext mutableListOf<PlateItem>()
            } catch (e: Exception) {
                // 兜底异常处理
                return@withContext mutableListOf<PlateItem>()
            }
        }

        private fun fetchContent(itemElements: Elements): MutableList<PlateItem> {

            val result = mutableListOf<PlateItem>()

            for (itemElement in itemElements) {

                val aElements = itemElement.getElementsByTag("a")

                // 版块图标
                val cover = BASE_URL + aElements[0].getElementsByTag("img").attr("src")

                // 版块文字
                val title = aElements[1].text()

                // 版块链接
                val url = BASE_URL + aElements[1].attr("href")

                result.add(PlateItem(cover, title, url))

            }

            return result

        }

    }

}

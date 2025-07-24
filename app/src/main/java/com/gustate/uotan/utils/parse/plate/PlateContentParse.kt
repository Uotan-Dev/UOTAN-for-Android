package com.gustate.uotan.utils.parse.plate

import com.gustate.uotan.utils.Utils.Companion.baseUrl
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

data class PlateContentData(
    val describe: String,
    val totalPage: Int,
    val topItemsList: MutableList<PlateContentItem>,
    val normalItemsList: MutableList<PlateContentItem>,
    val pageUrl: String
)

data class PlateContentItem(
    val title: String,
    val cover: String,
    val userName: String,
    val id: String,
    val time: String,
    val prefix: String,
    val link: String,
    val views: String,
    val comments: String
)

class PlateContentParse {
    companion object {
        suspend fun fetchPlateContentData(url: String): PlateContentData = withContext(Dispatchers.IO) {
            try {
                val document = Jsoup
                    .connect(baseUrl + url)
                    .cookies(Cookies)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .get()
                val totalPage = document
                    .getElementsByClass("pageNav-main")
                    .first()
                    ?.getElementsByTag("li")
                    ?.last()
                    ?.getElementsByTag("a")
                    ?.first()
                    ?.text()
                    ?: "1"
                val pageUrl = document
                    .getElementsByClass("pageNav-main")
                    .first()
                    ?.getElementsByTag("li")
                    ?.last()
                    ?.getElementsByTag("a")
                    ?.first()
                    ?.attr("href")
                    ?.replace(totalPage, "")
                    ?: ""
                val describe = document
                    .getElementsByClass("p-description")
                    .first()
                    ?.text()
                    ?: ""
                val topItemsElements = document
                    .getElementsByClass("uix_stickyContainerOuter  is-active")
                    .first()
                    ?.select("div.structItem")
                val normalItemsElements = document
                    .getElementsByClass("structItemContainer-group js-threadList")
                    .first()
                    ?.select("div.structItem")

                val topItemList = mutableListOf<PlateContentItem>()
                val normalItemList = mutableListOf<PlateContentItem>()

                topItemsElements?.forEach {
                    topItemList.add(getItem(it))
                }
                normalItemsElements?.forEach {
                    normalItemList.add(getItem(it))
                }
                return@withContext PlateContentData(
                    describe,
                    totalPage.toInt(),
                    topItemList,
                    normalItemList,
                    pageUrl
                )
            } catch (e: Exception) {
                return@withContext PlateContentData(
                    "",
                    1,
                    mutableListOf(),
                    mutableListOf(),
                    ""
                )
            }
        }

        private fun getItem(element: Element?): PlateContentItem {
            val userName = element
                ?.getElementsByClass("structItem-parts")
                ?.first()
                ?.getElementsByClass("username ")
                ?.first()
                ?.text()
                ?: ""
            val id = element
                ?.getElementsByClass("structItem-parts")
                ?.first()
                ?.getElementsByTag("a")
                ?.first()
                ?.attr("data-user-id")
                ?: ""
            val cover = element
                ?.getElementsByClass("structItem-cell structItem-cell--icon")
                ?.first()
                ?.getElementsByTag("img")
                ?.first()
                ?.attr("src")
                ?: ""
            val prefix = element
                ?.getElementsByClass("label label--uotan-threads")
                ?.first()
                ?.text()
                ?: ""
            val title = element
                ?.getElementsByClass("structItem-title")
                ?.first()
                ?.getElementsByTag("a")
                ?.last()
                ?.text()
                ?: ""
            val link = element
                ?.getElementsByClass("structItem-title")
                ?.first()
                ?.getElementsByTag("a")
                ?.last()
                ?.attr("href")
                ?.replace("/unread", "/")
                ?: ""
            val time = element
                ?.getElementsByTag("time")
                ?.first()
                ?.attr("data-date-string")
                ?: ""
            val views = element
                ?.getElementsByClass("pairs pairs--justified structItem-minor")
                ?.first()
                ?.getElementsByTag("dd")
                ?.text()
                ?: ""
            val comments = element
                ?.getElementsByClass("pairs pairs--justified")
                ?.first()
                ?.getElementsByTag("dd")
                ?.text()
                ?: ""
            return PlateContentItem(title, cover, userName, id, time, prefix, link, views, comments)
        }
    }
}
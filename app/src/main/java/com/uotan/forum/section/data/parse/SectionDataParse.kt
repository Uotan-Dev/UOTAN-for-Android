package com.uotan.forum.section.data.parse

import com.uotan.forum.section.data.model.SectionData
import com.uotan.forum.section.data.model.SectionDataItem
import com.uotan.forum.utils.Utils.baseUrl
import com.uotan.forum.utils.Utils.USER_AGENT
import com.uotan.forum.utils.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class SectionDataParse {
    suspend fun parseSectionData(
        url: String,
        onSuccess: (SectionData) -> Unit,
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val client = HttpClient.getClient()
            val request = Request.Builder()
                .url(baseUrl + url)
                .header("User-Agent", USER_AGENT)
                .build()
            val response = client.newCall(request).execute()
            val document = Jsoup.parse(response.body.string())
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

            val topItemList = mutableListOf<SectionDataItem>()
            val normalItemList = mutableListOf<SectionDataItem>()

            topItemsElements?.forEach {
                topItemList.add(parseSectionDataItem(it))
            }
            normalItemsElements?.forEach {
                normalItemList.add(parseSectionDataItem(it))
            }
            withContext(Dispatchers.Main) {
                onSuccess(SectionData(
                    describe,
                    totalPage.toInt(),
                    topItemList,
                    normalItemList,
                    pageUrl
                ))
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onException(e)
            }
        }
    }

    private fun parseSectionDataItem(element: Element?): SectionDataItem {
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
        return SectionDataItem(title, cover, userName, id, time, prefix, link, views, comments)
    }
}
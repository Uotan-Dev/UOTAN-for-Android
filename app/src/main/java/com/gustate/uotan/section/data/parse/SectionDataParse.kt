package com.gustate.uotan.section.data.parse

import com.gustate.uotan.section.data.model.SectionData
import com.gustate.uotan.section.data.model.SectionDataItem
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class SectionDataParse {
    suspend fun parseSectionData(
        url: String,
        onSuccess: (SectionData) -> Unit,
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val document = Jsoup
                .connect(BASE_URL + url)
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
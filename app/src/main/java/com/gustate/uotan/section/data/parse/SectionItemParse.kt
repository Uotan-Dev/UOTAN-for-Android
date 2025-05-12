package com.gustate.uotan.section.data.parse

import com.gustate.uotan.section.data.model.SectionItem
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.select.Elements

class SectionItemParse {
    suspend fun parseSectionItemList(
        section: String,
        onSuccess: (MutableList<SectionItem>) -> Unit,
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            if (section.startsWith("/watched") || section.startsWith("/categories")) {
                val document = Jsoup
                    .connect(BASE_URL + section)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .cookies(Cookies)
                    .get()
                val itemElements = document
                    .getElementsByClass("block-body")
                    .first()
                    ?.select("div.node--forum")
                val sectionItemList = parseSectionItemInfo(itemElements, false)
                withContext(Dispatchers.Main) {
                    onSuccess(sectionItemList)
                }
            } else {
                val document = Jsoup
                    .connect("$BASE_URL/forums/")
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .cookies(Cookies)
                    .get()
                val itemElements = document
                    .getElementsByClass("block block--category block--category$section")
                    .first()
                    ?.getElementsByClass("block-body")
                    ?.first()
                    ?.select("div.block-body > div")
                if (section == "251 ") {
                    val sectionItemList = parseSectionItemInfo(itemElements, true)
                    withContext(Dispatchers.Main) {
                        onSuccess(sectionItemList)
                    }
                } else {
                    val sectionItemList = parseSectionItemInfo(itemElements, false)
                    withContext(Dispatchers.Main) {
                        onSuccess(sectionItemList)
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onException(e)
            }
        }
    }

    private fun parseSectionItemInfo(itemElements: Elements?, del: Boolean): MutableList<SectionItem> {
        val result = mutableListOf<SectionItem>()
        if (itemElements != null) {
            for (itemElement in itemElements) {
                val aElements = itemElement
                    .getElementsByTag("a")
                val cover = aElements[0]
                    .getElementsByTag("img")
                    .attr("src")
                val title = aElements[1]
                    .text()
                val url = aElements[1]
                    .attr("href")
                result.add(SectionItem(cover, title, url))
            }
        }
        if (del) result.removeAt(0)
        return result
    }
}
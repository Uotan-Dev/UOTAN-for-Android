package com.gustate.uotan.utils.parse.resource

import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

data class ResourceItem(
    val cover:  String,
    val topic: String,
    val title: String,
    val version: String,
    val updateTime: String,
    val link: String
)

data class FetchResult(
    val items: MutableList<ResourceItem>,
    val totalPage: Int,
    val nextPageUrl: String
)

data class ResourcePlateItem(
    val plate: String,
    val plateUrl: String,
    val item: MutableList<String>,
    val itemUrl: MutableList<String>
)

class ResourceParse {
    // 伴生对象
    companion object {
        suspend fun fetchResourceData(page: String): FetchResult = withContext(Dispatchers.IO) {
            val result = mutableListOf<ResourceItem>()
            val document = Jsoup
                .connect("$BASE_URL/resources/?page=$page")
                .userAgent(USER_AGENT)
                .cookies(Cookies)
                .timeout(TIMEOUT_MS)
                .get()
            val totalPage = document
                .getElementsByClass("pageNav-main")
                .first()
                ?.getElementsByTag("li")
                ?.last()
                ?.text()
                ?.toInt()
                ?: 1
            val nextPageUrl = document
                .getElementsByClass("pageNav-jump pageNav-jump--next")
                .first()
                ?.attr("href")
                ?: ""
            val structItemContainer = document
                .getElementsByClass("structItemContainer")
                .first()
            val structItems = structItemContainer!!
                .select("div.structItem.structItem--resource")
            for (structItem in structItems) {
                val resImage = structItem
                    .getElementsByClass("structItem-iconContainer")
                    .first()
                    ?.getElementsByClass("avatar avatar--s")
                    ?.first()
                    ?.getElementsByTag("img")
                    ?.attr("src")
                    ?: ""
                val mainCell = structItem
                    .getElementsByClass("structItem-cell structItem-cell--main")
                    .first()
                    ?.getElementsByClass("structItem-title")
                    ?.first()
                val resTag = mainCell
                    ?.getElementsByClass("label label--pack-threads")
                    ?.first()
                    ?.text()
                    ?: ""
                val elementsA = mainCell?.getElementsByTag("a")!!
                var resTitle: String
                var resUrl: String
                if (elementsA.size == 1) {
                    resTitle = elementsA[0].text()
                    resUrl = elementsA[0].attr("href")
                } else {
                    resTitle = elementsA[1].text()
                    resUrl = elementsA[1].attr("href")
                }
                val secCell = structItem
                    .getElementsByClass("structItem-cell structItem-cell--main")
                    .first()
                    ?.getElementsByClass("structItem-resourceTagLine")
                    ?.first()
                val resVersion = secCell
                    ?.getElementsByClass("u-muted")
                    ?.text()
                    ?: ""
                val resUpdateTime = secCell
                    ?.getElementsByTag("time")
                    ?.attr("data-date-string")
                    ?: ""
                result.add(ResourceItem(resImage, resTag, resTitle, resVersion, resUpdateTime, resUrl))
            }
            return@withContext FetchResult(result, totalPage, nextPageUrl)
        }
        suspend fun fetchResourcePlateData(): MutableList<ResourcePlateItem> = withContext(Dispatchers.IO) {
            val result = mutableListOf<ResourcePlateItem>()
            val document = Jsoup
                .connect("$BASE_URL/resources/")
                .userAgent(USER_AGENT)
                .cookies(Cookies)
                .timeout(TIMEOUT_MS)
                .get()
            val categoryLists = document
                .getElementsByClass("categoryList toggleTarget is-active")
                .first()
            val categoryListItems = categoryLists!!
                .getElementsByClass("categoryList-item")
            for (categoryList in categoryListItems) {
                val plateName = categoryList
                    .getElementsByClass("categoryList-itemRow")
                    .first()
                    ?.getElementsByTag("a")
                    ?.first()
                    ?.text()
                    ?: ""
                val plateUrl = categoryList
                    .getElementsByClass("categoryList-itemRow")
                    .first()
                    ?.getElementsByTag("a")
                    ?.first()
                    ?.attr("href")
                    ?: ""
                val items = categoryList
                    .getElementsByTag("div")[1]
                    .getElementsByTag("a")
                val itemNameList = mutableListOf<String>()
                val itemUrlList = mutableListOf<String>()
                for (item in items) {
                    itemNameList.add(item.text())
                    itemUrlList.add(item.attr("href"))
                }
                result.add(ResourcePlateItem(plateName, plateUrl, itemNameList, itemUrlList))
            }
            return@withContext result
        }
    }
}
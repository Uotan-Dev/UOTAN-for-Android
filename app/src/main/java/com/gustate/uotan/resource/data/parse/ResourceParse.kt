package com.gustate.uotan.resource.data.parse

import com.gustate.uotan.resource.data.model.AllResourceFetchResult
import com.gustate.uotan.resource.data.model.ResourceItem
import com.gustate.uotan.resource.data.model.ResourcePlateItem
import com.gustate.uotan.utils.Utils.Companion.baseUrl
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class ResourceParse {

    /**
     * 爬取并解析推荐资源列表
     * @param onSuccess 爬取成功时回调 包含推荐列表
     * @param onThrowable 爬取失败时回调 包含抛出异常
     */
    suspend fun parseResourceRecommendData(
        onSuccess: (List<ResourceItem>) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val result = mutableListOf<ResourceItem>()
            val document = Jsoup
                .connect("$baseUrl/resources/featured")
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get()
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
                val resTitle = mainCell
                    ?.getElementsByTag("a")
                    ?.first()
                    ?.text()
                    ?: ""
                val resUrl = mainCell
                    ?.getElementsByTag("a")
                    ?.first()
                    ?.attr("href")
                    ?: ""
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
                result.add(
                    ResourceItem(resImage, resTag, resTitle, resVersion, resUpdateTime, resUrl))
                withContext(Dispatchers.Main) {
                    onSuccess(result)
                }
            }
        } catch (throwable: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(throwable)
            }
        }
    }

    /**
     * 爬取并解析某类型资源列表
     * @param page 当前页数
     * @param categories 资源类型
     * @param onSuccess 爬取成功时回调 包含资源列表
     * @param onThrowable 爬取失败时回调 包含抛出异常
     */
    suspend fun parseResourceData(
        page: Int,
        categories: String = "",
        onSuccess: (AllResourceFetchResult) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val result = mutableListOf<ResourceItem>()
            val pageContent = if (page == 1) "/" else "/?page=$page"
            val document = Jsoup
                .connect(
                    "$baseUrl/resources${
                        categories.replace("/resources", "").removeSuffix("/")
                    }${pageContent}"
                )
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
                result.add(
                    ResourceItem(
                        resImage,
                        resTag,
                        resTitle,
                        resVersion,
                        resUpdateTime,
                        resUrl
                    )
                )
            }
            withContext(Dispatchers.Main) {
                onSuccess(AllResourceFetchResult(result, totalPage, nextPageUrl))
            }
        } catch (throwable: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(throwable)
            }
        }
    }

    /**
     * 爬取并解析资源类型列表
     * @param onSuccess 爬取成功时回调 包含类型列表
     * @param onThrowable 爬取失败时回调 包含抛出异常
     */
    suspend fun parseResourcePlateData(
        onSuccess: (List<ResourcePlateItem>) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val result = mutableListOf<ResourcePlateItem>()
            val document = Jsoup
                .connect("$baseUrl/resources/")
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
            withContext(Dispatchers.Main) {
                onSuccess(result)
            }
        } catch (throwable: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(throwable)
            }
        }
    }
}
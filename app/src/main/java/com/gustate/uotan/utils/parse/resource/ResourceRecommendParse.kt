package com.gustate.uotan.utils.parse.resource

import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import com.gustate.uotan.utils.Utils.Companion.baseUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

data class ResourceRecommendItem(
    val cover:  String,
    val topic: String,
    val title: String,
    val version: String,
    val updateTime: String,
    val link: String
)

// 获取柚坛社区资源库推荐的类
class ResourceRecommendParse {
    // 伴生对象
    companion object {
        suspend fun fetchResourceRecommendData(): MutableList<ResourceRecommendItem> = withContext(Dispatchers.IO) {
            val result = mutableListOf<ResourceRecommendItem>()
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
                result.add(ResourceRecommendItem(resImage, resTag, resTitle, resVersion, resUpdateTime, resUrl))
            }
            return@withContext result
        }
    }
}
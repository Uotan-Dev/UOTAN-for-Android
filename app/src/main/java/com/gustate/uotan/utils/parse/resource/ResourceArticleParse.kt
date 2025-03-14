package com.gustate.uotan.utils.parse.resource

import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

data class ResourceArticle (
    val title: String,
    val cover: String,
    val device: String,
    val downloadType: String,
    val size: String,
    val password: String,
    val author: String,
    val authorUrl: String,
    val authorId: String,
    val authorAvatar: String,
    val downloadCount: String,
    val viewCount: String,
    val firstPost: String,
    val latestPost: String,
    val downloadUrl: String
)

class ResourceArticleParse {
    companion object {
        suspend fun fetchResourceArticle(url: String): ResourceArticle = withContext(Dispatchers.IO) {
            val document = Jsoup
                .connect("$BASE_URL/$url/")
                .userAgent(USER_AGENT)
                .cookies(Cookies)
                .timeout(TIMEOUT_MS)
                .get()
            val title = document
                .getElementsByClass("p-title-value")
                .first()
                ?.ownText()
                ?: ""
            val cover = document
                .getElementsByClass("avatar avatar--s")
                .first()
                ?.getElementsByTag("img")
                ?.first()
                ?.attr("src")
                ?: ""
            val customField = document
                .getElementsByClass("resourceBody-fields resourceBody-fields--before")
                .first()
                ?.getElementsByTag("dl")
            var devicePosition = 100
            var downloadPosition = 100
            var sizePosition = 100
            var passwordPosition = 100
            var device = ""
            var downloadType = ""
            var size = ""
            var password = ""
            if (customField != null) {
                for (index in customField.indices) {
                    val typeName = customField[index]
                        .getElementsByTag("dt")
                        .first()
                        ?.text()
                        ?: ""
                    when (typeName) {
                        "适用设备" -> devicePosition = index
                        "下载渠道" -> downloadPosition = index
                        "文件大小" -> sizePosition = index
                        "提取码" -> passwordPosition = index
                    }
                }
            }
            if (customField != null) {
                for (index in customField.indices) {
                    device = if (devicePosition != 100) {
                        customField[devicePosition]
                            .getElementsByTag("dd")
                            .first()
                            ?.text()
                            ?: ""
                    } else {
                        ""
                    }
                    downloadType = if (downloadPosition != 100) {
                        customField[downloadPosition]
                            .getElementsByTag("dd")
                            .first()
                            ?.text()
                            ?: ""
                    } else {
                        ""
                    }
                    size = if (sizePosition != 100) {
                        customField[sizePosition]
                            .getElementsByTag("dd")
                            .first()
                            ?.text()
                            ?: ""
                    } else {
                        ""
                    }
                    password = if (passwordPosition != 100) {
                        customField[passwordPosition]
                            .getElementsByTag("dd")
                            .first()
                            ?.text()
                            ?: ""
                    } else {
                        ""
                    }
                }
            }
            val pairs = document
                .getElementsByClass("resourceSidebarGroup")
                .first()
                ?.getElementsByTag("dl")
            val author = pairs
                ?.get(0)
                ?.getElementsByTag("dd")
                ?.first()
                ?.text()
                ?: ""
            val authorUrl = pairs
                ?.get(0)
                ?.getElementsByTag("dd")
                ?.first()
                ?.getElementsByTag("a")
                ?.first()
                ?.attr("href")
                ?: ""
            val authorId = pairs
                ?.get(0)
                ?.getElementsByTag("dd")
                ?.first()
                ?.getElementsByTag("a")
                ?.first()
                ?.attr("data-user-id")
                ?: ""
            val authorDocument = Jsoup
                .connect("$BASE_URL/members/$authorId/")
                .userAgent(USER_AGENT)
                .cookies(Cookies)
                .timeout(TIMEOUT_MS)
                .get()
            val authorAvatar = authorDocument
                .getElementsByClass("avatarWrapper")
                .first()
                ?.getElementsByTag("img")
                ?.first()
                ?.attr("src")
                ?: ""
            val downloadCount = pairs
                ?.get(1)
                ?.getElementsByTag("dd")
                ?.first()
                ?.text()
                ?: ""
            val viewCount = pairs
                ?.get(2)
                ?.getElementsByTag("dd")
                ?.first()
                ?.text()
                ?: ""
            val firstPost = pairs
                ?.get(3)
                ?.getElementsByTag("dd")
                ?.first()
                ?.getElementsByTag("time")
                ?.first()
                ?.attr("data-date-string")
                ?: ""
            val latestPost = pairs
                ?.get(4)
                ?.getElementsByTag("dd")
                ?.first()
                ?.getElementsByTag("time")
                ?.first()
                ?.attr("data-date-string")
                ?: ""
            val downloadUrl = document
                .getElementsByClass("resourceSidebarGroup resourceSidebarGroup--buttons")
                .first()
                ?.getElementsByTag("a")
                ?.last()
                ?.attr("href")
                ?: ""
            return@withContext ResourceArticle(
                title,
                cover,
                device,
                downloadType,
                size,
                password,
                author,
                authorUrl,
                authorId,
                authorAvatar,
                downloadCount,
                viewCount,
                firstPost,
                latestPost,
                downloadUrl
            )
        }
    }
}
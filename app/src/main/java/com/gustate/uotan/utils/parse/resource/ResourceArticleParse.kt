package com.gustate.uotan.utils.parse.resource

import android.util.Log
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import com.gustate.uotan.utils.parse.resource.ResourceData.PurchaseData
import com.gustate.uotan.utils.parse.resource.ResourceData.ResReportData
import com.gustate.uotan.utils.parse.resource.ResourceData.ResourceArticle
import com.gustate.uotan.utils.parse.resource.ResourceData.ResourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class ResourceArticleParse {
    companion object {
        suspend fun fetchResourceArticle(url: String): ResourceArticle = withContext(Dispatchers.IO) {
            val document = Jsoup
                .connect(BASE_URL + url)
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
            device = if (devicePosition != 100) {
                customField
                    ?.get(devicePosition)
                    ?.getElementsByTag("dd")
                    ?.first()
                    ?.text()
                    ?: ""
            } else {
                ""
            }
            downloadType = if (downloadPosition != 100) {
                customField
                    ?.get(downloadPosition)
                    ?.getElementsByTag("dd")
                    ?.first()
                    ?.text()
                    ?: ""
            } else {
                ""
            }
            size = if (sizePosition != 100) {
                customField
                    ?.get(sizePosition)
                    ?.getElementsByTag("dd")
                    ?.first()
                    ?.text()
                    ?: ""
            } else {
                ""
            }
            password = if (passwordPosition != 100) {
                customField
                    ?.get(passwordPosition)
                    ?.getElementsByTag("dd")
                    ?.first()
                    ?.text()
                    ?: ""
            } else {
                ""
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
            val content = document
                .getElementsByClass("bbWrapper")
                .first()
                ?.html()
                ?: ""
            val loveLink = document
                .getElementsByClass("reactionsBar-link")
                .first()
                ?.attr("href")
                ?: ""
            val numberOfLikes = if (loveLink != "") {
                val loveDocument = Jsoup.connect("$BASE_URL$loveLink/")
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .cookies(Cookies)
                    .get()
                val loveString = loveDocument
                    .getElementsByClass("tabs-tab tabs-tab--reaction0 is-active").first()?.text()
                loveString!!.replace("[^0-9]".toRegex(), "")
            } else "0"
            val reactUrlContent = document
                .getElementsByClass("actionBar-set actionBar-set--external")
                .first()
                ?.getElementsByTag("a")
                ?.first()
                ?.text()
                ?: ""
            val reactUrl = if (reactUrlContent == "点赞") {
                document
                    .getElementsByClass("actionBar-set actionBar-set--external")
                    .first()
                    ?.getElementsByTag("a")
                    ?.first()
                    ?.attr("href")
                    ?: ""
            } else ""
            val reactContent = document
                .getElementsByClass("reactionsBar-link")
                .first()
                ?.text()
                ?: ""
            val isReact = if (reactContent.length >= 2) {
                reactContent.substring(0..1) == "您,"
            } else if (reactContent.length == 1) {
                reactContent == "您"
            } else false
            val bookMarkContent = document
                .getElementsByClass("js-bookmarkText u-srOnly")
                .first()
                ?.text()
                ?: ""
            val isBookMark = bookMarkContent == "编辑收藏"
            val bookMarkUrl = document
                .select("a[class^='bookmarkLink']")
                .first()
                ?.attr("href")
                ?: ""
            return@withContext ResourceArticle(title, cover, device, downloadType, size, password,
                author, authorUrl, authorId, authorAvatar, downloadCount, viewCount, firstPost,
                latestPost, downloadUrl, content, numberOfLikes, reactUrl, isReact, isBookMark,
                bookMarkUrl)
        }

        suspend fun getResourceReport(url: String): MutableList<ResReportData> = withContext(
            Dispatchers.IO) {
            val document = Jsoup
                .connect(BASE_URL + url + "reviews")
                .cookies(Cookies)
                .timeout(TIMEOUT_MS)
                .userAgent(USER_AGENT)
                .get()
            val messages = document
                .getElementsByClass("message message--simple")
            val resourceReportList = mutableListOf<ResReportData>()
            messages.forEach {
                val authorId = it
                    .getElementsByClass("username ")
                    .first()
                    ?.attr("data-user-id")
                    ?: ""
                val author = it
                    .getElementsByClass("username ")
                    .first()
                    ?.text()
                    ?: ""
                val rating = it
                    .getElementsByClass("ratingStars ratingStars--smaller")
                    .first()
                    ?.attr("title")
                    ?.replace(".00 星", "")
                    ?.toFloat()
                    ?: 0f
                val time = it
                    .getElementsByTag("time")
                    .first()
                    ?.attr("title")
                    ?.replace("， ", " ")
                    ?: ""
                val version = it
                    .getElementsByClass("listInline listInline--bullet")
                    .first()
                    ?.getElementsByTag("li")
                    ?.last()
                    ?.text()
                    ?.replace("版本: ", "")
                    ?: "未知"
                val content = it
                    .getElementsByClass("message-body")
                    .first()
                    ?.html()
                    ?: ""
                resourceReportList.add(ResReportData(authorId, author, rating, time, version,
                    content))
            }
            return@withContext resourceReportList
        }

        suspend fun getPurchaseData(url: String): MutableList<PurchaseData> = withContext(
            Dispatchers.IO) {
            val response = Jsoup
                .connect(BASE_URL + url)
                .cookies(Cookies)
                .timeout(TIMEOUT_MS)
                .userAgent(USER_AGENT)
                .execute()
            val document = response.parse()
            val title = document.getElementsByClass("p-title-value").first()?.text()?: ""
            val resType = when {
                title == "选择网盘…" -> ResourceType.New
                title.startsWith("购买:") -> ResourceType.Old
                else -> ResourceType.Other
            }
            when (resType) {
                ResourceType.New -> {
                    return@withContext getNewResInfo(document)
                }
                ResourceType.Old -> {
                    return@withContext getOldResInfo(url, document)
                }
                ResourceType.Other -> {
                    return@withContext mutableListOf(PurchaseData(ResourceType.Other, false, "",
                        "", "", response.url().toString()))
                }
            }
        }

        fun getNewResInfo(document: Document): MutableList<PurchaseData> {
            val newPurchaseInfoList = mutableListOf<PurchaseData>()
            val driveList = document.getElementsByClass("dataList-row")
            driveList.forEach {
                val isPaid = it
                    .getElementsByClass("dataList-cell dataList-cell--min")
                    .first()
                    ?.text() == "下载"
                val driveName = it
                    .getElementsByClass("dataList-cell")
                    .first()
                    ?.text()
                    ?: ""
                val code = it
                    .getElementsByClass("dataList-cell")[1]
                    .text()
                    ?: ""
                val price = it
                    .getElementsByClass("dataList-cell dataList-cell--min")
                    .first()
                    ?.text()
                    ?.replace("使用 ", "")
                    ?.replace(" 购买并下载", "")
                    ?: ""
                val url = it
                    .getElementsByClass("button--link button")
                    .first()
                    ?.attr("href")
                    ?: ""
                newPurchaseInfoList.add(PurchaseData(ResourceType.New, isPaid, driveName, code,
                    price, url))
            }
            newPurchaseInfoList.removeAt(0)
            return newPurchaseInfoList
        }

        fun getOldResInfo(url: String, document: Document): MutableList<PurchaseData> {
            val price = document
                .getElementsByClass("formRow")
                .first()
                ?.getElementsByTag("dd")
                ?.text()
                ?: ""
            return mutableListOf(PurchaseData(ResourceType.Old, false, "", "", price, url))
        }
        suspend fun buyResource(
            url: String,
            xfToken: String,
            cookiesString: String,
            onSuccess: () -> Unit,
            onException: (String) -> Unit
        ) = withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("accept", "1")
                .addFormDataPart("_xfRequestUri", url)
                .addFormDataPart("_xfWithData", "1")
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("_xfResponseType", "json")
                .build()
            val request = Request.Builder()
                .url(BASE_URL + url)
                .addHeader("Cookie", cookiesString)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Origin", BASE_URL)
                .addHeader("Referer", url)
                .addHeader(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
                )
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .post(requestBody)
                .build()
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    onException("HTTP错误: ${response.code}\n${response.headers}\n请求内容：${response.request}")
                    Log.e("httpError", "HTTP错误: ${response.code}\n请求头：${response.headers}\n请求内容：${response.request}")
                } else {
                    onSuccess()
                }
            } catch (e: Exception) {
                onException(e.message ?: "未知错误")
                Log.e("exception", e.message ?: "未知错误")
            }
        }
        suspend fun reportResource(
            url: String,
            xfToken: String,
            rating: String,
            message: String,
            cookiesString: String,
            onSuccess: () -> Unit,
            onException: (String) -> Unit
        ) = withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("rating", rating)
                .addFormDataPart("message", message)
                .addFormDataPart("_xfRequestUri", url + "rate")
                .addFormDataPart("_xfWithData", "1")
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("_xfResponseType", "json")
                .build()
            val request = Request.Builder()
                .url(BASE_URL + url + "rate")
                .addHeader("Cookie", cookiesString)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Origin", BASE_URL)
                .addHeader("Referer", url + "rate")
                .addHeader(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
                )
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .post(requestBody)
                .build()
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onException("HTTP错误: ${response.code}\n${response.headers}\n请求内容：${response.request}")
                        Log.e("httpError", "HTTP错误: ${response.code}\n请求头：${response.headers}\n请求内容：${response.request}")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onException(e.message ?: "未知错误")
                    Log.e("exception", e.message ?: "未知错误")
                }
            }
        }
    }
}
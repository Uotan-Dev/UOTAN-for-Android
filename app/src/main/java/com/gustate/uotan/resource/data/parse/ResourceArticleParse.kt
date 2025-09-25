package com.gustate.uotan.resource.data.parse

import android.util.Log
import com.gustate.uotan.resource.data.model.PurchaseData
import com.gustate.uotan.resource.data.model.ResReplyData
import com.gustate.uotan.resource.data.model.ResourceArticle
import com.gustate.uotan.resource.data.model.ResourceType
import com.gustate.uotan.utils.Utils.USER_AGENT
import com.gustate.uotan.utils.Utils.baseUrl
import com.gustate.uotan.utils.network.HttpClient
import com.gustate.uotan.utils.network.SecurityParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class ResourceArticleParse {
    suspend fun fetchResourceArticle(
        url: String,
        onSuccess: (ResourceArticle) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val client = HttpClient.getClient()
            val request = Request.Builder()
                .url(baseUrl + url)
                .header("User-Agent", USER_AGENT)
                .build()
            val response = client.newCall(request).execute()
            val document = Jsoup.parse(response.body.string())
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
            val device = if (devicePosition != 100) {
                customField
                    ?.get(devicePosition)
                    ?.getElementsByTag("dd")
                    ?.first()
                    ?.text()
                    ?: ""
            } else ""
            val downloadType = if (downloadPosition != 100) {
                customField
                    ?.get(downloadPosition)
                    ?.getElementsByTag("dd")
                    ?.first()
                    ?.text()
                    ?: ""
            } else ""
            val size = if (sizePosition != 100) {
                customField
                    ?.get(sizePosition)
                    ?.getElementsByTag("dd")
                    ?.first()
                    ?.text()
                    ?: ""
            } else ""
            val password = if (passwordPosition != 100) {
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
            val authorRequest = Request.Builder()
                .url("$baseUrl/members/$authorId/")
                .header("User-Agent", USER_AGENT)
                .build()
            val authorResponse = client.newCall(authorRequest).execute()
            val authorDocument = Jsoup.parse(authorResponse.body.string())
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
                val client = HttpClient.getClient()
                val loveRequest = Request.Builder()
                    .url("$baseUrl$loveLink/")
                    .header("User-Agent", USER_AGENT)
                    .build()
                val loveResponse = client.newCall(loveRequest).execute()
                val loveDocument = Jsoup.parse(loveResponse.body.string())
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
                        || reactContent.substring(0..1) == "您和"
            } else if (reactContent.length == 1) {
                reactContent == "您"
            } else false
            val bookMarkContent = document
                .getElementsByClass("js-bookmarkText u-srOnly")
                .first()
                ?.text()
                ?: ""
            val isBookMark = bookMarkContent == "编辑收藏"
            val result = ResourceArticle(
                title, cover, device, downloadType, size, password,
                author, authorUrl, authorId, authorAvatar, downloadCount, viewCount, firstPost,
                latestPost, downloadUrl, content, numberOfLikes, reactUrl, isReact, isBookMark)
            withContext(Dispatchers.Main) {
                onSuccess(result)
            }
        } catch (throwable: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(throwable)
            }
        }
    }

    suspend fun getResourceReply(
        url: String,
        onSuccess: (MutableList<ResReplyData>) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val client = HttpClient.getClient()
            val request = Request.Builder()
                .url(baseUrl + url + "reviews")
                .header("User-Agent", USER_AGENT)
                .build()
            val response = client.newCall(request).execute()
            val document = Jsoup.parse(response.body.string())
            val messages = document
                .getElementsByClass("message message--simple")
            val resReplyList = mutableListOf<ResReplyData>()
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
                resReplyList.add(
                    ResReplyData(authorId, author, rating, time, version, content))
                withContext(Dispatchers.Main) {
                    onSuccess(resReplyList)
                }
            }
        } catch (throwable: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(throwable)
            }
        }
    }

    suspend fun getPurchaseData(url: String): MutableList<PurchaseData> = withContext(
        Dispatchers.IO) {
        val client = HttpClient.getClient()
        val request = Request.Builder()
            .url(baseUrl + url)
            .header("User-Agent", USER_AGENT)
            .build()
        val response = client.newCall(request).execute()
        val document = Jsoup.parse(response.body.string())
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
                return@withContext mutableListOf(
                    PurchaseData(
                        ResourceType.Other, false, "",
                        "", "", response.request.url.toString()
                    )
                )
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
            newPurchaseInfoList.add(
                PurchaseData(ResourceType.New, isPaid, driveName, code, price, url)
            )
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
        return mutableListOf(
            PurchaseData(
                ResourceType.Old, false, "", "", price, url)
        )
    }

    suspend fun buyResource(
        url: String,
        onSuccess: () -> Unit,
        onException: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val xfToken = SecurityParse.parseHiddenXfToken(baseUrl + url)
        val client = HttpClient.getClient()
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
            .url(baseUrl + url)
            .addHeader("User-Agent", USER_AGENT)
            .post(requestBody)
            .build()
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                onException(
                    "HTTP错误: ${response.code}\n${response.headers}\n请求内容：${response.request}")
            } else {
                onSuccess()
            }
        } catch (e: Exception) {
            onException(e.message ?: "未知错误")
        }
    }

    suspend fun reactResource(
        url: String,
        onSuccess: () -> Unit,
        onFailure: ((code: Int, body: String) -> Unit),
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val client = HttpClient.getClient()
            val xfToken = SecurityParse.parseHiddenXfToken(url)
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("reaction_id", "1")
                .build()
            val request = Request.Builder()
                .url(baseUrl + url)
                .addHeader("User-Agent", USER_AGENT)
                .post(requestBody)
                .build()
            client.newCall(request).execute().use { response ->
                val responseBody = response.body.string()
                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onFailure(response.code, responseBody)
                    }
                    return@withContext
                }
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onException(e)
            }
        }
    }

    /**
     *  检测关注状态
     */
    suspend fun isFollowAuthor(
        url: String,
        onSuccess: (Boolean) -> Unit,
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val client = HttpClient.getClient()
            Log.e("e", url)
            val request = Request.Builder()
                .url(baseUrl + url)
                .header("User-Agent", USER_AGENT)
                .build()
            val response = client.newCall(request).execute()
            val document = Jsoup.parse(response.body.string())
            val followContent = document
                .getElementsByClass("memberHeader-buttons")
                .first()
                ?.getElementsByClass("button-text")
                ?.first()
                ?.text()
                ?: ""
            Log.e("e", followContent+ "111")
            val isFollow = followContent == "已关注"
            withContext(Dispatchers.Main) {
                onSuccess(isFollow)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onException(e)
            }
        }
    }

    /**
     *  关注/取消关注
     */
    suspend fun onFollowAuthor(
        url: String,
        onSuccess: () -> Unit,
        onFailure: ((code: Int, body: String) -> Unit),
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val client = HttpClient.getClient()
            val xfToken = SecurityParse.parseHiddenXfToken(url)
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("_xfRedirect", baseUrl + url)
                .addFormDataPart("_xfRequestUri", "${url}follow")
                .addFormDataPart("_xfWithData", "1")
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("_xfResponseType", "json")
                .build()
            val request = Request.Builder()
                .url("${baseUrl + url}follow")
                .addHeader("User-Agent", USER_AGENT)
                .post(requestBody)
                .build()
            client.newCall(request).execute().use { response ->
                val responseBody = response.body.string()
                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onFailure(response.code, responseBody)
                    }
                    return@withContext
                }
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onException(e)
            }
        }
    }

    suspend fun onBookMark(
        url: String,
        isMarked: Boolean,
        onSuccess: () -> Unit,
        onFailure: ((code: Int, body: String) -> Unit),
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        val bookMarkUrl = "$baseUrl$url/bookmark"
        try {
            val client = HttpClient.getClient()
            val xfToken = SecurityParse.parseHiddenXfToken(url)
            // 创建 MultipartBody
            val addRequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("labels", "")
                .addFormDataPart("message", "")
                .addFormDataPart("_xfRequestUri", bookMarkUrl)
                .addFormDataPart("_xfWithData", "1")
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("_xfResponseType", "json")
                .build()
            val delRequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("labels", "")
                .addFormDataPart("message", "")
                .addFormDataPart("delete", "undefined")
                .addFormDataPart("_xfRequestUri", bookMarkUrl)
                .addFormDataPart("_xfWithData", "1")
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("_xfResponseType", "json")
                .build()
            // 创建 Request
            val request = Request.Builder()
                .url(bookMarkUrl)
                .addHeader("User-Agent", USER_AGENT)
                .post(if (isMarked) delRequestBody else addRequestBody)
                .build()
            client.newCall(request).execute().use { response ->
                val responseBody = response.body.string()
                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onFailure(response.code, responseBody)
                    }
                    return@withContext
                }
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onException(e)
            }
        }
    }

    suspend fun onReply(
        url: String,
        rating: String,
        message: String,
        onSuccess: () -> Unit,
        onFailure: ((code: Int, body: String) -> Unit),
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val client = HttpClient.getClient()
            val xfToken = SecurityParse.parseHiddenXfToken(url)
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("rating", rating)
                .addFormDataPart("message", message)
                .addFormDataPart("_xfRequestUri", url)
                .addFormDataPart("_xfWithData", "1")
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("_xfResponseType", "json")
                .build()
            val request = Request.Builder()
                .url(baseUrl + url + "rate")
                .addHeader("User-Agent", USER_AGENT)
                .post(requestBody)
                .build()
            client.newCall(request).execute().use { response ->
                val responseBody = response.body.string()
                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onFailure(response.code, responseBody)
                    }
                    return@withContext
                }
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onException(e)
            }
        }
    }
}
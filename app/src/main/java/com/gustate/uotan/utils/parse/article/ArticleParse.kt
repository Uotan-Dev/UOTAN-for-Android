package com.gustate.uotan.utils.parse.article

import com.gustate.uotan.utils.Utils.Companion.baseUrl
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.IOException
import kotlin.String

class ArticleParse {
    companion object {
        data class TagInfo(
            val name: String,
            val url: String
        )

        data class SectionInfo(
            val name: String,
            val url: String
        )

        data class ForumArticle(
            val topic: String,
            val title: String,
            val numberOfComments: String,
            val pageView: String,
            val tags: MutableList<TagInfo>,
            val section: MutableList<SectionInfo>,
            val totalPage: String,
            val avatarUrl: String,
            val authorName: String,
            val authorUrl: String,
            val time: String,
            val ipAddress: String,
            val article: String,
            val numberOfLikes: String,
            val isBilibili: Boolean,
            val bilibiliVideoLink: String,
            val isLocked: Boolean,
            val reactUrl: String,
            val isReact: Boolean,
            val reportUrl: String,
            val editUrl: String,
            val deleteUrl: String,
            val ipUrl: String,
            val changeAuthorUrl: String,
            val isJingTie: Boolean,
            val isBookMark: Boolean,
            val bookMarkUrl: String,
            val xfToken: String,
            val attachmentHash: String,
            val attachmentHashCombined: String,
            val lastCommentDate: String
        )

        data class CommentItem(
            val userId: String,
            val userName: String,
            val userIp: String,
            val time: String,
            val content: String,
            val postTime: String
        )

        data class CommentData(
            val commentItem: MutableList<CommentItem>,
            val totalPage: String
        )

        suspend fun articleParse(url: String): ForumArticle = withContext(Dispatchers.IO) {
            /**
             * 此 Document 对象就是网页的 document
             */
            val document = Jsoup
                .connect(baseUrl + url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .cookies(Cookies)
                .get()

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

            var reportIndex = 100
            var editIndex = 100
            var deleteIndex = 100
            var ipIndex = 100
            var changeAuthorIndex = 100

            val internalActionBar = document
                .getElementsByClass("actionBar-set actionBar-set--internal")
                .first()
                ?.getElementsByTag("a")

            internalActionBar?.let {
                for (index in it.indices) {
                    when (it[index].text()) {
                        "举报" -> reportIndex = index
                        "编辑" -> editIndex = index
                        "删除" -> deleteIndex = index
                        "IP" -> ipIndex = index
                        "变更作者" -> changeAuthorIndex = index
                    }
                }
            }

            val reportUrl = if (reportIndex != 100) {
                internalActionBar
                    ?.get(reportIndex)
                    ?.attr("href")
                    ?: ""
            } else ""

            val editUrl = if (editIndex != 100) {
                internalActionBar
                    ?.get(editIndex)
                    ?.attr("href")
                    ?: ""
            } else ""

            val deleteUrl = if (deleteIndex != 100) {
                internalActionBar
                    ?.get(deleteIndex)
                    ?.attr("href")
                    ?: ""
            } else ""

            val ipUrl = if (ipIndex != 100) {
                internalActionBar
                    ?.get(ipIndex)
                    ?.attr("href")
                    ?: ""
            } else ""

            val changeAuthorUrl = if (changeAuthorIndex != 100) {
                internalActionBar
                    ?.get(changeAuthorIndex)
                    ?.attr("href")
                    ?: ""
            } else ""

            val lockedContent = document
                .getElementsByClass("blockStatus-message blockStatus-message--locked")
                .first()
                ?.text()
                ?: ""

            val isLocked = lockedContent.isNotEmpty()

            /**
             * pTitleElement 包括话题和标题
             */
            val pTitleElement = if (
                document
                    .getElementsByClass("p-title ").first() != null
            )
            {
                document
                    .getElementsByClass("p-title ")
                    .first()
            } else {
                document
                    .getElementsByClass("p-title")
                    .first()
            }
            // 话题
            val topic = pTitleElement
                ?.getElementsByClass("label label--uotan-threads")
                ?.first()
                ?.text()
                ?: ""
            // 标题
            val title = pTitleElement
                ?.getElementsByClass("p-title-value")
                ?.first()
                ?.ownText()
                ?: ""
            /**
             * pDescriptionElements 包含评论数、浏览量和标签
             * 其中每个 li 标签 从上到下表示评论数、浏览量和标签
             */
            val upPDescriptionElement = document
                .getElementsByClass("p-description")
                .first()
            val pDescriptionElements = upPDescriptionElement
                ?.getElementsByClass("listInline listInline--bullet")
                ?.first()
                ?.getElementsByTag("li")
            // 评论数
            val numberOfComments = pDescriptionElements!![0]
                .ownText()
            // 评论数
            val pageView = pDescriptionElements[1]
                .ownText()
            // 标签
            val tags: MutableList<TagInfo> = pDescriptionElements
                .takeIf { it.size >= 3 }
                ?.get(2)
                ?.getElementsByClass("js-tagList")
                ?.first()
                ?.getElementsByTag("a")
                // 使用 map 转换每个元素
                ?.map { tagElement ->
                    TagInfo(
                        tagElement.text().trim(),
                        tagElement.attr("href")
                    )
                }?.toMutableList()  // 转换为可变列表
                ?: mutableListOf()  // 空列表兜底
            val sectionElement = document
                .getElementsByClass("p-breadcrumbs ")
                .first()
                ?.getElementsByTag("li")
                ?.last()
            val section: MutableList<SectionInfo> = sectionElement
                ?.getElementsByTag("a")
                // 使用 map 转换每个元素
                ?.map { tagElement ->
                    SectionInfo(
                        tagElement.getElementsByTag("span").first()?.text()!!,
                        tagElement.attr("href")
                    )
                }?.toMutableList()  // 转换为可变列表
                ?: mutableListOf()  // 空列表兜底
            val totalPage = document
                .getElementsByClass("pageNav-main")
                .first()
                ?.getElementsByTag("li")
                ?.last()
                ?.text()
                ?: ""
            /*
             * 是否文章、点赞数
             */
            val articleElement = document
                .getElementsByClass("block-container lbContainer")
                .first()
            // 作者头像
            val avatarUrl = articleElement
                ?.getElementsByClass("avatarWrapper")
                ?.first()
                ?.getElementsByTag("img")
                ?.first()
                ?.attr("srcset")
                ?: ""
            val author = articleElement
                ?.getElementsByClass("contentRow-header")
                ?.first()
                ?.getElementsByTag("a")
                ?.first()
            val authorName = author
                ?.text()
                ?: ""
            val authorUrl = author
                ?.attr("href")
                ?: ""
            val contentRowLesserElement = articleElement
                ?.getElementsByClass("contentRow-lesser")
                ?.first()
            val time = contentRowLesserElement
                ?.getElementsByTag("time")
                ?.first()
                ?.attr("title")
                ?.replace("， ", " ")
                ?: ""
            val bookMarkContent = contentRowLesserElement
                ?.getElementsByClass("js-bookmarkText u-srOnly")
                ?.first()
                ?.text()
                ?: ""
            val isBookMark = bookMarkContent == "编辑收藏"
            val bookMarkUrl = document
                .select("a[class^='bookmarkLink']")
                .first()
                ?.attr("href")
                ?: ""
            val secInfo = contentRowLesserElement
                ?.getElementsByClass("message-attribution-opposite message-attribution-opposite--list ")
                ?.first()
            val ipAddress = secInfo!!
                .getElementsByClass("user-login-ip")
                .first()
                ?.text()
                ?: ""
            val articleHtml = articleElement
                .getElementsByClass("bbWrapper")
                .first()
                ?.html()!!
            val article = articleElement
                .getElementsByClass("bbWrapper")
                .first()
            val isBilibiliVideo = article
                ?.getElementsByTag("span")
                ?.first()
                ?.attr("data-guineapigclub-mediaembed") ==
                    "bilibili"
            val bilibiliLink = article
                ?.getElementsByTag("iframe")
                ?.first()
                ?.attr("data-guineapigclub-mediaembed-src")
                ?: "noBilibili"
            val loveLink = articleElement
                .getElementsByClass("reactionsBar-link")
                .first()
                ?.attr("href")
                ?: ""
            val numberOfLikes = if (loveLink != "") {
                val loveDocument = Jsoup.connect("$baseUrl$loveLink/")
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .cookies(Cookies)
                    .get()
                val loveString = loveDocument
                    .getElementsByClass("tabs-tab tabs-tab--reaction0 is-active").first()?.text()
                loveString!!.replace("[^0-9]".toRegex(), "")
            } else "0"
            val isJingTie = (document
                .getElementsByClass("pairs pairs--columns pairs--fixedSmall pairs--customField")
                .first()
                ?.attr("data-field")
                ?: "") == "jingtie"
            val xfToken = document
                .select("input[name=_xfToken]")
                .first()
                ?.attr("value") ?: throw Exception("Xf token not found")
            val attachmentHash = document
                .select("input[name=attachment_hash]")
                .first()
                ?.attr("value") ?: throw Exception("Attachment hash not found")
            val attachmentHashCombined = document
                .select("input[name=attachment_hash_combined]")
                .first()
                ?.attr("value") ?: throw Exception("Attachment hash combined not found")
            val lastCommentDate = document
                .getElementsByTag("time")
                .first()
                ?.attr("data-time") ?: throw Exception("Data time not found")
            return@withContext ForumArticle(topic, title, numberOfComments, pageView, tags, section,
                totalPage, avatarUrl, authorName, authorUrl, time, ipAddress, articleHtml,
                numberOfLikes, isBilibiliVideo, bilibiliLink, isLocked, reactUrl, isReact,
                reportUrl, editUrl, deleteUrl, ipUrl, changeAuthorUrl, isJingTie, isBookMark,
                bookMarkUrl, xfToken, attachmentHash, attachmentHashCombined, lastCommentDate)
        }
        suspend fun fetchComments(url: String, page: String): CommentData = withContext(Dispatchers.IO) {
            val result = mutableListOf<CommentItem>()
            val document = Jsoup.connect("$baseUrl${url.removeSuffix("/").replace(Regex("/post-\\d+$"), "")}/page-$page")
                .cookies(Cookies)
                .timeout(TIMEOUT_MS)
                .userAgent(USER_AGENT)
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
            val commentElements = document
                .getElementsByClass("block-body js-replyNewMessageContainer")
                .first()
                ?.select("> article")
                ?.apply {
                    if (page == "1") {
                        removeAt(0)
                    }
                }
            var userId = ""
            var userName = ""
            var userIp = ""
            var time = ""
            var content = ""
            var postTime = ""
            commentElements?.forEach {
                userId = it
                    .getElementsByClass("avatarWrapper")
                    .first()
                    ?.getElementsByTag("a")
                    ?.first()
                    ?.attr("data-user-id")
                    ?: ""
                userName = it
                    .getElementsByClass("contentRow-header")
                    .first()
                    ?.getElementsByTag("a")
                    ?.first()
                    ?.text()
                    ?: ""
                userIp = it
                    .getElementsByClass("user-login-ip")
                    .first()
                    ?.getElementsByTag("li")
                    ?.text()
                    ?: ""
                time = it
                    .getElementsByClass("contentRow-lesser")
                    .first()
                    ?.getElementsByTag("time")
                    ?.first()
                    ?.attr("title")
                    ?.replace("， ", " ")
                    ?: ""
                content = it
                    .getElementsByClass("message-content  js-messageContent")
                    .first()
                    ?.getElementsByClass("message-userContent lbContainer js-lbContainer ")
                    ?.first()
                    ?.getElementsByClass("bbWrapper")
                    ?.first()
                    ?.html()
                    ?: ""
                postTime = it
                    .getElementsByClass("contentRow-lesser")
                    .first()
                    ?.getElementsByTag("time")
                    ?.first()
                    ?.attr("data-time")
                    ?: ""
                result.add(CommentItem(userId, userName, userIp, time, content, postTime))
            }
            return@withContext CommentData(result, totalPage)
        }

        suspend fun addArticleReply(
            url: String,
            cookiesString: String,
            xfToken: String,
            message: String,
            attachmentHash: String,
            attachmentHashCombined: String,
            lastDate: String,
            lastKnowDate: String,
            loadExtra: String,
            onSuccess: ((Long) -> Unit),
            onException: ((String) -> Unit)
        ) = withContext(Dispatchers.IO) {
            // 实例化 OkHttpClient
            val client = OkHttpClient()
            // 创建 MultipartBody
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("message_html", message)
                .addFormDataPart("attachment_hash", attachmentHash)
                .addFormDataPart("attachment_hash_combined", attachmentHashCombined)
                .addFormDataPart("last_date", lastDate)
                .addFormDataPart("last_known_date", lastKnowDate)
                .addFormDataPart("load_extra", loadExtra)
                .addFormDataPart("_xfRequestUri", url)
                .addFormDataPart("_xfWithData", "1")
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("_xfResponseType", "json")
                .build()
            // 创建 Request
            val request = Request.Builder()
                .url(baseUrl + url + "add-reply")
                .addHeader("Cookie", cookiesString)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Origin", baseUrl)
                .addHeader("Referer", baseUrl + url)
                .addHeader(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
                )
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .post(requestBody)
                .build()
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        val json = response.body?.string() ?: ""
                        val lastDate = JSONObject(json).getLong("lastDate")
                        onSuccess(lastDate)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onException("HTTP错误: ${response.code}\n请求头：${response.headers}\n请求内容：${response.request}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onException(e.message ?: "")
                }
            }
        }

        suspend fun addReaction(
            url: String,
            cookiesString: String,
            xfToken: String
        ): Boolean = withContext(Dispatchers.IO) {
            // 实例化 OkHttpClient
            val client = OkHttpClient()
            // 创建 MultipartBody
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("reaction_id", "1")
                .build()
            // 创建 Request
            val request = Request.Builder()
                .url(baseUrl + url)
                .addHeader("Cookie", cookiesString)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Origin", baseUrl)
                .addHeader("Referer", url)
                .addHeader(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
                )
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .post(requestBody)
                .build()
            return@withContext try {
                val execute = client
                    .newCall(request)
                    .execute()
                return@withContext execute.isSuccessful
            } catch (_: IOException) {
                return@withContext false
            }
        }
        suspend fun report(
            url: String,
            message: String,
            cookiesString: String,
            xfToken: String
        ): Boolean = withContext(Dispatchers.IO) {
            // 实例化 OkHttpClient
            val client = OkHttpClient()
            // 创建 MultipartBody
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("message", message)
                .addFormDataPart("_xfRequestUri", url)
                .addFormDataPart("_xfWithData", "1")
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("_xfResponseType", "json")
                .build()
            // 创建 Request
           val request = Request.Builder()
                .url("$baseUrl$url/report")
                .addHeader("Cookie", cookiesString)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Origin", baseUrl)
                .addHeader("Referer", url)
                .addHeader(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
                )
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .post(requestBody)
                .build()
            return@withContext try {
                val execute = client
                    .newCall(request)
                    .execute()
                return@withContext execute.isSuccessful
            } catch (_: IOException) {
                return@withContext false
            }
        }
        suspend fun deleteArticle(
            url: String,
            deleteUrl: String,
            cookiesString: String,
            xfToken: String
        ): Boolean = withContext(Dispatchers.IO) {
            // 实例化 OkHttpClient
            val client = OkHttpClient()
            // 创建 MultipartBody
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("hard_delete", "1")
                .addFormDataPart("_xfRedirect", baseUrl.replace(".cn/", ".cn") + url)
                .addFormDataPart("_xfRequestUri", deleteUrl)
                .addFormDataPart("_xfWithData", "1")
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("_xfResponseType", "json")
                .build()
            // 创建 Request
            val request = Request.Builder()
                .url(baseUrl + deleteUrl)
                .addHeader("Cookie", cookiesString)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Origin", baseUrl)
                .addHeader("Referer", deleteUrl)
                .addHeader(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
                )
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .post(requestBody)
                .build()
            return@withContext try {
                val execute = client
                    .newCall(request)
                    .execute()
                return@withContext execute.isSuccessful
            } catch (_: IOException) {
                return@withContext false
            }
        }
        suspend fun getAuthorIp(url: String): String = withContext(Dispatchers.IO) {
            val document = Jsoup
                .connect(baseUrl + url)
                .cookies(Cookies)
                .timeout(TIMEOUT_MS)
                .userAgent(USER_AGENT)
                .get()
            val ip = document
                .getElementsByClass("ip")
                .first()
                ?.text()
                ?: ""
            return@withContext ip
        }
        suspend fun changeAuthor(
            url: String,
            changeAuthor: String,
            cookiesString: String,
            xfToken: String
        ): Boolean = withContext(Dispatchers.IO) {
            // 实例化 OkHttpClient
            val client = OkHttpClient()
            // 创建 MultipartBody
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("new_post_author", changeAuthor)
                .addFormDataPart("_xfRequestUri", url)
                .addFormDataPart("_xfWithData", "1")
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("_xfResponseType", "json")
                .build()
            // 创建 Request
            val request = Request.Builder()
                .url(baseUrl + url + "save")
                .addHeader("Cookie", cookiesString)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Origin", baseUrl)
                .addHeader("Referer", url)
                .addHeader(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
                )
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .post(requestBody)
                .build()
            return@withContext try {
                val execute = client
                    .newCall(request)
                    .execute()
                return@withContext execute.isSuccessful
            } catch (_: IOException) {
                return@withContext false
            }
        }
    }
}
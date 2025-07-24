package com.gustate.uotan.threads.data.parse

import com.gustate.uotan.threads.data.model.NoApiPostInfo
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import com.gustate.uotan.utils.Utils.Companion.baseUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URLEncoder

class ThreadsParse {

    // 解析主题私有变量
    // 实例化 OkHttpClient
    private val okHttpClient = OkHttpClient()

    suspend fun parseThreadsInfo(
        threads: String,
        onSuccess: (NoApiPostInfo) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val document = Jsoup
                .connect(baseUrl + threads)
                .cookies(Cookies)
                .timeout(TIMEOUT_MS)
                .userAgent(USER_AGENT)
                .get()
            val isBookMark = (document
                .getElementsByClass("block-container lbContainer")
                .first()
                ?.getElementsByClass("contentRow-lesser")
                ?.first()
                ?.getElementsByClass("js-bookmarkText u-srOnly")
                ?.first()
                ?.text()
                ?: "") == "编辑收藏"
            val ipAddress = document
                .getElementsByClass("block-container lbContainer")
                .first()
                ?.getElementsByClass("user-login-ip")
                ?.first()
                ?.text()
                ?: ""
            // 是否为精帖
            val isJingTie = (document
                .getElementsByClass("pairs pairs--columns pairs--fixedSmall pairs--customField")
                .first()
                ?.attr("data-field")
                ?: "") == "jingtie"
            val lockedContent = document
                .getElementsByClass("blockStatus-message blockStatus-message--locked")
                .first()
                ?.text()
                ?: ""
            val isLocked = lockedContent.isNotEmpty()
            withContext(Dispatchers.Main) {
                onSuccess(NoApiPostInfo(isBookMark, ipAddress, isJingTie, isLocked))
            }
        } catch (throwable: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(throwable)
            }
        }
    }

    /**
     *  关注/取消关注
     *  @param userId
     */
    suspend fun follow(
        memberUrl: String,
        onSuccess: () -> Unit,
        onFailure: ((code: Int, body: String) -> Unit),
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        val followUrl = memberUrl + "follow"
        try {
            val hiddenXfToken = parseHiddenXfToken(followUrl)
            // 创建 MultipartBody
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_xfRequestUri", memberUrl)
                .addFormDataPart("_xfWithData", "1")
                .addFormDataPart("_xfToken", hiddenXfToken.xfToken)
                .addFormDataPart("_xfResponseType", "json")
                .build()
            // 创建 Request
            val request = Request.Builder()
                .url(followUrl)
                .addHeader("Cookie", hiddenXfToken.sessionCookie)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Origin", baseUrl)
                .addHeader("Referer", memberUrl)
                .post(requestBody)
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onFailure(response.code, responseBody ?: "")
                    }
                    return@withContext
                }
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }
        } catch (throwable: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(throwable)
            }
        }
    }

    suspend fun bookMark(
        postUrl: String,
        isMarked: Boolean,
        onSuccess: () -> Unit,
        onFailure: ((code: Int, body: String) -> Unit),
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        val bookMarkUrl = postUrl + "bookmark"
        try {
            val hiddenXfToken = parseHiddenXfToken(bookMarkUrl)
            // 创建 MultipartBody
            val addRequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("labels", "")
                .addFormDataPart("message", "")
                .addFormDataPart("_xfRequestUri", postUrl)
                .addFormDataPart("_xfWithData", "1")
                .addFormDataPart("_xfToken", hiddenXfToken.xfToken)
                .addFormDataPart("_xfResponseType", "json")
                .build()
            val delRequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("labels", "")
                .addFormDataPart("message", "")
                .addFormDataPart("delete", "undefined")
                .addFormDataPart("_xfRequestUri", postUrl)
                .addFormDataPart("_xfWithData", "1")
                .addFormDataPart("_xfToken", hiddenXfToken.xfToken)
                .addFormDataPart("_xfResponseType", "json")
                .build()
            // 创建 Request
            val request = Request.Builder()
                .url(bookMarkUrl)
                .addHeader("Cookie", hiddenXfToken.sessionCookie)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Origin", baseUrl)
                .addHeader("Referer", postUrl)
                .post(if (isMarked) delRequestBody else addRequestBody)
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onFailure(response.code, responseBody ?: "")
                    }
                    return@withContext
                }
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }
        } catch (throwable: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(throwable)
            }
        }
    }

    /**
     * 举报内容
     */
    suspend fun report(
        url: String,
        message: String,
        onSuccess: () -> Unit,
        onFailure: ((code: Int, body: String) -> Unit),
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        parseHiddenInput(
            url = url,
            onSuccess = { xfToken, _, _ ->
                postReport(
                    url, message, xfToken,
                    onSuccess = {
                        onSuccess()
                    },
                    onFailure = { code, body ->
                        onFailure(code, body)
                    },
                    onThrowable = {
                        onThrowable(it)
                    }
                )
            },
            onThrowable = {
                onThrowable(it)
            }
        )
    }

    /**
     *
     */
    fun postReport(
        url: String,
        message: String,
        xfToken: String,
        onSuccess: () -> Unit,
        onFailure: ((code: Int, body: String) -> Unit),
        onThrowable: (Throwable) -> Unit
    ) {
        try {
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
                .url(baseUrl + url)
                .addHeader("Cookie", Cookies.map2StringCookie())
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
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                if (!response.isSuccessful) {
                    onFailure(response.code, responseBody ?: "")
                    return
                }
                onSuccess()
            }
        } catch (throwable: Throwable) {
            onThrowable(throwable)
        }
    }

    suspend fun parseIp (
        url: String,
        onSuccess: (String) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val document = Jsoup
                .connect(url)
                .cookies(Cookies)
                .timeout(TIMEOUT_MS)
                .userAgent(USER_AGENT)
                .get()
            val ip = document
                .getElementsByClass("ip")
                .first()
                ?.text()
                ?: ""
            withContext(Dispatchers.Main) {
                onSuccess(ip)
            }
        } catch (throwable: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(throwable)
            }
        }
    }

    /**
     * 解析一次性 post key
     * @param url 主题地址
     * @param onSuccess 解析成功后的回调
     *        包括 xfToken, attachmentHash, attachmentHashCombined
     * @param onThrowable 抛出错误的回调 包括错误信息
     */
    suspend fun parseHiddenInput(
        url: String,
        onSuccess: (xfToken: String, attachmentHash: String,
            attachmentHashCombined: String) -> Unit,
        onThrowable: (throwable: Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val document = Jsoup
                .connect(baseUrl + url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .cookies(Cookies)
                .get()
            val xfToken = requireNotNull(
                document.select("input[name=_xfToken]").first()?.attr("value")
            ) { "xfToken not found" }
            val attachmentHash = requireNotNull(
                document.select("input[name=attachment_hash]").first()?.attr("value")
            ) { "attachmentHash not found" }
            val attachmentHashCombined = requireNotNull(
                document.select("input[name=attachment_hash_combined]").first()?.attr("value")
            ) { "attachmentHashCombined not found" }
            onSuccess(xfToken, attachmentHash, attachmentHashCombined)
        } catch (throwable: Throwable) {
            onThrowable(throwable)
        }
    }

    data class HiddenXfToken(
        val xfToken: String,
        val sessionCookie: String
    )

    suspend fun parseHiddenXfToken(url: String): HiddenXfToken = withContext(Dispatchers.IO) {
        val response = Jsoup
            .connect(url)
            .userAgent(USER_AGENT)
            .timeout(TIMEOUT_MS)
            .cookies(Cookies)
            .execute()
        val document = response.parse()
        val xfToken = requireNotNull(
            document.select("input[name=_xfToken]").first()?.attr("value")
        ) { "xfToken not found" }
        return@withContext HiddenXfToken(xfToken, response.cookies().map2StringCookie())
    }

    data class HiddenInput(
        val xfToken: String,
        val attachmentHash: String,
        val attachmentHashCombined: String
    )

    /**
     * 解析一次性 post key
     * @param url 主题地址
     */
    suspend fun parseHiddenInput(url: String): HiddenInput = withContext(Dispatchers.IO) {
        val document = Jsoup
            .connect(baseUrl + url)
            .userAgent(USER_AGENT)
            .timeout(TIMEOUT_MS)
            .cookies(Cookies)
            .get()
        val xfToken = requireNotNull(
            document.select("input[name=_xfToken]").first()?.attr("value")
        ) { "xfToken not found" }
        val attachmentHash = requireNotNull(
            document.select("input[name=attachment_hash]").first()?.attr("value")
        ) { "attachmentHash not found" }
        val attachmentHashCombined = requireNotNull(
            document.select("input[name=attachment_hash_combined]").first()?.attr("value")
        ) { "attachmentHashCombined not found" }
        return@withContext HiddenInput(xfToken, attachmentHash, attachmentHashCombined)
    }

    private fun Map<String, String>.map2StringCookie(): String {
        return entries.joinToString("; ") { (key, value) ->
            "$key=${URLEncoder.encode(value, "UTF-8")}"
        }
    }

}
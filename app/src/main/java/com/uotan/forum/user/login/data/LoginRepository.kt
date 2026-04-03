package com.uotan.forum.user.login.data

import android.util.Log
import com.uotan.forum.utils.Utils
import com.uotan.forum.utils.Utils.baseUrl
import com.uotan.forum.utils.data.model.ErrorResp
import com.uotan.forum.utils.network.BusinessStateHelper
import com.uotan.forum.utils.network.HttpClient
import com.uotan.forum.welcome.ui.model.LoginResult
import com.uotan.forum.welcome.ui.model.TwoFactorInfo
import com.uotan.forum.welcome.ui.model.TwoFactorResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Request
import org.jsoup.Jsoup

class LoginRepository {
    suspend fun login(account: String, password: String): LoginResult =
        withContext(Dispatchers.IO) {
            try {
                val loginUrl = "${baseUrl}/login/login"
                val client = HttpClient.getClient()
                val firstRequest = Request.Builder()
                    .url(loginUrl)
                    .header("User-Agent", Utils.USER_AGENT)
                    .header(
                        "Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
                    )
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .header("Connection", "keep-alive")
                    .build()
                val firstResponse = client.newCall(firstRequest).execute()
                if (!firstResponse.isSuccessful)
                    return@withContext LoginResult.Failure(message = "Unexpected code $firstResponse")
                val responseBody = firstResponse.body.string()
                val document = Jsoup.parse(responseBody)
                // 提取CSRF令牌
                val xfToken = document.select("input[name=_xfToken]")
                    .first()
                    ?.attr("value") ?: throw Exception("CSRF token not found")
                // 构建登录参数
                val formBody = FormBody.Builder()
                    .add("login", account)
                    .add("password", password)
                    .add("_xfToken", xfToken)
                    .add("_xfRedirect", "")
                    .add("remember", "1")
                    .add("accept", "1")
                    .build()
                // 发送登录请求
                val loginRequest = Request.Builder()
                    .url(loginUrl)
                    .header("User-Agent", Utils.USER_AGENT)
                    .post(formBody)
                    .build()
                val loginResponse = client.newCall(loginRequest).execute()
                if (!loginResponse.isSuccessful) {
                    return@withContext LoginResult.Failure(message = "Unexpected code $firstResponse")
                }
                val loginResponseBody = loginResponse.body.string()
                val loginDocument = Jsoup.parse(loginResponseBody)
                val isTwoStep = loginDocument.title() == "两步验证要求 | 柚坛社区"
                val error = loginDocument
                    .getElementsByClass("blockMessage blockMessage--error blockMessage--iconic")
                    .text()
                if (error.isNotEmpty()) {
                    return@withContext LoginResult.Failure(message = "Unexpected code $firstResponse")
                }
                val finalUrl = loginResponse.request.url.toString()
                if (isTwoStep) {
                    // 获取验证类型
                    val provider = Jsoup
                        .parse(loginResponseBody)
                        .select("input[name=provider]")
                        .first()
                        ?.attr("value")
                        ?: ""
                    return@withContext LoginResult.TwoFactor(
                        info = TwoFactorInfo(
                            url = finalUrl,
                            xfToken = xfToken,
                            provider = provider
                        )
                    )
                }
                return@withContext LoginResult.Success
            } catch (e: Exception) {
                return@withContext LoginResult.Error(exception = e)
            }
        }

    suspend fun checkTwoFactor(
        url: String,
        xfToken: String,
        provider: String,
        code: String
    ): TwoFactorResult =
        withContext(context = Dispatchers.IO) {
            try {
                val client = HttpClient.getClient()

                // 构建两步验证参数
                val formBody = FormBody.Builder()
                    .add("_xfToken", xfToken)
                    .add("_xfRedirect", "${baseUrl}/")
                    .add("_xfRequestUri", url.replace(baseUrl, ""))
                    .add("code", code)
                    .add("trust", "1")
                    .add("confirm", "1")
                    .add("provider", provider)
                    .add("remember", "1")
                    .add("_xfWithData", "1")
                    .add("_xfResponseType", "json")
                    .build()

                // 发送两步验证请求
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", Utils.USER_AGENT)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Origin", baseUrl)
                    .header("Referer", url)
                    .post(formBody)
                    .build()

                val response = client.newCall(request).execute()
                // 获取详细信息
                val responseBody = response.body.string()
                Log.e("e", responseBody)
                // 网络逻辑层面是否成功
                val isNetworkSuccess = response.isSuccessful
                // 业务逻辑层面是否成功
                val isBusinessAccepted = BusinessStateHelper
                    .isOperationAccepted(responseBody)
                // 逻辑分支判断
                return@withContext when {
                    !isNetworkSuccess -> {
                        TwoFactorResult.Failure(
                            title = "Internet Error",
                            message = "Login failed with status ${response.code}"
                        )
                    }
                    !isBusinessAccepted -> {
                        val resp = Json.decodeFromString<ErrorResp>(string = responseBody)
                        TwoFactorResult.Failure(
                            title = resp.errorHtml?.title ?: "业务逻辑错误",
                            message = resp.errors?.firstOrNull() ?: "相关业务逻辑发生未知错误"
                        )
                    }
                    else -> TwoFactorResult.Success
                }
            } catch (e: Exception) {
                return@withContext TwoFactorResult.Error(exception = e)
            }
        }
}
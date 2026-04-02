package com.uotan.forum.user.login.data

import com.uotan.forum.utils.Utils
import com.uotan.forum.utils.Utils.baseUrl
import com.uotan.forum.utils.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.IOException

class LoginRepository {
    suspend fun login(account: String, password: String): Result<FirstLoginData> =
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
                    return@withContext Result.failure(
                        IOException("Unexpected code $firstResponse"))
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
                    return@withContext Result.failure(IOException("Unexpected code $loginResponse"))
                }
                val loginResponseBody = loginResponse.body.string()
                val loginDocument = Jsoup.parse(loginResponseBody)
                val isTwoStep = loginDocument.title() == "两步验证要求 | 柚坛社区"
                val error = loginDocument
                    .getElementsByClass("blockMessage blockMessage--error blockMessage--iconic")
                    .text()
                if (error.isNotEmpty()) {
                    return@withContext Result.failure(IOException(error))
                }
                val finalUrl = loginResponse.request.url.toString()
                return@withContext Result.success(
                    FirstLoginData(isTwoStep, finalUrl, xfToken)
                )
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }

    suspend fun twoStep(url: String, xfToken: String, code: String): Result<Map<String, String>> =
        withContext(Dispatchers.IO) {
            try {
                val client = HttpClient.getClient()

                // 构建两步验证参数
                val formBody = FormBody.Builder()
                    .add("_xfToken", xfToken)
                    .add("_xfRedirect", "${baseUrl}/")
                    .add("_xfRequestUri", url.replace(Utils.baseUrl, ""))
                    .add("code", code)
                    .add("trust", "1")
                    .add("confirm", "1")
                    .add("provider", "email")
                    .add("remember", "1")
                    .add("_xfWithData", "1")
                    .add("_xfResponseType", "json")
                    .build()

                // 发送两步验证请求
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", Utils.USER_AGENT)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Origin", Utils.baseUrl)
                    .header("Referer", url)
                    .post(formBody)
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Unexpected code $response"))
                }

                // 获取更新后的 Cookie
                val cookieMap = HttpClient.getAllCookies()
                return@withContext Result.success(cookieMap)
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
    data class FirstLoginData(
        val isTwoStep: Boolean,
        val url: String,
        val xfToken: String
    )
}
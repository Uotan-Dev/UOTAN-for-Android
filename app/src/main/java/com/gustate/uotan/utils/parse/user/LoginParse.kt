package com.gustate.uotan.utils.parse.user

import com.gustate.uotan.utils.Utils.Companion.baseUrl
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup

data class FirstLoginData(
    val cookies: Map<String, String>,
    val isTwoStep: Boolean,
    val url: String,
    val xfToken: String
)

class LoginParse {

    // 伴生对象
    companion object {

        private var cookiesFirst = mapOf<String, String>()

        suspend fun login(account: String, password: String): FirstLoginData = withContext(Dispatchers.IO) {

            val loginUrl = "$baseUrl/login/login"

            // 第一次请求获取CSRF令牌和Cookies
            val firstResponse = Jsoup.connect(loginUrl)
                .userAgent(USER_AGENT)
                .header(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
                )
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Connection", "keep-alive")
                .method(Connection.Method.GET)
                .timeout(TIMEOUT_MS)
                .execute()

            // 提取CSRF令牌（关键安全参数）
            val xfToken = firstResponse.parse()
                .select("input[name=_xfToken]")
                .first()
                ?.attr("value") ?: throw Exception("CSRF token not found")

            // 构建登录参数（注意密码字段名称）
            val params = mapOf(
                "login" to account,
                "password" to password,
                "_xfToken" to xfToken,
                "_xfRedirect" to "",
                "remember" to "1",
                "accept" to "1"
            )

            cookiesFirst = firstResponse.cookies()

            // 发送登录请求（携带初始Cookies）
            val loginResponse = Jsoup.connect(loginUrl)
                .userAgent(USER_AGENT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Origin", "https://www.uotan.cn")
                .header("Referer", loginUrl)
                .cookies(firstResponse.cookies()) // 保持会话连续性
                .data(params)
                .method(Connection.Method.POST)
                .timeout(TIMEOUT_MS)
                .execute()

            // 获取最终Cookies
            val cookies = loginResponse.cookies()

            val isTwoStep = loginResponse.parse().title() == "两步验证要求 | 柚坛社区"

            val url = loginResponse.url().toString()

            return@withContext FirstLoginData(cookies, isTwoStep, url, xfToken)

        }

        suspend fun twoStep(url: String, xfToken: String, code: String): Map<String, String> = withContext(Dispatchers.IO) {

            // 构建登录参数（注意密码字段名称）
            val params = mapOf(
                "_xfToken" to xfToken,
                "_xfRedirect" to "https://www.uotan.cn/",
                "_xfRequestUri" to url.replace("https://www.uotan.cn", ""),
                "code" to code,
                "trust" to "1",
                "confirm" to "1",
                "provider" to "email",
                "remember" to "1",
                "_xfWithData" to "1",
                "_xfResponseType" to "json"
            )

            // 发送登录请求（携带初始 Cookies）
            val loginResponse = Jsoup.connect(url)
                .ignoreContentType(true)
                .userAgent(USER_AGENT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Origin", "https://www.uotan.cn")
                .header("Referer", url)
                .data(params)
                .cookies(cookiesFirst)
                .method(Connection.Method.POST)
                .timeout(TIMEOUT_MS)
                .execute()

            return@withContext loginResponse.cookies()

        }
    }
}
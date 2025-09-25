package com.gustate.uotan.startup.data.parse

import com.gustate.uotan.startup.data.model.StartupTypeData
import com.gustate.uotan.utils.Utils.USER_AGENT
import com.gustate.uotan.utils.Utils.baseUrl
import com.gustate.uotan.utils.Utils.isLogin
import com.gustate.uotan.utils.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.jsoup.Jsoup

class StartupParse {
    suspend fun parseStartupType(): Result<StartupTypeData> =
        withContext(Dispatchers.IO) {
            try {
                if (isLogin) {
                    val client = HttpClient.getClient()
                    val request = Request.Builder()
                        .header("User-Agent", USER_AGENT)
                        .url(baseUrl)
                        .build()
                    val response = client.newCall(request).execute().body.string()
                    val document = Jsoup.parse(response)
                    val noticeTitle = document
                        .getElementsByClass("notice-content")
                        .first()
                        ?.ownText()
                        ?: ""
                    val pageTitle = document
                        .select("#main-header > div > div > div > h1")
                        .first()
                        ?.text()
                        ?: ""
                    val xfToken = document
                        .select("input[name=_xfToken]")
                        .first()
                        ?.attr("value") ?: throw Exception("CSRF token not found")
                    val isAgreement = pageTitle == "隐私政策" || pageTitle == "服务协议"
                    val isSmsVerify = noticeTitle == "您需要验证手机号才能使用全部功能（仅限中国大陆）"
                    return@withContext Result.success(
                        StartupTypeData(
                            isSmsVerify, isAgreement, xfToken
                        )
                    )
                } else {
                    return@withContext Result.success(StartupTypeData())
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
}
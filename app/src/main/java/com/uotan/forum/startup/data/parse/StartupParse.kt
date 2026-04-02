package com.uotan.forum.startup.data.parse

import com.uotan.forum.startup.data.model.StartupTypeData
import com.uotan.forum.utils.Utils.USER_AGENT
import com.uotan.forum.utils.Utils.baseUrl
import com.uotan.forum.utils.Utils.isLogin
import com.uotan.forum.utils.network.HttpClient
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
                throw e
                //return@withContext Result.failure(e)
            }
        }
}

fun main() {
    val x = 2
    // 这里的变量 fx 先存储 f(x) 的结果
    val fx = f(x)
    // 接下来我们把 fx 传入 g, 也就是函数 g 里面的 x
    println(g(fx))
    // 我们也可以这么写，这不就和数学一样了嘛！
    println(g(f(x)))
}

// 所以本质上这两个函数是两个不同的加工厂
fun f(x: Int) = x * x - x
fun g(x: Int) = 2 * x + 1

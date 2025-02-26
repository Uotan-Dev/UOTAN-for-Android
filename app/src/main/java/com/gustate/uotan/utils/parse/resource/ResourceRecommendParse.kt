package com.gustate.uotan.utils.parse.resource

import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import org.jsoup.Connection
import org.jsoup.Jsoup

data class ResourceRecommendItem(
    val cover:  String,
    val topic: String,
    val title: String,
    val version: String,
    val updateTime: String,
    val downloadCount: String,
    val price: String,
    val link: String
)

// 获取柚坛社区资源库推荐的类
class ResourceRecommendParse {

    // 伴生对象
    companion object {

        // 一个协程方法，用于获取分析柚坛社区的 Document, 返回上面那个数据类的 MutableList
        /*suspend fun fetchResourceRecommendData(): MutableList<ResourceRecommendItem> = withContext(Dispatchers.IO) {

            // 设置一个变量存储柚坛社区的网址
            val basicUrl = "https://www.uotan.cn/"

            try {

                // 获取资源库推荐的 Document
                val document = Jsoup.parse(URL("https://www.uotan.cn/resources/featured"),30000)

                /*
                 *  爬取资源库推荐
                 *  @JiaGuZhuangZhi (LOVE JIANGXUN)
                 *  感谢 Jsoup 项目
                 */

                val element = document.getElementsByClass("structItemContainer").first()
                val coverElements = element.getElementsByTag("img")


            } finally {

            }

            return

        }*/

    }

}

fun main() {

    // 获取推荐资源的网页 document 文档
    val document = Jsoup.connect("https://www.uotan.cn/resources/featured")
        .userAgent(USER_AGENT)
        .timeout(TIMEOUT_MS)
        .cookies(login("汩汩加热装置","20090714mzz"))
        .get()


    val rootElements = document.getElementsByClass("structItemContainer").first()

    val resElements = rootElements!!.getElementsByTag("div")

    for (resElement in resElements) {


    }

}

fun login(account: String, password: String): Map<String, String> {

    val loginUrl = "https://www.uotan.cn/login/login"

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

    return cookies

}
package com.gustate.uotan.utils.parse.user

import android.content.Context
import com.gustate.uotan.utils.Utils.Companion.baseUrl
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

/**
 * 隐私政策更新数据类 (DataClass)
 * JiaGuZhuangZhi Miles
 * Gustate 02/24/2025
 * I Love Jiang’Xun
 */

data class PolicyUpdateInfo(
    val updateTime: String,
    val type: Int,
    val content: String
)

/**
 * 隐私政策更新解析类 (ParseUtils)
 * JiaGuZhuangZhi Miles
 * Gustate 02/24/2025
 * I Love Jiang’Xun
 */

class PolicyParse {

    // 伴生对象
    companion object {

        /**
         * 读新版隐私政策
         */
        suspend fun readPolicy(context: Context): PolicyUpdateInfo = withContext(Dispatchers.IO) {

            /** 获取网页 Document 文档 **/
            val document = Jsoup.connect(baseUrl)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .cookies(Cookies)
                .get()

            /** 获取隐私政策更新时间 **/
            // 获取根 Element
            val rootElement = document
                .getElementsByClass("p-body")
                .first()
            // 获取时间 Element
            val timeElement = rootElement
                ?.getElementsByClass("blockMessage blockMessage--iconic blockMessage--important")
                ?.first()
            // 获取时间内容
            val time = timeElement
                ?.getElementsByTag("time")
                ?.first()
                ?.attr("data-date-string")
                ?: ""

            val titleText = document
                .select("#main-header > div > div > div > h1")
                .first()
                ?.text()
                ?: ""

            val type = when (titleText) {
                "隐私政策" -> 1
                "服务协议" -> 2
                else -> 3
            }

            /** 获取隐私政策更新内容 **/
            val content = if (type == 1) {
                document
                    .select("#yesSideBar > div > div > div.block > div > div > div")
                    .html()
            } else {
                document
                    .select("#yesSideBar > div > div > div.block > div > div")
                    .html()
            }

            /** 返回值 **/
            return@withContext PolicyUpdateInfo(time, type, content)

        }
    }
}
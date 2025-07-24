package com.gustate.uotan.home.data.parse

import com.gustate.uotan.home.data.model.LatestItem
import com.gustate.uotan.utils.Utils.Companion.baseUrl
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class LatestParse {

    /**
     * 爬取/解析最新帖子
     * @param onSuccess 爬取成功时回调并附带结果
     * @param onException 爬取失败时回调并附带错误信息
     */
    suspend fun parseLatest(
        onSuccess: (MutableList<LatestItem>) -> Unit,
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            // 储存结果的变量
            val latestList = mutableListOf<LatestItem>()
            // 解析网页
            val document = Jsoup.connect("$baseUrl/whats-new/")
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .cookies(Cookies)
                .get()
            // 获取全部帖子元素
            val elements = document
                .getElementsByClass("structItemContainer")
                .first()
                ?.select("> div")
            // 逐一解析元素
            elements?.forEach { element ->
                // 解析封面
                val coverElement = element
                    .getElementsByClass("structItem-cell structItem-cell--icon")
                    .first()
                val cover = if (
                    coverElement!!.getElementsByTag("img").attr("src") != "https://www.uotan.cn/img/forums/%E5%B8%96%E5%AD%90.png"
                    && coverElement.getElementsByTag("img").attr("src") != ""
                ) {
                    baseUrl + coverElement.getElementsByTag("img").attr("src")
                } else {
                    ""
                }
                // 解析帖子信息
                val titleCell = element
                    .getElementsByClass("structItem-title")
                    .first()
                val link = titleCell
                    ?.attr("uix-href")
                    ?.replace("/unread", "/")
                    ?: ""
                val title = titleCell
                    ?.getElementsByTag("a")
                    ?.first()
                    ?.text()
                    ?: ""
                val topic = if (titleCell?.getElementsByTag("span")?.first() != null) {
                    titleCell
                        .getElementsByTag("span")
                        .first()
                        ?.text()
                        ?: ""
                } else ""
                // 解析作者信息
                val minorCell = element
                    .getElementsByClass("structItem-minor")
                    .first()
                val author = minorCell
                    ?.getElementsByClass("username ")
                    ?.first()
                    ?.text()
                    ?: ""
                val userId = minorCell
                    ?.getElementsByClass("username ")
                    ?.first()
                    ?.attr("data-user-id")
                    ?: ""
                // 解析其他信息
                val time = minorCell
                    ?.getElementsByClass("u-dt")
                    ?.first()
                    ?.attr("title")
                    ?.replace("， ", " ")
                    ?: ""
                val commentCountElement = element
                    .getElementsByClass("pairs pairs--justified")
                    .first()
                val commentCount = commentCountElement
                    ?.getElementsByTag("dd")
                    ?.text()
                    ?: ""
                val viewCountElement = element
                    .getElementsByClass("pairs pairs--justified structItem-minor")
                    .first()
                val viewCount = viewCountElement
                    ?.getElementsByTag("dd")
                    ?.text()
                    ?: ""
                latestList.add(LatestItem(title, cover, author, userId, time, topic, viewCount,
                    commentCount, link))
            }
            withContext(Dispatchers.Main) {
                onSuccess(latestList)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onException(e)
            }
        }
    }
}
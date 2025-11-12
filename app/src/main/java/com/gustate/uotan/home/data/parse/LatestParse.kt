package com.gustate.uotan.home.data.parse

import com.gustate.uotan.home.data.model.LatestItem
import com.gustate.uotan.utils.Utils.baseUrl
import com.gustate.uotan.utils.Utils.USER_AGENT
import com.gustate.uotan.utils.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
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
            val client = HttpClient.getClient()
            // 解析网页
            val request = Request.Builder()
                .url("$baseUrl/whats-new/")
                .header("User-Agent", USER_AGENT)
                .build()
            val response = client.newCall(request).execute()
            val document = Jsoup.parse(response.body.string())
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
                    coverElement!!
                        .getElementsByTag("img")
                        .attr("src")
                    != "/img/forums/%E5%B8%96%E5%AD%90.png"
                    && coverElement.getElementsByTag("img").attr("src")
                    != ""
                    && coverElement.getElementsByTag("img").attr("src")
                    != "$baseUrl/img/forums/%E5%B8%96%E5%AD%90.png"
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
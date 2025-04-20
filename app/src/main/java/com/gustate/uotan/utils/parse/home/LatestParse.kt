package com.gustate.uotan.utils.parse.home

import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

data class ForumLatestItem(
    val title: String,
    val cover: String,
    val author: String,
    val userId: String,
    val time: String,
    val topic: String,
    val viewCount: String,
    val commentCount: String,
    val url: String
)

class LatestParse {

    // 伴生对象
    companion object {

        // 协程函数
        suspend fun fetchLatestData(): MutableList<ForumLatestItem> = withContext(Dispatchers.IO) {

            val result = mutableListOf<ForumLatestItem>()

            // 解析网页, document 返回的就是网页 Document 对象
            val document = Jsoup.connect("$BASE_URL/whats-new/")
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .cookies(Cookies)
                .get()

            val rootElements = document.getElementsByClass("structItemContainer").first()
            val mainElements = rootElements!!.select("> div")
            for (element in mainElements) {

                val coverElement = element
                    .getElementsByClass("structItem-cell structItem-cell--icon")
                    .first()
                val cover = if (
                    coverElement!!.getElementsByTag("img").attr("src") != "https://www.uotan.cn/img/forums/%E5%B8%96%E5%AD%90.png"
                    && coverElement.getElementsByTag("img").attr("src") != ""
                ) {
                    BASE_URL + coverElement.getElementsByTag("img").attr("src")
                } else {
                    ""
                }

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
                } else {
                    ""
                }

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

                result.add(ForumLatestItem(title, cover, author, userId, time, topic, viewCount, commentCount, link))

            }

            return@withContext result

        }
    }
}

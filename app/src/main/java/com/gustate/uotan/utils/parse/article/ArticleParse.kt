package com.gustate.uotan.utils.parse.article

import android.content.Context
import androidx.core.content.ContextCompat.getString
import com.gustate.uotan.R
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

data class TagInfo(
    val name: String,
    val url: String
)

data class SectionInfo(
    val name: String,
    val url: String
)

data class ForumArticle(
    val topic: String,
    val title: String,
    val numberOfComments: String,
    val pageView: String,
    val tags: MutableList<TagInfo>,
    val section: MutableList<SectionInfo>,
    val totalPage: String,
    val avatarUrl: String,
    val authorName: String,
    val authorUrl: String,
    val time: String,
    val ipAddress: String,
    val article: String,
    val numberOfLikes: String,
    val isBilibili: Boolean,
    val bilibiliVideoLink: String
)

class ArticleParse {

    companion object {

        suspend fun articleParse(context: Context, url: String): ForumArticle = withContext(Dispatchers.IO) {

            /**
             * 此 Document 对象就是网页的 document
             */
            val document = Jsoup
                .connect(BASE_URL + url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .cookies(Cookies)
                .get()

            /**
             * pTitleElement 包括话题和标题
             */
            val pTitleElement = if (
                document
                    .getElementsByClass("p-title ").first() != null
            )
            {
                document
                    .getElementsByClass("p-title ")
                    .first()
            } else {
                document
                    .getElementsByClass("p-title")
                    .first()
            }

            // 话题
            val topic = pTitleElement
                ?.getElementsByClass("label label--uotan-threads")
                ?.first()
                ?.text()
                ?: ""
            // 标题
            val title = pTitleElement
                ?.getElementsByClass("p-title-value")
                ?.first()
                ?.ownText()
                ?: ""

            /**
             * pDescriptionElements 包含评论数、浏览量和标签
             * 其中每个 li 标签 从上到下表示评论数、浏览量和标签
             */
            val upPDescriptionElement = document
                .getElementsByClass("p-description")
                .first()
            val pDescriptionElements = upPDescriptionElement
                ?.getElementsByClass("listInline listInline--bullet")
                ?.first()
                ?.getElementsByTag("li")
            // 评论数
            val numberOfComments = pDescriptionElements!![0]
                .ownText()
            // 评论数
            val pageView = pDescriptionElements[1]
                .ownText()
            // 标签
            val tags: MutableList<TagInfo> = pDescriptionElements
                .takeIf { it.size >= 3 }
                ?.get(2)
                ?.getElementsByClass("js-tagList")
                ?.first()
                ?.getElementsByTag("a")
                // 使用 map 转换每个元素
                ?.map { tagElement ->
                    TagInfo(
                        tagElement.text().trim(),
                        tagElement.attr("href")
                    )
                }?.toMutableList()  // 转换为可变列表
                ?: mutableListOf()  // 空列表兜底

            val sectionElement = document
                .getElementsByClass("p-breadcrumbs ")
                .first()
                ?.getElementsByTag("li")
                ?.last()

            val section: MutableList<SectionInfo> = sectionElement
                ?.getElementsByTag("a")
                // 使用 map 转换每个元素
                ?.map { tagElement ->
                    SectionInfo(
                        tagElement.getElementsByTag("span").first()?.text()!!,
                        tagElement.attr("href")
                    )
                }?.toMutableList()  // 转换为可变列表
                ?: mutableListOf()  // 空列表兜底

            val totalPage = document
                .getElementsByClass("pageNav-main")
                .first()
                ?.getElementsByTag("li")
                ?.last()
                ?.text()
                ?: ""

            /*
             * 是否文章、点赞数
             */
            val articleElement = document
                .getElementsByClass("block-container lbContainer")
                .first()

            // 作者头像
            val avatarUrl = articleElement
                ?.getElementsByClass("avatarWrapper")
                ?.first()
                ?.getElementsByTag("img")
                ?.first()
                ?.attr("srcset")
                ?: ""

            val author = articleElement
                ?.getElementsByClass("contentRow-header")
                ?.first()
                ?.getElementsByTag("a")
                ?.first()

            val authorName = author
                ?.text()
                ?: ""
            val authorUrl = author
                ?.attr("href")
                ?: ""

            val contentRowLesserElement = articleElement
                ?.getElementsByClass("contentRow-lesser")
                ?.first()

            val time = contentRowLesserElement!!
                .getElementsByTag("time")
                .first()!!
                .attr("data-date-string")

            val secInfo = contentRowLesserElement
                .getElementsByClass("message-attribution-opposite message-attribution-opposite--list ")
                .first()

            val ipAddress = secInfo!!
                .getElementsByClass("user-login-ip")
                .first()
                ?.text()
                ?: getString(context, R.string.unknown)

            val articleHtml = articleElement
                .getElementsByClass("bbWrapper")
                .first()
                ?.html()!!

            val article = articleElement
                .getElementsByClass("bbWrapper")
                .first()

            val isBilibiliVideo = article
                ?.getElementsByTag("span")
                ?.first()
                ?.attr("data-guineapigclub-mediaembed") ==
                    "bilibili"

            val bilibiliLink = article
                ?.getElementsByTag("iframe")
                ?.first()
                ?.attr("data-guineapigclub-mediaembed-src")
                ?: "noBilibili"

            val loveLink = articleElement
                .getElementsByClass("reactionsBar-link")
                .first()
                ?.attr("href")
                ?: ""

            val numberOfLikes = if (loveLink != "") {
                val loveDocument = Jsoup.connect(BASE_URL + loveLink)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .cookies(Cookies)
                    .get()
                val loveString = loveDocument.getElementsByClass("tabs-tab tabs-tab--reaction0 is-active").first()?.text()
                loveString!!.replace("[^0-9]".toRegex(), "")
            } else "0"

            return@withContext ForumArticle(
                topic,
                title,
                numberOfComments,
                pageView,
                tags,
                section,
                totalPage,
                avatarUrl,
                authorName,
                authorUrl,
                time,
                ipAddress,
                articleHtml,
                numberOfLikes,
                isBilibiliVideo,
                bilibiliLink
            )

        }
    }
}
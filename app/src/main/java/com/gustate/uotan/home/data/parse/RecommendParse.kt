package com.gustate.uotan.home.data.parse

import com.gustate.uotan.home.data.model.Recommend
import com.gustate.uotan.home.data.model.RecommendItem
import com.gustate.uotan.utils.Utils.Companion.baseUrl
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class RecommendParse {

    /**
     * 爬取/解析推荐数据
     * @param page 爬取页码
     * @param onSuccess 解析成功回调且附带推荐信息
     * @param onException 解析错误回调且附带错误信息
     */
    suspend fun parseRecommend(
        page: Int,
        onSuccess: (Recommend) -> Unit,
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            // 储存结果的变量
            val recommendList = mutableListOf<RecommendItem>()
            // 爬取页面
            val pageUrl = buildPageUrl(page)
            val document = Jsoup.connect(pageUrl)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .cookies(Cookies)
                .get()
            val totalPage = parsePager(document)
            val content = document
                .getElementsByClass("block  porta-masonry")
                .first()
            val elements = content?.getElementsByClass("porta-article-item")
            // 逐一爬取内容
            elements?.forEach { element ->
                val urlP = element
                    .getElementsByClass("block-container porta-article-container")
                    .first()!!
                    .attr("onclick")
                val regex1 = """window\.open\('([^']+)'\)""".toRegex()
                val url = regex1.find(urlP)?.groupValues?.get(1) ?: ""
                // 获取文章标题
                val title = element
                    .getElementsByClass("porta-header-text")
                    .first()
                    ?.getElementsByTag("span")
                    ?.first()
                    ?.text()
                    ?: ""
                // 获取文章简介
                val describe = element
                    .getElementsByClass("message-body")
                    .first()
                    ?.getElementsByClass("bbWrapper")
                    ?.first()
                    ?.text()
                    ?: ""
                // 获取文章封面
                val coverStyle = element
                    .getElementsByClass("porta-header-image")
                    .first()
                    ?.attr("style")
                    ?: ""
                // 定义一个正则表达式，用来匹配 style 属性中的 URL
                val urlPattern = "url\\('(.*?)'\\)".toRegex()
                // 在 style 属性值中查找符合正则表达式的内容
                val coverUrl = urlPattern
                    .find(coverStyle)
                    ?.groupValues[1]
                    ?.removeSuffix("/")
                    ?: ""
                // 获取作者 ID
                val userId = element
                    .getElementsByClass("avatarWrapper")
                    .first()
                    ?.getElementsByClass("avatar avatar--s")
                    ?.first()
                    ?.attr("data-user-id")
                    ?: ""
                // 获取作者头昵称
                val author = element
                    .getElementsByClass("contentRow-header")
                    .first()
                    ?.getElementsByClass("u-concealed")
                    ?.first()
                    ?.text()
                    ?: ""
                // 获取文章发布时间
                val time = element
                    .getElementsByClass("contentRow-lesser")
                    .first()
                    ?.getElementsByTag("time")
                    ?.first()
                    ?.attr("title")
                    ?.replace("， ", " ")
                    ?: ""
                // 获取话题、浏览量和评论数
                val cellElement = element.getElementsByClass("message-attribution").first()
                // 获取话题
                val topic = cellElement
                    ?.getElementsByClass("label label--uotan-threads")
                    ?.first()
                    ?.text()
                    ?: ""
                // 获取浏览量
                val otherCellElement = cellElement
                    ?.getElementsByClass("listInline listInline--bullet")
                    ?.first()
                // 这里只能获取 li 标签, 因为浏览量的没有其他标识
                // 但是浏览量和评论数都是 li 标签, 所以获取到的是 "浏览量 评论数"
                val viewCCount = otherCellElement
                    ?.getElementsByTag("li")
                    ?.text()
                // 获取 class 为 before-display-none 的 li 标签, 这个里面包裹着评论数
                val commentCount = otherCellElement
                    ?.getElementsByClass("before-display-none")
                    ?.text()
                    ?: ""
                // 刚刚获得了"浏览量 评论数", 和"评论数", 用 replace() 简单替换一下就可以得到浏览量
                val viewCount = viewCCount
                    ?.replace(" $commentCount", "")
                    ?: ""
                // 在结果中赋值
                recommendList.add(RecommendItem(title, describe, coverUrl, userId, author, time,
                    topic, viewCount, commentCount, url))
            }
            withContext(Dispatchers.Main) {
                onSuccess(Recommend(recommendList, totalPage))
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onException(e)
            }
        }
    }

    /**
     * 构建完整链接
     * @param page 页码
     */
    private fun buildPageUrl(page: Int): String {
        return if (page == 1) {
            baseUrl
        } else {
            "$baseUrl/ewr-porta/page-$page"
        }
    }

    /**
     * 解析分页
     * @param document 当前爬取到的内容
     */
    private fun parsePager(document: Document): Int {
        // 解析总页数
        val pageNav = document
            .getElementsByClass("pageNav-main")
            .first()
            ?.getElementsByTag("li")
            ?.last()
        val totalPage = pageNav
            ?.getElementsByTag("a")
            ?.first()
            ?.text()
            ?.toInt()
            ?: 0
        return totalPage
    }
}
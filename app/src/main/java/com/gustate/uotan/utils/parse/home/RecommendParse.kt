package com.gustate.uotan.utils.parse.home

import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.SocketTimeoutException

data class ForumRecommendItem(
    val title: String,
    val describe: String,
    val cover: String,
    val authorAvatar: String,
    val author: String,
    val time: String,
    val topic: String,
    val viewCount: String,
    val commentCount: String,
    val url: String
)

data class FetchResult(
    val items: MutableList<ForumRecommendItem>,
    val totalPage: Int
)

class RecommendParse {

    // 伴生对象
    companion object {

        suspend fun fetchRecommendData(pageCount: Int): FetchResult = withContext(Dispatchers.IO) {

            // 创建一个储存结果的对象
            val result = mutableListOf<ForumRecommendItem>()

            var totalPage = 1

            try {

                val pageUrl = buildPageUrl(pageCount)


                // 解析网页, document 返回的就是网页 Document 对象
                val document = Jsoup.connect(pageUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .cookies(Cookies)
                    .get()

                /** 爬取总页数 **/
                // 获取总页数所在 class
                val pageNavS = document.getElementsByClass("pageNav-main").first()
                val pageNav = pageNavS!!.getElementsByTag("li").last()
                // 提取 a 标签数据, 即总页数
                totalPage = pageNav!!.getElementsByTag("a").first()!!.text().toInt()


                // 查找 block  porta-masonry 类
                val content = document.getElementsByClass("block  porta-masonry").first()
                // 爬取所有文章的 item
                val elements = content!!.getElementsByClass("porta-article-item")
                // 获取内容，这里的 element 就是每一个 div 元素
                for (element in elements) {

                    // 爬取推荐每个元素

                    val urlP = element
                        .getElementsByClass("block-container porta-article-container")
                        .first()!!
                        .attr("onclick")
                    val regex1 = """window\.open\('([^']+)'\)""".toRegex()
                    val url = regex1.find(urlP)?.groupValues?.get(1) ?: ""

                    /** 获取文章标题 **/
                    // 获取标题所在 Element
                    val titleElement = element.getElementsByClass("porta-header-text").first()
                    // 获取其中 span 包裹的标题
                    val title = titleElement!!.getElementsByTag("span").first()!!.text()

                    /** 获取文章简介 **/
                    // 获取简介所在 Element
                    val describeElement = element.getElementsByClass("message-body").first()
                    // 获取其中 bbWrapper 类中包裹的简介
                    val describe = describeElement!!.getElementsByClass("bbWrapper").first()!!.text().toString().replace("<br>", "\n")

                    /** 获取文章封面 **/
                    // 获取封面所在 Element
                    val coverElement = element.getElementsByClass("porta-header-image").first()
                    // 获取每个元素的 style 属性值, 这个属性值包含封面的 URL
                    val styleAttr = coverElement!!.attr("style")
                    // 定义一个正则表达式，用来匹配 style 属性中的 URL
                    val urlPattern = "url\\('(.*?)'\\)".toRegex()
                    // 在 style 属性值中查找符合正则表达式的内容
                    val coverUrl = urlPattern.find(styleAttr)!!.groupValues[1]

                    /** 获取作者头像 **/
                    // 获取头像所在的 Wrapper
                    val avatarElement = element.getElementsByClass("avatarWrapper").first()
                    // 作者头像分为两种，一种用户自定义了头像，一种用户未自定义头像使用其昵称的第一个字符
                    // 存储有头像用户的头像，无头像用户没有这个 tag
                    val imgElement = avatarElement!!.getElementsByTag("img").first()
                    // 这个 span 中还有一个 span 包裹了用户昵称的第一个字符，但所有用户都有这个 span
                    val spanElement = avatarElement.getElementsByTag("span").first()
                    // 建立一个储存头像的变量
                    val avatar: String
                    // 综上，我们判断用户是否有头像的条件是 imgElement 是否为空
                    if (imgElement != null) {
                        // 如果不为空就获取其中的 attr : "srcset" (这里获取 src 可能会出错，因为采用的懒加载)
                        val src = imgElement.attr("srcset")
                        // 然后赋值到 avatar
                        avatar = BASE_URL + src
                    } else {
                        // 如果为空就获取 span 中的 span 所包裹的信息，然后把他赋值到 avatar
                        avatar = spanElement!!.getElementsByTag("span").first()!!.text()
                    }

                    /** 获取作者头昵称 **/
                    // 获取昵称所在的 Element
                    val authorElement = element.getElementsByClass("contentRow-header").first()
                    // 取 Element 中 u-concealed 类包裹的昵称, 并赋值到 author
                    val author = authorElement!!.getElementsByClass("u-concealed").first()!!.text()

                    /** 获取文章发布时间 **/
                    // 获取发布时间所在的 Element
                    val timeElement = element.getElementsByClass("contentRow-lesser").first()
                    // 取 Element 中 u-dt 类包裹的昵称, 并赋值到 time
                    val time = timeElement!!.getElementsByClass("u-dt").first()!!.text()

                    /** 获取话题、浏览量和评论数 **/
                    // 获取话题、浏览量和评论数所在的 Element
                    val cellElement = element.getElementsByClass("message-attribution").first()
                    /* 获取话题 */
                    // 获取话题所在的 Element, 注意：并不是每一篇文章都有话题
                    val topicElement =
                        cellElement!!.getElementsByClass("label label--uotan-threads").first()
                    // 如果有话题就赋值, 没有话题就赋值 "" 空字符串
                    // 综上, 我们不能用 !! 断定非 null, 接下来我们鉴空
                    // 创建一个储存话题的变量
                    val topic: String = if (topicElement != null) topicElement.text() else ""
                    /* 获取浏览量 */
                    // 获取浏览量和评论数所在的 Element
                    val otherCellElement =
                        cellElement.getElementsByClass("listInline listInline--bullet").first()
                    // 这里只能获取 li 标签, 因为浏览量的没有其他标识, 但是浏览量和评论数都是 li 标签, 所以获取到的是 "浏览量 评论数"
                    val viewCCount = otherCellElement!!.getElementsByTag("li").text()
                    // 获取 class 为 before-display-none 的 li 标签, 这个里面包裹着评论数
                    val commentCount =
                        otherCellElement.getElementsByClass("before-display-none").text()
                    // 刚刚获得了"浏览量 评论数", 和"评论数", 用 replace() 简单替换一下就可以得到浏览量
                    val viewCount = viewCCount.replace(" $commentCount", "")

                    // 在结果中赋值
                    result.add(ForumRecommendItem(title, describe, coverUrl, avatar, author, time, topic, viewCount, commentCount, url))

                }

                return@withContext FetchResult(result, totalPage)

            } catch (e: SocketTimeoutException) {

                e.printStackTrace()

                println("Error")

                return@withContext FetchResult(result, totalPage)

            }



        }

        private fun buildPageUrl(pageCount: Int): String {
            return if (pageCount == 1) {
                BASE_URL
            } else {
                "$BASE_URL/ewr-porta/page-$pageCount"
            }
        }

    }

}
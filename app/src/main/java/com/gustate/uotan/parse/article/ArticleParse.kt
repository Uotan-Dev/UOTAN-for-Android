package com.gustate.uotan.parse.article

import android.text.SpannableStringBuilder
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

data class ForumAritcle(
    val topic: String,
    val title: String,
    val commentCount: String,
    val viewCount: String,
    val tag: MutableList<String>,

    val cover: String,
    val author: String,
    val time: String,


    val link: String
)

class ArticleParse {

    companion object {

        suspend fun ArticleParse(pageCount: Int): SpannableStringBuilder = withContext(Dispatchers.IO) {

            // 构建一个 SpannableStringBuilder 类的实例
            val builder = SpannableStringBuilder()

            // 获取文章的网页 document 文档
            val document = Jsoup.connect("https://www.uotan.cn/threads/uotan.38229/")
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .cookies(Cookies)
                .get()

            val mainCell = document.getElementsByClass("uix_headerInner").first()

            val rootElement = document.getElementsByClass("block-body js-replyNewMessageContainer").first()

            val articleElements = rootElement!!.getElementsByClass("message    message-threadStarterPost message--post  js-post js-inlineModContainer  ").first()

            val articleElement = articleElements!!.getElementsByClass("message-userContent lbContainer js-lbContainer ").first()

            val article = articleElement!!.selectFirst("div.bbWrapper")!!.toString().replace("<br>","\n")


            return@withContext builder

        }


    }

}

fun main() {
    // 获取文章的网页 document 文档
    val document = Jsoup.connect("https://www.uotan.cn/threads/uotan.38229/")
        .userAgent("UotanAPP/1.0")
        .timeout(30000)
        .get()

    val mainCell = document.getElementsByClass("uix_headerInner").first()

    val title = mainCell.getElementsByClass("p-title-value").first().text()

    println(title)

    val topic: String

    if (mainCell.getElementsByClass("label label--uotan-threads") != null) {

        topic = mainCell.getElementsByClass("label label--uotan-threads").text()

    } else {

        topic = ""

    }

    println(topic)

    val otherCell = document.getElementsByClass("listInline listInline--bullet").first()

    for (index in otherCell.getElementsByTag("li").indices) {

        val commentCount = otherCell.getElementsByTag("li")[0].ownText()

        println(commentCount)

        val viewCount = otherCell.getElementsByTag("li")[1].ownText()

        println(viewCount)

        val tagElements = otherCell.getElementsByClass("js-tagList")

        val tagList = mutableListOf<String>()

        for (tagElement in tagElements) {

            val tag = tagElement.getElementsByTag("a").first().text()

            tagList.add(tag)

        }

        println(tagList)

    }

    val rootArticleElement = document.getElementsByClass("block-body js-replyNewMessageContainer").first()

    val articleElements = rootArticleElement!!.getElementsByClass("message    message-threadStarterPost message--post  js-post js-inlineModContainer  ").first()

    val articleElement = articleElements!!.getElementsByClass("message-userContent lbContainer js-lbContainer ").first()

    val bbWrapper = articleElement!!.selectFirst("div.bbWrapper")!!

    // 遍历所有子节点处理混合内容
    /*val elements = bbWrapper.children().flatMap {
        if (it.tagName() == "br") listOf(UCharacter.LineBreak) // 处理换行
        else parseElement(it) // 解析其他元素
    }*/

    val favouriteElements = rootArticleElement.getElementsByClass("reactionsBar js-reactionsList is-active").first()

    val favouriteCount: Int

    if (favouriteElements != null) {

        val favouriteElement = favouriteElements.getElementsByClass("reactionsBar-link")

        favouriteCount = 1 + favouriteElement.select("bdi").size

    } else {
        favouriteCount = 0
    }

    println(favouriteCount)

}

// 示例元素解析函数
/*private fun parseElement(element: Element): List<ContentItem> {
    return when {
        element.tagName() == "b" -> listOf(TextSpan(element.text(), bold=true))
        element.tagName() == "i" -> listOf(TextSpan(element.text(), italic=true))
        element.hasAttr("style") -> handleStyleSpan(element)
        element.tagName() == "img" -> listOf(ImageItem(element.attr("data-src")))
        element.tagName() == "a" -> handleHyperlink(element)
        // ... 其他标签处理
        else -> element
    }
}*/
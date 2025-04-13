package com.gustate.uotan.utils.parse.search

import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class SearchResult (
    val topic: String,
    val url: String,
    val title: String,
    val content: String,
    val author: String,
    val authorUrl: String,
    val type: String,
    val time: String,
    val tag: MutableList<String>,
    val comment: String,
    val plate: String,
    val cover: String
)

data class FetchResult(
    val items: MutableList<SearchResult>,
    val totalPage: Int,
    val nextPageUrl: String
)

class SearchParse {
    companion object {

        suspend fun searchInfoParse(content: String, page: String, isMePage: Boolean = false): FetchResult = withContext(Dispatchers.IO) {
            val document = getDocument(content, page, isMePage)
            val result = searchParse(document)
            return@withContext result
        }

        suspend fun searchInfoParse(url: String): FetchResult = withContext(Dispatchers.IO) {
            val document = getDocument(url)
            val result = searchParse(document)
            return@withContext result
        }

        private suspend fun getDocument(url: String) = withContext(Dispatchers.IO) {
            val document = Jsoup
                .connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .cookies(Cookies)
                .get()
            return@withContext document
        }

        private suspend fun getDocument(search: String, page: String, isMePage: Boolean): Document = withContext(Dispatchers.IO) {
            return@withContext if (isMePage) {
                Jsoup
                    .connect("$BASE_URL/search/$search/?page=$page")
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .cookies(Cookies)
                    .get()
            } else {
                Jsoup
                    .connect("${BASE_URL}search/${(10000..99999).random()}/?page=$page&q=$search&t=post&o=relevance")
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .cookies(Cookies)
                    .get()
            }
        }

        private fun searchParse(document: Document): FetchResult {

            val result = mutableListOf<SearchResult>()

            val blockRows = document
                .getElementsByClass("block-body")
                .first()
                ?.select(":root > li")

            val totalPage = document
                .getElementsByClass("pageNav-main")
                .first()
                ?.getElementsByTag("li")
                ?.last()
                ?.text()
                ?.toInt()
                ?: 1

            val nextPageUrl = document
                .getElementsByClass("pageNav-jump pageNav-jump--next")
                .first()
                ?.attr("href")
                ?: ""

            if (blockRows != null) {
                for (blockRow in blockRows) {
                    val contentRowTitle = blockRow
                        .getElementsByClass("contentRow-title")
                        .first()
                    val topic = contentRowTitle
                        ?.getElementsByClass("label label--uotan-threads")
                        ?.first()
                        ?.text()
                        ?: ""
                    val url = contentRowTitle
                        ?.getElementsByTag("a")
                        ?.first()
                        ?.attr("href")
                        ?: ""
                    val title = contentRowTitle
                        ?.getElementsByTag("a")
                        ?.first()
                        ?.ownText()
                        ?: ""
                    val cover = blockRow
                        .getElementsByClass("avatar avatar--s")
                        .first()
                        ?.attr("src")
                        ?: ""
                    val content = blockRow
                        .getElementsByClass("contentRow-snippet")
                        .first()
                        ?.text()
                        ?: ""
                    val listInLine = blockRow
                        .getElementsByClass("listInline listInline--bullet")
                        .first()
                        ?.getElementsByTag("li")
                    var author = ""
                    var authorUrl = ""
                    var type = ""
                    var time = ""
                    val tag = mutableListOf<String>()
                    var comment = ""
                    var plate = ""
                    if (listInLine != null) {
                        author = listInLine[0]
                            .getElementsByTag("a")
                            .first()
                            ?.text()
                            ?: ""
                        authorUrl = listInLine[0]
                            .getElementsByTag("a")
                            .first()
                            ?.attr("href")
                            ?: ""
                        val typeName = listInLine[1]
                            .text()
                        when {
                            typeName == "主题" -> type = "article"
                            typeName == "提供资源" -> type = "res"
                            typeName.startsWith("帖子") -> type = "post"
                        }
                        time = listInLine[2]
                            .getElementsByTag("time")
                            .first()
                            ?.attr("data-date-string")
                            ?: ""
                        when (listInLine.size) {
                            5 -> {
                                val tagElements = listInLine[3]
                                    .getElementsByTag("span")
                                if (tagElements.isEmpty()) {
                                    comment = listInLine[3]
                                        .text()
                                        .replace("回复: ", "")
                                } else {
                                    tagElements.forEach { tagElement ->
                                        tag.add(tagElement.text())
                                    }
                                }
                            }
                            6 -> {
                                val tagElements = listInLine[3]
                                    .getElementsByTag("span")
                                tagElements.forEach { tagElement ->
                                    tag.add(tagElement.text())
                                }
                                comment = listInLine[4]
                                    .text()
                                    .replace("回复: ", "")
                            }
                        }
                        plate = listInLine
                            .last()
                            ?.getElementsByTag("a")
                            ?.first()
                            ?.text()
                            ?: ""
                    }
                    result.add(SearchResult(topic, url, title, content, author, authorUrl, type, time, tag, comment, plate, cover))
                }
            }
            return FetchResult(result, totalPage, nextPageUrl)
        }
    }
}
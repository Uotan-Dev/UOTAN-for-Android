package com.gustate.uotan.search.data.parse

import android.util.Log
import com.gustate.uotan.search.data.model.SearchItem
import com.gustate.uotan.search.data.model.SearchResult
import com.gustate.uotan.utils.Utils.USER_AGENT
import com.gustate.uotan.utils.Utils.baseUrl
import com.gustate.uotan.utils.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class SearchParse {

    suspend fun parseSearchInfo(
        content: String,
        isMePage: Boolean = false,
        onSuccess: (SearchResult) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val document = getDocument(content, "1", isMePage)
            val result = searchParse(document)
            withContext(Dispatchers.Main) {
                onSuccess(result)
            }
        } catch (t: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(t)
            }
        }
    }

    suspend fun parseSearchInfo(
        url: String,
        onSuccess: (SearchResult) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val document = getDocument(url)
            val result = searchParse(document)
            withContext(Dispatchers.Main) {
                onSuccess(result)
            }
        } catch (t: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(t)
            }
        }
    }

    private suspend fun getDocument(url: String) = withContext(Dispatchers.IO) {
        val client = HttpClient.getClient()
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()
        val response = client.newCall(request).execute()
        val document = Jsoup.parse(response.body.string())
        return@withContext document
    }

    private suspend fun getDocument(search: String, page: String, isMePage: Boolean): Document =
        withContext(Dispatchers.IO) {
            val client = HttpClient.getClient()
            return@withContext if (isMePage) {
                val request = Request.Builder()
                    .url("$baseUrl/search/$search/?page=$page")
                    .header("User-Agent", USER_AGENT)
                    .build()
                val response = client.newCall(request).execute()
                Jsoup.parse(response.body.string())
            } else {
                val request = Request.Builder()
                    .url("$baseUrl/search/${(10000..99999).random()}/?page=$page&q=$search&t=post&o=relevance")
                    .header("User-Agent", USER_AGENT)
                    .build()
                val response = client.newCall(request).execute()
                Jsoup.parse(response.body.string())
            }
        }

    private fun searchParse(document: Document): SearchResult {
        val result = mutableListOf<SearchItem>()

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
                    ?.replace("/unread", "/")
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
                var id = ""
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
                    id = listInLine[0]
                        .getElementsByTag("a")
                        .first()
                        ?.attr("data-user-id")
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
                result.add(
                    SearchItem(
                        topic,
                        url,
                        title,
                        content,
                        author,
                        id,
                        authorUrl,
                        type,
                        time,
                        tag,
                        comment,
                        plate,
                        cover
                    )
                )
            }
        }
        return SearchResult(result, totalPage, nextPageUrl)
    }
    suspend fun parseSearchInfoPage(content: String, page: String): SearchResult {
        return withContext(Dispatchers.IO) {
            val document = getDocument(content, page, false)
            searchParse(document)
        }
    }
}
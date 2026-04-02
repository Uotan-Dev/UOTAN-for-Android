package com.uotan.forum.message.data.parse

import com.uotan.forum.message.data.model.PersonalLetter
import com.uotan.forum.utils.Utils.baseUrl
import com.uotan.forum.utils.Utils.USER_AGENT
import com.uotan.forum.utils.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.jsoup.Jsoup

class PersonalLetterParse {
    suspend fun parsePersonalLetterList(userId: String, page: Int): MutableList<PersonalLetter> = withContext(Dispatchers.IO) {
        val personalLetterList = mutableListOf<PersonalLetter>()
        val client = HttpClient.getClient()
        val request = Request.Builder()
            .url("$baseUrl/conversations/page-$page")
            .header("User-Agent", USER_AGENT)
            .build()
        val response = client.newCall(request).execute()
        val document = Jsoup.parse(response.body.string())
        val totalPage = document
            .getElementsByClass("pageNav-main")
            .first()
            ?.getElementsByTag("li")
            ?.last()
            ?.text()
            ?.toInt()
            ?: 1
        val items = document
            .getElementsByClass("structItemContainer")
            .first()
            ?.getElementsByClass("structItem structItem--conversation  js-inlineModContainer")
        items?.forEach {
            val idElements = it
                .getElementsByClass("listInline listInline--comma listInline--selfInline")
                .first()
                ?.getElementsByTag("li")
            var id = ""
            var author = ""
            idElements?.forEach {
                val uId = it
                    .getElementsByClass("username ")
                    .first()
                    ?.attr("data-user-id")
                    ?: ""
                if (uId != userId) id = it
                    .getElementsByClass("username ")
                    .first()
                    ?.attr("data-user-id")
                    ?: ""
                if (uId != userId) author = it
                    .getElementsByClass("username ")
                    .first()
                    ?.text()
                    ?: ""
            }
            val title = it.getElementsByClass("structItem-title").first()?.text() ?: ""
            val time = it.getElementsByTag("time").first()?.attr("title")?.replace("， ", " ") ?: ""
            val url = it.getElementsByClass("structItem-title").first()?.attr("href") ?: ""
            personalLetterList.add(PersonalLetter(totalPage, id, author, title, time, url))
        }
        return@withContext personalLetterList
    }
}
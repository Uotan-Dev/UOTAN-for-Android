package com.gustate.uotan.message.data.parse

import com.gustate.uotan.message.data.model.PersonalLetter
import com.gustate.uotan.utils.Utils.Companion.baseUrl
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class PersonalLetterParse {
    suspend fun parsePersonalLetterList(userId: String, page: Int): MutableList<PersonalLetter> = withContext(Dispatchers.IO) {
        val personalLetterList = mutableListOf<PersonalLetter>()
        val document = Jsoup
            .connect("$baseUrl/conversations/page-$page")
            .userAgent(USER_AGENT)
            .cookies(Cookies)
            .timeout(TIMEOUT_MS)
            .get()
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
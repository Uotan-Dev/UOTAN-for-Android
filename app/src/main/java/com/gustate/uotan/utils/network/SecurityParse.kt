package com.gustate.uotan.utils.network

import com.gustate.uotan.utils.Utils.USER_AGENT
import com.gustate.uotan.utils.Utils.baseUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.jsoup.Jsoup

object SecurityParse {
    private val client = HttpClient.getClient()
    suspend fun parseHiddenXfToken(url: String): String = withContext(Dispatchers.IO) {
        val finalUrl =
            if (url.startsWith(baseUrl)) url
            else baseUrl + url
        val request = Request.Builder()
            .url(finalUrl)
            .header("User-Agent", USER_AGENT)
            .build()
        val response = client.newCall(request).execute()
        val document = Jsoup.parse(response.body.string())
        val xfToken = requireNotNull(
            document
                .select("input[name=_xfToken]")
                .first()
                ?.attr("value")
        ) { "xfToken not found" }
        return@withContext xfToken
    }
}
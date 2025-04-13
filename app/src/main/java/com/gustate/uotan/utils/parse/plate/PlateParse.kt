package com.gustate.uotan.utils.parse.plate

import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.IOException

data class PlateItem(
    val cover: String,
    val title: String,
    val link: String
)

class PlateParse {
    companion object {
        suspend fun fetchPlateData(content: String): MutableList<PlateItem> = withContext(Dispatchers.IO) {
            try {
                if (content.startsWith("/watched") || content.startsWith("/categories")) {
                    val document = Jsoup
                        .connect(BASE_URL + content)
                        .userAgent(USER_AGENT)
                        .timeout(TIMEOUT_MS)
                        .cookies(Cookies)
                        .get()
                    val itemElements = document
                        .getElementsByClass("block-body")
                        .first()
                        ?.select("div.node--forum")
                    return@withContext fetchContent(itemElements, false)
                } else {
                    val document = Jsoup
                        .connect("$BASE_URL/forums/")
                        .userAgent(USER_AGENT)
                        .timeout(TIMEOUT_MS)
                        .cookies(Cookies)
                        .get()
                    val itemElements = document
                        .getElementsByClass("block block--category block--category$content")
                        .first()
                        ?.getElementsByClass("block-body")
                        ?.first()
                        ?.select("div.block-body > div")
                    if (content == "251 ") {
                        return@withContext fetchContent(itemElements, true)
                    } else {
                        return@withContext fetchContent(itemElements, false)
                    }
                }
            } catch (e: HttpStatusException) {
                return@withContext mutableListOf<PlateItem>()
            } catch (e: IOException) {
                return@withContext mutableListOf<PlateItem>()
            } catch (e: Exception) {
                return@withContext mutableListOf<PlateItem>()
            }
        }

        private fun fetchContent(itemElements: Elements?, del: Boolean): MutableList<PlateItem> {
            val result = mutableListOf<PlateItem>()
            if (itemElements != null) {
                for (itemElement in itemElements) {
                    val aElements = itemElement
                        .getElementsByTag("a")
                    val cover = aElements[0]
                        .getElementsByTag("img")
                        .attr("src")
                    val title = aElements[1]
                        .text()
                    val url = aElements[1]
                        .attr("href")
                    result.add(PlateItem(cover, title, url))
                }
            }
            if (del) result.removeAt(0)
            return result
        }
    }
}

package com.gustate.uotan.utils.parse.user

import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

data class MeInfo(
    val userName: String,
    val cover: String,
    val avatar: String,
    val signature: String
)


class MeParse {

    // 伴生对象
    companion object {

        suspend fun fetchMeData(): MeInfo = withContext(Dispatchers.IO) {

            // 获取推荐资源的网页 document 文档
            val document = Jsoup.connect("$BASE_URL/account/account-details/")
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .cookies(Cookies)
                .get()

            val pageContent = document.getElementsByClass("p-body-pageContent").first()

            val blockBody = pageContent!!.getElementsByClass("block-body").first()

            val formRows = blockBody!!.getElementsByClass("formRow")

            val userName = formRows[0].getElementsByTag("dd").first()!!.ownText()

            val avatarUrl = BASE_URL + formRows[3].getElementsByTag("img").first()!!.attr("srcset")

            val coverStyle = formRows[4].getElementsByTag("dd").first()!!.getElementsByTag("div").first()!!.attr("style")

            val regex = Regex("""url\(([^)]+)""")

            val matchResult = regex.find(coverStyle)

            val coverUrl = (BASE_URL + matchResult?.groupValues?.get(1))

            val signatureElement = formRows[11].getElementsByClass("input")

            val signature = if (signatureElement.size == 1) {
                formRows[12].getElementsByClass("input")[1].text()
            } else {
                formRows[11].getElementsByClass("input")[1].text()
            }

            return@withContext MeInfo(userName, coverUrl, avatarUrl, signature)

        }

    }

}

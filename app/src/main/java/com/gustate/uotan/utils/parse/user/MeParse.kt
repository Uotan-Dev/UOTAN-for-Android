package com.gustate.uotan.utils.parse.user

import android.util.Log
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup

data class MeInfo(
    val userName: String,
    val cover: String,
    val avatar: String,
    val signature: String,
    val auth: String,
    val postCount: String,
    val resCount: String,
    val userId: String,
    val points: String,
    val uCoin: String,
    val ipAddress: String
)

data class ClockIn(
    val isClockIn: Boolean,
    val points: String
)

class MeParse {

    // 伴生对象
    companion object {

        suspend fun fetchMeData(): MeInfo = withContext(Dispatchers.IO) {

            Log.e("1", "111")

            // 获取推荐资源的网页 document 文档
            val document = Jsoup.connect("$BASE_URL/account/account-details/")
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .cookies(Cookies)
                .get()

            val pageContent = document
                .getElementsByClass("p-body-pageContent")
                .first()

            val blockBody = pageContent!!
                .getElementsByClass("block-body")
                .first()

            val formRows = blockBody!!
                .getElementsByClass("formRow")

            var userNamePosition = 0
            var phoneNumberPosition = 1
            var emailPosition = 2
            var emailChoicePosition = 3
            var avatarPosition = 4
            var coverPosition = 5
            var birthdayPosition = 6
            var addressPosition = 7
            var homePosition = 8
            var devicePosition = 9
            var describePosition = 10

            for (index in formRows.indices) {
                val title = formRows[index]
                    .getElementsByTag("dt")
                    .first()
                    ?.getElementsByClass("formRow-label")
                    ?.first()
                    ?.text()
                    ?: ""

                when (title) {
                    "用户名" -> userNamePosition = index
                    "手机号码" -> phoneNumberPosition = index
                    "邮件" -> emailPosition = index
                    "邮件选项" -> emailChoicePosition = index
                    "头像" -> avatarPosition = index
                    "个人空间背景" -> coverPosition = index
                    "出生日期" -> birthdayPosition = index
                    "所在地" -> addressPosition = index
                    "主页" -> homePosition = index
                    "设备" -> devicePosition = index
                    "个性签名" -> describePosition = index
                }
            }

            val userName = formRows[userNamePosition]
                .getElementsByTag("dd")
                .first()
                ?.ownText()
                ?: ""

            val avatarUrl = formRows[avatarPosition]
                .getElementsByTag("img")
                .first()
                ?.attr("srcset")
                ?: ""

            val userId = formRows[avatarPosition]
                .getElementsByTag("a")
                .first()
                ?.attr("data-user-id")
                ?: ""

            val coverStyle = formRows[coverPosition]
                .getElementsByTag("dd")
                .first()
                ?.getElementsByTag("div")
                ?.first()
                ?.attr("style")
                ?: ""

            val regex = Regex("""url\(([^)]+)""")

            val matchResult = regex.find(coverStyle)

            val coverUrl = matchResult
                ?.groupValues
                ?.get(1)
                ?: ""

            val signature = formRows[describePosition]
                .getElementsByClass("input")[1].text()

            Log.e("url", "$BASE_URL/members/$userId/")
            val perDocument = Jsoup.connect("$BASE_URL/members/$userId/")
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .cookies(Cookies)
                .get()

            val authDocuments = perDocument
                .getElementsByClass("memberHeader-banners")
                .first()
                ?.getElementsByTag("em")

            var auth = ""
            if (authDocuments != null) {
                for (index in authDocuments.indices) {
                    auth = if (index == 0) {
                        authDocuments[index]
                            .getElementsByTag("strong")
                            .text()
                    } else {
                        auth + "  " + authDocuments[index].getElementsByTag("strong").first()?.text()
                    }
                }
            }

            val ipAddress = perDocument
                .getElementsByClass("memberHeader-blurb user-login-ip")
                .first()
                ?.getElementsByTag("li")
                ?.last()
                ?.text()
                ?: ""

            val countDocuments = perDocument
                .getElementsByClass("pairJustifier")
                .first()
                ?.getElementsByTag("dl")

            val postCount = countDocuments
                ?.get(0)
                ?.getElementsByTag("a")
                ?.first()
                ?.text()
                ?: "0"

            val resCount = countDocuments
                ?.get(1)
                ?.getElementsByTag("a")
                ?.first()
                ?.text()
                ?: "0"

            val pointsElement = document
                .getElementsByClass("menu-row menu-row--highlighted")
                .first()

            val points = pointsElement
                ?.getElementsByTag("dd")
                ?.get(0)
                ?.text()
                ?: "0"

            val uCoin = pointsElement
                ?.getElementsByTag("dd")
                ?.get(1)
                ?.text()
                ?: "0"

            return@withContext MeInfo(userName, coverUrl, avatarUrl, signature, auth, postCount,
                resCount, userId, points, uCoin, ipAddress)

        }

        suspend fun doClockIn(): ClockIn = withContext(Dispatchers.IO) {
            try {
                // 1. 先获取CSRF Token（从任意页面获取）
                val firstResponse = Jsoup.connect(BASE_URL)
                    .cookies(Cookies) // 需要先登录获取
                    .execute()

                val xfToken = firstResponse.parse()
                    .select("input[name=_xfToken]")
                    .first()
                    ?.attr("value")
                    ?: ""

                // 2. 发送签到请求
                Jsoup.connect("https://www.uotan.cn/mjc-credits/clock")
                    .method(Connection.Method.POST)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Origin", BASE_URL)
                    .header("Referer", BASE_URL)
                    .cookies(firstResponse.cookies()) // 保持会话
                    .data("_xfToken", xfToken)
                    .execute()
                return@withContext isClockIn()
            } catch (_: Exception) {
                return@withContext ClockIn(false, "")
            }
        }

        suspend fun isClockIn(): ClockIn = withContext(Dispatchers.IO) {
            try {
                val document = Jsoup.connect(BASE_URL)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .cookies(Cookies)
                    .get()
                val buttonText = document
                    .getElementsByClass("block-body block-row")
                    .last()
                    ?.getElementsByTag("span")
                    ?.text()
                    ?: ""
                val points = document
                    .getElementsByClass("menu-row menu-row--highlighted")
                    .first()
                    ?.getElementsByTag("dd")
                    ?.get(0)
                    ?.text()
                    ?: "0"
                val isClockIn = buttonText == "今日已签到"
                return@withContext ClockIn(isClockIn, points)
            } catch (_: Exception) {
                return@withContext ClockIn(false, "")
            }
        }
    }
}
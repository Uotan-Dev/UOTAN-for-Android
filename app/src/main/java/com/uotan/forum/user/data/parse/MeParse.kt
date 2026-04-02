package com.uotan.forum.user.data.parse

import android.util.Log
import com.uotan.forum.user.data.model.ClockIn
import com.uotan.forum.user.data.model.MeInfo
import com.uotan.forum.utils.Utils.baseUrl
import com.uotan.forum.utils.Utils.USER_AGENT
import com.uotan.forum.utils.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request
import org.jsoup.Jsoup

class MeParse {

    suspend fun fetchMeData(): MeInfo = withContext(Dispatchers.IO) {
        val client = HttpClient.getClient()
        val firstRequest = Request.Builder()
            .url("$baseUrl/account/account-details/")
            .header("User-Agent", USER_AGENT)
            .build()
        val response = client.newCall(firstRequest).execute()
        val document = Jsoup.parse(response.body.string())
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

        Log.e("e", formRows.toString())

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

        val perRequest = Request.Builder()
            .url("$baseUrl/members/$userId/")
            .header("User-Agent", USER_AGENT)
            .build()
        val perResponse = client.newCall(perRequest).execute()
        val perDocument = Jsoup.parse(perResponse.body.string())

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

        return@withContext MeInfo(
            userName, coverUrl, avatarUrl, signature, auth, postCount,
            resCount, userId, points, uCoin, ipAddress
        )

    }

    suspend fun doClockIn(
        onSuccess: (ClockIn) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val client = HttpClient.getClient()
            val firstRequest = Request.Builder()
                .url("$baseUrl/account/account-details/")
                .header("User-Agent", USER_AGENT)
                .build()
            val response = client.newCall(firstRequest).execute()
            val xfToken = Jsoup.parse(response.body.string())
                .select("input[name=_xfToken]")
                .first()
                ?.attr("value")
                ?: ""
            val formBody = FormBody.Builder()
                .add("_xfToken", xfToken)
                .build()
            val secondRequest = Request.Builder()
                .url("$baseUrl/mjc-credits/clock")
                .post(formBody)
                .header("User-Agent", USER_AGENT)
                .header("Origin", baseUrl)
                .header("Referer", baseUrl)
                .build()
            client.newCall(secondRequest).execute()
            withContext(Dispatchers.Main) {
                onSuccess(isClockIn())
            }
        } catch (t: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(t)
            }
        }
    }

    suspend fun isClockIn(): ClockIn = withContext(Dispatchers.IO) {
        try {
            val client = HttpClient.getClient()
            val request = Request.Builder()
                .url(baseUrl)
                .header("User-Agent", USER_AGENT)
                .build()
            val response = client.newCall(request).execute()
            val document = Jsoup.parse(response.body.string())
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
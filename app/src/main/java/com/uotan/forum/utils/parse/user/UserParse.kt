package com.uotan.forum.utils.parse.user

import com.uotan.forum.utils.Utils.baseUrl
import com.uotan.forum.utils.Utils.USER_AGENT
import com.uotan.forum.utils.network.HttpClient
import com.uotan.forum.utils.network.SecurityParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class UserParse {
    companion object {
        data class UserData(
            val id: String,
            val cover: String,
            val avatar: String,
            val name: String,
            val isFollow: Boolean,
            val auth: String,
            val ipAddress: String,
            val postCount: String,
            val resCount: String,
            val signature: String,
            val xfToken: String
        )
        /**
         * 获取用户信息
         */
        suspend fun fetchUserData(url: String): UserData = withContext(Dispatchers.IO) {
            val document = getMemberDoc(url)
            val id = document
                .getElementsByClass("avatarWrapper")
                .first()
                ?.getElementsByTag("a")
                ?.first()
                ?.attr("data-user-id")
                ?: ""
            val coverStyle = document
                .select(".memberProfileBanner.memberHeader-main")
                .first()
                ?.attr("style")
                ?: ""
            val cover = Regex("""url\(([^)]+)""")
                .find(coverStyle)
                ?.groupValues
                ?.get(1)
                ?: ""
            val avatar = document
                .getElementsByClass("avatarWrapper")
                .first()
                ?.getElementsByTag("a")
                ?.first()
                ?.attr("href")
                ?: ""
            val name = document
                .getElementsByClass("username ")
                .first()
                ?.text()
                ?: ""
            val isFollow = isFollow(document)
            val authDocuments = document
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
            val ipAddress = document
                .getElementsByClass("memberHeader-blurb user-login-ip")
                .first()
                ?.getElementsByTag("li")
                ?.last()
                ?.text()
                ?: ""
            val countDocuments = document
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
            val aboutDocument = getMemberDoc("$url/about")
            val signatureTitle = aboutDocument
                .getElementsByClass("block-row block-row--separated")
                .first()
                ?.getElementsByTag("dt")
                ?.text()
                ?: ""
            val signature = if (signatureTitle.isEmpty()) {
                aboutDocument
                    .getElementsByClass("block-row block-row--separated")
                    .first()
                    ?.text()
                    ?: ""
            } else ""
            val xfToken = document
                .select("input[name=_xfToken]")
                .first()
                ?.attr("value") ?: throw Exception("CSRF token not found")
            return@withContext UserData(id, cover, avatar, name, isFollow, auth, ipAddress, postCount,
                resCount, signature, xfToken)
        }



        /**
         *  检测关注状态
         */
        suspend fun isFollow(url: String): Boolean = withContext(Dispatchers.IO) {
            val document = getMemberDoc(url)
            val followContent = document
                .getElementsByClass("memberHeader-buttons")
                .first()
                ?.getElementsByClass("button-text")
                ?.last()
                ?.text()
                ?: ""
            val isFollow = followContent == "已关注"
            return@withContext isFollow
        }
        private suspend fun isFollow(document: Document): Boolean = withContext(Dispatchers.IO) {
            val followContent = document
                .getElementsByClass("memberHeader-buttons")
                .first()
                ?.getElementsByClass("button-text")
                ?.last()
                ?.text()
                ?: ""
            val isFollow = followContent == "已关注"
            return@withContext isFollow
        }

        /**
         *  关注/取消关注
         */
        suspend fun follow(url: String): Boolean = withContext(Dispatchers.IO) {
            val client = HttpClient.getClient()
            val xfToken = SecurityParse.parseHiddenXfToken(url)
            // 创建 MultipartBody
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("_xfRedirect", baseUrl + url)
                .addFormDataPart("_xfRequestUri", "${url}follow")
                .addFormDataPart("_xfWithData", "1")
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("_xfResponseType", "json")
                .build()
            // 创建 Request
            val request = Request.Builder()
                .url("${baseUrl + url}follow")
                .addHeader("User-Agent", USER_AGENT)
                .post(requestBody)
                .build()
            try {
                val execute = client
                    .newCall(request)
                    .execute()
                return@withContext execute.isSuccessful
            } catch (e: Exception) {
                return@withContext false
            }
        }

        /**
         * 关注/取消关注
         */
        private suspend fun getMemberDoc(url: String): Document = withContext(Dispatchers.IO) {
            val client = HttpClient.getClient()
            val request = Request.Builder()
                .url(baseUrl + url)
                .header("User-Agent", USER_AGENT)
                .build()
            val response = client.newCall(request).execute()
            val document = Jsoup.parse(response.body.string())
            return@withContext document
        }
    }
}
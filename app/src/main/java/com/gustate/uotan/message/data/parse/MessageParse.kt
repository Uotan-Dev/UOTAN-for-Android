package com.gustate.uotan.message.data.parse

import com.gustate.uotan.message.data.model.AllMessage
import com.gustate.uotan.message.data.model.LikeMessage
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class MessageParse {
    suspend fun parseAllMessage(): MutableList<AllMessage> = withContext(Dispatchers.IO) {
        val result = mutableListOf<AllMessage>()
        val document = Jsoup
            .connect("https://www.uotan.cn/account/alerts")
            .userAgent(USER_AGENT)
            .cookies(Cookies)
            .timeout(TIMEOUT_MS)
            .get()
        val container = document.select("div.notappalerts ol.listPlain")
        container.select("li.alert").forEach { element ->
            // 头像处理
            val userId = element
                .select(".contentRow-figure a")
                .first()
                ?.attr("data-user-id")
                ?: ""
            // 用户处理
            val contentRow = element
                .getElementsByClass("contentRow-main contentRow-main--close")
                .first()
            val userName = contentRow
                ?.getElementsByClass("username ")
                ?.first()
                ?.text()
                ?: ""
            /** 处理点赞消息 **/
            val isLikeNotice = contentRow
                ?.getElementsByTag("bdi")
                ?.first()
                ?.text() == "点赞"
            val likeContent = if (isLikeNotice) {
                contentRow.ownText().replace("了主题", "")
            } else {
                "noLike"
            }
            // 网页并未提供点赞文章的链接
            /** 处理回复消息 **/
            val isCommentNotice = contentRow
                ?.ownText() == "回复了主题"
            val commentContent = if (isCommentNotice) {
                contentRow
                    .getElementsByClass("fauxBlockLink-blockLink")
                    .first()
                    ?.text()
                    ?: ""
            } else {
                "noComment"
            }
            val commentUrl = if (isCommentNotice) {
                contentRow
                    .getElementsByClass("fauxBlockLink-blockLink")
                    .first()
                    ?.attr("href")
                    ?: ""
            } else {
                "noComment"
            }
            /** 处理资源更新 **/
            val isUpdateNotice = contentRow
                ?.ownText() == "更新了资源 ."
            val updateContent = if (isUpdateNotice) {
                contentRow
                    .getElementsByClass("fauxBlockLink-blockLink")
                    .first()
                    ?.ownText()
                    ?: ""
            } else {
                "noUpdate"
            }
            val updateUrl = if (isUpdateNotice) {
                contentRow
                    .getElementsByClass("fauxBlockLink-blockLink")
                    .first()
                    ?.attr("href")
                    ?: ""
            } else {
                "noUpdate"
            }
            /** 处理积分消息 **/
            val integralTypeName = element
                .select("div.contentRow-main.contentRow-main--close strong:nth-child(2)")
                .first()
                ?.text()
                ?: element
                    .select("div.contentRow-main.contentRow-main--close strong:nth-child(3)")
                    .first()
                    ?.text()
                ?: ""
            val integralType = when (integralTypeName) {
                "每日签到" -> "dailyAttendance"
                "发布新帖子" -> "publishPost"
                "发布新评论" -> "publishComment"
                "新帖子" -> "publishPost"
                "调整U币" -> "adjustUCoin"
                "购买网盘链接" -> "buyDriveLink"
                "购买资源" -> "buyResource"
                "出售资源" -> "sellResource"
                "注册送积分" -> "register"
                "回复内容被删除" -> "delComment"
                "帖子被删除" -> "delPost"
                "评论被删除" -> "delComment"
                else -> "noIntegral"
            }
            val integralContent = element
                .getElementsByClass("fauxBlockLink-blockLink")
                .first()
            val isIntegralNotice = integralContent?.attr("href") == "/mjc-credits/transactions/"
            val integralCount = if (isIntegralNotice) {
                integralContent.text() ?: ""
            } else {
                "noIntegral"
            }
            val isSystemNotice = !isIntegralNotice && !isLikeNotice && !isUpdateNotice && !isCommentNotice
            val systemContent = if (isSystemNotice) {
                contentRow?.ownText() ?: ""
            } else {
                "noSystem"
            }
            val time = contentRow
                ?.getElementsByTag("time")
                ?.first()
                ?.attr("data-date-string")
                ?: ""
            result.add(AllMessage(userId, userName, isLikeNotice, likeContent, isCommentNotice,
                commentContent, commentUrl, isUpdateNotice, updateContent, updateUrl,
                isIntegralNotice, integralType, integralCount, isSystemNotice, systemContent, time))
        }
        return@withContext result
    }
    suspend fun parseLikeMessage(): MutableList<LikeMessage> = withContext(Dispatchers.IO) {
        val result = mutableListOf<LikeMessage>()
        val document = Jsoup
            .connect("https://www.uotan.cn/account/alerts")
            .userAgent(USER_AGENT)
            .cookies(Cookies)
            .timeout(TIMEOUT_MS)
            .get()
        val container = document.select("div.notappalerts ol.listPlain")
        container.select("li.alert").forEach { element ->
            // 头像处理
            val userId = element
                .select(".contentRow-figure a")
                .first()
                ?.attr("data-user-id")
                ?: ""
            // 用户处理
            val contentRow = element
                .getElementsByClass("contentRow-main contentRow-main--close")
                .first()
            val userName = contentRow
                ?.getElementsByClass("username ")
                ?.first()
                ?.text()
                ?: ""

            /** 处理点赞消息 **/
            val isLikeNotice = contentRow
                ?.getElementsByTag("bdi")
                ?.first()
                ?.text() == "点赞"
            val content = if (isLikeNotice) {
                contentRow.ownText().replace("了主题", "")
            } else {
                "noLike"
            }
            val time = contentRow
                ?.getElementsByTag("time")
                ?.first()
                ?.attr("data-date-string")
                ?: ""
            if (content != "noLike") {
                result.add(LikeMessage(userId, userName, content, time))
            }
        }
        return@withContext result
    }
}
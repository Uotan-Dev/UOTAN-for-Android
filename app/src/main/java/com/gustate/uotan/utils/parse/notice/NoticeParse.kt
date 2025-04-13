package com.gustate.uotan.utils.parse.notice

import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup

data class LikeItem(
    val noticeUserAvatar: String,
    val noticeUserName: String,
    val noticeUserUrl: String,
    val noticeTitle: String,
    val noticeContent: String,
    val noticeTime: String,
    val noticeUrl: String
)

data class NoticeItem(
    val avatarUrl: String,
    val userName: String,
    val isLikeNotice: Boolean,
    val likeContent: String,
    val isCommentNotice: Boolean,
    val commentContent: String,
    val commentUrl: String,
    val isUpdateNotice: Boolean,
    val updateContent: String,
    val updateUrl: String,
    val isIntegralNotice: Boolean,
    val integralType: String,
    val integralCount: String,
    val isSystemNotice: Boolean,
    val systemContent: String,
    val time: String
)

class NoticeParse {
    // 伴生对象
    companion object {
        suspend fun fetchNoticeData(): MutableList<NoticeItem> = withContext(Dispatchers.IO) {
            val result = mutableListOf<NoticeItem>()
            val document = Jsoup
                .connect("https://www.uotan.cn/account/alerts")
                .userAgent(USER_AGENT)
                .cookies(Cookies)
                .timeout(TIMEOUT_MS)
                .get()
            val container = document.select("div.notappalerts ol.listPlain")
            container.select("li.alert").forEach { element ->
                // 头像处理
                val avatarElement = element
                    .select(".contentRow-figure img")
                    .first()
                val avatarUrl = avatarElement
                    ?.attr("src")
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
                    contentRow?.ownText()?.replace("了主题", "")!!
                } else {
                    "noLike"
                }
                // 网页并未提供点赞文章的链接
                /** 处理回复消息 **/
                val isCommentNotice = contentRow
                    ?.ownText() == "回复了主题"
                val commentContent = if (isCommentNotice) {
                    contentRow
                        ?.getElementsByClass("fauxBlockLink-blockLink")
                        ?.first()
                        ?.text()
                        ?: ""
                } else {
                    "noComment"
                }
                val commentUrl = if (isCommentNotice) {
                    contentRow
                        ?.getElementsByClass("fauxBlockLink-blockLink")
                        ?.first()
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
                        ?.getElementsByClass("fauxBlockLink-blockLink")
                        ?.first()
                        ?.ownText()
                        ?: ""
                } else {
                    "noUpdate"
                }
                val updateUrl = if (isUpdateNotice) {
                    contentRow
                        ?.getElementsByClass("fauxBlockLink-blockLink")
                        ?.first()
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
                    "发布新帖子" -> "post"
                    "新帖子" -> "post"
                    "购买网盘链接" -> "buy"
                    "出售资源" -> "sell"
                    "注册送积分" -> "register"
                    "回复内容被删除" -> "del"
                    "帖子被删除" -> "del"
                    else -> "noIntegral"
                }
                val integralContent = element
                    .getElementsByClass("fauxBlockLink-blockLink")
                    .first()
                val isIntegralNotice = integralContent?.attr("href") == "/mjc-credits/transactions/"
                val integralCount = if (isIntegralNotice) {
                    integralContent
                        ?.text()
                        ?: ""
                } else {
                    "noIntegral"
                }
                val isSystemNotice = !isIntegralNotice && !isLikeNotice && !isUpdateNotice && !isCommentNotice
                val systemContent = if (isSystemNotice) {
                    contentRow
                        ?.ownText()
                        ?: ""
                } else {
                    "noSystem"
                }
                val time = contentRow
                    ?.getElementsByTag("time")
                    ?.first()
                    ?.attr("data-date-string")
                    ?: ""
                result.add(NoticeItem(
                    avatarUrl,
                    userName,
                    isLikeNotice,
                    likeContent,
                    isCommentNotice,
                    commentContent,
                    commentUrl,
                    isUpdateNotice,
                    updateContent,
                    updateUrl,
                    isIntegralNotice,
                    integralType,
                    integralCount,
                    isSystemNotice,
                    systemContent,
                    time
                ))
            }
            return@withContext result
        }
        suspend fun fetchLikesTotalPage(): Int = withContext(Dispatchers.IO) {
            val document = Jsoup
                .connect("$BASE_URL/account/reactions?reaction_id=0&page=10000000000000000000000")
                .userAgent(USER_AGENT)
                .cookies(Cookies)
                .method(Connection.Method.GET)
                .timeout(TIMEOUT_MS)
                .execute()
            val pattern = Regex("page=(\\d+)")
            val totalPageCount = pattern
                .find(document.url().toString())
                ?.groups
                ?.get(1)
                ?.value
                ?.toIntOrNull()
            return@withContext totalPageCount!!
        }
        suspend fun fetchLikesData(page: Int): MutableList<LikeItem> = withContext(Dispatchers.IO) {
            val result = mutableListOf<LikeItem>()
            val document = Jsoup
                .connect("$BASE_URL/account/reactions?reaction_id=0&page=$page")
                .userAgent(USER_AGENT)
                .cookies(Cookies)
                .timeout(TIMEOUT_MS)
                .get()
            val blockContainer = document
                .getElementsByClass("block-body js-reactionList-0").first()
            val blockRows = blockContainer!!
                .getElementsByClass("block-row")
            for (blockRow in blockRows) {
                val noticeUserAvatar = blockRow
                    .getElementsByClass("contentRow-figure")
                    .first()
                    ?.getElementsByClass("avatar avatar--s")
                    ?.first()
                    ?.getElementsByTag("img")
                    ?.first()
                    ?.attr("srcset")
                    ?: ""
                val contentRowMain = blockRow
                    .getElementsByClass("contentRow-main")
                    .first()
                val noticeUserName = contentRowMain
                    ?.getElementsByClass("username ")
                    ?.first()
                    ?.text()
                    ?: ""
                val noticeUserUrl = contentRowMain
                    ?.getElementsByClass("username ")
                    ?.first()
                    ?.attr("href")
                    ?: ""
                val topic = contentRowMain
                    ?.getElementsByClass("label label--uotan-threads")
                    ?.first()
                    ?.ownText()
                    ?: ""
                val title = contentRowMain
                    ?.getElementsByTag("a")
                    ?.get(1)
                    ?.ownText()
                    ?: ""
                val noticeTitle = if (topic != "") {
                    "#$topic# $title"
                } else {
                    title
                }
                val noticeUrl = contentRowMain
                    ?.getElementsByTag("a")
                    ?.get(1)
                    ?.attr("href")
                    ?: ""
                val noticeContent = contentRowMain
                    ?.getElementsByClass("contentRow-snippet")
                    ?.first()
                    ?.ownText()
                    ?: ""
                val noticeTime = contentRowMain
                    ?.getElementsByClass("contentRow-minor")
                    ?.first()
                    ?.getElementsByTag("time")
                    ?.attr("data-date-string")
                    ?: ""
                result.add(LikeItem(noticeUserAvatar, noticeUserName, noticeUserUrl, noticeTitle, noticeContent, noticeTime, noticeUrl))
            }
            return@withContext result
        }
    }
}
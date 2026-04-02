package com.uotan.forum.section.data.parse

import com.uotan.forum.section.data.model.Categories
import com.uotan.forum.section.data.model.SectionItem
import com.uotan.forum.utils.Utils.baseUrl
import com.uotan.forum.utils.Utils.USER_AGENT
import com.uotan.forum.utils.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.select.Elements

class SectionItemParse {

    /**
     * 爬取并解析第一次为用户展示的版块列表（位于 首页->版块）
     * @param onSuccess 加载成功时的回调
     * @param onThrowable 加载失败时的回调且包含所抛出的异常
     */
    suspend fun parseCollapseSectionItemList(
        onSuccess: (MutableList<Categories>) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            // 获取版块页面 Document
            val client = HttpClient.getClient()
            val request = Request.Builder()
                .url("$baseUrl/forums/")
                .header("User-Agent", USER_AGENT)
                .build()
            val response = client.newCall(request).execute()
            val document = Jsoup.parse(response.body.string())
            // 爬取并选择每一个版块大类（例：小米手机（类名）包含小米15（子板块名））
            val categoryList = document
                .select("div.block.block--category.block--category")
            // 创建一个存放版块大类的可变列表
            val sectionCollapseList = mutableListOf<Categories>()
            // 逐一解析版块大类
            categoryList.forEach { category ->
                val categoryTitle = category
                    .getElementsByClass("uix_categoryTitle")
                    .first()
                    ?.text()
                    ?: "UNKNOWN"
                val forumList = category.select("div[class*=\"node--id\"]")
                val sectionItemList = parseForumList(forumList)
                // 添加到可变列表中
                sectionCollapseList.add(Categories(categoryTitle, sectionItemList))
            }
            // 成功回调
            withContext(Dispatchers.Main) {
                onSuccess(sectionCollapseList)
            }
        } catch (throwable: Throwable) {
            // 异常回调
            withContext(Dispatchers.Main) {
                onThrowable(throwable)
            }
        }
    }

    /**
     * 解析版块类别（第一次）的版块列表
     * @param forumList 版块大类的 Elements 列表
     *        例：小米手机（类名）包含小米15（子板块名）
     * @see SectionItem 返回版块项
     */
    private fun parseForumList(forumList: Elements?): MutableList<SectionItem> {
        // 创建一个存放子板块项的可变列表
        val sectionItemList = mutableListOf<SectionItem>()
        // 逐一解析子板块项
        forumList?.forEach { forum ->
            val icon = forum
                .getElementsByTag("img")
                .first()
                ?.attr("src")
                ?: ""
            val title = forum
                .getElementsByTag("a")[1]
                .text()
            val link = forum
                .getElementsByTag("a")[1]
                .attr("href")
            // 添加到可变列表中
            sectionItemList.add(SectionItem(icon, title, link))
        }
        return sectionItemList
    }

    /**
     * 解析一个版块类下的子版块项（即二级版块类）
     * @param section 链接后缀
     * @param onSuccess 解析成功后的回调 包含该类别下的版块项
     */
    suspend fun parseCategoriesSectionList(
        section: String,
        onSuccess: (MutableList<SectionItem>) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            // 获取版块页面 Document
            val client = HttpClient.getClient()
            val request = Request.Builder()
                .url(baseUrl + section)
                .header("User-Agent", USER_AGENT)
                .build()
            val response = client.newCall(request).execute()
            val document = Jsoup.parse(response.body.string())
            val itemElements = document
                .getElementsByClass("block-body")
                .first()
                ?.select("div.node--forum")
            val sectionItemList = parseSectionItemInfo(itemElements)
            withContext(Dispatchers.Main) {
                onSuccess(sectionItemList)
            }
        } catch (throwable: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(throwable)
            }
        }
    }

    private fun parseSectionItemInfo(itemElements: Elements?): MutableList<SectionItem> {
        val result = mutableListOf<SectionItem>()
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
                result.add(SectionItem(cover, title, url))
            }
        }
        return result
    }
}
package com.gustate.uotan.section.data.parse

import com.gustate.uotan.section.data.model.Categories
import com.gustate.uotan.section.data.model.SectionItem
import com.gustate.uotan.utils.Utils.Companion.baseUrl
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
            val document = Jsoup
                .connect("$baseUrl/forums/")
                .timeout(TIMEOUT_MS)
                .userAgent(USER_AGENT)
                .cookies(Cookies)
                .get()
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
            val document = Jsoup
                .connect(baseUrl + section)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .cookies(Cookies)
                .get()
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
package com.gustate.uotan.resource.ui.details

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustate.uotan.resource.data.model.ResReplyData
import com.gustate.uotan.resource.data.model.ResourceArticle
import com.gustate.uotan.resource.data.parse.ResourceArticleParse
import kotlinx.coroutines.launch

class ResDetailsViewModel: ViewModel() {

    // ViewModel 私有变量
    // 实例化爬取类
    private val rap = ResourceArticleParse()
    // 页码控制
    private var isResArticleInitialLoadDone = false
    private var isResReplyInitialLoadDone = false
    // 文章全部信息
    private val _details = MutableLiveData<ResourceArticle>()
    private val _reply = MutableLiveData<List<ResReplyData>>()

    private var url = ""

    // 供 View 观察的变量（不可直接修改这些变量，请修改开头为 “_” 的同名变量）
    val details get() = _details
    val reply get() = _reply

    fun loadInitialDetails(
        isRefresh: Boolean,
        resShortUrl: String,
        onThrowable: (Throwable) -> Unit
    ) {
        // 刷新时重置页码控制
        if (isRefresh) isResArticleInitialLoadDone = false
        // 防止 Activity/Fragment 重复调用）
        if (isResArticleInitialLoadDone) return
        url = resShortUrl
        viewModelScope.launch {
            // 文章加载部分
            rap.fetchResourceArticle(
                url = resShortUrl,
                onSuccess = {
                    _details.value = it
                    isResArticleInitialLoadDone = true
                },
                onThrowable
            )
        }
    }

    fun loadInitialReply(
        isRefresh: Boolean,
        onThrowable: (Throwable) -> Unit
    ) {
        // 刷新时重置页码控制
        if (isRefresh) isResReplyInitialLoadDone = false
        // 防止 Activity/Fragment 重复调用）
        if (isResReplyInitialLoadDone) return
        viewModelScope.launch {
            Log.e("e", "")
            // 文章加载部分
            rap.getResourceReply(
                url = url,
                onSuccess = {
                    _reply.value = it
                    isResReplyInitialLoadDone = true
                },
                onThrowable
            )
        }
    }

}
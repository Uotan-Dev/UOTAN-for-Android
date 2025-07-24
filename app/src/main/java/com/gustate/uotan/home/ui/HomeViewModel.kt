package com.gustate.uotan.home.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustate.uotan.home.data.model.LatestItem
import com.gustate.uotan.home.data.model.RecommendItem
import com.gustate.uotan.home.data.parse.LatestParse
import com.gustate.uotan.home.data.parse.RecommendParse
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    // ViewModel 私有变量
    // 实例化爬取类
    private val recommendParse = RecommendParse()
    private val latestParse = LatestParse()
    // Pager 页码控制
    private var isPagerInitialLoadDone = false
    private val _pagerPage = MutableLiveData<Int>()
    // 推荐内容页码控制
    private var recommendCurrentPage = 1
    private var isRecommendInitialLoadDone = false
    // 推荐列表内容与状态
    private val _recommendList = MutableLiveData<List<RecommendItem>>()
    private val _isRecommendLastPage = MutableLiveData<Boolean>()
    // 最新内容状态
    private var isLatestLoadDone = false
    // 最新列表内容
    private val _latestList = MutableLiveData<List<LatestItem>>()

    // 供 View 观察的变量（不可直接修改这些变量，请修改开头为 “_” 的同名变量）
    // Pager 页数
    val pagerPage: MutableLiveData<Int> get() = _pagerPage
    // 推荐列表内容与状态
    val recommendList: MutableLiveData<List<RecommendItem>> get() = _recommendList
    val isRecommendLastPage: MutableLiveData<Boolean> get() = _isRecommendLastPage
    // 最新列表内容
    val latestList: MutableLiveData<List<LatestItem>> get() = _latestList

    /**
     * 初始化 Pager 页码控制
     */
    fun loadInitialPager() {
        if (isPagerInitialLoadDone) return
        // 初始加载推荐页（第二页）
        _pagerPage.value = 1
        isPagerInitialLoadDone = true
    }

    /**
     * 修改 Pager 页码
     * @param page 页码或 position
     */
    fun changePager(page: Int) {
        _pagerPage.value = page
    }

    /**
     * 加载/刷新初始推荐内容
     * @param isRefresh 是否为刷新时调用
     * @param onSuccess 加载成功时的回调
     * @param onException 加载失败时的回调且包含错误信息
     */
    fun loadInitialRecommendData(
        isRefresh: Boolean,
        onSuccess: () -> Unit,
        onException: (Exception) -> Unit
    ) {
        if (isRefresh) {
            // 初始化页码控制变量
            recommendCurrentPage = 1
            isRecommendInitialLoadDone = false
        }
        // 防止 Activity/Fragment 重复调用）
        if (isRecommendInitialLoadDone) return
        viewModelScope.launch {
            recommendParse.parseRecommend(
                page = recommendCurrentPage,
                onSuccess = {
                    _recommendList.value = it.items
                    recommendCurrentPage ++
                    _isRecommendLastPage.value = recommendCurrentPage > it.totalPage
                    isRecommendInitialLoadDone = true
                    onSuccess()
                },
                onException = {
                    onException(it)
                }
            )
        }
    }

    /**
     * 加载更多推荐内容
     * @param onSuccess 加载成功时的回调
     * @param onException 加载失败时的回调且包含错误信息
     */
    fun loadMoreRecommendData(
        onSuccess: () -> Unit,
        onException: (Exception) -> Unit
    ) {
        // 最后一页检测
        if (_isRecommendLastPage.value == true) return
        viewModelScope.launch {
            recommendParse.parseRecommend(
                page = recommendCurrentPage,
                onSuccess = {
                    // 取原列表并添加内容
                    val rl = _recommendList.value?.toMutableList()
                    rl?.addAll(it.items)
                    _recommendList.value = rl?.toList() ?: it.items.toList()
                    recommendCurrentPage ++
                    _isRecommendLastPage.value = recommendCurrentPage > it.totalPage
                    onSuccess()
                },
                onException = {
                    onException(it)
                }
            )
        }
    }

    /**
     * 加载最新帖子
     * @param isRefresh 是否为刷新时调用
     * @param onSuccess 加载成功回调
     * @param onException 加载失败回调并附带错误信息
     */
    fun loadLatestData(
        isRefresh: Boolean,
        onSuccess: () -> Unit,
        onException: (Exception) -> Unit
    ) {
        // 刷新与防止错误调用
        if (isRefresh) isLatestLoadDone = false
        if (isLatestLoadDone) return
        viewModelScope.launch {
            latestParse.parseLatest(
                onSuccess = {
                    // 赋值给列表
                    _latestList.value = it.toList()
                    isLatestLoadDone = true
                    onSuccess()
                },
                onException = {
                    onException(it)
                }
            )
        }
    }
}
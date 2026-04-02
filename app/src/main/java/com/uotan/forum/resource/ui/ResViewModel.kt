package com.uotan.forum.resource.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uotan.forum.resource.data.model.ResourceItem
import com.uotan.forum.resource.data.model.ResourcePlateItem
import com.uotan.forum.resource.data.parse.ResourceParse
import kotlinx.coroutines.launch

class ResViewModel: ViewModel() {

    // ViewModel 私有变量
    // 实例化爬取类
    private val rp = ResourceParse()
    // 精选推荐内容与状态
    private val _recommendList = MutableLiveData<List<ResourceItem>>()
    private var isRecommendInitialLoadDone = false
    // 分类类别内容与状态
    private val _allPlate = MutableLiveData<List<ResourcePlateItem>>()
    private val _currentPlateName = MutableLiveData<String>()
    private val _currentPlateUrl = MutableLiveData<String>()
    private var isPlateLoading = false
    private var isPlateInitialLoadDone = false
    // 分类内容页码控制
    private var plateCurrentPage = 1
    private var plateNextPageUrl = ""
    private var isPlateContentInitialLoadDone = false
    // 分类内容内容与状态
    private val _plateContentList = MutableLiveData<List<ResourceItem>>()
    private val _isPlateContentLastPage = MutableLiveData<Boolean>()

    // 精选推荐内容与状态
    val recommendList get() = _recommendList
    // 分类类别内容与状态
    val allPlate get() = _allPlate
    val currentPlateName get() = _currentPlateName
    val currentPlateUrl get() = _currentPlateUrl
    // 分类内容内容与状态
    val plateContentList get() = _plateContentList
    val isPlateContentLastPage get() = _isPlateContentLastPage

    /**
     * 加载/刷新初始推荐内容
     * @param isRefresh 是否为刷新时调用
     * @param onThrowable 加载失败时的回调且包含错误信息
     */
    fun loadRecommendData(
        isRefresh: Boolean,
        onThrowable: (Throwable) -> Unit
    ) {
        // 防止重复调用
        if (isRefresh) isRecommendInitialLoadDone = false
        if (isRecommendInitialLoadDone) return
        viewModelScope.launch {
            rp.parseResourceRecommendData(
                onSuccess = {
                    _recommendList.value = it
                    isRecommendInitialLoadDone = true
                },
                onThrowable
            )
        }
    }

    /**
     * 加载/刷新初始类别内容
     * @param isRefresh 是否为刷新时调用
     * @param onSuccess 加载成功时的回调
     * @param onThrowable 加载失败时的回调且包含错误信息
     */
    fun loadInitialPlateContentData(
        isRefresh: Boolean,
        onSuccess: () -> Unit = { },
        onThrowable: (Throwable) -> Unit
    ) {
        if (isRefresh) {
            // 初始化页码控制变量
            plateCurrentPage = 1
            isPlateContentInitialLoadDone = false
        }
        // 防止重复调用
        if (isPlateContentInitialLoadDone) return
        viewModelScope.launch {
            rp.parseResourceData(
                page = plateCurrentPage,
                categories = _currentPlateUrl.value ?: "",
                onSuccess = {
                    _plateContentList.value = it.items
                    plateNextPageUrl = it.nextPageUrl
                    plateCurrentPage ++
                    _isPlateContentLastPage.value = plateCurrentPage > it.totalPage
                    isPlateContentInitialLoadDone = true
                    onSuccess()
                },
                onThrowable
            )
        }
    }

    /**
     * 加载更多动态内容
     * @param onThrowable 加载失败时的回调且包含错误信息
     */
    fun loadMoreRecommendData(onThrowable: (Throwable) -> Unit) {
        // 最后一页检测
        if (_isPlateContentLastPage.value == true) return
        viewModelScope.launch {
            rp.parseResourceData(
                page = plateCurrentPage,
                categories = _currentPlateUrl.value ?: "",
                onSuccess = {
                    // 取原列表并添加内容
                    val pcl = _plateContentList.value?.toMutableList()
                    // 将新内容添加到原列表
                    pcl?.addAll(it.items)
                    _plateContentList.value = pcl?.toList()
                    plateNextPageUrl = it.nextPageUrl
                    plateCurrentPage ++
                    _isPlateContentLastPage.value = plateCurrentPage > it.totalPage
                },
                onThrowable
            )
        }
    }

    fun loadPlateType(
        onSuccess: () -> Unit,
        onThrowable: (Throwable) -> Unit
    ) {
        if (isPlateLoading) return
        if (isPlateInitialLoadDone) {
            onSuccess()
            return
        }
        isPlateLoading = true
        viewModelScope.launch {
            rp.parseResourcePlateData(
                onSuccess = {
                    _allPlate.value = it
                    isPlateLoading = false
                    isPlateInitialLoadDone = true
                    onSuccess()
                },
                onThrowable
            )
        }
    }

    fun setPlateType(
        name: String,
        url: String,
        onSuccess: () -> Unit,
        onThrowable: (Throwable) -> Unit
    ) {
        _currentPlateName.value = name
        _currentPlateUrl.value = url
        loadInitialPlateContentData(
            isRefresh = true,
            onSuccess, onThrowable
        )
    }

}
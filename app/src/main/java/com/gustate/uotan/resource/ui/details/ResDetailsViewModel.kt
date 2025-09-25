package com.gustate.uotan.resource.ui.details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustate.uotan.R
import com.gustate.uotan.resource.data.model.PurchaseData
import com.gustate.uotan.resource.data.model.ResReplyData
import com.gustate.uotan.resource.data.model.ResourceArticle
import com.gustate.uotan.resource.data.model.ResourceType
import com.gustate.uotan.resource.data.parse.ResourceArticleParse
import com.gustate.uotan.resource.ui.details.model.ResourceBuyNavEvent
import com.gustate.uotan.resource.ui.details.model.ResourceBuyUiState
import com.gustate.uotan.utils.Utils
import com.gustate.uotan.utils.room.UserDao
import com.gustate.uotan.utils.room.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ResDetailsViewModel @Inject constructor(userDao: UserDao): ViewModel() {

    // ViewModel 私有变量
    // 实例化爬取类
    private val rap = ResourceArticleParse()
    private val ur = UserRepository(userDao)
    // 页码控制
    private var isResArticleInitialLoadDone = false
    private var isResReplyInitialLoadDone = false
    // 文章全部信息
    private var _url = MutableLiveData<String>()
    private val _details = MutableLiveData<ResourceArticle>()
    private val _isReact = MutableLiveData<Boolean>()
    private val _isReacting = MutableLiveData<Boolean>()
    private val _isReplying = MutableLiveData<Boolean>()
    private val _reactCount = MutableLiveData<Long>()
    private val _isAuthorFollow = MutableLiveData<Boolean>()
    private val _isAuthorFollowing = MutableLiveData<Boolean>()
    private val _isBook = MutableLiveData<Boolean>()
    private val _isBooking = MutableLiveData<Boolean>()
    private val _reply = MutableLiveData<List<ResReplyData>>()
    private val _uiState = MutableStateFlow<ResourceBuyUiState>(ResourceBuyUiState.Idle)
    private val _navigationEvent = MutableSharedFlow<ResourceBuyNavEvent>()
    private val _toastEvent = MutableSharedFlow<Int>()

    // 供 View 观察的变量（不可直接修改这些变量，请修改开头为 “_” 的同名变量）
    val url get() = _url
    val details get() = _details
    val isReact get() = _isReact
    val isReacting get() = _isReacting
    val isReplying get() = _isReplying
    val reactCount get() = _reactCount
    val isAuthorFollow get() = _isAuthorFollow
    val isAuthorFollowing get() = _isAuthorFollowing
    val isBook get() = _isBook
    val isBooking get() = _isBooking
    val reply get() = _reply
    val uiState: StateFlow<ResourceBuyUiState> = _uiState.asStateFlow()
    val navigationEvent: SharedFlow<ResourceBuyNavEvent> = _navigationEvent
    val toastEvent: SharedFlow<Int> = _toastEvent

    fun loadInitialDetails(
        isRefresh: Boolean,
        resShortUrl: String,
        onThrowable: (Throwable) -> Unit
    ) {
        // 刷新时重置页码控制
        if (isRefresh) isResArticleInitialLoadDone = false
        // 防止 Activity/Fragment 重复调用）
        if (isResArticleInitialLoadDone) return
        _url.value = resShortUrl
        viewModelScope.launch {
            // 文章加载部分
            rap.fetchResourceArticle(
                url = resShortUrl,
                onSuccess = {
                    _details.value = it
                    _isReact.value = it.isReact
                    _reactCount.value = it.numberOfLikes.toLong()
                    _isBook.value = it.isBookMark
                    isAuthorFollow(
                        onSuccess = {
                            isResArticleInitialLoadDone = true
                        }, onException = { e ->
                            onThrowable(e)
                            isResArticleInitialLoadDone = true
                        }
                    )
                }, onThrowable = {
                    onThrowable(it)
                    isResArticleInitialLoadDone = true
                }
            )
        }
    }

    fun isAuthorFollow(
        onSuccess: () -> Unit,
        onException: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            rap.isFollowAuthor(
                url = _details.value?.authorUrl
                    ?: throw NullPointerException("author url is empty"),
                onSuccess = {
                    _isAuthorFollow.value = it
                    onSuccess()
                }, onException
            )
        }
    }

    fun onAuthorFollow(
        onFailure: ((code: Int, body: String) -> Unit),
        onException: (Exception) -> Unit
    ) {
        if (_isAuthorFollowing.value == true) return
        _isAuthorFollowing.value = true
        viewModelScope.launch {
            rap.onFollowAuthor(
                url = _details.value?.authorUrl
                    ?: throw NullPointerException("伟大圣龙库胡勒阿乔没在燃素中找到你的信息"),
                onSuccess = {
                    val lastFollow = _isAuthorFollow.value
                    _isAuthorFollow.value = lastFollow == false
                    _isAuthorFollowing.value = false
                }, onFailure = { code, body ->
                    onFailure(code, body)
                    _isAuthorFollowing.value = false
                }, onException = {
                    onException(it)
                    _isAuthorFollowing.value = false
                }
            )
        }
    }

    fun onBookMark(
        onFailure: ((code: Int, body: String) -> Unit),
        onException: (Exception) -> Unit
    ) {
        if (_isBooking.value == true) return
        _isBooking.value = true
        viewModelScope.launch {
            rap.onBookMark(
                url = _url.value
                    ?: throw NullPointerException("res url is empty"),
                isMarked = _isBook.value
                    ?: throw NullPointerException("app do not know your book mark state"),
                onSuccess = {
                    val lastBook = _isBook.value
                    _isBook.value = lastBook == false
                    _isBooking.value = false
                }, onFailure = { code, body ->
                    onFailure(code, body)
                    _isBooking.value = false
                }, onException = {
                    onException(it)
                    _isBooking.value = false
                }
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
            // 文章加载部分
            rap.getResourceReply(
                url = _url.value ?: throw NullPointerException("Intent Error"),
                onSuccess = {
                    _reply.value = it
                    isResReplyInitialLoadDone = true
                }, onThrowable
            )
        }
    }

    fun downloadResource(url: String) {
        viewModelScope.launch {
            _uiState.value = ResourceBuyUiState.Loading
            try {
                val purchaseData = rap.getPurchaseData(url)
                val resType = purchaseData.first().resType
                when(resType) {
                    ResourceType.New -> {
                        _uiState.value = ResourceBuyUiState.ShowNewResourceBuyDialog(purchaseData)
                    }
                    ResourceType.Old -> {
                        _uiState.value = ResourceBuyUiState.ShowOldResourceBuyDialog(purchaseData)
                    }
                    ResourceType.Other -> {
                        _navigationEvent.emit(
                            ResourceBuyNavEvent
                                .OpenUrlInBrowser(purchaseData.first().url)
                        )
                        _uiState.value = ResourceBuyUiState.Idle
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ResourceBuyUiState.Error(
                    title = R.string.unknown_error,
                    message = e.message
                )
            }
        }
    }

    fun onNewResourceSelected(
        oldUrl: String,
        data: PurchaseData,
        isNewRes: Boolean
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = ResourceBuyUiState.Loading
                if (data.isPaid) {
                    _navigationEvent.emit(ResourceBuyNavEvent
                        .OpenUrlInBrowser(data.url))
                    _uiState.value = ResourceBuyUiState.Idle
                } else {
                    rap.buyResource(
                        url = if (isNewRes) data.url
                        else data.url.replace("/download", "/purchase"),
                        onSuccess = {
                            viewModelScope.launch {
                                _toastEvent.emit(R.string.purchase_successful)
                                downloadResource(oldUrl)
                            }
                        },
                        onException = { error ->
                            _uiState.value = ResourceBuyUiState.Error(
                                title = R.string.unknown_error,
                                message = error
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ResourceBuyUiState.Error(
                    title = R.string.unknown_error,
                    message = e.message
                )
            }
        }
    }

    fun onOldResourceDonate(oldUrl: String, data: PurchaseData) {
        onNewResourceSelected(oldUrl, data, false)
    }

    fun onDialogDismissed() {
        _uiState.value = ResourceBuyUiState.Idle
    }

    fun onErrorHandled() {
        _uiState.value = ResourceBuyUiState.Idle
    }

    fun onReact(
        onFailure: ((code: Int, body: String) -> Unit),
        onException: (Exception) -> Unit
    ) {
        if (_isReacting.value == true) return
        _isReacting.value = true
        viewModelScope.launch {
            rap.reactResource(
                url = _details.value?.reactUrl
                    ?: throw NullPointerException("react url is empty"),
                onSuccess = {
                    if (_isReact.value == true) {
                        _reactCount.value = _reactCount.value?.minus(1)
                        _isReact.value = false
                    } else {
                        _reactCount.value = _reactCount.value?.plus(1)
                        _isReact.value = true
                    }
                    _isReacting.value = false
                }, onFailure = { code, body ->
                    _isReacting.value = false
                    onFailure(code, body)
                }, onException = {
                    _isReacting.value = false
                    onException(it)
                }
            )
        }
    }

    fun onReply(
        rating: String,
        message: String,
        onSuccess: () -> Unit,
        onFailure: ((code: Int, body: String) -> Unit),
        onException: (Exception) -> Unit
    ) {
        if (_isReplying.value == true) return
        _isReplying.value = true
        viewModelScope.launch {
            val userInfo = ur.getUser()
            rap.onReply(
                url = _url.value
                    ?: throw NullPointerException("res url is empty"),
                rating, message, onSuccess = {
                    val time = Utils.getSystemTime()
                        ?: throw java.lang.NullPointerException("time is unknow")
                    val newItem = ResReplyData(
                        authorId = userInfo?.id?.toString()
                            ?: throw NullPointerException("user info is empty"),
                        author = userInfo.userName, rating = rating.toFloat(),
                        time, version = "0", content = message
                    )
                    val oldList = _reply.value?.toMutableList() ?: mutableListOf()
                    oldList.add(0, newItem)
                    _reply.value = oldList
                    onSuccess()
                    _isReplying.value = false
                }, onFailure = { code, body ->
                    _isReplying.value = false
                    onFailure(code, body)
                }, onException = {
                    _isReplying.value = false
                    onException(it)
                }
            )
        }
    }
}
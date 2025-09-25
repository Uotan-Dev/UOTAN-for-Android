package com.gustate.uotan.threads.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustate.uotan.threads.data.model.Post
import com.gustate.uotan.threads.data.model.post.PostResponse
import com.gustate.uotan.threads.data.parse.ThreadsParse
import com.gustate.uotan.threads.data.repository.ThreadsRepository
import com.gustate.uotan.utils.Utils.baseUrl
import kotlinx.coroutines.launch

class ThreadsViewModel : ViewModel() {

    // ViewModel 私有变量
    // 实例化网络操作类
    private val tr = ThreadsRepository()
    private val tp = ThreadsParse()
    // 评论内容页码控制
    private var isInitialLoadDone = false
    private val _isLastPage = MutableLiveData<Boolean>()
    // 文章内容
    private val _threadTitle = MutableLiveData<String>()
    private val _threadPost = MutableLiveData<Post>()
    private val _threadsAuthorIpAddress = MutableLiveData<String>()
    private val _isThreadsReact = MutableLiveData<Boolean>()
    private val _isThreadsBookMark = MutableLiveData<Boolean>()
    private val _isThreadsAuthorFollow = MutableLiveData<Boolean>()
    private val _threadsReactCount = MutableLiveData<Long>()
    private val _threadsReplyCount = MutableLiveData<Long>()
    private val _isThreadsJingTie = MutableLiveData<Boolean>()
    private val _isThreadsLocked = MutableLiveData<Boolean>()
    // 评论内容与状态
    private val _posts = MutableLiveData<List<Post>>()
    private val _postsReply = MutableLiveData<List<PostResponse>>()
    private val _isReacting = MutableLiveData<Boolean>()
    private val _isBooking = MutableLiveData<Boolean>()
    private val _isFollowing = MutableLiveData<Boolean>()
    private var currentPage = 1

    // 供 View 观察的变量（不可直接修改这些变量，请修改开头为 “_” 的同名变量）
    // 文章内容
    val threadTitle get() = _threadTitle
    val threadPost get() = _threadPost
    val threadsAuthorIpAddress get() = _threadsAuthorIpAddress
    val isThreadsReact get() = _isThreadsReact
    val isThreadsBookMark get() = _isThreadsBookMark
    val isThreadsAuthorFollow get() = _isThreadsAuthorFollow
    val threadsReactCount get() = _threadsReactCount
    val threadsReplyCount get() = _threadsReplyCount
    val isThreadsJingTie get() = _isThreadsJingTie
    val isThreadsLocked get() = _isThreadsLocked
    // 评论内容与状态
    val posts get() = _posts
    val postsReply get() = _postsReply
    val isLastPage get() = _isLastPage
    val isReacting get() = _isReacting
    val isBooking get() = _isBooking
    val isFollowing get() = _isFollowing

    /**
     * 初次加载主题和帖子
     * （仅主题第一页的帖子）
     * @param threadOrPostId
     */
    fun loadInitialThreadsAndPosts(
        threadOrPostId: String,
        isRefresh: Boolean,
        onSuccess: () -> Unit,
        onException: (Exception) -> Unit
    ) {
        if (isRefresh) {
            currentPage = 1
            isInitialLoadDone = false
        }
        if (isInitialLoadDone) {
            onSuccess()
            return
        }
        val threadId = delPostId(threadOrPostId)
        viewModelScope.launch {
            tr.getThreadsAllPosts(
                posts = threadId, page = currentPage,
                onSuccess = {
                    val mp = it.posts.toMutableList()
                    val th = mp[0]
                    _threadPost.value = th
                    _isThreadsReact.value = th.isReactedTo
                    _isThreadsAuthorFollow.value = th.user.is_followed
                    _threadsReactCount.value = th.reactionScore
                    _threadsReplyCount.value = it.pagination.shown - 1
                    mp.removeAt(0)
                    _posts.value = mp
                    loadPostAllReply(mp)
                    _isLastPage.value =
                        it.pagination.lastPage <= currentPage
                    currentPage ++
                    isInitialLoadDone = true
                    onSuccess()
                },
                onException = {
                    onException(it)
                }
            )
        }
    }

    fun loadThreadInfo(
        threadOrPostId: String,
        isRefresh: Boolean,
        onSuccess: () -> Unit,
        onThrowable: (Throwable) -> Unit
    ) {
        val threadId = delPostId(threadOrPostId)
        viewModelScope.launch {
            tp.parseThreadsInfo(
                threadId,
                onSuccess = {
                    _threadTitle.value = it.title
                    _threadsAuthorIpAddress.value = it.ipAddress
                    _isThreadsBookMark.value = it.isBookMark
                    _isThreadsLocked.value = it.isLocked
                    _isThreadsJingTie.value = it.isJingTie
                },
                onThrowable
            )
        }
    }

    fun loadPostAllReply(newPostList: List<Post>) {
        viewModelScope.launch {
            tr.getPostAllReply(
                newPostList,
                onSuccess = {
                    // Log.e("e", it.toString())
                    val currentList = _postsReply.value ?: emptyList() // 处理 null 情况
                    val newList = currentList.toMutableList().apply {
                        addAll(it)
                    }
                    _postsReply.value = newList
                },
                onThrowable = {
                    //Log.e("e", it.message?:" ")
                }
            )
        }
    }

    // 初始化流程 加载帖子内容与评论 -> 加载帖子信息 -> 加载评论的评论


    private fun delPostId(threadOrPostId: String): String {
        val postIdIndex = threadOrPostId.lastIndexOf("/post-")
        val threadId = if (postIdIndex != -1) {
            threadOrPostId.substring(0, postIdIndex + 1) // 截取 "/post-" 之前的部分（保留结尾斜杠）
        } else {
            threadOrPostId // 未找到则返回原字符串
        }
        return threadId
    }

    fun loadMorePosts(
        threadOrPostId: String,
        onSuccess: () -> Unit,
        onException: (Exception) -> Unit
    ) {
        val threadId = delPostId(threadOrPostId)
        viewModelScope.launch {
            tr.getThreadsAllPosts(
                posts = threadId, page = currentPage,
                onSuccess = {
                    val op = _posts.value?.toMutableList()
                    op?.addAll(it.posts)
                    _posts.value = op?.toList()
                    loadPostAllReply(it.posts)
                    _isLastPage.value = it.pagination.lastPage <= currentPage
                    currentPage ++
                    onSuccess()
                },
                onException = {
                    onException(it)
                }
            )
        }
    }

    fun followThreadsAuthor(
        onFailure: ((code: Int, body: String) -> Unit),
        onThrowable: (Throwable) -> Unit
    ) {
        _isFollowing.value = true
        viewModelScope.launch {
            tp.follow(
                memberUrl = _threadPost.value?.user?.view_url?: "",
                onSuccess = {
                    _isThreadsAuthorFollow.value.let {
                        _isThreadsAuthorFollow.value = it != true
                    }
                    _isFollowing.value = false
                },
                onFailure = { code, body ->
                    onFailure(code, body)
                },
                onThrowable = {
                    onThrowable(it)
                }
            )
        }
    }

    fun bookMarkThreads(
        onFailure: ((code: Int, body: String) -> Unit),
        onThrowable: (Throwable) -> Unit
    ) {
        _isBooking.value = true
        viewModelScope.launch {
            tp.bookMark(
                postUrl = _threadPost.value?.viewURL?: "",
                isMarked = _isThreadsBookMark.value == true,
                onSuccess = {
                    _isThreadsBookMark.value.let {
                        _isThreadsBookMark.value = it != true
                    }
                    _isBooking.value = false
                },
                onFailure = { code, body ->
                    onFailure(code, body)
                },
                onThrowable = {
                    onThrowable(it)
                }
            )
        }
    }

    fun replyThreads(
        message: String,
        onSuccess: ((newPost: Post) -> Unit),
        onFailure: ((code: Int, body: String) -> Unit),
        onThrowable: ((Throwable) -> Unit)
    ) {
        viewModelScope.launch {
            tr.addPostOrReplyThreads(
                threadsId = _threadPost.value?.threadID ?: 0L,
                message = message,
                onSuccess = {
                    onSuccess(it)
                },
                onFailure = { code, body ->
                    onFailure(code, body)
                },
                onThrowable = {
                    onThrowable(it)
                }
            )
        }
    }

    fun reactPosts() {
        _isReacting.value = true
        viewModelScope.launch {
            tr.reactPosts(
                postsId = _threadPost.value?.postID.toString(),
                onSuccess = {
                    if (_isThreadsReact.value == true) {
                        _threadsReactCount.value = _threadsReactCount.value?.minus(1)
                    } else {
                        _threadsReactCount.value = _threadsReactCount.value?.plus(1)
                    }
                    _isThreadsReact.value = it
                    _isReacting.value = false
                },
                onFailure = { code, body ->

                },
                onThrowable = {

                }
            )
        }
    }

    // 获取 IP 的业务逻辑
    fun fetchIp(
        postId: String,
        onSuccess: (String) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            tr.getIp("$baseUrl/posts/$postId/ip", onSuccess, onThrowable)
        }
    }

}
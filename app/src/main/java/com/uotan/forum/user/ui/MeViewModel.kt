package com.uotan.forum.user.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.uotan.forum.search.data.model.SearchItem
import com.uotan.forum.search.data.parse.SearchParse
import com.uotan.forum.user.data.model.MeInfo
import com.uotan.forum.user.data.parse.MeParse
import com.uotan.forum.utils.Utils.baseUrl
import com.uotan.forum.utils.Utils.saveToExternalPrivateDir
import com.uotan.forum.utils.room.User
import com.uotan.forum.utils.room.UserDatabase
import com.uotan.forum.utils.room.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MeViewModel(application: Application) : AndroidViewModel(application) {

    private val ur =
        UserRepository(UserDatabase.getDatabase(application).userDao())
    private val mp = MeParse()
    private val sp = SearchParse()
    private var isProfileLoading = false
    private var isProfileInitDone = false
    private var isPostLoading = false
    private var currentPostPage = 1
    private var postNextPageUrl = ""
    private val _profile = MutableLiveData<MeInfo>()
    private val _isClockIn = MutableLiveData<Boolean>()
    private val _posts = MutableLiveData<MutableList<SearchItem>>()
    private val _isPostLastPage = MutableLiveData<Boolean>()
    val profile get() = _profile
    val isClockIn get() = _isClockIn
    val posts get() = _posts
    val isPostLastPage get() = _isPostLastPage

    fun loadProfile(
        isRefresh: Boolean = false,
        onSuccess: () -> Unit = {},
        onThrowable: (Throwable) -> Unit
    ) {
        if (isProfileLoading) return
        if (!isRefresh && isProfileInitDone) return
        isProfileLoading = true
        viewModelScope.launch {
            try {
                val profileCache = loadProfileCache()
                profileCache.apply {
                    _profile.value = MeInfo(
                        userName, "local", "local",
                        signature, auth ?: "", postCount ?: "",
                        resCount ?: "", userId ?: "",
                        points ?: "", uCoin ?: "", ipAddress ?: ""
                    )
                }
                val profileOnline = mp.fetchMeData()
                val clockInInfo = mp.isClockIn()
                _isClockIn.value = clockInInfo.isClockIn
                if (clockInInfo.points != _profile.value?.points)
                    _profile.value?.points = clockInInfo.points
                _profile.value = profileOnline
                isProfileInitDone = true
                isProfileLoading = false
                onSuccess()
            } catch (t: Throwable) {
                onThrowable(t)
                isProfileLoading = false
            }
        }
    }

    fun loadPosts(
        isInitOrRefresh: Boolean = false,
        onSuccess: () -> Unit = {},
        onThrowable: (Throwable) -> Unit
    ) {
        if (isPostLoading) return
        if (isInitOrRefresh) {
            currentPostPage = 1
            postNextPageUrl = ""
            _isPostLastPage.value = false
        }
        if (_isPostLastPage.value == true) return
        isPostLoading = true
        viewModelScope.launch {
            val userId = ur.getUser()?.userId?:"0"
            if (currentPostPage == 1) {
                sp.parseSearchInfo(
                    content = "member?user_id=$userId",
                    isMePage = true,
                    onSuccess = {
                        currentPostPage ++
                        _posts.value = it.items
                        postNextPageUrl = it.nextPageUrl
                        _isPostLastPage.value = currentPostPage > it.totalPage
                        isPostLoading = false
                        onSuccess()
                    },
                    onThrowable = {
                        onThrowable(it)
                        isPostLoading = false
                    }
                )
            } else {
                if (postNextPageUrl.isEmpty())
                    throw NullPointerException("next page url is empty")
                sp.parseSearchInfo(
                    url = postNextPageUrl,
                    onSuccess = {
                        currentPostPage ++
                        val oPosts = _posts.value ?: mutableListOf()
                        oPosts.addAll(it.items)
                        _posts.value = oPosts
                        postNextPageUrl = it.nextPageUrl
                        _isPostLastPage.value = currentPostPage > it.totalPage
                        isPostLoading = false
                        onSuccess()
                    },
                    onThrowable = {
                        onThrowable(it)
                        isPostLoading = false
                    }
                )
            }
        }
    }

    private suspend fun loadProfileCache(): User = withContext(Dispatchers.IO) {
        val myInfo = ur.getUser()
        if (myInfo == null)
                throw NullPointerException("你的登录状态不完整，请重新登录")
        return@withContext myInfo
    }

    fun updateAvatarCache(avatar: String) {
        viewModelScope.launch {
            // 缓存头像文件
            saveToExternalPrivateDir(
                application,
                baseUrl + avatar,
                "user/", "avatar.jpg"
            )
        }
    }

    fun updateCoverCache(cover: String) {
        viewModelScope.launch {
            // 缓存封面文件
            saveToExternalPrivateDir(
                application,
                baseUrl + cover,
                "user/", "cover.jpg"
            )
        }
    }

    fun clockInToday(
        onSuccess: () -> Unit,
        onThrowable: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            mp.doClockIn(
                onSuccess = {
                    _isClockIn.value = it.isClockIn
                    _profile.value?.points = it.points
                    onSuccess()
                }, onThrowable)
        }
    }

}
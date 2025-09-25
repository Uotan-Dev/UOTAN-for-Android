package com.gustate.uotan.settings.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustate.uotan.settings.data.profile.ProfileParse
import com.gustate.uotan.user.data.api.MineApiService
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel: ViewModel() {

    private val mas = MineApiService()
    private val pp = ProfileParse()

    private val _username = MutableLiveData<String>()
    val username get() = _username
    private val _email = MutableLiveData<String>()
    val email get() = _email
    private val _avatar = MutableLiveData<Any>()
    val avatar get() = _avatar

    fun loadUserInfo() {
        viewModelScope.launch {
            mas.getMeUserInfo(
                onSuccess = {
                    val profile = it.me
                    _username.value = profile.username
                    _email.value = profile.email
                    _avatar.value = profile.avatarUrls.o
                },
                onException = {

                }
            )
        }
    }

    fun updateAvatar(
        avatarFile: File,
        onSuccess: (xfToken: String) -> Unit,
        onFailure: ((code: Int, body: String) -> Unit),
        onThrowable: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            pp.changeAvatar(avatarFile, onSuccess, onFailure, onThrowable)
        }
    }

    fun applyAvatar(
        avatarFile: File,
        xfToken: String,
        onSuccess: () -> Unit,
        onFailure: ((code: Int, body: String) -> Unit),
        onThrowable: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            pp.applyAvatar(
                xfToken,
                onSuccess = {
                    _avatar.value = avatarFile
                    onSuccess()
                }, onFailure, onThrowable)
        }
    }

    fun updateUserName(
        name: String,
        reason: String,
        onSuccess: () -> Unit,
        onFailure: ((code: Int, body: String) -> Unit),
        onThrowable: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            pp.changeUserName(
                name, reason,
                onSuccess = {
                    _username.value = name
                    onSuccess()
                },
                onFailure, onThrowable
            )
        }
    }

}
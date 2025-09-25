package com.gustate.uotan.user.login.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gustate.uotan.user.login.data.LoginRepository
import com.gustate.uotan.user.login.ui.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.gustate.uotan.user.data.parse.MeParse
import com.gustate.uotan.utils.Utils.baseUrl
import com.gustate.uotan.utils.Utils.saveToExternalPrivateDir
import com.gustate.uotan.utils.network.HttpClient
import com.gustate.uotan.utils.room.User
import com.gustate.uotan.utils.room.UserDatabase
import com.gustate.uotan.utils.room.UserRepository
import kotlinx.coroutines.delay

class LoginViewModel(private val context: Application) : AndroidViewModel(context) {
    private val loginRepository = LoginRepository()
    private val userRepository =
        UserRepository(UserDatabase.getDatabase(context).userDao())
    private val mp = MeParse()

    // 使用 MutableStateFlow 来保存状态，外部只读使用 asStateFlow()
    private val _uiState = MutableStateFlow<LoginState>(LoginState.Idle)
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    // 登录方法
    fun login(account: String, password: String) {
        // 设置为加载状态
        _uiState.value = LoginState.Loading

        viewModelScope.launch {
            val result = loginRepository.login(account, password)
            when {
                result.isSuccess -> {
                    val data = result.getOrNull()!!
                    if (data.isTwoStep) {
                        // 需要两步验证
                        _uiState.value = LoginState.TwoFactorRequired(data.url, data.xfToken)
                    } else {
                        delay(100)
                        // 登录成功
                        val userData = mp.fetchMeData()
                        // 缓存基本信息链接
                        userRepository.insert(
                            User(
                                0, userData.userName, userData.cover,
                                userData.avatar, userData.signature, userData.auth,
                                userData.postCount, userData.resCount, userData.userId,
                                userData.points, userData.uCoin, userData.ipAddress
                            )
                        )
                        // 缓存头像文件
                        saveToExternalPrivateDir(context,
                            baseUrl + userData.avatar, "user/", "avatar.jpg")
                        // 缓存封面文件
                        saveToExternalPrivateDir(context,
                            baseUrl + userData.cover, "user/", "cover.jpg")
                        val preferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                        preferences.edit { putBoolean("isLoggedIn", true) }
                        _uiState.value = LoginState.Success
                    }
                }
                else -> {
                    // 登录失败
                    val exception = result.exceptionOrNull()
                    _uiState.value = LoginState.Error(exception?.message ?: "登录失败")
                }
            }
        }
    }

    // 提交两步验证码
    fun submitTwoFactorCode(url: String, xfToken: String, code: String) {
        _uiState.value = LoginState.Loading

        viewModelScope.launch {
            val result = loginRepository.twoStep(url, xfToken, code)
            when {
                result.isSuccess -> {
                    // 两步验证成功
                    _uiState.value = LoginState.Success
                }
                else -> {
                    // 两步验证失败
                    val exception = result.exceptionOrNull()
                    _uiState.value = LoginState.Error(exception?.message ?: "验证失败")
                }
            }
        }
    }

    // 重置状态
    fun resetState() {
        _uiState.value = LoginState.Idle
    }
}
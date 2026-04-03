package com.uotan.forum.welcome.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uotan.forum.startup.data.parse.StartupParse
import com.uotan.forum.startup.ui.StartupState
import com.uotan.forum.user.data.parse.MeParse
import com.uotan.forum.user.login.data.LoginRepository
import com.uotan.forum.utils.Utils
import com.uotan.forum.utils.room.User
import com.uotan.forum.utils.room.UserDatabase
import com.uotan.forum.utils.room.UserRepository
import com.uotan.forum.welcome.ui.model.ErrorDialogState
import com.uotan.forum.welcome.ui.model.LoginEffect
import com.uotan.forum.welcome.ui.model.LoginResult
import com.uotan.forum.welcome.ui.model.LoginUiState
import com.uotan.forum.welcome.ui.model.TwoFactorResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(private val context: Application) : AndroidViewModel(context) {

    private val userRepository =
        UserRepository(UserDatabase.Companion.getDatabase(context).userDao())
    private val mp = MeParse()

    private val loginRepository = LoginRepository()
    private val _uiState = MutableStateFlow(value = LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect = _effect.asSharedFlow()

    private val sp = StartupParse()
    private val _startupUiState = MutableStateFlow<StartupState>(StartupState.Idle)
    val startupUiState: StateFlow<StartupState> = _startupUiState.asStateFlow()

    // 登录方法
    fun login(
        account: String,
        password: String
    ) {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            val result = loginRepository.login(
                account = account,
                password = password
            )
            when (result) {
                is LoginResult.Success -> {
                    loadUserInfo(
                        onSuccess = {
                            _uiState.update { it.copy(loading = false) }
                            viewModelScope.launch {
                                _effect.emit(value = LoginEffect.NavigateToHome)
                            }
                        }
                    )
                }
                is LoginResult.TwoFactor -> {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            twoFactor = result.info
                        )
                    }
                    _effect.emit(value = LoginEffect.NavigateToTwoStep)
                }
                is LoginResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            errorDialog = ErrorDialogState(
                                title = "业务错误",
                                message = result.message
                            )
                        )
                    }
                }
                is LoginResult.Error -> {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            errorDialog = ErrorDialogState(
                                title = "发生错误",
                                message = result.exception.message ?: "未知错误"
                            )
                        )
                    }
                }
            }
        }
    }

    fun thirdPartyLoginSuccess() {
        viewModelScope.launch {
            loadUserInfo(
                onSuccess = {
                    _uiState.update { it.copy(loading = false) }
                    viewModelScope.launch {
                        _effect.emit(value = LoginEffect.NavigateToHome)
                    }
                }
            )
        }
    }

    private suspend fun loadUserInfo(onSuccess: () -> Unit) = withContext(Dispatchers.IO) {
        val userData = mp.fetchMeData()
        // 缓存基本信息链接
        userRepository.insert(
            User(
                0, userData.userName, userData.cover,
                userData.avatar, userData.signature, userData.auth,
                userData.postCount, userData.resCount,
                userData.userId, userData.points, userData.uCoin,
                userData.ipAddress
            )
        )
        // 缓存头像文件
        Utils.saveToExternalPrivateDir(
            context,
            Utils.baseUrl + userData.avatar, "user/", "avatar.jpg"
        )
        // 缓存封面文件
        Utils.saveToExternalPrivateDir(
            context,
            Utils.baseUrl + userData.cover, "user/", "cover.jpg"
        )
        onSuccess()
    }

    // 提交两步验证码
    fun submitTwoFactorCode(code: String) {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            val result = uiState.value.twoFactor?.let {
                loginRepository.checkTwoFactor(
                    url = it.url,
                    xfToken = it.xfToken,
                    provider = it.provider,
                    code = code
                )
            } ?: return@launch
            when (result) {
                is TwoFactorResult.Success -> {
                    loadUserInfo(
                        onSuccess = {
                            _uiState.update { it.copy(loading = false) }
                            viewModelScope.launch {
                                _effect.emit(value = LoginEffect.NavigateToHome)
                            }
                        }
                    )
                }
                is TwoFactorResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            errorDialog = ErrorDialogState(
                                title = result.title,
                                message = result.message
                            )
                        )
                    }
                }
                is TwoFactorResult.Error -> {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            errorDialog = ErrorDialogState(
                                title = "发生错误",
                                message = result.exception.message ?: "未知错误"
                            )
                        )
                    }
                }
            }
        }
    }

    fun changeTotpProvider() {
        _uiState.update {
            it.copy(
                twoFactor = it.twoFactor?.copy(
                    provider = when (it.twoFactor.provider) {
                        "totp" -> "backup"
                        "backup" -> "totp"
                        else -> "totp"
                    }
                )
            )
        }
    }

    fun startup() {
        _uiState.update { it.copy(loading = true) }
        _startupUiState.value = StartupState.Loading
        viewModelScope.launch {
            val result = sp.parseStartupType()
            when {
                result.isSuccess -> {
                    _uiState.update { it.copy(loading = false) }
                    val data = result.getOrNull()!!
                    if (data.isSmsVerify) _startupUiState.value = StartupState.NeedSmsVerify
                    else if (data.isAgreement)
                        _startupUiState.value = StartupState.NeedAgreePrivacy(data.agreementXfToken)
                    else _startupUiState.value = StartupState.Success
                }
                else -> {
                    _uiState.update { it.copy(loading = false) }
                    val exception = result.exceptionOrNull()
                    _startupUiState.value = StartupState.Error(exception?.message ?: "未知错误")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(
                errorDialog = null
            )
        }
    }

    // 重置状态
    fun resetState() {
        _uiState.update {
            it.copy(
                loading = false
            )
        }
    }

}
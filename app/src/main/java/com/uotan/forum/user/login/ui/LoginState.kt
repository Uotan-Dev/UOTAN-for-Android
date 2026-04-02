package com.uotan.forum.user.login.ui

sealed class LoginState {
    object Idle : LoginState() // 初始状态
    object Loading : LoginState() // 加载中
    data class TwoFactorRequired(val url: String, val xfToken: String) : LoginState() // 需要两步验证
    object Success : LoginState() // 登录成功
    data class Error(val message: String) : LoginState() // 错误状态
}
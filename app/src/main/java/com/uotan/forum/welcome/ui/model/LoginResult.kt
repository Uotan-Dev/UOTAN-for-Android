package com.uotan.forum.welcome.ui.model

sealed class LoginResult {
    data object Success : LoginResult()
    data class TwoFactor(val info: TwoFactorInfo) : LoginResult()
    data class Failure(val message: String) : LoginResult()
    data class Error(val exception: Exception) : LoginResult()
}

sealed class TwoFactorResult {
    data object Success : TwoFactorResult()
    data class Failure(val title: String, val message: String) : TwoFactorResult()
    data class Error(val exception: Exception) : TwoFactorResult()
}
package com.uotan.forum.startup.ui

sealed class StartupState {
    object Idle : StartupState()
    object Loading : StartupState()
    object NeedSmsVerify : StartupState()
    data class NeedAgreePrivacy(val xfToken: String) : StartupState()
    object Success : StartupState()
    data class Error(val message: String) : StartupState()
}
package com.uotan.forum.welcome.ui.model

data class LoginUiState(
    val loading: Boolean = false,
    val twoFactor: TwoFactorInfo? = null,
    val errorDialog: ErrorDialogState? = null
)

data class TwoFactorInfo(
    val url: String,
    val xfToken: String,
    val provider: String
)

data class ErrorDialogState(
    val title: String = "Error",
    val message: String
)

sealed interface LoginIntent {
    data class SubmitLogin(
        val account: String,
        val password: String
    ) : LoginIntent
    data class SubmitTwoFactor(
        val code: String
    ) : LoginIntent
    data object CopyError : LoginIntent
    data object DismissError : LoginIntent
}

sealed interface LoginEffect {
    data object NavigateToHome : LoginEffect
    data object NavigateToTwoStep : LoginEffect
}
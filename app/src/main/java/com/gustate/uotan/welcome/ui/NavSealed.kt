package com.gustate.uotan.welcome.ui

import kotlinx.serialization.Serializable

@Serializable
sealed class NavSealed(val route: String) {

    @Serializable
    object Sealed : NavSealed("welcome")

    @Serializable
    object Login : NavSealed("login")

    @Serializable
    object QQLogin : NavSealed("login/qq")

    @Serializable
    object WeiboLogin : NavSealed("login/weibo")

    @Serializable
    object XiaomiLogin : NavSealed("login/xiaomi")

}
package com.uotan.forum.welcome.ui

import kotlinx.serialization.Serializable

@Serializable
sealed class NavSealed(val route: String) {

    @Serializable
    object Sealed : NavSealed(route = "welcome")

    @Serializable
    object Login : NavSealed(route = "login")

    @Serializable
    object LoginTwoStep: NavSealed(route = "login/two_step")

    @Serializable
    object QQLogin : NavSealed(route = "login/qq")

    @Serializable
    object WeiboLogin : NavSealed(route = "login/weibo")

    @Serializable
    object XiaomiLogin : NavSealed(route = "login/xiaomi")

}
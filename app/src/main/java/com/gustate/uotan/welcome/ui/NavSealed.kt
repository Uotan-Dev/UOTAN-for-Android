package com.gustate.uotan.welcome.ui

sealed class NavSealed(val route: String) {
    object Sealed : NavSealed("welcome")
    object Login : NavSealed("login")
    //object Re : WelcomeNav("profile")

    /* 带参数的路由也可以很好地管理
    object UserDetail : Screen("user_detail/{userId}") {
        fun createRoute(userId: String) = "user_detail/$userId"
    }*/
}
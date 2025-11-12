package com.gustate.uotan.welcome.ui.composable

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gustate.uotan.welcome.ui.NavSealed
import com.gustate.uotan.welcome.ui.model.LoginSealed

@Composable
fun WelcomeNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavSealed.Sealed.route // 设置起始目的地
    ) {
        // 定义你的各个页面（可组合项目的地）
        composable(NavSealed.Sealed.route) {
            WelcomeScreen(navController) // 将 navController 传递给页面以便其进行导航操作
        }
        composable(NavSealed.Login.route) {
            LoginScreen(navController)
        }
        composable(NavSealed.QQLogin.route) {
            WebViewLoginScreen(navController,LoginSealed.QQ)
        }
        composable(NavSealed.XiaomiLogin.route) {
            WebViewLoginScreen(navController,LoginSealed.Xiaomi)
        }
        composable(NavSealed.WeiboLogin.route) {
            WebViewLoginScreen(navController,LoginSealed.Weibo)
        }
    }
}

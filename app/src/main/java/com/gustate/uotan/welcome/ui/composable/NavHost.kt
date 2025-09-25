package com.gustate.uotan.welcome.ui.composable

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gustate.uotan.welcome.ui.NavSealed.Login
import com.gustate.uotan.welcome.ui.NavSealed.Sealed

@Composable
fun WelcomeNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Sealed.route // 设置起始目的地
    ) {
        // 定义你的各个页面（可组合项目的地）
        composable(Sealed.route) {
            WelcomeScreen(navController) // 将 navController 传递给页面以便其进行导航操作
        }
        composable(Login.route) {
            LoginScreen(navController)
        }
        // 可以继续添加更多 composable 目的地
        // 你也可以使用 navigation 来构建嵌套导航图:cite[8]
    }
}

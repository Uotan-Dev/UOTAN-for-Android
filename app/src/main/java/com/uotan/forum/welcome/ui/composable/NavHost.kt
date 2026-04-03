package com.uotan.forum.welcome.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.uotan.forum.ui.dialog.LoadingDialog
import com.uotan.forum.ui.theme.uotanColors
import com.uotan.forum.welcome.ui.LoginViewModel
import com.uotan.forum.welcome.ui.NavSealed
import com.uotan.forum.welcome.ui.model.LoginSealed
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun WelcomeNavHost(
    navController: NavHostController
) {
    val context = LocalContext.current
    val loading = LoadingDialog(context)
    val hazeState = rememberHazeState()
    val pageModifier = Modifier
        .fillMaxSize()
        .background(color = MaterialTheme.uotanColors.background)
        .hazeSource(state = hazeState)
        .fillMaxSize()
        .statusBarsPadding()
    NavHost(
        navController = navController,
        startDestination = NavSealed.Sealed.route
    ) {
        // 定义你的各个页面（可组合项目的地）
        composable(NavSealed.Sealed.route) {
            WelcomeScreen(navController)
        }
        composable(route = NavSealed.Login.route) {
            val viewModel: LoginViewModel = viewModel()
            LoginPage(
                modifier = pageModifier,
                viewModel = viewModel,
                navController = navController,
                hazeState = hazeState,
                loading = loading
            )
        }
        composable(route = NavSealed.LoginTwoStep.route) {
            val parentEntry = remember(key1 = navController.currentBackStackEntry) {
                navController.getBackStackEntry(route = NavSealed.Login.route)
                }
            val viewModel: LoginViewModel = viewModel(viewModelStoreOwner = parentEntry)
            TwoStepPage(
                modifier = pageModifier,
                viewModel = viewModel,
                navController = navController,
                hazeState = hazeState,
                loading = loading
            )
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

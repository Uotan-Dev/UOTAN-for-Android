package com.uotan.forum.welcome.ui.composable

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.uotan.forum.R
import com.uotan.forum.main.ui.MainActivity
import com.uotan.forum.ui.composable.liquid.ClassicHeader
import com.uotan.forum.welcome.ui.LoginViewModel
import com.uotan.forum.utils.Utils.baseUrl
import com.uotan.forum.utils.Utils.showToast
import com.uotan.forum.utils.network.CookieManager
import com.uotan.forum.welcome.ui.model.LoginSealed
import com.kevinnzou.web.LoadingState
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberWebViewNavigator
import com.kevinnzou.web.rememberWebViewState
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewLoginScreen(navController: NavController, loginType: LoginSealed) {

    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel()
    var currentUrl by remember { mutableStateOf(loginType.url) }
    var isLoading by remember { mutableStateOf(true) }
    val cookieManager = remember { CookieManager() }
    val webViewState = rememberWebViewState(currentUrl)
    val navigator = rememberWebViewNavigator()
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val hazeState = rememberHazeState()
    val backdrop = rememberLayerBackdrop()

    LaunchedEffect(webViewRef) {
        webViewRef?.let { webView ->
            cookieManager.setupCookieManager(webView)
            cookieManager.syncJarCookiesToWebView(currentUrl)
        }
    }

    var handled by remember { mutableStateOf(false) }
    LaunchedEffect(webViewState.loadingState) {
        fun navSuccess() {
            isLoading = false
            currentUrl = webViewState.lastLoadedUrl ?: ""
            if (!handled && isLoginSuccessUrl(currentUrl)) {
                handled = true
                cookieManager.syncWebViewCookiesToJar(baseUrl)
                viewModel.loadUserInfo {
                    showToast(context, R.string.login_successful)
                    context.startActivity(
                        Intent(context, MainActivity::class.java))
                    (context as Activity).finish()
                    return@loadUserInfo
                }
            }
        }
        when (val state = webViewState.loadingState) {
            is LoadingState.Finished -> {
                navSuccess()
            }

            is LoadingState.Loading -> {
                isLoading = true
                if (state.progress == 1.0f) {
                    navSuccess()
                }
            }

            LoadingState.Initializing -> {
                handled = false
            }
        }
    }
    Box(modifier = Modifier.hazeSource(hazeState).layerBackdrop(backdrop)) {
        WebView(
            state = webViewState,
            navigator = navigator,
            modifier = Modifier
                .fillMaxSize(),
            onCreated = { webView ->
                // 保存 WebView 引用用于后续配置
                webViewRef = webView
                // 基础配置
                webView.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    when (loginType) {
                        is LoginSealed.QQ -> {
                            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                    "Chrome/141.0.0.0 " +
                                    "Safari/537.36 " +
                                    "Edg/141.0.0.0"
                            showToast(
                                context, "请您截图至 QQ 客户端扫码登录 " +
                                        "暂不支持密码登录"
                            )
                        }

                        is LoginSealed.Weibo -> {
                            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                    "Chrome/141.0.0.0 " +
                                    "Safari/537.36 " +
                                    "Edg/141.0.0.0"
                            showToast(context, "请您截图至微博客户端扫码登录")
                        }

                        is LoginSealed.Xiaomi -> {
                            showToast(context, "请您输入账号密码")
                        }
                    }
                }
            }
        )
        if (isLoading) {
            CircularProgressIndicator(Modifier
                .align(Alignment.Center)
                .size(48.dp))
        }
    }
    ClassicHeader(
        modifier = Modifier,
        hazeState = hazeState,
        backdrop = backdrop,
        title = "第三方登录",
        enableLeftButton = true,
        onLeftButtonClick = {
            navController.popBackStack()
        }
    )
}

private fun isLoginSuccessUrl(url: String) = url.contains(baseUrl)
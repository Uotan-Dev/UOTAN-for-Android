package com.uotan.forum.startup.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uotan.forum.startup.ui.composeable.StartupScreen
import com.uotan.forum.ui.theme.UotanTheme
import com.uotan.forum.utils.Utils.isLogin
import com.uotan.forum.utils.mode.AppMode
import com.uotan.forum.utils.mode.AppModeViewModel
import com.uotan.forum.welcome.ui.WelcomeActivity
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.collectAsState

/**
 * 启动页面 (Activity)
 * Gustate 03/03/2025
 * Compose 迁移（Part 1） 2025/9/21
 */

@AndroidEntryPoint
class StartupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UotanTheme {
                val viewModel = viewModel<AppModeViewModel>()
                val appMode = viewModel.appMode.collectAsState().value
                if (!isLogin && (appMode == AppMode.NONE || appMode == AppMode.ALL)) {
                    startActivity(
                        Intent(
                            this,
                            WelcomeActivity::class.java
                        )
                    )
                    finish()
                }
                StartupScreen()
            }
        }
    }
}
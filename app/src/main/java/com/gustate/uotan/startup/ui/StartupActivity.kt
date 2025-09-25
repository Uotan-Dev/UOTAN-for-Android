package com.gustate.uotan.startup.ui

import android.content.Intent
import android.os.Bundle
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.databinding.ActivityStartupBinding
import com.gustate.uotan.startup.ui.composeable.StartupScreen
import com.gustate.uotan.ui.theme.UotanTheme
import com.gustate.uotan.utils.Utils.isLogin
import com.gustate.uotan.welcome.ui.WelcomeActivity

/**
 * 启动页面 (Activity)
 * Gustate 03/03/2025
 * Compose 迁移（Part 1） 2025/9/21
 */

class StartupActivity : BaseActivity() {

    private lateinit var binding: ActivityStartupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 绑定视图
        binding = ActivityStartupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!isLogin) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }

        binding.root.setContent {
            UotanTheme {
                StartupScreen()
            }
        }
    }

}
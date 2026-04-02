package com.uotan.forum.welcome.ui

import android.os.Bundle
import androidx.navigation.compose.rememberNavController
import com.uotan.forum.BaseActivity
import com.uotan.forum.databinding.ActivityWelcomeBinding
import com.uotan.forum.ui.theme.UotanTheme
import com.uotan.forum.welcome.ui.composable.WelcomeNavHost

class WelcomeActivity : BaseActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.setContent {
            UotanTheme {
                val navController = rememberNavController()
                WelcomeNavHost(navController)
            }
        }
    }
}
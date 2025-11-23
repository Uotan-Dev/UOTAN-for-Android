package com.gustate.uotan.welcome.ui

import android.os.Bundle
import androidx.navigation.compose.rememberNavController
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.databinding.ActivityWelcomeBinding
import com.gustate.uotan.ui.theme.UotanTheme
import com.gustate.uotan.welcome.ui.composable.WelcomeNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
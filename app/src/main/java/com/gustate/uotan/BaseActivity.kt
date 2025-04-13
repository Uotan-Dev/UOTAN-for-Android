package com.gustate.uotan

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gustate.uotan.utils.Utils.Companion.isLogin
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.openImmersion
import com.gustate.uotan.utils.parse.data.CookiesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class BaseActivity : AppCompatActivity() {

    private var currentThemeResId: Int = 0

    private val cookiesManager by lazy { CookiesManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        lifecycleScope.launch {
            setCookies()
            isLogin = isLogin()
        }
        enableEdgeToEdge()
        openImmersion(window)
        super.onCreate(savedInstanceState)
    }

    private fun applyTheme() {
        val themeResId = when (ThemePreference.getTheme(this)) {
            "base" -> R.style.Base_Theme_Uotan
            "monet" -> R.style.Monet_Theme_Uotan
            else -> R.style.Base_Theme_Uotan
        }
        currentThemeResId = themeResId
        setTheme(themeResId)
    }

    override fun onResume() {
        checkAndRecreateIfThemeChanged()
        lifecycleScope.launch {
            setCookies()
            isLogin = isLogin()
        }
        enableEdgeToEdge()
        openImmersion(window)
        super.onResume()
    }

    private fun checkAndRecreateIfThemeChanged() {
        val newThemeResId = when (ThemePreference.getTheme(this)) {
            "base" -> R.style.Base_Theme_Uotan
            "monet" -> R.style.Monet_Theme_Uotan
            else -> R.style.Base_Theme_Uotan
        }
        if (newThemeResId != currentThemeResId) {
            Handler(Looper.getMainLooper()).post {
                recreate()
            }
        }
    }

    private suspend fun setCookies() = withContext(Dispatchers.IO) {
        // 将数据库中的 Cookies 赋值到全局变量中
        Cookies = cookiesManager.cookiesFlow.first()
    }

    private fun isLogin() = Cookies != mapOf<String, String>()

}
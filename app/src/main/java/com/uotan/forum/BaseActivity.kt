package com.uotan.forum

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.uotan.forum.utils.Utils.openImmersion

open class BaseActivity : AppCompatActivity() {

    private var currentThemeResId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        enableEdgeToEdge()
        openImmersion(window)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        updateTheme()
        enableEdgeToEdge()
        openImmersion(window)
        super.onResume()
    }

    /**
     * 第一次获取并应用主题
     * @see ThemePreference
     */
    private fun applyTheme() {
        val themeResId = when (ThemePreference.getTheme(this)) {
            "base" -> R.style.Base_Theme_Uotan
            "monet" -> R.style.Monet_Theme_Uotan
            else -> R.style.Base_Theme_Uotan
        }
        currentThemeResId = themeResId
        setTheme(themeResId)
    }

    /**
     * 后续重载时判断主题是否相同
     * @see ThemePreference
     */
    private fun updateTheme() {
        val newThemeResId = when (ThemePreference.getTheme(this)) {
            "base" -> R.style.Base_Theme_Uotan
            "monet" -> R.style.Monet_Theme_Uotan
            else -> R.style.Base_Theme_Uotan
        }
        if (newThemeResId != currentThemeResId) {
            Handler(Looper.getMainLooper()).post {
                currentThemeResId = newThemeResId
                recreate()
            }
        }
    }

}
package com.uotan.forum.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.uotan.forum.ThemePreference

// 创建 CompositionLocal 用于提供自定义 ColorScheme
val LocalUotanColors = staticCompositionLocalOf { uotanLightColorScheme() }
// 创建 CompositionLocal 用于提供主题配置（可选）
val LocalUotanThemeConfiguration = staticCompositionLocalOf { UotanThemeConfiguration() }

data class UotanThemeConfiguration(
    val isDark: Boolean = false,
    val isDynamic: Boolean = false
)

/**
 * UOTAN Theme
 * BasicTheme - MonetTheme
 * Light and Dark
 */
@Composable
fun UotanTheme(
    dynamicColorForce: Boolean? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val theme = ThemePreference.getTheme(context)
    var dynamicColor = theme == "monet"
    if (dynamicColorForce != null) dynamicColor = dynamicColorForce
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> uotanDarkColorScheme()
        else -> uotanLightColorScheme()
    }
    CompositionLocalProvider(
        LocalUotanColors provides colorScheme,
        LocalUotanThemeConfiguration provides UotanThemeConfiguration(
            isDark = darkTheme,
            isDynamic = dynamicColor
        )
    ) {
        MaterialTheme(
            content = content
        )
    }
}

val MaterialTheme.uotanColors: ColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalUotanColors.current
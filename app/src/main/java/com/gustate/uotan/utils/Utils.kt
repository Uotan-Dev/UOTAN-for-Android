package com.gustate.uotan.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.Window
import android.view.WindowManager

/*
 * 这是一个工具（Utils）类
 * 用于声明一些静态方法以供程序调用
 */

class Utils {

    companion object {

        const val BASE_URL = "https://www.uotan.cn/"
        const val USER_AGENT = "UotanAPP/1.0"
        const val TIMEOUT_MS = 30000

        var Cookies = mapOf<String,String>()
        var isLogin = false

        fun dp2Px(dp: Int, context: Context): Float {
            val density = context.resources.displayMetrics.density
            return dp * density
        }

        fun openImmersion(window: Window) {

            if (isXiaomi()) {
                //设置沉浸式状态栏，在金凡的狗屎系统中，状态栏背景透明。原生系统中，状态栏背景半透明。
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }

        }

        fun isXiaomi(): Boolean {
            return checkSystemProperty("ro.miui.region") &&
                    checkSystemProperty("ro.miui.ui.version.code") &&
                    checkSystemProperty("ro.miui.ui.version.name")
        }

        @SuppressLint("PrivateApi")
        private fun checkSystemProperty(propName: String): Boolean {
            return try {
                val systemProperties = Class.forName("android.os.SystemProperties")
                val getMethod = systemProperties.getMethod("get", String::class.java)
                getMethod.invoke(systemProperties, propName)?.toString()?.isNotEmpty() ?: false
            } catch (e: Exception) {
                false
            }
        }

    }

}
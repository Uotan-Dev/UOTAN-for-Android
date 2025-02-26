package com.gustate.uotan.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.gustate.uotan.anim.TransitionAnimConfig

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

        fun openUrlInBrowser(context: Context, url: String) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                // 确保在新任务栈打开
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }

        private fun isXiaomi(): Boolean {
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

        //版本名
        fun getVersionName(context: Context) = getPackageInfo(context).versionName
        //版本名
        fun getVersionCode(context: Context) = getPackageInfo(context).longVersionCode.toString()

        private fun getPackageInfo(context: Context): PackageInfo {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, PackageManager.GET_CONFIGURATIONS)
            return  pi
        }

    }

}
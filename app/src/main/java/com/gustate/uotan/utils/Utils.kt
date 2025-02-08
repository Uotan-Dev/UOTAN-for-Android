package com.gustate.uotan.utils

import android.content.Context

/*
 * 这是一个工具（Utils）类
 * 用于声明一些静态方法以供程序调用
 */

class Utils {

    companion object {

        fun dp2Px(dp: Int, context: Context): Float {
            val density = context.resources.displayMetrics.density
            return dp * density
        }

    }

}
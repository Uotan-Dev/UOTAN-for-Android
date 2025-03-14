package com.gustate.uotan.anim

import android.graphics.Rect
import android.view.View

class TitleAnim(private val smallTitle: View, private val bigTitle: View, private val titleBarHeight : Float, private val statusBarHeight : Float, private val type: Int = 0) {

    // 小标题偏移量, 默认赋值为 0
    private var smallTitleTranslationY = 0f
    // 大标题高度
    private var bigTitleHeight = 0f

    private var rect: Rect? = null
    private var listener: OnRatioChangeListener? = null

    init {
        // 确保小标题被加载
        smallTitle.post {
            if (type != 1) {
                // 将小标题偏移量变量 设置为 小标题的原偏移量 （即小标题的上边距 + 小标题的高度）
                smallTitleTranslationY = titleBarHeight - statusBarHeight
                // 将小标题偏移量变量设置为小标题偏移量
                smallTitle.translationY = smallTitleTranslationY
            }
            // 设置大标题的高度
            bigTitleHeight = bigTitle.measuredHeight.toFloat()
            // 创建 rect (一个矩形) 实例
            rect = Rect()
        }
        // 观察大标题视图树的变化，添加一个 OnDrawListener 用于监听绘制操作，绘制发生时执行 check() 函数
        bigTitle.viewTreeObserver.addOnDrawListener { check() }
    }

    private fun check() {
        // 检查是否有 Rect 实例，没有则退出函数，如果不为空则赋值到函数内的 rect
        val rect = rect ?: return
        // 将大标题的视图在屏幕中的位置记录至 rect 中
        bigTitle.getGlobalVisibleRect(rect)
        // 设置一个可变变量 ratio 表示显示区域的比例
        var ratio = (rect.bottom - titleBarHeight) / bigTitleHeight

        // 确保 ratio 不小于 0
        if (ratio < 0) {
            ratio = 0f
        } else if (ratio > 1) {
            ratio = 1f
        }

        // 如果大标题完全显示，则隐藏小标题；否则根据比例显示
        if (rect.bottom <= titleBarHeight) {
            if (type != 1) {
                smallTitle.translationY = 0f
                bigTitle.alpha = 0f
            }
            smallTitle.alpha = 1f - 0f
        } else if (ratio == 1.0f) {
            if (type != 1) {
                smallTitle.translationY = smallTitleTranslationY
                bigTitle.alpha = 1f
            }
            smallTitle.alpha = 1f - 1f
        } else {
            if (type != 1) {
                smallTitle.translationY = smallTitleTranslationY * ratio
                bigTitle.alpha = ratio
            }
            smallTitle.alpha = 1f - ratio
            listener?.onRatioChange(ratio)
        }
    }

    interface OnRatioChangeListener {
        fun onRatioChange(ratio: Float)
    }
}

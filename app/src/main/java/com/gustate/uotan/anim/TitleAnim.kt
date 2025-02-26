package com.gustate.uotan.anim

import android.graphics.Rect
import android.view.View

class TitleAnim(private val smallTitle: View, private val bigTitle: View, private val titleBarHeight : Float, private val statusBarHeight : Float) {

    // 小标题偏移量, 默认赋值为 0
    private var smallTitleTranslationY = 0f
    // 大标题高度
    private var bigTitleHight = 0f

    private var rect: Rect? = null
    private var listener: OnRatioChangeListener? = null

    init {
        // 确保小标题被加载
        smallTitle.post {
            // 将小标题偏移量变量 设置为 小标题的原偏移量 （即小标题的上边距 + 小标题的高度）
            smallTitleTranslationY = titleBarHeight - statusBarHeight
            // 设置大标题的高度
            bigTitleHight = bigTitle.measuredHeight.toFloat()
            // 创建 rect (一个矩形) 实例
            rect = Rect()
            // 将小标题偏移量变量设置为小标题偏移量
            smallTitle.translationY = smallTitleTranslationY
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
        var ratio = (rect.bottom - titleBarHeight) / bigTitleHight

        // 确保 ratio 不小于 0
        if (ratio < 0) {
            ratio = 0f
        } else if (ratio > 1) {
            ratio = 1f
        }

        // 如果大标题完全显示，则隐藏小标题；否则根据比例显示
        if (rect.bottom <= titleBarHeight) {
            smallTitle.translationY = 0f
            smallTitle.alpha = 1f - 0f
            bigTitle.alpha = 0f
        } else if (ratio == 1.0f) {
            smallTitle.translationY = smallTitleTranslationY
            smallTitle.alpha = 1f - 1f
            bigTitle.alpha = 1f
        } else {
            smallTitle.translationY = smallTitleTranslationY * ratio
            smallTitle.alpha = 1f - ratio
            bigTitle.alpha = ratio
            listener?.onRatioChange(ratio)
        }
    }

    interface OnRatioChangeListener {
        fun onRatioChange(ratio: Float)
    }
}

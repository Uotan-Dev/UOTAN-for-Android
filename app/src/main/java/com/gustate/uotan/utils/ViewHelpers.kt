package com.gustate.uotan.utils

import android.view.View
import androidx.core.animation.Animator
import androidx.core.animation.AnimatorListenerAdapter
import androidx.core.animation.AnimatorSet
import androidx.core.animation.ObjectAnimator
import androidx.core.view.isVisible

object ViewHelpers {

    private var isDoingSlideUp = false
    private var isDoingSlideDown = false

    fun View.slideUp(duration: Long = 300) {
        if (this.isVisible || isDoingSlideUp) return
        isDoingSlideUp = true

        // 先测量View的高度
        var height = this.height
        if (height == 0) {
            this.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            height = this.measuredHeight
        }

        // 设置初始位置和透明度
        this.translationY = height.toFloat() / 6
        this.alpha = 0f
        this.visibility = View.VISIBLE

        // 创建并启动动画集合
        val translateAnim = ObjectAnimator.ofFloat(this, "translationY", 0f)
        val alphaAnim = ObjectAnimator.ofFloat(this, "alpha", 1f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(translateAnim, alphaAnim)
        animatorSet.duration = duration
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                isDoingSlideUp = false
            }
        })
        animatorSet.start()
    }

    fun View.slideDown(duration: Long = 300) {
        if (!this.isVisible || isDoingSlideDown) return
        isDoingSlideDown = true

        // 创建并启动动画集合
        val translateAnim = ObjectAnimator.ofFloat(this, "translationY", this.height.toFloat() / 6)
        val alphaAnim = ObjectAnimator.ofFloat(this, "alpha", 0f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(translateAnim, alphaAnim)
        animatorSet.duration = duration
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                this@slideDown.visibility = View.GONE
                isDoingSlideDown = false
            }
        })
        animatorSet.start()
    }

    fun View.tSlide(duration: Long = 300) {
        if (this.isVisible) {
            this.slideDown(duration)
        } else {
            this.slideUp(duration)
        }
    }
}
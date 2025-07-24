package com.gustate.uotan.anim

import android.view.View
import androidx.core.animation.Animator
import androidx.core.animation.AnimatorListenerAdapter
import androidx.core.animation.LinearInterpolator
import androidx.core.animation.ObjectAnimator
import androidx.core.animation.ValueAnimator

class LoadingAnim {

    fun loadingAnim(view: View?): ObjectAnimator? {
        return view?.let {
            ObjectAnimator.ofFloat(it, "rotation", 0f, 360f)
        }?.apply {
            setDuration(1000)
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.rotation = 0f
                }
                override fun onAnimationCancel(animation: Animator) {
                    view.rotation = 0f
                }
            })
        }
    }

}
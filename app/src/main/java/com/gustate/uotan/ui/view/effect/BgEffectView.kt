package com.gustate.uotan.ui.view.effect

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.gustate.uotan.R

class BgEffectView : ConstraintLayout {
    private lateinit var mBgEffectController: BgEffectController

    constructor(context: Context) : super(context) { init(context) }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { init(context) }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context) }

    private fun init(context: Context) {

        val inflatedView = LayoutInflater.from(context).inflate(R.layout.layout_effect_bg, this, false)
        addView(inflatedView)

        mBgEffectController = BgEffectController(inflatedView)

        // 确保视图有合适的尺寸
        post {
            startRuntimeShader()
        }
    }

    fun startRuntimeShader() {
        post {
            mBgEffectController.start()
            mBgEffectController.setType(context, this, null)
        }
    }

    fun stopRuntimeShader() {
        mBgEffectController.stop()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startRuntimeShader()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRuntimeShader()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) {
            startRuntimeShader()
        } else {
            stopRuntimeShader()
        }
    }
}
package com.uotan.forum.ui.view.effect

import android.app.ActionBar
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.view.ViewGroup
import com.uotan.forum.R

class BgEffectController(private val mTarget: View) : Runnable {
    private var bound: FloatArray? = null
    private var mBgEffectPainter: BgEffectPainter? = null
    private var mDeltaTime: Float = 0f
    private var mLastGlobalTime: Long = 0
    private var mTime: Float = 0f
    private var mTimeDirection: Float = 1.0f

    fun start() {
        if (mBgEffectPainter == null) {
            mBgEffectPainter = BgEffectPainter(mTarget.context)
            mLastGlobalTime = System.nanoTime()
            resetTime()
            mTarget.post(this)
        }
    }

    override fun run() {
        mBgEffectPainter?.let {
            tickPingPong()
            it.setResolution(mTarget.width.toFloat(), mTarget.height.toFloat())
            it.updateMaterials(mDeltaTime)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mTarget.setRenderEffect(it.getRenderEffect())
            }
            mTarget.postDelayed(this, 16)
        }
    }

    private fun tickPingPong() {
        val currentTime = System.nanoTime()
        val delta = (currentTime - mLastGlobalTime) * 1.0E-9
        mDeltaTime = delta.toFloat()
        mTime += mDeltaTime * mTimeDirection

        if (mTimeDirection > 0) {
            if (mTime >= 7200.0f) {
                mTimeDirection = -1.0f
            }
        } else {
            if (mTime <= 0.0f) {
                mTimeDirection = 1.0f
            }
        }
        mLastGlobalTime = currentTime
    }

    fun resetTime() {
        mLastGlobalTime = System.nanoTime()
        mTime = 0.0f
        mTimeDirection = 1.0f
    }

    fun stop() {
        mTarget.removeCallbacks(this)
        mBgEffectPainter?.stop()
        mBgEffectPainter = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mTarget.setRenderEffect(null)
        }
    }

    fun setType(context: Context, view: View, actionBar: ActionBar?) {
        resetTime()
        calcAnimationBound(context, view, actionBar)

        val isNight = isNightMode(context)
        val deviceType = if (isTablet(context)) {
            BgEffectDataManager.DeviceType.TABLET
        } else {
            BgEffectDataManager.DeviceType.PHONE
        }
        val themeMode = if (isNight) {
            BgEffectDataManager.ThemeMode.DARK
        } else {
            BgEffectDataManager.ThemeMode.LIGHT
        }

        mBgEffectPainter?.setType(deviceType, themeMode, bound!!)
    }

    private fun calcAnimationBound(context: Context, view: View, actionBar: ActionBar?) {
        val actionBarHeight = actionBar?.height ?: 0
        val logoAreaHeight = context.resources.getDimensionPixelSize(R.dimen.about_logo_area_height)
        val totalHeight = actionBarHeight + logoAreaHeight
        val parentHeight = (view.parent as ViewGroup).height
        val heightRatio = totalHeight.toFloat() / parentHeight

        val parentWidth = (view.parent as ViewGroup).width

        bound = if (parentWidth <= totalHeight) {
            floatArrayOf(0.0f, 1.0f - heightRatio, 1.0f, heightRatio)
        } else {
            val widthRatio = totalHeight.toFloat() / parentWidth
            val xStart = (parentWidth - totalHeight) / 2.0f / parentWidth
            floatArrayOf(xStart, 1.0f - heightRatio, widthRatio, heightRatio)
        }
    }

    private fun isNightMode(context: Context): Boolean {
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }

    private fun isTablet(context: Context): Boolean {
        return context.resources.configuration.smallestScreenWidthDp >= 600
    }
}
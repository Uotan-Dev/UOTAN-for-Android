package com.uotan.forum.ui.view.effect

import android.content.Context
import android.content.res.Resources
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.OvershootInterpolator
import androidx.core.animation.ValueAnimator
import com.uotan.forum.R
import java.util.Scanner
import kotlin.math.abs
import kotlin.math.floor

class BgEffectPainter(context: Context) {
    private var cycleCount = 0f
    private var endColorValue: FloatArray
    private var mBgEffectData: BgEffectDataManager.BgEffectData
    private val mBgEffectDataManager = BgEffectDataManager()
    private var mBgRuntimeShader: RuntimeShader
    private val mHandler = Handler(Looper.getMainLooper())
    private var startColorValue: FloatArray
    private var uAnimTime = 0f
    private var uBgBound = floatArrayOf(0.0f, 0.4489f, 1.0f, 0.5511f)
    private var uColors = floatArrayOf(
        0.57f, 0.76f, 0.98f, 1.0f,
        0.98f, 0.85f, 0.68f, 1.0f,
        0.98f, 0.75f, 0.93f, 1.0f,
        0.73f, 0.7f, 0.98f, 1.0f
    )
    private var prevT = 0f
    var colorInterpT = 0f
    var gradientSpeed = 1f

    // 动画器
    private var colorAnimator: ValueAnimator? = null
    private var speedAnimator: ValueAnimator? = null
    private var speedResetRunnable: Runnable? = null

    init {
        val loadShader = loadShader(context.resources, R.raw.bg_frag)
        mBgRuntimeShader = RuntimeShader(loadShader)

        val data = mBgEffectDataManager.getData(
            BgEffectDataManager.DeviceType.PHONE,
            BgEffectDataManager.ThemeMode.LIGHT
        )
        mBgEffectData = data
        cycleCount = 0f

        // 设置着色器统一变量
        mBgRuntimeShader.setFloatUniform("uTranslateY", data.uTranslateY)
        data.uPoints?.let { mBgRuntimeShader.setFloatUniform("uPoints", it) }
        mBgRuntimeShader.setFloatUniform("uColors", uColors)
        mBgRuntimeShader.setFloatUniform("uNoiseScale", data.uNoiseScale)
        mBgRuntimeShader.setFloatUniform("uPointOffset", data.uPointOffset)
        mBgRuntimeShader.setFloatUniform("uPointRadiusMulti", data.uPointRadiusMulti)
        mBgRuntimeShader.setFloatUniform("uSaturateOffset", data.uSaturateOffset)
        mBgRuntimeShader.setFloatUniform("uShadowColorMulti", data.uShadowColorMulti)
        mBgRuntimeShader.setFloatUniform("uShadowColorOffset", data.uShadowColorOffset)
        mBgRuntimeShader.setFloatUniform("uShadowOffset", data.uShadowOffset)
        mBgRuntimeShader.setFloatUniform("uBound", uBgBound)
        mBgRuntimeShader.setFloatUniform("uAlphaMulti", data.uAlphaMulti)
        mBgRuntimeShader.setFloatUniform("uLightOffset", data.uLightOffset)
        mBgRuntimeShader.setFloatUniform("uAlphaOffset", data.uAlphaOffset)
        mBgRuntimeShader.setFloatUniform("uShadowNoiseScale", data.uShadowNoiseScale)

        startColorValue = data.gradientColors2!!
        endColorValue = data.gradientColors2!!

        // 初始化速度重置任务
        speedResetRunnable = Runnable {
            animateGradientSpeed(mBgEffectData.gradientSpeedRest)
        }
    }

    fun getRenderEffect(): RenderEffect {
        return RenderEffect.createShaderEffect(mBgRuntimeShader)
    }

    fun stop() {
        mHandler.removeCallbacksAndMessages(null)
        colorAnimator?.cancel()
        speedAnimator?.cancel()
        speedResetRunnable?.let { mHandler.removeCallbacks(it) }
    }

    fun updateMaterials(deltaTime: Float) {
        uAnimTime += deltaTime * gradientSpeed
        computeGradientColor()
        mBgRuntimeShader.setFloatUniform("uAnimTime", uAnimTime)
        mBgRuntimeShader.setFloatUniform("uColors", uColors)
    }

    fun setResolution(width: Float, height: Float) {
        mBgRuntimeShader.setFloatUniform("uResolution", width, height)
    }

    private fun loadShader(resources: Resources, id: Int): String {
        return try {
            resources.openRawResource(id).use { inputStream ->
                Scanner(inputStream).useDelimiter("\\A").next()
            }
        } catch (e: Exception) {
            Log.e("BgEffectPainter", "Error loading shader: ${e.message}")
            ""
        }
    }

    private fun computeGradientColor() {
        val d = uAnimTime / mBgEffectData.colorInterpPeriod
        val floorValue = floor(d)
        val t = (d - floorValue) * 2.0f

        // 修复：确保t在[0, 2)范围内
        val normalizedT = if (t >= 2f) t - 2f * floor(t / 2f) else t

        // 修复：使用更精确的阈值检测周期变化
        val cycleChanged = abs(normalizedT - prevT) > 1.5f || (normalizedT < 0.1f && prevT > 1.9f)

        if (cycleChanged) {
            when ((cycleCount % 4f).toInt()) {
                0 -> {
                    startColorValue = mBgEffectData.gradientColors2!!
                    endColorValue = mBgEffectData.gradientColors1!!
                    executeAnim()
                }
                1 -> {
                    startColorValue = mBgEffectData.gradientColors1!!
                    endColorValue = mBgEffectData.gradientColors2!!
                    executeAnim()
                }
                2 -> {
                    startColorValue = mBgEffectData.gradientColors2!!
                    endColorValue = mBgEffectData.gradientColors3!!
                    executeAnim()
                }
                3 -> {
                    startColorValue = mBgEffectData.gradientColors3!!
                    endColorValue = mBgEffectData.gradientColors2!!
                    executeAnim()
                }
            }
            cycleCount += 1f
        }
        prevT = normalizedT

        // 修复：确保插值参数在有效范围内
        val clampedT = colorInterpT.coerceIn(0f, 1f)
        linearInterpolate(uColors, startColorValue, endColorValue, clampedT)
    }

    private fun executeAnim() {
        // 取消之前的动画
        colorAnimator?.cancel()
        speedAnimator?.cancel()

        // 颜色插值动画 - 修复：使用正确的动画值
        colorAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                colorInterpT = (animation as ValueAnimator).animatedValue as Float
            }
            start()
        }

        // 渐变速度变化动画 - 修复：使用正确的动画值
        animateGradientSpeed(mBgEffectData.gradientSpeedChange, 300)

        // 延迟执行速度重置
        speedResetRunnable?.let { mHandler.removeCallbacks(it) }
        mHandler.postDelayed(speedResetRunnable!!, 300)
    }

    private fun animateGradientSpeed(targetSpeed: Float, duration: Long = 300L) {
        speedAnimator?.cancel()
        speedAnimator = ValueAnimator.ofFloat(gradientSpeed, targetSpeed).apply {
            this.duration = duration
            interpolator = OvershootInterpolator(0.6f)
            addUpdateListener { animation ->
                gradientSpeed = (animation as ValueAnimator).animatedValue as Float
            }
            start()
        }
    }

    companion object {
        fun linearInterpolate(out: FloatArray, start: FloatArray, end: FloatArray, t: Float) {
            for (i in start.indices) {
                out[i] = start[i] + (end[i] - start[i]) * t
            }
        }
    }

    fun setType(deviceType: BgEffectDataManager.DeviceType,
                themeMode: BgEffectDataManager.ThemeMode,
                bound: FloatArray) {
        uBgBound = bound
        mBgRuntimeShader.setFloatUniform("uBound", bound)

        val data = mBgEffectDataManager.getData(deviceType, themeMode)
        mBgEffectData = data
        uAnimTime = 0f

        startColorValue = data.gradientColors2!!
        endColorValue = data.gradientColors2!!

        // 重置状态变量
        cycleCount = 0f
        prevT = 0f
        colorInterpT = 0f
        gradientSpeed = data.gradientSpeedRest

        // 更新着色器参数
        mBgRuntimeShader.setFloatUniform("uTranslateY", data.uTranslateY)
        data.uPoints?.let { mBgRuntimeShader.setFloatUniform("uPoints", it) }
        mBgRuntimeShader.setFloatUniform("uNoiseScale", data.uNoiseScale)
        mBgRuntimeShader.setFloatUniform("uPointOffset", data.uPointOffset)
        mBgRuntimeShader.setFloatUniform("uPointRadiusMulti", data.uPointRadiusMulti)
        mBgRuntimeShader.setFloatUniform("uSaturateOffset", data.uSaturateOffset)
        mBgRuntimeShader.setFloatUniform("uShadowColorMulti", data.uShadowColorMulti)
        mBgRuntimeShader.setFloatUniform("uShadowColorOffset", data.uShadowColorOffset)
        mBgRuntimeShader.setFloatUniform("uShadowOffset", data.uShadowOffset)
        mBgRuntimeShader.setFloatUniform("uAlphaMulti", data.uAlphaMulti)
        mBgRuntimeShader.setFloatUniform("uLightOffset", data.uLightOffset)
        mBgRuntimeShader.setFloatUniform("uAlphaOffset", data.uAlphaOffset)
        mBgRuntimeShader.setFloatUniform("uShadowNoiseScale", data.uShadowNoiseScale)

        // 立即更新一次颜色
        linearInterpolate(uColors, startColorValue, endColorValue, colorInterpT)
        mBgRuntimeShader.setFloatUniform("uColors", uColors)
    }
}
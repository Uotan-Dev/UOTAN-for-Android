package com.gustate.uotan.view.smooth

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.withClip
import com.gustate.uotan.R
import kotlin.math.max
import kotlin.math.min

open class SmoothCornerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    // 圆角参数
    private var cornerRadiusTL: Float = 0f
    private var smoothnessTL: Float = DEFAULT_SMOOTHNESS
    private var cornerRadiusTR: Float = 0f
    private var smoothnessTR: Float = DEFAULT_SMOOTHNESS
    private var cornerRadiusBR: Float = 0f
    private var smoothnessBR: Float = DEFAULT_SMOOTHNESS
    private var cornerRadiusBL: Float = 0f
    private var smoothnessBL: Float = DEFAULT_SMOOTHNESS

    // 边框参数
    private var borderWidth: Float = 0f
    private var borderColor: Int = Color.TRANSPARENT
    private val enableBorder: Boolean get() = borderWidth > 0 && borderColor != Color.TRANSPARENT

    // 绘制路径和画笔
    private val clipPath = Path()
    private val borderPath = Path()
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    // 尺寸缓存
    private var lastWidth: Int = 0
    private var lastHeight: Int = 0

    private var preDrawListener: ViewTreeObserver.OnPreDrawListener? = null

    companion object {
        private const val DEFAULT_SMOOTHNESS = 0.6f
        private const val MIN_RADIUS = 0f
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.SmoothCornerLayout) {
            val radius = getDimension(R.styleable.SmoothCornerLayout_cornerRadius, 0f)
            val smoothness = getFloat(R.styleable.SmoothCornerLayout_smoothness, DEFAULT_SMOOTHNESS)

            // 读取圆角参数
            cornerRadiusTL = getDimension(R.styleable.SmoothCornerLayout_cornerRadiusTL, radius)
            smoothnessTL = getFloat(R.styleable.SmoothCornerLayout_smoothnessTL, smoothness)
            cornerRadiusTR = getDimension(R.styleable.SmoothCornerLayout_cornerRadiusTR, radius)
            smoothnessTR = getFloat(R.styleable.SmoothCornerLayout_smoothnessTR, smoothness)
            cornerRadiusBR = getDimension(R.styleable.SmoothCornerLayout_cornerRadiusBR, radius)
            smoothnessBR = getFloat(R.styleable.SmoothCornerLayout_smoothnessBR, smoothness)
            cornerRadiusBL = getDimension(R.styleable.SmoothCornerLayout_cornerRadiusBL, radius)
            smoothnessBL = getFloat(R.styleable.SmoothCornerLayout_smoothnessBL, smoothness)

            // 读取边框参数
            borderWidth = getDimension(R.styleable.SmoothCornerLayout_scl_border_width, 0f)
            borderColor = getColor(
                R.styleable.SmoothCornerLayout_scl_border_color,
                Color.TRANSPARENT
            )
        }
        clipToOutline = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0 || h == 0) return

        if (w != lastWidth || h != lastHeight) {
            lastWidth = w
            lastHeight = h
            updateClipPath(w, h)
            updateBorderPaint()
            updateOutlineProvider()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        // 使用缓存的路径进行裁剪
        canvas.withClip(clipPath) {
            super.dispatchDraw(this)
        }
        canvas.drawPath(borderPath, borderPaint)
    }

    override fun onDraw(canvas: Canvas) {
        // 添加这个检查确保子视图能正确刷新
        if (clipPath.isEmpty) {
            updateClipPath(width, height)
        }
        super.onDraw(canvas)
        if (enableBorder) {
            canvas.drawPath(borderPath, borderPaint)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 添加一次性预绘制监听器，确保首次加载时刷新轮廓
        preDrawListener = ViewTreeObserver.OnPreDrawListener {
            updateOutlineProvider()
            viewTreeObserver.removeOnPreDrawListener(preDrawListener!!)
            preDrawListener = null
            true
        }
        viewTreeObserver.addOnPreDrawListener(preDrawListener)
    }

    // 新增方法：统一更新轮廓提供者
    private fun updateOutlineProvider() {
        outlineProvider = SmoothCornerOutlineProvider()
        invalidateOutline()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 清理预绘制监听器
        preDrawListener?.let {
            viewTreeObserver.removeOnPreDrawListener(it)
            preDrawListener = null
        }
    }

    private fun updateClipPath(w: Int, h: Int) {
        clipPath.rewind()
        createSmoothPath(clipPath, w, h, inset = 0f)

        borderPath.rewind()
        if (enableBorder) {
            // 边框向内缩进，确保完整显示
            val inset = max(borderWidth / 2, 1f) // 至少1px避免渲染问题
            createSmoothPath(borderPath, w, h, inset)
        }
    }

    private fun updateBorderPaint() {
        borderPaint.strokeWidth = borderWidth
        borderPaint.color = borderColor
    }

    /**
     * 创建平滑路径（优化后的实现）
     */
    private fun createSmoothPath(
        path: Path,
        width: Int,
        height: Int,
        inset: Float
    ) {
        val w = width.toFloat()
        val h = height.toFloat()
        val inset2 = inset * 2

        // 安全半径计算（确保不超过可用空间）
        val maxRadiusX = (w - inset2) / 2
        val maxRadiusY = (h - inset2) / 2
        val maxRadius = min(maxRadiusX, maxRadiusY)

        // 应用安全限制
        val rTL = cornerRadiusTL.coerceIn(MIN_RADIUS, maxRadius)
        val rTR = cornerRadiusTR.coerceIn(MIN_RADIUS, maxRadius)
        val rBR = cornerRadiusBR.coerceIn(MIN_RADIUS, maxRadius)
        val rBL = cornerRadiusBL.coerceIn(MIN_RADIUS, maxRadius)

        // 控制点偏移量
        val cpTL = rTL * smoothnessTL
        val cpTR = rTR * smoothnessTR
        val cpBR = rBR * smoothnessBR
        val cpBL = rBL * smoothnessBL

        path.moveTo(inset, inset + rTL)

        // 左上角
        if (rTL > 0) {
            path.cubicTo(
                inset, inset + rTL - cpTL,
                inset + rTL - cpTL, inset,
                inset + rTL, inset
            )
        } else {
            path.lineTo(inset, inset)
        }

        // 顶部连线
        path.lineTo(w - inset - rTR, inset)

        // 右上角
        if (rTR > 0) {
            path.cubicTo(
                w - inset - rTR + cpTR, inset,
                w - inset, inset + rTR - cpTR,
                w - inset, inset + rTR
            )
        } else {
            path.lineTo(w - inset, inset)
        }

        // 右侧连线
        path.lineTo(w - inset, h - inset - rBR)

        // 右下角
        if (rBR > 0) {
            path.cubicTo(
                w - inset, h - inset - rBR + cpBR,
                w - inset - rBR + cpBR, h - inset,
                w - inset - rBR, h - inset
            )
        } else {
            path.lineTo(w - inset, h - inset)
        }

        // 底部连线
        path.lineTo(inset + rBL, h - inset)

        // 左下角
        if (rBL > 0) {
            path.cubicTo(
                inset + rBL - cpBL, h - inset,
                inset, h - inset - rBL + cpBL,
                inset, h - inset - rBL
            )
        } else {
            path.lineTo(inset, h - inset)
        }

        // 闭合路径
        path.close()
    }

    /**
     * 自定义ViewOutlineProvider（避免每次创建新实例）
     */
    private inner class SmoothCornerOutlineProvider : ViewOutlineProvider() {
        private val outlinePath = Path()

        override fun getOutline(view: View, outline: Outline) {
            outlinePath.rewind()
            createSmoothPath(outlinePath, view.width, view.height, inset = 0f)
            outline.setPath(outlinePath)
        }
    }
}
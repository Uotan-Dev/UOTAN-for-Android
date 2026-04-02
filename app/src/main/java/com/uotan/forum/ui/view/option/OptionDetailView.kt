package com.uotan.forum.ui.view.option

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.withStyledAttributes
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import com.uotan.forum.R
import com.uotan.forum.databinding.UotanOptionDetailBinding
import com.uotan.forum.utils.Utils.dpToPx
import com.uotan.forum.ui.view.smooth.SmoothCornerLayout
import kotlin.math.roundToInt

class OptionDetailView(context: Context, attrs: AttributeSet): SmoothCornerLayout(context, attrs) {

    // 视图绑定
    private val binding = UotanOptionDetailBinding
        .inflate(LayoutInflater.from(context), this, true)

    var title = ""
        set(value) {
            binding.tvTitle.text = value
            field = value
        }

    var detail = ""
        set(value) {
            binding.tvDetail.text = value
            field = value
        }

    var isArrowGone = false
        set(value) {
            binding.icoArrow.isGone = value
            binding.tvDetail.updateLayoutParams<MarginLayoutParams> {
                rightMargin = if (value) 18f.dpToPx(context).roundToInt()
                else 12f.dpToPx(context).roundToInt()
            }
            field = value
        }

    var background = -1
        set(value) {
            if (value != -1)
                binding.root.setBackgroundResource(value)
            field = value
        }

    // 初始化
    init {
        // 取 xml 值
        context.withStyledAttributes(attrs, R.styleable.OptionDetailView) {
            title = getString(R.styleable.OptionDetailView_odv_title) ?: ""
            detail = getString(R.styleable.OptionDetailView_odv_detail) ?: ""
            isArrowGone = getBoolean(
                R.styleable.OptionDetailView_odv_arrow_gone, false)
            background = getResourceId(
                R.styleable.OptionDetailView_android_background, -1)
        }
    }
}
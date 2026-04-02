package com.uotan.forum.ui.view.option

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.withStyledAttributes
import com.bumptech.glide.Glide
import com.uotan.forum.R
import com.uotan.forum.databinding.UotanOptionUserBinding
import com.uotan.forum.utils.Helpers.avatarOptions
import com.uotan.forum.ui.view.smooth.SmoothCornerLayout

class OptionUserView(context: Context, attrs: AttributeSet): SmoothCornerLayout(context, attrs) {

    private val binding = UotanOptionUserBinding
        .inflate(LayoutInflater.from(context), this, true)

    var avatarAssets = ""
        set(value) {
            if (value == "") return
            Glide.with(context)
                .load(value)
                .apply(avatarOptions)
                .into(binding.imgAvatar)
            field = value
        }

    var title = ""
        set(value) {
            binding.tvTitle.text = value
            field = value
        }

    var describe = ""
        set(value) {
            binding.tvDescribe.text = value
            field = value
        }

    // 初始化
    init {
        context.withStyledAttributes(attrs, R.styleable.OptionUserView) {
            avatarAssets = getString(R.styleable.OptionUserView_ouv_avatar_assets) ?: ""
            title = getString(R.styleable.OptionUserView_ouv_title) ?: ""
            describe = getString(R.styleable.OptionUserView_ouv_describe) ?: ""
        }
    }

}
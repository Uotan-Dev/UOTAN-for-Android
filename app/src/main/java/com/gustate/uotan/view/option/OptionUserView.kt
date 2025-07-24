package com.gustate.uotan.view.option

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.utils.Helpers.Companion.avatarOptions
import com.gustate.uotan.view.smooth.SmoothCornerLayout

class OptionUserView(context: Context, attrs: AttributeSet): SmoothCornerLayout(context, attrs) {

    // 初始化
    init {

        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.uotan_option_user, this, true)

        // 加载自定义项
        val customStyle = context.theme.obtainStyledAttributes(attrs, R.styleable.OptionUserView, 0, 0)

        // 获取资源 ID
        // 获取选项卡头像的 ImageView
        val imgAvatar = findViewById<ImageView>(R.id.img_avatar)
        // 获取选项卡标题的 TextView
        val tvOption = findViewById<TextView>(R.id.tv_option)
        // 获取选项卡介绍的 TextView
        val tvDescribe = findViewById<TextView>(R.id.tv_describe)

        // 加载选项卡标题
        try {
            val avatarResID = customStyle.getResourceId(R.styleable.OptionUserView_optionAvatar_user, 0)
            val titleResID = customStyle.getResourceId(R.styleable.OptionUserView_optionTitle_user, 0)
            val describeResID = customStyle.getResourceId(R.styleable.OptionUserView_optionDescribe_user, 0)
            if (avatarResID != 0) {
                Glide.with(imgAvatar.context)
                    .load(avatarResID)
                    .apply(avatarOptions)
                    .into(imgAvatar)
            }
            if (titleResID != 0) {
                tvOption.setText(titleResID)
            }
            if (describeResID != 0) {
                tvDescribe.setText(describeResID)
            }
        } finally {
            customStyle.recycle()
        }

    }

}
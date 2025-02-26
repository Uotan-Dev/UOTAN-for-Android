package com.gustate.uotan.gustatex.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.gustate.uotan.R

class OptionView(context: Context, attrs: AttributeSet): ConstraintLayout(context, attrs) {

    // 初始化
    init {

        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.gustatex_option, this, true)

        // 加载自定义项
        val customStyle = context.theme.obtainStyledAttributes(attrs, R.styleable.OptionView, 0, 0)

        // 获取资源 ID
        // 获取选项卡图标的 ImageView
        val optionIcon = findViewById<ImageView>(R.id.optionIcon)
        // 获取选项卡文本的 TextView
        val optionText = findViewById<TextView>(R.id.optionText)

        // 加载选项卡图标
        try {
            val iconResID = customStyle.getResourceId(R.styleable.OptionView_optionIcon, 0)
            val textResID = customStyle.getResourceId(R.styleable.OptionView_optionTitle, 0)
            if (iconResID != 0) {
                optionIcon.setImageResource(iconResID)
            }
            if (textResID != 0) {
                optionText.setText(textResID)
            }
        } finally {
            customStyle.recycle()
        }

    }

}

class OptionOnlyTitleView(context: Context, attrs: AttributeSet): ConstraintLayout(context, attrs) {

    // 初始化
    init {

        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.gustatex_option_onlytitle, this, true)

        // 加载自定义项
        val customStyle = context.theme.obtainStyledAttributes(attrs, R.styleable.OptionOnlyTitleView, 0, 0)

        // 获取资源 ID
        // 获取选项卡文本的 TextView
        val optionText = findViewById<TextView>(R.id.optionText)

        // 加载选项卡标题
        try {
            val textResID = customStyle.getResourceId(R.styleable.OptionOnlyTitleView_optionTitle_onlyTitle, 0)
            if (textResID != 0) {
                optionText.setText(textResID)
            }
        } finally {
            customStyle.recycle()
        }

    }

}

class OptionTitleAndDescribeView(context: Context, attrs: AttributeSet): ConstraintLayout(context, attrs) {

    // 初始化
    init {

        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.gustatex_option_title_and_describe, this, true)

        // 加载自定义项
        val customStyle = context.theme.obtainStyledAttributes(attrs, R.styleable.OptionTitleAndDescribeView, 0, 0)

        // 获取资源 ID
        // 获取选项卡标题的 TextView
        val optionTitle = findViewById<TextView>(R.id.optionText)
        // 获取选项卡介绍的 TextView
        val optionDescribe = findViewById<TextView>(R.id.optionDescribe)

        // 加载选项卡标题
        try {
            val titleResID = customStyle.getResourceId(R.styleable.OptionTitleAndDescribeView_optionTitle_tiltleAndDescribe, 0)
            val describeResID = customStyle.getResourceId(R.styleable.OptionTitleAndDescribeView_optionDescribe_tiltleAndDescribe, 0)
            if (titleResID != 0) {
                optionTitle.setText(titleResID)
            }
            if (describeResID != 0) {
                optionDescribe.setText(describeResID)
            }
        } finally {
            customStyle.recycle()
        }

    }

}

class OptionUserView(context: Context, attrs: AttributeSet): ConstraintLayout(context, attrs) {

    // 初始化
    init {

        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.gustatex_option_user, this, true)

        // 加载自定义项
        val customStyle = context.theme.obtainStyledAttributes(attrs, R.styleable.OptionUserView, 0, 0)

        // 获取资源 ID
        // 获取选项卡头像的 ImageView
        val optionAvatar = findViewById<ImageView>(R.id.optionAvatar)
        // 获取选项卡标题的 TextView
        val optionTitle = findViewById<TextView>(R.id.optionText)
        // 获取选项卡介绍的 TextView
        val optionDescribe = findViewById<TextView>(R.id.optionDescribe)

        // 加载选项卡标题
        try {
            val avatarResID = customStyle.getResourceId(R.styleable.OptionUserView_optionAvatar_user, 0)
            val titleResID = customStyle.getResourceId(R.styleable.OptionUserView_optionTitle_user, 0)
            val describeResID = customStyle.getResourceId(R.styleable.OptionUserView_optionDescribe_user, 0)
            if (avatarResID != 0) {
                optionAvatar.setImageResource(avatarResID)
            }
            if (titleResID != 0) {
                optionTitle.setText(titleResID)
            }
            if (describeResID != 0) {
                optionDescribe.setText(describeResID)
            }
        } finally {
            customStyle.recycle()
        }

    }

}
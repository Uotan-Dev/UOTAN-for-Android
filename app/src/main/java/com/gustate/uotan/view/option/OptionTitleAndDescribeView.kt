package com.gustate.uotan.view.option

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import com.gustate.uotan.R
import com.gustate.uotan.view.smooth.SmoothCornerLayout

class OptionTitleAndDescribeView(context: Context, attrs: AttributeSet): SmoothCornerLayout(context, attrs) {

    // 初始化
    init {

        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.uotan_option_title_and_describe, this, true)

        // 加载自定义项
        val customStyle = context.theme.obtainStyledAttributes(attrs, R.styleable.OptionTitleAndDescribeView, 0, 0)

        // 获取资源 ID
        // 获取选项卡标题的 TextView
        val tvOption = findViewById<TextView>(R.id.tv_option)
        // 获取选项卡介绍的 TextView
        val tvDescribe = findViewById<TextView>(R.id.tv_describe)

        // 加载选项卡标题
        try {
            val titleResID = customStyle.getResourceId(R.styleable.OptionTitleAndDescribeView_optionTitle_tiltleAndDescribe, 0)
            val describeResID = customStyle.getResourceId(R.styleable.OptionTitleAndDescribeView_optionDescribe_tiltleAndDescribe, 0)
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
    fun setDescribe(value: String) {
        val tvDescribe = findViewById<TextView>(R.id.tv_describe)
        tvDescribe.text = value
    }

}
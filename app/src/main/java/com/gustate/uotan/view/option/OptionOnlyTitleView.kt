package com.gustate.uotan.view.option

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import com.gustate.uotan.R
import com.gustate.uotan.view.smooth.SmoothCornerLayout

class OptionOnlyTitleView(context: Context, attrs: AttributeSet): SmoothCornerLayout(context, attrs) {

    // 初始化
    init {

        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.uotan_option_only_title, this, true)

        // 加载自定义项
        val customStyle = context.theme.obtainStyledAttributes(attrs, R.styleable.OptionOnlyTitleView, 0, 0)

        // 获取资源 ID
        // 获取选项卡文本的 TextView
        val tvOption = findViewById<TextView>(R.id.tv_option)

        // 加载选项卡标题
        try {
            val textResID = customStyle.getResourceId(R.styleable.OptionOnlyTitleView_optionTitle_onlyTitle, 0)
            if (textResID != 0) {
                tvOption.setText(textResID)
            }
        } finally {
            customStyle.recycle()
        }

    }

}
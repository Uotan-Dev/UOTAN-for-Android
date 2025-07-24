package com.gustate.uotan.view.option

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.gustate.uotan.R
import com.gustate.uotan.view.smooth.SmoothCornerLayout

class OptionView(context: Context, attrs: AttributeSet): SmoothCornerLayout(context, attrs) {

    // 初始化
    init {

        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.uotan_option, this, true)

        // 加载自定义项
        val customStyle = context.theme.obtainStyledAttributes(attrs, R.styleable.OptionView, 0, 0)

        // 获取资源 ID
        // 获取选项卡图标的 ImageView
        val img = findViewById<ImageView>(R.id.img_option)
        // 获取选项卡文本的 TextView
        val tv = findViewById<TextView>(R.id.tv_option)

        // 加载选项卡图标
        try {
            val iconResID = customStyle.getResourceId(R.styleable.OptionView_optionIcon, 0)
            val textResID = customStyle.getResourceId(R.styleable.OptionView_optionTitle, 0)
            if (iconResID != 0) {
                img.setImageResource(iconResID)
            }
            if (textResID != 0) {
                tv.setText(textResID)
            }
        } finally {
            customStyle.recycle()
        }

    }

}
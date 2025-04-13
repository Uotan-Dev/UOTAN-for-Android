package com.gustate.uotan.gustatex.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.gustate.uotan.R

/**
 * 加载 (Dialog)
 * JiaGuZhuangZhi Miles
 * Gustate 02/23/2025
 * I Love Jiang’Xun
 */

class LoadingDialog(context: Context) : Dialog(context, R.style.Gustatex_Dialog) {

    /**
     * 创建布局后
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        /** 设置布局 **/
        setContentView(R.layout.gustatex_dialog_loading)

        // 禁止点击空白处关闭
        setCanceledOnTouchOutside(false)

        // 禁用系统默认返回键关闭行为，改为自定义逻辑
        setCancelable(false)

        // 监听返回键事件
        setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                (context as? Activity)?.finish() // 结束当前Activity
                dismiss() // 关闭对话框
                true // 消费事件
            } else {
                false
            }
        }


        /** 从布局中取出需要调用的视图 **/
        // 加载图片
        val loadingImage = findViewById<ImageView>(R.id.loadingImage)

        /** 播放动图 **/
        // 使用 Glide
        Glide
            .with(context)
            .load(R.drawable.loading)
            .into(loadingImage)

    }

}
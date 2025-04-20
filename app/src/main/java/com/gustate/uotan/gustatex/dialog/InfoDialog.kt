@file:Suppress("unused")

package com.gustate.uotan.gustatex.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.gustate.uotan.R
import com.gustate.uotan.databinding.GustatexDialogInfoBinding

class InfoDialog(context: Context) : Dialog(context, R.style.Gustatex_Dialog) {
    private var binding: GustatexDialogInfoBinding? = null

    // 临时存储变量
    private var pendingTitle: String? = null
    private var pendingDescription: String? = null
    private var pendingConfirm: String? = null
    private var pendingCancel: String? = null

    // 回调
    var onConfirm: (() -> Unit)? = null
    var onCancel: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GustatexDialogInfoBinding.inflate(layoutInflater)
        binding?.root?.let { setContentView(it) }

        // 应用缓存的配置
        pendingTitle?.let { binding?.title?.text = it }
        pendingDescription?.let { binding?.describe?.text = it }
        pendingConfirm?.let { binding?.ok?.text = it }
        pendingCancel?.let { binding?.cancel?.text = it }

        initWindowSettings()
        setupClickListeners()
    }

    private fun initWindowSettings() {
        window?.apply {
            attributes = attributes.apply {
                gravity = Gravity.BOTTOM
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
        }
    }

    private fun setupClickListeners() {
        binding?.ok?.setOnClickListener {
            onConfirm?.invoke()
        }

        binding?.cancel?.setOnClickListener {
            onCancel?.invoke()
        }
    }

    // 以下是链式调用配置方法
    // 修改配置方法
    fun setTitle(text: String): InfoDialog {
        binding?.title?.text = text
        pendingTitle = text
        return this
    }

    fun setDescription(text: String): InfoDialog {
        binding?.describe?.text = text
        pendingDescription = text
        return this
    }

    fun setConfirmText(text: String): InfoDialog {
        binding?.ok?.text = text
        pendingConfirm = text
        return this
    }

    fun setCancelText(text: String): InfoDialog {
        binding?.cancel?.text = text
        pendingCancel = text
        return this
    }

    fun withOnConfirm(listener: () -> Unit): InfoDialog {
        onConfirm = listener
        return this
    }

    fun withOnCancel(listener: () -> Unit): InfoDialog {
        onCancel = listener
        return this
    }

}
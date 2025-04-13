package com.gustate.uotan.gustatex.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.gustate.uotan.R
import com.gustate.uotan.databinding.GustatexDialogInputBinding

class InputDialog(context: Context) : Dialog(context, R.style.Gustatex_Dialog) {
    private lateinit var binding: GustatexDialogInputBinding
    private var inputText: String = ""

    // 使用lambda表达式替代接口
    var onConfirm: ((String) -> Unit)? = null
    var onCancel: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GustatexDialogInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initWindowSettings()
        setupClickListeners()
    }

    private fun initWindowSettings() {
        window?.apply {
            // 底部显示配置
            setGravity(Gravity.BOTTOM)
            attributes = attributes.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    private fun setupClickListeners() {
        binding.ok.setOnClickListener {
            inputText = binding.etContent.text.toString().trim()
            if (inputText.isNotEmpty()) {
                onConfirm?.invoke(inputText)
            }
            dismiss()
        }

        binding.cancel.setOnClickListener {
            onCancel?.invoke()
            dismiss()
        }
    }

    // 构建者模式配置方法
    fun setTitle(text: String): InputDialog {
        binding.title.text = text
        return this
    }

    fun setDescription(text: String): InputDialog {
        binding.describe.text = text
        return this
    }

    fun setInitialInput(text: String): InputDialog {
        binding.etContent.setText(text)
        return this
    }

    // 兼容Java的链式调用
    fun withOnConfirm(listener: (String) -> Unit): InputDialog {
        onConfirm = listener
        return this
    }

    fun withOnCancel(listener: () -> Unit): InputDialog {
        onCancel = listener
        return this
    }
}
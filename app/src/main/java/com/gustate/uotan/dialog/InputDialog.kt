package com.gustate.uotan.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.isGone
import com.gustate.uotan.R
import com.gustate.uotan.databinding.GustatexDialogInputBinding

class InputDialog(context: Context) : Dialog(context, R.style.Gustatex_Dialog) {

    private lateinit var binding: GustatexDialogInputBinding

    private var inputText: String = ""
    private var inputText2: String = ""

    // 临时存储变量
    private var pendingTitle: String? = null
    private var pendingDescription: String? = null
    private var pendingConfirm: String? = null
    private var pendingCancel: String? = null
    private var hintO: String? = null
    private var hintT: String? = null
    private var hasTwoEdit = false

    // 回调
    var onConfirm: ((String, String) -> Unit)? = null
    var onCancel: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GustatexDialogInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTitle.text = pendingTitle
        binding.tvDescribe.text = pendingDescription
        binding.tvCancel.text = pendingCancel
        binding.tvOk.text = pendingConfirm
        binding.etContent.hint = hintO
        binding.etContent2.hint = hintT
        binding.etContent2.isGone = !hasTwoEdit

        initWindowSettings()
        setupClickListeners()
    }

    private fun initWindowSettings() {
        window?.apply {
            // 底部显示配置
            setGravity(Gravity.CENTER)
            attributes = attributes.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    private fun setupClickListeners() {
        binding.btnOk.setOnClickListener {
            inputText = binding.etContent.text.toString().trim()
            inputText2 = binding.etContent2.text.toString().trim()
            if (inputText.isNotEmpty() && inputText2.isNotEmpty()) {
                onConfirm?.invoke(inputText, inputText2)
            } else {
                Toast.makeText(context, "请输入文本", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancel.setOnClickListener {
            onCancel?.invoke()
        }
    }

    // 构建者模式配置方法
    fun setTitle(text: String): InputDialog {
        pendingTitle = text
        return this
    }

    // 构建者模式配置方法
    fun setTwoEdit(enable: Boolean): InputDialog {
        hasTwoEdit = enable
        return this
    }

    fun setDescription(text: String): InputDialog {
        pendingDescription = text
        return this
    }

    // 构建者模式配置方法
    fun setEditHint(hint1: String, hint2: String): InputDialog {
        hintO = hint1
        hintT = hint2
        return this
    }

    fun setCancel(text: String): InputDialog {
        pendingCancel = text
        return this
    }

    fun setConfirm(text: String): InputDialog {
        pendingConfirm = text
        return this
    }

    // 兼容Java的链式调用
    fun withOnConfirm(listener: (String, String) -> Unit): InputDialog {
        onConfirm = listener
        return this
    }

    fun withOnCancel(listener: () -> Unit): InputDialog {
        onCancel = listener
        return this
    }
}
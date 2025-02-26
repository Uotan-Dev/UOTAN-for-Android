package com.gustate.uotan.gustatex.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gustate.uotan.R

class InputDialog(context: Context, private val dialogId: Int, private val listener: InputDialogListener) : Dialog(context, R.style.Gustatex_Dialog) {

    // private 私有变量
    // lateinit 延迟启动，防止空指针异常
    // 标题文本框
    private lateinit var title: TextView
    // 介绍文本框
    private lateinit var describe: TextView
    // 输入框文本框
    private lateinit var input: EditText
    // 关闭文本框
    private lateinit var cancel: View
    // 确定文本框
    private lateinit var ok: View

    interface InputDialogListener {
        fun onInputSubmitted(dialogId: Int, input: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.gustatex_dialog_input)

        title = findViewById(R.id.title)
        describe = findViewById(R.id.describe)
        input = findViewById(R.id.editText)
        cancel = findViewById(R.id.cancel)
        ok = findViewById(R.id.ok)

        val rootLayout: View = findViewById(R.id.rootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            rootLayout.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        ok.setOnClickListener {
            val inputString = input.text.toString()
            if (inputString != "") {
                listener.onInputSubmitted(dialogId, inputString)
                dismiss()
            }
            else {
                dismiss()
            }
        }

        cancel.setOnClickListener {
            dismiss()
        }

        // 设置对话框宽高

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(window?.attributes)
        layoutParams.gravity = Gravity.BOTTOM
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = layoutParams

        // 设置软键盘弹出时对话框的调整方式
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

    }

    fun updateContent(titleText: String, describeText: String) {

        title.text = titleText
        describe.text = describeText

    }

}
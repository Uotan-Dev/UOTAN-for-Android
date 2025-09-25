@file:Suppress("unused")

package com.gustate.uotan.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.gustate.uotan.R
import com.gustate.uotan.databinding.GustatexDialogInfoBinding
import com.gustate.uotan.utils.Utils.getThemeColor
import net.center.blurview.ShapeBlurView
import net.center.blurview.enu.BlurMode

class InfoDialog(context: Context) : Dialog(context, R.style.Gustatex_Dialog) {
    private var binding: GustatexDialogInfoBinding? = null

    // 属性定义
    var title: String? = null
        set(value) {
            field = value
            binding?.tvTitle?.text = value
        }

    var description: String? = null
        set(value) {
            field = value
            binding?.tvDescribe?.text = value
        }

    var confirmText: String? = null
        set(value) {
            field = value
            binding?.tvOk?.text = value
        }

    var cancelText: String? = null
        set(value) {
            field = value
            binding?.tvCancel?.text = value
        }

    // 回调
    var onConfirm: (() -> Unit)? = null
    var onCancel: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GustatexDialogInfoBinding.inflate(layoutInflater)
        binding?.root?.let { setContentView(it) }
        val blurOverlay =
            (196 shl 24) or (getThemeColor(context, R.attr.colorCardNormal) and 0x00FFFFFF)
        binding?.blurBkg?.refreshView(
            ShapeBlurView.build(context)
                .setBlurMode(BlurMode.MODE_RECTANGLE)
                .setBlurRadius(25f)
                .setOverlayColor(blurOverlay)
        )

        // 应用已设置的属性值
        title?.let { binding?.tvTitle?.text = it }
        description?.let { binding?.tvDescribe?.text = it }
        confirmText?.let { binding?.tvOk?.text = it }
        cancelText?.let { binding?.tvCancel?.text = it }

        initWindowSettings()
        setupClickListeners()
    }

    private fun initWindowSettings() {
        window?.apply {
            attributes = attributes.apply {
                gravity = Gravity.CENTER
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
        }
    }

    private fun setupClickListeners() {
        binding?.btnOk?.setOnClickListener {
            onConfirm?.invoke()
            dismiss()
        }

        binding?.btnCancel?.setOnClickListener {
            onCancel?.invoke()
            dismiss()
        }
    }

    // 链式调用方法
    fun setTitle(text: String): InfoDialog {
        this.title = text
        return this
    }

    fun setDescription(text: String): InfoDialog {
        this.description = text
        return this
    }

    fun setConfirmText(text: String): InfoDialog {
        this.confirmText = text
        return this
    }

    fun setCancelText(text: String): InfoDialog {
        this.cancelText = text
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
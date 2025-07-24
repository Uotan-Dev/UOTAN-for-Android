package com.gustate.uotan.dialog

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.gustate.uotan.R
import com.gustate.uotan.gustatex.dialog.InfoDialog
import com.gustate.uotan.settings.ui.DomainActivity
import net.center.blurview.ShapeBlurView
import javax.net.ssl.SSLHandshakeException

class ErrorDialog(private val context: Context) {
    fun setupErrorDialog(e: Throwable) {
        Toast.makeText(context, e.javaClass.name, Toast.LENGTH_SHORT).show()
        when(e) {
            is SSLHandshakeException -> {
                errorDialog(true, "证书错误", "未找到证书路径的信任锚，请检查自定义域名或服务器。", context.getString(R.string.domain_customization)) {
                    context.startActivity(Intent(context, DomainActivity::class.java))
                }
            }
            else -> {
                errorDialog(true, "毁灭性故障", "请你查看“域名与代理”设置项以排查错误。", context.getString(R.string.domain_customization)) {
                    context.startActivity(Intent(context, DomainActivity::class.java))
                }
            }
        }
    }

    private fun errorDialog(
        isCustomDispose: Boolean,
        title: String,
        message: String?,
        confirmText: String,
        onConfirm: (() -> Unit)
    ) {
        val dialog = InfoDialog(context)
        dialog
            .setTitle(title)
            .setDescription(message ?: "Unknown error")
            .setCancelText(context.getString(R.string.cancel))
            .setConfirmText(if (isCustomDispose) confirmText else context.getString(R.string.share))
            .withOnCancel { dialog.dismiss() }
            .withOnConfirm {
                if (isCustomDispose) {
                    onConfirm()
                    dialog.dismiss()
                } else {
                    val share = Intent.createChooser(Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message ?: "Unknown error")
                    }, null)
                    context.startActivity(share)
                    dialog.dismiss()
                }
            }
            .show()
    }
}
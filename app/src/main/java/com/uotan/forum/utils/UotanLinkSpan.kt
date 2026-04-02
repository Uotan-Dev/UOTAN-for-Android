package com.uotan.forum.utils

import android.content.Intent
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import com.uotan.forum.resource.ui.details.ResourceActivity
import com.uotan.forum.utils.Utils.baseUrl

class UotanLinkSpan(private val url: String) : ClickableSpan() {

    override fun onClick(widget: View) {
        Log.e("e", url)
        val context = widget.context
        if (url.startsWith(baseUrl)) {
            val shortUrl = url.replace(baseUrl, "")
            when {
                shortUrl.startsWith("/res") -> {
                    val intent = Intent(context, ResourceActivity::class.java)
                        .putExtra("url", shortUrl)
                    context.startActivity(intent)
                }
            }
            Toast.makeText(widget.context, "isUotanLink", Toast.LENGTH_SHORT).show()
        } else {
            // 默认使用浏览器打开
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            widget.context.startActivity(intent)
        }

    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = false
    }
}
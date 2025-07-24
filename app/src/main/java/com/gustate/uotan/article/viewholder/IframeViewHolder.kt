package com.gustate.uotan.article.viewholder

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.recyclerview.widget.RecyclerView
import com.gustate.uotan.R
import com.gustate.uotan.article.ContentBlock.IframeBlock

class IframeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)  {

    private val webView = itemView.findViewById<WebView>(R.id.web_iframe)

    @SuppressLint("SetJavaScriptEnabled")
    fun bind(block: IframeBlock) {
        val url = block.url
        webView.settings.javaScriptEnabled = true // 启用JS
        webView.settings.domStorageEnabled = true // 支持HTML5本地存储
        webView.settings.mediaPlaybackRequiresUserGesture = false // 自动播放音频/视频
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webView.settings.allowContentAccess = true
        webView.settings.allowFileAccess = true
        webView.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        webView.loadUrl(if (url.startsWith("http")) url else "https:$url")
    }

}
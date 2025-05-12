package com.gustate.uotan.utils

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class CookieUtil(
    private val context: Context,
    private val url: String,
    private val cookies: Map<String, String>
) {
    private lateinit var document: Document
    private lateinit var cookieManager: CookieManager
    private lateinit var cookiesString: String

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebViewAndParse(onComplete: (Boolean) -> Unit) {
        if (::document.isInitialized) {
            onComplete(true)
            return
        }
        val webView = WebView(context.applicationContext)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            allowContentAccess = true
            allowFileAccess = true
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        }
        cookieManager = CookieManager.getInstance()
        cookieManager.apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
            cookies.forEach { (key, value) ->
                val cookieString = buildString {
                    append("$key=$value")
                }
                setCookie(url, cookieString)
            }
            flush()
        }
        webView.loadUrl(url)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                webView.evaluateJavascript("document.documentElement.outerHTML") { html ->
                    document = Jsoup.parse(html.unescapeHtml())
                    cookiesString = cookieManager.getCookie(url)
                    onComplete(true)
                }
            }
            fun String.unescapeHtml(): String {
                return this.replace("\\u003C", "<")
                    .replace("\\\"", "\"")
                    .replace("\\'", "'")
                    .replace("\\\\", "\\")
            }
        }
    }

    fun getSecurityInfo(
        onSuccess: (String, String, String, String, String, String, String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            if (!::document.isInitialized) {
                loadWebViewAndParse { success ->
                    if (success) {
                        val xfToken = document.select("input[name=_xfToken]").first()?.attr("value")
                        val attachmentHash = document.select("input[name=attachment_hash]").first()
                            ?.attr("value")
                        val attachmentHashCombined = document
                            .select("input[name=attachment_hash_combined]").first()?.attr("value")
                        val lastDate = document.select("input[name=last_date]").first()
                            ?.attr("value")
                        val lastKnownDate = document.select("input[name=last_known_date]").first()
                            ?.attr("value")
                        val loadExtra = document.select("input[name=load_extra]").first()
                            ?.attr("value")
                        when {
                            xfToken == null -> onError("_xfToken not found")
                            attachmentHash == null -> onError("attachment_hash not found")
                            attachmentHashCombined == null ->
                                onError("attachment_hash_combined not found")
                            lastDate == null -> onError("last_date not found")
                            lastKnownDate == null -> onError("last_known_date not found")
                            loadExtra == null -> onError("load_extra not found")
                            else -> onSuccess(cookiesString, xfToken, attachmentHash,
                                attachmentHashCombined, lastDate, lastKnownDate, loadExtra)
                        }
                    } else {
                        onError("page load failed")
                    }
                }
            } else {
                val xfToken = document.select("input[name=_xfToken]").first()?.attr("value")
                val attachmentHash = document.select("input[name=attachment_hash]").first()
                    ?.attr("value")
                val attachmentHashCombined = document
                    .select("input[name=attachment_hash_combined]").first()?.attr("value")
                val lastDate = document.select("input[name=last_date]").first()
                    ?.attr("value")
                val lastKnownDate = document.select("input[name=last_known_date]").first()
                    ?.attr("value")
                val loadExtra = document.select("input[name=load_extra]").first()
                    ?.attr("value")
                when {
                    xfToken == null -> onError("_xfToken not found")
                    attachmentHash == null -> onError("attachment_hash not found")
                    attachmentHashCombined == null ->
                        onError("attachment_hash_combined not found")
                    lastDate == null -> onError("last_date not found")
                    lastKnownDate == null -> onError("last_known_date not found")
                    loadExtra == null -> onError("load_extra not found")
                    else -> onSuccess(cookiesString, xfToken, attachmentHash,
                        attachmentHashCombined, lastDate, lastKnownDate, loadExtra)
                }
            }
        } catch (e: Exception) {
            onError(e.message ?: "failed to get document")
        }
    }

    fun getSecurityInfo(onSuccess: (String, String) -> Unit, onError: (String) -> Unit) {
        try {
            if (!::document.isInitialized) {
                loadWebViewAndParse { success ->
                    val xfToken = document.select("input[name=_xfToken]").first()?.attr("value")
                    if (success) {
                        when {
                            xfToken == null -> onError("_xfToken not found")
                            else -> onSuccess(cookiesString, xfToken)
                        }
                    } else {
                        onError("page load failed")
                    }
                }
            } else {
                val xfToken = document.select("input[name=_xfToken]").first()?.attr("value")
                when {
                    xfToken == null -> onError("_xfToken not found")
                    else -> onSuccess(cookiesString, xfToken)
                }
            }
        } catch (e: Exception) {
            onError(e.message ?: "failed to get document")
        }
    }
}
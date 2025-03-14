package com.gustate.uotan.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.anim.TitleAnim
import com.gustate.uotan.databinding.ActivityArticleBinding
import com.gustate.uotan.gustatex.dialog.LoadingDialog
import com.gustate.uotan.utils.parse.article.ArticleParse
import com.gustate.uotan.utils.Utils
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.openImmersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArticleActivity : AppCompatActivity() {

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val binding = ActivityArticleBinding.inflate(layoutInflater)
        setContentView(binding.rootLayout)

        // 针对部分系统的小白条沉浸
        openImmersion(window)

        loadingDialog = LoadingDialog(this)

        /*
         * 修改各个占位布局的高度
         * 以实现小白条与状态栏的沉浸
         */
        // 使用 ViewCompat 类设置一个窗口监听器（这是一个回调函数），需要传入当前页面的根布局
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLayout) { _, insets ->
            // systemBars 是一个 insets 对象
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 设置状态栏占位布局高度
            binding.statusBarView.layoutParams.height = systemBars.top
            // 修改滚动布局的边距
            binding.rootScrollView
                .setPadding(
                    systemBars.left,
                    systemBars.top + Utils.dp2Px(60, this).toInt(),
                    systemBars.right,
                    systemBars.bottom + Utils.dp2Px(70, this).toInt()
                )
            // 设置小白条占位布局高度
            binding.gestureView.layoutParams.height = systemBars.bottom
            // 创建 SimonEdgeIllusion 实例
            TitleAnim(
                binding.title,
                binding.bigTitle,
                Utils.dp2Px(60, this) + systemBars.top.toFloat(),
                systemBars.top.toFloat())
            // 返回 insets
            insets
        }

        loadData(binding, intent.getStringExtra("url")!!)

    }

    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled", "SetTextI18n")
    private fun loadData(binding: ActivityArticleBinding, url: String) {
        loadingDialog.show()
        lifecycleScope.launch {
            val content = ArticleParse.articleParse(baseContext, url)
            withContext(Dispatchers.Main) {
                if (content.authorUrl.isNotEmpty()) {
                    Glide.with(baseContext)
                        .load(BASE_URL + content.avatarUrl)
                        .into(binding.userAvatar)
                    Glide.with(baseContext)
                        .load(BASE_URL + content.avatarUrl)
                        .into(binding.bigUserAvatar)
                }
                binding.bilibiliCard.isGone = !content.isBilibili
                binding.bigUserNameText.text = content.authorName
                binding.userNameText.text = content.authorName
                binding.bigTime.text = content.time
                binding.time.text = content.time
                binding.bigIpLocation.text = getString(R.string.ip_location) + ": " + content.ipAddress
                binding.ipLocation.text = getString(R.string.ip_location) + ": " + content.ipAddress
                binding.bigTitleText.text = content.title
                binding.favouriteCount.text = content.numberOfLikes
                binding.commentCount.text = content.numberOfComments
                loadingDialog.cancel()
                binding.webView.isVerticalScrollBarEnabled = false
                binding.bilibiliWebView.settings.javaScriptEnabled = true // 启用JS
                binding.bilibiliWebView.settings.domStorageEnabled = true // 支持HTML5本地存储
                binding.bilibiliWebView.settings.mediaPlaybackRequiresUserGesture = false // 自动播放音频/视频
                binding.bilibiliWebView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                binding.bilibiliWebView.settings.allowContentAccess = true
                binding.bilibiliWebView.settings.allowFileAccess = true
                binding.bilibiliWebView.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
                // 需要添加的配置：
                binding.bilibiliWebView.webChromeClient = object : WebChromeClient() {
                    // 处理全屏和权限请求
                    override fun onPermissionRequest(request: PermissionRequest?) {
                        request?.grant(request.resources) // 必须授予权限才能播放视频
                    }

                    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                        // 实现全屏逻辑（参考原回答的方案）
                    }

                    override fun onHideCustomView() {
                        // 退出全屏逻辑
                    }
                }
                if (content.isBilibili) binding.bilibiliWebView.loadUrl("https:" + content.bilibiliVideoLink)
                binding.webView.apply {
                    // 允许 JavaScript
                    settings.apply {
                        javaScriptEnabled = true // 启用 JavaScript
                        domStorageEnabled = true // 启用 DOM 存储
                        loadWithOverviewMode = true // 适应屏幕
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW // 允许混合内容
                    }

                    // 处理链接点击
                    webViewClient = object : WebViewClient() {
                        @Deprecated("Deprecated in Java", ReplaceWith("false"))
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            url: String?
                        ): Boolean {
                            // 自定义链接处理逻辑
                            return false // 在 WebView 内部打开
                        }

                        override fun onPageFinished(view: WebView, url: String?) {
                            super.onPageFinished(view, url)
                            applyThemeColors(binding.webView)
                            // 延迟确保内容加载完成
                            binding.webView.postDelayed({
                                val params = binding.webView.layoutParams
                                params.height = (binding.webView.contentHeight * resources.displayMetrics.density).toInt()
                                binding.webView.layoutParams = params
                            }, 500)
                        }

                    }

                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        // 加载HTML内容
                        loadDataWithBaseURL(
                            null,
                            content.article.replace(
                                Regex("""<span data-guineapigclub-mediaembed="bilibili">.*?</span>""",
                                    RegexOption.DOT_MATCHES_ALL),
                        ""
                            ), // 你的HTML字符串
                            "text/html",
                            "UTF-8",
                            null
                        )
                    }
                }
                binding.webView.setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            // 禁用父容器拦截事件
                            v.parent.requestDisallowInterceptTouchEvent(true)
                        }
                        MotionEvent.ACTION_UP -> {
                            v.parent.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                    false
                }
            }
        }
    }

    private fun applyThemeColors(webView: WebView) {
        val context = webView.context

        val colors = mapOf(
            "bg" to ContextCompat.getColor(context, R.color.background_2),
            "text" to ContextCompat.getColor(context, R.color.label_primary),
            "accent" to ContextCompat.getColor(context, R.color.red),
            "warning" to ContextCompat.getColor(context, R.color.red)
        ).mapValues { "#${Integer.toHexString(it.value).substring(2)}" }

        val css = """
        :root {
            --sys-padding-left: 0px;
            --sys-padding-top: ${ webView.paddingTop / 3 }px;
            --sys-padding-right: 0px;
            --sys-padding-bottom: ${ webView.paddingBottom / 3 }px;
            --bg: ${colors["bg"]};
            --text: ${colors["text"]};
            --accent: ${colors["accent"]};
            --warning: ${colors["warning"]};
        }
        body {
            padding: 
                var(--sys-padding-top) 
                var(--sys-padding-right) 
                var(--sys-padding-bottom) 
                var(--sys-padding-left) !important;
            background: var(--bg) !important;
            color: var(--text) !important;
            overflow-x: hidden !important;  // 新增禁止横向滚动
        }
        img, iframe, video {
            max-width: 100% !important;
            height: auto !important;
            display: block;
            border-radius: 12px !important;
        }
        .bbImageWrapper { 
            max-width: 100% !important; 
            border-radius: 18px !important; 
        }
        .bbImage { 
            max-width: 100% !important; 
            height: auto !important; 
            border-radius: 12px !important; 
        }
        .contentRow { 
            margin:10px 0; 
            border:1px solid #eee; 
            border-radius:12px; 
        }
        a { 
            color: var(--accent) !important; 
            word-break: break-word !important;  // 长链接换行
            overflow-wrap: anywhere !important;
        }
        span[style*="#b22c46"] { color: var(--warning) !important; }
    """.trimIndent()

        webView.evaluateJavascript("""
        var style = document.getElementById('dynamic-theme') || document.createElement('style');
        style.id = 'dynamic-theme';
        style.innerHTML = `$css`;
        document.head.appendChild(style);
    """.trimIndent(), null)
    }
}
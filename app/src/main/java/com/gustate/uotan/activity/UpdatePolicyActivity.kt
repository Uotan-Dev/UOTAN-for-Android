package com.gustate.uotan.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.gustate.uotan.R
import com.gustate.uotan.databinding.ActivityUpdatePolicyBinding
import com.gustate.uotan.gustatex.dialog.LoadingDialog
import com.gustate.uotan.utils.parse.user.PolicyParse
import com.gustate.uotan.utils.Utils
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.openImmersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 隐私政策更新页面 (Activity)
 * JiaGuZhuangZhi Miles
 * Gustate 02/23/2025
 * I Love Jiang’Xun
 */

class UpdatePolicyActivity : AppCompatActivity() {

    /** 全类变量 **/
    // 初始化视图绑定
    private lateinit var binding: ActivityUpdatePolicyBinding

    /**
     * 视图被创建
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** 窗体设置 **/
        // 启用边到边设计
        enableEdgeToEdge()
        // 针对部分系统的系统栏沉浸
        openImmersion(window)
        // 实例化 binding
        binding = ActivityUpdatePolicyBinding.inflate(layoutInflater)
        // 绑定视图
        setContentView(binding.main)

        /** 常量设置 **/
        // 隐私政策 url
        val url = intent.getStringExtra("url")!!
        // 配置 Cookie 管理器
        val cookieManager = CookieManager.getInstance()
        // 实例化 LoadingDialog
        val loadingDialog = LoadingDialog(this)

        /** 获取系统栏高度并同步到占位布局 **/
        // 使用 ViewCompat 的回调函数
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            // 获取系统栏高度 (包含 top, bottom, left 和 right)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusBarView
                .updateLayoutParams<ViewGroup.LayoutParams> {
                    height = systemBars.top
                }
            // 同步导航栏占位布局高度
            binding.gestureView
                .updateLayoutParams<ViewGroup.LayoutParams> {
                    height = systemBars.bottom
                }
            // 同步系统栏高度到 webView 边距
            binding.webView.setPadding(
                systemBars.left,
                systemBars.top + Utils.dp2Px(60, this).toInt(),
                systemBars.right,
                systemBars.bottom + binding.bottomBar.height
            )
            // 立即触发 CSS 更新
            applyThemeColors(binding.webView)
            // 返回 insets
            insets
        }

        /** WebView 设置 **/
        // 查看隐私协议的 WebView
        binding.webView.apply {
            // 允许JavaScript
            settings.apply {
                // 启用 JavaScript 支持
                javaScriptEnabled = true
                // 把页面内容缩放以适应屏幕宽度
                loadWithOverviewMode = true
            }
            // 处理链接点击（可选）
            webViewClient = object : WebViewClient() {
                @Deprecated("Deprecated in Java", ReplaceWith("false"))
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    // 自定义链接处理逻辑
                    return false // 在WebView内部打开
                }
                override fun onPageFinished(view: WebView, url: String?) {
                    super.onPageFinished(view, url)
                    applyThemeColors(binding.webView)
                }
            }
            // 开启协程
            lifecycleScope.launch {
                // 获取隐私政策更新的信息
                val policyUpdateInfo = PolicyParse.readPolicy()
                // 切回主线程
                withContext(Dispatchers.Main) {
                    // 设置更新时间
                    binding.lastUpdatedTime.text = policyUpdateInfo.updateTime
                    // 加载 HTML 内容
                    loadDataWithBaseURL(
                        null,
                        policyUpdateInfo.content,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            }
        }
        // 模拟同意隐私协议的 WebView
        cookieManager.setAcceptCookie(true) // 允许接受Cookie
        // 允许第三方 Cookies
        cookieManager.setAcceptThirdPartyCookies(binding.web, true)
        // 设置自定义Cookies
        setCookiesForDomain(url, Cookies)
        // 配置基础设置
        with(binding.web.settings) {
            // 启用 JavaScript 支持
            javaScriptEnabled = true
            // 启用本地存储
            domStorageEnabled = true
            // 使用默认缓存策略
            cacheMode = WebSettings.LOAD_DEFAULT
        }
        // 加载隐私政策页面
        binding.web.loadUrl(url)

        /** 设置监听 **/
        // 为返回按钮设置点击监听
        binding.back.setOnClickListener {
            // 结束当前 Activity
            finish()
        }
        // 为同意按钮设置点击监听
        binding.agreeButton.setOnClickListener {
            // 显示加载 Dialog
            loadingDialog.show()
            // 执行自动同意操作的 JavaScript 代码
            binding.web.evaluateJavascript("""
                (function() {
                    try {
                        // 通过属性定位
                        const checkbox = document.querySelector('input[type="checkbox"][name="accept"]');
                        if (!checkbox) throw new Error('未找到目标复选框');                        
                        // 定位目标按钮
                        const buttonContainer = document.querySelector('div.formSubmitRow-controls');
                        if (!buttonContainer) throw new Error('找不到按钮容器');
                        // 状态验证
                        if (checkbox.disabled) throw new Error('复选框处于禁用状态');
                        if (checkbox.offsetParent === null) throw new Error('复选框不可见');

                        // 执行勾选操作
                        if (!checkbox.checked) {
                            // 模拟真实点击
                            checkbox.click();
                        }

                        // 精确匹配按钮
                        const targetButtons = Array.from(buttonContainer.querySelectorAll('button[type="submit"]'));
                        const acceptButton = targetButtons.find(btn => {
                            const textSpan = btn.querySelector('span.button-text');
                            return textSpan && textSpan.textContent.trim() === '接受';
                        });

                        if (!acceptButton) {
                            console.error('候选按钮:', targetButtons.map(b => ({
                                classes: b.className,
                                text: b.innerText,
                                html: b.outerHTML
                        })));
                        throw new Error('未找到包含"接受"文本的按钮');
                    }

                    // 点击前验证
                    const verifyClickable = (btn) => {
                        if (btn.disabled) throw new Error('按钮处于禁用状态');
                        if (btn.offsetWidth === 0) throw new Error('按钮不可见');
                        return true;
                    };
                    verifyClickable(acceptButton);

                    // 模拟真实点击
                    acceptButton.style.transition = 'all 0.3s';
                    acceptButton.style.filter = 'brightness(0.9)';
                
                    // 触发完整点击事件序列
                    const eventSequence = ['mousedown', 'mouseup', 'click', 'mouseout'];
                    eventSequence.forEach(eventType => {
                        acceptButton.dispatchEvent(new MouseEvent(eventType, {
                            bubbles: true,
                            cancelable: true,
                            view: window
                        }));
                    });

                    // 重置样式
                    setTimeout(() => {
                        acceptButton.style.filter = '';
                    }, 300);

                    // 结果验证
                    const successCheck = () => {
                        const successElement = document.querySelector('.privacy-accept-success');
                        return successElement ? true : false;
                    };

                    return {
                        status: 'success',
                        message: '已成功触发接受操作',
                        elementInfo: {
                            id: acceptButton.id,
                            classes: acceptButton.className,
                            text: acceptButton.innerText
                        },
                        verified: successCheck()
                    };
                } catch (error) {
                    return {
                        status: 'error',
                        message: error.message,
                        stack: error.stack
                    };
                }
            })()
        """) { result ->
                // 同意成功
                if (result.contains("成功")) {
                    // 弹出同意成功的提示
                    Toast.makeText(
                        this,
                        R.string.thank_choice,
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this, MainActivity::class.java))
                }
                // 同意失败
                else {
                    // 弹出同意成功的提示
                    Toast.makeText(
                        this,
                        R.string.please_try_again_later,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                // 隐藏 Dialog
                loadingDialog.cancel()
            }
        }
    }

    /**
     * 为指定域名设置多个Cookies
     * @param domain 目标域名（如：www.uotan.cn）
     * @param cookiesMap Cookie键值对集合
     */
    private fun setCookiesForDomain(domain: String, cookiesMap: Map<String, String>) {
        // 设置 CookieManager
        val cookieManager = CookieManager.getInstance()
        // 构建 Cookie
        cookiesMap.forEach { (key, value) ->
            // 构建符合规范的 Cookie 字符串
            val cookieString = buildString {
                // 基础键值对
                append("$key=$value")
            }
            // 设置 Cookie
            cookieManager.setCookie(domain, cookieString)
        }
        // 同步 Cookies
        cookieManager.flush()
    }

    /**
     * 自定义 WebView 样式
     */
    private fun applyThemeColors(webView: WebView) {
        val context = webView.context

        val colors = mapOf(
            "bg" to ContextCompat.getColor(context, R.color.background_1),
            "text" to ContextCompat.getColor(context, R.color.label_primary),
            "accent" to ContextCompat.getColor(context, R.color.red),
            "warning" to ContextCompat.getColor(context, R.color.red)
        ).mapValues { "#${Integer.toHexString(it.value).substring(2)}" }

        val css = """
        :root {
            --sys-padding-left: ${ Utils.dp2Px(12, context) / 3 }px;
            --sys-padding-top: ${ webView.paddingTop / 3 }px;
            --sys-padding-right: ${ Utils.dp2Px(12, context) / 3 }px;
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
            margin: 8px 0;
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
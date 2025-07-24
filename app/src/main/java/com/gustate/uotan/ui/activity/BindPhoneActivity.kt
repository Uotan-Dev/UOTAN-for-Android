package com.gustate.uotan.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.databinding.ActivityBindPhoneBinding
import com.gustate.uotan.gustatex.dialog.LoadingDialog
import com.gustate.uotan.main.ui.MainActivity
import com.gustate.uotan.utils.Utils.Companion.baseUrl
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import com.gustate.uotan.utils.Utils.Companion.openImmersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

/**
 * 绑定手机号页面 (Activity)
 * JiaGuZhuangZhi Miles
 * Gustate 03/04/2025
 * I Love Jiang’Xun
 */

class BindPhoneActivity : BaseActivity() {

    /** 全类变量 **/
    // 初始化视图绑定
    private lateinit var binding: ActivityBindPhoneBinding
    // 加载
    private lateinit var loadingDialog: LoadingDialog
    // 倒计时
    private lateinit var bindingTimer: CountDownTimer
    // 发送间隔
    private lateinit var sendTimer: CountDownTimer
    // 是否在 60 秒内发送验证码
    var isBinding = false

    /**
     * 视图被创建
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** 窗体设置 **/
        // 启用边到边设计
        enableEdgeToEdge()
        // 针对部分系统的系统栏沉浸
        openImmersion(window)
        // 实例化 binding
        binding = ActivityBindPhoneBinding.inflate(layoutInflater)
        // 绑定视图
        setContentView(binding.main)

        /** 常量设置 **/
        // 绑定手机 url
        val url = baseUrl + "account/sms-verification"
        // 配置 Cookie 管理器
        val cookieManager = CookieManager.getInstance()
        // 实例化 LoadingDialog
        loadingDialog = LoadingDialog(this)
        // 自定义编辑框监听
        val sendTextWatcher = object: TextWatcher {
            // 文本变化前
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 无操作
            }
            // 文本变化时
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 获取手机号编辑框的内容
                val phoneNumber = binding.phoneEdit.text.toString()
                // 检查是否为 +86 手机号
                if (validatePhoneNumber(phoneNumber)) {
                    // 设置发送按钮透明度为 1.0 (Float)
                    binding.sendButton.alpha = 1.0f
                }
                // 不是 +86 手机号
                else {
                    // 设置发送按钮透明度为 0.15 (Float)
                    binding.sendButton.alpha = 0.15f
                }
            }
            // 文本变化后
            override fun afterTextChanged(s: Editable?) {
                // 无操作
            }
        }
        val bindTextWatcher = object: TextWatcher {
            // 文本变化前
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 无操作
            }
            // 文本变化时
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 获取手机号编辑框的内容
                val phoneNumber = binding.phoneEdit.text.toString()
                // 获取验证码编辑框的内容
                val code = binding.codeEdit.text.toString()
                // 两个编辑框都不为空
                if (validatePhoneNumber(phoneNumber) && code.length == 6) {
                    // 设置绑定按钮透明度为 1.0 (Float)
                    binding.bindingButton.alpha = 1.0f
                }
                // 两个编辑框其一为空
                else {
                    // 设绑定按钮透明度为 0.15 (Float)
                    binding.bindingButton.alpha = 0.15f
                }
            }
            // 文本变化后
            override fun afterTextChanged(s: Editable?) {
                // 无操作
            }
        }

        /** 获取系统栏高度并同步到占位布局 **/
        // 使用 ViewCompat 的回调函数
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            // 获取系统栏高度 (包含 top, bottom, left 和 right)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 同步状态栏高度
            binding.statusBarView
                .updateLayoutParams<ViewGroup.LayoutParams> {
                    height = systemBars.top
                }
            // 同步导航栏占位布局高度
            binding.gestureView
                .updateLayoutParams<ViewGroup.LayoutParams> {
                    height = systemBars.bottom
                }
            // 返回 insets
            insets
        }

        /** WebView 设置 **/
        // 配置 WebView
        configureWebView()
        // 允许接受Cookie
        cookieManager.setAcceptCookie(true)
        // 允许第三方 Cookies
        cookieManager.setAcceptThirdPartyCookies(binding.webView, true)
        // 设置自定义 Cookies
        setCookiesForDomain(Cookies)
        // 加载验证页面
        binding.webView.loadUrl(url)

        /** 设置监听 **/
        // 为手机号码编辑框添加监听 (textWatcher)
        binding.phoneEdit.addTextChangedListener(sendTextWatcher)
        // 为验证码编辑框添加监听 (textWatcher)
        binding.codeEdit.addTextChangedListener(bindTextWatcher)
        // 为发送验证码按钮设置点击监听
        binding.sendButton.setOnClickListener {
            // 当发送按钮透明时
            if (!isBinding) {
                if (binding.sendButton.alpha != 1.0f) {
                    if (bindingTimer.equals(null))
                    // 提示输入正确手机号
                        Toast.makeText(
                            this,
                            R.string.enter_valid_phone_number,
                            Toast.LENGTH_SHORT
                        ).show()
                }
                // 手机号输入正确时
                else {
                    // 取 phoneEdit 内容
                    val phone = binding.phoneEdit.text.toString()
                    // 注入 JavaScript 填充手机号并点击发送按钮
                    binding.webView.evaluateJavascript(
                        """
                    var phoneInput = document.querySelector('input[name="phone"]');
                    if (phoneInput) {
                        phoneInput.value = '$phone';
                        phoneInput.dispatchEvent(new Event('input'));
                    
                        var sendBtn = document.querySelector('input[type="button"][value="获取短信验证码"]');
                        sendBtn.click();
                    }
                    """, null
                    )
                    isBinding = true
                    sendTimer = object : CountDownTimer(60000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            // 更新剩余时间显示
                            val sec = getString(R.string.send) + " (${millisUntilFinished / 1000}s)"
                            binding.sendButton.alpha = 0.15f
                            binding.send.text = sec
                        }
                        override fun onFinish() {
                            binding.send.text = getString(R.string.send)
                            binding.sendButton.alpha = 1.0f
                            isBinding = false
                        }
                    }.start()
                }
            } else {
                Toast.makeText(this, "你手速太快惹", Toast.LENGTH_SHORT).show()
            }
        }
        // 为绑定按钮设置点击监听
        binding.bindingButton.setOnClickListener {
            // 当绑定按钮透明时
            if (binding.bindingButton.alpha != 1.0f) {
                // 提示输入正确验证码
                Toast.makeText(
                    this,
                    R.string.code_format_doesnt_match,
                    Toast.LENGTH_SHORT
                ).show()
            }
            // 当绑定按钮不透明时
            else {
                // 显示加载
                loadingDialog.show()
                // 取 codeEdit 内容
                val code = binding.codeEdit.text.toString()
                // 注入JavaScript填充验证码并提交表单
                binding.webView.evaluateJavascript(
                    """
                    var codeInputs = document.querySelectorAll('input[type="text"]');
                    var codeInput = Array.from(codeInputs).find(i => i.name === 'code');
                    if (codeInput) {
                        codeInput.value = '$code';
                        codeInput.dispatchEvent(new Event('input'));
                        var submitBtn = document.querySelector('button[class="button--primary button button--icon button--icon--save"]');
                        submitBtn.click();
                    }
                    """, null
                )
                bindingTimer = object : CountDownTimer(2000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        // 更新剩余时间显示（可选）
                        // val sec = getString(R.string.skip) + " (${millisUntilFinished / 1000}s)"
                        // skip.text = sec
                    }
                    override fun onFinish() {
                        // 检测是否跳转到成功页面
                        lifecycleScope.launch {
                            if (isSucceed()) {
                                withContext(Dispatchers.Main) {
                                    // 弹出成功的提示
                                    Toast.makeText(
                                        this@BindPhoneActivity,
                                        R.string.thank_choice,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(Intent(this@BindPhoneActivity, MainActivity::class.java))
                                    loadingDialog.cancel()
                                    finish()
                                }
                            }
                        }
                    }
                }.start()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // 页面加载完成时注入监听脚本
                injectChangeListeners()
            }
        }
    }

    private fun injectChangeListeners() {
        binding.webView.evaluateJavascript("""
            // 监听手机号输入框变化
            var phoneInput = document.querySelector('input[name="phone"]');
            if (phoneInput) {
                phoneInput.addEventListener('input', function(e) {
                    Android.updatePhoneNumber(e.target.value);
                });
            }

            // 监听验证码输入框变化
            var codeInputs = document.querySelectorAll('input[type="text"]');
            var codeInput = Array.from(codeInputs).find(i => i.name === 'code');
            if (codeInput) {
                codeInput.addEventListener('input', function(e) {
                    Android.updateVerificationCode(e.target.value);
                });
            }
        """, null)
    }

    @JavascriptInterface
    fun updatePhoneNumber(number: String) {
        runOnUiThread {
            if (!binding.phoneEdit.isFocused) {
                binding.phoneEdit.setText(number.replace("+86", ""))
            }
        }
    }

    @JavascriptInterface
    fun updateVerificationCode(code: String) {
        runOnUiThread {
            if (!binding.codeEdit.isFocused) {
                binding.codeEdit.setText(code)
            }
        }
    }

    private fun validatePhoneNumber(phone: String): Boolean {
        return phone.matches(Regex("^\\+86[1-9]\\d{9}\$")) || phone.matches(Regex("^1[3-9]\\d{9}\$"))
    }

    private suspend fun isSucceed(): Boolean = withContext(Dispatchers.IO) {
        // 解析网页, document 返回的就是网页 Document 对象
        val response = Jsoup.connect(baseUrl)
            .userAgent(USER_AGENT)
            .timeout(TIMEOUT_MS)
            .cookies(Cookies)
            .execute()
        val document = response
            .parse()
        val noticeTitle = document
            .getElementsByClass("notice-content")
            .first()
            ?.ownText()
            ?: ""
        val isSmsVerify = noticeTitle == "您需要验证手机号才能使用全部功能（仅限中国大陆）"
        return@withContext !isSmsVerify
    }

    /**
     * 为指定域名设置多个Cookies
     * @param cookiesMap Cookie键值对集合
     */
    private fun setCookiesForDomain(cookiesMap: Map<String, String>) {
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
            cookieManager.setCookie(baseUrl + "account/sms-verification", cookieString)
        }
        // 同步 Cookies
        cookieManager.flush()
    }
}
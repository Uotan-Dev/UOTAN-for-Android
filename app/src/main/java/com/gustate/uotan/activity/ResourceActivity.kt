package com.gustate.uotan.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.animation.ValueAnimator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.databinding.ActivityResourceBinding
import com.gustate.uotan.gustatex.dialog.LoadingDialog
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.getThemeColor
import com.gustate.uotan.utils.Utils.Companion.htmlToSpan
import com.gustate.uotan.utils.parse.article.ArticleParse.Companion.addReaction
import com.gustate.uotan.utils.parse.resource.ResourceArticleParse.Companion.fetchResourceArticle
import com.gustate.uotan.utils.parse.user.UserParse
import com.gustate.uotan.utils.parse.user.UserParse.Companion.follow
import com.kongzue.dialogx.dialogs.BottomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import kotlin.math.roundToInt

class ResourceActivity : BaseActivity() {

    private lateinit var binding: ActivityResourceBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var cookieManager: CookieManager
    private var colorOnFilledButton = 0
    private var colorOnOutlineButton = 0
    private var title = ""
    private var url = ""
    private var xfToken = ""
    private var cookieString = ""
    private var authorUrl = ""
    private var isFollowing = false
    private var isFollowAuthor = false
    private var reactUrl = ""
    private var isReacting = false
    private var isReact = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResourceBinding.inflate(layoutInflater)
        setContentView(binding.rootLayout)
        val loadingAnimator = ObjectAnimator.ofFloat(
            binding.btnReact,
            "rotation",
            0f,
            360f
        ).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.btnReact.rotation = 0f
                }
                override fun onAnimationCancel(animation: Animator) {
                    binding.btnReact.rotation = 0f
                }
            })
        }
        loadingDialog = LoadingDialog(this)
        url = intent
            .getStringExtra("url")
            ?: ""

        colorOnFilledButton = getThemeColor(this, R.attr.colorOnFilledButton)
        colorOnOutlineButton = getThemeColor(this, R.attr.colorBottomNavigationBarOnBackgroundSecondary)

        // 配置 Cookie 管理器
        cookieManager = CookieManager.getInstance()
        // 配置 WebView
        configureWebView()
        // 允许接受Cookie
        cookieManager.setAcceptCookie(true)
        // 允许第三方 Cookies
        cookieManager.setAcceptThirdPartyCookies(binding.invisibleWebView, true)
        setCookiesForDomain(BASE_URL + url, Cookies)
        binding.invisibleWebView.loadUrl(BASE_URL + url)
        binding.invisibleWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                binding.invisibleWebView.evaluateJavascript("document.documentElement.outerHTML") { html ->
                    val unescapedHtml = html.unescapeHtml()
                    val document = Jsoup.parse(unescapedHtml)
                    cookieString = cookieManager.getCookie(url)
                    xfToken = document
                        .select("input[name=_xfToken]")
                        .first()
                        ?.attr("value") ?: throw Exception("Xf token not found")
                }
            }
            fun String.unescapeHtml(): String {
                return this.replace("\\u003C", "<")
                    .replace("\\\"", "\"")
                    .replace("\\'", "'")
                    .replace("\\\\", "\\")
            }
        }

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
                    (systemBars.top + 60f.dpToPx(this)).roundToInt(),
                    systemBars.right,
                    (systemBars.top + 70f.dpToPx(this)).roundToInt()
                )
            // 设置小白条占位布局高度
            binding.gestureView.layoutParams.height = systemBars.bottom
            // 返回 insets
            insets
        }

        binding.follow.setOnClickListener {
            if (isFollowing) {
                Toast.makeText(
                    this,
                    "正在操作中，请稍后",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            isFollowing = true
            lifecycleScope.launch {
                val follow = follow(
                    authorUrl,
                    cookieString,
                    xfToken
                )
                if (follow) {
                    if (isFollowAuthor) {
                        isFollowAuthor = false
                        binding.follow.background = AppCompatResources.getDrawable(
                            this@ResourceActivity, R.drawable.gustatex_button_filled
                        )
                        binding.follow.text = getString(R.string.follow)
                        binding.follow.setTextColor(colorOnFilledButton)
                    } else {
                        isFollowAuthor = true
                        binding.follow.background = AppCompatResources.getDrawable(
                            this@ResourceActivity, R.drawable.gustatex_button_outline
                        )
                        binding.follow.text = getString(R.string.following)
                        binding.follow.setTextColor(colorOnOutlineButton)
                    }
                } else {
                    Toast.makeText(
                        this@ResourceActivity,
                        "操作失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                isFollowing = false
            }
        }

        binding.btnReact.setOnClickListener {
            if (reactUrl.isEmpty()) {
                Toast.makeText(
                    this,
                    "自己的文章不可点赞",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (isReacting) {
                Toast.makeText(
                    this,
                    "正在点赞中，请稍后",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            isReacting = true
            val oldImg  = binding.btnReact.drawable
            binding.btnReact.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_loading))
            loadingAnimator.start()
            lifecycleScope.launch {
                val changeReact = addReaction(
                    reactUrl,
                    cookieString,
                    xfToken
                )
                if (changeReact) {
                    if (isReact) {
                        isReact = false
                        binding.btnReact.setImageDrawable(AppCompatResources.getDrawable(this@ResourceActivity, R.drawable.ic_favourite))
                        val reactCount = binding.favouriteCount.text.toString().toInt() - 1
                        binding.favouriteCount.text = reactCount.toString()
                    } else {
                        isReact = true
                        binding.btnReact.setImageDrawable(AppCompatResources.getDrawable(this@ResourceActivity, R.drawable.ic_is_react))
                        val reactCount = binding.favouriteCount.text.toString().toInt() + 1
                        binding.favouriteCount.text = reactCount.toString()
                    }
                } else {
                    Toast.makeText(
                        this@ResourceActivity,
                        "操作失败",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnReact.setImageDrawable(oldImg)
                }
                isReacting = false
                loadingAnimator.cancel()
            }
        }

        binding.btnShare.setOnClickListener {
            val share = Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, BASE_URL.replace(".cn/", ".cn") + url)
                putExtra(Intent.EXTRA_TITLE, title)
            }, null)
            startActivity(share)
        }
        binding.btnMore.setOnClickListener {
            BottomDialog.show(object : OnBindView<BottomDialog?>(R.layout.dialogx_article_more) {
                override fun onBind(dialog: BottomDialog?, v: View?) {
                    val btnClose = v?.findViewById<View>(R.id.close)
                    btnClose?.setOnClickListener {
                        dialog?.dismiss()
                    }
                    val btnCopyLink = v?.findViewById<View>(R.id.btnCopyLink)
                    btnCopyLink?.setOnClickListener {
                        // 获取 ClipboardManager 实例
                        val clipboard = this@ResourceActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        // 创建 ClipData 对象（带标签和内容）
                        val clipData = ClipData.newPlainText(title, BASE_URL.replace(".cn/", ".cn") + url)
                        // 设置到系统剪贴板
                        clipboard.setPrimaryClip(clipData)
                        Toast.makeText(
                            this@ResourceActivity,
                            R.string.link_copied_clipboard,
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog?.dismiss()
                    }
                    val btnReport = v?.findViewById<View>(R.id.btnReport)
                    val tvReport = v?.findViewById<View>(R.id.tvReport)
                    btnReport?.isGone = true
                    tvReport?.isGone = true
                    val btnEdit = v?.findViewById<View>(R.id.btnEdit)
                    val tvEdit = v?.findViewById<View>(R.id.tvEdit)
                    btnEdit?.isGone = true
                    tvEdit?.isGone = true
                    val btnDelete = v?.findViewById<View>(R.id.btnDelete)
                    val tvDelete = v?.findViewById<View>(R.id.tvDelete)
                    btnDelete?.isGone = true
                    tvDelete?.isGone = true
                    val btnIp = v?.findViewById<View>(R.id.btnIp)
                    val tvIp = v?.findViewById<View>(R.id.tvIp)
                    btnIp?.isGone = true
                    tvIp?.isGone = true
                    val btnChangeAuthor = v?.findViewById<View>(R.id.btnChangeAuthor)
                    val tvChangeAuthor = v?.findViewById<View>(R.id.tvChangeAuthor)
                    btnChangeAuthor?.isGone = true
                    tvChangeAuthor?.isGone = true
                }
            })
        }

        lifecycleScope.launch {
            val content = fetchResourceArticle(url)
            isFollowAuthor = UserParse.isFollow(authorUrl)
            withContext(Dispatchers.Main) {
                if (content.authorAvatar != "") {
                    Glide.with(this@ResourceActivity)
                        .load(BASE_URL + content.authorAvatar)
                        .into(binding.userAvatar)
                }
                binding.userNameText.text = content.author
                binding.time.text = content.latestPost
                if (content.cover != "") {
                    Glide.with(this@ResourceActivity)
                        .load(BASE_URL + content.cover)
                        .into(binding.cover)
                }
                binding.bigTitle.text = content.title
                if (content.device != "") {
                    binding.deviceText.isGone = false
                    binding.device.isGone = false
                    binding.device.text = content.device
                } else {
                    binding.deviceText.isGone = true
                    binding.device.isGone = true
                }
                if (content.downloadType != "") {
                    binding.channelText.isGone = false
                    binding.channel.isGone = false
                    binding.channel.text = content.downloadType
                } else {
                    binding.channelText.isGone = true
                    binding.channel.isGone = true
                }
                if (content.size != "") {
                    binding.sizeText.isGone = false
                    binding.size.isGone = false
                    binding.size.text = content.size
                } else {
                    binding.sizeText.isGone = true
                    binding.size.isGone = true
                }
                if (content.password != "") {
                    binding.codeText.isGone = false
                    binding.code.isGone = false
                    binding.code.text = content.password
                } else {
                    binding.codeText.isGone = true
                    binding.code.isGone = true
                }
                if (content.author != "") {
                    binding.authorText.isGone = false
                    binding.author.isGone = false
                    binding.author.text = content.author
                } else {
                    binding.authorText.isGone = true
                    binding.author.isGone = true
                }
                if (content.downloadCount != "") {
                    binding.downloadText.isGone = false
                    binding.download.isGone = false
                    binding.download.text = content.downloadCount
                } else {
                    binding.downloadText.isGone = true
                    binding.download.isGone = true
                }
                if (content.viewCount != "") {
                    binding.viewText.isGone = false
                    binding.view.isGone = false
                    binding.view.text = content.viewCount
                } else {
                    binding.viewText.isGone = true
                    binding.view.isGone = true
                }
                if (content.firstPost != "") {
                    binding.firstText.isGone = false
                    binding.first.isGone = false
                    binding.first.text = content.firstPost
                } else {
                    binding.firstText.isGone = true
                    binding.first.isGone = true
                }
                if (content.latestPost != "") {
                    binding.lastText.isGone = false
                    binding.last.isGone = false
                    binding.last.text = content.latestPost
                } else {
                    binding.lastText.isGone = true
                    binding.last.isGone = true
                }
                htmlToSpan(binding.tvContent, content.content)
                binding.favouriteCount.text = content.numberOfLikes
                isReact = content.isReact
                reactUrl = content.reactUrl
                if (content.isReact) {
                    binding.btnReact.setImageDrawable(
                        AppCompatResources.getDrawable(this@ResourceActivity, R.drawable.ic_is_react)
                    )
                } else {
                    binding.btnReact.setImageDrawable(
                        AppCompatResources.getDrawable(this@ResourceActivity, R.drawable.ic_favourite)
                    )
                }
                if (content.isBookMark) {
                    binding.collect.setImageDrawable(
                        AppCompatResources.getDrawable(this@ResourceActivity, R.drawable.ic_option_collect)
                    )
                } else {
                    binding.collect.setImageDrawable(
                        AppCompatResources.getDrawable(this@ResourceActivity, R.drawable.ic_collect)
                    )
                }
                authorUrl = content.authorUrl
                if (isFollowAuthor) {
                    binding.follow.background = AppCompatResources.getDrawable(this@ResourceActivity, R.drawable.gustatex_button_outline)
                    binding.follow.text = getString(R.string.following)
                    binding.follow.setTextColor(colorOnOutlineButton)
                } else {
                    binding.follow.background = AppCompatResources.getDrawable(this@ResourceActivity, R.drawable.gustatex_button_filled)
                    binding.follow.text = getString(R.string.follow)
                    binding.follow.setTextColor(colorOnFilledButton)
                }
            }
        }
    }
    private fun configureWebView() {
        binding.invisibleWebView.settings.apply {
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
        }
    }
    private fun setCookiesForDomain(url: String, cookiesMap: Map<String, String>) {
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
            cookieManager.setCookie(url, cookieString)
        }
        // 同步 Cookies
        cookieManager.flush()
    }
}
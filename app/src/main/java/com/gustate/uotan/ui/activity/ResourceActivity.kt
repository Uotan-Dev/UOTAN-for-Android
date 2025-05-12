package com.gustate.uotan.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.animation.ValueAnimator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.databinding.ActivityResourceBinding
import com.gustate.uotan.databinding.DialogxAddResourceReplyBinding
import com.gustate.uotan.databinding.DialogxArticleMoreBinding
import com.gustate.uotan.databinding.DialogxDonationDownloadBinding
import com.gustate.uotan.databinding.DialogxSelectDownloadTypeBinding
import com.gustate.uotan.ui.activity.resource.adapter.NewResourceTypeAdapter
import com.gustate.uotan.ui.activity.resource.adapter.ResourceCommentAdapter
import com.gustate.uotan.utils.CookieUtil
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.errorDialog
import com.gustate.uotan.utils.Utils.Companion.getThemeColor
import com.gustate.uotan.utils.Utils.Companion.htmlToSpan
import com.gustate.uotan.utils.Utils.Companion.openUrlInBrowser
import com.gustate.uotan.utils.parse.article.ArticleParse.Companion.addReaction
import com.gustate.uotan.utils.parse.resource.ResourceArticleParse.Companion.buyResource
import com.gustate.uotan.utils.parse.resource.ResourceArticleParse.Companion.fetchResourceArticle
import com.gustate.uotan.utils.parse.resource.ResourceArticleParse.Companion.getPurchaseData
import com.gustate.uotan.utils.parse.resource.ResourceArticleParse.Companion.getResourceReport
import com.gustate.uotan.utils.parse.resource.ResourceArticleParse.Companion.reportResource
import com.gustate.uotan.utils.parse.resource.ResourceData.PurchaseData
import com.gustate.uotan.utils.parse.resource.ResourceData.ResReportData
import com.gustate.uotan.utils.parse.resource.ResourceData.ResourceArticle
import com.gustate.uotan.utils.parse.resource.ResourceData.ResourceType
import com.gustate.uotan.utils.parse.user.UserParse.Companion.follow
import com.gustate.uotan.utils.parse.user.UserParse.Companion.isFollow
import com.kongzue.dialogx.dialogs.BottomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class ResourceActivity : BaseActivity() {

    /* 延迟启动 */
    // 视图绑定
    private lateinit var binding: ActivityResourceBinding

    private lateinit var adapter: ResourceCommentAdapter

    /* 需要刷新的变量 */
    // 文章标题
    private var title = ""
    // 文章作者
    private var author = ""
    // 文章作者链接
    private var authorUrl = ""
    // 关注作者状态
    private var isFollowAuthor = false
    // 点赞文章链接
    private var reactUrl = ""
    // 点赞文章状态
    private var isReact = false
    // 下载链接
    private var downloadUrl = ""

    /* 关键加载项 */
    // 链接
    private var url = ""
    // xfToken
    private var xfToken = ""
    // cookie
    private var cookieString = ""

    /* 关键加载项错误 */
    // 意图错误
    private var isIntentError = false
    // 安全错误
    private var isSecurityError = false

    /* 加载状态 */
    // 文章正在加载
    private var isArticleLoading = false
    // 点赞正在加载
    private var isReactLoading = false
    // 评论正在加载
    private var isFollowLoading = false

    /* UI 数据 */
    // 非关注作者颜色
    private var isNotFollowAuthorColor = 0
    // 关注作者颜色
    private var isFollowAuthorColor = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResourceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val loadingAnimator = ObjectAnimator.ofFloat(
            binding.btnReact, "rotation", 0f, 360f
        ).apply {
            setDuration(1000)
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
        url = intent.getStringExtra("url") ?: ""
        if (url.isEmpty()) {
            errorDialog(
                this, getString(R.string.intent_error),
                getString(R.string.intent_error_dialog)
            )
        }
        CookieUtil(this, BASE_URL + url, Cookies).getSecurityInfo(
            onSuccess = { cookiesString, token ->
                cookieString = cookiesString
                xfToken = token
                Log.i(getString(R.string.security_info),
                    getString(R.string.security_info_log_complete))
            },
            onError = { message ->
                errorDialog(this, getString(R.string.security_info_error), message)
                Log.e(getString(R.string.security_info),
                    getString(R.string.security_info_log_field))
                Log.e(getString(R.string.security_info), message)
            }
        )
        isNotFollowAuthorColor = getThemeColor(this, R.attr.colorOnFilledButton)
        isFollowAuthorColor = getThemeColor(this,
            R.attr.colorBottomNavigationBarOnBackgroundSecondary)

        adapter = ResourceCommentAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.srlRoot.setEnableLoadMore(false)
        binding.srlRoot.setEnableAutoLoadMore(false)
        binding.srlRoot.setEnableOverScrollDrag(true)


        /*
         * 修改各个占位布局的高度
         * 以实现小白条与状态栏的沉浸
         */
        // 使用 ViewCompat 类设置一个窗口监听器（这是一个回调函数），需要传入当前页面的根布局
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLayout) { _, insets ->
            // systemBars 是一个 insets 对象
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 设置状态栏占位布局高度
            binding.headerBarLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            binding.srlRoot.setHeaderInsetStartPx((systemBars.top + 60f.dpToPx(this))
                .roundToInt())
            // 修改滚动布局的边距
            binding.rootScrollView
                .setPadding(
                    systemBars.left,
                    (systemBars.top + 60f.dpToPx(this)).roundToInt(),
                    systemBars.right,
                    (systemBars.bottom + 70f.dpToPx(this)).roundToInt()
                )
            // 设置小白条占位布局高度
            binding.bottomBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom
            }
            binding.srlRoot.setFooterInsetStartPx((systemBars.bottom + 70f.dpToPx(this))
                .roundToInt())
            insets
        }
        // 初次加载数据
        loadResData()
        binding.srlRoot.setOnRefreshListener { loadResData() }
        binding.follow.setOnClickListener { followRes() }
        binding.btnDownload.setOnClickListener { downloadResource(downloadUrl) }
        binding.btnComment.setOnClickListener {
            val bindingArr = DialogxAddResourceReplyBinding.inflate(layoutInflater)
            BottomDialog.show(object : OnBindView<BottomDialog>(bindingArr.root) {
                override fun onBind(
                    dialog: BottomDialog,
                    v: View
                ) {
                    bindingArr.btnPost.setOnClickListener {
                        if (bindingArr.edtContent.text.isEmpty()) {
                            Toast.makeText(this@ResourceActivity, "Null", Toast.LENGTH_SHORT).show()
                        } else {
                            lifecycleScope.launch {
                                try {
                                    reportResource(url, xfToken, bindingArr.rtRes.rating.toString(),
                                        bindingArr.edtContent.text.toString(), cookieString,
                                        onSuccess = {
                                            Toast.makeText(this@ResourceActivity,
                                                R.string.published_successfully, Toast.LENGTH_SHORT)
                                                .show()
                                            dialog.dismiss()
                                        },
                                        onException = {
                                            errorDialog(this@ResourceActivity, "ERROR", it)
                                            dialog.dismiss()
                                        }
                                    )
                                } catch (e: Exception) {
                                    errorDialog(this@ResourceActivity, "ERROR", e.message)
                                    dialog.dismiss()
                                }
                            }
                        }
                    }
                }
            })
        }
        binding.btnReact.setOnClickListener { reactRes(loadingAnimator) }
        binding.btnShare.setOnClickListener {
            val share = Intent.createChooser(Intent().apply {
                setAction(Intent.ACTION_SEND)
                setType("text/plain")
                putExtra(Intent.EXTRA_TEXT, BASE_URL + url)
                putExtra(Intent.EXTRA_TITLE, title)
            }, null)
            startActivity(share)
        }
        binding.btnMore.setOnClickListener { resMoreDialog() }
        binding.back.setOnClickListener { finish() }
    }

    private fun resMoreDialog() {
        val bindingMore = DialogxArticleMoreBinding.inflate(layoutInflater)
        BottomDialog.show(object : OnBindView<BottomDialog>(bindingMore.root) {
            override fun onBind(dialog: BottomDialog, view: View) {
                bindingMore.btnClose.setOnClickListener {
                    dialog.dismiss()
                }
                bindingMore.btnCopyLink.setOnClickListener {
                    // 获取 ClipboardManager 实例
                    val clipboard = this@ResourceActivity.getSystemService(
                        CLIPBOARD_SERVICE) as ClipboardManager
                    // 创建 ClipData 对象（带标签和内容）
                    val clipData = ClipData.newPlainText(title, BASE_URL + url)
                    // 设置到系统剪贴板
                    clipboard.setPrimaryClip(clipData)
                    Toast.makeText(this@ResourceActivity, R.string.link_copied_clipboard,
                        Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                bindingMore.btnReport.isGone = true
                bindingMore.tvReport.isGone = true
                bindingMore.btnEdit.isGone = true
                bindingMore.tvEdit.isGone = true
                bindingMore.btnDelete.isGone = true
                bindingMore.tvDelete.isGone = true
                bindingMore.btnIp.isGone = true
                bindingMore.tvIp.isGone = true
                bindingMore.btnChangeAuthor.isGone = true
                bindingMore.tvChangeAuthor.isGone = true
            }
        })
    }

    private fun followRes() {
        if (isFollowLoading) {
            Toast.makeText(this, R.string.toast_operation, Toast.LENGTH_SHORT).show()
            return
        }
        isFollowLoading = true
        lifecycleScope.launch {
            val follow = follow(authorUrl, cookieString, xfToken)
            if (follow) {
                if (isFollowAuthor) {
                    isFollowAuthor = false
                    binding.follow.background = AppCompatResources.getDrawable(
                        this@ResourceActivity, R.drawable.gustatex_button_filled
                    )
                    binding.follow.text = getString(R.string.follow)
                    binding.follow.setTextColor(isNotFollowAuthorColor)
                } else {
                    isFollowAuthor = true
                    binding.follow.background = AppCompatResources.getDrawable(
                        this@ResourceActivity, R.drawable.gustatex_button_outline
                    )
                    binding.follow.text = getString(R.string.following)
                    binding.follow.setTextColor(isFollowAuthorColor)
                }
            } else {
                Toast.makeText(
                    this@ResourceActivity, R.string.operation_failed,
                    Toast.LENGTH_SHORT
                ).show()
            }
            isFollowLoading = false
        }
    }

    /**
     * 加载资源
     * @param isRefresh 是否为刷新状态
     */
    private fun loadResData(isRefresh: Boolean = true) {
        // 错误或已经开始加载直接返回
        if (isIntentError || isArticleLoading) return
        isArticleLoading = true
        // 刷新变量
        if (isRefresh) {
            title = ""               // 文章标题
            author = ""              // 文章作者
            authorUrl = ""           // 文章作者链接
            isFollowAuthor = false   // 关注作者状态
            reactUrl = ""            // 点赞文章链接
            isReact = false          // 点赞文章状态
            downloadUrl = ""         // 下载链接
            adapter = ResourceCommentAdapter()
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
        }
        // 启动协程 (IO 线程)
        lifecycleScope.launch {
            try {
                /**
                 * 获取内容
                 * @see fetchResourceArticle 爬取资源文章
                 * @see isFollow 获取关注状态
                 */
                val content = fetchResourceArticle(url)
                val comment = getResourceReport(url)
                // 更新变量
                title = content.title                  // 文章标题
                author = content.author                // 文章作者
                authorUrl = content.authorUrl          // 文章作者链接
                isFollowAuthor = isFollow(authorUrl)   // 关注作者状态
                reactUrl = content.reactUrl            // 点赞文章链接
                isReact = content.isReact              // 点赞文章状态
                downloadUrl = content.downloadUrl      // 下载链接
                withContext(Dispatchers.Main) {
                    updateAuthorInfo(content)                        // 更新资源作者信息
                    updateResInfo(content)                           // 更新资源基本信息
                    updateButtons(content)                           // 更新可互动按钮
                    htmlToSpan(binding.tvContent, content.content)   // 更新资源文章
                    if (isRefresh) {
                        adapter.addAll(comment)
                    }
                }
            } catch (e: Exception) {
                Log.e("Error", e.message ?: "")
                withContext(Dispatchers.Main) {
                    errorDialog(this@ResourceActivity, "ERROR", e.message)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.srlRoot.finishRefresh()
                    isArticleLoading = false
                }
            }
        }
    }

    /**
     * 更新资源作者信息
     * @param content 资源内容
     * @see ResourceArticle 数据类
     */
    private fun updateAuthorInfo(content: ResourceArticle) {
        if (content.authorAvatar.isNotEmpty())
            Glide.with(this)
                .load(BASE_URL + content.authorAvatar)
                .placeholder(R.drawable.avatar_account)
                .error(R.drawable.avatar_account)
                .into(binding.userAvatar)
        binding.userNameText.text = content.author
        binding.time.text = content.latestPost
    }

    /**
     * 资源信息数据类
     * @param value 值
     * @param labelView 标题 View
     * @param valueView 值 TextView
     */
    data class ResInfo(
        val value: String,
        val labelView: View,
        val valueView: TextView
    )

    /**
     * 更新资源基本信息
     * @param content 资源内容
     * @see ResourceArticle 数据类
     */
    private fun updateResInfo(content: ResourceArticle) {
        if (content.cover.isNotEmpty())
            Glide.with(this)
                .load(BASE_URL + content.cover)
                .error(R.drawable.ic_uo)
                .into(binding.cover)
        binding.tvTitle.text = content.title
        val resInfoList = listOf(
            ResInfo(content.device, binding.lblDevice, binding.tvDevice),
            ResInfo(content.downloadType, binding.lblChannel, binding.tvChannel),
            ResInfo(content.size, binding.lblSize, binding.tvSize),
            ResInfo(content.password, binding.lblCode, binding.tvCode),
            ResInfo(content.author, binding.lblAuthor, binding.tvAuthor),
            ResInfo(content.downloadCount, binding.lblDownload, binding.tvDownload),
            ResInfo(content.viewCount, binding.lblView, binding.tvView),
            ResInfo(content.firstPost, binding.lblFirst, binding.tvFirst),
            ResInfo(content.latestPost, binding.lblLast, binding.tvLast)
        )
        resInfoList.forEach { (value, labelView, valueView) ->
            val isGone = value.isEmpty() == true
            labelView.isGone = isGone
            valueView.isGone = isGone
            if (!isGone) valueView.text = value
        }
    }

    /**
     * 更新可互动按钮
     * @param content 资源内容
     * @see ResourceArticle 数据类
     */
    private fun updateButtons(content: ResourceArticle) {
        binding.favouriteCount.text = content.numberOfLikes
        isReact = content.isReact
        reactUrl = content.reactUrl
        binding.btnReact.setBtnImgRes(
            selected = content.isReact,
            trueRes = R.drawable.ic_is_react,
            falseRes = R.drawable.ic_favourite
        )
        binding.collect.setBtnImgRes(
            selected = content.isBookMark,
            trueRes = R.drawable.ic_option_collect,
            falseRes = R.drawable.ic_collect
        )
        val (backgroundRes, textRes, colorRes) = if (isFollowAuthor) {
            Triple(R.drawable.gustatex_button_outline, R.string.following, isFollowAuthorColor)
        } else {
            Triple(R.drawable.gustatex_button_filled, R.string.follow, isNotFollowAuthorColor)
        }
        binding.follow.apply {
            background = AppCompatResources.getDrawable(this@ResourceActivity, backgroundRes)
            text = getString(textRes)
            setTextColor(colorRes)
        }
    }

    private fun ImageView.setBtnImgRes(selected: Boolean, trueRes: Int, falseRes: Int) {
        val drawable = AppCompatResources.getDrawable(
            context,
            if (selected) trueRes else falseRes
        )
        setImageDrawable(drawable)
    }

    private fun reactRes(loadingAnimator: ObjectAnimator) {
        if (reactUrl.isEmpty()) {
            Toast.makeText(this, R.string.toast_own_article_like, Toast.LENGTH_SHORT).show()
            return
        }
        if (isReactLoading) {
            Toast.makeText(this, R.string.toast_operation, Toast.LENGTH_SHORT).show()
            return
        }
        isReactLoading = true
        val oldImg = binding.btnReact.drawable
        binding.btnReact.setImageDrawable(AppCompatResources.getDrawable(this,
            R.drawable.ic_loading))
        loadingAnimator.start()
        lifecycleScope.launch {
            val changeReact = addReaction(reactUrl, cookieString, xfToken)
            if (changeReact) {
                if (isReact) {
                    isReact = false
                    binding.btnReact.setImageDrawable(AppCompatResources.getDrawable(
                        this@ResourceActivity, R.drawable.ic_favourite))
                    val reactCount = binding.favouriteCount.text.toString().toInt() - 1
                    binding.favouriteCount.text = reactCount.toString()
                } else {
                    isReact = true
                    binding.btnReact.setImageDrawable(AppCompatResources.getDrawable(
                        this@ResourceActivity, R.drawable.ic_is_react))
                    val reactCount = binding.favouriteCount.text.toString().toInt() + 1
                    binding.favouriteCount.text = reactCount.toString()
                }
            } else {
                Toast.makeText(this@ResourceActivity, R.string.operation_failed,
                    Toast.LENGTH_SHORT).show()
                binding.btnReact.setImageDrawable(oldImg)
            }
            isReactLoading = false
            loadingAnimator.cancel()
        }
    }

    private fun downloadResource(url: String) {
        lifecycleScope.launch {
            val purchaseData = getPurchaseData(url)
            val resType = purchaseData.first().resType
            when(resType) {
                ResourceType.New -> {
                    buyNewResDialog(
                        purchaseData = purchaseData,
                        onPaid = {
                            Toast.makeText(this@ResourceActivity, R.string.purchase_successful,
                                Toast.LENGTH_SHORT).show()
                            downloadResource(url)
                        }
                    )
                }
                ResourceType.Old -> {
                    buyOldResDialog(
                        purchaseData = purchaseData,
                        onPaid = {
                            Toast.makeText(this@ResourceActivity, R.string.purchase_successful,
                                Toast.LENGTH_SHORT).show()
                            downloadResource(url)
                        }
                    )
                }
                ResourceType.Other -> {
                    openUrlInBrowser(this@ResourceActivity, purchaseData.first().url)
                }
            }
        }
    }

    private fun buyNewResDialog(
        purchaseData: MutableList<PurchaseData>,
        onPaid: (Boolean) -> Unit
    ) {
        val bindingSdt = DialogxSelectDownloadTypeBinding.inflate(layoutInflater)
        BottomDialog.show(object : OnBindView<BottomDialog>(bindingSdt.root) {
            override fun onBind(
                dialog: BottomDialog,
                view: View
            ) {
                bindingSdt.recyclerView.layoutManager = LinearLayoutManager(this@ResourceActivity)
                bindingSdt.recyclerView.adapter = NewResourceTypeAdapter().apply {
                    onItemClick = {
                        buyRes(it, dialog, true, onPaid)
                    }
                }
                val adapter = bindingSdt.recyclerView.adapter as NewResourceTypeAdapter
                adapter.addAll(purchaseData)
                bindingSdt.close.setOnClickListener {
                    dialog.dismiss()
                }
            }
        })
    }

    private fun buyOldResDialog(
        purchaseData: MutableList<PurchaseData>,
        onPaid: (Boolean) -> Unit
    ) {
        val bindingDdb = DialogxDonationDownloadBinding.inflate(layoutInflater)
        BottomDialog.show(object : OnBindView<BottomDialog>(bindingDdb.root) {
            override fun onBind(
                dialog: BottomDialog,
                v: View
            ) {
                bindingDdb.tvPrice.text = getString(
                    R.string.res_price,
                    purchaseData.first().price
                )
                bindingDdb.btnDonate.setOnClickListener {
                    buyRes(purchaseData.first(), dialog, false, onPaid)
                }
            }
        })
    }

    private fun buyRes(
        data: PurchaseData,
        dialog: BottomDialog,
        isNewRes: Boolean,
        onPaid: (Boolean) -> Unit
    ) {
        lifecycleScope.launch {
            try {
                if (data.isPaid) {
                    dialog.dismiss()
                    openUrlInBrowser(this@ResourceActivity, data.url)
                } else {
                    buyResource(
                        url = if (isNewRes) data.url else data.url
                            .replace("/download", "/purchase"),
                        xfToken = xfToken, cookiesString = cookieString,
                        onSuccess = {
                            onPaid(true)
                            dialog.dismiss()
                        },
                        onException = {
                            Exception(it)
                            dialog.dismiss()
                        }
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorDialog(
                        context = this@ResourceActivity, title = getString(R.string.unknown_error),
                        message = e.message
                    )
                }
            }
        }
    }
}
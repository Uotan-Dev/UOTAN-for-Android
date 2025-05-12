package com.gustate.uotan.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.webkit.WebSettings
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.animation.ValueAnimator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.anim.TitleAnim
import com.gustate.uotan.databinding.ActivityArticleBinding
import com.gustate.uotan.gustatex.dialog.InfoDialog
import com.gustate.uotan.gustatex.dialog.InputDialog
import com.gustate.uotan.gustatex.dialog.LoadingDialog
import com.gustate.uotan.utils.CookieUtil
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.errorDialog
import com.gustate.uotan.utils.Utils.Companion.getThemeColor
import com.gustate.uotan.utils.Utils.Companion.htmlToSpan
import com.gustate.uotan.utils.Utils.Companion.idToAvatar
import com.gustate.uotan.utils.Utils.Companion.openUrlInBrowser
import com.gustate.uotan.utils.parse.article.ArticleParse.Companion.CommentItem
import com.gustate.uotan.utils.parse.article.ArticleParse.Companion.addReaction
import com.gustate.uotan.utils.parse.article.ArticleParse.Companion.addArticleReply
import com.gustate.uotan.utils.parse.article.ArticleParse.Companion.articleParse
import com.gustate.uotan.utils.parse.article.ArticleParse.Companion.changeAuthor
import com.gustate.uotan.utils.parse.article.ArticleParse.Companion.deleteArticle
import com.gustate.uotan.utils.parse.article.ArticleParse.Companion.fetchComments
import com.gustate.uotan.utils.parse.article.ArticleParse.Companion.getAuthorIp
import com.gustate.uotan.utils.parse.article.ArticleParse.Companion.report
import com.gustate.uotan.utils.parse.user.UserParse.Companion.follow
import com.gustate.uotan.utils.parse.user.UserParse.Companion.isFollow
import com.gustate.uotan.utils.room.UserViewModel
import com.kongzue.dialogx.dialogs.BottomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * 文章页面 (Activity)
 * 2025-04-04
 */

class ArticleActivity : BaseActivity() {

    /**
     * 评论列表框适配器 (Adapter)
     */
    class CommentAdapter() : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
        private val commentsList = mutableListOf<CommentItem>()
        private val filterSet = mutableSetOf<String>()
        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val avatar: ImageView = view.findViewById(R.id.userAvatar)
            val userName: TextView = view.findViewById(R.id.userNameText)
            val time: TextView = view.findViewById(R.id.time)
            val ip: TextView = view.findViewById(R.id.tv_ip)
            val content: TextView = view.findViewById(R.id.content)
        }
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_comment_item, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int
        ) {
            val content = commentsList[position]
            val avatarUrl = idToAvatar(content.userId)
            Glide.with(holder.itemView.context)
                .load(avatarUrl)
                .error(R.drawable.avatar_account)
                .into(holder.avatar)
            holder.userName.text = content.userName
            holder.time.text = content.time
            holder.ip.text = content.userIp
            htmlToSpan(holder.content, content.content)
        }

        override fun getItemCount(): Int = commentsList.size

        fun addAll(newItems: MutableList<CommentItem>) {
            val filteredList = newItems.filter {
                !filterSet.contains(it.postTime) && !commentsList.any { existing -> existing.postTime == it.postTime }
            }
            val startPosition = commentsList.size
            commentsList.addAll(filteredList)
            notifyItemRangeInserted(startPosition, filteredList.size)
        }

        fun addNewReply(comment: CommentItem) {
            filterSet.add(comment.content)
            commentsList.add(0, comment)
            notifyItemInserted(0)
        }
    }

    private lateinit var binding: ActivityArticleBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var adapter: CommentAdapter
    private lateinit var viewModel: UserViewModel
    private lateinit var cookiesString: String
    private lateinit var changeAuthorUrl: String
    private lateinit var xfToken: String
    private lateinit var attachmentHash: String
    private lateinit var attachmentHashCombined: String
    private lateinit var lastDate: String
    private lateinit var lastKnowDate: String
    private lateinit var loadExtra: String
    private var colorOnFilledButton = 0
    private var colorOnOutlineButton = 0
    private var isReacting = false
    private var isFollowing = false
    private var isFollowAuthor = false
    private var isLocked = false
    private var isReact = false
    private var isJingHua = false
    private var totalPage = 1
    private var currentPage = 1
    private var title = ""
    private var url = ""
    private var reactUrl = ""
    private var reportUrl = ""
    private var editUrl = ""
    private var deleteUrl = ""
    private var ipUrl = ""
    private var ip = ""
    private var authorUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityArticleBinding.inflate(layoutInflater)
        setContentView(binding.rootLayout)

        colorOnFilledButton = getThemeColor(this, R.attr.colorOnFilledButton)
        colorOnOutlineButton = getThemeColor(this, R.attr.colorBottomNavigationBarOnBackgroundSecondary)

        loadingDialog = LoadingDialog(this)
        url = intent.getStringExtra("url")!!

        val cookieUtil = CookieUtil(this, BASE_URL + url, Cookies)

        cookieUtil.getSecurityInfo(
            onSuccess = { cs, xft, h, hc, ld, lkd, le ->
                cookiesString = cs
                xfToken = xft
                attachmentHash = h
                attachmentHashCombined = hc
                lastDate = ld
                lastKnowDate = lkd
                loadExtra = le
                Log.i("SecurityInfo", "Security information loading completed")
            },
            onError = {
                errorDialog(this, getString(R.string.security_info_error), it)
                Log.e("SecurityInfo", "Security information loading field")
                Log.e("SecurityInfo", it)
            }
        )

        viewModel = ViewModelProvider(this)[UserViewModel::class.java]
        val loadingAnimator = ObjectAnimator.ofFloat(
            binding.btnReact,
            "rotation",
            0f,
            360f
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
            //修改滚动布局的边距
            binding.rootScrollView.setPadding(
                systemBars.left,
                (systemBars.top + 60f.dpToPx(this)).roundToInt(),
                systemBars.right,
                (systemBars.bottom + 70f.dpToPx(this)).roundToInt()
            )
            binding.srlRoot.setHeaderInsetStartPx((systemBars.top + 60f.dpToPx(this@ArticleActivity)).roundToInt())
            binding.srlRoot.setFooterInsetStartPx((systemBars.bottom + 70f.dpToPx(this@ArticleActivity)).roundToInt())
            // 设置小白条占位布局高度
            binding.gestureView.layoutParams.height = systemBars.bottom
            // 创建 SimonEdgeIllusion 实例
            TitleAnim(
                binding.tvTitle,
                binding.bigTitle,
                (systemBars.top + 60f.dpToPx(this)),
                systemBars.top.toFloat()
            )
            // 返回 insets
            insets
        }

        loadArticle(url)

        binding.tvContent.movementMethod = LinkMovementMethod.getInstance()

        loadComments(url, true)

        binding.srlRoot.setOnRefreshListener {
            loadArticle(url)
            loadComments(url, true)
        }

        binding.srlRoot.setOnLoadMoreListener {
            loadComments(url, false)
        }

        binding.bigTitle.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    UserActivity::class.java
                ).putExtra("url", authorUrl)
            )
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
                    cookiesString,
                    xfToken
                )
                if (follow) {
                    if (isFollowAuthor) {
                        isFollowAuthor = false
                        binding.follow.background = AppCompatResources.getDrawable(
                            this@ArticleActivity, R.drawable.gustatex_button_filled
                        )
                        binding.bigFollow.background = AppCompatResources.getDrawable(
                            this@ArticleActivity, R.drawable.gustatex_button_filled
                        )
                        binding.follow.text = getString(R.string.follow)
                        binding.bigFollow.text = getString(R.string.follow)
                        binding.follow.setTextColor(colorOnFilledButton)
                        binding.bigFollow.setTextColor(colorOnFilledButton)
                    } else {
                        isFollowAuthor = true
                        binding.follow.background = AppCompatResources.getDrawable(
                            this@ArticleActivity, R.drawable.gustatex_button_outline
                        )
                        binding.bigFollow.background = AppCompatResources.getDrawable(
                            this@ArticleActivity, R.drawable.gustatex_button_outline
                        )
                        binding.follow.text = getString(R.string.following)
                        binding.bigFollow.text = getString(R.string.following)
                        binding.follow.setTextColor(colorOnOutlineButton)
                        binding.bigFollow.setTextColor(colorOnOutlineButton)
                    }
                } else {
                    Toast.makeText(
                        this@ArticleActivity,
                        "操作失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                isFollowing = false
            }
        }

        binding.btnComment.setOnClickListener {
            if (isLocked) {
                Toast.makeText(
                    this,
                    R.string.locked_post,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            BottomDialog.show(object : OnBindView<BottomDialog>(R.layout.dialogx_add_reply) {
                override fun onBind(dialog: BottomDialog, v: View) {
                    val btnClose = v.findViewById<View>(R.id.close)
                    btnClose?.setOnClickListener {
                        dialog.dismiss()
                    }
                    val btnPost = v.findViewById<View>(R.id.btnPost)
                    val edtContent = v.findViewById<EditText>(R.id.edtContent)
                    btnPost?.setOnClickListener {
                        loadingDialog.show()
                        val message = edtContent?.text.toString()
                        lifecycleScope.launch {
                            val userData = viewModel.getUser()
                            addArticleReply(
                                url,
                                cookiesString,
                                xfToken,
                                message,
                                attachmentHash,
                                attachmentHashCombined,
                                lastDate,
                                lastKnowDate,
                                loadExtra,
                                onSuccess = {
                                    dialog.dismiss()
                                    val instant = Instant.ofEpochSecond(it)
                                    val formatter = DateTimeFormatter
                                        .ofPattern("yyyy-MM-dd HH:mm")
                                        .withZone(ZoneId.systemDefault())
                                    val formattedDate = formatter.format(instant)
                                    adapter.addNewReply(CommentItem(userData?.userId ?:"",
                                        userData?.userName ?:"", userData?.ipAddress ?:"",
                                        formattedDate, message, it.toString()))
                                    val commentCount = binding.commentCount.text.toString()
                                        .toInt() + 1
                                    binding.commentCount.text = commentCount.toString()
                                    loadingDialog.cancel()
                                    Toast.makeText(this@ArticleActivity,
                                        R.string.published_successfully, Toast.LENGTH_SHORT).show()
                                },
                                onException = {
                                    loadingDialog.cancel()
                                    Toast.makeText(this@ArticleActivity,
                                        R.string.publication_failed, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            })
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
            binding.btnReact.setImageDrawable(
                AppCompatResources.getDrawable(this, R.drawable.ic_loading)
            )
            loadingAnimator.start()
            lifecycleScope.launch {
                val changeReact = addReaction(
                    reactUrl,
                    cookiesString,
                    xfToken
                )
                if (changeReact) {
                    if (isReact) {
                        isReact = false
                        binding.btnReact.setImageDrawable(
                            AppCompatResources.getDrawable(this@ArticleActivity, R.drawable.ic_favourite)
                        )
                        val reactCount = binding.favouriteCount.text.toString().toInt() - 1
                        binding.favouriteCount.text = reactCount.toString()
                    } else {
                        isReact = true
                        binding.btnReact.setImageDrawable(
                            AppCompatResources.getDrawable(this@ArticleActivity, R.drawable.ic_is_react)
                        )
                        val reactCount = binding.favouriteCount.text.toString().toInt() + 1
                        binding.favouriteCount.text = reactCount.toString()
                    }
                } else {
                    Toast.makeText(
                        this@ArticleActivity,
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
                setAction(Intent.ACTION_SEND)
                setType("text/plain")
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
                    val btnCopyLink = v?.findViewById<View>(R.id.btn_copy_link)
                    btnCopyLink?.setOnClickListener {
                        // 获取 ClipboardManager 实例
                        val clipboard = this@ArticleActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        // 创建 ClipData 对象（带标签和内容）
                        val clipData = ClipData.newPlainText(title, BASE_URL.replace(".cn/", ".cn") + url)
                        // 设置到系统剪贴板
                        clipboard.setPrimaryClip(clipData)
                        Toast.makeText(
                            this@ArticleActivity,
                            R.string.link_copied_clipboard,
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog?.dismiss()
                    }
                    val btnReport = v?.findViewById<View>(R.id.btn_report)
                    val tvReport = v?.findViewById<View>(R.id.tv_report)
                    btnReport?.isGone = reportUrl.isEmpty()
                    tvReport?.isGone = reportUrl.isEmpty()
                    btnReport?.setOnClickListener {
                        dialog?.dismiss()
                        val inputDialog = InputDialog(this@ArticleActivity)
                        inputDialog
                            .setTitle("举报")
                            .setDescription("举报该内容")
                            .setCancel("取消")
                            .setConfirm("举报")
                            .withOnConfirm {
                                loadingDialog.show()
                                lifecycleScope.launch {
                                    val isReport = report(reportUrl, it, cookiesString, xfToken)
                                    withContext(Dispatchers.Main) {
                                        if (isReport) {
                                            Toast.makeText(
                                                this@ArticleActivity,
                                                "举报成功",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@ArticleActivity,
                                                "举报失败",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        inputDialog.dismiss()
                                        loadingDialog.cancel()
                                    }
                                }
                            }
                            .withOnCancel { inputDialog.dismiss() }
                            .show()
                    }
                    val btnEdit = v?.findViewById<View>(R.id.btn_edit)
                    val tvEdit = v?.findViewById<View>(R.id.tv_edit)
                    btnEdit?.isGone = editUrl.isEmpty()
                    tvEdit?.isGone = editUrl.isEmpty()
                    btnEdit?.setOnClickListener {
                        dialog?.dismiss()
                        openUrlInBrowser(this@ArticleActivity, BASE_URL + editUrl)
                    }
                    val btnDelete = v?.findViewById<View>(R.id.btn_delete)
                    val tvDelete = v?.findViewById<View>(R.id.tv_delete)
                    btnDelete?.isGone = deleteUrl.isEmpty()
                    tvDelete?.isGone = deleteUrl.isEmpty()
                    btnDelete?.setOnClickListener {
                        dialog?.dismiss()
                        val deleteDialog = InfoDialog(this@ArticleActivity)
                        deleteDialog
                            .setTitle(getString(R.string.permanently_delete))
                            .setDescription(getString(R.string.dialog_permanently_delete))
                            .setConfirmText(getString(R.string.ok))
                            .setCancelText(getString(R.string.cancel))
                            .withOnConfirm {
                                loadingDialog.show()
                                lifecycleScope.launch {
                                    val isDelete = deleteArticle(url, deleteUrl, cookiesString, xfToken)
                                    withContext(Dispatchers.Main) {
                                        if (isDelete) {
                                            Toast.makeText(
                                                this@ArticleActivity,
                                                R.string.delete_successfully,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@ArticleActivity,
                                                R.string.delete_failed,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        deleteDialog.dismiss()
                                        loadingDialog.cancel()
                                    }
                                }
                            }
                            .withOnCancel { deleteDialog.dismiss() }
                            .show()
                    }
                    val btnIp = v?.findViewById<View>(R.id.btn_ip)
                    val tvIp = v?.findViewById<View>(R.id.tv_ip)
                    btnIp?.isGone = ipUrl.isEmpty()
                    tvIp?.isGone = ipUrl.isEmpty()
                    btnIp?.setOnClickListener {
                        lifecycleScope.launch {
                            ip = if (ip.isEmpty()) {
                                getAuthorIp(ipUrl)
                            } else ip
                            withContext(Dispatchers.Main) {
                                dialog?.dismiss()
                                val deleteDialog = InfoDialog(this@ArticleActivity)
                                deleteDialog
                                    .setTitle(getString(R.string.IP))
                                    .setDescription(ip)
                                    .setConfirmText(getString(R.string.query))
                                    .setCancelText(getString(R.string.cancel))
                                    .withOnConfirm {
                                        openUrlInBrowser(this@ArticleActivity,
                                            "$BASE_URL/misc/ip-info?ip=$ip"
                                        )
                                        deleteDialog.dismiss()
                                    }
                                    .withOnCancel { deleteDialog.dismiss() }
                                    .show()
                            }
                        }
                    }
                    val btnChangeAuthor = v?.findViewById<View>(R.id.btn_change_author)
                    val tvChangeAuthor = v?.findViewById<View>(R.id.tv_change_author)
                    btnChangeAuthor?.isGone = changeAuthorUrl.isEmpty()
                    tvChangeAuthor?.isGone = changeAuthorUrl.isEmpty()
                    btnChangeAuthor?.setOnClickListener {
                        dialog?.dismiss()
                        val inputDialog = InputDialog(this@ArticleActivity)
                        inputDialog
                            .setTitle("变更作者")
                            .setDescription("变更该内容作者")
                            .setCancel("取消")
                            .setConfirm("变更")
                            .withOnConfirm {
                                loadingDialog.show()
                                lifecycleScope.launch {
                                    Log.e("it", it)
                                    val isChangeAuthor = changeAuthor(changeAuthorUrl, it, cookiesString, xfToken)
                                    withContext(Dispatchers.Main) {
                                        if (isChangeAuthor) {
                                            Toast.makeText(
                                                this@ArticleActivity,
                                                "变更成功",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@ArticleActivity,
                                                "变更失败，不存在该用户",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        inputDialog.dismiss()
                                        loadingDialog.cancel()
                                    }
                                }
                            }
                            .withOnCancel { inputDialog.dismiss() }
                            .show()
                    }
                }
            })
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadArticle(url: String) {
        loadingDialog.show()
        isFollowAuthor = false
        isReact = false
        isLocked = false
        isJingHua = false
        title = ""
        reactUrl = ""
        reportUrl = ""
        editUrl = ""
        deleteUrl = ""
        ipUrl = ""
        changeAuthorUrl = ""
        ip = ""
        authorUrl = ""
        lifecycleScope.launch {
            val content = articleParse(url)
            authorUrl = content.authorUrl
            isFollowAuthor = isFollow(authorUrl)
            isReact = content.isReact
            isLocked = content.isLocked
            isJingHua = content.isJingTie
            title = content.title
            reactUrl = content.reactUrl
            reportUrl = content.reportUrl
            editUrl = content.editUrl
            deleteUrl = content.deleteUrl
            ipUrl = content.ipUrl
            changeAuthorUrl = content.changeAuthorUrl
            withContext(Dispatchers.Main) {
                if (content.authorUrl.isNotEmpty()) {
                    Glide.with(this@ArticleActivity)
                        .load(BASE_URL + content.avatarUrl)
                        .into(binding.userAvatar)
                    Glide.with(this@ArticleActivity)
                        .load(BASE_URL + content.avatarUrl)
                        .into(binding.bigUserAvatar)
                }
                if (isFollowAuthor) {
                    binding.follow.background = AppCompatResources.getDrawable(this@ArticleActivity, R.drawable.gustatex_button_outline)
                    binding.bigFollow.background = AppCompatResources.getDrawable(this@ArticleActivity, R.drawable.gustatex_button_outline)
                    binding.follow.text = getString(R.string.following)
                    binding.bigFollow.text = getString(R.string.following)
                    binding.follow.setTextColor(colorOnOutlineButton)
                    binding.bigFollow.setTextColor(colorOnOutlineButton)
                } else {
                    binding.follow.background = AppCompatResources.getDrawable(this@ArticleActivity, R.drawable.gustatex_button_filled)
                    binding.bigFollow.background = AppCompatResources.getDrawable(this@ArticleActivity, R.drawable.gustatex_button_filled)
                    binding.follow.text = getString(R.string.follow)
                    binding.bigFollow.text = getString(R.string.follow)
                    binding.follow.setTextColor(colorOnFilledButton)
                    binding.bigFollow.setTextColor(colorOnFilledButton)
                }
                if (content.isBookMark) binding.collect.setImageDrawable(AppCompatResources.getDrawable(this@ArticleActivity, R.drawable.ic_option_collect))
                else binding.collect.setImageDrawable(AppCompatResources.getDrawable(this@ArticleActivity, R.drawable.ic_collect))
                binding.bilibiliCard.isGone = !content.isBilibili
                binding.bigUserNameText.text = content.authorName
                binding.userNameText.text = content.authorName
                binding.bigTime.text = content.time
                binding.time.text = content.time
                binding.bigIpLocation.text = content.ipAddress
                binding.ipLocation.text = content.ipAddress
                binding.bigTitleText.text = title
                binding.cardJingTie.isGone = !isJingHua
                binding.cardPostLocked.isGone = !isLocked
                if (isReact) binding.btnReact.setImageDrawable(AppCompatResources.getDrawable(this@ArticleActivity, R.drawable.ic_is_react))
                else binding.btnReact.setImageDrawable(AppCompatResources.getDrawable(this@ArticleActivity, R.drawable.ic_favourite))
                binding.favouriteCount.text = content.numberOfLikes
                binding.commentCount.text = content.numberOfComments
                binding.bilibiliWebView.settings.javaScriptEnabled = true // 启用JS
                binding.bilibiliWebView.settings.domStorageEnabled = true // 支持HTML5本地存储
                binding.bilibiliWebView.settings.mediaPlaybackRequiresUserGesture = false // 自动播放音频/视频
                binding.bilibiliWebView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                binding.bilibiliWebView.settings.allowContentAccess = true
                binding.bilibiliWebView.settings.allowFileAccess = true
                binding.bilibiliWebView.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"

                if (content.isBilibili) binding.bilibiliWebView.loadUrl("https:" + content.bilibiliVideoLink)
                loadingDialog.cancel()
                binding.srlRoot.finishRefresh()
                htmlToSpan(binding.tvContent, content.article.replace(
                    Regex(
                        """<span data-guineapigclub-mediaembed="bilibili">.*?</span>""",
                        RegexOption.DOT_MATCHES_ALL),
                        ""
                    ).replace(
                    Regex(
                        """<script class="js-extraPhrases" type="application/json">.*?</script>""",
                        RegexOption.DOT_MATCHES_ALL),
                    ""
                ))
            }
        }
    }

    private fun loadComments(url: String, first: Boolean) {
        if (first) {
            currentPage = 1
            totalPage = 1
            adapter = CommentAdapter()
            binding.commentRecyclerView.adapter = adapter
            binding.commentRecyclerView.layoutManager = LinearLayoutManager(this)
        }
        // 加载评论
        lifecycleScope.launch {
            try {
                val comments = fetchComments(url, currentPage.toString())
                withContext(Dispatchers.Main) {
                    adapter.addAll(comments.commentItem)
                    currentPage++
                    totalPage = comments.totalPage.toInt()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    if (currentPage <= totalPage) {
                        binding.srlRoot.finishLoadMore()
                    } else {
                        binding.srlRoot.setNoMoreData(true)
                        binding.srlRoot.finishLoadMoreWithNoMoreData()
                    }
                }
            }
        }
    }
}
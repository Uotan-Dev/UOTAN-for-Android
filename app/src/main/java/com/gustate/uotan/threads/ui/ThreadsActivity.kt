package com.gustate.uotan.threads.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.github.iielse.imageviewer.ImageViewerBuilder
import com.github.iielse.imageviewer.core.Photo
import com.github.iielse.imageviewer.core.SimpleDataProvider
import com.github.iielse.imageviewer.utils.Config
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.anim.LoadingAnim
import com.gustate.uotan.article.ContentAdapter
import com.gustate.uotan.article.HtmlParse
import com.gustate.uotan.article.imageviewer.ImageLoader
import com.gustate.uotan.article.imageviewer.ImageTransformer
import com.gustate.uotan.databinding.ActivityThreadsBinding
import com.gustate.uotan.gustatex.dialog.InfoDialog
import com.gustate.uotan.gustatex.dialog.LoadingDialog
import com.gustate.uotan.threads.data.model.ThreadPhoto
import com.gustate.uotan.ui.activity.UserActivity
import com.gustate.uotan.utils.Helpers.Companion.avatarOptions
import com.gustate.uotan.utils.Utils.Companion.baseUrl
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.errorDialog
import com.gustate.uotan.utils.Utils.Companion.getThemeColor
import com.gustate.uotan.utils.data.model.Attachment
import com.kongzue.dialogx.dialogs.BottomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class ThreadsActivity : BaseActivity() {

    //
    private lateinit var binding: ActivityThreadsBinding
    private lateinit var contentAdapter: ContentAdapter
    private lateinit var adapter: PostsAdapter
    private lateinit var loadingDialog: LoadingDialog

    private var colorOnFilledButton = 0
    private var colorOnFilledTonalButton = 0

    private val pictureList = mutableListOf<Photo>()
    private val loadingAnim = LoadingAnim()

    private val viewModel: ThreadsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialog = LoadingDialog(this)
        binding = ActivityThreadsBinding.inflate(layoutInflater)
        colorOnFilledButton = getThemeColor(this, R.attr.colorOnFilledButton)
        colorOnFilledTonalButton = getThemeColor(this, R.attr.colorOnFilledTonalButton)
        setContentView(binding.root)
        adapter = PostsAdapter()
        binding.rvPosts.adapter = adapter
        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        contentAdapter = ContentAdapter().apply {
            onImageClick = { id, url ->
                showPictureViewer(id.toLong(), url)
            }
        }
        Config.VIEWER_BACKGROUND_COLOR = 0
        binding.rvContent.adapter = contentAdapter
        binding.rvContent.layoutManager = LinearLayoutManager(this)
        setSystemBarsInsets()
        val threadOrPost = intent.getStringExtra("url")?: ""
        obverseThreads()
        viewModel.loadInitialThreadsAndPosts(
            threadOrPostId = threadOrPost, isRefresh = false, onSuccess = {},
            onException = { errorDialog(this, "ERROR", it.toString()) }
        )
        setRefreshLayoutListener(threadOrPost)
        showShareDialog(threadOrPost)
        showMoreDialog(threadOrPost)
        binding.btnReact.setOnClickListener {
            if (viewModel.isReacting.value == true) {
                Toast.makeText(
                    this@ThreadsActivity,
                    R.string.toast_operation,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            viewModel.reactPosts()
        }
        binding.btnCollect.setOnClickListener {
            if (viewModel.isBooking.value == true) {
                Toast.makeText(
                    this@ThreadsActivity,
                    R.string.toast_operation,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            viewModel.bookMarkThreads(
                onThrowable = {

                },
                onFailure = { i, t ->

                }
            )
        }
        back()
        changeAuthorFollowState()
        scrollToComment()
        binding.layoutUser.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    UserActivity::class.java
                ).putExtra("url", viewModel.threadPost.value?.user?.viewURL?.replace(baseUrl, ""))
            )
        }
        // background drawable alpha 128
        val lb = binding.btnReply.background
        lb?.alpha = 128
        binding.btnReply.background = lb
        binding.btnReply.setOnClickListener {
            if (viewModel.isThreadsLocked.value == true) {
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
                        viewModel.replyThreads(
                            message = message,
                            onSuccess = {
                                loadingDialog.dismiss()
                                dialog.dismiss()
                                adapter.addNewReply(binding.rvPosts, it)
                                Toast.makeText(
                                    this@ThreadsActivity,
                                    R.string.published_successfully,
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onThrowable = {

                            },
                            onFailure = { code, body ->

                            }
                        )
                    }
                }
            })
        }
    }

    /**
     * 适配系统栏
     */
    private fun setSystemBarsInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 图片查看器状态栏偏移
            Config.TRANSITION_OFFSET_Y = systemBars.top
            // 手机
            binding.headerBarLayout?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            // 平板
            if (binding.btnReply.tag == "pad") {
                binding.btnReply.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = systemBars.bottom
                }
            }
            // 竖屏
            if (binding.nsvRoot?.tag == "pad") {
                binding.nsvRoot?.setPadding(systemBars.left, systemBars.top, systemBars.right,
                    (2 * systemBars.bottom) + 60f.dpToPx(this).roundToInt())
                binding.srlRoot?.setHeaderInsetStartPx(systemBars.top)
                binding.srlRoot?.setFooterInsetStartPx((2 * systemBars.bottom) + 60f
                    .dpToPx(this@ThreadsActivity).roundToInt())
            } else {
                binding.nsvRoot?.setPadding(systemBars.left,
                    systemBars.top + 60f.dpToPx(this).roundToInt(), systemBars.right,
                    systemBars.bottom + 70f.dpToPx(this).roundToInt())
                binding.srlRoot?.setHeaderInsetStartPx(systemBars.top + 60f.dpToPx(this).roundToInt())
                binding.srlRoot?.setFooterInsetStartPx(systemBars.bottom + 70f.dpToPx(this).roundToInt())
            }
            // 横屏
            binding.srlArticle?.apply {
                setHeaderInsetStartPx(systemBars.top)
                setFooterInsetStartPx((2 * systemBars.bottom) + 60f
                    .dpToPx(this@ThreadsActivity).roundToInt())
            }
            binding.srlPosts?.apply {
                setHeaderInsetStartPx(systemBars.top)
                setFooterInsetStartPx(systemBars.bottom)
            }
            binding.nsvArticle?.setPadding(0, systemBars.top, 0,
                (2 * systemBars.bottom) + 60f.dpToPx(this).roundToInt())
            binding.nsvPosts?.setPadding(0, systemBars.top, 0, systemBars.bottom)
            // 横竖屏通用
            binding.btnBack.let {
                if (it.tag == "pad") {
                    it.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        topMargin = systemBars.top + 10f.dpToPx(this@ThreadsActivity).roundToInt()
                    }
                }
            }
            insets
        }
    }

    /**
     *
     */
    private fun setRefreshLayoutListener(url: String) {
        // Phone
        binding.srlRoot?.let { srl ->
            srl.setOnRefreshListener { refreshThreadsAndPosts(url, srl) }
            srl.setOnLoadMoreListener { loadMorePosts(url, srl) }
        }
        // Pad (Land)
        binding.srlArticle?.let { srl ->
            srl.setOnRefreshListener{ refreshThreadsAndPosts(url, srl) }
        }
        binding.srlPosts?.let { srl ->
            srl.setOnRefreshListener { refreshThreadsAndPosts(url, srl) }
            srl.setOnLoadMoreListener { loadMorePosts(url, srl) }
        }
    }

    private fun refreshThreadsAndPosts(url: String, srl: SmartRefreshLayout) {
        viewModel.loadInitialThreadsAndPosts(
            threadOrPostId = url,
            isRefresh = true,
            onSuccess = {
                srl.finishRefresh()
            },
            onException = {
                srl.finishRefresh()
                errorDialog(this, "ERROR", it.message)
            }
        )
    }

    private fun loadMorePosts(url: String, srl: SmartRefreshLayout) {
        viewModel.loadMorePosts(
            threadOrPostId = url,
            onSuccess = {
                srl.finishLoadMore()
            },
            onException = {
                srl.finishLoadMore()
                errorDialog(this, "ERROR", it.message)
            }
        )
    }

    private fun obverseThreads() {
        updateThreadsContent()
        updateThreadsInfo()
        viewModel.posts.observe(this) {
            adapter.submitList(it)
        }
        viewModel.isLastPage.observe(this) {
            binding.srlRoot?.setNoMoreData(it)
            binding.srlPosts?.setNoMoreData(it)
        }
        updateAuthorFollowButton()
    }

    /**
     * 监听并更新标题栏与主题内容
     * @param title 文章标题
     */
    private fun updateThreadsContent() {
        viewModel.threadPost.observe(this) {
            // 标题栏更新
            // 作者信息（Phone 与 Pad 的共有内容）
            Glide.with(this)
                .load(it.user.avatarUrls.o)
                .apply(avatarOptions)
                .into(binding.imgAvatar)
            binding.tvUsername.text = it.username
            // 作者信息（仅有 Pad 有的内容）
            binding.imgArticleAvatar?.let { view ->
                Glide.with(this)
                    .load(it.user.avatarUrls.o)
                    .apply(avatarOptions)
                    .into(view)
            }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            binding.tvTime.text = dateFormat.format(Date(it.postDate * 1000L))
            binding.tvIp.text = it.user.location
            // 主题内容更新
            viewModel.threadInfo.observe(this) {
                it.title.let { title ->
                    if (title.isNotEmpty()) {
                        binding.tvTitle.text = title
                    } else {
                        binding.tvTitle.isGone = false
                    }
                }
            }
            var threadsHtml = it.messageParsed
            it.attachments?.forEach {
                threadsHtml = threadsHtml
                    .replace(it.thumbnailURL, it.directURL)
            }
            contentAdapter.updateContent(HtmlParse.parse(threadsHtml))
            // 初始化图片查看器
            setupPictureViewer(it.attachments)
        }
    }

    /**
     * 更新主题信息
     * 含回应状态与数量、评论数量
     */
    private fun updateThreadsInfo() {
        // 实例化加载动画
        val loading = loadingAnim.loadingAnim(binding.btnReact)
        val bookLoading = loadingAnim.loadingAnim(binding.btnCollect)
        // 更新回应信息
        viewModel.isReacting.observe(this) {
            val oldImg = binding.btnReact.drawable
            if (it) {
                binding.btnReact.background = null
                binding.btnReact.setImageResource(R.drawable.ic_loading)
                loading?.start()
            } else {
                loading?.end()
                binding.btnReact.background = AppCompatResources
                    .getDrawable(this, R.drawable.gustatex_btn_none_bkg)
                binding.btnReact.setImageDrawable(oldImg)
            }
        }
        viewModel.isThreadsReact.observe(this) {
            binding.btnReact.setImageResource(
                if (it) R.drawable.ic_reacted else R.drawable.ic_favourite
            )
        }
        viewModel.threadsReactCount.observe(this) {
            binding.tvReactCount.text = it.toString()
        }
        // 更新评论信息
        viewModel.threadsReplyCount.observe(this) {
            if (it == 0L) {
                binding.tvPosts.isGone = true
            }
            binding.tvPostCount.text = it.toString()
        }
        viewModel.isBooking.observe(this) {
            val oldImg = binding.btnCollect.drawable
            if (it) {
                binding.btnCollect.background = null
                binding.btnCollect.setImageResource(R.drawable.ic_loading)
                bookLoading?.start()
            } else {
                bookLoading?.end()
                binding.btnCollect.background = AppCompatResources
                    .getDrawable(this, R.drawable.gustatex_btn_none_bkg)
                binding.btnCollect.setImageDrawable(oldImg)
            }
        }
        viewModel.isThreadsBookMark.observe(this) {
            binding.btnCollect.setImageResource(
                if (it) R.drawable.ic_option_collect else R.drawable.ic_collect
            )
        }
        viewModel.threadsAuthorIpAddress.observe(this) {
            binding.tvIp.text = it
        }
        viewModel.isThreadsLocked.observe(this) {
            binding.cardPostLocked.isGone = !it
        }
        viewModel.isThreadsJingTie.observe(this) {
            binding.cardJingTie.isGone = !it
        }
    }

    private fun changeAuthorFollowState() {
        binding.btnFollow.setOnClickListener { view ->
            if (viewModel.isFollowing.value == true) {
                Toast.makeText(
                    this@ThreadsActivity,
                    R.string.toast_operation,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            viewModel.followThreadsAuthor(
                onFailure = { code, body ->

                },
                onThrowable = {

                }
            )
        }
    }

    private fun updateAuthorFollowButton() {
        viewModel.isThreadsAuthorFollow.observe(this) {
            binding.chkFollow?.isChecked = it
            binding.btnFollow.background = AppCompatResources.getDrawable(
                this,
                if (it) R.drawable.uotan_btn_tonal
                else R.drawable.uotan_btn_filled
            )
            binding.loadingFollow.setColorFilter(
                if (it) colorOnFilledTonalButton else colorOnFilledButton
            )
            binding.tvFollow.text = getString(if (it) R.string.following else R.string.follow)
            binding.tvFollow.setTextColor(
                if (it) colorOnFilledTonalButton else colorOnFilledButton
            )
        }
        viewModel.isFollowing.observe(this) {
            binding.loadingFollow.isVisible = it
            val followAnim = loadingAnim.loadingAnim(binding.loadingFollow)
            val oldTextColor = binding.tvFollow.textColors
            if (it) {
                binding.tvFollow.setTextColor(TRANSPARENT)
            } else {
                binding.tvFollow.setTextColor(oldTextColor)
            }
            if (it) followAnim?.start() else followAnim?.cancel()
            binding.loadingFollow
        }
    }

    /**
     * 跳转评论
     */
    private fun scrollToComment() {
        binding.btnPost.setOnClickListener {
            binding.nsvRoot?.smoothScrollTo(0, binding.tvPosts.top)
        }
    }

    /**
     * 分享主题
     */
    private fun showShareDialog(url: String) {
        binding.btnShare.setOnClickListener {
            val share = Intent.createChooser(
                Intent().apply {
                    setAction(Intent.ACTION_SEND)
                    setType("text/plain")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "【${viewModel.threadInfo.value?.title} - ${getString(R.string.app_name)}】 ${baseUrl + url}"
                    )
                },
                null
            )
            startActivity(share)
        }
    }

    /**
     * 更多功能菜单
     */
    private fun showMoreDialog(url: String) {
        binding.btnMore.setOnClickListener {
            BottomDialog.show(object : OnBindView<BottomDialog?>(R.layout.dialogx_article_more) {
                override fun onBind(dialog: BottomDialog?, v: View?) {
                    val btnClose = v?.findViewById<View>(R.id.close)
                    btnClose?.setOnClickListener {
                        dialog?.dismiss()
                    }
                    copyThreadLink(v, dialog, url)
                    showReportDialog(v, dialog)
                    editThreads(v, dialog, url)
                    showIpDialog(v, dialog)
                }
            }).setMaxWidth(560f.dpToPx(this).roundToInt())
        }
    }

    /**
     * 复制主题链接
     */
    private fun copyThreadLink(v: View?, dialog: BottomDialog?, url: String) {
        val btnCopyLink = v?.findViewById<View>(R.id.btn_copy_link)
        btnCopyLink?.setOnClickListener {
            // 获取 ClipboardManager 实例
            val clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            // 创建 ClipData 对象（带标签和内容）
            val clipData = ClipData.newPlainText(viewModel.threadInfo.value?.title, baseUrl + url)
            // 设置到系统剪贴板
            clipboard.setPrimaryClip(clipData)
            Toast.makeText(this, R.string.link_copied_clipboard, Toast.LENGTH_SHORT).show()
            dialog?.dismiss()
        }
    }

    /**
     * 举报主题
     */
    private fun showReportDialog(v: View?, dialog: BottomDialog?) {
        val btnReport = v?.findViewById<View>(R.id.btn_report)
        val postId = viewModel.threadPost.value?.postID ?:"0"
        btnReport?.setOnClickListener {
            dialog?.dismiss()
            openUrlInBrowser(this, "$baseUrl/posts/$postId/report")
        }
    }

    /**
     * 编辑主题
     */
    private fun editThreads(v: View?, dialog: BottomDialog?, url: String) {
        val btnEdit = v?.findViewById<View>(R.id.btn_edit)
        btnEdit?.setOnClickListener {
            dialog?.dismiss()
            openUrlInBrowser(this, "$baseUrl$url/edit")
        }
    }

    private fun showIpDialog(v: View?, dialog: BottomDialog?) {
        val btnIp = v?.findViewById<View>(R.id.btn_ip)
        val postId = viewModel.threadPost.value?.postID ?:"0"
        btnIp?.setOnClickListener {
            dialog?.dismiss()
            loadingDialog.show()
            viewModel.fetchIp(
                postId = postId.toString(),
                onSuccess = {
                    loadingDialog.cancel()
                    val infoDialog = InfoDialog(this).apply {
                        setTitle(getString(R.string.ip_location))
                        setDescription(it)
                        setCancelText(getString(R.string.cancel))
                        setConfirmText(getString(R.string.ok))
                    }
                    infoDialog.withOnCancel { infoDialog.cancel() }
                        .withOnConfirm { infoDialog.cancel() }
                        .show()
                },
                onThrowable = {
                    errorDialog(this, "ERROR", it.message)
                }
            )
        }
    }

    /**
     * 初始化图片查看器（在 obverse 到 thread 发生变化）
     * @param attachmentList API 获取到的原始全部图片数据
     */
    private fun setupPictureViewer(attachmentList: List<Attachment>?) {
        pictureList.clear()
        attachmentList?.forEachIndexed { index, attachment ->
            pictureList.add(ThreadPhoto(attachment.directURL, index.toLong()))
        }
    }

    /**
     * 启动图片查看器
     * @param position 图片索引 用于绑定动画
     * @param url 图片链接
     */
    private fun showPictureViewer(position: Long, url: String) {
        val clickedData: Photo = ThreadPhoto(url, position)
        val builder = ImageViewerBuilder(
            context = this,
            dataProvider = SimpleDataProvider(clickedData, pictureList),        // 一次性全量加载
            imageLoader = ImageLoader(),                                        // 实现对数据源的加载
            transformer = ImageTransformer(),                                   // 设置过渡动画的配对
        )
        builder.show()
    }

    /**
     * 浏览器打开链接
     */
    private fun openUrlInBrowser(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = url.toUri()
            // 确保在新任务栈打开
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun back() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

}
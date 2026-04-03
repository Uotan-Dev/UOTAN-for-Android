package com.uotan.forum.resource.ui.details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.uotan.forum.BaseActivity
import com.uotan.forum.R
import com.uotan.forum.ui.anim.LoadingAnim
import com.uotan.forum.databinding.ActivityResourceBinding
import com.uotan.forum.databinding.DialogxAddResourceReplyBinding
import com.uotan.forum.databinding.DialogxArticleMoreBinding
import com.uotan.forum.databinding.DialogxDonationDownloadBinding
import com.uotan.forum.databinding.DialogxSelectDownloadTypeBinding
import com.uotan.forum.ui.dialog.LoadingDialog
import com.uotan.forum.resource.data.model.PurchaseData
import com.uotan.forum.resource.ui.details.adapter.ResPagerAdapter
import com.uotan.forum.resource.ui.details.model.ResourceBuyNavEvent
import com.uotan.forum.resource.ui.details.model.ResourceBuyUiState
import com.uotan.forum.user.ui.UserActivity
import com.uotan.forum.ui.activity.resource.adapter.NewResourceTypeAdapter
import com.uotan.forum.utils.Helpers.avatarOptions
import com.uotan.forum.utils.Utils.baseUrl
import com.uotan.forum.utils.Utils.dpToPx
import com.uotan.forum.utils.Utils.errorDialog
import com.uotan.forum.utils.Utils.getThemeColor
import com.uotan.forum.utils.Utils.openUrlInBrowser
import com.uotan.forum.utils.Utils.showToast
import com.imxie.exvpbs.BottomSheetVP2Helper
import com.imxie.exvpbs.ViewPagerBottomSheetBehavior
import com.kongzue.dialogx.dialogs.BottomDialog
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback
import com.kongzue.dialogx.interfaces.OnBindView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@AndroidEntryPoint
class ResourceActivity : BaseActivity() {

    private val viewModel by viewModels<ResDetailsViewModel>()
    private val loadingAnim = LoadingAnim()
    private lateinit var binding: ActivityResourceBinding
    private lateinit var pagerAdapter: ResPagerAdapter
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResourceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initAndOnClick()
    }

    private fun initAndOnClick() {
        initWindowInsets()
        initResPager()
        initResDetails()
        onTopBarBtnClick()
        onDownloadBtnClick()
        onBottomBarBtnClick()
        obverseAndUpdate()
    }

    private fun initWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.headerBarLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            binding.layoutInfo.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = (systemBars.top + 60f.dpToPx(this@ResourceActivity))
                    .roundToInt()
            }
            binding.shadowResource
                .setPadding(systemBars.left,
                    (systemBars.top + 60f.dpToPx(this)).roundToInt(),
                    systemBars.right, 0)
            binding.layoutBottomBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom
            }
            val displayHeight = binding.main.height
            val infoHeight = binding.layoutInfo.height
            val peekHeight = displayHeight - infoHeight
            val behavior = ViewPagerBottomSheetBehavior.from(binding.shadowResource)
            behavior.setPeekHeight(peekHeight)
            BottomSheetVP2Helper.setupViewPager(binding.pagerRes)
            insets
        }
    }

    private fun initResPager() {
        pagerAdapter = ResPagerAdapter(this)
        binding.pagerRes.adapter = pagerAdapter
        TabLayoutMediator(
            binding.tabRes, binding.pagerRes
        ) { tab, position ->
            when(position) {
                0 -> tab.text = getString(R.string.res_details)
                1 -> tab.text = getString(R.string.reply)
            }
        }.attach()
    }

    private fun initResDetails() {
        loadingDialog = LoadingDialog(this)
        // 取 intent 中的 url
        val url = intent.getStringExtra("url") ?: ""
        if (url.isEmpty()) {
            errorDialog(
                this, getString(R.string.intent_error),
                getString(R.string.intent_error_dialog)
            )
        }
        viewModel.loadInitialDetails(
            isRefresh = false,
            resShortUrl = url,
            onThrowable = {
                errorDialog(this@ResourceActivity, it.javaClass.name,
                    it.message ?: "")
            }
        )
    }

    private fun obverseAndUpdate() {
        updateAuthorUi()
        updateBookMarkUi()
        updateResDetails()
        updateOnBuyUi()
        updateReplyUi()
        updateReactUi()
    }

    private fun updateAuthorUi() {
        val loading = loadingAnim.loadingAnim(binding.loadingFollow)
        val colorOnFilledButton = getThemeColor(
            this, R.attr.colorOnFilledButton)
        val colorOnFilledTonalButton = getThemeColor(
            this, R.attr.colorOnFilledTonalButton)
        viewModel.isAuthorFollow.observe(this) {
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
        viewModel.isAuthorFollowing.observe(this) {
            binding.loadingFollow.isVisible = it
            val oldTextColor = binding.tvFollow.textColors
            if (it) {
                binding.tvFollow.setTextColor(TRANSPARENT)
            } else {
                binding.tvFollow.setTextColor(oldTextColor)
            }
            if (it) loading?.start() else loading?.cancel()
            binding.loadingFollow
        }
    }

    private fun updateBookMarkUi() {
        val loading = loadingAnim.loadingAnim(binding.btnCollect)
        viewModel.isBook.observe(this) {
            binding.btnCollect.setImageResource(
                if (it) R.drawable.ic_option_collect else R.drawable.ic_collect
            )
        }
        viewModel.isBooking.observe(this) {
            val oldImg = binding.btnCollect.drawable
            if (it) {
                binding.btnCollect.background = null
                binding.btnCollect.setImageResource(R.drawable.ic_loading)
                loading?.start()
            } else {
                loading?.end()
                binding.btnCollect.background = AppCompatResources
                    .getDrawable(this, R.drawable.gustatex_btn_none_bkg)
                binding.btnCollect.setImageDrawable(oldImg)
            }
        }
    }

    private fun updateResDetails() {
        viewModel.details.observe(this) {
            // 作者信息
            Glide.with(this)
                .load(baseUrl + it.authorAvatar)
                .apply(avatarOptions)
                .into(binding.imgAvatar)
            binding.tvUsername.text = it.author
            binding.tvTime.text = it.latestPost
            // 资源信息
            Glide.with(this)
                .load(baseUrl + it.cover)
                .error(R.drawable.ic_uo)
                .into(binding.imgCover)
            binding.tvTitle.text = it.title
            binding.tvDevice.text = it.device
            binding.tvChannel.text = it.downloadType
            binding.tvSize.text = it.size
            binding.tvLast.text = it.latestPost
            binding.tvReactCount.text = it.numberOfLikes
        }
    }

    private fun updateReplyUi() {
        val loading = LoadingDialog(this)
        viewModel.isReplying.observe(this) {
            if (it) loading.show()
            else loading.dismiss()
        }
    }

    private fun updateReactUi() {
        val loading = loadingAnim.loadingAnim(binding.btnReact)
        viewModel.isReact.observe(this) {
            binding.btnReact.setImageResource(
                if (it) R.drawable.ic_reacted else R.drawable.ic_favourite
            )
        }
        viewModel.reactCount.observe(this) {
            binding.tvReactCount.text = it.toString()
        }
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
    }

    private fun updateOnBuyUi() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ResourceBuyUiState.Idle -> {
                        loadingDialog.dismiss()
                    }
                    is ResourceBuyUiState.Loading -> {
                        loadingDialog.show()
                    }
                    is ResourceBuyUiState.ShowNewResourceBuyDialog -> {
                        showNewResourceDialog(state.purchaseData)
                    }
                    is ResourceBuyUiState.ShowOldResourceBuyDialog -> {
                        showOldResourceDialog(state.purchaseData)
                    }
                    is ResourceBuyUiState.Error -> {
                        errorDialog(
                            this@ResourceActivity,
                            getString(state.title),
                            state.message)
                        viewModel.onErrorHandled()
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewModel.navigationEvent.collect { event ->
                when (event) {
                    is ResourceBuyNavEvent.OpenUrlInBrowser -> {
                        openUrlInBrowser(this@ResourceActivity, event.url)
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewModel.toastEvent.collect { messageRes ->
                showToast(this@ResourceActivity, messageRes)
            }
        }
    }

    private fun onTopBarBtnClick() {
        onBackBtnClick()
        onAuthorLayoutClick()
        onAuthorFollowBtnClick()
        onBookMarkBtnClick()
    }

    private fun onBottomBarBtnClick() {
        onReplyBtnClick()
        onReactBtnClick()
        onShareBtnClick()
        onMoreBtnClick()
    }

    private fun onBackBtnClick() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun onAuthorLayoutClick() {
        binding.layoutUser.setOnClickListener {
            startActivity(
                Intent(this, UserActivity::class.java)
                    .putExtra("url", viewModel.url.value ?: "")
            )
        }
    }

    private fun onAuthorFollowBtnClick() {
        binding.btnFollow.setOnClickListener {
            viewModel.onAuthorFollow(
                onFailure = { code, body ->
                    errorDialog(this, code.toString(), body)
                },
                onException = {
                    errorDialog(this, it.javaClass.name,
                        it.message ?: "")
                }
            )
        }
    }

    // 触发下载资源（例如按钮点击事件）
    private fun onDownloadBtnClick() {
        binding.btnDownload.setOnClickListener {
            viewModel.downloadResource(viewModel.details.value?.downloadUrl?:"")
        }
    }

    private fun showNewResourceDialog(purchaseData: MutableList<PurchaseData>) {
        val bindingSdt = DialogxSelectDownloadTypeBinding.inflate(layoutInflater)
        BottomDialog.show(object : OnBindView<BottomDialog>(bindingSdt.root) {
            override fun onBind(dialog: BottomDialog, view: View) {
                loadingDialog.dismiss()
                bindingSdt.recyclerView.layoutManager = LinearLayoutManager(this@ResourceActivity)
                bindingSdt.recyclerView.adapter = NewResourceTypeAdapter().apply {
                    onItemClick = {
                        viewModel.onNewResourceSelected(viewModel.details.value?.downloadUrl?:"", it, true)
                        dialog.dismiss()
                    }
                }
                val adapter = bindingSdt.recyclerView.adapter as NewResourceTypeAdapter
                adapter.addAll(purchaseData)
                bindingSdt.close.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.setDialogLifecycleCallback(object :
                    DialogLifecycleCallback<BottomDialog>() {
                        override fun onDismiss(dialog: BottomDialog?) {
                            super.onDismiss(dialog)
                            viewModel.onDialogDismissed()
                        }
                    }
                )
            }
        })
    }

    private fun showOldResourceDialog(purchaseData: MutableList<PurchaseData>) {
        val bindingDdb = DialogxDonationDownloadBinding.inflate(layoutInflater)
        BottomDialog.show(object : OnBindView<BottomDialog>(bindingDdb.root) {
            override fun onBind(dialog: BottomDialog, v: View) {
                loadingDialog.dismiss()
                bindingDdb.tvPrice.text = getString(R.string.res_price, purchaseData.first().price)
                bindingDdb.btnDonate.setOnClickListener {
                    viewModel.onOldResourceDonate(viewModel.details.value?.downloadUrl?:"",purchaseData.first())
                    dialog.dismiss()
                }
                dialog.setDialogLifecycleCallback(
                    object : DialogLifecycleCallback<BottomDialog>() {
                        override fun onDismiss(dialog: BottomDialog?) {
                            super.onDismiss(dialog)
                            viewModel.onDialogDismissed()
                        }
                    }
                )
            }
        })
    }

    private fun onBookMarkBtnClick() {
        binding.btnCollect.setOnClickListener {
            viewModel.onBookMark(
                onFailure = { code, body ->
                    errorDialog(this, code.toString(), body)
                },
                onException = {
                    errorDialog(this, it.javaClass.name,
                        it.message ?: "")
                }
            )
        }
    }

    private fun onReplyBtnClick() {
        binding.btnReply.setOnClickListener {
            val bindingArr = DialogxAddResourceReplyBinding.inflate(layoutInflater)
            BottomDialog.show(object : OnBindView<BottomDialog>(bindingArr.root) {
                override fun onBind(
                    dialog: BottomDialog,
                    v: View
                ) {
                    bindingArr.close.setOnClickListener { dialog.dismiss() }
                    bindingArr.btnPost.setOnClickListener {
                        if (bindingArr.edtContent.text.isEmpty()) {
                            showToast(this@ResourceActivity, "content is empty")
                        } else {
                            viewModel.onReply(
                                rating = bindingArr.rtRes.rating.toString(),
                                message = bindingArr.edtContent.text.toString(),
                                onFailure = { code, body ->
                                    errorDialog(this@ResourceActivity, code.toString(), body)
                                }, onSuccess = {
                                    dialog.dismiss()
                                    showToast(this@ResourceActivity,
                                        R.string.published_successfully)
                                }, onException = {
                                    errorDialog(this@ResourceActivity, it.javaClass.name,
                                        it.message ?: "")
                                }
                            )
                        }
                    }
                }
            })
        }
    }

    private fun onReactBtnClick() {
        binding.btnReact.setOnClickListener {
            viewModel.onReact(
                onFailure = { code, body ->
                    errorDialog(this, code.toString(), body)
                },
                onException = {
                    errorDialog(this, it.javaClass.name,
                        it.message ?: "")
                }
            )
        }
    }

    /**
     * 分享资源
     */
    private fun onShareBtnClick() {
        binding.btnShare.setOnClickListener {
            val title = viewModel.details.value?.title ?: "就连你也配直视我？！"
            val url = viewModel.url.value ?: "伟力如此，皆为神诞！"
            val share = Intent.createChooser(
                Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "【$title - " + getString(R.string.app_name) +
                                "${getString(R.string.resource)}】 " + (baseUrl + url)
                    )
                },
                null
            )
            startActivity(share)
        }
    }

    private fun onMoreBtnClick() {
        val bindingMore = DialogxArticleMoreBinding.inflate(layoutInflater)
        binding.btnMore.setOnClickListener {
            BottomDialog.show(
                object : OnBindView<BottomDialog>(bindingMore.root) {
                    override fun onBind(dialog: BottomDialog, view: View) {
                        bindingMore.btnClose.setOnClickListener { dialog.dismiss() }
                        onClipBtnClick(bindingMore, dialog)
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
                }
            )
        }
    }

    private fun onClipBtnClick(
        bindingMore: DialogxArticleMoreBinding,
        dialog: BottomDialog
    ) {
        bindingMore.btnCopyLink.setOnClickListener {
            val url = viewModel.url.value ?: "圣龙烈焰，将照彻汝等腐朽！"
            val clipboard = this@ResourceActivity
                .getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(title, baseUrl + url)
            clipboard.setPrimaryClip(clipData)
            showToast(
                this@ResourceActivity, R.string.link_copied_clipboard
            )
            dialog.dismiss()
        }
    }
}
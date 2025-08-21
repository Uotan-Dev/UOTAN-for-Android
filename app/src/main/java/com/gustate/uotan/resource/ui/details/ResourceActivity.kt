package com.gustate.uotan.resource.ui.details

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.databinding.ActivityResourceBinding
import com.gustate.uotan.resource.ui.details.adapter.ResPagerAdapter
import com.gustate.uotan.utils.Helpers.Companion.avatarOptions
import com.gustate.uotan.utils.Utils.Companion.baseUrl
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.errorDialog
import kotlin.math.roundToInt

class ResourceActivity : BaseActivity() {

    private val viewModel by viewModels<ResDetailsViewModel>()

    /* 延迟启动 */
    // 视图绑定
    private lateinit var binding: ActivityResourceBinding

    private lateinit var pagerAdapter: ResPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResourceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pagerAdapter = ResPagerAdapter(this)

        binding.pagerRes.adapter = pagerAdapter

        TabLayoutMediator(binding.tabRes, binding.pagerRes) { tab, position ->
            when(position) {
                0 -> tab.text = getString(R.string.res_details)
                1 -> tab.text = getString(R.string.reply)
                //2 -> tab.text = getString(R.string.historical_version)
            }
        }.attach()

        val url = intent.getStringExtra("url") ?: ""
        if (url.isEmpty()) {
            errorDialog(
                this, getString(R.string.intent_error),
                getString(R.string.intent_error_dialog)
            )
        }

        /*
         * 修改各个占位布局的高度
         * 以实现小白条与状态栏的沉浸
         */
        // 使用 ViewCompat 类设置一个窗口监听器（这是一个回调函数），需要传入当前页面的根布局
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            // systemBars 是一个 insets 对象
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 设置状态栏占位布局高度
            binding.headerBarLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            binding.layoutInfo.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = (systemBars.top + 60f.dpToPx(this@ResourceActivity)).roundToInt()
            }
            // 修改滚动布局的边距
            binding.nsvResource
                .setPadding(
                    systemBars.left,
                    (systemBars.top + 60f.dpToPx(this)).roundToInt(),
                    systemBars.right,
                    0
                )
            // 设置小白条占位布局高度
            binding.layoutBottomBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom
            }

            val displayHeight = binding.main.height
            val infoHeight = binding.layoutInfo.height
            val peekHeight = displayHeight - infoHeight

            val bottomSheetBehavior = BottomSheetBehavior.from(binding.nsvResource)
            bottomSheetBehavior.setPeekHeight(peekHeight, true)

            insets
        }
        viewModel.loadInitialDetails(
            isRefresh = false,
            resShortUrl = url,
            onThrowable = {

            }
        )
        obverseDetails()
    }

    private fun obverseDetails() {
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
}
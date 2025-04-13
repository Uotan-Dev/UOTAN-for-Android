package com.gustate.uotan.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.databinding.ActivityResourceBinding
import com.gustate.uotan.gustatex.dialog.LoadingDialog
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.openImmersion
import com.gustate.uotan.utils.parse.resource.ResourceArticleParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class ResourceActivity : BaseActivity() {

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val binding = ActivityResourceBinding.inflate(layoutInflater)
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
                    (systemBars.top + 60f.dpToPx(this)).roundToInt(),
                    systemBars.right,
                    (systemBars.top + 70f.dpToPx(this)).roundToInt()
                )
            // 设置小白条占位布局高度
            binding.gestureView.layoutParams.height = systemBars.bottom
            // 返回 insets
            insets
        }

        val url = intent
            .getStringExtra("url")
            ?: ""

        lifecycleScope.launch {
            val content = ResourceArticleParse.fetchResourceArticle(url)
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
            }
        }
    }
}
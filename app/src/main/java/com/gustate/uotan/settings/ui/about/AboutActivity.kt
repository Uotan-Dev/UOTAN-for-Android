package com.gustate.uotan.settings.ui.about

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.databinding.ActivityAboutBinding
import com.gustate.uotan.user.ui.UserActivity
import com.gustate.uotan.utils.Utils
import com.gustate.uotan.utils.Utils.dpToPx
import kotlin.math.roundToInt

/**
 * 关于页面 (Activity)
 * JiaGuZhuangZhi Miles
 * Gustate 02/23/2025
 */

class AboutActivity : BaseActivity() {

    /** 全类变量 **/
    // 初始化视图绑定
    private lateinit var binding: ActivityAboutBinding
    private val viewModel by viewModels<AboutViewModel>()

    /**
     * 视图被创建
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** 窗体设置 **/
        // 实例化 binding
        binding = ActivityAboutBinding.inflate(layoutInflater)
        // 绑定视图
        setContentView(binding.main)

        /** 获取系统栏高度并同步到占位布局 **/
        // 使用 ViewCompat 的回调函数
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            // 获取系统栏高度 (包含 top, bottom, left 和 right)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val headerBarHeight = systemBars.top + 60f.dpToPx(this).roundToInt()
            binding.layoutHeaderBar.setSystemBarsPadding(
                nIsYieldStatusBar = true, nStatusBarHeight = systemBars.top,
                nLeftSystemBarWidth = systemBars.left, nRightSystemBarWidth = systemBars.right)
            binding.layoutIcon.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = 100f.dpToPx(this@AboutActivity).roundToInt() + headerBarHeight
            }
            binding.nsvRoot.setPadding(systemBars.left, headerBarHeight,
                systemBars.right, systemBars.bottom)
            binding.rootView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = 300f.dpToPx(this@AboutActivity).roundToInt() + systemBars.top
            }
            insets
        }

        setOnScrollChangeListener()
        binding.tvVersion.text = Utils.getVersionName(this)
        binding.odvVersion.detail = Utils.getVersionCode(this)

        /** 设置监听 **/
        openUrl(binding.odvWeb, "https://www.uotan.cn/")
        openUser(binding.ouvMhym, "/members/2/")
        openUser(binding.ouvLn, "/members/47/")
        openUser(binding.ouvMjw, "/members/256/")
        openUser(binding.ouvYuzh, "/members/yuzh.2414/")
        openUser(binding.ouvLemo, "/members/lemo.1042/")
        openUser(binding.ouvXh, "/members/779/")
        openUser(binding.ouvZach, "/members/zach.1219/")
        openUser(binding.ouvHy, "/members/haoyang.2377/")
        openUrl(binding.ouvRayzggz, "https://www.uotan.cn/pages/about/")
        openUser(binding.ouvGustate, "/members/3059/")
        openUrl(binding.otdvOpen, "https://github.com/Uotan-Dev/UOTAN-for-Android/")

        binding.layoutHeaderBar.setOnLeftBtnClickListener {
            finish()
        }

    }

    private fun openUrl(view: View, url: String) {
        view.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        }
    }

    private fun openUser(view: View, url: String) {
        view.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    UserActivity::class.java
                ).putExtra("url", url)
            )
        }
    }

    private fun setOnScrollChangeListener() {
        updateUIBasedOnProgress(viewModel.scrollProgress.value ?: 0f)
        binding.nsvRoot.setOnScrollChangeListener { _, _, _, _, _ ->
            // 计算目标视图相对于滚动容器的位置
            val progress = calculateScrollProgress()
            updateUIBasedOnProgress(progress)
        }
    }

    /**
     * 计算目标视图的滚动进度，考虑padding的影响
     * @return 0f 到 1f 之间的进度值
     */
    private fun calculateScrollProgress(): Float {
        // 计算目标视图在滚动容器中的实际位置（考虑padding）
        val viewPosition = IntArray(2)
        binding.rootView.getLocationInWindow(viewPosition)
        // 获取滚动容器在窗口中的位置
        val scrollViewPosition = IntArray(2)
        binding.nsvRoot.getLocationInWindow(scrollViewPosition)
        // 计算目标视图顶部相对于滚动容器内容区域顶部的位置
        val viewTopRelative = viewPosition[1] - scrollViewPosition[1] - binding.nsvRoot.paddingTop
        // 计算进度值：当视图完全到达顶部时为1，完全在屏幕外时为0
        val progress = when {
            viewTopRelative <= 0 -> 1f // 视图已到达或超过顶部
            viewTopRelative >= binding.rootView.marginTop -> 0f // 视图完全在屏幕外
            else -> 1f - (viewTopRelative.toFloat() / binding.rootView.marginTop)
        }
        // 确保进度值在0到1之间
        return progress.coerceIn(0f, 1.0f)
    }

    /**
     * 根据滚动进度更新UI
     * @param progress 当前的滚动进度值
     */
    private fun updateUIBasedOnProgress(progress: Float) {
        viewModel.updateScrollProgress(progress)
        val cardAlpha = ((progress + 0.48) * 256).toInt()
        binding.layoutInfo.background.apply { alpha = cardAlpha }
        binding.layoutApp.background.apply { alpha = cardAlpha }
        binding.bkgEffect.alpha = 1 - progress
        if (progress > 0.8) {
            binding.layoutHeaderBar.updateTitleVisible(
                visible = true, anim = true, duration = 180)
        } else {
            binding.layoutHeaderBar.updateTitleVisible(
                visible = false, anim = true, duration = 180)
        }
        binding.layoutHeaderBar.updateHeaderBlurAlpha { of ->
            if (progress == 1f) 1f else 0f
        }
    }
}
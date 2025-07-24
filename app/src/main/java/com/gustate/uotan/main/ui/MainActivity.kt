package com.gustate.uotan.main.ui

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.databinding.ActivityMainBinding
import com.gustate.uotan.ui.activity.SettingsActivity
import com.gustate.uotan.utils.Helpers.Companion.avatarOptions
import com.gustate.uotan.utils.Utils
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.errorDialog
import com.gustate.uotan.utils.Utils.Companion.getThemeColor
import com.gustate.uotan.utils.Utils.Companion.idToAvatar
import com.gustate.uotan.utils.room.UserViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 主页面 (Activity)
 */

class MainActivity : BaseActivity() {

    /** 可变变量 **/
    // 延迟初始化视图绑定
    private lateinit var binding: ActivityMainBinding
    // 延迟初始化 MainPagerAdapter
    private lateinit var adapter: MainPagerAdapter
    // 取 MainViewModel
    private val viewModel: MainViewModel by viewModels()
    private val userViewModel by viewModels<UserViewModel>()

    /**
     * 视图被创建
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 实例化 binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        // 绑定视图
        setContentView(binding.main)
        // 基础设置
        setWindow()

        // 底栏按钮默认颜色
        val navNormalColor = getThemeColor(this, R.attr.colorOnBackgroundSecondary)
        // 底栏按钮选择颜色
        val navSelectedColor = getThemeColor(this, R.attr.colorOnBackgroundPrimary)

        /** 设置适配器 **/
        // 实例化 MainViewPagerAdapter()
        adapter = MainPagerAdapter(this)
        binding.pagerMain.adapter = adapter
        binding.pagerMain.isSaveEnabled = false

        /** 设置组件 **/
        // 关闭 viewPager 用户滑动
        binding.pagerMain.isUserInputEnabled = false

        binding.imgAvatar?.let {
            lifecycleScope.launch {
                val userInfo = userViewModel.getUser()
                Glide.with(this@MainActivity).load(idToAvatar(userInfo?.userId?:"0")).apply(avatarOptions).into(it)
                binding.tvUsername?.text = userInfo?.userName?: ""
            }
        }

        binding.btnSettings?.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SettingsActivity::class.java
                )
            )
        }


        viewModel.loadInitial(
            { },
            { errorDialog(this, "ERROR", it.message) }
        )

        viewModel.pagerPage.observe(this) {
            // change page
            binding.pagerMain.setCurrentItem(it, false)
            // Phone
            // change tv
            val texts = arrayOf(binding.tvHome, binding.tvSection, binding.tvMessage, binding.tvRes,
                binding.tvMine)
            texts.forEachIndexed { index, textView ->
                textView?.setTextColor(if (index == it) navSelectedColor else navNormalColor)
            }
            // change ico
            val icons = arrayOf(binding.imgHome to R.drawable.ic_nav_home,
                binding.imgSection to R.drawable.ic_nav_plate,
                binding.imgMessage to R.drawable.ic_nav_notice,
                binding.imgRes to R.drawable.ic_nav_res, binding.imgMine to R.drawable.ic_nav_me)
            val selectedIcons = arrayOf(R.drawable.ic_nav_home_selected,
                R.drawable.ic_nav_plate_selected, R.drawable.ic_nav_notice_selected,
                R.drawable.ic_nav_res_selected, R.drawable.ic_nav_me_selected)
            icons.forEachIndexed { index, (icon, defaultResId) ->
                icon?.setImageDrawable(AppCompatResources.getDrawable(this@MainActivity,
                    if (index == it) selectedIcons[index]
                    else defaultResId))
            }
            // Pad
            val tabs = arrayOf(binding.homeTabPad, binding.plateTabPad, binding.noticeTabPad,
                binding.resTabPad, binding.mineTabPad)
            tabs.forEachIndexed { index, tab ->
                tab?.setBackgroundResource(
                    if (index == it) R.color.color_nav_option_check
                    else R.color.color_nav_option_uncheck
                )
            }
        }

        listOf(binding.tabHome, binding.tabSection, binding.tabMessage, binding.tabRes,
            binding.tabMine).forEachIndexed { index, layout ->
                layout?.setOnClickListener { viewModel.pager(index) }
        }

        listOf(binding.homeTabPad, binding.plateTabPad, binding.noticeTabPad, binding.resTabPad,
            binding.mineTabPad).forEachIndexed { index, layout ->
                layout?.setOnClickListener { viewModel.pager(index) }
        }
    }

    /**
     * 窗体基本设置
     */
    private fun setWindow() {
        // 启用边到边设计
        enableEdgeToEdge()
        // 针对部分系统的系统栏沉浸
        Utils.Companion.openImmersion(window)
        // 使用 ViewCompat 的回调函数
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            // 获取系统栏高度 (包含 top, bottom, left 和 right)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Pad
            binding.imgUotan?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top + 36f.dpToPx(this@MainActivity).roundToInt()
            }
            binding.mineTabPad?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom + 6f.dpToPx(this@MainActivity).roundToInt()
            }
            // Phone
            binding.layoutNavBar?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom
            }
            // 返回 insets
            insets
        }
    }

}
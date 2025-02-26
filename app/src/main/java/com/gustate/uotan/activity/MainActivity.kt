package com.gustate.uotan.activity

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.gustate.uotan.R
import com.gustate.uotan.databinding.ActivityMainBinding
import com.gustate.uotan.fragment.home.HomeFragment
import com.gustate.uotan.fragment.notice.NoticeFragment
import com.gustate.uotan.fragment.plate.PlateFragment
import com.gustate.uotan.fragment.resource.ResFragment
import com.gustate.uotan.fragment.user.MeFragment
import com.gustate.uotan.utils.Utils.Companion.isLogin
import com.gustate.uotan.utils.Utils.Companion.openImmersion

/**
 * 主页面 (Activity)
 * JiaGuZhuangZhi Miles
 * Gustate 02/23/2025
 * I Love Jiang’Xun
 */

class MainActivity : AppCompatActivity() {

    /** 全类变量 **/
    // 初始化视图绑定
    private lateinit var binding: ActivityMainBinding

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
        binding = ActivityMainBinding.inflate(layoutInflater)
        // 绑定视图
        setContentView(binding.main)

        /** 常量设置 **/
        // 底栏按钮默认颜色
        val navColor = ContextCompat.getColor(this, R.color.label_tertiary)
        // 底栏按钮选择颜色
        val navSelectedColor = ContextCompat.getColor(this, R.color.label_primary)

        /** 获取系统栏高度并同步到占位布局 **/
        // 使用 ViewCompat 的回调函数
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            // 获取系统栏高度 (包含 top, bottom, left 和 right)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 同步导航栏占位布局高度
            binding.gestureView
                .updateLayoutParams<ViewGroup.LayoutParams> {
                    height = systemBars.bottom
                }
            // 返回 insets
            insets
        }

        /** 设置适配器 **/
        // 实例化 MainViewPagerAdapter()
        binding.viewPager.adapter = MainViewPagerAdapter(this)

        /** 设置组件 **/
        // 关闭 viewPager 用户滑动
        binding.viewPager.isUserInputEnabled = false

        /** 设置监听 **/
        // 为主页, 版块, 消息和资源 tabs 设置监听
        listOf(
            // 主页 tab
            binding.homeTab,
            // 版块 tab
            binding.plateTab,
            // 通知 tab
            binding.noticeTab,
            // 资源 tab
            binding.resTab
        ).forEachIndexed { index, layout ->
            layout.setOnClickListener {
                binding.viewPager.setCurrentItem(index, true)
            }
        }

        // 个人中心单独处理登录逻辑
        binding.meTab.setOnClickListener {
            if (isLogin) binding.viewPager.setCurrentItem(4, true)
            else startActivity(Intent(baseContext, LoginActivity::class.java))
        }


        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

                val texts = arrayOf(binding.homeText, binding.plateText, binding.noticeText, binding.resText, binding.meText)

                texts.forEachIndexed { index, textView ->
                    textView.setTextColor(if (index == position) navSelectedColor else navColor)
                }

                val icons = arrayOf(
                    binding.homeIcon to R.drawable.ic_nav_home,
                    binding.plateIcon to R.drawable.ic_nav_plate,
                    binding.noticeIcon to R.drawable.ic_nav_notice,
                    binding.resIcon to R.drawable.ic_nav_res,
                    binding.meIcon to R.drawable.ic_nav_me
                )

                val selectedIcons = arrayOf(
                    R.drawable.ic_nav_home_selected,
                    R.drawable.ic_nav_plate_selected,
                    R.drawable.ic_nav_notice_selected,
                    R.drawable.ic_nav_res_selected,
                    R.drawable.ic_nav_me_selected
                )

                icons.forEachIndexed { index, (icon, defaultResId) ->
                    icon.setImageDrawable(AppCompatResources
                        .getDrawable(
                            baseContext,
                            if (index == position) selectedIcons[index]
                            else defaultResId
                        )
                    )
                }

            }
        })

    }

}

class MainViewPagerAdapter(fragmentActivity: FragmentActivity):
    FragmentStateAdapter(fragmentActivity){
    private val fragments = listOf(
        HomeFragment(),
        PlateFragment(),
        NoticeFragment(),
        ResFragment(),
        MeFragment()
    )
    override fun getItemCount(): Int = fragments.size
    override fun createFragment(position: Int): Fragment = fragments[position]
}

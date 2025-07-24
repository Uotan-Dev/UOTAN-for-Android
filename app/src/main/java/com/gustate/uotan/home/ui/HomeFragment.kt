package com.gustate.uotan.home.ui

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.gustate.uotan.R
import com.gustate.uotan.SearchActivity
import com.gustate.uotan.databinding.FragmentHomeBinding
import com.gustate.uotan.home.ui.adapter.HomeViewPagerAdapter
import com.gustate.uotan.ui.activity.PostArticleActivity
import com.gustate.uotan.utils.Utils.Companion.getThemeColor

class HomeFragment : Fragment() {

    // HomeFragment 私有变量
    // 可空的视图绑定，请在 Create 时初始化，Destroy 时置空
    private var _binding: FragmentHomeBinding? = null
    // 对视图绑定的非空访问，仅在生命周期有效时可用
    private val binding: FragmentHomeBinding get() = _binding!!
    // 在 Activity 的生命周期中取 ViewModel
    private val viewModel by activityViewModels<HomeViewModel>()

    /**
     * 实例化用户界面视图
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 初始化视图绑定
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * 实例化用户界面视图后
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 修改状态栏和底栏占位布局的高度
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Phone
            binding.layoutHeaderBar?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            // Pad
            binding.layoutBottomBar?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom
            }
            insets
        }
        obversePager()
        val blurOverlay = (128 shl 24) or (getThemeColor(requireContext(), R.attr.colorBackground) and 0x00FFFFFF)
        binding.blurHeaderBar?.setupWith(binding.root)?.setOverlayColor(blurOverlay)?.setBlurRadius(18f)?.setBlurEnabled(true)?.setBlurAutoUpdate(false)?.setFrameClearDrawable(activity?.window?.decorView?.background)
        binding.pagerHome.adapter = HomeViewPagerAdapter(this)
        binding.pagerHome.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.changePager(position)
            }
        })
        viewModel.loadInitialPager()
        binding.tabLatest.setOnClickListener{ viewModel.changePager(0) }
        binding.tabRecommend.setOnClickListener{ viewModel.changePager(1) }
        binding.layoutPost.setOnClickListener {
            startActivity(Intent(requireContext(), PostArticleActivity::class.java))
        }
        binding.layoutSearch.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }
        val searchBkg = binding.layoutSearch.background
        searchBkg.alpha = 146
        binding.layoutSearch.background = searchBkg

        if (binding.layoutTabs.tag != "phone") {
            val tabsBkg = binding.layoutTabs.background
            tabsBkg.alpha = 146
            binding.layoutTabs.background = tabsBkg
        }

        val postBkg = binding.layoutPost.background
        postBkg.alpha = 146
        binding.layoutPost.background = postBkg
    }

    /**
     * 监听 Pager
     */
    private fun obversePager() {
        viewModel.pagerPage.observe(viewLifecycleOwner) {
            val normalColor = getThemeColor(requireContext(), R.attr.colorOnBackgroundSecondary)
            val selectedColor = getThemeColor(requireContext(), R.attr.colorOnBackgroundPrimary)
            binding.pagerHome.setCurrentItem(it, true)
            updateTabStyle(it, normalColor, selectedColor)
        }
    }

    /**
     * 更新 Tab 样式
     * @param position 页码
     * @param normalColor 未选中颜色
     * @param selectedColor 选中颜色
     */
    private fun updateTabStyle(position: Int, normalColor: Int, selectedColor: Int) {
        when (position) {
            0 -> {
                binding.tabLatest.setTextColor(selectedColor)
                binding.tabLatest.typeface = Typeface.create("sans-serif-bold", Typeface.BOLD)
                binding.tabRecommend.setTextColor(normalColor)
                binding.tabRecommend.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            1 -> {
                binding.tabLatest.setTextColor(normalColor)
                binding.tabLatest.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                binding.tabRecommend.setTextColor(selectedColor)
                binding.tabRecommend.typeface = Typeface.create("sans-serif-bold", Typeface.BOLD)
            }
        }
    }

    /**
     * 销毁时
     */
    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
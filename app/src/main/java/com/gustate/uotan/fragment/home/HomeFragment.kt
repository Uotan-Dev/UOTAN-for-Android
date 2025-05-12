package com.gustate.uotan.fragment.home

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.gustate.uotan.R
import com.gustate.uotan.SearchActivity
import com.gustate.uotan.ui.activity.PostArticleActivity

class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*
         * 修改状态栏和底栏占位布局的高度
         */
        val headerBarView = view.findViewById<View>(R.id.header_bar_layout)
        // 取当前页面相对根布局
        val rootView: View = view.findViewById(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            headerBarView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            insets
        }
        val homeViewPager: ViewPager2 = view.findViewById(R.id.homeViewPager)
        homeViewPager.adapter = HomeViewPagerAdapter(this)
        homeViewPager.setCurrentItem(1,true)
        val latestItem = view.findViewById<TextView>(R.id.latest)
        val recommendItem = view.findViewById<TextView>(R.id.recommend)
        latestItem.setOnClickListener{ homeViewPager.setCurrentItem(0, true) }
        recommendItem.setOnClickListener{ homeViewPager.setCurrentItem(1, true) }
        val typedArray = requireContext().obtainStyledAttributes(intArrayOf(R.attr.colorOnBackgroundPrimary, R.attr.colorOnBackgroundSecondary))
        // 底栏按钮默认颜色
        val normalColor = typedArray.getColor(1, Color.RED)
        // 底栏按钮选择颜色
        val selectedColor = typedArray.getColor(0, Color.RED)
        typedArray.recycle()
        homeViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        latestItem.textSize = 16f
                        latestItem.setTextColor(selectedColor)
                        latestItem.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                        recommendItem.textSize = 14f
                        recommendItem.setTextColor(normalColor)
                        recommendItem.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                    }
                    1 -> {
                        latestItem.textSize = 14f
                        latestItem.setTextColor(normalColor)
                        latestItem.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                        recommendItem.textSize = 16f
                        recommendItem.setTextColor(selectedColor)
                        recommendItem.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                    }
                }
            }
        })
        val btnNew = view.findViewById<View>(R.id.btnNew)
        btnNew.setOnClickListener {
            startActivity(Intent(requireContext(), PostArticleActivity::class.java))
        }
        val searchBox = view.findViewById<View>(R.id.searchBox)
        searchBox.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }
    }

    class HomeViewPagerAdapter(fragment: Fragment):
        FragmentStateAdapter(fragment.childFragmentManager, fragment.lifecycle){
        private val fragments = listOf(
            LatestFragment(),
            RecommendFragment()
        )
        override fun getItemCount(): Int = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position]
    }

}
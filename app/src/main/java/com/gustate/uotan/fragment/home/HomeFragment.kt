package com.gustate.uotan.fragment.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.gustate.uotan.R

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
        // 取当前页面状态栏占位布局
        val statusBarView: View = view.findViewById(R.id.statusBarView)
        // 取当前页面相对根布局
        val rootView: View = view.findViewById(R.id.rootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarView.updateLayoutParams<ViewGroup.LayoutParams> {
                height = systemBars.top
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
        homeViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        latestItem.textSize = 16f
                        latestItem.setTextColor(ContextCompat.getColor(requireContext(), R.color.label_primary))
                        recommendItem.textSize = 14f
                        recommendItem.setTextColor(ContextCompat.getColor(requireContext(), R.color.label_secondary))
                    }
                    1 -> {
                        latestItem.textSize = 14f
                        latestItem.setTextColor(ContextCompat.getColor(requireContext(), R.color.label_secondary))
                        recommendItem.textSize = 16f
                        recommendItem.setTextColor(ContextCompat.getColor(requireContext(), R.color.label_primary))
                    }
                }
            }
        })
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
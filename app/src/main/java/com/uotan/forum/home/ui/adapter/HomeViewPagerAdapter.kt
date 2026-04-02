package com.uotan.forum.home.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.uotan.forum.home.ui.LatestFragment
import com.uotan.forum.home.ui.RecommendFragment

class HomeViewPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment){
    private val fragments = listOf(
        LatestFragment(),
        RecommendFragment()
    )
    override fun getItemCount(): Int = fragments.size
    override fun createFragment(position: Int): Fragment = fragments[position]
}
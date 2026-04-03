package com.uotan.forum.main.ui

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.uotan.forum.home.ui.HomeFragment
import com.uotan.forum.section.ui.SectionFragment
import com.uotan.forum.resource.ui.ResFragment
import com.uotan.forum.user.ui.MeFragment
import com.uotan.forum.message.MessageFragment

class MainPagerAdapter(fragmentActivity: FragmentActivity):
    FragmentStateAdapter(fragmentActivity){
    override fun getItemCount(): Int = 5
    override fun createFragment(position: Int) = when(position) {
        0 -> HomeFragment()
        1 -> SectionFragment()
        2 -> MessageFragment()
        3 -> ResFragment()
        4 -> MeFragment()
        else -> throw IllegalArgumentException()
    }
}
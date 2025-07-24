package com.gustate.uotan.main.ui

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gustate.uotan.home.ui.HomeFragment
import com.gustate.uotan.section.ui.SectionFragment
import com.gustate.uotan.fragment.resource.ResFragment
import com.gustate.uotan.fragment.user.MeFragment
import com.gustate.uotan.message.ui.MessageFragment

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
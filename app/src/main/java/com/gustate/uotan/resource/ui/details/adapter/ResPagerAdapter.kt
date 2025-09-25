package com.gustate.uotan.resource.ui.details.adapter

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gustate.uotan.resource.ui.details.fragment.ResDetailsFragment
import com.gustate.uotan.resource.ui.details.fragment.ResReplyFragment

class ResPagerAdapter(fragmentActivity: FragmentActivity):
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount() = 2
    override fun createFragment(position: Int) = when(position) {
        0 -> ResDetailsFragment()
        1 -> ResReplyFragment()
        else -> throw IllegalArgumentException()
    }
}
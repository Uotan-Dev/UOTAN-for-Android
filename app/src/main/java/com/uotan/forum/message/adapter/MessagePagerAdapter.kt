package com.uotan.forum.message.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.uotan.forum.message.message.SubMessageFragment
import com.uotan.forum.message.personal.PersonalLetterFragment

class MessagePagerAdapter(fragment: Fragment):
    FragmentStateAdapter(fragment.childFragmentManager, fragment.lifecycle){
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int) = when(position) {
        0 -> SubMessageFragment()
        1 -> PersonalLetterFragment()
        else -> throw IllegalArgumentException()
    }
}
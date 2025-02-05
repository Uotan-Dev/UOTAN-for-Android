package com.gustate.uotan.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.gustate.uotan.R

class HomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
            statusBarView.layoutParams.height = systemBars.top
            insets
        }
        val homeViewPager: ViewPager2 = view.findViewById(R.id.homeViewPager)
        homeViewPager.adapter = HomeViewPagerAdapter(this)
    }

}

class HomeViewPagerAdapter(fragment: Fragment):
    FragmentStateAdapter(fragment.childFragmentManager, fragment.lifecycle){
    private val fragments = listOf(
        RecommendFragment()
    )
    override fun getItemCount(): Int = fragments.size
    override fun createFragment(position: Int): Fragment = fragments[position]
}
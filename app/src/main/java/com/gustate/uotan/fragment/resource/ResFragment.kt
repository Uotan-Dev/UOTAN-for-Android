package com.gustate.uotan.fragment.resource

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.gustate.uotan.R
import com.gustate.uotan.anim.TitleAnim
import com.gustate.uotan.fragment.notice.PrivateMessageFragment
import com.gustate.uotan.utils.Utils
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import kotlin.math.roundToInt

class ResFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_res, container, false)
    }

    @SuppressLint("Recycle", "ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val statusBarView: View = view.findViewById(R.id.statusBarView)
        val coordinatorLayout: CoordinatorLayout = view.findViewById(R.id.coordinatorLayout)
        val title: View = view.findViewById(R.id.title)
        val bigTitle: View = view.findViewById(R.id.bigTitleText)
        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarView.updateLayoutParams<ViewGroup.LayoutParams> { height = systemBars.top }
            coordinatorLayout.setPadding(
                systemBars.left,
                (systemBars.top + 60f.dpToPx(requireContext())).roundToInt(),
                systemBars.right,
                0
            )
            // 创建 SimonEdgeIllusion 实例
            TitleAnim(
                title,
                bigTitle,
                (systemBars.top + 60f.dpToPx(requireContext())),
                systemBars.top.toFloat()
            )
            insets
        }
        val viewPager: ViewPager2 = view.findViewById(R.id.viewPager)
        viewPager.adapter = ResourceViewPagerAdapter(this)
        val bigTitleTrends: TextView = view.findViewById(R.id.bigTitleTrends)
        val bigTitleCategorize: TextView = view.findViewById(R.id.bigTitleCategorize)
        val titleTrends: TextView = view.findViewById(R.id.titleTrends)
        val titleCategorize: TextView = view.findViewById(R.id.titleCategorize)
        bigTitleTrends.setOnClickListener{ viewPager.setCurrentItem(0, true) }
        bigTitleCategorize.setOnClickListener{ viewPager.setCurrentItem(1, true) }
        titleTrends.setOnClickListener{ viewPager.setCurrentItem(0, true) }
        titleCategorize.setOnClickListener{ viewPager.setCurrentItem(1, true) }
        val typedArray = requireContext().obtainStyledAttributes(intArrayOf(R.attr.colorOnBackgroundPrimary, R.attr.colorOnBackgroundSecondary))
        // 底栏按钮默认颜色
        val normalColor = typedArray.getColor(1, Color.RED)
        // 底栏按钮选择颜色
        val selectedColor = typedArray.getColor(0, Color.RED)
        typedArray.recycle()
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        bigTitleTrends.setTextColor(selectedColor)
                        bigTitleCategorize.setTextColor(normalColor)
                        titleTrends.setTextColor(selectedColor)
                        titleCategorize.setTextColor(normalColor)
                    }
                    1 -> {
                        bigTitleTrends.setTextColor(normalColor)
                        bigTitleCategorize.setTextColor(selectedColor)
                        titleTrends.setTextColor(normalColor)
                        titleCategorize.setTextColor(selectedColor)
                    }
                }
            }
        })
    }
}

class ResourceViewPagerAdapter(fragment: Fragment):
    FragmentStateAdapter(fragment.childFragmentManager, fragment.lifecycle){
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int) = when(position) {
        0 -> TrendsResourceFragment()
        1 -> PrivateMessageFragment()
        else -> throw IllegalArgumentException()
    }
}
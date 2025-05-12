package com.gustate.uotan.message.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.gustate.uotan.R
import com.gustate.uotan.anim.TitleAnim
import com.gustate.uotan.databinding.FragmentNoticeBinding
import com.gustate.uotan.message.ui.adapter.MessagePagerAdapter
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.getThemeColor
import kotlin.math.roundToInt

class MessageFragment : Fragment() {

    private lateinit var binding: FragmentNoticeBinding
    private val viewModel: MessageViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNoticeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.headerBarLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            binding.coordinatorLayout.setPadding(
                systemBars.left,
                (systemBars.top + 60f.dpToPx(requireContext()).roundToInt()),
                systemBars.right,
                0
            )
            binding.viewPager.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = - (systemBars.top + 105f.dpToPx(requireContext()).roundToInt())
            }
            binding.viewPager.setPadding(
                systemBars.left,
                (systemBars.top + 105f.dpToPx(requireContext()).roundToInt()),
                systemBars.right,
                0
            )
            TitleAnim(
                binding.tvTitle,
                binding.bigTitleText,
                (systemBars.top + 60f.dpToPx(requireContext())),
                systemBars.top.toFloat()
            )
            insets
        }
        observerPager()

        binding.viewPager.adapter = MessagePagerAdapter(this)
        binding.bigTitle.setOnClickListener{ binding.viewPager.setCurrentItem(0, true) }
        binding.bigTitleMessage.setOnClickListener{ binding.viewPager.setCurrentItem(1, true) }
        binding.titleNotice.setOnClickListener{ binding.viewPager.setCurrentItem(0, true) }
        binding.titleMessage.setOnClickListener{ binding.viewPager.setCurrentItem(1, true) }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.onPageSelected(position)
            }
        })
    }

    private fun observerPager() {
        val normalColor = getThemeColor(requireContext(), R.attr.colorOnBackgroundSecondary)
        val selectedColor = getThemeColor(requireContext(), R.attr.colorOnBackgroundPrimary)
        viewModel.currentPage.observe(viewLifecycleOwner) {
            when (it) {
                0 -> {
                    binding.bigTitle.setTextColor(selectedColor)
                    binding.bigTitleMessage.setTextColor(normalColor)
                    binding.titleNotice.setTextColor(selectedColor)
                    binding.titleMessage.setTextColor(normalColor)
                }
                1 -> {
                    binding.bigTitle.setTextColor(normalColor)
                    binding.bigTitleMessage.setTextColor(selectedColor)
                    binding.titleNotice.setTextColor(normalColor)
                    binding.titleMessage.setTextColor(selectedColor)
                }
            }
        }
    }
}
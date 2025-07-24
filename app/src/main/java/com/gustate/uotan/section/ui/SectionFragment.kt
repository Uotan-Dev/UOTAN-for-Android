package com.gustate.uotan.section.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import com.gustate.uotan.R
import com.gustate.uotan.databinding.FragmentSectionBinding
import com.gustate.uotan.section.data.model.Categories
import com.gustate.uotan.section.ui.adapter.CategoriesAdapter
import com.gustate.uotan.section.ui.adapter.SectionAdapter
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.errorDialog
import kotlin.math.roundToInt

class SectionFragment : Fragment() {

    // PlateFragment 私有变量
    // 可空的视图绑定，请在 Create 时初始化，Destroy 时置空
    private var _binding: FragmentSectionBinding? = null
    private val binding: FragmentSectionBinding get() = _binding!!
    private val viewModel by activityViewModels<SectionViewModel>()
    private lateinit var categoriesAdapter: CategoriesAdapter
    private lateinit var sectionAdapter: SectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSectionBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            // 获取系统栏高度 (包含 top, bottom, left 和 right)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Phone
            binding.statusBarView
                ?.updateLayoutParams<ViewGroup.LayoutParams> { height = systemBars.top }
            binding.statusBarBlurView
                ?.updateLayoutParams<ViewGroup.LayoutParams> { height = systemBars.top }
            binding.appBarLayout
                ?.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = systemBars.top }
            binding.rvCategories.setPadding(
                12f.dpToPx(requireContext()).roundToInt(),
                systemBars.top, 0, systemBars.bottom
            )
            binding.rvSection.let {
                when (it.tag) {
                    "pad_port" -> {
                        it.setPadding(
                            7f.dpToPx(requireContext()).roundToInt(),
                            systemBars.top, systemBars.right + 7f.dpToPx(requireContext()).roundToInt(),
                            systemBars.bottom
                        )
                    }
                    "pad_land" -> {
                        it.setPadding(
                            7f.dpToPx(requireContext()).roundToInt(),
                            systemBars.top, systemBars.right + 7f.dpToPx(requireContext()).roundToInt(),
                            systemBars.bottom
                        )
                    }
                    else -> {
                        it.setPadding(
                            7f.dpToPx(requireContext()).roundToInt(),
                            (126f.dpToPx(requireContext()).roundToInt()),
                            systemBars.right + 7f.dpToPx(requireContext()).roundToInt(),
                            (70f.dpToPx(requireContext())
                                .roundToInt() + systemBars.top + systemBars.bottom)
                        )
                    }
                }
            }
            // 返回 insets
            insets
        }
        // 实例化版块大类列表适配器
        categoriesAdapter = CategoriesAdapter(requireContext()).apply {
            onItemClick = { position, content ->
                viewModel.setCurrentTabPosition(position)
            }
        }
        binding.rvCategories.let {
            it.adapter = categoriesAdapter
            it.layoutManager = when (it.tag) {
                "pad_port" -> {
                    StaggeredGridLayoutManager(2, VERTICAL)
                }
                "pad_land" -> {
                    StaggeredGridLayoutManager(4, VERTICAL)
                }
                else -> {
                    LinearLayoutManager(context)
                }
            }
        }
        sectionAdapter = SectionAdapter().apply {
            onItemClick = { link, cover, title ->
                if (link.startsWith("/categories")) {
                    startActivity(
                        Intent(
                            requireContext(),
                            CategoriesActivity::class.java
                        ).apply {
                            putExtra("link", link)
                            putExtra("cover", cover)
                            putExtra("title",
                                viewModel.currentTabPosition.value?.let { viewModel.sectionCollapseList.value?.get(it) }?.title
                            )
                        }
                    )
                } else {
                    startActivity(
                        Intent(
                            requireContext(),
                            SectionDataActivity::class.java
                        ).apply {
                            putExtra("link", link)
                            putExtra("cover", cover)
                            putExtra("title", title)
                        }
                    )
                }
            }
        }
        binding.rvSection.let {
            it.adapter = sectionAdapter
            it.layoutManager = when (it.tag) {
                "pad_port" -> {
                    LinearLayoutManager(context)
                }
                "pad_land" -> {
                    StaggeredGridLayoutManager(2, VERTICAL)
                }
                else -> {
                    LinearLayoutManager(context)
                }
            }
        }
        obverseSectionList()
        viewModel.updateSectionList(true, 0) {
            errorDialog(requireContext(), "ERROR", it.message)
        }
    }

    private fun obverseSectionList() {
        viewModel.sectionCollapseList.observe(viewLifecycleOwner) {
            if (it[0].title != getString(R.string.my_follow)) {
                it.add(0, Categories(getString(R.string.my_follow), mutableListOf()))
            }
            categoriesAdapter.submitList(it)
        }
        viewModel.currentTabPosition.observe(viewLifecycleOwner) {
            categoriesAdapter.setSelected(it)
            if (it == 0) {
                viewModel.updateFollowList(
                    onFirstSuccess = {
                        sectionAdapter.submitList(it)
                    },
                    onSuccess = {
                        sectionAdapter.submitList(viewModel.followList.value)
                    },
                    onThrowable = {

                    }
                )
                binding.imgNonePost?.isVisible = viewModel.followList.value?.isEmpty() == true
                binding.tvNonePost?.isVisible = viewModel.followList.value?.isEmpty() == true
            } else {
                val list = viewModel.sectionCollapseList.value?.get(it)?.sectionItemList?:mutableListOf()
                sectionAdapter.submitList(list)
                binding.imgNonePost?.isVisible = list.isEmpty()
                binding.tvNonePost?.isVisible = list.isEmpty()
            }
        }
    }

}
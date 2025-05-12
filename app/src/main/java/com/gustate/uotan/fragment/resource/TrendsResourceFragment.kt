package com.gustate.uotan.fragment.resource

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.gustate.uotan.R
import com.gustate.uotan.ui.activity.ResourceActivity
import com.gustate.uotan.databinding.FragmentTrendsResourceBinding
import com.gustate.uotan.fragment.resource.ResFragment.ResourceAdapter
import com.gustate.uotan.fragment.resource.ResFragment.TrendsResourceAdapter
import com.gustate.uotan.gustatex.dialog.InfoDialog
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.parse.resource.ResourceParse.Companion.fetchResourceData
import com.gustate.uotan.utils.parse.resource.ResourceRecommendParse.Companion.fetchResourceRecommendData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class TrendsResourceFragment : Fragment() {

    /** 定义全类可变变量 **/
    private var _binding: FragmentTrendsResourceBinding? = null
    private val binding: FragmentTrendsResourceBinding get() = _binding!!
    // 最新当前页面
    private var laCurrentPage = 1
    // 最新总页面
    private var laTotalPages = 1
    // 最新是否正在加载
    private var isLoading = false
    // 最新是否最后一页
    private var isLaLastPage = false

    /** 定义全类非可变变量 **/
    private lateinit var reAdapter: TrendsResourceAdapter
    private lateinit var laAdapter: ResourceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrendsResourceBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.nsvRoot.setPadding(
                0, 0, 0,
                (systemBars.bottom + 70f.dpToPx(requireContext())).roundToInt()
            )
            binding.srlRoot.setFooterInsetStartPx(
                (systemBars.bottom + 70f.dpToPx(requireContext())).roundToInt()
            )
            insets
        }

        loadData(true)

        binding.srlRoot.setOnRefreshListener { loadData(true) }

        binding.srlRoot.setOnLoadMoreListener { loadData(false) }

        binding.rvTrends.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        // 滚动完全停止时触发
                        (parentFragment as? ResFragment)?.apply {
                            // 直接访问父 Fragment 的绑定对象
                            val viewPager = parentFragment?.view?.findViewById<ViewPager2>(R.id.viewPager)
                            viewPager?.isUserInputEnabled = true
                        }

                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        // 用户手指拖动列表时的状态
                        (parentFragment as? ResFragment)?.apply {
                            // 直接访问父 Fragment 的绑定对象
                            val viewPager = parentFragment?.view?.findViewById<ViewPager2>(R.id.viewPager)
                            viewPager?.isUserInputEnabled = false
                        }
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {
                        // 列表自动惯性滚动时的状态
                        (parentFragment as? ResFragment)?.apply {
                            // 直接访问父 Fragment 的绑定对象
                            val viewPager = parentFragment?.view?.findViewById<ViewPager2>(R.id.viewPager)
                            viewPager?.isUserInputEnabled = true
                        }
                    }
                }
            }
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    private fun loadData(refresh: Boolean) {
        if (isLoading || isLaLastPage) return
        isLoading = true
        if (refresh) {
            reAdapter = TrendsResourceAdapter().apply {
                onItemClick = { clickItem ->
                    context?.let {
                        val intent = Intent(
                            it,
                            ResourceActivity::class.java
                        ).apply {
                            putExtra("url", clickItem.link)
                        }
                        startActivity(intent)
                    }
                }
            }
            binding.rvTrends.layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = HORIZONTAL
            }
            binding.rvTrends.adapter = reAdapter

            laCurrentPage = 1
            laTotalPages = 1
            isLaLastPage = false
            laAdapter = ResourceAdapter().apply {
                onItemClick = { clickItem ->
                    context?.let {
                        val intent = Intent(
                            it,
                            ResourceActivity::class.java
                        ).apply {
                            putExtra("url", clickItem.link)
                        }
                        startActivity(intent)
                    }
                }
            }
            binding.rvLatest.layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = VERTICAL
            }
            binding.rvLatest.adapter = laAdapter
        }
        lifecycleScope.launch {
            try {
                if (refresh) {
                    val reList = fetchResourceRecommendData()
                    withContext(Dispatchers.Main) { reAdapter.addAll(reList) }
                }
                val laList = fetchResourceData(laCurrentPage)
                laCurrentPage ++
                laTotalPages = laList.totalPage
                isLaLastPage = laCurrentPage > laTotalPages
                withContext(Dispatchers.Main) { laAdapter.addAll(laList.items) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    context?.let {
                        InfoDialog(it)
                            .setTitle("ERROR")
                            .setDescription(e.message.toString())
                            .show()
                    }
                }
                isLoading = false
            } finally {
                withContext(Dispatchers.Main) {
                    if (isLaLastPage) {
                        binding.srlRoot.finishLoadMoreWithNoMoreData()
                        binding.srlRoot.setNoMoreData(true)
                        binding.srlRoot.finishRefresh()
                    } else {
                        binding.srlRoot.finishLoadMore()
                        binding.srlRoot.finishRefresh()
                    }
                }
                isLoading = false
            }
        }
    }
}
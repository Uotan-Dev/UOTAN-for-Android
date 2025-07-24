package com.gustate.uotan.home.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import com.gustate.uotan.databinding.FragmentLatestBinding
import com.gustate.uotan.home.ui.adapter.LatestAdapter
import com.gustate.uotan.threads.ui.ThreadsActivity
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.errorDialog
import kotlin.getValue
import kotlin.math.roundToInt

class LatestFragment : Fragment() {

    // LatestFragment 私有变量
    // 可空的视图绑定，请在 Create 时初始化，Destroy 时置空
    private var _binding: FragmentLatestBinding? = null
    // 对视图绑定的非空访问，仅在生命周期有效时可用
    private val binding: FragmentLatestBinding get() = _binding!!
    // 在 Activity 的生命周期中取 ViewModel
    private val viewModel by activityViewModels<HomeViewModel>()
    // 延迟创建 LatestAdapter 实例
    private lateinit var adapter: LatestAdapter

    /**
     * 实例化用户界面视图
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 初始化视图绑定
        _binding = FragmentLatestBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * 实例化用户界面视图后
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 修改状态栏和底栏占位布局的高度
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            when (binding.rvRoot.tag?.toString()) {
                "pad_land" -> {
                    binding.srlRoot.layout.setPadding(
                        0, systemBars.top, 0,
                        (systemBars.bottom + 84f.dpToPx(requireContext())).roundToInt()
                    )
                    binding.srlRoot.setHeaderInsetStartPx(systemBars.top)
                    binding.srlRoot.setFooterInsetStartPx(
                        (systemBars.bottom + 84f
                            .dpToPx(requireContext())).roundToInt()
                    )
                }
                "pad_port" -> {
                    binding.srlRoot.layout.setPadding(
                        0, systemBars.top, 0,
                        (systemBars.bottom + 154f.dpToPx(requireContext())).roundToInt()
                    )
                    binding.srlRoot.setHeaderInsetStartPx(systemBars.top)
                    binding.srlRoot.setFooterInsetStartPx(
                        (systemBars.bottom + 154f
                            .dpToPx(requireContext())).roundToInt()
                    )
                }
                // 正常设备
                else -> {
                    binding.srlRoot.setPadding(
                        0,
                        (systemBars.top + 102f.dpToPx(requireContext())).roundToInt(), 0,
                        (systemBars.bottom + 70f.dpToPx(requireContext())).roundToInt()
                    )
                    binding.srlRoot.setHeaderInsetStartPx(
                        (systemBars.top + 102f
                            .dpToPx(requireContext())).roundToInt()
                    )
                    binding.srlRoot.setFooterInsetStartPx(
                        (systemBars.bottom + 70f
                            .dpToPx(requireContext())).roundToInt()
                    )
                }
            }
            insets
        }

        // 为 adapter 赋予 LatestAdapter 实例
        adapter = LatestAdapter().apply {
            // 单击监听
            onItemClick = {
                val intent = Intent(requireContext(), ThreadsActivity::class.java)
                    .putExtra("url", it.url).putExtra("title", it.title)
                startActivity(intent)
            }
        }

        // RecyclerView 设置
        binding.rvRoot.adapter = adapter
        when (binding.rvRoot.tag?.toString()) {
            "pad_land" -> {
                binding.rvRoot.layoutManager = StaggeredGridLayoutManager(2, VERTICAL)
            }
            // 平板横屏与普通设备
            else -> {
                binding.rvRoot.layoutManager = LinearLayoutManager(requireContext())
            }
        }

        obverse()

        // 首次加载
        viewModel.loadLatestData(
            isRefresh = false,
            onSuccess = {},
            onException = {
                errorDialog(requireContext(), "ERROR", it.message)
            }
        )

        // 刷新监听
        binding.srlRoot.setOnRefreshListener {
            viewModel.loadLatestData(
                isRefresh = true,
                onSuccess = {
                    binding.srlRoot.finishRefresh()
                },
                onException = {
                    errorDialog(requireContext(), "ERROR", it.message)
                }
            )
        }
    }

    /**
     * ViewModel 监听
     */
    private fun obverse() {
        viewModel.latestList.observe(viewLifecycleOwner) {
            adapter.addAll(it.toMutableList())
        }
    }

    /**
     * 销毁时
     */
    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
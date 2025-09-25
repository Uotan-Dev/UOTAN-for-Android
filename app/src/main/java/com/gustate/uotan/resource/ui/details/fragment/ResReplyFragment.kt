package com.gustate.uotan.resource.ui.details.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gustate.uotan.databinding.FragmentResReplyBinding
import com.gustate.uotan.resource.ui.details.ResDetailsViewModel
import com.gustate.uotan.resource.ui.details.adapter.ResourceReplyAdapter
import com.gustate.uotan.utils.Utils.dpToPx
import kotlin.getValue
import kotlin.math.roundToInt

class ResReplyFragment : Fragment() {

    // ResReplyFragment 私有变量
    // 可空的视图绑定，请在 Create 时初始化，Destroy 时置空
    private var _binding: FragmentResReplyBinding? = null
    // 对视图绑定的非空访问，仅在生命周期有效时可用
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<ResDetailsViewModel>()
    private lateinit var replyAdapter: ResourceReplyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResReplyBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val bottomMenuHeight = context?.let { 65f.dpToPx(it).roundToInt() } ?: 0
            binding.rvPosts.let {
                it.setPadding(it.paddingLeft, it.paddingTop, it.paddingRight,
                    systemBars.bottom + bottomMenuHeight)
            }
            insets
        }
        replyAdapter = ResourceReplyAdapter()
        binding.rvPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPosts.adapter = replyAdapter

        viewModel.loadInitialReply(false, {})
        viewModel.reply.observe(viewLifecycleOwner) {
            binding.layoutNoReply.root.isGone = !it.isEmpty()
            replyAdapter.submitList(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
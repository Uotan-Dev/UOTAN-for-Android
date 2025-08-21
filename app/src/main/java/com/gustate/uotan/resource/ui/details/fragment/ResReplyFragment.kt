package com.gustate.uotan.resource.ui.details.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gustate.uotan.databinding.FragmentResReplyBinding
import com.gustate.uotan.resource.ui.details.ResDetailsViewModel
import com.gustate.uotan.resource.ui.details.adapter.ResourceReplyAdapter
import kotlin.getValue

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

        replyAdapter = ResourceReplyAdapter()
        binding.rvPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPosts.adapter = replyAdapter

        viewModel.loadInitialReply(false, {})
        viewModel.reply.observe(viewLifecycleOwner) {
            replyAdapter.addAll(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
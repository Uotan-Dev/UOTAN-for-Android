package com.uotan.forum.message.message

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.uotan.forum.databinding.FragmentSubNoticeBinding
import com.uotan.forum.message.adapter.MessageRecyclerAdapter
import com.uotan.forum.threads.ui.ThreadsActivity
import com.uotan.forum.utils.Utils.dpToPx
import kotlin.math.roundToInt

class SubMessageFragment : Fragment() {
    private lateinit var binding: FragmentSubNoticeBinding
    private lateinit var adapter: MessageRecyclerAdapter
    private val viewModel: SubMessageViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubNoticeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.recyclerView.setPadding(
                0,0,0,
                (systemBars.bottom + 70f.dpToPx(requireContext())).roundToInt()
            )
            insets
        }
        adapter = MessageRecyclerAdapter().apply {
            onItemClick = {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                requireContext().startActivity(
                    Intent(
                        requireContext(),
                        ThreadsActivity::class.java
                    ).apply {
                        putExtra("url", it)
                    }
                )
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.srlRoot.setOnRefreshListener {
            viewModel.loadInitialData()
        }

        viewModel.noticeList.observe(viewLifecycleOwner) {
            adapter.updateList(it)
            binding.srlRoot.finishRefresh()
        }

        viewModel.loadInitialData()
    }
}
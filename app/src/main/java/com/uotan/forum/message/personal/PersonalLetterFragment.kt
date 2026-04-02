package com.uotan.forum.message.personal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.uotan.forum.databinding.FragmentPersonalLetterBinding
import com.uotan.forum.message.adapter.PersonalLetterRecyclerAdapter
import com.uotan.forum.utils.room.UserViewModel
import kotlinx.coroutines.launch

class PersonalLetterFragment : Fragment() {

    private lateinit var binding: FragmentPersonalLetterBinding
    private lateinit var adapter: PersonalLetterRecyclerAdapter
    private val viewModel: PersonalLetterViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPersonalLetterBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = PersonalLetterRecyclerAdapter()
        binding.rvPersonalLetterList.adapter = adapter
        binding.rvPersonalLetterList.layoutManager = LinearLayoutManager(requireContext())
        lifecycleScope.launch {
            val userId = userViewModel.getUser()?.userId!!
            viewModel.loadInitialData(userId)
        }
        viewModel.personalLetterList.observe(viewLifecycleOwner) {
            adapter.updateList(it)
        }
    }
}
package com.uotan.forum.resource.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.google.android.flexbox.FlexDirection.ROW
import com.google.android.flexbox.FlexWrap.WRAP
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent.FLEX_START
import com.uotan.forum.R
import com.uotan.forum.databinding.FragmentResBinding
import com.uotan.forum.databinding.ItemResourceTypeBinding
import com.uotan.forum.databinding.ItemResourceTypeCellBinding
import com.uotan.forum.resource.data.model.ResourcePlateItem
import com.uotan.forum.resource.ui.adapter.ResourceAdapter
import com.uotan.forum.resource.ui.adapter.TrendsAdapter
import com.uotan.forum.resource.ui.details.ResourceActivity
import com.uotan.forum.ui.view.scrollcontroller.DialogRecyclerView
import com.kongzue.dialogx.dialogs.BottomDialog
import com.kongzue.dialogx.interfaces.OnBindView

class ResFragment : Fragment() {

    private var _binding: FragmentResBinding? = null
    private val binding: FragmentResBinding get() = _binding!!
    private val viewModel by viewModels<ResViewModel>()

    private lateinit var trAdapter: TrendsAdapter
    private lateinit var tyAdapter: ResourceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBarLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            binding.headerBarBlurContent.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            insets
        }
        binding.rvTrends.layoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = HORIZONTAL
        }
        trAdapter = TrendsAdapter().apply {
            onItemClick = { clickItem ->
                context?.let {
                    val intent = Intent(it, ResourceActivity::class.java)
                        .putExtra("url", clickItem.link)
                    startActivity(intent)
                }
            }
        }
        binding.rvTrends.adapter = trAdapter
        binding.rvLatest.layoutManager = LinearLayoutManager(requireContext())
        tyAdapter = ResourceAdapter().apply {
            onItemClick = { clickItem ->
                context?.let {
                    val intent = Intent(it, ResourceActivity::class.java)
                        .putExtra("url", clickItem.link)
                    startActivity(intent)
                }
            }
        }
        binding.rvLatest.adapter = tyAdapter
        obverse()
        typeDialog()
        viewModel.loadRecommendData(isRefresh = true, onThrowable = {})
        viewModel.loadInitialPlateContentData(isRefresh = true, onThrowable = {})
    }

    private fun obverse() {
        viewModel.recommendList.observe(viewLifecycleOwner) {
            trAdapter.submitList(it.toMutableList())
        }
        viewModel.currentPlateName.observe(viewLifecycleOwner) {
            binding.tvType.text = it
        }
        viewModel.plateContentList.observe(viewLifecycleOwner) {
            tyAdapter.submitList(it.toMutableList())
        }
    }

    private fun typeDialog() {
        binding.btnType.setOnClickListener {
            BottomDialog.show(
                object : OnBindView<BottomDialog?>(R.layout.dialogx_res_type) {
                    override fun onBind(
                        dialog: BottomDialog?,
                        v: View?
                    ) {
                        val recyclerView = v?.findViewById<DialogRecyclerView>(R.id.recyclerView)
                        val adapter = ResTypeAdapter().apply {
                            onDismissDialog = { name, url ->
                                viewModel.setPlateType(
                                    name = name,
                                    url = url,
                                    onSuccess = {
                                        dialog?.dismiss()
                                    },
                                    onThrowable = {

                                    }
                                )
                            }
                        }
                        recyclerView?.adapter = adapter
                        recyclerView?.layoutManager = LinearLayoutManager(context)
                        viewModel.loadPlateType(
                            onSuccess = {
                                viewModel.allPlate.value?.let { items -> adapter.addAll(items) }
                            },
                            onThrowable = {

                            }
                        )
                    }
                }
            )
        }
    }

    private class ResTypeAdapter(): Adapter<ResTypeAdapter.ViewHolder>() {
        private val resTypeList = mutableListOf<ResourcePlateItem>()
        var onDismissDialog: ((String, String) -> Unit)? = null
        inner class ViewHolder(private val binding: ItemResourceTypeBinding)
            : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
                fun bind(item: ResourcePlateItem) {
                    with(binding) {
                        tvTitle.text = item.plate
                        val flexboxLayoutManager = FlexboxLayoutManager(root.context).apply {
                            flexDirection = ROW
                            flexWrap = WRAP
                            justifyContent = FLEX_START
                        }
                        recyclerView.layoutManager = flexboxLayoutManager
                        val adapter = ResNameAdapter().apply {
                            onItemClick = { name, url ->
                                onDismissDialog?.invoke(name, url)
                            }
                        }
                        recyclerView.adapter = adapter
                        adapter.addAll(item.item, item.itemUrl)
                    }
                }
        }
        fun addAll(items: List<ResourcePlateItem>) {
            val startPosition = resTypeList.size
            resTypeList.addAll(items)
            notifyItemRangeInserted(startPosition, items.size)
        }
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            val binding = ItemResourceTypeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }
        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int
        ) {
            holder.bind(resTypeList[position])
        }
        override fun getItemCount(): Int = resTypeList.size
        private class ResNameAdapter(): Adapter<ResNameAdapter.ViewHolder>() {
            private val resNameList = mutableListOf<String>()
            private val resUrlList = mutableListOf<String>()
            var onItemClick: ((String, String) -> Unit)? = null
            inner class ViewHolder(private val binding: ItemResourceTypeCellBinding)
                : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
                    fun bind(name: String, url: String) {
                        with(binding) {
                            tvResType.text = name
                            itemView.setOnClickListener {
                                onItemClick?.invoke(name, url)
                            }
                        }
                    }
            }
            fun addAll(nameList: MutableList<String>, urlList: MutableList<String>) {
                val startPosition = resNameList.size
                resNameList.addAll(nameList)
                resUrlList.addAll(urlList)
                notifyItemRangeInserted(startPosition, nameList.size)
            }
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ViewHolder {
                val binding = ItemResourceTypeCellBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return ViewHolder(binding)
            }
            override fun onBindViewHolder(
                holder: ViewHolder,
                position: Int
            ) {
                holder.bind(resNameList[position], resUrlList[position])
            }
            override fun getItemCount(): Int = resNameList.size
        }
    }

}
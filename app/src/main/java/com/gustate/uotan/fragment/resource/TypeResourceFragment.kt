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
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.google.android.flexbox.FlexDirection.ROW
import com.google.android.flexbox.FlexWrap.WRAP
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent.FLEX_START
import com.gustate.uotan.R
import com.gustate.uotan.activity.ResourceActivity
import com.gustate.uotan.databinding.FragmentTypeResourceBinding
import com.gustate.uotan.databinding.RecyclerResTypeNameItemBinding
import com.gustate.uotan.databinding.RecyclerTypeResItemBinding
import com.gustate.uotan.fragment.resource.ResFragment.ResourceAdapter
import com.gustate.uotan.gustatex.dialog.InfoDialog
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.parse.resource.ResourceParse.Companion.fetchResourceData
import com.gustate.uotan.utils.parse.resource.ResourceParse.Companion.fetchResourcePlateData
import com.gustate.uotan.utils.parse.resource.ResourcePlateItem
import com.kongzue.dialogx.dialogs.BottomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class TypeResourceFragment : Fragment() {

    private var _binding: FragmentTypeResourceBinding? = null
    private val binding: FragmentTypeResourceBinding get() = _binding!!
    private var isTypeLoading = false
    private var isResLoading = false
    private var isResLastPage = false
    private var resCurrentPage = 1
    private var resTotalPage = 1
    private var typeName = ""
    private var typeUrl = ""

    /**
     * @see ResourceAdapter
     */
    private lateinit var adapter: ResourceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTypeResourceBinding.inflate(
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

        loadData(typeUrl, true)

        binding.btnType.setOnClickListener {
            BottomDialog.show(
                object : OnBindView<BottomDialog?>(R.layout.dialogx_res_type) {
                    override fun onBind(
                        dialog: BottomDialog?,
                        v: View?
                    ) {
                        lifecycleScope.launch {
                            val resTypeData = fetchResourcePlateData()
                            withContext(Dispatchers.Main) {
                                val recyclerView = v?.findViewById<RecyclerView>(R.id.recyclerView)
                                recyclerView?.layoutManager = LinearLayoutManager(requireContext()).apply {
                                    orientation = VERTICAL
                                }
                                val adapter = ResTypeAdapter().apply {
                                    onDismissDialog = { name, url ->
                                        binding.btnType.text = name
                                        typeName = name
                                        typeUrl = url
                                        dialog?.dismiss()
                                        loadData(typeUrl, true)
                                    }
                                }
                                recyclerView?.adapter = adapter
                                adapter.addAll(resTypeData)
                            }
                        }
                    }
                }
            )
        }

        binding.srlRoot.setOnRefreshListener {
            loadData(typeUrl, true)
        }

        binding.srlRoot.setOnLoadMoreListener {
            loadData(typeUrl, false)
        }
    }

    private fun loadData(url: String, refresh: Boolean) {
        if (isResLoading || isResLastPage) return
        isResLoading = true
        if (refresh) {
            resCurrentPage = 1
            resTotalPage = 1
            isResLastPage = false
            adapter = ResourceAdapter().apply {
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
            binding.rvRes.layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = VERTICAL
            }
            binding.rvRes.adapter = adapter
        }
        lifecycleScope.launch {
            try {
                val resourceList = fetchResourceData(resCurrentPage, url)
                resCurrentPage ++
                resTotalPage = resourceList.totalPage
                isResLastPage = resCurrentPage > resTotalPage
                withContext(Dispatchers.Main) { adapter.addAll(resourceList.items) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    context?.let {
                        InfoDialog(it)
                            .setTitle("ERROR")
                            .setDescription(e.message.toString())
                            .show()
                    }
                }
                isResLoading = false
            } finally {
                withContext(Dispatchers.Main) {
                    if (isResLastPage) {
                        binding.srlRoot.finishLoadMoreWithNoMoreData()
                        binding.srlRoot.setNoMoreData(true)
                        binding.srlRoot.finishRefresh()
                    } else {
                        binding.srlRoot.finishLoadMore()
                        binding.srlRoot.finishRefresh()
                    }
                }
                isResLoading = false
            }
        }
    }

    private class ResTypeAdapter(): Adapter<ResTypeAdapter.ViewHolder>() {
        private val resTypeList = mutableListOf<ResourcePlateItem>()
        var onDismissDialog: ((String, String) -> Unit)? = null
        inner class ViewHolder(private val binding: RecyclerTypeResItemBinding): RecyclerView.ViewHolder(binding.root) {
            fun bind(item: ResourcePlateItem) {
                with(binding) {
                    title.text = item.plate
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
        fun addAll(items: MutableList<ResourcePlateItem>) {
            val startPosition = resTypeList.size
            resTypeList.addAll(items)
            notifyItemRangeInserted(startPosition, items.size)
        }
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            val binding = RecyclerTypeResItemBinding.inflate(
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
            inner class ViewHolder(private val binding: RecyclerResTypeNameItemBinding): RecyclerView.ViewHolder(binding.root) {
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
                val binding = RecyclerResTypeNameItemBinding.inflate(
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
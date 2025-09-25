package com.gustate.uotan.resource.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.databinding.ItemResourceTrendsBinding
import com.gustate.uotan.resource.data.model.ResourceItem
import com.gustate.uotan.utils.Utils.baseUrl

class TrendsAdapter() : ListAdapter<ResourceItem, TrendsAdapter.ViewHolder>(DiffCallback()) {
    // 点击监听接口
    var onItemClick: ((ResourceItem) -> Unit)? = null
    // 新旧数据对比类
    class DiffCallback : DiffUtil.ItemCallback<ResourceItem>() {
        override fun areItemsTheSame(
            oldItem: ResourceItem,
            newItem: ResourceItem
        ) = oldItem.link == newItem.link
        override fun areContentsTheSame(
            oldItem: ResourceItem,
            newItem: ResourceItem
        ) = oldItem == newItem

    }
    inner class ViewHolder(val binding: ItemResourceTrendsBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResourceTrendsBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = getItem(position)
        val binding = holder.binding
        Glide.with(holder.itemView.context)
            .load(baseUrl + content.cover)
            .error(R.drawable.ic_no_data)
            .into(binding.imgCover)
        binding.tvTitle.text = content.title
        binding.tvDescribe.text = content.version
        binding.tvTime.text = content.updateTime
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(content)
        }
    }
}
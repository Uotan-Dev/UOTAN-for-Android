package com.gustate.uotan.home.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.databinding.ItemThreadBinding
import com.gustate.uotan.home.data.model.RecommendItem
import com.gustate.uotan.utils.Helpers
import com.gustate.uotan.utils.Utils

class RecommendAdapter(): ListAdapter<RecommendItem, RecommendAdapter.ViewHolder>(DiffCallback()) {
    class DiffCallback : DiffUtil.ItemCallback<RecommendItem>() {
        override fun areItemsTheSame(old: RecommendItem, new: RecommendItem) = old == new
        override fun areContentsTheSame(old: RecommendItem, new: RecommendItem) = old == new
    }
    var onItemClick: ((RecommendItem, View) -> Unit)? = null
    inner class ViewHolder(val binding: ItemThreadBinding):
        RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemThreadBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = getItem(position)
        val binding = holder.binding
        Glide.with(holder.itemView.context)
            .load(content.cover)
            .into(binding.imgCover)
        Glide.with(holder.itemView.context)
            .load(Utils.Companion.idToAvatar(content.userId))
            .apply(Helpers.Companion.avatarOptions)
            .into(binding.imgAvatar)
        binding.tvUsername.text = content.author
        binding.tvTime.text = content.time
        binding.tvTitle.text = content.title
        binding.tvDescribe.text = content.describe
        if (content.topic != "") {
            binding.cardTopic.isVisible = true
            binding.tvTopic.text = content.topic
        } else {
            binding.cardTopic.isVisible = false
        }
        binding.tvViewCount.text = content.viewCount
        binding.tvCommentCount.text = content.commentCount
        binding.layoutItem.setOnClickListener {
            onItemClick?.invoke(content, binding.layoutItem)
        }
    }
}
package com.uotan.forum.search.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uotan.forum.databinding.ItemThreadBinding
import com.uotan.forum.search.data.model.SearchItem
import com.uotan.forum.utils.Helpers.avatarOptions
import com.uotan.forum.utils.Utils.baseUrl
import com.uotan.forum.utils.Utils.dpToPx
import com.uotan.forum.utils.Utils.idToAvatar
import kotlin.math.roundToInt

class SearchAdapter(): ListAdapter<SearchItem, SearchAdapter.ViewHolder>(DiffCallback()) {
    class DiffCallback : DiffUtil.ItemCallback<SearchItem>() {
        override fun areItemsTheSame(old: SearchItem, new: SearchItem) = old.url == new.url
        override fun areContentsTheSame(old: SearchItem, new: SearchItem) = old == new
    }
    var onItemClick: ((SearchItem) -> Unit)? = null
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
        if (content.cover.isNotEmpty() && !content.cover.startsWith("/img")) {
            binding.imgCover.isVisible = true
            Glide.with(holder.itemView.context)
                .load(baseUrl + content.cover)
                .into(binding.imgCover)
            val userParams = binding.layoutUser.layoutParams as ViewGroup.MarginLayoutParams
            userParams.topMargin = 12f.dpToPx(holder.itemView.context).roundToInt()
            binding.layoutUser.layoutParams = userParams
        } else {
            binding.imgCover.isVisible = false
            val userParams = binding.layoutUser.layoutParams as ViewGroup.MarginLayoutParams
            userParams.topMargin = 0
            binding.layoutUser.layoutParams = userParams
        }
        Glide.with(holder.itemView.context)
            .load(idToAvatar(content.id))
            .apply(avatarOptions)
            .into(binding.imgAvatar)
        binding.tvUsername.text = content.author
        binding.tvTime.text = content.time
        binding.tvTitle.text = content.title
        binding.tvDescribe.text = content.content
        if (content.topic != "") {
            binding.cardTopic.isVisible = true
            binding.tvTopic.text = content.topic
        } else {
            binding.cardTopic.isVisible = false
        }
        binding.tvViewCount.isGone = true
        binding.icoViewCount.isGone = true
        binding.tvCommentCount.isGone = true
        binding.icoCommentCount.isGone = true
        binding.layoutItem.setOnClickListener {
            onItemClick?.invoke(content)
        }
    }
}
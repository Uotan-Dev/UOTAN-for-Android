package com.gustate.uotan.home.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.gustate.uotan.databinding.ItemThreadBinding
import com.gustate.uotan.home.data.model.LatestItem
import com.gustate.uotan.utils.Helpers.Companion.avatarOptions
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.idToAvatar
import kotlin.math.roundToInt

class LatestAdapter(): Adapter<LatestAdapter.ViewHolder>() {
    private val latestList = mutableListOf<LatestItem>()
    // 点击监听接口
    var onItemClick: ((LatestItem) -> Unit)? = null

    fun addAll(newItems: MutableList<LatestItem>) {
        val startPosition = latestList.size
        latestList.addAll(newItems)
        notifyItemRangeInserted(startPosition, newItems.size)
    }

    inner class ViewHolder(val binding: ItemThreadBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemThreadBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = latestList[position]
        val binding = holder.binding
        if (content.cover.startsWith("http")) {
            binding.imgCover.isVisible = true
            Glide.with(holder.itemView.context)
                .load(content.cover)
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
            .load(idToAvatar(content.userId))
            .apply(avatarOptions)
            .into(binding.imgAvatar)
        binding.tvUsername.text = content.author
        binding.tvTime.text = content.time
        binding.tvTitle.text = content.title
        binding.tvDescribe.isVisible = false
        if (content.topic != "") {
            binding.cardTopic.isVisible = true
            binding.tvTopic.text = content.topic
        } else {
            binding.cardTopic.isVisible = false
        }
        binding.tvViewCount.text = content.viewCount
        binding.tvCommentCount.text =content.commentCount
        binding.layoutItem.setOnClickListener {
            onItemClick?.invoke(content)
        }
    }
    override fun getItemCount(): Int = latestList.size
}
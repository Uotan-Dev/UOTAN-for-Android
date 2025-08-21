package com.gustate.uotan.resource.ui.details.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.gustate.uotan.R
import com.gustate.uotan.databinding.RecyclerResourceCommentItemBinding
import com.gustate.uotan.resource.data.model.ResReplyData
import com.gustate.uotan.resource.ui.details.adapter.ResourceReplyAdapter.ViewHolder
import com.gustate.uotan.utils.Utils

class ResourceReplyAdapter: RecyclerView.Adapter<ViewHolder>() {
    private val resourceCommentList = mutableListOf<ResReplyData>()
    var onItemClick: ((ResReplyData) -> Unit)? = null
    inner class ViewHolder(val binding: RecyclerResourceCommentItemBinding):
        RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = RecyclerResourceCommentItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val binding = holder.binding
        val content = resourceCommentList[position]
        val context = holder.itemView.context
        Glide.with(context)
            .load(Utils.Companion.idToAvatar(content.authorId))
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .placeholder(R.drawable.avatar_account)
            .error(R.drawable.avatar_account)
            .into(binding.imgAvatar)
        binding.tvUsername.text = content.author
        binding.tvTime.text = content.time
        binding.rtRes.rating = content.rating
        Utils.Companion.htmlToSpan(binding.tvContent, content.content)
    }
    override fun getItemCount(): Int = resourceCommentList.size
    fun addAll(newItems: List<ResReplyData>) {
        val startPosition = resourceCommentList.size
        resourceCommentList.addAll(newItems)
        notifyItemRangeInserted(startPosition, newItems.size)
    }
}
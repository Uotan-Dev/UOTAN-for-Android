package com.gustate.uotan.ui.activity.resource.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.gustate.uotan.R
import com.gustate.uotan.databinding.RecyclerResourceCommentItemBinding
import com.gustate.uotan.ui.activity.resource.adapter.ResourceCommentAdapter.ViewHolder
import com.gustate.uotan.utils.Utils.Companion.htmlToSpan
import com.gustate.uotan.utils.Utils.Companion.idToAvatar
import com.gustate.uotan.utils.parse.resource.ResourceData.ResReportData

class ResourceCommentAdapter: Adapter<ViewHolder>() {
    private val resourceCommentList = mutableListOf<ResReportData>()
    var onItemClick: ((ResReportData) -> Unit)? = null
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
            .load(idToAvatar(content.authorId))
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .placeholder(R.drawable.avatar_account)
            .error(R.drawable.avatar_account)
            .into(binding.imgAvatar)
        binding.tvUsername.text = content.author
        binding.tvTime.text = content.time
        binding.rtRes.rating = content.rating
        htmlToSpan(binding.tvContent, content.content)
    }
    override fun getItemCount(): Int = resourceCommentList.size
    fun addAll(newItems: MutableList<ResReportData>) {
        val startPosition = resourceCommentList.size
        resourceCommentList.addAll(newItems)
        notifyItemRangeInserted(startPosition, newItems.size)
    }
}
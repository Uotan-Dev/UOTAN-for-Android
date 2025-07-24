package com.gustate.uotan.section.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.databinding.RecyclerSectionItemBinding
import com.gustate.uotan.section.data.model.SectionItem
import com.gustate.uotan.utils.Utils

class SectionAdapter : ListAdapter<SectionItem, SectionAdapter.ViewHolder>(DiffCallback()) {
    var onItemClick: ((String, String, String) -> Unit)? = null
    class DiffCallback : DiffUtil.ItemCallback<SectionItem>() {
        override fun areItemsTheSame(old: SectionItem, new: SectionItem) = old.link == new.link
        override fun areContentsTheSame(old: SectionItem, new: SectionItem) = old == new
    }
    inner class ViewHolder(val binding: RecyclerSectionItemBinding):
        RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerSectionItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = getItem(position)
        val binding = holder.binding
        Glide.with(holder.itemView.context)
            .load(Utils.Companion.baseUrl + content.cover)
            .error(R.drawable.ic_more_device)
            .into(binding.imgCover)
        binding.tvTitle.text = content.title
        binding.layoutItem.setOnClickListener {
            onItemClick?.invoke(content.link, content.cover, content.title)
        }
    }
}
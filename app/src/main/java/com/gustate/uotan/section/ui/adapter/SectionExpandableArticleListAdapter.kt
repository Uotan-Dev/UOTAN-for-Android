package com.gustate.uotan.section.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gustate.uotan.R
import com.gustate.uotan.section.data.model.SectionDataItem

class SectionExpandableArticleListAdapter : ListAdapter<SectionDataItem, SectionExpandableArticleListAdapter.ViewHolder>(DiffCallback()) {
    var isExpanded = false
        private set
    val fullList = mutableListOf<SectionDataItem>()
    var onItemClick: ((String) -> Unit)? = null
    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val itemLayout: View = view.findViewById(R.id.itemLayout)
        val title: TextView = view.findViewById(R.id.tv_title)
    }
    fun setExpanded(expanded: Boolean) {
        isExpanded = expanded
        submitList(if (expanded) fullList else fullList.take(2))
    }
    class DiffCallback : DiffUtil.ItemCallback<SectionDataItem>() {
        override fun areItemsTheSame(old: SectionDataItem, new: SectionDataItem) = old == new
        override fun areContentsTheSame(old: SectionDataItem, new: SectionDataItem) = old == new
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_plate_top_article, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = getItem(position)
        holder.title.text = content.title
        holder.itemLayout.setOnClickListener {
            onItemClick?.invoke(content.link)
        }
    }
}
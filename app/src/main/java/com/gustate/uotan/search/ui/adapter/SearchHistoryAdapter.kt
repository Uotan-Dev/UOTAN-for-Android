package com.gustate.uotan.search.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gustate.uotan.R
import java.util.LinkedList

class SearchHistoryAdapter(private val searchHistoryList: LinkedList<String>) : RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {
    var onItemClick: ((String) -> Unit)? = null
    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tvHistory: TextView = view.findViewById(R.id.tvHistory)
        val itemLayout: View = view.findViewById(R.id.itemLayout)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_search_history_item, parent, false)
        return ViewHolder(view)
    }
    override fun getItemCount(): Int = searchHistoryList.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvHistory.text = searchHistoryList[position]
        holder.itemLayout.setOnClickListener {
            onItemClick?.invoke(searchHistoryList[position])
        }
    }
}
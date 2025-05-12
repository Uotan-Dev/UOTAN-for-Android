package com.gustate.uotan.ui.activity.resource.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.gustate.uotan.R
import com.gustate.uotan.databinding.RecyclerDownloadTypeBinding
import com.gustate.uotan.utils.parse.resource.ResourceData.PurchaseData
import kotlin.collections.mutableListOf

class NewResourceTypeAdapter(): Adapter<NewResourceTypeAdapter.ViewHolder>() {
    private val newResourceList = mutableListOf<PurchaseData>()
    var onItemClick: ((PurchaseData) -> Unit)? = null
    inner class ViewHolder(val binding: RecyclerDownloadTypeBinding):
        RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = RecyclerDownloadTypeBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val binding = holder.binding
        val content = newResourceList[position]
        val context = holder.itemView.context
        binding.tvDrive.text = content.driveName
        binding.btnDownload.text = if (!content.isPaid) {
            context.getString(R.string.buy_download, content.price)
        } else {
            context.getString(R.string.buy_download, "")
        }
        binding.btnDownload.setOnClickListener {
            onItemClick?.invoke(content)
        }
    }
    override fun getItemCount(): Int = newResourceList.size
    fun addAll(newItems: MutableList<PurchaseData>) {
        val startPosition = newResourceList.size
        newResourceList.addAll(newItems)
        notifyItemRangeInserted(startPosition, newItems.size)
    }
}
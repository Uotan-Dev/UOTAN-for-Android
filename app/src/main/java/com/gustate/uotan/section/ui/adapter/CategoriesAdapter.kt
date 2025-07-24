package com.gustate.uotan.section.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.os.VibrationEffect
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gustate.uotan.R
import com.gustate.uotan.databinding.RecyclerCategoriesItemBinding
import com.gustate.uotan.section.data.model.Categories
import com.gustate.uotan.section.data.model.SectionItem
import com.gustate.uotan.utils.Utils.Companion.getThemeColor

class CategoriesAdapter(private val context: Context) :
    ListAdapter<Categories, CategoriesAdapter.ViewHolder>(DiffCallback()) {
    var onItemClick: ((Int, MutableList<SectionItem>) -> Unit)? = null
    private var selectedPosition = -1
    private var isFirstSelected = true
    class DiffCallback : DiffUtil.ItemCallback<Categories>() {
        override fun areItemsTheSame(old: Categories, new: Categories) = old.title == new.title
        override fun areContentsTheSame(old: Categories, new: Categories) = old == new
    }
    inner class ViewHolder(val binding: RecyclerCategoriesItemBinding):
        RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerCategoriesItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = getItem(position)
        val binding = holder.binding
        updateTabStyle(binding.tabCategories, selectedPosition == position)
        binding.tabCategories.text = content.title
        binding.tabCategories.setOnClickListener {
            setSelected(position)
            onItemClick?.invoke(position, content.sectionItemList)
        }
    }
    private fun updateTabStyle(tab: TextView, isSelected: Boolean) {
        // 底栏按钮默认颜色
        val normalColor = getThemeColor(context, R.attr.colorOnBackgroundSecondary)
        // 底栏按钮选择颜色
        val selectedColor = getThemeColor(context, R.attr.colorOnBackgroundPrimary)
        tab.apply {
            if (isSelected) {
                // 设置选中样式
                setTypeface(null, Typeface.BOLD)
                setTextColor(selectedColor)
            } else {
                // 重置所有样式
                setTypeface(null, Typeface.NORMAL)
                setTextColor(normalColor)
            }
        }
    }
    // 重置所有状态并设置新选中项
    fun setSelected(position: Int) {
        if (position == selectedPosition) return
        val prevSelected = selectedPosition
        selectedPosition = position
        if (isFirstSelected) {
            isFirstSelected = false
        } else {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator.vibrate(VibrationEffect.createOneShot(48, VibrationEffect.DEFAULT_AMPLITUDE))
        }
        // 只刷新新旧位置避免全局刷新
        if (prevSelected != -1) notifyItemChanged(prevSelected)
        notifyItemChanged(selectedPosition)
    }
}
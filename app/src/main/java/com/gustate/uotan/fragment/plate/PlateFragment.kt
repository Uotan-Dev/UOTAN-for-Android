package com.gustate.uotan.fragment.plate

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.utils.parse.plate.PlateItem
import com.gustate.uotan.utils.parse.plate.PlateParse
import com.gustate.uotan.utils.Utils
import kotlinx.coroutines.launch

class PlateFragment : Fragment() {

    private lateinit var tip: View
    private lateinit var recyclerView: RecyclerView
    // 所有选项卡的TextView集合
    private val tabTextViews = mutableListOf<TextView>()
    private var currentSelectedIndex = 0 // 默认选中第一个

    // 假设网页和element每个 tab 对应的 content 参数
    private val tabContents = listOf(
        "/watched/forums/", // 我的关注
        "/categories/21/", // 小米手机
        "/categories/533/", // 红米手机
        "565 ", // 一加手机
        "494 ", // 魅族手机
        "56 ", // 平板/笔记本电脑
        "93 ", // 智能生活
        "251 ", // 系统软件
        "13 " // 站务专区
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_plate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tip = view.findViewById(R.id.tip)

        loadInitialData()
        setupTabs(view)

        val rootLayout = view.findViewById<View>(R.id.main)
        val rootView = view.findViewById<View>(R.id.rootView)
        val statusBarView = view.findViewById<View>(R.id.statusBarView)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarView.updateLayoutParams<ViewGroup.LayoutParams> { height = systemBars.top }
            rootView.setPadding(0, systemBars.top + Utils.dp2Px(44, requireContext()).toInt(), 0, systemBars.bottom + Utils.dp2Px(70, requireContext()).toInt())
            insets
        }

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.adapter = PlateAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(context)

    }

    private fun setupTabs(view: View) {

        tabTextViews.add(view.findViewById(R.id.follow))
        tabTextViews.add(view.findViewById(R.id.xiaomi))
        tabTextViews.add(view.findViewById(R.id.redmi))
        tabTextViews.add(view.findViewById(R.id.oneplus))
        tabTextViews.add(view.findViewById(R.id.meizu))
        tabTextViews.add(view.findViewById(R.id.tablet_laptop))
        tabTextViews.add(view.findViewById(R.id.smart_life))
        tabTextViews.add(view.findViewById(R.id.system_app))
        tabTextViews.add(view.findViewById(R.id.forum_services))

        for (index in tabTextViews.indices) {
            tabTextViews[index].setOnClickListener { onTabClicked(index) }
        }

    }

    private fun onTabClicked(index: Int) {
        if (index == currentSelectedIndex) return

        lifecycleScope.launch {
            // 获取新数据
            val newData = PlateParse.fetchPlateData(tabContents[index])

            if (newData != mutableListOf<PlateItem>()) {
                tip.isVisible = false
                recyclerView.isVisible = true
                recyclerView.adapter = PlateAdapter(newData)
            } else {
                tip.isVisible = true
                recyclerView.isVisible = false
            }

            // 更新样式
            updateTabStyle(index)
            currentSelectedIndex = index
        }
    }

    private fun loadInitialData() {
        lifecycleScope.launch {
            // 加载默认数据（第一个tab）
            val initialData = PlateParse.fetchPlateData(tabContents[0])

            if (initialData != mutableListOf<PlateItem>()) {
                tip.isVisible = false
                recyclerView.isVisible = true
                recyclerView.adapter = PlateAdapter(initialData)
            } else {
                tip.isVisible = true
                recyclerView.isVisible = false
            }
            updateTabStyle(currentSelectedIndex) // 初始选中第一个
        }
    }

    private fun updateTabStyle(selectedIndex: Int) {

        val normalColor = ContextCompat.getColor(requireContext(), R.color.label_secondary)
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.label_primary)

        // 重置所有样式
        tabTextViews.forEach { tv ->
            tv.setTypeface(null, Typeface.NORMAL)
            tv.setTextColor(normalColor)
            tv.isSelected = false
        }

        // 设置选中样式
        tabTextViews[selectedIndex].apply {
            setTypeface(null, Typeface.BOLD)
            setTextColor(selectedColor)
            isSelected = true
        }
    }

}

class PlateAdapter(private var data: MutableList<PlateItem>) :
    RecyclerView.Adapter<PlateAdapter.ViewHolder>() {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val item: View = view.findViewById(R.id.item)
        val cover: ImageView = view.findViewById(R.id.coverImage)
        val title: TextView = view.findViewById(R.id.title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_plate, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = data[position]
        Glide.with(holder.itemView.context)
            .load(content.cover)
            .into(holder.cover)
        holder.title.text = content.title
    }

    override fun getItemCount(): Int = data.size
}
package com.gustate.uotan.fragment.plate

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.activity.PlateActivity
import com.gustate.uotan.gustatex.dialog.LoadingDialog
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.parse.plate.PlateItem
import com.gustate.uotan.utils.parse.plate.PlateParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class PlateFragment : Fragment() {

    class PlateAdapter : ListAdapter<PlateItem, PlateAdapter.ViewHolder>(DiffCallback()) {

        var onItemClick: ((String, String, String) -> Unit)? = null

        class DiffCallback : DiffUtil.ItemCallback<PlateItem>() {
            override fun areItemsTheSame(old: PlateItem, new: PlateItem) = old.cover == new.cover
            override fun areContentsTheSame(old: PlateItem, new: PlateItem) = old == new
        }

        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val item: View = view.findViewById(R.id.item)
            val cover: ImageView = view.findViewById(R.id.coverImage)
            val title: TextView = view.findViewById(R.id.title)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_plate_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val content = getItem(position)
            Glide.with(holder.itemView.context)
                .load(BASE_URL + content.cover)
                .error(R.drawable.ic_uo)
                .into(holder.cover)
            holder.title.text = content.title
            holder.item.setOnClickListener {
                onItemClick?.invoke(content.link, content.cover, content.title)
            }
        }

        fun updateList(newData: List<PlateItem>) {
            submitList(newData.toMutableList())
        }
    }

    private lateinit var recyclerView: RecyclerView
    // 所有选项卡的TextView集合
    private val tabTextViews = mutableListOf<TextView>()
    private var currentSelectedIndex = 0 // 默认选中第一个
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var plateAdapter: PlateAdapter

    // 假设网页和element每个 tab 对应的 content 参数
    private val tabContents = listOf(
        "/watched/forums/",
        "/categories/21/",
        "/categories/533/",
        "/categories/oneplus/",
        "/categories/499/",
        "/categories/385/",
        "/categories/redmi-pad.546/ ",
        "/categories/539/",
        "/categories/219/",
        "/categories/403/",
        "/categories/94/",
        "/categories/95/",
        "/categories/Win/",
        "251 ",
        "13 "
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

        loadingDialog = LoadingDialog(requireContext())

        loadInitialData()
        setupTabs(view)

        val statusBarView = view.findViewById<View>(R.id.statusBarView)
        val statusBarBlurView = view.findViewById<View>(R.id.statusBarBlurView)
        val appBarLayout = view.findViewById<View>(R.id.appBarLayout)
        recyclerView = view.findViewById(R.id.recyclerView)
        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            // 获取系统栏高度 (包含 top, bottom, left 和 right)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarView.updateLayoutParams<ViewGroup.LayoutParams> { height = systemBars.top }
            statusBarBlurView.updateLayoutParams<ViewGroup.LayoutParams> { height = systemBars.top }
            appBarLayout.updateLayoutParams<MarginLayoutParams> { topMargin = systemBars.top }
            recyclerView.setPadding(
                0,
                (126f.dpToPx(requireContext()).roundToInt()),
                0,
                (70f.dpToPx(requireContext()).roundToInt() + systemBars.top + systemBars.bottom)
            )
            // 返回 insets
            insets
        }
        plateAdapter = PlateAdapter().apply {
            onItemClick = { link, cover, title ->
                startActivity(
                    Intent(
                        requireContext(),
                        PlateActivity::class.java
                    ).apply {
                        putExtra("link", link)
                        putExtra("cover", cover)
                        putExtra("title", title)
                    }
                )
            }
        }
        recyclerView.adapter = plateAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupTabs(view: View) {
        tabTextViews.add(view.findViewById(R.id.follow))
        tabTextViews.add(view.findViewById(R.id.xiaomi))
        tabTextViews.add(view.findViewById(R.id.redmi))
        tabTextViews.add(view.findViewById(R.id.oneplus))
        tabTextViews.add(view.findViewById(R.id.meizu))
        tabTextViews.add(view.findViewById(R.id.mipad))
        tabTextViews.add(view.findViewById(R.id.redmipad))
        tabTextViews.add(view.findViewById(R.id.lenovopad))
        tabTextViews.add(view.findViewById(R.id.xiaomi_laptop))
        tabTextViews.add(view.findViewById(R.id.mijia))
        tabTextViews.add(view.findViewById(R.id.miband))
        tabTextViews.add(view.findViewById(R.id.miwatch))
        tabTextViews.add(view.findViewById(R.id.windows))
        tabTextViews.add(view.findViewById(R.id.system_app))
        tabTextViews.add(view.findViewById(R.id.forum_services))
        for (index in tabTextViews.indices) { tabTextViews[index].setOnClickListener { onTabClicked(index) } }
    }

    private fun onTabClicked(index: Int) {
        if (index == currentSelectedIndex) return
        // 更新样式
        updateTabStyle(index)
        currentSelectedIndex = index
        loadingDialog.show()
        lifecycleScope.launch {
            // 获取新数据
            val newData = PlateParse.fetchPlateData(tabContents[index])
            withContext(Dispatchers.Main) {
                if (newData != mutableListOf<PlateItem>()) {
                    plateAdapter.updateList(newData)
                } else {
                    plateAdapter.updateList(mutableListOf(PlateItem("", getString(R.string.no_follow_plate), "")))
                }
                loadingDialog.cancel()
            }
        }
    }

    private fun loadInitialData() {
        lifecycleScope.launch {
            // 加载默认数据（第一个tab）
            val initialData = PlateParse.fetchPlateData(tabContents[0])
            withContext(Dispatchers.Main) {
                if (initialData != mutableListOf<PlateItem>()) {
                    plateAdapter.updateList(initialData)
                } else {
                    plateAdapter.updateList(mutableListOf(PlateItem("", getString(R.string.no_follow_plate), "")))
                }
                updateTabStyle(currentSelectedIndex) // 初始选中第一个
            }
        }
    }

    @SuppressLint("Recycle", "ResourceType")
    private fun updateTabStyle(selectedIndex: Int) {

        val typedArray = requireContext().obtainStyledAttributes(intArrayOf(R.attr.colorOnBackgroundPrimary, R.attr.colorOnBackgroundSecondary))
        // 底栏按钮默认颜色
        val normalColor = typedArray.getColor(1, Color.RED)
        // 底栏按钮选择颜色
        val selectedColor = typedArray.getColor(0, Color.RED)
        typedArray.recycle()
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
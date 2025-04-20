package com.gustate.uotan.activity

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
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.databinding.ActivitySelectSectionBinding
import com.gustate.uotan.gustatex.dialog.LoadingDialog
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.openImmersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import kotlin.math.roundToInt

class SelectSectionActivity : BaseActivity() {

    /**
     * 一个版块列表数据类
     */
    private data class SectionItem(
        val sectionImageUrl: String,
        val sectionName: String,
        val sectionPostUrl: String
    )

    /**
     * 为版块列表写一个适配器
     */
    private class SectionAdapter : ListAdapter<SectionItem, SectionAdapter.ViewHolder>(DiffCallback()) {

        var onItemClick: ((String, String) -> Unit)? = null

        class DiffCallback : DiffUtil.ItemCallback<SectionItem>() {
            override fun areItemsTheSame(old: SectionItem, new: SectionItem) = old.sectionName == new.sectionName
            override fun areContentsTheSame(old: SectionItem, new: SectionItem) = old == new
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
                .load(BASE_URL + content.sectionImageUrl)
                .error(R.drawable.ic_uo)
                .into(holder.cover)
            holder.title.text = content.sectionName
            holder.item.setOnClickListener {
                onItemClick?.invoke(content.sectionPostUrl, content.sectionName)
            }
        }

        override fun getItemCount(): Int = currentList.size

        fun updateList(newData: List<SectionItem>) {
            submitList(newData.toMutableList())
        }
    }

    /** 全类变量 **/
    /* 延迟启动 */
    // 视图绑定
    private lateinit var binding: ActivitySelectSectionBinding
    // 版块列表 adapter
    private lateinit var sectionAdapter: SectionAdapter
    // 延迟启动一个加载 Dialog
    private lateinit var loadingDialog: LoadingDialog
    // 创建一个空的 textView Tab 的可变列表
    private var tabTextViews = mutableListOf<TextView>()
    // 选中的 tab
    private var currentSelectedIndex = 100
    // 假设网页和element每个 tab 对应的 content 参数
    private val tabContents = listOf(
        "xiaomiphone", // 小米手机
        "heisha", // 黑鲨手机
        "redmi", // 红米手机
        "yijia", // 一加手机
        "meizu", // 魅族手机
        "xmpb", // 小米平板
        "redmipad", // 红米平板
        "lenovo", // 联想平板
        "laotop", // 小米笔记本
        "mihome", // 米家
        "miband", // 小米手环
        "miwatch", // 小米手表
        "windows", // Windows
        "software", // 系统软件
        "forum" // 站务专区
    )

    /**
     * 当窗体创建时
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** 窗体设置 **/
        // 启用边到边设计
        enableEdgeToEdge()
        // 针对部分系统的系统栏沉浸
        openImmersion(window)
        // 实例化 binding
        binding = ActivitySelectSectionBinding.inflate(layoutInflater)
        // 绑定视图
        setContentView(binding.main)

        /** 变量设置 **/
        // 实例化 Loading Dialog
        loadingDialog = LoadingDialog(this)

        /** 基础设置 **/
        // recyclerView 设置
        // 为 recyclerView 设置纵向 layoutManager
        binding.recyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        sectionAdapter = SectionAdapter()
        // 为 recyclerView 设置一个空的 adapter
        binding.recyclerView.adapter = sectionAdapter.apply {
            onItemClick = { content, title ->
                val resultIntent = Intent().apply {
                    putExtra("section_name", title)
                    putExtra("section_url", content)
                }
                // 设置结果码
                setResult(RESULT_OK, resultIntent)
                // 关闭当前 Activity
                finish()
            }
        }

        setupTabTextViews()
        onTabClicked(0)

        /** 获取系统栏高度并同步到占位布局 **/
        // 使用 ViewCompat 的回调函数
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            // 获取系统栏高度 (包含 top, bottom, left 和 right)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusBarView.layoutParams.height = systemBars.top
            binding.statusBarBlurView.layoutParams.height = systemBars.top
            val appBarLayoutMargin = binding.appBarLayout.layoutParams as MarginLayoutParams
            appBarLayoutMargin.topMargin = systemBars.top
            binding.recyclerView.setPadding(
                0,
                126f.dpToPx(this).roundToInt(),
                0,
                systemBars.top + systemBars.bottom
            )
            // 返回 insets
            insets
        }
    }

    private fun setupTabTextViews() {
        tabTextViews.add(binding.xiaomi)
        tabTextViews.add(binding.blackshark)
        tabTextViews.add(binding.redmi)
        tabTextViews.add(binding.oneplus)
        tabTextViews.add(binding.meizu)
        tabTextViews.add(binding.mipad)
        tabTextViews.add(binding.redmipad)
        tabTextViews.add(binding.lenovopad)
        tabTextViews.add(binding.xiaomiLaptop)
        tabTextViews.add(binding.mijia)
        tabTextViews.add(binding.miband)
        tabTextViews.add(binding.miwatch)
        tabTextViews.add(binding.windows)
        tabTextViews.add(binding.systemApp)
        tabTextViews.add(binding.forumServices)
        for (index in tabTextViews.indices) {
            tabTextViews[index].setOnClickListener { onTabClicked(index) }
        }
    }

    private fun onTabClicked(index: Int) {
        if (index == currentSelectedIndex) return
        // 更新样式
        updateTabStyle(index)
        currentSelectedIndex = index
        loadingDialog.show()
        lifecycleScope.launch {
            // 获取新数据
            val newData = fetchSectionList(tabContents[index])
            withContext(Dispatchers.Main) {
                sectionAdapter.updateList(newData)
                loadingDialog.cancel()
            }
        }
    }

    @SuppressLint("ResourceType", "Recycle")
    private fun updateTabStyle(index: Int){
        val typedArray = this.obtainStyledAttributes(intArrayOf(R.attr.colorOnBackgroundPrimary, R.attr.colorOnBackgroundSecondary))
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
        tabTextViews[index].apply {
            setTypeface(null, Typeface.BOLD)
            setTextColor(selectedColor)
            isSelected = true
        }
    }

    /**
     * 获取版块列表
     */
    private suspend fun fetchSectionList(section: String) : MutableList<SectionItem> = withContext(Dispatchers.IO) {
        // 创建一个空的可变列表储存结果
        val resultMutableList = mutableListOf<SectionItem>()
        // 获取当前页面的版块列表
        val sectionsList = when(section) {
            "software" -> {
                Jsoup.connect("$BASE_URL/pages/postpage/")
                    .cookies(Cookies)
                    .timeout(TIMEOUT_MS)
                    .userAgent(USER_AGENT)
                    .get()
                    .getElementsByClass("fullContent wear")
                    .first()
                    ?.getElementsByTag("li")
                    ?.let { elements ->
                        (elements as? MutableList<Element>)?.removeAt(0)
                        // 返回修改后的列表
                        elements
                    }
            }
            "forum" -> {
                Jsoup.connect("$BASE_URL/pages/postpage/")
                    .cookies(Cookies)
                    .timeout(TIMEOUT_MS)
                    .userAgent(USER_AGENT)
                    .get()
                    .getElementsByClass("fullContent forum")
                    .first()
                    ?.getElementsByTag("li")
            }
            else -> {
                Jsoup.connect("$BASE_URL/pages/$section/")
                    .cookies(Cookies)
                    .timeout(TIMEOUT_MS)
                    .userAgent(USER_AGENT)
                    .get()
                    .getElementsByClass("mainContent")
                    .first()
                    ?.getElementsByTag("li")
            }
        }
        // 逐一获取每个版块的 Element
        sectionsList?.forEach {
            // 获取版块图片的 Url
            val sectionImageUrl = it
                .getElementsByTag("img")
                .first()
                ?.attr("src")
                ?: ""
            // 获取版块名称
            val sectionName = it
                .getElementsByTag("span")
                .first()
                ?.text()
                ?: ""
            // 逐一获取版块发布页链接
            val sectionPostUrl = it
                .getElementsByTag("a")
                .first()
                ?.attr("href")
                ?: ""
            // 为结果列表添加内容
            resultMutableList.add(SectionItem(sectionImageUrl, sectionName, sectionPostUrl))
        }
        // 将列表结果返回
        return@withContext resultMutableList
    }
}
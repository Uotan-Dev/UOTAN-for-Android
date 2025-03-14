package com.gustate.uotan.fragment.resource

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.activity.ResourceActivity
import com.gustate.uotan.utils.Utils
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.parse.resource.FetchResult
import com.gustate.uotan.utils.parse.resource.ResourceItem
import com.gustate.uotan.utils.parse.resource.ResourceParse
import com.gustate.uotan.utils.parse.resource.ResourceRecommendItem
import com.gustate.uotan.utils.parse.resource.ResourceRecommendParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException

class TrendsResourceFragment : Fragment() {

    /** 定义全类可变变量 **/
    // 推荐列表框
    private lateinit var reRecyclerView: RecyclerView
    // 抓取推荐结果
    private lateinit var reFetchResult: FetchResult
    // 最新列表框
    private lateinit var laRecyclerView: RecyclerView
    // 抓取最新结果
    private lateinit var laFetchResult: FetchResult
    // 最新当前页面
    private var laCurrentPage = 1
    // 最新总页面
    private var laTotalPages = 1
    // 最新是否正在加载
    private var isLaLoading = false
    // 最新是否最后一页
    private var isLaLastPage = false

    /** 定义全类非可变变量 **/
    // 一个空 adapter, 防止 No adapter attached; skipping layout ERROR
    private val nullTrendsAdapter = TrendsResourceAdapter(mutableListOf())
    private lateinit var laNullAdapter: LatestResourceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trends_resource, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        laNullAdapter = LatestResourceAdapter(mutableListOf())
        val reRecyclerView = view.findViewById<RecyclerView>(R.id.reRecyclerView)
        val reLinearLayout = LinearLayoutManager(requireContext())
        laRecyclerView = view.findViewById(R.id.laRecyclerView)
        laRecyclerView.adapter = laNullAdapter
        val laLinearLayout = LinearLayoutManager(requireContext())
        laLinearLayout.orientation = LinearLayoutManager.VERTICAL
        laRecyclerView.layoutManager = laLinearLayout
        val rootScrollView = view.findViewById<View>(R.id.rootScrollView)
        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            rootScrollView.setPadding(
                0,0,0,
                systemBars.bottom + Utils.dp2Px(70, requireContext()).toInt()
            )
            insets
        }
        // 加载数据
        loadLatestData()
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val trendsResourceData = ResourceRecommendParse.fetchResourceRecommendData()
                withContext(Dispatchers.Main) {
                    reLinearLayout.orientation = LinearLayoutManager.HORIZONTAL
                    reRecyclerView.layoutManager = reLinearLayout
                    val trendsResourceAdapter = TrendsResourceAdapter(trendsResourceData).apply {
                        onItemClick = { selectedItem ->
                            // 安全上下文检查
                            context?.let {
                                startActivity(Intent(it, ResourceActivity::class.java).apply {
                                    putExtra("url", selectedItem.link)
                                })
                            }
                        }
                    }
                    reRecyclerView.adapter = trendsResourceAdapter
                }
            }
        }
        // 为 recyclerView 设置滚动监听
        laRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            // recyclerView 当前序号
            private var lastVisibleItem = 0
            // recyclerView 列表总数
            private var totalItemCount = 0

            /**
             * recyclerView 滚动时
             */
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // 获取 recyclerView 的线性布局管理器
                val layoutManager = laRecyclerView.layoutManager as LinearLayoutManager
                // 获取当前 recyclerView 的项目总数
                totalItemCount = layoutManager.itemCount
                // 获取 recyclerView 滚动到的项目
                lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                // 当没有在加载、不是最后一页、项目总数 - 5 小于当前项目数
                if (!isLaLoading && !isLaLastPage && totalItemCount <= (lastVisibleItem + 5)) {
                    Log.e("666", lastVisibleItem.toString())
                    // 加载数据
                    loadLatestData()
                }
            }
        })
    }
    /**
     * 数据加载的方法
     */
    private fun loadLatestData() {

        // 如果正在加载或在最后一页不执行该方法
        if (isLaLoading || isLaLastPage) return

        // 设置为正在加载
        isLaLoading = true

        // 使用 try catch 方法健壮代码
        try {
            // 启动协程
            lifecycleScope.launch {
                // 切换到 IO 线程执行
                withContext(Dispatchers.IO) {
                    // 获取推荐数据
                    laFetchResult = ResourceParse.fetchResourceData(laCurrentPage.toString())
                    val adapter = laRecyclerView.adapter as LatestResourceAdapter
                    // 切换到 Main 线程执行
                    withContext(Dispatchers.Main) {
                        if (laFetchResult.totalPage != 1) {
                            laFetchResult.items.let { newItems ->
                                if (adapter == laNullAdapter) {
                                    // 创建新Adapter时设置点击监听
                                    val newAdapter = LatestResourceAdapter(newItems.toMutableList()).apply {
                                        onItemClick = { selectedItem ->
                                            // 安全上下文检查
                                            context?.let {
                                                startActivity(Intent(it, ResourceActivity::class.java).apply {
                                                    putExtra("url", selectedItem.link)
                                                })
                                            }
                                        }
                                    }
                                    laRecyclerView.adapter = newAdapter
                                } else {
                                    adapter.addAll(newItems)
                                }
                                laCurrentPage += 1
                            }
                            laTotalPages = laFetchResult.totalPage
                            isLaLastPage = laCurrentPage > laTotalPages
                        } else {
                            Toast.makeText(context,"请稍候再试", Toast.LENGTH_SHORT).show()
                        }
                        isLaLoading = false
                    }
                }
            }
        } catch (e: SocketTimeoutException) {
            e.printStackTrace()
        }
    }
}

class TrendsResourceAdapter(private val trendsList: MutableList<ResourceRecommendItem>):
    RecyclerView.Adapter<TrendsResourceAdapter.ViewHolder>() {

    // 点击监听接口
    var onItemClick: ((ResourceRecommendItem) -> Unit)? = null

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val cover: ImageView = view.findViewById(R.id.cover)
        val title: TextView = view.findViewById(R.id.title)
        val describe: TextView = view.findViewById(R.id.describe)
        val updateTime: TextView = view.findViewById(R.id.update_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_trends_resource_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = trendsList[position]
        Glide
            .with(holder.itemView.context)
            .load(BASE_URL + content.cover)
            .into(holder.cover)
        holder.title.text = content.title
        holder.describe.text = content.version
        holder.updateTime.text = content.updateTime
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(content)
        }
    }

    override fun getItemCount(): Int = trendsList.size

}

class LatestResourceAdapter(private val resourceList: MutableList<ResourceItem>):
    RecyclerView.Adapter<LatestResourceAdapter.ViewHolder>() {

    // 点击监听接口
    var onItemClick: ((ResourceItem) -> Unit)? = null

    fun addAll(newItems: MutableList<ResourceItem>) {
        val startPosition = resourceList.size
        resourceList.addAll(newItems)
        notifyItemRangeInserted(startPosition, newItems.size)
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.cover)
        val title: TextView = view.findViewById(R.id.title)
        val describe: TextView = view.findViewById(R.id.describe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_latest_resource_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = resourceList[position]
        Glide
            .with(holder.itemView.context)
            .load(BASE_URL + content.cover)
            .into(holder.avatar)
        holder.title.text = content.title
        holder.describe.text = content.version
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(content)
        }
    }

    override fun getItemCount(): Int = resourceList.size

}
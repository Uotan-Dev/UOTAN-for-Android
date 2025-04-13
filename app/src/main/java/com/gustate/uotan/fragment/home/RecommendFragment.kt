package com.gustate.uotan.fragment.home

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.activity.ArticleActivity
import com.gustate.uotan.utils.Utils
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.parse.home.FetchResult
import com.gustate.uotan.utils.parse.home.ForumRecommendItem
import com.gustate.uotan.utils.parse.home.RecommendParse
import com.scwang.smart.refresh.layout.api.RefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import kotlin.math.roundToInt
import androidx.core.content.withStyledAttributes
import com.gustate.uotan.utils.Utils.Companion.idToAvatar


/**
 * 推荐内容 (Fragment)
 * JiaGuZhuangZhi Miles
 * Gustate 03/02/2025
 * I Love Jiang’Xun
 */

class RecommendFragment : Fragment() {

    /** 定义全类可变变量 **/
    // 推荐列表框
    private lateinit var recyclerView: RecyclerView
    // 抓取内容结果
    private lateinit var fetchResult: FetchResult
    // 当前页面
    private var currentPage = 1
    // 总页面
    private var totalPages = 1
    // 是否正在加载
    private var isLoading = false
    // 是否最后一页
    private var isLastPage = false

    /** 定义全类非可变变量 **/
    // 一个空 adapter, 防止 No adapter attached; skipping layout ERROR
    private val nullAdapter = RecommendAdapter(mutableListOf())

    /**
     * 加载视图时
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 加载布局
        return inflater.inflate(
            R.layout.fragment_recommend,
            container,
            false
        )
    }

    /**
     * 视图加载完成时
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** 从布局中取出需要调用的视图 **/
        // 初始化 RecyclerView
        recyclerView = view.findViewById(R.id.recommendRecycler)
        // 刷新布局
        val refreshLayout: RefreshLayout = view.findViewById(R.id.refreshLayout)

        /** 获取系统栏高度并同步到占位布局 **/
        // 使用 ViewCompact 设置 insets 监听器
        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            // 获取系统栏高度 (包含 top, bottom, left 和 right)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 为 refreshLayout 设置必要的 padding
            refreshLayout.layout.setPadding(
                0,
                (systemBars.top + 100f.dpToPx(requireContext())).roundToInt(),
                0,
                (systemBars.bottom + 70f.dpToPx(requireContext())).roundToInt()
            )
            // 返回 insets
            insets
        }

        /** 初始加载 **/
        // 初始加载数据
        loadData()

        /** recyclerView 设置 **/
        // 创建线性布局管理器
        val linearLayout = LinearLayoutManager(requireContext())
        // 设置方向为纵向
        linearLayout.orientation = LinearLayoutManager.VERTICAL
        // 为 recyclerView 设置布局管理器
        recyclerView.layoutManager = linearLayout
        // 设置 adapter 适配器
        recyclerView.adapter = nullAdapter
        // 为 recyclerView 设置滚动监听
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                // 获取当前 recyclerView 的项目总数
                totalItemCount = layoutManager.itemCount
                // 获取 recyclerView 滚动到的项目
                lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                // 当没有在加载、不是最后一页、项目总数 - 5 小于当前项目数
                if (!isLoading && !isLastPage && totalItemCount <= (lastVisibleItem + 5)) {
                    // 加载数据
                    loadData()
                }
            }
        })
    }

    /**
     * 数据加载的方法
     */
    private fun loadData() {

        // 如果正在加载或在最后一页不执行该方法
        if (isLoading || isLastPage) return

        // 设置为正在加载
        isLoading = true

        // 使用 try catch 方法健壮代码
        try {
            // 启动协程
            lifecycleScope.launch {
                // 切换到 IO 线程执行
                withContext(Dispatchers.IO) {
                    // 获取推荐数据
                    fetchResult = RecommendParse.fetchRecommendData(currentPage)
                    val adapter = recyclerView.adapter as RecommendAdapter
                    // 切换到 Main 线程执行
                    withContext(Dispatchers.Main) {
                        if (fetchResult.totalPage != 1) {
                            fetchResult.items.let { newItems ->
                                if (adapter == nullAdapter) {
                                    // 创建新Adapter时设置点击监听
                                    val newAdapter = RecommendAdapter(newItems.toMutableList()).apply {
                                        onItemClick = { selectedItem, itemLayout ->
                                            val intent = Intent(
                                                requireContext(),
                                                ArticleActivity::class.java
                                            ).putExtra("url", selectedItem.url)
                                            startActivity(intent)
                                        }
                                    }
                                    recyclerView.adapter = newAdapter
                                } else {
                                    adapter.addAll(newItems)
                                }
                                currentPage += 1
                            }
                            totalPages = fetchResult.totalPage
                            isLastPage = currentPage > totalPages
                        } else {
                            Toast.makeText(context,"请稍候再试",Toast.LENGTH_SHORT).show()
                        }
                        isLoading = false
                    }
                }
            }
        } catch (e: SocketTimeoutException) {
            e.printStackTrace()
        }
    }
}

class RecommendAdapter(private val recommendList: MutableList<ForumRecommendItem>):
        RecyclerView.Adapter<RecommendAdapter.ViewHolder>() {

    // 点击监听接口
    var onItemClick: ((ForumRecommendItem, View) -> Unit)? = null

    fun addAll(newItems: MutableList<ForumRecommendItem>) {
        val startPosition = recommendList.size
        recommendList.addAll(newItems)
        notifyItemRangeInserted(startPosition, newItems.size)
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val itemLayout: View = view.findViewById(R.id.itemLayout)
        val coverImage: ImageView = view.findViewById(R.id.coverImage)
        val userAvatar: ImageView = view.findViewById(R.id.userAvatar)
        val userName: TextView = view.findViewById(R.id.userNameText)
        val time: TextView = view.findViewById(R.id.time)
        val title: TextView = view.findViewById(R.id.title)
        val describe: TextView = view.findViewById(R.id.describe)
        val topic: TextView = view.findViewById(R.id.topic)
        val topicCard: CardView= view.findViewById(R.id.topicCard)
        val viewCount: TextView = view.findViewById(R.id.viewCount)
        val commentCount: TextView = view.findViewById(R.id.commentCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_recommend_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = recommendList[position]
        Glide.with(holder.itemView.context)
            .load(content.cover)
            .into(holder.coverImage)

        val avatarUrl = idToAvatar(content.userId)
        Glide.with(holder.itemView.context)
            .load(avatarUrl)
            .error(R.drawable.avatar_account)
            .into(holder.userAvatar)

        holder.userName.text = content.author
        holder.time.text = content.time
        holder.title.text = content.title
        holder.describe.text = content.describe

        if (content.topic != "") {
            holder.topicCard.isVisible = true
            holder.topic.text = content.topic
        } else {
            holder.topicCard.isVisible = false
        }

        holder.viewCount.text = content.viewCount
        holder.commentCount.text = content.commentCount

        holder.itemLayout.setOnClickListener {
            onItemClick?.invoke(content, holder.itemLayout)
        }

    }

    override fun getItemCount(): Int = recommendList.size

}
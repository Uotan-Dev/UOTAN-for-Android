package com.gustate.uotan.fragment.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.ui.activity.ArticleActivity
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.idToAvatar
import com.gustate.uotan.utils.parse.home.ForumRecommendItem
import com.gustate.uotan.utils.parse.home.RecommendParse
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import kotlin.math.roundToInt


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
    private lateinit var srlRoot: SmartRefreshLayout
    private lateinit var adapter: RecommendAdapter
    // 当前页面
    private var currentPage = 1
    // 总页面
    private var totalPages = 1
    // 是否正在加载
    private var isLoading = false
    // 是否最后一页
    private var isLastPage = false

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
        recyclerView = view.findViewById(R.id.recyclerView)
        // 刷新布局
        srlRoot = view.findViewById(R.id.srlRoot)

        /** 获取系统栏高度并同步到占位布局 **/
        // 使用 ViewCompact 设置 insets 监听器
        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            // 获取系统栏高度 (包含 top, bottom, left 和 right)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 为 refreshLayout 设置必要的 padding
            srlRoot.layout.setPadding(
                0,
                (systemBars.top + 100f.dpToPx(requireContext())).roundToInt(),
                0,
                (systemBars.bottom + 70f.dpToPx(requireContext())).roundToInt()
            )
            srlRoot.setHeaderInsetStartPx((systemBars.top + 100f.dpToPx(requireContext())).roundToInt())
            srlRoot.setFooterInsetStartPx((systemBars.bottom + 70f.dpToPx(requireContext())).roundToInt())
            // 返回 insets
            insets
        }

        /** 初始加载 **/
        // 初始加载数据
        loadData(true)

        srlRoot.setOnRefreshListener {
            loadData(true)
        }

        srlRoot.setOnLoadMoreListener {
            loadData(false)
        }

    }

    /**
     * 数据加载的方法
     */
    private fun loadData(refresh: Boolean) {
        // 如果正在加载或在最后一页不执行该方法
        if (isLoading || isLastPage) return
        if (refresh) {
            /** recyclerView 设置 **/
            // 为 recyclerView 设置布局管理器
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            // 设置 adapter 适配器
            adapter = RecommendAdapter().apply {
                onItemClick = { selectedItem, itemLayout ->
                    val intent = Intent(
                        requireContext(),
                        ArticleActivity::class.java
                    ).putExtra("url", selectedItem.url)
                    startActivity(intent)
                }
            }
            recyclerView.adapter = adapter
            currentPage = 1
            totalPages = 1
            isLastPage = false
        }
        // 设置为正在加载
        isLoading = true
        // 启动协程
        lifecycleScope.launch {
            // 使用 try catch 方法健壮代码
            try {
                // 获取推荐数据
                val recommendData = RecommendParse.fetchRecommendData(currentPage)
                // 切换到 Main 线程执行
                withContext(Dispatchers.Main) {
                    recommendData.items.let {
                        adapter.addAll(it)
                        currentPage++
                    }
                    totalPages = recommendData.totalPage
                    isLastPage = currentPage > totalPages
                }
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
            } finally {
                if (isLastPage) {
                    srlRoot.finishLoadMoreWithNoMoreData()
                    srlRoot.setNoMoreData(true)
                    srlRoot.finishRefresh()
                }
                else {
                    srlRoot.finishLoadMore()
                    srlRoot.finishRefresh()
                }
                isLoading = false
            }
        }
    }

    class RecommendAdapter(): Adapter<RecommendAdapter.ViewHolder>() {

        private val recommendList = mutableListOf<ForumRecommendItem>()

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
            val title: TextView = view.findViewById(R.id.tv_title)
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

}
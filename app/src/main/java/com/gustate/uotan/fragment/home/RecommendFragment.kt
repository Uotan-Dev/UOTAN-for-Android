package com.gustate.uotan.fragment.home

import android.content.Intent
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
import com.gustate.uotan.utils.parse.home.FetchResult
import com.gustate.uotan.utils.parse.home.ForumRecommendItem
import com.gustate.uotan.utils.parse.home.RecommendParse
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import kotlinx.coroutines.launch


/**
 * 推荐内容 (Fragment)
 * JiaGuZhuangZhi Miles
 * Gustate 02/23/2025
 * I Love Jiang’Xun
 */

class RecommendFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    private var currentPage = 1
    private var totalPages = 1
    private var isLoading = false
    private var isLastPage = false
    private lateinit var fetchResult: FetchResult

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recommend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val refreshLayout: RefreshLayout = view.findViewById(R.id.refreshLayout)
        //refreshLayout.setRefreshHeader(ClassicsHeader(requireContext()))
        //refreshLayout.setRefreshFooter(ClassicsFooter(requireContext()))

        // 初始化 RecyclerView
        recyclerView = view.findViewById(R.id.recommendRecycler)
        recyclerView.layoutManager = LinearLayoutManager(context)
        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            refreshLayout.layout.setPadding(0, systemBars.top + Utils.dp2Px(114, requireContext()).toInt(), 0, systemBars.bottom + Utils.dp2Px(70, requireContext()).toInt())
            insets
        }
        loadData()
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var lastVisibleItem = 0
            private var totalItemCount = 0

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManagerd = recyclerView.layoutManager as LinearLayoutManager
                totalItemCount = layoutManagerd.itemCount
                lastVisibleItem = layoutManagerd.findLastVisibleItemPosition()

                if (!isLoading && !isLastPage && totalItemCount <= (lastVisibleItem + 5)) {
                    loadData()
                    isLoading = true
                }
            }
        })
    }


    private fun loadData() {

        if (isLoading || isLastPage) return

        isLoading = true

        try {
            // 启动协程
            lifecycleScope.launch {
                fetchResult = RecommendParse.fetchRecommendData(currentPage)
                val adapter = recyclerView.adapter as? RecommendAdapter
                if (fetchResult.totalPage != 1) {
                    fetchResult.items.let { newItems ->
                        if (adapter == null) {
                            // 创建新Adapter时设置点击监听
                            val newAdapter = RecommendAdapter(newItems.toMutableList()).apply {
                                onItemClick = { selectedItem ->
                                    // 安全上下文检查
                                    context?.let {
                                        startActivity(Intent(it, ArticleActivity::class.java).apply {
                                            putExtra("url", selectedItem.url)
                                        })
                                    }
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
        } finally {
            isLoading = false
        }
    }
}

class RecommendAdapter(private val recommendList: MutableList<ForumRecommendItem>):
        RecyclerView.Adapter<RecommendAdapter.ViewHolder>() {

    // 点击监听接口
    var onItemClick: ((ForumRecommendItem) -> Unit)? = null

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
        if (content.authorAvatar.startsWith("http")) {
            Glide.with(holder.itemView.context)
                .load(content.authorAvatar)
                .into(holder.userAvatar)
        }

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
        holder.commentCount.text =content.commentCount

        holder.itemLayout.setOnClickListener {
            onItemClick?.invoke(content)
        }

    }

    override fun getItemCount(): Int = recommendList.size

}
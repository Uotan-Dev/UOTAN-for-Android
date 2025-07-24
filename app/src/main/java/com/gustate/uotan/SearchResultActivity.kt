package com.gustate.uotan

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.databinding.ActivitySearchResultBinding
import com.gustate.uotan.threads.ui.ThreadsActivity
import com.gustate.uotan.utils.Helpers.Companion.avatarOptions
import com.gustate.uotan.utils.Utils.Companion.baseUrl
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.idToAvatar
import com.gustate.uotan.utils.parse.search.FetchResult
import com.gustate.uotan.utils.parse.search.SearchParse
import com.gustate.uotan.utils.parse.search.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class SearchResultActivity : BaseActivity() {

    class SearchAdapter(): RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
        private val searchList = mutableListOf<SearchResult>()
        // 点击监听接口
        var onItemClick: ((SearchResult) -> Unit)? = null

        fun addAll(newItems: MutableList<SearchResult>) {
            val startPosition = searchList.size
            searchList.addAll(newItems)
            notifyItemRangeInserted(startPosition, newItems.size)
        }

        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val itemLayout: View = view.findViewById(R.id.layout_item)
            val coverImage: ImageView = view.findViewById(R.id.img_cover)
            val userLayout: ConstraintLayout = view.findViewById(R.id.layout_user)
            val avatar: ImageView = view.findViewById(R.id.img_avatar)
            val userName: TextView = view.findViewById(R.id.tv_username)
            val time: TextView = view.findViewById(R.id.tv_time)
            val title: TextView = view.findViewById(R.id.tv_title)
            val describe: TextView = view.findViewById(R.id.tv_describe)
            val topic: TextView = view.findViewById(R.id.tv_topic)
            val topicCard: CardView = view.findViewById(R.id.card_topic)
            val viewCount: TextView = view.findViewById(R.id.tv_view_count)
            val viewCountIco: View = view.findViewById(R.id.ico_view_count)
            val commentCount: TextView = view.findViewById(R.id.tv_comment_count)
            val commentCountIco: View = view.findViewById(R.id.ico_comment_count)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_thread, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val content = searchList[position]
            if (content.cover.isNotEmpty() && !content.cover.startsWith("/img")) {
                holder.coverImage.isVisible = true
                Glide.with(holder.itemView.context)
                    .load(baseUrl + content.cover)
                    .into(holder.coverImage)
                val userParams = holder.userLayout.layoutParams as ViewGroup.MarginLayoutParams
                userParams.topMargin = 12f.dpToPx(holder.itemView.context).roundToInt()
                holder.userLayout.layoutParams = userParams
            } else {
                holder.coverImage.isVisible = false
                val userParams = holder.userLayout.layoutParams as ViewGroup.MarginLayoutParams
                userParams.topMargin = 0
                holder.userLayout.layoutParams = userParams
            }
            Glide.with(holder.itemView.context)
                .load(idToAvatar(content.id))
                .apply(avatarOptions)
                .into(holder.avatar)
            holder.userName.text = content.author
            holder.time.text = content.time
            holder.title.text = content.title
            holder.describe.text = content.content
            if (content.topic != "") {
                holder.topicCard.isVisible = true
                holder.topic.text = content.topic
            } else {
                holder.topicCard.isVisible = false
            }
            holder.viewCount.isGone = true
            holder.viewCountIco.isGone = true
            holder.commentCount.isGone = true
            holder.commentCountIco.isGone = true
            holder.itemLayout.setOnClickListener {
                onItemClick?.invoke(content)
            }
        }

        override fun getItemCount(): Int = searchList.size
    }

    private lateinit var adapter: SearchAdapter
    private lateinit var searchResult: FetchResult
    // 视图绑定
    private lateinit var binding: ActivitySearchResultBinding
    // 当前页面
    private var currentPage = 1
    // 总页面
    private var totalPages = 1
    // 是否正在加载
    private var isLoading = false
    // 是否最后一页
    private var isLastPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.main)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusBarView.layoutParams.height = systemBars.top
            binding.srlRoot.setPadding(
                0,
                (systemBars.top + 70f.dpToPx(this)).roundToInt(),
                0 ,
                systemBars.bottom
            )
            insets
        }
        val content = intent.getStringExtra("content") ?: ""
        binding.etSearch.text = content
        loadData(content, currentPage.toString())
        /** recyclerView 设置 **/
        // 创建线性布局管理器
        val linearLayout = LinearLayoutManager(this)
        // 设置方向为纵向
        linearLayout.orientation = LinearLayoutManager.VERTICAL
        // 为 recyclerView 设置布局管理器
        binding.recyclerView.layoutManager = linearLayout
        adapter = SearchAdapter().apply {
            onItemClick = { selectedItem ->
                startActivity(
                    Intent(
                        this@SearchResultActivity, ThreadsActivity::class.java
                    ).apply {
                        putExtra("url", selectedItem.url)
                    }
                )
            }
        }
        binding.recyclerView.adapter = adapter
        binding.srlRoot.setOnRefreshListener {
            currentPage = 1
            totalPages = 1
            loadData(content, currentPage.toString())
        }
        binding.srlRoot.setOnLoadMoreListener {
            loadData(content, currentPage.toString())
        }
        binding.etSearch.setOnClickListener {
            finish()
        }
        binding.back.setOnClickListener {
            finish()
        }
    }

    private fun loadData(content: String, page: String) {
        // 如果正在加载或在最后一页不执行该方法
        if (isLoading || isLastPage) return

        // 设置为正在加载
        isLoading = true

        // 启动协程
        lifecycleScope.launch {
            // 使用 try catch 方法健壮代码
            try {
                // 获取推荐数据
                searchResult = SearchParse.searchInfoParse(content, page)
                val adapter = binding.recyclerView.adapter as SearchAdapter
                // 切换到 Main 线程执行
                withContext(Dispatchers.Main) {
                    searchResult.items.let { newItems ->
                        adapter.addAll(newItems)
                        currentPage += 1
                    }
                    totalPages = searchResult.totalPage
                    isLastPage = currentPage > totalPages
                }

            } finally {
                if (isLastPage) {
                    binding.srlRoot.finishLoadMoreWithNoMoreData()
                    binding.srlRoot.setNoMoreData(true)
                }
                else {
                    binding.srlRoot.finishLoadMore()
                }
                isLoading = false
            }
        }
    }
}
package com.gustate.uotan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
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
import com.gustate.uotan.activity.ArticleActivity
import com.gustate.uotan.databinding.ActivitySearchResultBinding
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.openImmersion
import com.gustate.uotan.utils.parse.search.FetchResult
import com.gustate.uotan.utils.parse.search.SearchParse
import com.gustate.uotan.utils.parse.search.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import kotlin.math.roundToInt

class SearchResultActivity : BaseActivity() {

    class SearchAdapter(private val context: Context, private val searchList: MutableList<SearchResult>):
        RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

        // 点击监听接口
        var onItemClick: ((SearchResult) -> Unit)? = null

        fun addAll(newItems: MutableList<SearchResult>) {
            val startPosition = searchList.size
            searchList.addAll(newItems)
            notifyItemRangeInserted(startPosition, newItems.size)
        }

        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val itemLayout: View = view.findViewById(R.id.itemLayout)
            val coverImage: ImageView = view.findViewById(R.id.coverImage)
            val userLayout: ConstraintLayout = view.findViewById(R.id.userLayout)
            val userAvatar: CardView = view.findViewById(R.id.userAvatarCard)
            val userName: TextView = view.findViewById(R.id.userNameText)
            val time: TextView = view.findViewById(R.id.time)
            val title: TextView = view.findViewById(R.id.title)
            val describe: TextView = view.findViewById(R.id.describe)
            val topic: TextView = view.findViewById(R.id.topic)
            val topicCard: CardView = view.findViewById(R.id.topicCard)
            val viewCount: TextView = view.findViewById(R.id.viewCount)
            val viewCountIco: View = view.findViewById(R.id.viewCountIco)
            val commentCount: TextView = view.findViewById(R.id.commentCount)
            val commentCountIco: View = view.findViewById(R.id.commentCountIco)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_recommend_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val content = searchList[position]
            if (content.cover.isNotEmpty() && !content.cover.startsWith("http")) {
                holder.coverImage.isVisible = true
                Glide.with(holder.itemView.context)
                    .load(BASE_URL + content.cover)
                    .into(holder.coverImage)
                val userParams = holder.userLayout.layoutParams as ViewGroup.MarginLayoutParams
                userParams.topMargin = 12f.dpToPx(context).roundToInt()
                holder.userLayout.layoutParams = userParams
            } else {
                holder.coverImage.isVisible = false
                val userParams = holder.userLayout.layoutParams as ViewGroup.MarginLayoutParams
                userParams.topMargin = 0
                holder.userLayout.layoutParams = userParams
            }
            holder.userAvatar.isVisible = false
            val nameParams = holder.userName.layoutParams as ViewGroup.MarginLayoutParams
            nameParams.leftMargin = 0
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

    private lateinit var content: String
    private lateinit var adapter: SearchAdapter
    private lateinit var nullAdapter: SearchAdapter
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
        enableEdgeToEdge()
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.main)
        openImmersion(window)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusBarView.layoutParams.height = systemBars.top
            binding.refreshLayout.setPadding(
                0,
                (systemBars.top + 60f.dpToPx(this)).roundToInt(),
                0 ,
                systemBars.bottom
            )
            insets
        }
        content = intent.getStringExtra("content") ?: ""
        binding.etSearch.text = content
        loadData(content, currentPage.toString())
        // 为 recyclerView 设置滚动监听
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                    loadData(content, currentPage.toString())
                }
            }
        })
        /** recyclerView 设置 **/
        // 创建线性布局管理器
        val linearLayout = LinearLayoutManager(this)
        // 设置方向为纵向
        linearLayout.orientation = LinearLayoutManager.VERTICAL
        // 为 recyclerView 设置布局管理器
        binding.recyclerView.layoutManager = linearLayout
        nullAdapter = SearchAdapter(this, mutableListOf())
        adapter = nullAdapter
        binding.recyclerView.adapter = adapter
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

        // 使用 try catch 方法健壮代码
        try {
            // 启动协程
            lifecycleScope.launch {
                // 切换到 IO 线程执行
                withContext(Dispatchers.IO) {
                    // 获取推荐数据
                    searchResult = SearchParse.searchInfoParse(content, page)
                    val adapter = binding.recyclerView.adapter as SearchAdapter
                    // 切换到 Main 线程执行
                    withContext(Dispatchers.Main) {
                        searchResult.items.let { newItems ->
                            if (adapter == nullAdapter) {
                                // 创建新Adapter时设置点击监听
                                val newAdapter = SearchAdapter(
                                    this@SearchResultActivity,
                                    newItems.toMutableList()
                                ).apply {
                                    onItemClick = { selectedItem ->
                                        // 安全上下文检查
                                        startActivity(
                                            Intent(
                                                this@SearchResultActivity,
                                                ArticleActivity::class.java
                                            ).apply {
                                                putExtra("url", selectedItem.url)
                                            }
                                        )
                                    }
                                }
                                binding.recyclerView.adapter = newAdapter
                            } else {
                                adapter.addAll(newItems)
                            }
                            currentPage += 1
                        }
                        totalPages = searchResult.totalPage
                        isLastPage = currentPage > totalPages
                        isLoading = false
                    }
                }
            }
        } catch (e: SocketTimeoutException) {
            e.printStackTrace()
        }
    }

}
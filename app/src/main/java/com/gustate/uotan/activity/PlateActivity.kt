package com.gustate.uotan.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.anim.TitleAnim
import com.gustate.uotan.databinding.ActivityPlateBinding
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.idToAvatar
import com.gustate.uotan.utils.Utils.Companion.openImmersion
import com.gustate.uotan.utils.parse.plate.PlateContentItem
import com.gustate.uotan.utils.parse.plate.PlateContentParse.Companion.fetchPlateContentData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class PlateActivity : BaseActivity() {

    private lateinit var binding: ActivityPlateBinding
    private lateinit var nullAdapter: PlateAdapter
    private lateinit var adapter: PlateAdapter
    private lateinit var topAdapter: ExpandableTopAdapter
    private lateinit var nextPageUrl: String
    private var currentPage = 1
    private var isLoading = false
    private var hasMoreData = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        openImmersion(window)
        binding = ActivityPlateBinding.inflate(layoutInflater)
        setContentView(binding.main)
        nullAdapter = PlateAdapter(this, mutableListOf())
        binding.recyclerView.adapter = nullAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        topAdapter = ExpandableTopAdapter()
        binding.topRecyclerView.adapter = topAdapter
        binding.topRecyclerView.layoutManager = LinearLayoutManager(this)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusBarView.updateLayoutParams<LayoutParams> { height = systemBars.top }
            TitleAnim(
                binding.title,
                binding.bigTitle,
                (systemBars.top + 60f.dpToPx(this)),
                systemBars.top.toFloat()
            )
            binding.refreshLayout.setPadding(systemBars.left, (systemBars.top + 60f.dpToPx(this)).roundToInt(), systemBars.right, systemBars.bottom)
            insets
        }
        val url = intent.getStringExtra("link")
        val ico = intent.getStringExtra("cover")
        val title = intent.getStringExtra("title")
        binding.title.text = title
        binding.tvBigTitle.text = title
        Glide.with(this)
            .load(BASE_URL + ico)
            .error(R.drawable.ic_uo)
            .into(binding.imageView5)
        binding.rootScrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            // 检测是否到达触发加载的位置
            if (shouldLoadMore()) {
                isLoading = true
                loadMoreData()
            }
        }
        binding.btnExpand.setOnClickListener {
            val willExpand = !topAdapter.isExpanded
            topAdapter.setExpanded(willExpand)
            binding.btnExpand.text = if (willExpand) getString(R.string.retract) else getString(R.string.expand)
        }
        isLoading = true
        lifecycleScope.launch {
            val data = fetchPlateContentData(url!!)
            currentPage ++
            hasMoreData = data.totalPage >= currentPage
            nextPageUrl = data.pageUrl + currentPage
            withContext(Dispatchers.Main) {
                binding.tvDescribe.text = data.describe
                adapter = PlateAdapter(this@PlateActivity, data.normalItemsList).apply {
                    onItemClick = {
                        startActivity(Intent(this@PlateActivity, ArticleActivity::class.java).apply {
                            putExtra("url", it.link)
                        })
                    }
                }
                binding.btnExpand.isGone = data.topItemsList.size < 3
                binding.recyclerView.adapter = adapter
                topAdapter.apply {
                    submitList(data.topItemsList.take(2)) // 初始显示2项
                    fullList.addAll(data.topItemsList)     // 存储完整数据
                }
                isLoading = false
            }
        }
    }

    private fun loadMoreData() {
        lifecycleScope.launch {
            val data = fetchPlateContentData(nextPageUrl)
            currentPage ++
            hasMoreData = data.totalPage >= currentPage
            nextPageUrl = data.pageUrl + currentPage
            withContext(Dispatchers.Main) {
                binding.tvDescribe.text = data.describe
                adapter.addAll(data.normalItemsList)
                isLoading = false
            }
        }
    }

    private fun shouldLoadMore(): Boolean {
        val recyclerView = binding.recyclerView
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager

        // 获取 RecyclerView 总高度和当前可见的最后一个 Item 位置
        val totalItemCount = layoutManager.itemCount
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

        // 当剩余 Item 数量 <=5 时触发加载
        return (totalItemCount - lastVisiblePosition) <= 5 && !isLoading && hasMoreData
    }

    class ExpandableTopAdapter : ListAdapter<PlateContentItem, ExpandableTopAdapter.ViewHolder>(DiffCallback()) {
        var isExpanded = false
            private set
        val fullList = mutableListOf<PlateContentItem>()
        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.title)
        }
        fun setExpanded(expanded: Boolean) {
            isExpanded = expanded
            submitList(if (expanded) fullList else fullList.take(2))
        }
        class DiffCallback : DiffUtil.ItemCallback<PlateContentItem>() {
            override fun areItemsTheSame(old: PlateContentItem, new: PlateContentItem) = old == new
            override fun areContentsTheSame(old: PlateContentItem, new: PlateContentItem) = old == new
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_plate_top_article, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val title = getItem(position).title
            holder.title.text = title
        }
    }

    class PlateAdapter(private val context: Context, private val plateContentList: MutableList<PlateContentItem>):
        RecyclerView.Adapter<PlateAdapter.ViewHolder>() {

        // 点击监听接口
        var onItemClick: ((PlateContentItem) -> Unit)? = null

        fun addAll(newItems: MutableList<PlateContentItem>) {
            val startPosition = plateContentList.size
            plateContentList.addAll(newItems)
            notifyItemRangeInserted(startPosition, newItems.size)
        }

        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val itemLayout: View = view.findViewById(R.id.itemLayout)
            val coverImage: ImageView = view.findViewById(R.id.coverImage)
            val userLayout: ConstraintLayout = view.findViewById(R.id.userLayout)
            val avatar: ImageView = view.findViewById(R.id.userAvatar)
            val userAvatar: CardView = view.findViewById(R.id.userAvatarCard)
            val userName: TextView = view.findViewById(R.id.userNameText)
            val time: TextView = view.findViewById(R.id.time)
            val title: TextView = view.findViewById(R.id.title)
            val describe: TextView = view.findViewById(R.id.describe)
            val topic: TextView = view.findViewById(R.id.topic)
            val topicCard: CardView = view.findViewById(R.id.topicCard)
            val viewCount: TextView = view.findViewById(R.id.viewCount)
            val commentCount: TextView = view.findViewById(R.id.commentCount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_recommend_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val content = plateContentList[position]

            if (content.cover.isNotEmpty() && !content.cover.startsWith("http")) {
                holder.coverImage.isVisible = true
                Glide.with(holder.itemView.context)
                    .load(BASE_URL + content.cover)
                    .into(holder.coverImage)
                val userParams = holder.userLayout.layoutParams as ViewGroup.MarginLayoutParams
                userParams.topMargin = (12f.dpToPx(context)).roundToInt()
                holder.userLayout.layoutParams = userParams
            } else {
                holder.coverImage.isVisible = false
                val userParams = holder.userLayout.layoutParams as ViewGroup.MarginLayoutParams
                userParams.topMargin = 0
                holder.userLayout.layoutParams = userParams
            }

            val avatarUrl = idToAvatar(content.id)
            Glide.with(holder.itemView.context)
                .load(avatarUrl)
                .error(R.drawable.avatar_account)
                .into(holder.avatar)


            holder.userName.text = content.userName
            holder.time.text = content.time
            holder.title.text = content.title

            holder.describe.isVisible = false

            if (content.prefix != "") {
                holder.topicCard.isVisible = true
                holder.topic.text = content.prefix
            } else {
                holder.topicCard.isVisible = false
            }

            holder.viewCount.text = content.views
            holder.commentCount.text = content.comments

            holder.itemLayout.setOnClickListener {
                onItemClick?.invoke(content)
            }

        }

        override fun getItemCount(): Int = plateContentList.size

    }
}
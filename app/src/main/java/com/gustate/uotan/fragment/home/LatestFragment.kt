package com.gustate.uotan.fragment.home

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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.activity.ArticleActivity
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.idToAvatar
import com.gustate.uotan.utils.parse.home.ForumLatestItem
import com.gustate.uotan.utils.parse.home.LatestParse.Companion.fetchLatestData
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import kotlin.math.roundToInt

class LatestFragment : Fragment() {

    /** 定义全类可变变量 **/
    // 推荐列表框
    private lateinit var recyclerView: RecyclerView
    private lateinit var srlRoot: SmartRefreshLayout
    private lateinit var adapter: LatestAdapter
    // 是否正在加载
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_latest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** 从布局中取出需要调用的视图 **/
        // 初始化 RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        // 刷新布局
        srlRoot = view.findViewById(R.id.srlRoot)

        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            srlRoot.layout.setPadding(
                0,
                (systemBars.top + 100f.dpToPx(requireContext())).roundToInt(),
                0,
                (systemBars.bottom + 70f.dpToPx(requireContext())).roundToInt()
            )
            srlRoot.setHeaderInsetStartPx((systemBars.top + 100f.dpToPx(requireContext())).roundToInt())
            srlRoot.setFooterInsetStartPx((systemBars.bottom + 70f.dpToPx(requireContext())).roundToInt())
            insets
        }

        loadData()

        srlRoot.setOnRefreshListener {
            loadData()
        }

        srlRoot.setEnableLoadMore(false)
        srlRoot.setEnableAutoLoadMore(false)
        srlRoot.setEnableOverScrollDrag(true)

    }

    private fun loadData(refresh: Boolean = true) {
        // 如果正在加载或在最后一页不执行该方法
        if (isLoading) return
        if (refresh) {
            /** recyclerView 设置 **/
            // 为 recyclerView 设置布局管理器
            recyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = VERTICAL
            }
            // 设置 adapter 适配器
            adapter = LatestAdapter().apply {
                onItemClick = {
                    val intent = Intent(
                        requireContext(),
                        ArticleActivity::class.java
                    ).putExtra("url", it.url)
                    startActivity(intent)
                }
            }
            recyclerView.adapter = adapter
        }
        // 设置为正在加载
        isLoading = true
        // 启动协程
        lifecycleScope.launch {
            // 使用 try catch 方法健壮代码
            try {
                // 获取推荐数据
                val latestData = fetchLatestData()
                // 切换到 Main 线程执行
                withContext(Dispatchers.Main) {
                    adapter.addAll(latestData)
                }
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
            } finally {
                srlRoot.finishRefresh()
                isLoading = false
            }
        }
    }

    class LatestAdapter(): Adapter<LatestAdapter.ViewHolder>() {
        private val latestList = mutableListOf<ForumLatestItem>()
        // 点击监听接口
        var onItemClick: ((ForumLatestItem) -> Unit)? = null

        fun addAll(newItems: MutableList<ForumLatestItem>) {
            val startPosition = latestList.size
            latestList.addAll(newItems)
            notifyItemRangeInserted(startPosition, newItems.size)
        }

        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val itemLayout: View = view.findViewById(R.id.itemLayout)
            val coverImage: ImageView = view.findViewById(R.id.coverImage)
            val userLayout: ConstraintLayout = view.findViewById(R.id.userLayout)
            val userAvatar: ImageView = view.findViewById(R.id.userAvatar)
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
            val content = latestList[position]

            if (content.cover.startsWith("http")) {
                holder.coverImage.isVisible = true
                Glide.with(holder.itemView.context)
                    .load(content.cover)
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

            val avatarUrl = idToAvatar(content.userId)
            Glide.with(holder.itemView.context)
                .load(avatarUrl)
                .error(R.drawable.avatar_account)
                .into(holder.userAvatar)

            holder.userName.text = content.author
            holder.time.text = content.time
            holder.title.text = content.title

            holder.describe.isVisible = false

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

        override fun getItemCount(): Int = latestList.size

    }

}
package com.gustate.uotan.home

import android.os.Bundle
import androidx.fragment.app.Fragment
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
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.parse.home.FetchResult
import com.gustate.uotan.parse.home.ForumRecommendItem
import com.gustate.uotan.parse.home.RecommendParse
import com.gustate.uotan.utils.Utils
import kotlinx.coroutines.launch

private lateinit var recyclerView: RecyclerView

class RecommendFragment : Fragment() {

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
        val layoutManager = LinearLayoutManager(context)
        // 初始化 RecyclerView
        recyclerView = view.findViewById(R.id.recommendRecycler)
        recyclerView.layoutManager = layoutManager
        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            recyclerView.setPadding(0, systemBars.top + Utils.dp2Px(114, context!!).toInt(), 0, systemBars.bottom + Utils.dp2Px(70, context!!).toInt())
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
                    // 更新数据
                    fetchResult.items.let { newItems ->
                        if (adapter == null) {
                            recyclerView.adapter = RecommendAdapter(newItems.toMutableList())
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

    fun addAll(newItems: MutableList<ForumRecommendItem>) {
        val startPosition = recommendList.size
        recommendList.addAll(newItems)
        notifyItemRangeInserted(startPosition, newItems.size)
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val coverImage: ImageView = view.findViewById(R.id.coverImage)
        val userAvatar: ImageView = view.findViewById(R.id.userAvatar)
        val spanAvatar: CardView = view.findViewById(R.id.spanCard)
        val spanText: TextView = view.findViewById(R.id.avatarSpan)
        val userName: TextView = view.findViewById(R.id.userName)
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
            holder.userAvatar.isVisible = true
            holder.spanAvatar.isVisible = false
            Glide.with(holder.itemView.context)
                .load(content.authorAvatar)
                .into(holder.userAvatar)
        } else {
            holder.userAvatar.isVisible = false
            holder.spanAvatar.isVisible = true
            holder.spanText.text = content.authorAvatar
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
    }

    override fun getItemCount(): Int = recommendList.size

}
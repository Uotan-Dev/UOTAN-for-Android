package com.gustate.uotan.fragment.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.activity.ArticleActivity
import com.gustate.uotan.utils.Utils
import com.gustate.uotan.utils.parse.home.ForumLatestItem
import com.gustate.uotan.utils.parse.home.LatestParse
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.idToAvatar
import com.scwang.smart.refresh.layout.api.RefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class LatestFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var nullAdapter: LatestAdapter
    private lateinit var fetchResult: MutableList<ForumLatestItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_latest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val refreshLayout: RefreshLayout = view.findViewById(R.id.refreshLayout)
        // 初始化 RecyclerView
        recyclerView = view.findViewById(R.id.recommendRecycler)
        val linearLayout = LinearLayoutManager(requireContext())
        linearLayout.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayout
        nullAdapter = LatestAdapter(requireContext(), mutableListOf())
        recyclerView.adapter = nullAdapter
        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            refreshLayout.layout.setPadding(
                0,
                (systemBars.top + 100f.dpToPx(requireContext())).roundToInt(),
                0,
                (systemBars.bottom + 70f.dpToPx(requireContext())).roundToInt()
            )
            insets
        }
        // 启动协程
        lifecycleScope.launch {
            fetchResult = LatestParse.fetchLatestData()
            withContext(Dispatchers.Main) {
                // 创建新Adapter时设置点击监听
                val newAdapter = LatestAdapter(requireContext(), fetchResult.toMutableList()).apply {
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
            }
        }
    }

}

class LatestAdapter(private val context: Context, private val latestList: MutableList<ForumLatestItem>):
    RecyclerView.Adapter<LatestAdapter.ViewHolder>() {

    // 点击监听接口
    var onItemClick: ((ForumLatestItem) -> Unit)? = null

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
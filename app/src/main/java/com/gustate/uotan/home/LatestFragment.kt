package com.gustate.uotan.home

import android.content.Context
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
import com.gustate.uotan.parse.home.ForumLatestItem
import com.gustate.uotan.parse.home.LatestParse
import com.gustate.uotan.utils.Utils
import kotlinx.coroutines.launch

private lateinit var recyclerView: RecyclerView

class LatestFragment : Fragment() {

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
        val layoutManager = LinearLayoutManager(context)
        // 初始化 RecyclerView
        recyclerView = view.findViewById(R.id.recommendRecycler)
        recyclerView.layoutManager = layoutManager
        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            recyclerView.setPadding(0, systemBars.top + Utils.dp2Px(114, requireContext()).toInt(), 0, systemBars.bottom + Utils.dp2Px(70, requireContext()).toInt())
            insets
        }
        // 启动协程
        lifecycleScope.launch {
            fetchResult = LatestParse.fetchLatestData()
            recyclerView.adapter = context?.let { LatestAdapter(it, fetchResult) }
        }
    }

}

class LatestAdapter(private val context: Context, private val latestList: MutableList<ForumLatestItem>):
    RecyclerView.Adapter<LatestAdapter.ViewHolder>() {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val coverImage: ImageView = view.findViewById(R.id.coverImage)
        val userLayout: ConstraintLayout = view.findViewById(R.id.userLayout)
        val userAvatar: CardView = view.findViewById(R.id.userAvatarCard)
        val userName: TextView = view.findViewById(R.id.userName)
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
            userParams.topMargin = Utils.dp2Px(12, context).toInt()
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
        holder.userName.layoutParams = nameParams

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
    }

    override fun getItemCount(): Int = latestList.size

}
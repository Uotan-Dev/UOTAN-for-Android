package com.gustate.uotan.section.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.section.data.model.SectionDataItem
import com.gustate.uotan.utils.Helpers.Companion.avatarOptions
import com.gustate.uotan.utils.Utils
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import kotlin.math.roundToInt

class SectionArticleListAdapter : ListAdapter<SectionDataItem, SectionArticleListAdapter.ViewHolder>(
    DiffCallback()
) {

    var onItemClick: ((String) -> Unit)? = null

    class DiffCallback : DiffUtil.ItemCallback<SectionDataItem>() {
        override fun areItemsTheSame(old: SectionDataItem, new: SectionDataItem) = old == new
        override fun areContentsTheSame(old: SectionDataItem, new: SectionDataItem) = old == new
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
        val commentCount: TextView = view.findViewById(R.id.tv_comment_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_thread, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = getItem(position)
        if (content.cover.isNotEmpty() && !content.cover.startsWith("/img/forums/")) {
            holder.coverImage.isVisible = true
            Glide.with(holder.itemView.context)
                .load(Utils.Companion.baseUrl + content.cover)
                .into(holder.coverImage)
            val userParams = holder.userLayout.layoutParams as ViewGroup.MarginLayoutParams
            userParams.topMargin = (12f.dpToPx(holder.itemView.context)).roundToInt()
            holder.userLayout.layoutParams = userParams
        } else {
            holder.coverImage.isVisible = false
            val userParams = holder.userLayout.layoutParams as ViewGroup.MarginLayoutParams
            userParams.topMargin = 0
            holder.userLayout.layoutParams = userParams
        }
        val avatarUrl = Utils.Companion.idToAvatar(content.id)
        Glide.with(holder.itemView.context)
            .load(avatarUrl)
            .apply(avatarOptions)
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
            onItemClick?.invoke(content.link)
        }

    }
}
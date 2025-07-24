package com.gustate.uotan.threads.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.databinding.ItemPostBinding
import com.gustate.uotan.threads.data.model.Post
import com.gustate.uotan.utils.Helpers.Companion.avatarOptions
import com.gustate.uotan.utils.Utils.Companion.htmlToSpan
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PostsAdapter : ListAdapter<Post, PostsAdapter.ViewHolder>(DiffCallback()) {
    class DiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(old: Post, new: Post) = old.postID == new.postID
        override fun areContentsTheSame(old: Post, new: Post) = old.message == new.message
    }

    inner class ViewHolder(val binding: ItemPostBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val post = getItem(position)
        val binding = holder.binding
        // 头像
        Glide.with(holder.itemView.context)
            .load(post.user.avatarUrls.o)
            .apply(avatarOptions)
            .into(binding.imgAvatar)
        binding.tvUsername.text = post.username
        val instant = Instant.ofEpochSecond(post.postDate)
        val formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault())
        val formattedDate = formatter.format(instant)
        binding.tvTime.text = formattedDate
        binding.tvIp.text = post.user.location
        var messageHtml = post.messageParsed
        post.attachments?.forEach {
            messageHtml = messageHtml
                .replace(it.thumbnailURL, it.directURL)
        }
        htmlToSpan(binding.tvContent, messageHtml)
    }

    // 回复
    fun addNewReply(rv: RecyclerView, post: Post) {
        // 创建新列表
        val newList = currentList.toMutableList().apply {
            add(0, post)  // 添加到顶部
        }
        submitList(newList)
        rv.scrollToPosition(0)
    }
}
package com.gustate.uotan.threads.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.iielse.imageviewer.ImageViewerBuilder
import com.github.iielse.imageviewer.core.Photo
import com.github.iielse.imageviewer.core.SimpleDataProvider
import com.gustate.uotan.article.ContentAdapter
import com.gustate.uotan.article.ContentBlock
import com.gustate.uotan.article.HtmlParse
import com.gustate.uotan.article.imageviewer.ImageLoader
import com.gustate.uotan.article.imageviewer.ImageTransformer
import com.gustate.uotan.databinding.ItemPostBinding
import com.gustate.uotan.databinding.ItemPostReplyBinding
import com.gustate.uotan.threads.data.model.Post
import com.gustate.uotan.threads.data.model.ThreadPhoto
import com.gustate.uotan.threads.data.model.post.ReplyPost
import com.gustate.uotan.utils.Helpers.avatarOptions
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.collections.mutableListOf

class PostsAdapter : ListAdapter<Post, PostsAdapter.ViewHolder>(DiffCallback()) {

    private val repliesMap = mutableMapOf<Int, List<ReplyPost>>()

    class DiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(old: Post, new: Post) = old.postID == new.postID
        override fun areContentsTheSame(old: Post, new: Post) = old.message == new.message
    }

    inner class ViewHolder(val binding: ItemPostBinding):
        RecyclerView.ViewHolder(binding.root) {
            val replyAdapter = ReplyAdapter()
            init {
                binding.rvReply.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = replyAdapter
                }
            }
        }

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
        val context = holder.itemView.context
        val contentAdapter = ContentAdapter(isReply = true)
        // 头像
        Glide.with(holder.itemView.context)
            .load(post.user.avatar_urls.l)
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
        val replies = repliesMap[post.postID.toInt()] ?: emptyList()
        holder.replyAdapter.submitList(replies)
        binding.navReply.isGone = replies.isEmpty()
        binding.rvReply.isGone = replies.isEmpty()
        val contentBlocks = HtmlParse.parse(post.messageParsed)
        contentAdapter.updateContent(contentBlocks)
        val pictureList = mutableListOf<Photo>()
        contentBlocks.forEachIndexed { index, src ->
            if (src is ContentBlock.ImageBlock) {
                pictureList.add(ThreadPhoto(src.src, index.toLong()))
            }
        }
        contentAdapter.onImageClick = { id, url ->
            showPictureViewer(context, url, id.toLong(), pictureList)
        }
    }

    /**
     * 启动图片查看器
     * @param position 图片索引 用于绑定动画
     * @param url 图片链接
     */
    private fun showPictureViewer(
        context: Context,
        url: String,
        position: Long,
        pictureList: MutableList<Photo>
    ) {
        val clickedData: Photo = ThreadPhoto(url, position)
        val builder = ImageViewerBuilder(
            context = context,
            dataProvider = SimpleDataProvider(clickedData, pictureList),        // 一次性全量加载
            imageLoader = ImageLoader(),                                        // 实现对数据源的加载
            transformer = ImageTransformer(),                                   // 设置过渡动画的配对
        )
        builder.show()
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

    fun setRepliesForPost(postId: Int, replies: List<ReplyPost>?) {
        replies?.let { it ->
            repliesMap[postId] = it
            // 查找该帖子在列表中的位置并刷新
            val position = currentList.indexOfFirst {
                it.postID.toInt() == postId
            }
            if (position >= 0) {
                notifyItemChanged(position)
            }
        }
    }

    class ReplyAdapter : ListAdapter<ReplyPost, ReplyAdapter.ViewHolder>(DiffCallback()) {
        class DiffCallback : DiffUtil.ItemCallback<ReplyPost>() {
            override fun areItemsTheSame(old: ReplyPost, new: ReplyPost) =
                old.replyPostId == new.replyPostId
            override fun areContentsTheSame(old: ReplyPost, new: ReplyPost) =
                old.messageParsed == new.messageParsed
        }
        inner class ViewHolder(val binding: ItemPostReplyBinding):
            RecyclerView.ViewHolder(binding.root) {
            val contentAdapter = ContentAdapter(isReply = true)
                init {
                    binding.rvContent.apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = contentAdapter
                    }
                }
            }
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            val binding = ItemPostReplyBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }
        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int
        ) {
            val reply = getItem(position)
            val binding = holder.binding
            val context = holder.itemView.context
            // 头像
            binding.tvAuthor.text = reply.username
            val contentBlock = HtmlParse.parse(reply.messageParsed)
            holder.contentAdapter.updateContent(contentBlock)
            val pictureList = mutableListOf<Photo>()
            contentBlock.forEachIndexed { index, src ->
                if (src is ContentBlock.ImageBlock) {
                    pictureList.add(ThreadPhoto(src.src, index.toLong()))
                }
            }
            holder.contentAdapter.onImageClick = { id, url ->
                showPictureViewer(context, url, id.toLong(), pictureList)
            }
        }

        /**
         * 启动图片查看器
         * @param position 图片索引 用于绑定动画
         * @param url 图片链接
         */
        private fun showPictureViewer(
            context: Context,
            url: String,
            position: Long,
            pictureList: MutableList<Photo>
        ) {
            val clickedData: Photo = ThreadPhoto(url, position)
            val builder = ImageViewerBuilder(
                context = context,
                dataProvider = SimpleDataProvider(clickedData, pictureList),        // 一次性全量加载
                imageLoader = ImageLoader(),                                        // 实现对数据源的加载
                transformer = ImageTransformer(),                                   // 设置过渡动画的配对
            )
            builder.show()
        }
    }
}
package com.gustate.uotan.resource.ui.details.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
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
import com.gustate.uotan.databinding.RecyclerResourceCommentItemBinding
import com.gustate.uotan.resource.data.model.ResReplyData
import com.gustate.uotan.resource.ui.details.adapter.ResourceReplyAdapter.ViewHolder
import com.gustate.uotan.threads.data.model.ThreadPhoto
import com.gustate.uotan.utils.Helpers.avatarOptions
import com.gustate.uotan.utils.Utils

class ResourceReplyAdapter : ListAdapter<ResReplyData, ViewHolder>(DiffCallback()) {

    class DiffCallback : DiffUtil.ItemCallback<ResReplyData>() {
        override fun areItemsTheSame(
            oldItem: ResReplyData,
            newItem: ResReplyData
        ): Boolean {
            return oldItem.time == newItem.time && oldItem.rating == newItem.rating
        }

        override fun areContentsTheSame(
            oldItem: ResReplyData,
            newItem: ResReplyData
        ): Boolean {
            return oldItem.content == newItem.content
        }

    }

    inner class ViewHolder(val binding: RecyclerResourceCommentItemBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = RecyclerResourceCommentItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val binding = holder.binding
        val reply = getItem(position)
        val context = holder.itemView.context
        val contentAdapter = ContentAdapter(isReply = true)
        Glide.with(context)
            .load(Utils.idToAvatar(reply.authorId))
            .apply(avatarOptions)
            .into(binding.imgAvatar)
        binding.tvUsername.text = reply.author
        binding.tvTime.text = reply.time
        binding.rtRes.rating = reply.rating
        binding.rvContent.adapter = contentAdapter
        binding.rvContent.layoutManager = LinearLayoutManager(context)
        val contentBlocks = HtmlParse.parse(reply.content)
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
}
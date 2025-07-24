package com.gustate.uotan.message.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.message.data.model.AllMessage
import com.gustate.uotan.utils.Helpers.Companion.avatarOptions
import com.gustate.uotan.utils.Utils.Companion.idToAvatar

class MessageRecyclerAdapter : ListAdapter<AllMessage, MessageRecyclerAdapter.ViewHolder>(DiffCallback()) {

    var onItemClick: ((String) -> Unit)? = null

    class DiffCallback : DiffUtil.ItemCallback<AllMessage>() {
        override fun areItemsTheSame(old: AllMessage, new: AllMessage) = old == new
        override fun areContentsTheSame(old: AllMessage, new: AllMessage) = old == new
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val item: View = view.findViewById(R.id.layout_item)
        val avatar: ImageView = view.findViewById(R.id.img_avatar)
        val name: TextView = view.findViewById(R.id.tv_name)
        val time: TextView = view.findViewById(R.id.tv_time)
        val title: TextView = view.findViewById(R.id.tv_title)
        val describe: TextView = view.findViewById(R.id.tv_describe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_message_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = getItem(position)
        val context = holder.itemView.context
        if (content.isLikeNotice) {
            if (content.userId.isNotEmpty()) {
                Glide.with(context)
                    .load(idToAvatar(content.userId))
                    .apply(avatarOptions)
                    .into(holder.avatar)
            }
            holder.name.text = content.userName
            holder.time.text = content.time
            holder.title.text =
                ContextCompat.getString(holder.itemView.context, R.string.liked_the_article)
            holder.describe.text = content.likeContent
        }
        if (content.isSystemNotice) {
            Glide.with(context)
                .load(R.mipmap.ic_launcher)
                .apply(avatarOptions)
                .into(holder.avatar)
            holder.name.text = ContextCompat.getString(holder.itemView.context, R.string.app_name)
            holder.time.text = content.time
            holder.title.text =
                ContextCompat.getString(holder.itemView.context, R.string.system_message)
            holder.describe.text = content.systemContent
        }
        if (content.isUpdateNotice) {
            if (content.userId.isNotEmpty()) {
                Glide.with(context)
                    .load(idToAvatar(content.userId))
                    .apply(avatarOptions)
                    .into(holder.avatar)
            }
            holder.name.text =
                ContextCompat.getString(holder.itemView.context, R.string.resource_update)
            holder.time.text = content.time
            holder.title.text =
                ContextCompat.getString(holder.itemView.context, R.string.resource_update)
            holder.describe.text = content.updateContent
            holder.item.setOnClickListener {
                onItemClick?.invoke(content.updateUrl)
            }
        }
        if (content.isCommentNotice) {
            if (content.userId.isNotEmpty()) {
                Glide.with(context)
                    .load(idToAvatar(content.userId))
                    .apply(avatarOptions)
                    .into(holder.avatar)
            }
            holder.name.text = content.userName
            holder.time.text = content.time
            holder.title.text =
                ContextCompat.getString(holder.itemView.context, R.string.replied_to_the_article)
            holder.describe.text = content.commentContent
            holder.item.setOnClickListener {
                onItemClick?.invoke(content.commentUrl)
            }
        }
        if (content.isIntegralNotice) {
            Glide.with(context)
                .load(R.mipmap.ic_launcher)
                .apply(avatarOptions)
                .into(holder.avatar)
            holder.name.text =
                ContextCompat.getString(holder.itemView.context, R.string.system_message)
            holder.time.text = content.time
            holder.title.text =
                ContextCompat.getString(holder.itemView.context, R.string.points_change)
            when (content.integralType) {
                "dailyAttendance" -> {
                    holder.describe.text = context.getString(
                        R.string.add_balance,
                        context.getString(R.string.daily_attendance),
                        content.integralCount
                    )
                }
                "publishPost" -> {
                    holder.describe.text = context.getString(
                        R.string.add_balance,
                        context.getString(R.string.publish_new_post),
                        content.integralCount
                    )
                }
                "publishComment" -> {
                    holder.describe.text = context.getString(
                        R.string.add_balance,
                        context.getString(R.string.post_new_comment),
                        content.integralCount
                    )
                }
                "register" -> {
                    holder.describe.text = context.getString(
                        R.string.add_balance,
                        context.getString(R.string.register_improve_points),
                        content.integralCount
                    )
                }
                "sellResource" -> {
                    holder.describe.text = context.getString(
                        R.string.add_balance,
                        context.getString(R.string.selling_resources),
                        content.integralCount
                    )
                }
                "adjustUCoin" -> {
                    holder.describe.text = context.getString(
                        R.string.add_balance,
                        context.getString(R.string.adjust_u_coin),
                        content.integralCount
                    )
                }
                "buyDriveLink" -> {
                    holder.describe.text = context.getString(
                        R.string.reduce_balance,
                        context.getString(R.string.purchase_online_storage_link),
                        content.integralCount
                    )
                }
                "buyResource" -> {
                    holder.describe.text = context.getString(
                        R.string.reduce_balance,
                        context.getString(R.string.purchased_resources),
                        content.integralCount
                    )
                }
                "delComment" -> {
                    holder.describe.text = context.getString(
                        R.string.reduce_balance,
                        context.getString(R.string.delete_comment),
                        content.integralCount
                    )
                }
                "delPost" -> {
                    holder.describe.text = context.getString(
                        R.string.reduce_balance,
                        context.getString(R.string.delete_article),
                        content.integralCount
                    )
                }
            }
        }
    }

    fun updateList(newData: List<AllMessage>) {
        submitList(newData.toMutableList())
    }
}
package com.gustate.uotan.message.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.message.data.model.PersonalLetter
import com.gustate.uotan.utils.Helpers.Companion.avatarOptions
import com.gustate.uotan.utils.Utils.Companion.idToAvatar

class PersonalLetterRecyclerAdapter :
    ListAdapter<PersonalLetter, PersonalLetterRecyclerAdapter.ViewHolder>(DiffCallback()) {

    var onItemClick: ((String) -> Unit)? = null

    class DiffCallback : DiffUtil.ItemCallback<PersonalLetter>() {
        override fun areItemsTheSame(old: PersonalLetter, new: PersonalLetter) = old == new
        override fun areContentsTheSame(old: PersonalLetter, new: PersonalLetter) = old == new
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val item: View = view.findViewById(R.id.layout_item)
        val avatar: ImageView = view.findViewById(R.id.img_avatar)
        val name: TextView = view.findViewById(R.id.tv_name)
        val time: TextView = view.findViewById(R.id.tv_time)
        val describe: TextView = view.findViewById(R.id.tv_describe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_personal_letter_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = getItem(position)
        val context = holder.itemView.context
        Glide.with(context)
            .load(idToAvatar(content.id))
            .apply(avatarOptions)
            .into(holder.avatar)
        holder.name.text = content.author
        holder.describe.text = content.title
        holder.time.text = content.time
    }

    fun updateList(newData: List<PersonalLetter>) {
        submitList(newData.toMutableList())
    }
}
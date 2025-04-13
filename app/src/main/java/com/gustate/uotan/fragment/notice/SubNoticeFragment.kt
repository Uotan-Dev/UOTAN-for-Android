package com.gustate.uotan.fragment.notice

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getString
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.anim.TitleAnim
import com.gustate.uotan.utils.Utils
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.parse.notice.NoticeItem
import com.gustate.uotan.utils.parse.notice.NoticeParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class SubNoticeFragment : Fragment() {

    private lateinit var noticeAdapter: NoticeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sub_notice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val linearLayout = LinearLayoutManager(requireContext())
        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            recyclerView.setPadding(
                0,0,0,
                (systemBars.bottom + 70f.dpToPx(requireContext())).roundToInt()
            )
            insets
        }
        linearLayout.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayout
        recyclerView.adapter = NoticeAdapter(mutableListOf())
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val noticeData = NoticeParse.fetchNoticeData()
                withContext(Dispatchers.Main) {
                    val noticeAdapter = NoticeAdapter(noticeData)
                    recyclerView.adapter = noticeAdapter
                }
            }
        }
    }
}

class NoticeAdapter(private val noticeList: MutableList<NoticeItem>):
    RecyclerView.Adapter<NoticeAdapter.ViewHolder>() {

    // 点击监听接口
    var onItemClick: ((NoticeItem) -> Unit)? = null

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val userAvatar: ImageView = view.findViewById(R.id.userAvatar)
        val userName: TextView = view.findViewById(R.id.userNameText)
        val time: TextView = view.findViewById(R.id.time)
        val title: TextView = view.findViewById(R.id.title)
        val describe: TextView = view.findViewById(R.id.describe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_notice_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = noticeList[position]
        if (content.isLikeNotice) {
            if (content.avatarUrl.isNotEmpty()) {
                Glide
                    .with(holder.itemView.context)
                    .load(BASE_URL + content.avatarUrl)
                    .into(holder.userAvatar)
            }
            holder.userName.text = content.userName
            holder.time.text = content.time
            holder.title.text = getString(holder.itemView.context, R.string.liked_the_article)
            holder.describe.text = content.likeContent
        }
        if (content.isSystemNotice) {
            holder.userAvatar.setImageResource(R.mipmap.ic_launcher)
            holder.userName.text = getString(holder.itemView.context, R.string.app_name)
            holder.time.text = content.time
            holder.title.text = getString(holder.itemView.context, R.string.system_message)
            holder.describe.text = content.systemContent
        }
        if (content.isUpdateNotice) {
            if (content.avatarUrl.isNotEmpty()) {
                Glide
                    .with(holder.itemView.context)
                    .load(BASE_URL + content.avatarUrl)
                    .into(holder.userAvatar)
            }
            holder.userName.text = getString(holder.itemView.context, R.string.resource_update)
            holder.time.text = content.time
            holder.title.text = getString(holder.itemView.context, R.string.resource_update)
            holder.describe.text = content.updateContent
        }
        if (content.isCommentNotice) {
            if (content.avatarUrl.isNotEmpty()) {
                Glide
                    .with(holder.itemView.context)
                    .load(BASE_URL + content.avatarUrl)
                    .into(holder.userAvatar)
            }
            holder.userName.text = content.userName
            holder.time.text = content.time
            holder.title.text = getString(holder.itemView.context, R.string.replied_to_the_article)
            holder.describe.text = content.commentContent
        }
        if (content.isIntegralNotice) {
            holder.userAvatar.setImageResource(R.mipmap.ic_launcher)
            holder.userName.text = getString(holder.itemView.context, R.string.system_message)
            holder.time.text = content.time
            holder.title.text = getString(holder.itemView.context, R.string.points_change)
            when (content.integralType) {
                "dailyAttendance" -> holder.describe.text = getString(holder.itemView.context, R.string.attendance_improve_points) + content.integralCount
                "post" -> holder.describe.text = getString(holder.itemView.context, R.string.post_improve_points) + content.integralCount
                "buy" -> holder.describe.text = getString(holder.itemView.context, R.string.buy_resource_lower_points) + content.integralCount
                "sell"  -> holder.describe.text = getString(holder.itemView.context, R.string.sell_resource_improve_points) + content.integralCount
                "register" -> holder.describe.text = getString(holder.itemView.context, R.string.register_improve_points) + content.integralCount
                "del" -> holder.describe.text = getString(holder.itemView.context, R.string.del_comment_lower_points) + content.integralCount
            }
        }
    }

    override fun getItemCount(): Int = noticeList.size

}
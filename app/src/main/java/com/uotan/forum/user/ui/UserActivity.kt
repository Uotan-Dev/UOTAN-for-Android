package com.uotan.forum.user.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.uotan.forum.BaseActivity
import com.uotan.forum.R
import com.uotan.forum.ui.anim.TitleAnim
import com.uotan.forum.databinding.ActivityUserBinding
import com.uotan.forum.ui.dialog.LoadingDialog
import com.uotan.forum.threads.ui.ThreadsActivity
import com.uotan.forum.utils.Helpers.avatarOptions
import com.uotan.forum.utils.Utils
import com.uotan.forum.utils.Utils.baseUrl
import com.uotan.forum.utils.Utils.dpToPx
import com.uotan.forum.utils.Utils.idToAvatar
import com.uotan.forum.utils.Utils.openUrlInBrowser
import com.uotan.forum.utils.parse.search.SearchParse
import com.uotan.forum.utils.parse.search.SearchResult
import com.uotan.forum.utils.parse.user.UserParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class UserActivity : BaseActivity() {
    class SearchAdapter(): RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
        private val searchList = mutableListOf<SearchResult>()
        // 点击监听接口
        var onItemClick: ((SearchResult) -> Unit)? = null

        fun addAll(newItems: MutableList<SearchResult>) {
            val startPosition = searchList.size
            searchList.addAll(newItems)
            notifyItemRangeInserted(startPosition, newItems.size)
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
            val viewCountIco: View = view.findViewById(R.id.ico_view_count)
            val commentCount: TextView = view.findViewById(R.id.tv_comment_count)
            val commentCountIco: View = view.findViewById(R.id.ico_comment_count)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_thread, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val content = searchList[position]
            if (content.cover.isNotEmpty() && !content.cover.startsWith("/img")) {
                holder.coverImage.isVisible = true
                Glide.with(holder.itemView.context)
                    .load(baseUrl + content.cover)
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
            Glide.with(holder.itemView.context)
                .load(idToAvatar(content.id))
                .apply(avatarOptions)
                .into(holder.avatar)
            holder.userName.text = content.author
            holder.time.text = content.time
            holder.title.text = content.title
            holder.describe.text = content.content
            if (content.topic != "") {
                holder.topicCard.isVisible = true
                holder.topic.text = content.topic
            } else {
                holder.topicCard.isVisible = false
            }
            holder.viewCount.isGone = true
            holder.viewCountIco.isGone = true
            holder.commentCount.isGone = true
            holder.commentCountIco.isGone = true
            holder.itemLayout.setOnClickListener {
                onItemClick?.invoke(content)
            }
        }

        override fun getItemCount(): Int = searchList.size
    }
    private lateinit var binding: ActivityUserBinding
    private lateinit var loading: LoadingDialog
    private lateinit var adapter: SearchAdapter
    private var currentPage = 1
    private var totalPage = 1
    private var isLastPage = false
    private var isFollow = false
    private var isProfileLoading = false
    private var isPostLoading = false
    private var isFollowing = false
    private var nextPageUrl = ""

    private var colorOnBackgroundPrimary = 0
    private var colorOnBackgroundSecondary = 0
    private var colorOnOutlineButton = 0
    private var colorOnFilledButton = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        loading = LoadingDialog(this)
        setContentView(binding.root)
        colorOnBackgroundPrimary = Utils.getThemeColor(this, R.attr.colorOnBackgroundPrimary)
        colorOnBackgroundSecondary = Utils.getThemeColor(this, R.attr.colorOnBackgroundSecondary)
        colorOnOutlineButton =
            Utils.getThemeColor(this, R.attr.colorBottomNavigationBarOnBackgroundSecondary)
        colorOnFilledButton = Utils.getThemeColor(this, R.attr.colorOnFilledButton)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.headerBarLayout
                .updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = systemBars.top
                }
            binding.back
                .updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = systemBars.top
                }
            binding.cardView
                .updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = (systemBars.top + 60f.dpToPx(this@UserActivity))
                        .roundToInt()
                }
            TitleAnim(
                binding.headerBar,
                binding.avatarImage,
                (systemBars.top + 60f.dpToPx(this@UserActivity)),
                systemBars.top.toFloat(),
                1
            )
            insets
        }
        adapter = SearchAdapter().apply {
            onItemClick = { selectedItem ->
                startActivity(
                    Intent(
                        this@UserActivity, ThreadsActivity::class.java
                    ).apply {
                        putExtra("url", selectedItem.url)
                    }
                )
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        val url = intent.getStringExtra("url") ?: ""
        if (url.isEmpty()) {
            Utils.errorDialog(
                this,
                getString(R.string.intent_error),
                getString(R.string.intent_error_dialog)
            )
        }
        loadProfileData(url)
        binding.srlContent.setOnRefreshListener {
            loadProfileData(url)
        }
        binding.srlContent.setOnLoadMoreListener {
            loadUserPost("", "", baseUrl + nextPageUrl)
        }
        binding.sclRes.setOnClickListener {
            openUrlInBrowser(this, "$baseUrl/resources/")
        }
        binding.tabFollow.setOnClickListener {
            if (isFollowing) {
                Toast.makeText(
                    this,
                    "正在操作中，请稍后",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            isFollowing = true
            lifecycleScope.launch {
                val follow = UserParse.Companion.follow(url)
                if (follow) {
                    if (isFollow) {
                        isFollow = false
                        binding.tabFollow.background = AppCompatResources.getDrawable(
                            this@UserActivity,
                            R.drawable.uotan_btn_filled
                        )
                        binding.tabFollow.text = getString(R.string.follow)
                        binding.tabFollow.setTextColor(colorOnFilledButton)
                        binding.headerFollow.background = AppCompatResources.getDrawable(
                            this@UserActivity,
                            R.drawable.uotan_btn_filled
                        )
                        binding.headerFollow.text = getString(R.string.follow)
                        binding.headerFollow.setTextColor(colorOnFilledButton)
                    } else {
                        isFollow = true
                        binding.tabFollow.background = AppCompatResources.getDrawable(
                            this@UserActivity,
                            R.drawable.gustatex_button_outline
                        )
                        binding.tabFollow.text = getString(R.string.following)
                        binding.tabFollow.setTextColor(colorOnOutlineButton)
                        binding.headerFollow.background = AppCompatResources.getDrawable(
                            this@UserActivity,
                            R.drawable.gustatex_button_outline
                        )
                        binding.headerFollow.text = getString(R.string.following)
                        binding.headerFollow.setTextColor(colorOnOutlineButton)
                    }
                } else {
                    Toast.makeText(
                        this@UserActivity,
                        "操作失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                isFollowing = false
            }
        }
        binding.headerBack.setOnClickListener{
            finish()
        }
        binding.back.setOnClickListener{
            finish()
        }
    }

    private fun loadProfileData(url: String) {
        if (isProfileLoading) {
            isProfileLoading = false
            loading.cancel()
            return
        }
        isProfileLoading = true
        loading.show()
        currentPage = 1
        totalPage = 1
        lifecycleScope.launch {
            val profileData = UserParse.Companion.fetchUserData(url)
            withContext(Dispatchers.Main) {
                if (profileData.cover.isNotEmpty()) {
                    Glide.with(this@UserActivity)
                        .load(baseUrl + profileData.cover)
                        .error(Color.TRANSPARENT.toDrawable())
                        .into(binding.userCover)
                    binding.frameLayout.isGone = false
                    binding.userCover.isGone = false
                    binding.back.imageTintList = ColorStateList.valueOf(0xFFFFFFFF.toInt())
                    binding.userName.setTextColor(0xFFFFFFFF.toInt())
                    binding.signatureText.setTextColor(0x99EBEBF5.toInt())
                    binding.authText.setTextColor(0xFFFFFFFF.toInt())
                    binding.postCount.setTextColor(0xFFFFFFFF.toInt())
                    binding.postCountText.setTextColor(0x99EBEBF5.toInt())
                    binding.resCount.setTextColor(0xFFFFFFFF.toInt())
                    binding.resCountText.setTextColor(0x99EBEBF5.toInt())
                } else {
                    binding.frameLayout.isGone = true
                    binding.userCover.isGone = true
                    binding.back.imageTintList = ColorStateList.valueOf(colorOnBackgroundPrimary)
                    binding.userName.setTextColor(colorOnBackgroundPrimary)
                    binding.signatureText.setTextColor(colorOnBackgroundSecondary)
                    binding.authText.setTextColor(colorOnBackgroundPrimary)
                    binding.postCount.setTextColor(colorOnBackgroundPrimary)
                    binding.postCountText.setTextColor(colorOnBackgroundSecondary)
                    binding.resCount.setTextColor(colorOnBackgroundPrimary)
                    binding.resCountText.setTextColor(colorOnBackgroundSecondary)
                }
                Glide.with(this@UserActivity)
                    .load(baseUrl + profileData.avatar)
                    .apply(RequestOptions().circleCrop())
                    .error(R.drawable.avatar_account)
                    .into(binding.headerAvatar)
                binding.headerName.text = profileData.name
                Glide.with(this@UserActivity)
                    .load(baseUrl + profileData.avatar)
                    .apply(RequestOptions().circleCrop())
                    .error(R.drawable.avatar_account)
                    .into(binding.avatarImage)
                binding.userName.text = profileData.name
                if (profileData.signature.isNotEmpty()) {
                    binding.signatureText.isGone = false
                    binding.signatureText.text = profileData.signature
                } else {
                    binding.signatureText.isGone = true
                }
                if (profileData.auth.isNotEmpty()) {
                    binding.authIcon.isGone = false
                    binding.authText.isGone = false
                    binding.authText.text = profileData.auth
                } else {
                    binding.authIcon.isGone = true
                    binding.authText.isGone = true
                }
                binding.postCount.text = profileData.postCount
                binding.resCount.text = profileData.resCount
                isFollow = profileData.isFollow
                if (isFollow) {
                    binding.tabFollow.background = AppCompatResources.getDrawable(
                        this@UserActivity,
                        R.drawable.gustatex_button_outline
                    )
                    binding.tabFollow.text = getString(R.string.following)
                    binding.tabFollow.setTextColor(colorOnOutlineButton)
                    binding.headerFollow.background = AppCompatResources.getDrawable(
                        this@UserActivity,
                        R.drawable.gustatex_button_outline
                    )
                    binding.headerFollow.text = getString(R.string.following)
                    binding.headerFollow.setTextColor(colorOnOutlineButton)
                } else {
                    binding.tabFollow.background = AppCompatResources.getDrawable(
                        this@UserActivity,
                        R.drawable.uotan_btn_filled
                    )
                    binding.tabFollow.text = getString(R.string.follow)
                    binding.tabFollow.setTextColor(colorOnFilledButton)
                    binding.headerFollow.background = AppCompatResources.getDrawable(
                        this@UserActivity,
                        R.drawable.uotan_btn_filled
                    )
                    binding.headerFollow.text = getString(R.string.follow)
                    binding.headerFollow.setTextColor(colorOnFilledButton)
                }
                isProfileLoading = false
                loadUserPost("member?user_id=${profileData.id}", "1", "")
                binding.srlContent.finishRefresh()
                loading.cancel()
            }
        }
    }
    private fun loadUserPost(content: String, page: String, url: String) {
        if (isPostLoading || isLastPage) return
        isPostLoading = true
        lifecycleScope.launch {
            try {
                val userPostData = if (page == "1") SearchParse.Companion.searchInfoParse(
                    content,
                    "1",
                    true
                )
                else SearchParse.Companion.searchInfoParse(url)
                withContext(Dispatchers.Main) {
                    userPostData.items.let {
                        adapter.addAll(it)
                    }
                    currentPage += 1
                    nextPageUrl = userPostData.nextPageUrl
                    totalPage = userPostData.totalPage
                    isLastPage = currentPage > totalPage
                }
            } finally {
                withContext(Dispatchers.Main) {
                    if (isLastPage) {
                        binding.srlContent.finishLoadMoreWithNoMoreData()
                        binding.srlContent.setNoMoreData(true)
                    } else {
                        binding.srlContent.finishLoadMore()
                    }
                    isPostLoading = false
                }
            }
        }
    }
}
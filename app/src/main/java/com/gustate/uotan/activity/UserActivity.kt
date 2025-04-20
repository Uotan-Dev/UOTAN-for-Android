package com.gustate.uotan.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.SearchResultActivity.SearchAdapter
import com.gustate.uotan.anim.TitleAnim
import com.gustate.uotan.databinding.ActivityUserBinding
import com.gustate.uotan.gustatex.dialog.LoadingDialog
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.parse.search.SearchParse.Companion.searchInfoParse
import com.gustate.uotan.utils.parse.user.UserParse.Companion.fetchUserData
import com.gustate.uotan.utils.parse.user.UserParse.Companion.follow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class UserActivity : BaseActivity() {
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
    private var xfToken = ""

    private var colorOnBackgroundPrimary = 0
    private var colorOnBackgroundSecondary = 0
    private var colorOnOutlineButton = 0
    private var colorOnFilledButton = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        loading = LoadingDialog(this)
        setContentView(binding.root)
        withStyledAttributes(
            attrs = intArrayOf(
                R.attr.colorOnBackgroundPrimary,
                R.attr.colorOnBackgroundSecondary,
                R.attr.colorBottomNavigationBarOnBackgroundSecondary,
                R.attr.colorOnFilledButton
            )
        ) {
            val primaryIndex = getIndex(0)
            val secondaryIndex = getIndex(1)
            val outlineIndex = getIndex(2)
            val filledIndex = getIndex(3)
            colorOnBackgroundPrimary = getColor(primaryIndex, Color.RED)
            colorOnBackgroundSecondary = getColor(secondaryIndex, Color.RED)
            colorOnOutlineButton = getColor(outlineIndex, Color.RED)
            colorOnFilledButton = getColor(filledIndex, Color.RED)
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusBarView
                .updateLayoutParams<ViewGroup.LayoutParams> {
                    height = systemBars.top
                }
            binding.statusBarView2
                .updateLayoutParams<ViewGroup.LayoutParams> {
                    height = systemBars.top
                }
            binding.toolbar
                .updateLayoutParams<ViewGroup.LayoutParams> {
                    height = (systemBars.top + 60f.dpToPx(this@UserActivity)).roundToInt()
                }
            binding.avatar
                .updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = (systemBars.top + 60f.dpToPx(this@UserActivity)).roundToInt()
                }
            TitleAnim(
                binding.headerBar,
                binding.avatar,
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
                        this@UserActivity, ArticleActivity::class.java
                    ).apply {
                        putExtra("url", selectedItem.url)
                    }
                )
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        val url = intent.getStringExtra("url")!!
        loadProfileData(url)
        binding.smartRefreshLayout.setOnRefreshListener {
            loadProfileData(url)
        }
        binding.smartRefreshLayout.setOnLoadMoreListener {
            loadUserPost("", "", BASE_URL + nextPageUrl)
        }
        binding.follow.setOnClickListener {
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
                val follow = follow(
                    url,
                    Cookies.map { "${it.key}=${it.value}" }.joinToString("; "),
                    xfToken
                )
                if (follow) {
                    if (isFollow) {
                        isFollow = false
                        binding.follow.background = AppCompatResources.getDrawable(
                            this@UserActivity,
                            R.drawable.gustatex_button_filled
                        )
                        binding.follow.text = getString(R.string.follow)
                        binding.follow.setTextColor(colorOnFilledButton)
                    } else {
                        isFollow = true
                        binding.follow.background = AppCompatResources.getDrawable(
                            this@UserActivity,
                            R.drawable.gustatex_button_outline
                        )
                        binding.follow.text = getString(R.string.following)
                        binding.follow.setTextColor(colorOnOutlineButton)
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
            val profileData = fetchUserData(url)
            withContext(Dispatchers.Main) {
                if (profileData.cover.isNotEmpty()) {
                    Glide.with(this@UserActivity)
                        .load(BASE_URL + profileData.cover)
                        .error(Color.TRANSPARENT.toDrawable())
                        .into(binding.userCover)
                    binding.frameLayout.isGone = false
                    binding.userCover.isGone = false
                    binding.back2.imageTintList = ColorStateList.valueOf(0xFFFFFFFF.toInt())
                    binding.name.setTextColor(0xFFFFFFFF.toInt())
                    binding.signatureText.setTextColor(0x99EBEBF5.toInt())
                    binding.authText.setTextColor(0xFFFFFFFF.toInt())
                    binding.postCount.setTextColor(0xFFFFFFFF.toInt())
                    binding.postCountText.setTextColor(0x99EBEBF5.toInt())
                    binding.resCount.setTextColor(0xFFFFFFFF.toInt())
                    binding.resCountText.setTextColor(0x99EBEBF5.toInt())
                } else {
                    binding.frameLayout.isGone = true
                    binding.userCover.isGone = true
                    binding.back2.imageTintList = ColorStateList.valueOf(colorOnBackgroundPrimary)
                    binding.name.setTextColor(colorOnBackgroundPrimary)
                    binding.signatureText.setTextColor(colorOnBackgroundSecondary)
                    binding.authText.setTextColor(colorOnBackgroundPrimary)
                    binding.postCount.setTextColor(colorOnBackgroundPrimary)
                    binding.postCountText.setTextColor(colorOnBackgroundSecondary)
                    binding.resCount.setTextColor(colorOnBackgroundPrimary)
                    binding.resCountText.setTextColor(colorOnBackgroundSecondary)
                }
                Glide.with(this@UserActivity)
                    .load(BASE_URL + profileData.avatar)
                    .apply(RequestOptions().circleCrop())
                    .error(R.drawable.avatar_account)
                    .into(binding.headerAvatar)
                binding.headerName.text = profileData.name
                Glide.with(this@UserActivity)
                    .load(BASE_URL + profileData.avatar)
                    .apply(RequestOptions().circleCrop())
                    .error(R.drawable.avatar_account)
                    .into(binding.avatar)
                binding.name.text = profileData.name
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
                    binding.follow.background = AppCompatResources.getDrawable(this@UserActivity,
                        R.drawable.gustatex_button_outline)
                    binding.follow.text = getString(R.string.following)
                    binding.follow.setTextColor(colorOnOutlineButton)
                } else {
                    binding.follow.background = AppCompatResources.getDrawable(this@UserActivity,
                        R.drawable.gustatex_button_filled)
                    binding.follow.text = getString(R.string.follow)
                    binding.follow.setTextColor(colorOnFilledButton)
                }
                xfToken = profileData.xfToken
                isProfileLoading = false
                loadUserPost("member?user_id=${profileData.id}", "1", "")
                binding.smartRefreshLayout.finishRefresh()
                loading.cancel()
            }
        }
    }
    private fun loadUserPost(content: String, page: String, url: String) {
        if (isPostLoading || isLastPage) return
        isPostLoading = true
        lifecycleScope.launch {
            try {
                val userPostData = if (page == "1") searchInfoParse(content, "1", true)
                else searchInfoParse(url)
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
                        binding.smartRefreshLayout.finishLoadMoreWithNoMoreData()
                        binding.smartRefreshLayout.setNoMoreData(true)
                    }
                    else {
                        binding.smartRefreshLayout.finishLoadMore()
                    }
                    isPostLoading = false
                }
            }
        }
    }
}
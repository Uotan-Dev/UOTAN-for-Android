package com.gustate.uotan.ui.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
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
import com.gustate.uotan.threads.ui.ThreadsActivity
import com.gustate.uotan.utils.CookieUtil
import com.gustate.uotan.utils.Utils.Companion.baseUrl
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.errorDialog
import com.gustate.uotan.utils.Utils.Companion.getThemeColor
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
    private lateinit var cookieUtil: CookieUtil
    private var currentPage = 1
    private var totalPage = 1
    private var isLastPage = false
    private var isFollow = false
    private var isProfileLoading = false
    private var isPostLoading = false
    private var isFollowing = false
    private var nextPageUrl = ""
    private var cookiesString = ""
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
        colorOnBackgroundPrimary = getThemeColor(this, R.attr.colorOnBackgroundPrimary)
        colorOnBackgroundSecondary = getThemeColor(this, R.attr.colorOnBackgroundSecondary)
        colorOnOutlineButton = getThemeColor(this, R.attr.colorBottomNavigationBarOnBackgroundSecondary)
        colorOnFilledButton = getThemeColor(this, R.attr.colorOnFilledButton)
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
                    topMargin = (systemBars.top + 60f.dpToPx(this@UserActivity)).roundToInt()
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
            errorDialog(this, getString(R.string.intent_error), getString(R.string.intent_error_dialog))
        }
        cookieUtil = CookieUtil(this, baseUrl + url, Cookies)
        cookieUtil.getSecurityInfo(
            onSuccess = { cs, xft ->
                cookiesString = cs
                xfToken = xft
            },
            onError = {
                errorDialog(this, getString(R.string.security_info_error), it)
            }
        )
        loadProfileData(url)
        binding.srlContent.setOnRefreshListener {
            loadProfileData(url)
        }
        binding.srlContent.setOnLoadMoreListener {
            loadUserPost("", "", baseUrl + nextPageUrl)
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
                val follow = follow(url, cookiesString, xfToken)
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
            val profileData = fetchUserData(url)
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
                    binding.tabFollow.background = AppCompatResources.getDrawable(this@UserActivity,
                        R.drawable.gustatex_button_outline)
                    binding.tabFollow.text = getString(R.string.following)
                    binding.tabFollow.setTextColor(colorOnOutlineButton)
                    binding.headerFollow.background = AppCompatResources.getDrawable(this@UserActivity,
                        R.drawable.gustatex_button_outline)
                    binding.headerFollow.text = getString(R.string.following)
                    binding.headerFollow.setTextColor(colorOnOutlineButton)
                } else {
                    binding.tabFollow.background = AppCompatResources.getDrawable(this@UserActivity,
                        R.drawable.uotan_btn_filled)
                    binding.tabFollow.text = getString(R.string.follow)
                    binding.tabFollow.setTextColor(colorOnFilledButton)
                    binding.headerFollow.background = AppCompatResources.getDrawable(this@UserActivity,
                        R.drawable.uotan_btn_filled)
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
                        binding.srlContent.finishLoadMoreWithNoMoreData()
                        binding.srlContent.setNoMoreData(true)
                    }
                    else {
                        binding.srlContent.finishLoadMore()
                    }
                    isPostLoading = false
                }
            }
        }
    }
}
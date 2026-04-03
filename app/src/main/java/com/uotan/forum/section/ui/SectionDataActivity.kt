package com.uotan.forum.section.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.activity.viewModels
import androidx.core.animation.ValueAnimator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.uotan.forum.BaseActivity
import com.uotan.forum.R
import com.uotan.forum.ui.anim.TitleAnim
import com.uotan.forum.databinding.ActivityPlateBinding
import com.uotan.forum.section.data.model.SectionDataItem
import com.uotan.forum.section.ui.adapter.SectionArticleListAdapter
import com.uotan.forum.section.ui.adapter.SectionExpandableArticleListAdapter
import com.uotan.forum.threads.ui.ThreadsActivity
import com.uotan.forum.utils.Utils
import com.uotan.forum.utils.Utils.dpToPx
import com.uotan.forum.utils.Utils.errorDialog
import kotlin.math.roundToInt

class SectionDataActivity : BaseActivity() {

    private val viewModel: SectionDataViewModel by viewModels()

    private lateinit var binding: ActivityPlateBinding
    private lateinit var adapter: SectionArticleListAdapter
    private lateinit var topAdapter: SectionExpandableArticleListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = SectionArticleListAdapter().apply {
            onItemClick = {
                startActivity(Intent(this@SectionDataActivity, ThreadsActivity::class.java).apply {
                    putExtra("url", it)
                })
            }
        }
        val loadingAnimator = ObjectAnimator.ofFloat(
            binding.imgLoading, "rotation", 0f, 360f
        ).apply {
            setDuration(1000)
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.imgLoading.rotation = 0f
                }
                override fun onAnimationCancel(animation: Animator) {
                    binding.imgLoading.rotation = 0f
                }
            })
        }
        loadingAnimator.start()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        topAdapter = SectionExpandableArticleListAdapter().apply {
            onItemClick = { title, url ->
                startActivity(Intent(this@SectionDataActivity, ThreadsActivity::class.java).apply {
                    putExtra("url", url)
                })
            }
        }
        binding.topRecyclerView.adapter = topAdapter
        binding.topRecyclerView.layoutManager = LinearLayoutManager(this)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusBarView.updateLayoutParams<ViewGroup.LayoutParams> { height = systemBars.top }
            TitleAnim(
                binding.tvTitle,
                binding.tvBigTitle,
                (systemBars.top + 60f.dpToPx(this)),
                systemBars.top.toFloat()
            )
            binding.srlRoot.setPadding(systemBars.left, (systemBars.top + 60f.dpToPx(this)).roundToInt(), systemBars.right, systemBars.bottom)
            binding.srlRoot.setHeaderInsetStartPx((systemBars.top + 60f.dpToPx(this)).roundToInt())
            binding.srlRoot.setFooterInsetStartPx(systemBars.bottom)
            insets
        }
        val url = intent.getStringExtra("link") ?: ""
        val ico = intent.getStringExtra("cover")
        val title = intent.getStringExtra("title")
        binding.tvTitle.text = title
        binding.tvBigTitle.text = title
        binding.imgNonePost.isGone = true
        binding.tvNonePost.isGone = true
        Glide.with(this)
            .load(Utils.baseUrl + ico)
            .error(R.drawable.ic_uo)
            .into(binding.imgCover)
        observerList()
        binding.btnExpand.setOnClickListener {
            val willExpand = !topAdapter.isExpanded
            topAdapter.setExpanded(willExpand)
            binding.tvExpand.text = if (willExpand) getString(R.string.retract) else getString(R.string.expand)
        }
        viewModel.loadInitialData(
            url = url,
            isRefresh = false,
            onSuccess = {
                loadingAnimator.cancel()
                binding.imgLoading.isGone = true
                binding.tvLoading.isGone = true
            },
            onException = {
                errorDialog(this, "ERROR", it.message)
            }
        )
        binding.srlRoot.setOnRefreshListener {
            viewModel.loadInitialData(
                url = url,
                isRefresh = true,
                onSuccess = {
                    binding.srlRoot.finishRefresh()
                },
                onException = {
                    errorDialog(
                        context = this,
                        title = "ERROR",
                        message = it.message
                    )
                }
            )
        }
        binding.srlRoot.setOnLoadMoreListener {
            viewModel.loadMoreData(
                onSuccess = {
                    binding.srlRoot.finishLoadMore()
                },
                onException = {
                    errorDialog(
                        context = this,
                        title = "ERROR",
                        message = it.message
                    )
                }
            )
        }
    }

    private fun observerList() {
        viewModel.normalSectionDataList.observe(this) {
            adapter.submitList(it)
            val nonePost = it.first() == SectionDataItem("", "", "", "", "", "", "", "", "")
            binding.recyclerView.isGone = nonePost
            binding.imgNonePost.isGone = !nonePost
            binding.tvNonePost.isGone = !nonePost
        }
        viewModel.topSectionDataList.observe(this) {
            topAdapter.apply {
                submitList(it.take(2))
                fullList.addAll(it)
                binding.btnExpand.isGone = it.size < 3
                binding.tvExpand.text = getString(R.string.expand)
            }
        }
        viewModel.sectionDescribe.observe(this) {
            binding.tvDescribe.text = it
        }
        viewModel.hasMoreData.observe(this) {
            binding.srlRoot.setNoMoreData(!it)
        }
    }
}
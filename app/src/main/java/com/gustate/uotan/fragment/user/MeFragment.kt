package com.gustate.uotan.fragment.user

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.SearchResultActivity.SearchAdapter
import com.gustate.uotan.ui.activity.SettingsActivity
import com.gustate.uotan.anim.TitleAnim
import com.gustate.uotan.databinding.FragmentMeBinding
import com.gustate.uotan.threads.ui.ThreadsActivity
import com.gustate.uotan.utils.Utils.Companion.baseUrl
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.parse.search.FetchResult
import com.gustate.uotan.utils.parse.search.SearchParse.Companion.searchInfoParse
import com.gustate.uotan.utils.parse.user.MeInfo
import com.gustate.uotan.utils.parse.user.MeParse
import com.gustate.uotan.utils.parse.user.MeParse.Companion.doClockIn
import com.gustate.uotan.utils.parse.user.MeParse.Companion.fetchMeData
import com.gustate.uotan.utils.room.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.SocketTimeoutException
import kotlin.math.roundToInt

class MeFragment : Fragment() {

    /** 全类变量 **/
    // 初始化视图绑定
    private var _binding: FragmentMeBinding? = null

    // 只能在 onCreateView 与 onDestroyView 之间的生命周期里使用
    private val binding: FragmentMeBinding get() = _binding!!
    private lateinit var adapter: SearchAdapter
    private lateinit var myPostsResult: FetchResult
    private lateinit var viewModel: UserViewModel

    // 当前页面
    private var currentPage = 1

    // 下一页面 Url
    private var nextPageUrl = ""

    // 总页面
    private var totalPages = 1

    // 是否正在加载
    private var isLoading = false

    // 是否最后一页
    private var isLastPage = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 实例化 binding
        _binding = FragmentMeBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 实例化用户控制的 ViewModel
        viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        binding.srlContent.setOnLoadMoreListener {
            loadData(baseUrl + nextPageUrl)
        }

        binding.srlContent.setOnRefreshListener {
            lifecycleScope.launch {
                val informationResult = fetchMeData()
                val isClockIn = MeParse.isClockIn().isClockIn
                val userId = informationResult.userId
                loadData("member?user_id=$userId", currentPage)
                withContext(Dispatchers.Main) {
                    loadInformationData(informationResult, false)
                    if (isClockIn) {
                        binding.tvClockIn.text = getText(R.string.signed_in_today)
                        binding.btnClockIn.alpha = 0.15f
                    } else {
                        binding.tvClockIn.text = getText(R.string.daily_attendance)
                        binding.btnClockIn.alpha = 1.0f
                    }
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.srlContent.setPadding(
                0, 0, 0,
                (systemBars.bottom + 70f.dpToPx(requireContext())).roundToInt()
            )
            binding.srlContent.setFooterInsetStartPx(
                (systemBars.bottom + 70f.dpToPx(requireContext())).roundToInt()
            )
            binding.headerBarLayout
                .updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = systemBars.top
                }
            binding.btnSettings
                .updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = systemBars.top
                }
            binding.bigTitle
                .updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = (systemBars.top + 60f.dpToPx(requireContext())).roundToInt()
                }
            TitleAnim(
                binding.headerBar,
                binding.avatarImage,
                (systemBars.top + 60f.dpToPx(requireContext())),
                systemBars.top.toFloat(),
                1
            )
            binding.textView6.updateLayoutParams {
                height = systemBars.top - 28f.dpToPx(requireContext()).roundToInt()
            }
            insets
        }

        lifecycleScope.launch {
            loadCachedData()
            val informationResult = fetchMeData()
            val isClockIn = MeParse.isClockIn().isClockIn
            val userId = informationResult.userId
            loadData("member?user_id=$userId", currentPage)
            withContext(Dispatchers.Main) {
                loadInformationData(informationResult, false)
                if (isClockIn) {
                    binding.tvClockIn.text = getText(R.string.signed_in_today)
                    binding.btnClockIn.alpha = 0.15f
                } else {
                    binding.tvClockIn.text = getText(R.string.daily_attendance)
                    binding.btnClockIn.alpha = 1.0f
                }
            }
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        binding.btnSettings2.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        binding.btnClockIn.setOnClickListener {
            if (binding.btnClockIn.alpha == 0.15f) {
                Toast.makeText(
                    requireContext(),
                    R.string.you_have_checked_in_today,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                lifecycleScope.launch {
                    val clockIn = doClockIn()
                    if (clockIn.isClockIn) {
                        Toast.makeText(
                            requireContext(),
                            R.string.sign_in_successful,
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.points.text = clockIn.points
                        binding.tvClockIn.text = getText(R.string.signed_in_today)
                        binding.btnClockIn.alpha = 0.15f
                    } else {
                        Toast.makeText(
                            requireContext(),
                            R.string.check_in_failed,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private suspend fun loadCachedData() = withContext(Dispatchers.IO) {
        val oldUserData = viewModel.getUser()!!
        val informationResult = MeInfo(oldUserData.userName,oldUserData.coverUrl,
            oldUserData.avatarUrl?: "", oldUserData.signature, oldUserData.auth?: "",
            oldUserData.postCount?: "", oldUserData.resCount?: "", oldUserData.userId?: "",
            oldUserData.points?: "", oldUserData.uCoin?: "", oldUserData.ipAddress?: "")
        withContext(Dispatchers.Main) {
            loadInformationData(informationResult, true)
        }
    }

    private fun loadInformationData(informationResult: MeInfo, local: Boolean) {

        binding.srlContent.autoRefresh()

        val typedArray = requireContext().obtainStyledAttributes(
            intArrayOf(
                R.attr.colorOnBackgroundPrimary,
                R.attr.colorOnBackgroundSecondary
            )
        )

        val primaryIndex = typedArray.getIndex(0)
        val secondaryIndex = typedArray.getIndex(1)

        val selectedColor = typedArray.getColor(primaryIndex, Color.RED)
        val normalColor = typedArray.getColor(secondaryIndex, Color.RED)

        typedArray.recycle()

        binding.userName.text = informationResult.userName
        binding.toolbarLayout.title = informationResult.userName
        if (informationResult.signature.isNotEmpty()) {
            binding.signatureText.isGone = false
            binding.signatureText.text = informationResult.signature
        } else {
            binding.signatureText.isGone = true
        }

        if (informationResult.cover.isNotEmpty()) {
            binding.frameLayout.isGone = false
            binding.userCover.isGone = false

            val localCoverFile = if (requireContext().getExternalFilesDir(null) == null) {
                null
            } else {
                File(requireContext().getExternalFilesDir(null), "user/cover.jpg")
            }

            if (local) {
                Glide.with(requireContext())
                    .load(localCoverFile)
                    .error(Color.TRANSPARENT.toDrawable())
                    .into(binding.userCover)
            } else {
                Glide.with(requireContext())
                    .load(baseUrl + informationResult.cover)
                    .error(Color.TRANSPARENT.toDrawable())
                    .into(binding.userCover)
            }

            binding.bigTitle.setTextColor(0xFFFFFFFF.toInt())
            binding.btnSettings.imageTintList = ColorStateList.valueOf(0xFFFFFFFF.toInt())
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
            binding.bigTitle.setTextColor(selectedColor)
            binding.btnSettings.imageTintList = ColorStateList.valueOf(selectedColor)
            binding.userName.setTextColor(selectedColor)
            binding.signatureText.setTextColor(normalColor)
            binding.authText.setTextColor(selectedColor)
            binding.postCount.setTextColor(selectedColor)
            binding.postCountText.setTextColor(normalColor)
            binding.resCount.setTextColor(selectedColor)
            binding.resCountText.setTextColor(normalColor)
        }

        if (informationResult.avatar.isNotEmpty()) {
            val localAvatarFile = if (requireContext().getExternalFilesDir(null) == null) {
                null
            } else {
                File(requireContext().getExternalFilesDir(null), "user/avatar.jpg")
            }
            if (local) {
                Glide.with(requireContext())
                    .load(localAvatarFile)
                    .error(R.drawable.avatar_account)
                    .into(binding.avatarImage)
            } else {
                Glide.with(requireContext())
                    .load(baseUrl + informationResult.avatar)
                    .error(R.drawable.avatar_account)
                    .into(binding.avatarImage)
            }
        }
        if (informationResult.auth.isNotEmpty()) {
            binding.authIcon.isGone = false
            binding.authText.isGone = false
            binding.authText.text = informationResult.auth
        } else {
            binding.authIcon.isGone = true
            binding.authText.isGone = true
        }

        binding.postCount.text = informationResult.postCount
        binding.resCount.text = informationResult.resCount
        binding.points.text = informationResult.points
        binding.uCoin.text = informationResult.uCoin
        binding.srlContent.finishRefresh()
    }

    /**
     * 数据加载的方法
     */
    private fun loadData(content: String, page: Int) {

        // 如果正在加载或在最后一页不执行该方法
        if (isLoading || isLastPage) return

        currentPage = 1
        nextPageUrl = ""
        totalPages = 1
        isLastPage = false
        // 帖子列表配置
        binding.recyclerView.let {
            it.layoutManager = if (it.tag == "pad") StaggeredGridLayoutManager(2, VERTICAL)
            else LinearLayoutManager(requireContext())
        }
        adapter = SearchAdapter().apply {
            onItemClick = { selectedItem ->
                context?.let {
                    startActivity(
                        Intent(it, ThreadsActivity::class.java).apply {
                            putExtra("url", selectedItem.url)
                        }
                    )
                }
            }
        }
        binding.recyclerView.adapter = adapter

        // 设置为正在加载
        isLoading = true
        // 启动协程
        lifecycleScope.launch {
            // 使用 try catch 方法健壮代码
            try {
                // 获取推荐数据
                myPostsResult = searchInfoParse(content, page.toString(), true)
                nextPageUrl = myPostsResult.nextPageUrl
                // 切换到 Main 线程执行
                withContext(Dispatchers.Main) {
                    myPostsResult.items.let { newItems ->
                        adapter.addAll(newItems)
                        currentPage ++
                    }
                    totalPages = myPostsResult.totalPage
                    isLastPage = currentPage > totalPages
                    isLoading = false
                }

            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    if (isLastPage) {
                        binding.srlContent.finishLoadMoreWithNoMoreData()
                        binding.srlContent.setNoMoreData(true)
                    } else {
                        binding.srlContent.finishLoadMore()
                    }
                    isLoading = false
                }
            }
        }
    }

    private fun loadData(url: String) {

        // 如果正在加载或在最后一页不执行该方法
        if (isLoading || isLastPage) return

        // 设置为正在加载
        isLoading = true

        // 启动协程
        lifecycleScope.launch {
            // 使用 try catch 方法健壮代码
            try {
                // 获取推荐数据
                myPostsResult = searchInfoParse(url)
                nextPageUrl = myPostsResult.nextPageUrl
                val adapter = binding.recyclerView.adapter as SearchAdapter
                // 切换到 Main 线程执行
                withContext(Dispatchers.Main) {
                    myPostsResult.items.let { newItems ->
                        adapter.addAll(newItems)
                        currentPage ++
                    }
                    totalPages = myPostsResult.totalPage
                    isLastPage = currentPage > totalPages
                    isLoading = false
                }
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    if (isLastPage) {
                        binding.srlContent.finishLoadMoreWithNoMoreData()
                        binding.srlContent.setNoMoreData(true)
                    } else {
                        binding.srlContent.finishLoadMore()
                    }
                    isLoading = false
                }
            }
        }
    }
}
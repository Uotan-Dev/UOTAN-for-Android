package com.gustate.uotan.fragment.user

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.activity.ArticleActivity
import com.gustate.uotan.activity.SettingsActivity
import com.gustate.uotan.anim.TitleAnim
import com.gustate.uotan.databinding.FragmentMeBinding
import com.gustate.uotan.utils.parse.user.MeParse
import com.gustate.uotan.utils.Utils
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.parse.search.FetchResult
import com.gustate.uotan.utils.parse.search.SearchParse
import com.gustate.uotan.utils.parse.search.SearchResult
import com.gustate.uotan.utils.parse.user.MeInfo
import com.gustate.uotan.utils.parse.user.MeParse.Companion.doClockIn
import com.gustate.uotan.utils.parse.user.MeParse.Companion.fetchMeData
import com.gustate.uotan.utils.room.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.SocketTimeoutException
import kotlin.math.roundToInt

// 用户头像
private var userAvatar = ""

class MeFragment : Fragment() {

    /** 全类变量 **/
    // 初始化视图绑定
    private var _binding: FragmentMeBinding? = null

    // 只能在 onCreateView 与 onDestroyView 之间的生命周期里使用
    private val binding: FragmentMeBinding get() = _binding!!
    private lateinit var adapter: SearchAdapter
    private lateinit var nullAdapter: SearchAdapter
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

    // userID
    private lateinit var userId: String

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

    @SuppressLint("Recycle", "ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** recyclerView 设置 **/
        // 创建线性布局管理器
        val linearLayout = LinearLayoutManager(requireContext())
        // 设置方向为纵向
        linearLayout.orientation = LinearLayoutManager.VERTICAL
        // 为 recyclerView 设置布局管理器
        binding.recyclerView.layoutManager = linearLayout
        nullAdapter = SearchAdapter(requireContext(), mutableListOf())
        adapter = nullAdapter
        viewModel = ViewModelProvider(this)[UserViewModel::class.java]
        binding.recyclerView.adapter = adapter
        // 为 recyclerView 设置滚动监听
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            // recyclerView 当前序号
            private var lastVisibleItem = 0

            // recyclerView 列表总数
            private var totalItemCount = 0

            /**
             * recyclerView 滚动时
             */
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // 获取 recyclerView 的线性布局管理器
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                // 获取当前 recyclerView 的项目总数
                totalItemCount = layoutManager.itemCount
                // 获取 recyclerView 滚动到的项目
                lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                // 当没有在加载、不是最后一页、项目总数 - 5 小于当前项目数
                if (!isLoading && !isLastPage && totalItemCount <= (lastVisibleItem + 5)) {
                    // 加载数据
                    loadData(BASE_URL + nextPageUrl)
                }
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
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
                    height = (systemBars.top + 60f.dpToPx(requireContext())).roundToInt()
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
            insets
        }

        lifecycleScope.launch {
            loadCachedData()
            val informationResult = fetchMeData()
            val isClockIn = MeParse.isClockIn().isClockIn
            userId = informationResult.userId
            userAvatar = informationResult.avatar
            Toast.makeText(requireContext(), userId, Toast.LENGTH_SHORT).show()
            loadData("member?user_id=$userId", currentPage)
            withContext(Dispatchers.Main) {
                loadInformationData(informationResult, false)
                if (isClockIn) {
                    binding.clockIn.text = getText(R.string.signed_in_today)
                    binding.clockIn.alpha = 0.15f
                } else {
                    binding.clockIn.text = getText(R.string.daily_attendance)
                    binding.clockIn.alpha = 1.0f
                }
            }
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        binding.btnSettings2.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        binding.clockIn.setOnClickListener {
            if (binding.clockIn.alpha == 0.15f) {
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
                        binding.clockIn.text = getText(R.string.signed_in_today)
                        binding.clockIn.alpha = 0.15f
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
                    .load(BASE_URL + informationResult.cover)
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
                    .load(BASE_URL + informationResult.avatar)
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
    }

    /**
     * 数据加载的方法
     */
    private fun loadData(content: String, page: Int) {

        // 如果正在加载或在最后一页不执行该方法
        if (isLoading || isLastPage) return

        // 设置为正在加载
        isLoading = true

        // 使用 try catch 方法健壮代码
        try {
            // 启动协程
            lifecycleScope.launch {
                // 切换到 IO 线程执行
                withContext(Dispatchers.IO) {
                    // 获取推荐数据
                    myPostsResult = SearchParse.searchInfoParse(content, page.toString(),true)
                    nextPageUrl = myPostsResult.nextPageUrl
                    val adapter = binding.recyclerView.adapter as SearchAdapter
                    // 切换到 Main 线程执行
                    withContext(Dispatchers.Main) {
                        myPostsResult.items.let { newItems ->
                            if (adapter == nullAdapter) {
                                // 创建新Adapter时设置点击监听
                                val newAdapter = SearchAdapter(
                                    requireContext(),
                                    newItems.toMutableList()
                                ).apply {
                                    onItemClick = { selectedItem ->
                                        // 安全上下文检查
                                        context?.let {
                                            startActivity(
                                                Intent(
                                                    it,
                                                    ArticleActivity::class.java
                                                ).apply {
                                                    putExtra("url", selectedItem.url)
                                                }
                                            )
                                        }
                                    }
                                }
                                binding.recyclerView.adapter = newAdapter
                            } else {
                                adapter.addAll(newItems)
                            }
                            currentPage += 1
                        }
                        totalPages = myPostsResult.totalPage
                        isLastPage = currentPage > totalPages
                        isLoading = false
                    }
                }
            }
        } catch (e: SocketTimeoutException) {

            e.printStackTrace()

        }
    }

    private fun loadData(url: String) {

        // 如果正在加载或在最后一页不执行该方法
        if (isLoading || isLastPage) return

        // 设置为正在加载
        isLoading = true

        // 使用 try catch 方法健壮代码
        try {
            // 启动协程
            lifecycleScope.launch {
                // 切换到 IO 线程执行
                withContext(Dispatchers.IO) {
                    // 获取推荐数据
                    myPostsResult = SearchParse.searchInfoParse(url)
                    nextPageUrl = myPostsResult.nextPageUrl
                    val adapter = binding.recyclerView.adapter as SearchAdapter
                    // 切换到 Main 线程执行
                    withContext(Dispatchers.Main) {
                        myPostsResult.items.let { newItems ->
                            if (adapter == nullAdapter) {
                                // 创建新Adapter时设置点击监听
                                val newAdapter = SearchAdapter(
                                    requireContext(),
                                    newItems.toMutableList()
                                ).apply {
                                    onItemClick = { selectedItem ->
                                        // 安全上下文检查
                                        context?.let {
                                            startActivity(
                                                Intent(
                                                    it,
                                                    ArticleActivity::class.java
                                                ).apply {
                                                    putExtra("url", selectedItem.url)
                                                })
                                        }
                                    }
                                }
                                binding.recyclerView.adapter = newAdapter
                            } else {
                                adapter.addAll(newItems)
                            }
                            currentPage += 1
                        }
                        totalPages = myPostsResult.totalPage
                        isLastPage = currentPage > totalPages
                        isLoading = false
                    }
                }
            }
        } catch (e: SocketTimeoutException) {
            e.printStackTrace()
        }
    }
    class SearchAdapter(private val context: Context, private val searchList: MutableList<SearchResult>):
        RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

        // 点击监听接口
        var onItemClick: ((SearchResult) -> Unit)? = null

        fun addAll(newItems: MutableList<SearchResult>) {
            val startPosition = searchList.size
            searchList.addAll(newItems)
            notifyItemRangeInserted(startPosition, newItems.size)
        }

        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val itemLayout: View = view.findViewById(R.id.itemLayout)
            val coverImage: ImageView = view.findViewById(R.id.coverImage)
            val userLayout: ConstraintLayout = view.findViewById(R.id.userLayout)
            val avatar: ImageView = view.findViewById(R.id.userAvatar)
            val userName: TextView = view.findViewById(R.id.userNameText)
            val time: TextView = view.findViewById(R.id.time)
            val title: TextView = view.findViewById(R.id.title)
            val describe: TextView = view.findViewById(R.id.describe)
            val topic: TextView = view.findViewById(R.id.topic)
            val topicCard: CardView = view.findViewById(R.id.topicCard)
            val viewCount: TextView = view.findViewById(R.id.viewCount)
            val viewCountIco: View = view.findViewById(R.id.viewCountIco)
            val commentCount: TextView = view.findViewById(R.id.commentCount)
            val commentCountIco: View = view.findViewById(R.id.commentCountIco)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_recommend_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val content = searchList[position]

            if (content.cover.isNotEmpty() && !content.cover.startsWith("http")) {
                holder.coverImage.isVisible = true
                Glide.with(holder.itemView.context)
                    .load(BASE_URL + content.cover)
                    .into(holder.coverImage)
                val userParams = holder.userLayout.layoutParams as ViewGroup.MarginLayoutParams
                userParams.topMargin = (12f.dpToPx(context)).roundToInt()
                holder.userLayout.layoutParams = userParams
            } else {
                holder.coverImage.isVisible = false
                val userParams = holder.userLayout.layoutParams as ViewGroup.MarginLayoutParams
                userParams.topMargin = 0
                holder.userLayout.layoutParams = userParams
            }

            if (userAvatar.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(BASE_URL + userAvatar)
                    .into(holder.avatar)
            }

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
}
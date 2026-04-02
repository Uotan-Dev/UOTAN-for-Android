package com.uotan.forum.user.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
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
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.uotan.forum.R
import com.uotan.forum.anim.TitleAnim
import com.uotan.forum.databinding.FragmentMeBinding
import com.uotan.forum.search.ui.adapter.SearchAdapter
import com.uotan.forum.settings.ui.SettingsActivity
import com.uotan.forum.threads.ui.ThreadsActivity
import com.uotan.forum.utils.Helpers.avatarOptions
import com.uotan.forum.utils.Utils
import com.uotan.forum.utils.Utils.baseUrl
import com.uotan.forum.utils.Utils.dpToPx
import com.uotan.forum.utils.Utils.errorDialog
import com.uotan.forum.utils.Utils.getThemeColor
import com.uotan.forum.utils.Utils.openUrlInBrowser
import com.uotan.forum.utils.Utils.showToast
import java.io.File
import kotlin.math.roundToInt

class MeFragment : Fragment() {

    /** 全类变量 **/
    // 初始化视图绑定
    private var _binding: FragmentMeBinding? = null

    // 只能在 onCreateView 与 onDestroyView 之间的生命周期里使用
    private val binding: FragmentMeBinding get() = _binding!!
    private lateinit var adapter: SearchAdapter
    private val viewModel by activityViewModels<MeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 实例化 binding
        _binding = FragmentMeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                .updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = systemBars.top }
            binding.btnSetting
                .updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = systemBars.top }
            binding.bigTitle
                .updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = (systemBars.top + 60f.dpToPx(requireContext()))
                        .roundToInt()
                }
            TitleAnim(binding.headerBar, binding.avatarImage,
                (systemBars.top + 60f.dpToPx(requireContext())),
                systemBars.top.toFloat(), 1
            )
            binding.headerMod.updateLayoutParams {
                height = ((systemBars.top
                        + 60f.dpToPx(requireContext()))
                        - 7f.dpToPx(requireContext())).roundToInt()
            }
            insets
        }
        if (!Utils.isLogin) showToast(requireContext(), R.string.no_login)
        initPage()
        setOnListener()
    }

    private fun initPage() {
        loadProfile()
        updateProfile()
        initRv()
        updatePosts()
        updateSrlState()
        updateClockInState()
    }

    private fun setOnListener() {
        onClockInBtnListener()
        onRefreshListener()
        onLoadMoreListener()
        onSettingClickListener()
        binding.sclRes.setOnClickListener { openUrlInBrowser(requireContext(),
            "$baseUrl/resources/"
        ) }
        binding.sclCollect.setOnClickListener { openUrlInBrowser(requireContext(),
            "$baseUrl/account/bookmarks/"
        ) }
    }

    private fun initRv() {
        adapter = SearchAdapter().apply {
            onItemClick = { item ->
                context?.let {
                    startActivity(
                        Intent(it, ThreadsActivity::class.java).apply {
                            putExtra("url", item.url)
                        }
                    )
                }
            }
        }
        binding.rvPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPosts.adapter = adapter
    }

    private fun loadProfile() {
        viewModel.loadProfile { tDialog(it) }
        viewModel.loadPosts(isInitOrRefresh = true) { tDialog(it) }
    }

    private fun updateProfile() {
        viewModel.profile.observe(viewLifecycleOwner) {
            val selectedColor = getThemeColor(
                requireContext(), R.attr.colorOnBackgroundPrimary
            )
            val normalColor = getThemeColor(
                requireContext(), R.attr.colorOnBackgroundSecondary
            )
            binding.tvUsername.text = it.userName
            binding.toolbarLayout.title = it.userName
            if (it.signature.isNotEmpty()) {
                binding.signatureText.isGone = false
                binding.signatureText.text = it.signature
            } else {
                binding.signatureText.isGone = true
            }
            if (it.cover.isNotEmpty()) {
                binding.frameLayout.isGone = false
                binding.userCover.isGone = false
                val localCoverFile =
                    if (requireContext().getExternalFilesDir(null) == null) null
                    else File(
                        requireContext()
                            .getExternalFilesDir(null), "user/cover.jpg"
                    )
                if (it.cover == "local") {
                    Glide.with(requireContext())
                        .load(localCoverFile)
                        .error(Color.TRANSPARENT.toDrawable())
                        .into(binding.userCover)
                } else {
                    Glide.with(requireContext())
                        .load(baseUrl + it.cover)
                        .error(Color.TRANSPARENT.toDrawable())
                        .into(binding.userCover)
                    viewModel.updateCoverCache(it.cover)
                }
                binding.bigTitle.setTextColor(0xFFFFFFFF.toInt())
                binding.btnSetting.imageTintList = ColorStateList.valueOf(0xFFFFFFFF.toInt())
                binding.tvUsername.setTextColor(0xFFFFFFFF.toInt())
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
                binding.btnSetting.imageTintList = ColorStateList.valueOf(selectedColor)
                binding.tvUsername.setTextColor(selectedColor)
                binding.signatureText.setTextColor(normalColor)
                binding.authText.setTextColor(selectedColor)
                binding.postCount.setTextColor(selectedColor)
                binding.postCountText.setTextColor(normalColor)
                binding.resCount.setTextColor(selectedColor)
                binding.resCountText.setTextColor(normalColor)
            }
            if (it.avatar.isNotEmpty()) {
                val localAvatarFile =
                    if (requireContext().getExternalFilesDir(null) == null) null
                    else File(
                        requireContext()
                            .getExternalFilesDir(null), "user/avatar.jpg"
                    )
                if (it.avatar == "local") {
                    Glide.with(requireContext())
                        .load(localAvatarFile)
                        .apply(avatarOptions)
                        .into(binding.avatarImage)
                } else {
                    Glide.with(requireContext())
                        .load(baseUrl + it.avatar)
                        .apply(avatarOptions)
                        .into(binding.avatarImage)
                    viewModel.updateAvatarCache(it.avatar)
                }
            }
            if (it.auth.isNotEmpty()) {
                binding.authIcon.isGone = false
                binding.authText.isGone = false
                binding.authText.text = it.auth
            } else {
                binding.authIcon.isGone = true
                binding.authText.isGone = true
            }
            binding.postCount.text = it.postCount
            binding.resCount.text = it.resCount
            binding.points.text = it.points
            binding.uCoin.text = it.uCoin
            binding.srlContent.finishRefresh()
        }
    }

    private fun updateClockInState() {
        viewModel.isClockIn.observe(viewLifecycleOwner) {
            if (it) {
                binding.tvClockIn.text = getText(R.string.signed_in_today)
                binding.btnClockIn.alpha = 0.15f
            } else {
                binding.tvClockIn.text = getText(R.string.daily_attendance)
                binding.btnClockIn.alpha = 1.0f
            }
        }
    }

    private fun onClockInBtnListener() {
        binding.btnClockIn.setOnClickListener {
            viewModel.clockInToday(
                onSuccess = {
                    Toast.makeText(
                        requireContext(), R.string.sign_in_successful,
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onThrowable = {
                    Toast.makeText(
                        requireContext(), R.string.check_in_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    errorDialog(
                        requireContext(), getString(R.string.check_in_failed),
                        it.message
                    )
                }
            )
        }
    }

    private fun updatePosts() {
        viewModel.posts.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun onRefreshListener() {
        binding.srlContent.setOnRefreshListener {
            viewModel.loadProfile {
                tDialog(it)
            }
            viewModel.loadPosts(
                isInitOrRefresh = true,
                onSuccess = { binding.srlContent.finishRefresh() },
                onThrowable = { tDialog(it) }
            )
        }
    }

    private fun onLoadMoreListener() {
        binding.srlContent.setOnLoadMoreListener {
            viewModel.loadPosts(
                onSuccess = { binding.srlContent.finishLoadMore() },
                onThrowable = { tDialog(it) }
            )
        }
    }

    private fun onSettingClickListener() {
        binding.btnSetting.setOnClickListener {
            startActivity(Intent(requireContext(),
                SettingsActivity::class.java))
        }
        binding.btnSettingHeader.setOnClickListener {
            startActivity(Intent(requireContext(),
                SettingsActivity::class.java))
        }
    }

    private fun updateSrlState() {
        viewModel.isPostLastPage.observe(viewLifecycleOwner) {
            binding.srlContent.setNoMoreData(true)
        }
    }

    private fun tDialog(t: Throwable) {
        errorDialog(
            requireContext(),
            t.javaClass.name ?: "ERROR",
            t.message
        )
    }
}
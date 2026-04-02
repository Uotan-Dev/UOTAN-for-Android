package com.uotan.forum.main.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.uotan.forum.R
import com.uotan.forum.databinding.ActivityMainBinding
import com.uotan.forum.settings.ui.SettingsActivity
import com.uotan.forum.utils.Helpers.avatarOptions
import com.uotan.forum.utils.Utils
import com.uotan.forum.utils.Utils.dpToPx
import com.uotan.forum.utils.Utils.errorDialog
import com.uotan.forum.utils.Utils.getThemeColor
import com.uotan.forum.utils.Utils.idToAvatar
import com.uotan.forum.utils.mode.AppMode
import com.uotan.forum.utils.room.UserViewModel
import com.uotan.forum.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.math.roundToInt

/**
 * 主页面 (Activity)
 */

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    /** 可变变量 **/
    // 延迟初始化视图绑定
    private lateinit var binding: ActivityMainBinding
    // 延迟初始化 MainPagerAdapter
    private lateinit var adapter: MainPagerAdapter
    // 取 MainViewModel
    private val viewModel by viewModels<MainViewModel>()
    private val userViewModel by viewModels<UserViewModel>()

    /**
     * 视图被创建
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 窗口基础设置
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.main)
        setUXMode()
        setWindow()

        // 底栏按钮颜色
        val navNormalColor = getThemeColor(
            context = this, attr = R.attr.colorOnBackgroundSecondary)
        val navSelectedColor = getThemeColor(
            context = this, attr = R.attr.colorOnBackgroundPrimary)

        // 配置 Pager
        adapter = MainPagerAdapter(this)
        binding.pagerMain.adapter = adapter
        binding.pagerMain.isSaveEnabled = false
        binding.pagerMain.isUserInputEnabled = false

        binding.imgAvatar?.let {
            lifecycleScope.launch {
                val userInfo = userViewModel.getUser()
                Glide.with(this@MainActivity)
                    .load(idToAvatar(userInfo?.userId?:"0"))
                    .apply(avatarOptions)
                    .into(it)
                binding.tvUsername?.text = userInfo?.userName?: ""
            }
        }

        binding.btnSettings?.setOnClickListener {
            startActivity(
                Intent(this, SettingsActivity::class.java))
        }
        
        viewModel.loadInitial(
            {},
            { errorDialog(this, "ERROR", it.message) }
        )

        viewModel.pagerPage.observe(this) {
            // change page
            binding.pagerMain.setCurrentItem(it, false)
            // Phone
            // change tv
            val texts = arrayOf(binding.tvHome, binding.tvSection, binding.tvMessage, binding.tvRes,
                binding.tvMine)
            texts.forEachIndexed { index, textView ->
                textView?.setTextColor(if (index == it) navSelectedColor else navNormalColor)
            }
            // change ico
            val icons = arrayOf(binding.imgHome to R.drawable.ic_nav_home,
                binding.imgSection to R.drawable.ic_nav_plate,
                binding.imgMessage to R.drawable.ic_nav_notice,
                binding.imgRes to R.drawable.ic_nav_res, binding.imgMine to R.drawable.ic_nav_me)
            val selectedIcons = arrayOf(R.drawable.ic_nav_home_selected,
                R.drawable.ic_nav_plate_selected, R.drawable.ic_nav_notice_selected,
                R.drawable.ic_nav_res_selected, R.drawable.ic_nav_me_selected)
            icons.forEachIndexed { index, (icon, defaultResId) ->
                icon?.setImageDrawable(AppCompatResources.getDrawable(this@MainActivity,
                    if (index == it) selectedIcons[index]
                    else defaultResId))
            }
            // Pad
            val tabs = arrayOf(binding.homeTabPad, binding.plateTabPad, binding.noticeTabPad,
                binding.resTabPad, binding.mineTabPad)
            tabs.forEachIndexed { index, tab ->
                tab?.setBackgroundResource(
                    if (index == it) R.drawable.gustatex_nav_option_check
                    else R.drawable.gustatex_nav_option_uncheck
                )
            }
        }

        listOf(binding.tabHome, binding.tabSection, binding.tabMessage, binding.tabRes,
            binding.tabMine).forEachIndexed { index, layout ->
                layout?.setOnClickListener { viewModel.pager(page = index) }
        }

        listOf(binding.homeTabPad, binding.plateTabPad, binding.noticeTabPad, binding.resTabPad,
            binding.mineTabPad).forEachIndexed { index, layout ->
                layout?.setOnClickListener { viewModel.pager(page = index) }
        }
    }

    private fun setUXMode() {
        viewModel.appMode.observe(this) { mode ->
            val isBasicMode = mode == AppMode.BASIC
            binding.tabSection?.isGone = isBasicMode
            binding.tabMessage?.isGone = isBasicMode
            binding.tabRes?.isGone = isBasicMode
        }
    }

    /**
     * 窗体基本设置
     */
    private fun setWindow() {
        // 启用边到边设计
        enableEdgeToEdge()
        // 针对部分系统的系统栏沉浸
        Utils.openImmersion(window)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Pad
            binding.imgUotan?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin =
                    systemBars.top + 36f.dpToPx(this@MainActivity).roundToInt()
            }
            binding.mineTabPad?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin =
                    systemBars.bottom + 6f.dpToPx(this@MainActivity).roundToInt()
            }
            // Phone
            binding.layoutNavBar?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom
            }
            insets
        }
        lifecycleScope.launch {
            f()
        }
    }



    suspend fun f() = withContext(context = Dispatchers.IO) {
        val client = OkHttpClient()
        /*val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(name = "search_type", value = "relevance")
            .addFormDataPart(name = "keywords", value = "MJW")
            .build()
        Log.e("url", "$baseApiUrl/search/")
        val request = Request.Builder()
            .url(url = "$baseApiUrl/search/")
            .addHeader(name = "XF-Api-Key", value = "JHXfW92A_j7wHMJM6tK8Py6Albj0KZpV")
            .addHeader(
                name = "XF-Api-User",
                value = "3059%2CtOK6donAL22ZSsWEcS1j0Xk7GvYzduIMcAmxkz0c"
            )
            .post(requestBody)
            .build()*/
        val jsonBody = """
        {
  "search_type": "post",
  "keywords": "MJW",
  "c": {
    "nodes": [2, 3, 5]  // ← 明确指定你有权访问的 forum IDs
  },
  "order": "relevance"
}
    """.trimIndent()

        val requestBody = jsonBody
            .toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url = "https://www.uotan.cn/api/search/")
            .addHeader(name = "XF-Api-Key", value = "JHXfW92A_j7wHMJM6tK8Py6Albj0KZpV")
            .addHeader(
                name = "XF-Api-User",
                value = "3059%2CtOK6donAL22ZSsWEcS1j0Xk7GvYzduIMcAmxkz0c"
            )
            .post(requestBody)
            .build()
        client.newCall(request).execute().use { response ->
            val responseBody = response.body.string()
            if (!response.isSuccessful) {
                Log.e("test", "${response.code} $responseBody")
            }
            Log.e("test", responseBody)
        }
    }



}
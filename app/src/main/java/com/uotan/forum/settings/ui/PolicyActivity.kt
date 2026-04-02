package com.uotan.forum.settings.ui

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.uotan.forum.BaseActivity
import com.uotan.forum.R
import com.uotan.forum.article.ContentAdapter
import com.uotan.forum.article.HtmlParse
import com.uotan.forum.databinding.ActivityPolicyBinding
import com.uotan.forum.utils.Utils
import com.uotan.forum.utils.Utils.dpToPx
import com.uotan.forum.utils.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.jsoup.Jsoup
import kotlin.math.roundToInt

class PolicyActivity : BaseActivity() {
    private lateinit var binding: ActivityPolicyBinding
    private val adapter = ContentAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.headerBarBlurContent
                .updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = systemBars.top }
            binding.appBarLayout
                .updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = systemBars.top }
            binding.srlRoot.setPadding(
                0,
                116f.dpToPx(this).roundToInt(),
                0,
                (systemBars.top + systemBars.bottom)
            )
            insets
        }
        val titleStringList = listOf(
            // 0 积分规则
            getString(R.string.points_rule),
            // 1 用户公约
            getString(R.string.user_agreement),
            // 2 服务协议
            getString(R.string.service_agreement),
            // 3 授权协议
            getString(R.string.authorization_agreement),
            // 4 隐私政策
            getString(R.string.privacy_policy),
            // 5 免责声明
            getString(R.string.disclaimers),
            // 6 Cookie 使用条款
            getString(R.string.cookie_terms_of_use)
        )
        val url = intent.getStringExtra("url") ?: "${Utils.baseUrl}/help/jfgz/"
        val type = intent.getIntExtra("type", 0)
        val title = titleStringList[type]
        binding.collapsingToolbar.title = title
        binding.rvContent.adapter = adapter
        binding.rvContent.layoutManager = LinearLayoutManager(this)
        binding.toolBar.setNavigationOnClickListener {
            finish()
        }
        lifecycleScope.launch {
            convertToSpannable(url)
        }
    }
    private suspend fun convertToSpannable(url: String) = withContext(Dispatchers.IO) {
        val client = HttpClient.getClient()
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", Utils.USER_AGENT)
            .build()
        val response = client.newCall(request).execute()
        val document = Jsoup.parse(response.body.string())
        val htmlContent = document
            .getElementsByClass("block-body block-row")
            .first()
            ?.html()
            ?: ""
        withContext(Dispatchers.Main) {
            adapter.updateContent(HtmlParse.parse(htmlContent))
        }
    }
}
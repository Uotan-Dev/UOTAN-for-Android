package com.gustate.uotan.activity

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.databinding.ActivityPolicyBinding
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import com.gustate.uotan.utils.Utils.Companion.htmlToSpan
import com.gustate.uotan.utils.Utils.Companion.openImmersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.xml.sax.XMLReader
import java.util.Locale

class PolicyActivity : BaseActivity() {
    private lateinit var binding: ActivityPolicyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPolicyBinding.inflate(layoutInflater)
        setContentView(binding.main)
        openImmersion(window)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusBarView.layoutParams.height = systemBars.top
            binding.statusBarBlurView.layoutParams.height = systemBars.top
            val headerBarMargin = binding.headerBar.layoutParams  as ViewGroup.MarginLayoutParams
            val headerBarBlurLayoutMargin = binding.headerBarBlurLayout.layoutParams as ViewGroup.MarginLayoutParams
            headerBarMargin.topMargin = - systemBars.top
            headerBarBlurLayoutMargin.topMargin = - systemBars.top
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
        val url = intent.getStringExtra("url") ?: "$BASE_URL/help/jfgz/"
        val type = intent.getIntExtra("type", 0)
        val title = titleStringList[type]
        binding.collapsingToolbar.title = title
        lifecycleScope.launch {
            convertToSpannable(url)
        }
    }
    private suspend fun convertToSpannable(url: String) = withContext(Dispatchers.IO) {
        val document = Jsoup
            .connect(url)
            .cookies(Cookies)
            .timeout(TIMEOUT_MS)
            .userAgent(USER_AGENT)
            .get()
        val htmlContent = document
            .getElementsByClass("block-body block-row")
            .first()
            ?.html()
            ?: ""
        withContext(Dispatchers.Main) {
            htmlToSpan(binding.tvContent, htmlContent)
        }
    }
}
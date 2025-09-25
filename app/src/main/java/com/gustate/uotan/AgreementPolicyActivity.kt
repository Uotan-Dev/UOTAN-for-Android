package com.gustate.uotan

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.gustate.uotan.settings.ui.PolicyActivity
import com.gustate.uotan.databinding.ActivityAgreementPolicyBinding
import com.gustate.uotan.utils.Utils.baseUrl
import com.gustate.uotan.utils.Utils.dpToPx
import kotlin.math.roundToInt

class AgreementPolicyActivity : BaseActivity() {
    private lateinit var binding: ActivityAgreementPolicyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgreementPolicyBinding.inflate(layoutInflater)
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
        val btnList = listOf(
            // 0 积分规则
            binding.btnPointsRule,
            // 1 用户公约
            binding.btnUserAgreement,
            // 2 服务协议
            binding.btnServiceAgreement,
            // 3 授权协议
            binding.btnAuthAgreement,
            // 4 隐私政策
            binding.btnPrivacyPolicy,
            // 5 免责声明
            binding.btnDisclaimers,
            // 6 Cookie 使用条款
            binding.btnCookieTermsOfUse
        )
        val urlList = listOf(
            // 0 积分规则
            "$baseUrl/help/jfgz/",
            // 1 用户公约
            "$baseUrl/help/yhgy/",
            // 2 服务协议
            "$baseUrl/help/terms/",
            // 3 授权协议
            "$baseUrl/help/sqxy/",
            // 4 隐私政策
            "$baseUrl/help/privacy-policy/",
            // 5 免责声明
            "$baseUrl/help/mzsm/",
            // 6 Cookie 使用条款
            "$baseUrl/help/cookies/"
        )
        btnList.forEachIndexed { index, it ->
            it.setOnClickListener {
                val intent = Intent(
                    this,
                    PolicyActivity::class.java
                ).apply {
                    putExtra("type", index)
                    putExtra("url", urlList[index])
                }
                startActivity(intent)
            }
        }
        binding.toolBar.setNavigationOnClickListener {
            finish()
        }
    }
}
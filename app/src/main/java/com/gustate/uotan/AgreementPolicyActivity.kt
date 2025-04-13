package com.gustate.uotan

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gustate.uotan.activity.PolicyActivity
import com.gustate.uotan.databinding.ActivityAgreementPolicyBinding
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.openImmersion

class AgreementPolicyActivity : BaseActivity() {
    private lateinit var binding: ActivityAgreementPolicyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgreementPolicyBinding.inflate(layoutInflater)
        enableEdgeToEdge()
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
            "$BASE_URL/help/jfgz/",
            // 1 用户公约
            "$BASE_URL/help/yhgy/",
            // 2 服务协议
            "$BASE_URL/help/terms/",
            // 3 授权协议
            "$BASE_URL/help/sqxy/",
            // 4 隐私政策
            "$BASE_URL/help/privacy-policy/",
            // 5 免责声明
            "$BASE_URL/help/mzsm/",
            // 6 Cookie 使用条款
            "$BASE_URL/help/cookies/"
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
    }
}
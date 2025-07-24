package com.gustate.uotan.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.gustate.uotan.AgreementPolicyActivity
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.ThemeActivity
import com.gustate.uotan.databinding.ActivitySettingsBinding
import com.gustate.uotan.settings.ui.DomainActivity
import com.gustate.uotan.utils.Utils.Companion.baseUrl
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.openUrlInBrowser
import kotlin.math.roundToInt

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)

        // 链接首页窗口布局文件
        setContentView(binding.root)

        /*
         * 修改状态栏和底栏占位布局的高度
         */
        // 取当前页面相对根布局
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.back.updateLayoutParams<MarginLayoutParams> { topMargin = systemBars.top }
            binding.headerBarBlurContent.updateLayoutParams<MarginLayoutParams> { topMargin = systemBars.top }
            binding.appBarLayout.updateLayoutParams<MarginLayoutParams> { topMargin = systemBars.top }
            binding.srlRoot.setPadding(
                0,
                116f.dpToPx(this).roundToInt(),
                0,
                (systemBars.top + systemBars.bottom)
            )
            insets
        }

        /** 设置监听 **/
        // 为关于选项卡设置点击监听
        binding.aboutOption.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        binding.setThemeOption.setOnClickListener {
            val intent = Intent(this, ThemeActivity::class.java)
            startActivity(intent)
        }

        binding.updaterOption.setOnClickListener {
            val intent = Intent(this, UpdaterActivity::class.java)
            startActivity(intent)
        }

        binding.privacyOption.setOnClickListener {
            val intent = Intent(this, AgreementPolicyActivity::class.java)
            startActivity(intent)
        }

        binding.setUserOption.setOnClickListener {
            openUrlInBrowser(this, "$baseUrl/account/account-details")
        }

        binding.domainOption.setOnClickListener {
            val intent = Intent(this, DomainActivity::class.java)
            startActivity(intent)
        }

        binding.back.setOnClickListener {
            finish()
        }
    }
}
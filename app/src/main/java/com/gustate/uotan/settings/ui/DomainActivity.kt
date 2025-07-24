package com.gustate.uotan.settings.ui

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.databinding.ActivityDomainBinding
import com.gustate.uotan.gustatex.dialog.InfoDialog
import com.gustate.uotan.gustatex.dialog.InputDialog
import com.gustate.uotan.settings.data.SettingModel.Companion.DOMAIN_CUSTOM_ENABLED_KEY
import com.gustate.uotan.settings.data.SettingModel.Companion.DOMAIN_CUSTOM_VALUE_KEY
import com.gustate.uotan.settings.data.SettingModel.Companion.SSL_AUTH_DISABLE_KEY
import kotlin.getValue

class DomainActivity : BaseActivity() {
    private lateinit var binding: ActivityDomainBinding
    private val viewModel by viewModels<DomainViewModel>()

    // 提取监听器避免匿名内部类
    private val domainSwitchListener = CompoundButton.OnCheckedChangeListener { _, checked ->
        viewModel.updateSettingValue(DOMAIN_CUSTOM_ENABLED_KEY, checked.toString())
        restart()
    }

    private val sslSwitchListener = CompoundButton.OnCheckedChangeListener { _, checked ->
        viewModel.updateSettingValue(SSL_AUTH_DISABLE_KEY, checked.toString())
        restart()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDomainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.switchMonet2.setOnCheckedChangeListener(domainSwitchListener)
        binding.ddd.setOnClickListener {
            val inputDialog = InputDialog(this@DomainActivity)
            inputDialog
                .setTitle(getString(R.string.domain_customization))
                .setDescription(getString(R.string.domain_customization))
                .setCancel(getString(R.string.cancel))
                .setConfirm(getString(R.string.ok))
                .withOnCancel {
                    inputDialog.dismiss()
                }
                .withOnConfirm {
                    viewModel.updateSettingValue(DOMAIN_CUSTOM_VALUE_KEY, it)
                    inputDialog.dismiss()
                    restart()
                }
                .show()
        }
        binding.switchMonet3.setOnCheckedChangeListener(sslSwitchListener)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBarLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            binding.headerBarBlurContent.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            insets
        }
        viewModel.isDomainCustom.observe(this) { isChecked ->
            binding.switchMonet2.setOnCheckedChangeListener(null)
            binding.switchMonet2.isChecked = isChecked
            binding.switchMonet2.setOnCheckedChangeListener(domainSwitchListener)
        }

        viewModel.customDomainValue.observe(this) {
            binding.ddd.setDescribe(it)
        }

        viewModel.isSslAuthDisable.observe(this) { isChecked ->
            binding.switchMonet3.setOnCheckedChangeListener(null)
            binding.switchMonet3.isChecked = isChecked
            binding.switchMonet3.setOnCheckedChangeListener(sslSwitchListener)
        }

        binding.toolBar.setNavigationOnClickListener {
            finish()
        }

    }

    private fun restart() {
        val dialog = InfoDialog(this)
        dialog
            .setTitle(getString(R.string.restart_software))
            .setDescription(getString(R.string.dialog_restart_ensure))
            .setCancelText(getString(R.string.restart_later))
            .setConfirmText(getString(R.string.restart_now))
            .withOnCancel {
                dialog.dismiss()
            }
            .withOnConfirm {
                dialog.dismiss()
                val packageManager: PackageManager = this.packageManager
                val intent: Intent? = packageManager.getLaunchIntentForPackage(this.packageName)
                val componentName: ComponentName? = intent?.component
                val mainIntent: Intent = Intent.makeRestartActivityTask(componentName)
                this.startActivity(mainIntent)
                this.finishAffinity() // 关闭所有关联Activity
                Runtime.getRuntime().exit(0) // 结束进程
            }
            .show()
    }
}
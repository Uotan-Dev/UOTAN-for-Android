package com.uotan.forum.settings.ui

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.uotan.forum.BaseActivity
import com.uotan.forum.ThemePreference
import com.uotan.forum.databinding.ActivityThemeBinding
import com.uotan.forum.utils.Utils
import com.uotan.forum.utils.Utils.dpToPx
import kotlin.math.roundToInt

class ThemeActivity : BaseActivity() {
    private lateinit var binding: ActivityThemeBinding
    private var isUserInput = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemeBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        Utils.openImmersion(window)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.headerBarBlurContent.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = systemBars.top }
            binding.appBarLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = systemBars.top }
            binding.srlRoot.setPadding(
                0,
                116f.dpToPx(this).roundToInt(),
                0,
                (systemBars.top + systemBars.bottom)
            )
            insets
        }
        val switchMonet = binding.switchMonet
        when (ThemePreference.getTheme(this)) {
            "base" -> {
                isUserInput = false
                switchMonet.isChecked = false
                isUserInput = true
            }
            "monet" -> {
                isUserInput = false
                switchMonet.isChecked = true
                isUserInput = true
            }
            else -> {
                isUserInput = false
                switchMonet.isChecked = false
                isUserInput = true
            }
        }
        switchMonet.setOnCheckedChangeListener { _, isChecked ->
            if (isUserInput) {
                if (isChecked) {
                    ThemePreference.saveTheme(this, "monet")
                    this.recreate()
                } else {
                    ThemePreference.saveTheme(this, "base")
                    this.recreate()
                }
            }
        }
        binding.toolBar.setNavigationOnClickListener {
            finish()
        }
    }
}
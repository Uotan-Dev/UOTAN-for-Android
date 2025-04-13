package com.gustate.uotan.activity

import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.databinding.ActivityFindUpdateBinding
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import kotlin.math.roundToInt

class FindUpdateActivity : BaseActivity() {

    private lateinit var binding: ActivityFindUpdateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindUpdateBinding.inflate(layoutInflater)
        setContentView(binding.main)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusBarView.updateLayoutParams<ViewGroup.LayoutParams> { height = systemBars.top }
            binding.statusBarBlurView.updateLayoutParams<ViewGroup.LayoutParams> { height = systemBars.top }
            binding.appBarLayout.updateLayoutParams<MarginLayoutParams> { topMargin = systemBars.top }
            binding.smartRefreshLayout.updateLayoutParams<MarginLayoutParams> { topMargin = - (systemBars.top + 60f.dpToPx(this@FindUpdateActivity)).roundToInt() }
            binding.smartRefreshLayout.setPadding(0, (systemBars.top + 60f.dpToPx(this@FindUpdateActivity)).roundToInt(), 0, 0)
            binding.scrollView.updateLayoutParams<MarginLayoutParams> { topMargin = - (systemBars.top + 60f.dpToPx(this@FindUpdateActivity)).roundToInt() }
            binding.scrollView.setPadding(0, (systemBars.top + 60f.dpToPx(this@FindUpdateActivity)).roundToInt(), 0, 0)
            binding.recyclerView.setPadding(0, 0, 0, (103f.dpToPx(this).roundToInt() + systemBars.top + systemBars.bottom))
            insets
        }
    }
}
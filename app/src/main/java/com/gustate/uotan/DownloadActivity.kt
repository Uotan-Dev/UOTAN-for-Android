package com.gustate.uotan

import android.os.Bundle
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gustate.uotan.databinding.ActivityDownloadBinding
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.tonyodev.fetch2.Fetch
import kotlin.math.roundToInt

/**
 * 下载管理 (Activity)
 */
class DownloadActivity : BaseActivity() {
    // 视图绑定
    private lateinit var binding: ActivityDownloadBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusBarView.layoutParams.height = systemBars.top
            binding.statusBarBlurView.layoutParams.height = systemBars.top
            val appBarLayoutMargin = binding.appBarLayout.layoutParams as MarginLayoutParams
            appBarLayoutMargin.topMargin = systemBars.top
            binding.recyclerView.setPadding(
                0,
                126f.dpToPx(this).roundToInt(),
                0,
                systemBars.top + systemBars.bottom
            )
            insets
        }

    }
}
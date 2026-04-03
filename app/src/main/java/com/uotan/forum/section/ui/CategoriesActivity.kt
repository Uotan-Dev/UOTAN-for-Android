package com.uotan.forum.section.ui

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import com.uotan.forum.BaseActivity
import com.uotan.forum.databinding.ActivityCategoriesBinding
import com.uotan.forum.ui.dialog.ErrorDialog
import com.uotan.forum.section.ui.adapter.SectionAdapter
import com.uotan.forum.utils.Utils.dpToPx
import kotlin.math.roundToInt

class CategoriesActivity : BaseActivity() {

    // 版块大类私有变量
    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var errorDialog: ErrorDialog
    private lateinit var adapter: SectionAdapter
    private val viewModel by viewModels<CategoriesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        errorDialog = ErrorDialog(this)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.layoutAppBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            binding.contentHeaderBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            binding.srlSection.setPadding(
                0,
                116f.dpToPx(this).roundToInt(),
                0,
                (systemBars.top + systemBars.bottom)
            )
            insets
        }
        adapter = SectionAdapter().apply {
            onItemClick = { link, cover, title ->
                startActivity(
                    Intent(
                        this@CategoriesActivity,
                        SectionDataActivity::class.java
                    ).apply {
                        putExtra("link", link)
                        putExtra("cover", cover)
                        putExtra("title", title)
                    }
                )
            }
        }
        binding.rvSection.adapter = adapter
        binding.rvSection.layoutManager = LinearLayoutManager(this)
        val url = intent.getStringExtra("link") ?:""
        viewModel.updateSectionList(
            url = url,
            onThrowable = {
                errorDialog.setupErrorDialog(it)
            }
        )
        viewModel.sectionList.observe(this) {
            adapter.submitList(it)
        }
        binding.layoutCollapsingBar.title = intent.getStringExtra("title") ?: ""
    }
}
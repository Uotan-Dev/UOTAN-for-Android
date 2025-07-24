package com.gustate.uotan.section.ui

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.databinding.ActivityCategoriesBinding
import com.gustate.uotan.dialog.ErrorDialog
import com.gustate.uotan.section.ui.adapter.SectionAdapter

class CategoriesActivity : BaseActivity() {

    // 版块大类私有变量
    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var errorDialog: ErrorDialog
    private val viewModel by viewModels<CategoriesViewModel>()
    private val adapter = SectionAdapter()

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
            insets
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
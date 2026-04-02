package com.uotan.forum.search.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.uotan.forum.BaseActivity
import com.uotan.forum.databinding.ActivitySearchResultBinding
import com.uotan.forum.dialog.LoadingDialog
import com.uotan.forum.search.ui.adapter.SearchAdapter
import com.uotan.forum.threads.ui.ThreadsActivity
import com.uotan.forum.utils.Utils.dpToPx
import com.uotan.forum.utils.Utils.errorDialog
import kotlin.math.roundToInt

class SearchResultActivity : BaseActivity() {

    private val viewModel by viewModels<SearchViewModel>()
    private lateinit var adapter: SearchAdapter
    private lateinit var binding: ActivitySearchResultBinding
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.main)
        loadingDialog = LoadingDialog(this)
        adapter = SearchAdapter().apply {
            onItemClick = { selectedItem ->
                startActivity(
                    Intent(this@SearchResultActivity,
                        ThreadsActivity::class.java).apply {
                        putExtra("url", selectedItem.url)
                    }
                )
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusBarView.layoutParams.height = systemBars.top
            binding.srlRoot.setPadding(0,
                (systemBars.top + 70f.dpToPx(this)).roundToInt(), 0,
                systemBars.bottom)
            binding.srlRoot.setHeaderInsetStartPx(
                (systemBars.top + 70f.dpToPx(this)).roundToInt())
            binding.srlRoot.setFooterInsetStartPx(systemBars.bottom)
            insets
        }
        loadingDialog.show()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        val content = intent.getStringExtra("content") ?: ""
        binding.etSearch.text = content
        viewModel.loadData(
            content, true,
            onSuccess = {
                loadingDialog.dismiss()
            }, onException = {
                errorDialog(this, it.javaClass.name, it.message)
            }
        )
        binding.srlRoot.setOnRefreshListener {
            viewModel.loadData(
                content, true,
                onSuccess = {
                    binding.srlRoot.finishRefresh()
                }, onException = {
                    errorDialog(this, it.javaClass.name, it.message)
                }
            )
        }
        binding.srlRoot.setOnLoadMoreListener {
            viewModel.loadData(
                content, false,
                onSuccess = {
                    binding.srlRoot.finishLoadMore()
                }, onException = {
                    errorDialog(this, it.javaClass.name, it.message)
                }
            )
        }
        viewModel.isLastPage.observe(this) {
            binding.srlRoot.setNoMoreData(it)
        }
        viewModel.searchList.observe(this) {
            adapter.submitList(it)
        }
        binding.etSearch.setOnClickListener {
            finish()
        }
        binding.back.setOnClickListener {
            finish()
        }
    }
}
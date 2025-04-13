package com.gustate.uotan

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.gustate.uotan.databinding.ActivitySearchBinding
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.openImmersion
import java.util.LinkedList
import kotlin.math.roundToInt

/**
 * 搜索页面 (Activity)
 */

class SearchActivity : BaseActivity() {
    class SearchHistoryAdapter(private val searchHistoryList: LinkedList<String>) : Adapter<SearchHistoryAdapter.ViewHolder>() {
        var onItemClick: ((String) -> Unit)? = null
        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val tvHistory: TextView = view.findViewById(R.id.tvHistory)
            val itemLayout: View = view.findViewById(R.id.itemLayout)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_search_history_item, parent, false)
            return ViewHolder(view)
        }
        override fun getItemCount(): Int = searchHistoryList.size
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tvHistory.text = searchHistoryList[position]
            holder.itemLayout.setOnClickListener {
                onItemClick?.invoke(searchHistoryList[position])
            }
        }
    }
    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: SearchHistoryAdapter
    private val searchHistory = LinkedList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        openImmersion(window)
        setContentView(binding.main)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusBarView.layoutParams.height = systemBars.top
            binding.refreshLayout.setPadding(0, systemBars.top + 60f.dpToPx(this).roundToInt(), 0 , systemBars.bottom)
            insets
        }
        val flexboxLayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }
        binding.recyclerView.layoutManager = flexboxLayoutManager
        loadFromLocalStorage()
        adapter = SearchHistoryAdapter(searchHistory).apply {
            onItemClick = { content ->
                addSearchQuery(content)
                // 安全上下文检查
                startActivity(
                    Intent(
                        this@SearchActivity,
                        SearchResultActivity::class.java
                    ).apply {
                        putExtra("content", content)
                    }
                )
            }
        }
        binding.recyclerView.adapter = adapter
        binding.btnSearch.setOnClickListener {
            if (binding.etSearch.text.isNotEmpty()){
                addSearchQuery(binding.etSearch.text.toString())
                adapter = SearchHistoryAdapter(searchHistory).apply {
                    onItemClick = { content ->
                        addSearchQuery(content)
                        // 安全上下文检查
                        startActivity(
                            Intent(
                                this@SearchActivity,
                                SearchResultActivity::class.java
                            ).apply {
                                putExtra("content", content)
                            }
                        )
                    }
                }
                binding.recyclerView.adapter = adapter
                val intent = Intent(
                    this,
                    SearchResultActivity::class.java
                ).apply {
                    putExtra(
                        "content",
                        binding.etSearch.text.toString()
                    )
                }
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    R.string.enter_search_content,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.btnClear.setOnClickListener {
            clearAllHistory()
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun addSearchQuery(query: String) {
        // 移除已存在的相同记录
        searchHistory.removeAll { it.equals(query, ignoreCase = true) }
        // 插入到首位
        searchHistory.add(0, query.trim())
        saveToLocalStorage()
        adapter.notifyDataSetChanged() // 新增此行
    }
    private fun clearAllHistory() {
        searchHistory.clear()
        saveToLocalStorage()
    }
    private fun saveToLocalStorage() {
        val prefs = this.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putStringSet("history", LinkedHashSet(searchHistory))
            apply() // 异步提交
        }
    }
    private fun loadFromLocalStorage() {
        val prefs = this.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        searchHistory.addAll(prefs.getStringSet("history", emptySet()) ?: emptySet())
    }
}
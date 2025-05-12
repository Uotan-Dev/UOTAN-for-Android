package com.gustate.uotan.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.databinding.ActivityUpdaterBinding
import com.gustate.uotan.utils.Utils
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.parse.update.UpdateLog
import com.gustate.uotan.utils.parse.update.UpdateParse
import com.gustate.uotan.utils.parse.update.UpdateParse.Companion.isVerLow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class UpdaterActivity : BaseActivity() {
    class UpdateLogAdapter() : Adapter<UpdateLogAdapter.ViewHolder>() {
        private val updateLogList = mutableListOf<UpdateLog>()
        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.tv_title)
            val describe: TextView = view.findViewById(R.id.describe)
        }
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_updater_log_item, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.title.text = updateLogList[position].category
            holder.describe.text = updateLogList[position].description
        }
        override fun getItemCount(): Int = updateLogList.size
        fun addAll(newItems: MutableList<UpdateLog>) {
            val startPosition = updateLogList.size
            updateLogList.addAll(newItems)
            notifyItemRangeInserted(startPosition, newItems.size)
        }
    }

    // 视图绑定
    private lateinit var binding: ActivityUpdaterBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var updateLogAdapter: UpdateLogAdapter
    private lateinit var versionName: String
    private lateinit var versionCode: String
    private var hasNewVersion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdaterBinding.inflate(layoutInflater)
        setContentView(binding.main)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusBarView.updateLayoutParams<ViewGroup.LayoutParams> { height = systemBars.top }
            binding.statusBarBlurView.updateLayoutParams<ViewGroup.LayoutParams> { height = systemBars.top }
            binding.appBarLayout.updateLayoutParams<MarginLayoutParams> { topMargin = systemBars.top }
            binding.smartRefreshLayout.updateLayoutParams<MarginLayoutParams> { topMargin = - (systemBars.top + 60f.dpToPx(this@UpdaterActivity)).roundToInt() }
            binding.smartRefreshLayout.setPadding(0, (systemBars.top + 60f.dpToPx(this@UpdaterActivity)).roundToInt(), 0, 0)
            binding.scrollView.updateLayoutParams<MarginLayoutParams> { topMargin = - (systemBars.top + 60f.dpToPx(this@UpdaterActivity)).roundToInt() }
            binding.scrollView.setPadding(0, (systemBars.top + 60f.dpToPx(this@UpdaterActivity)).roundToInt(), 0, 0)
            binding.recyclerView.setPadding(0, 0, 0, (103f.dpToPx(this).roundToInt() + systemBars.top + systemBars.bottom))
            insets
        }
        recyclerView = binding.recyclerView
        updateLogAdapter = UpdateLogAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = updateLogAdapter
        versionName = Utils.getVersionName(this)
        versionCode = Utils.getVersionCode(this)
        lifecycleScope.launch {
            checkUpdate()
        }
        binding.versionName.text = versionName
        binding.collapsingToolbar.title = versionName
        lifecycleScope.launch {
            val versionInfo = UpdateParse.fetchUpdateLog()
            withContext(Dispatchers.Main) {
                val historyVersions = versionInfo.historyVersions
                var currentPosition = 0
                for (index in historyVersions.indices) {
                    if (versionCode == historyVersions[index].code.toString()) {
                        currentPosition = index
                    }
                }
                val updateLog = versionInfo.historyVersions[currentPosition].changelog
                updateLogAdapter.addAll(updateLog)
            }
        }
        binding.back.setOnClickListener{
            finish()
        }
    }
    private suspend fun checkUpdate() = withContext(Dispatchers.IO) {
        val isVerLow = isVerLow(this@UpdaterActivity)
        withContext(Dispatchers.Main) {
            if (isVerLow) {
                Toast.makeText(this@UpdaterActivity, R.string.discover_new_version, Toast.LENGTH_SHORT).show()
                hasNewVersion = true
                setUpdateButton()
            } else {
                Toast.makeText(this@UpdaterActivity, R.string.is_latest_version, Toast.LENGTH_SHORT).show()
                hasNewVersion = false
                setUpdateButton()
            }
        }
    }
    private fun setUpdateButton() {
        if (hasNewVersion) {
            binding.chkUpdate.apply {
                text = getText(R.string.go_update)
                setOnClickListener {
                    startActivity(
                        Intent(
                            this@UpdaterActivity,
                            FindUpdateActivity::class.java
                        )
                    )
                }
            }
            binding.btnNewVer.isGone = false
            binding.btnNewVer.setOnClickListener {
                startActivity(
                    Intent(
                    this@UpdaterActivity,
                    FindUpdateActivity::class.java
                    )
                )
            }
        } else {
            binding.chkUpdate.apply {
                text = getText(R.string.check_for_updates)
                setOnClickListener {
                    lifecycleScope.launch {
                        checkUpdate()
                    }
                }
            }
            binding.btnNewVer.setOnClickListener { null }
            binding.btnNewVer.isGone = true
        }
    }
}
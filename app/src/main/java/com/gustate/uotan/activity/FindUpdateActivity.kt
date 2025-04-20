package com.gustate.uotan.activity

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.activity.UpdaterActivity.UpdateLogAdapter
import com.gustate.uotan.databinding.ActivityFindUpdateBinding
import com.gustate.uotan.gustatex.dialog.InfoDialog
import com.gustate.uotan.utils.Utils.Companion.downloadFile
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.parse.update.UpdateParse.Companion.fetchNewVersion
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2core.DownloadBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt


class FindUpdateActivity : BaseActivity() {

    private lateinit var binding: ActivityFindUpdateBinding
    private lateinit var adapter: UpdateLogAdapter
    private lateinit var settingsLauncher: ActivityResultLauncher<Intent>
    private lateinit var installLauncher: ActivityResultLauncher<Intent>
    private lateinit var downloadLink: String

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
        adapter = UpdateLogAdapter()
        settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            checkPermissionAfterReturnFromSettings()
        }
        installLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            checkPermissionAfterReturnFromInstall()
        }
        lifecycleScope.launch {
            val newVerInfo = fetchNewVersion()
            withContext(Dispatchers.Main) {
                binding.collapsingToolbar.title = newVerInfo.newVersionName
                binding.versionName.text = newVerInfo.newVersionName
                adapter.addAll(newVerInfo.newVersionLog)
                downloadLink = newVerInfo.newVersionLink
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        binding.btnDownload.setOnClickListener {
            if (!::downloadLink.isInitialized) {
                Toast.makeText(
                    this,
                    R.string.please_try_again_later,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (!Environment.isExternalStorageManager()) {
                val infoDialog = InfoDialog(this)
                infoDialog
                    .setTitle("权限")
                    .setDescription("所有文件访问权，受权隐私严格保障")
                    .setCancelText("暂不授权")
                    .setConfirmText("前往授权")
                    .withOnCancel {
                        Toast.makeText(this, "授权才能下载", Toast.LENGTH_SHORT).show()
                        infoDialog.dismiss()
                    }
                    .withOnConfirm {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.setData(("package:" + this.packageName).toUri())
                        settingsLauncher.launch(intent)
                        infoDialog.dismiss()
                    }
                    .show()
                return@setOnClickListener
            }
            downloadNewVer(downloadLink.toUri().lastPathSegment!!)
        }
    }
    private fun checkPermissionAfterReturnFromSettings() {
        if (Environment.isExternalStorageManager()) {
            Toast.makeText(this, "继续操作", Toast.LENGTH_SHORT).show()
            downloadNewVer(downloadLink.toUri().lastPathSegment!!)
        } else {
            Toast.makeText(this, "失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadNewVer(fileName: String) {
        val fetchData = downloadFile(this, downloadLink)
        val fetch = fetchData.fetch
        fetch.addListener(object : FetchListener {
            override fun onAdded(download: Download) {
            }

            override fun onQueued(
                download: Download,
                waitingOnNetwork: Boolean
            ) {
            }

            override fun onWaitingNetwork(download: Download) {
                Toast.makeText(this@FindUpdateActivity, "等待网络", Toast.LENGTH_SHORT).show()
            }

            override fun onCompleted(download: Download) {
                Toast.makeText(this@FindUpdateActivity, "下载完成", Toast.LENGTH_SHORT).show()
                if (this@FindUpdateActivity.packageManager.canRequestPackageInstalls()) {
                    installApk(fileName)
                } else {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        ("package:$packageName").toUri()
                    )
                    installLauncher.launch(intent)
                }
            }

            override fun onError(
                download: Download,
                error: Error,
                throwable: Throwable?
            ) {
                Toast.makeText(this@FindUpdateActivity, "错误：$error", Toast.LENGTH_SHORT).show()
            }

            override fun onDownloadBlockUpdated(
                download: Download,
                downloadBlock: DownloadBlock,
                totalBlocks: Int
            ) {
            }

            override fun onStarted(
                download: Download,
                downloadBlocks: List<DownloadBlock>,
                totalBlocks: Int
            ) {
                binding.btnDownload.isGone = true
                binding.tvProgress.isGone = false
                binding.progressBar.isGone = false
                binding.btnDownload.setOnClickListener(null)
                binding.progressBar.setOnClickListener {
                    fetch.pause(download.id)
                }
            }

            override fun onProgress(
                download: Download,
                etaInMilliSeconds: Long,
                downloadedBytesPerSecond: Long
            ) {
                // 计算进度百分比
                val progress = download.progress
                // 主线程更新 UI
                runOnUiThread {
                    binding.progressBar.progress = progress
                    val strProgress = "${getString(R.string.downloading)} $progress%"
                    binding.tvProgress.text = strProgress
                }
            }

            override fun onPaused(download: Download) {
                binding.btnDownload.isGone = false
                binding.tvProgress.isGone = true
                binding.progressBar.isGone = true
                binding.progressBar.setOnClickListener(null)
                binding.btnDownload.setOnClickListener {
                    fetch.resume(download.id)
                }
            }

            override fun onResumed(download: Download) {
                binding.btnDownload.isGone = true
                binding.tvProgress.isGone = false
                binding.progressBar.isGone = false
                binding.btnDownload.setOnClickListener(null)
                binding.progressBar.setOnClickListener {
                    fetch.pause(download.id)
                }
            }

            override fun onCancelled(download: Download) {
                //
            }

            override fun onRemoved(download: Download) {
                //
            }

            override fun onDeleted(download: Download) {
                //
            }
        })
    }

    private fun checkPermissionAfterReturnFromInstall() {
        if (this.packageManager.canRequestPackageInstalls()) {
            Toast.makeText(this, "继续操作", Toast.LENGTH_SHORT).show()
            installApk(downloadLink.toUri().lastPathSegment!!)
        } else {
            Toast.makeText(this, "失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun installApk(fileName: String) {
        val apkFile = File(Environment.getExternalStorageDirectory(), "Download/Uotan/$fileName")
        Log.e("e", fileName)
        val uri = FileProvider.getUriForFile(
            this,
            "${this.packageName}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }
}
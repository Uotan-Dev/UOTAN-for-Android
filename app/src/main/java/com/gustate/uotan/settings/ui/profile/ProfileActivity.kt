package com.gustate.uotan.settings.ui.profile

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.loader.content.CursorLoader
import com.bumptech.glide.Glide
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.databinding.ActivityProfileBinding
import com.gustate.uotan.dialog.InfoDialog
import com.gustate.uotan.dialog.InputDialog
import com.gustate.uotan.dialog.LoadingDialog
import com.gustate.uotan.utils.Helpers.avatarOptions
import com.gustate.uotan.utils.PermissionUtils
import com.gustate.uotan.utils.Utils.dpToPx
import com.gustate.uotan.utils.Utils.errorDialog
import com.gustate.uotan.utils.network.HttpClient
import com.gustate.uotan.utils.parse.data.CookiesManager
import com.gustate.uotan.utils.room.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

class ProfileActivity : BaseActivity(), PermissionUtils.PermissionCallback {

    private val requestCodePermission = 1001

    private lateinit var binding: ActivityProfileBinding
    private val viewModel by viewModels<ProfileViewModel>()
    private val userViewModel by viewModels<UserViewModel>()
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.headerBarBlurContent.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top }
            binding.appBarLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top }
            binding.srlRoot.setPadding(0, 116f.dpToPx(this).roundToInt(),
                0, (systemBars.top + systemBars.bottom))
            insets
        }
        viewModel.loadUserInfo()
        updateBaseProfile()
        updateUserName()
        setNavListener()
        setOnAvatarOptionClickListener()
        setOnLogoutBtnClickListener()
    }

    private fun updateBaseProfile() {
        viewModel.avatar.observe(this) {
            Glide.with(this)
                .load(it)
                .apply(avatarOptions)
                .into(binding.imgAvatar)
        }
        viewModel.username.observe(this) {
            binding.odvUsername.detail = it
        }
        viewModel.email.observe(this) {
            binding.odvEmail.detail = it
        }
    }

    private fun setNavListener() {
        binding.toolBar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setOnAvatarOptionClickListener() {
        binding.odvAvatar.setOnClickListener {
            PermissionUtils.checkPermission(
                this, requestCodePermission, this)
        }
    }

    // 处理权限结果
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtils.handlePermissionResult(
            requestCode, grantResults, requestCodePermission, this)
    }

    // 权限授予回调
    override fun onPermissionGranted() {
        pickImageLauncher.launch("image/*")
    }

    // 权限拒绝回调
    override fun onPermissionDenied() {
        Toast.makeText(
            this,
            "需要权限才能选择图片",
            Toast.LENGTH_SHORT
        ).show()
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            loadingDialog.show()
            // 获取文件路径
            val filePath = getPathFromUri(uri)
            // 获取文件
            val file = File(filePath)
            viewModel.updateAvatar(
                file,
                onSuccess = {
                    Log.e("s", "viewModelSuccess")
                    viewModel.applyAvatar(
                        file,
                        it,
                        {
                            loadingDialog.cancel()
                            Toast.makeText(this, getString(R.string.modified_successfully),
                                Toast.LENGTH_SHORT).show()
                        },
                        { code, body ->
                            loadingDialog.cancel()
                            errorDialog(this, getString(R.string.error) + " " + code, body)
                        },
                        onThrowable = { throwable ->
                            loadingDialog.cancel()
                            errorDialog(this, getString(R.string.error), throwable.message)
                        }
                    )
                },
                onThrowable = {
                    loadingDialog.cancel()
                    errorDialog(this, getString(R.string.error), it.message)
                    throw it
                },
                onFailure = { code, message ->
                    loadingDialog.cancel()
                    errorDialog(this, getString(R.string.error) + " " + code, message)
                }
            )
        }
    }

    private fun updateUserName() {
        binding.odvUsername.setOnClickListener {
            val inputDialog = InputDialog(this)
            inputDialog
                .setTwoEdit(true)
                .setEditHint(getString(R.string.hint_new_username), getString(R.string.reason_for_modification))
                .setTitle(getString(R.string.rename_user))
                .setDescription(getString(R.string.change_username_time))
                .setCancel(getString(R.string.cancel))
                .setConfirm(getString(R.string.ok))
                .withOnCancel { inputDialog.cancel() }
                .withOnConfirm { a, b ->
                    loadingDialog.show()
                    viewModel.updateUserName(
                        name = a, reason = b,
                        onSuccess = {
                            loadingDialog.cancel()
                            inputDialog.cancel()
                            Toast.makeText(this,
                                getString(R.string.modified_successfully), Toast.LENGTH_SHORT).show()
                        }, onThrowable = {
                            loadingDialog.cancel()
                            inputDialog.cancel()
                            errorDialog(this, getString(R.string.error), it.message ?: "")
                        }, onFailure = { code, message ->
                            loadingDialog.cancel()
                            inputDialog.cancel()
                            errorDialog(this, getString(R.string.error) + " " + code, message)
                        }
                    )
                }
                .show()
        }
    }

    private fun getPathFromUri(uri: Uri): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val loader = CursorLoader(this, uri, proj, null,
            null, null)
        val cursor = loader.loadInBackground()
        return if (cursor != null) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } else uri.path ?: ""
    }

    private fun setOnLogoutBtnClickListener() {
        val infoDialog = InfoDialog(this)
        infoDialog.title = getString(R.string.restart_software)
        infoDialog.description = getString(R.string.dialog_restart_ensure)
        binding.btnLogout.setOnClickListener {
            infoDialog.show()
            infoDialog.withOnCancel {
                infoDialog.cancel()
            }
            infoDialog.withOnConfirm {
                lifecycleScope.launch {
                    HttpClient.clearCookies()
                    userViewModel.delete(userViewModel.getUser()!!)
                    withContext(Dispatchers.Main) {
                        infoDialog.cancel()
                        val packageManager: PackageManager = this@ProfileActivity.packageManager
                        val intent: Intent? = packageManager.getLaunchIntentForPackage(this@ProfileActivity.packageName)
                        val componentName: ComponentName? = intent?.component
                        val mainIntent: Intent = Intent.makeRestartActivityTask(componentName)
                        this@ProfileActivity.startActivity(mainIntent)
                        this@ProfileActivity.finishAffinity() // 关闭所有关联Activity
                        Runtime.getRuntime().exit(0) // 结束进程
                    }
                }
            }
        }
    }
}
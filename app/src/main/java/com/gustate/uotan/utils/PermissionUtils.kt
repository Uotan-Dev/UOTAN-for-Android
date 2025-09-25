package com.gustate.uotan.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {

    // 定义权限请求回调接口
    interface PermissionCallback {
        fun onPermissionGranted()
        fun onPermissionDenied()
    }

    // 检查权限（静态方法）
    fun checkPermission(
        context: Context,
        requestCode: Int,
        callback: PermissionCallback
    ) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(permission),
                requestCode
            )
        } else {
            // 已授权
            callback.onPermissionGranted()
        }
    }

    fun handlePermissionResult(
        requestCode: Int,
        grantResults: IntArray,
        targetRequestCode: Int,
        callback: PermissionCallback
    ) {
        if (requestCode == targetRequestCode && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callback.onPermissionGranted()
            } else {
                callback.onPermissionDenied()
            }
        }
    }
}
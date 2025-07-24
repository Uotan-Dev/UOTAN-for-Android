@file:Suppress("unused")

package com.gustate.uotan.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Environment
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.gustate.uotan.App
import com.gustate.uotan.R
import com.gustate.uotan.gustatex.dialog.InfoDialog
import com.haoge.easyandroid.easy.EasyImageGetter
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2.Priority
import com.tonyodev.fetch2.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

/*
 * 这是一个工具（Utils）类
 * 用于声明一些静态方法以供程序调用
 */

class Utils {

    companion object {
        const val REQUEST_CODE_PERMISSION = 100
        var baseUrl = "https://www.uotan.cn"
        const val USER_AGENT = "UotanAPP/1.0"
        const val TIMEOUT_MS = 300000

        var userTheme = R.style.Base_Theme_Uotan

        var Cookies = mapOf<String,String>()
        var isLogin = false

        private val Context.density: Float
            get() = resources.displayMetrics.density

        private val Context.fontScale: Float
            get() = resources.configuration.fontScale

        fun Float.dpToPx(context: Context): Float = this * context.density
        fun Float.spToPx(context: Context): Float = this * context.density * context.fontScale
        fun Float.pxToDp(context: Context): Float = this / context.density
        fun Float.pxToSp(context: Context): Float = this / (context.density * context.fontScale)

        fun openImmersion(window: Window) {
            if (isXiaomi()) {
                //设置沉浸式状态栏，在金凡的狗屎系统中，状态栏背景透明。原生系统中，状态栏背景半透明。
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            }
        }

        fun openUrlInBrowser(context: Context, url: String) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = url.toUri()
                // 确保在新任务栈打开
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }

        private fun isXiaomi(): Boolean {
            return checkSystemProperty("ro.miui.region") &&
                    checkSystemProperty("ro.miui.ui.version.code") &&
                    checkSystemProperty("ro.miui.ui.version.name")
        }

        @SuppressLint("PrivateApi")
        private fun checkSystemProperty(propName: String): Boolean {
            return try {
                val systemProperties = Class.forName("android.os.SystemProperties")
                val getMethod = systemProperties.getMethod("get", String::class.java)
                getMethod.invoke(systemProperties, propName)?.toString()?.isNotEmpty() == true
            } catch (e: Exception) {
                false
            }
        }

        //版本名
        fun getVersionName(context: Context) = getPackageInfo(context).versionName.toString()
        //版本名
        fun getVersionCode(context: Context) = getPackageInfo(context).longVersionCode.toString()

        private fun getPackageInfo(context: Context): PackageInfo {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, PackageManager.GET_CONFIGURATIONS)
            return  pi
        }

        fun idToAvatar(id: String): String {
            val urlPrefix = "$baseUrl/data/avatars/o/"
            val path = if (id.length >= 4) {
                id.dropLast(3)
            } else {
                "0"
            }
            val avatarUrl = "${urlPrefix}${path}/${id}.jpg"
            return avatarUrl
        }

        fun convertCookiesToString(cookies: Map<String, String>): String {
            return cookies.entries.joinToString("; ") { "${it.key}=${it.value}" }
        }

        fun htmlToSpan(textView: TextView, html: String) {
            val processedHtml = html
                .replace("<div[^>]*>".toRegex(), "")
                .replace("</div>", "<br>")
                .replace("&nbsp;", " ")
            EasyImageGetter
                .create()
                .setPlaceHolder(R.drawable.ic_uo)
                .setLoader { url ->
                    if (textView.width == 0) return@setLoader null
                    // 计算目标宽度（减去左右padding）
                    val targetWidth = textView.width - textView.paddingLeft - textView.paddingRight
                    if (targetWidth <= 0) return@setLoader null
                    // 使用Glide加载并调整尺寸
                    try {
                        Glide.with(textView.context)
                            .load(url)
                            .override(targetWidth, Target.SIZE_ORIGINAL)
                            .fitCenter()
                            .apply(RequestOptions.bitmapTransform(RoundedCorners(12f.dpToPx(textView.context).roundToInt())))
                            .submit()
                            .get()
                    } catch (e: Exception) {
                        null
                    }
                }
                .loadHtml(processedHtml, textView)
        }
        suspend fun saveToExternalPrivateDir(
            context: Context,
            urlString: String,
            dirType: String, // 子目录路径（如 "Documents/Images"）
            fileName: String // 可选自定义文件名
        ): File? = withContext(Dispatchers.IO) {
            try {
                // 0. 检查外部存储是否可用
                if (context.getExternalFilesDir(null) == null) {
                    return@withContext null
                }

                // 1. 创建自定义子目录
                val targetDir = File(context.getExternalFilesDir(null), dirType).apply {
                    if (!exists()) mkdirs() // 递归创建目录
                }

                // 3. 创建目标文件
                val outputFile = File(targetDir, fileName)

                // 4. 下载并保存文件
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = TIMEOUT_MS
                connection.connect()

                connection.inputStream.use { input ->
                    FileOutputStream(outputFile).use { output ->
                        input.copyTo(output)
                    }
                }
                outputFile
            } catch (e: Exception) {
                null
            }
        }


        data class FetchData(val fetch: Fetch, val request: Request)

        fun downloadFile(context: Context, url: String): FetchData {
            val app: App? = context.applicationContext as App?
            // 配置 Fetch
            val fetch = app?.getFetch()!!
            val path = File(
                Environment.getExternalStorageDirectory(),
                "Download/Uotan/${url.toUri().lastPathSegment}"
            ).absolutePath
            val request = Request(url, path).apply {
                priority = Priority.HIGH
                networkType = NetworkType.ALL
                tag = url
            }
            fetch.enqueue(request)
            return FetchData(fetch, request)
        }

        fun getThemeColor(context: Context, attr: Int): Int {
            val typedArray = context.obtainStyledAttributes(intArrayOf(attr))
            val color = typedArray.getColor(0, Color.TRANSPARENT)
            typedArray.recycle()
            return color
        }

        fun errorDialog(context: Context, title: String, message: String?) {
            val errorDialog = InfoDialog(context)
            errorDialog
                .setTitle(title)
                .setDescription(message ?: "Unknown error")
                .setCancelText(context.getString(R.string.cancel))
                .setConfirmText(context.getString(R.string.share))
                .withOnCancel { errorDialog.dismiss() }
                .withOnConfirm {
                    val share = Intent.createChooser(Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message ?: "Unknown error")
                    }, null)
                    context.startActivity(share)
                    errorDialog.dismiss()
                }
                .show()
        }
    }
}


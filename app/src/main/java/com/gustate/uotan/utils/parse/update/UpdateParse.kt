package com.gustate.uotan.utils.parse.update

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.gustate.uotan.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class UpdateLog(
    @SerializedName("type") val category: String,
    @SerializedName("content") val description: String
)

data class VersionInfo(
    @SerializedName("latest_version_name") val latestVersionName: String,
    @SerializedName("latest_version_code") val latestVersionCode: Int,
    @SerializedName("latest_version_download_url") val downloadUrl: String,
    @SerializedName("version_channel") val channel: String,
    @SerializedName("versions") val historyVersions: MutableList<Version>,
    @SerializedName("min_support_version") val minSupportedVersion: Int,
    @SerializedName("force_update_versions") val forceUpdateVersions: List<Int>
)

data class Version(
    @SerializedName("version_name") val versionName: String,
    @SerializedName("version_code") val code: Int,
    @SerializedName("update_date") val releaseDate: String,
    @SerializedName("update_log") val changelog: MutableList<UpdateLog>
)

data class NewVersion(
    val newVersionName: String,
    val newVersionCode: Int,
    val newVersionLog: MutableList<UpdateLog>,
    val newVersionLink: String,
    val updateDate: String
)

class UpdateParse {
    companion object {
        private val gson = Gson() // 复用 Gson 实例

        suspend fun isVerLow(context: Context): Boolean = withContext(Dispatchers.IO) {
            val currentVerCode = Utils.getVersionCode(context)
            val latestVerCode = fetchUpdateLog().latestVersionCode
            return@withContext currentVerCode.toInt() < latestVerCode
        }

        suspend fun fetchNewVersion(): NewVersion = withContext(Dispatchers.IO) {
            val latestVerInfo = fetchUpdateLog()
            val latestVerName = latestVerInfo.latestVersionName
            val latestVerCode = latestVerInfo.latestVersionCode
            val latestVerLog = latestVerInfo.historyVersions[0].changelog
            val latestVerLink = latestVerInfo.downloadUrl
            val latestVerData = latestVerInfo.historyVersions[0].releaseDate
            return@withContext NewVersion(
                latestVerName,
                latestVerCode,
                latestVerLog,
                latestVerLink,
                latestVerData
            )
        }

        suspend fun fetchUpdateLog(): VersionInfo = withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://gitee.com/XiaoMeng22333/uotan-for-android/raw/master/update.json")
                    .build()

                suspendCancellableCoroutine { continuation ->
                    client.newCall(request).enqueue(object : Callback {
                        override fun onResponse(call: Call, response: Response) {
                            response.use {
                                if (!it.isSuccessful) {
                                    continuation.resumeWithException(IOException("HTTP ${it.code}"))
                                    return
                                }

                                try {
                                    val body = it.body ?: throw IOException("Empty response")
                                    val json = body.string()
                                    val versionInfo: VersionInfo = gson.fromJson(json, object : TypeToken<VersionInfo>() {}.type)
                                    continuation.resume(versionInfo)
                                } catch (e: Exception) {
                                    continuation.resumeWithException(e)
                                }
                            }
                        }

                        override fun onFailure(call: Call, e: IOException) {
                            continuation.resumeWithException(e)
                        }
                    })
                }
            } catch (e: Exception) {
                throw IOException("Network request failed", e)
            }
        }
    }
}
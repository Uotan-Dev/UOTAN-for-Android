package com.gustate.uotan.utils.parse.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup

@Serializable
data class VersionResponse(
    @SerialName("latest_version_name")
    val latestVersionName: String,
    @SerialName("latest_version_code")
    val latestVersionCode: Int,
    @SerialName("version_channel")
    val versionChannel: String,
    @SerialName("versions")
    val versions: MutableList<VersionInfo>,
    @SerialName("min_support_version")
    val minSupportVersion: Int,
    @SerialName("force_update_versions")
    val forceUpdateVersions: MutableList<Int>
)

@Serializable
data class VersionInfo(
    @SerialName("version_name") val versionName: String? = null,
    @SerialName("version") val versionAlt: String? = null,  // 处理字段名不一致
    @SerialName("version_code") val versionCode: Int? = null,
    @SerialName("code") val codeAlt: Int? = null,  // 备用字段
    @SerialName("update_date") val updateDate: String,
    @SerialName("update_log") val updateLog: List<UpdateLog>,
    @SerialName("download_url") val downloadUrl: String? = null
) {
    // 合并不同字段名获取最终值
    val finalVersionName: String get() = versionName ?: versionAlt ?: ""
    val finalVersionCode: Int get() = versionCode ?: codeAlt ?: 0
}

@Serializable
data class UpdateLog(
    val type: String,
    val content: String
)

class UpdateParse {
    // 3. Jsoup网络请求 + JSON解析
    suspend fun fetchVersionInfo(url: String): VersionResponse? = withContext(Dispatchers.IO) {
        try {
            // 使用Jsoup获取原始JSON
            val response = Jsoup.connect(url)
                .ignoreContentType(true)  // 关键参数：允许非HTML内容
                .timeout(15000)
                .execute()

            // 使用Kotlinx序列化解析JSON
            Json.decodeFromString<VersionResponse>(response.body())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun main() {
    try {
        // 使用Jsoup获取原始JSON
        val response = Jsoup
            .connect("https://raw.githubusercontent.com/Uotan-Dev/UOTAN-for-Android/refs/heads/main/update.json")
            .ignoreContentType(true)  // 关键参数：允许非 HTML 内容
            .timeout(15000)
            .get()

        // 使用Kotlinx序列化解析JSON
        Json.decodeFromString<VersionResponse>(response.allElements.first().text().toString())
        println(Json.decodeFromString<VersionResponse>(response.allElements.first().text().toString()))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
package com.uotan.forum.settings.data.profile

import com.uotan.forum.utils.Utils.USER_AGENT
import com.uotan.forum.utils.Utils.baseUrl
import com.uotan.forum.utils.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.File

class ProfileParse {

    // 实例化 OkHttpClient
    private val okHttpClient = HttpClient.getClient()
    private val xfRequestUri = "/account/account-details"

    suspend fun changeAvatar(
        avatarFile: File,
        onSuccess: (xfToken: String) -> Unit,
        onFailure: ((code: Int, body: String) -> Unit),
        onThrowable: (Throwable) -> Unit
    )  = withContext(Dispatchers.IO) {
        try {
            uploadAvatar(
                avatarFile,
                onSuccess = {
                    onSuccess(it)
                    return@uploadAvatar
                },
                onFailure = { code, body ->
                    onFailure(code, body)
                    return@uploadAvatar
                },
                onThrowable = {
                    onThrowable(it)
                    return@uploadAvatar
                }
            )
        } catch (throwable: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(throwable)
            }
        }
    }

    suspend fun uploadAvatar(
        avatarFile: File,
        onSuccess: (xfToken: String) -> Unit,
        onFailure: ((code: Int, body: String) -> Unit),
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        val url = "$baseUrl/account/avatar"
        try {
            val xfToken = parseHiddenXfToken(url)
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("avatar_crop_x", "0")
                .addFormDataPart("avatar_crop_y", "0")
                .addFormDataPart("use_custom","1")
                .addFormDataPart("upload", avatarFile.name,
                    avatarFile.asRequestBody(
                        "application/octet-stream".toMediaTypeOrNull()))
                .addFormDataPart("_xfRequestUri", xfRequestUri)
                .addFormDataPart("_xfWithData", "1")
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("_xfResponseType", "json")
                .build()
            // 创建 Request
            val request = Request.Builder()
                .url(url)
                .addHeader(
                    "Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Origin", baseUrl)
                .addHeader("Referer", baseUrl + xfRequestUri)
                .post(requestBody)
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                val responseBody = response.body.string()
                withContext(Dispatchers.Main) {
                    val json = JSONObject(responseBody)
                    withContext(Dispatchers.Main) {
                        val status = json.optString("status", "")
                        if (!response.isSuccessful) {
                            val errorContent = json.getJSONArray("errors")[0]
                            onFailure(response.code, errorContent.toString())
                            return@withContext
                        }
                        if (status == "error") {
                            val errorContent = json.getJSONArray("errors")[0]
                            onFailure(response.code, errorContent.toString())
                            return@withContext
                        }
                        onSuccess(xfToken)
                    }
                }
            }
        } catch (throwable: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(throwable)
            }
        }
    }

    suspend fun applyAvatar(
        xfToken: String,
        onSuccess: () -> Unit,
        onFailure: ((code: Int, body: String) -> Unit),
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        val url = "$baseUrl/account/avatar"
        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("avatar_crop_x", "0")
                .addFormDataPart("avatar_crop_y", "0")
                .addFormDataPart("use_custom","1")
                .addFormDataPart("_xfRequestUri", xfRequestUri)
                .addFormDataPart("_xfWithData", "1")
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("_xfResponseType", "json")
                .build()
            // 创建 Request
            val request = Request.Builder()
                .url(url)
                .addHeader(
                    "Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Origin", baseUrl)
                .addHeader("Referer", baseUrl + xfRequestUri)
                .post(requestBody)
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                val responseBody = response.body.string()
                val json = JSONObject(responseBody)
                withContext(Dispatchers.Main) {
                    val status = json.optString("status", "")
                    if (!response.isSuccessful) {
                        val errorContent = json.getJSONArray("errors")[0]
                        onFailure(response.code, errorContent.toString())
                        return@withContext
                    }
                    if (status == "error") {
                        val errorContent = json.getJSONArray("errors")[0]
                        onFailure(response.code, errorContent.toString())
                        return@withContext
                    }
                    onSuccess()
                }
            }
        } catch (throwable: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(throwable)
            }
        }
    }

    suspend fun changeUserName(
        newUserName: String,
        changeReason: String,
        onSuccess: () -> Unit,
        onFailure: ((code: Int, body: String) -> Unit),
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        val url = "$baseUrl/account/username"
        try {
            val xfToken = parseHiddenXfToken(url)
            // 创建 MultipartBody
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("username", newUserName)
                .addFormDataPart("change_reason", changeReason)
                .addFormDataPart("_xfRedirect", baseUrl + xfRequestUri)
                .addFormDataPart("_xfRequestUri",xfRequestUri)
                .addFormDataPart("_xfWithData", "1")
                .addFormDataPart("_xfToken", xfToken)
                .addFormDataPart("_xfResponseType", "json")
                .build()
            // 创建 Request
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Origin", baseUrl)
                .addHeader("Referer", baseUrl + xfRequestUri)
                .post(requestBody)
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                val responseBody = response.body.string()
                withContext(Dispatchers.Main) {
                    val json = JSONObject(responseBody)
                    val status = json.optString("status", "")
                    if (!response.isSuccessful) {
                        val errorContent = json.getJSONArray("errors")[0]
                        onFailure(response.code, errorContent.toString())
                        return@withContext
                    }
                    if (status == "error") {
                        val errorContent = json.getJSONObject("errors").getString("new_username")
                        onFailure(response.code, errorContent.toString())
                        return@withContext
                    }
                    onSuccess()
                }
            }
        } catch (throwable: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(throwable)
            }
        }
    }

    suspend fun parseHiddenXfToken(url: String): String = withContext(Dispatchers.IO) {
        val client = HttpClient.getClient()
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()
        val response = client.newCall(request).execute()
        val document = Jsoup.parse(response.body.string())
        val xfToken = requireNotNull(
            document.select("input[name=_xfToken]").first()?.attr("value")
        ) { "xfToken not found" }
        return@withContext xfToken
    }
}
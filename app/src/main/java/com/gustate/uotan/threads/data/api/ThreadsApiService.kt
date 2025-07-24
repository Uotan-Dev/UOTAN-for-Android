package com.gustate.uotan.threads.data.api

import android.util.Log
import com.google.gson.Gson
import com.gustate.uotan.BuildConfig
import com.gustate.uotan.threads.data.model.Post
import com.gustate.uotan.threads.data.model.PostList
import com.gustate.uotan.threads.data.model.Thread
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import com.gustate.uotan.utils.Utils.Companion.baseUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class ThreadsApiService {

    private val okHttpClient = OkHttpClient()

    suspend fun getThread(
        posts: String,
        onSuccess: (Thread) -> Unit,
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .build()
            val request = Request.Builder()
                .url("$baseUrl/api$posts")
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("XF-Api-Key", BuildConfig.xfApiKey)
                .addHeader("XF-Api-User", Cookies["xf_user"] ?: "")
                .build()
            val allJson = JSONObject(client.newCall(request).execute().body?.string()?:"")
            val threadJson = allJson.getString("thread")
            val thread = Gson().fromJson(threadJson, Thread::class.java)
            Log.e("thread", thread.toString())
            withContext(Dispatchers.Main) {
                onSuccess(thread)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onException(e)
            }
        }
    }

    suspend fun getThreadsAllPosts(
        posts: String, page: Int,
        onSuccess: (PostList) -> Unit,
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .build()
            val request = Request.Builder()
                .url("$baseUrl/api$posts" + "posts?page=$page")
                .addHeader("User-Agent", "UotanApp/1.0")
                .addHeader("XF-Api-Key", BuildConfig.xfApiKey)
                .addHeader("XF-Api-User", Cookies["xf_user"] ?: "")
                .build()
            val json = client.newCall(request).execute().body?.string()
            val threadsAndPosts = Gson().fromJson(json, PostList::class.java)
            withContext(Dispatchers.Main) {
                onSuccess(threadsAndPosts)
            }
        } catch (e: Exception) {
           withContext(Dispatchers.Main) {
                onException(e)
            }
        }
    }

    /**
     * 在 xenforo 框架的论坛中对文章进行 react (反应)
     * POST $baseUrl/api/posts/{id}/react
     * 在 xenforo 中默认有许多反应，正面的、反面的以及中立的
     * 在 uotan.cn 中仅支持正向反馈 其应该传递的 reaction_id 为 1
     * 反应的响应内容有两个参数：
     * 其一 为 success 类型为 Boolean
     * 其二 为 action 类型为 String
     * 其中 action 的内容为 "insert" 或 "delete" 的根据是反应被添加还是被删除
     */
    suspend fun reactPosts(
        postsId: String,
        onSuccess: ((action: String) -> Unit),
        onFailure: ((code: Int, body: String) -> Unit),
        onThrowable: ((Throwable) -> Unit)
    ) = withContext(Dispatchers.IO) {
        try {
            // 创建 MultipartBody
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                // (必填) 要使用的反应的 id
                .addFormDataPart("reaction_id", "1")
                .build()
            // 创建 Request
            val request = Request.Builder()
                .url("$baseUrl/api/posts/$postsId/react")
                .addHeader("XF-Api-Key", BuildConfig.xfApiKey)
                .addHeader("XF-Api-User", Cookies["xf_user"] ?:"")
                .post(requestBody)
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                if (!response.isSuccessful) {
                    onFailure(response.code, response.body?.string() ?: "")
                    return@withContext
                }
                responseBody?.let {
                    val json = JSONObject(it)
                    when {
                        json.getBoolean("success") == true -> {
                            // 操作成功
                            val action = json.getString("action")
                            withContext(Dispatchers.Main) {
                                onSuccess(action)
                            }
                        }
                        else -> {
                            // API返回业务错误
                            withContext(Dispatchers.Main) {
                                onFailure(response.code, response.body?.string() ?:"")
                            }
                        }
                    }
                }
            }
        } catch (throwable: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(throwable)
            }
        }
    }

    /**
     * 在 xenforo 框架的论坛中对文章进行 reply (回复：post)(仅文本)
     * POST $baseUrl/api/posts/
     * 表单需要传递的内容有两个参数：
     * 其一 为 thread_id 类型为 String 是要回复的主题的 ID ！！必填
     * 其二 为 message 类型为 String 是回复内容的 HTML 内容（不确定可否使用 bbcode） ！！必填
     * 其三 为 attachment_key 类型为 String 用于上传文件的 API attachment 键。
     *     注意：附件键上下文类型必须为 post，并将 context[thread_id] 设置为此线程 ID。
     * 其中 action 的内容为 "insert" 或 "delete" 的根据是反应被添加还是被删除
     * 反应的响应内容有两个参数：
     * 其一 为 success 类型为 Boolean
     * 其二 为 action 类型为 Post
     * @see com.gustate.uotan.threads.data.model.Post
     */
    suspend fun replyThreads(
        threadsId: String,
        message: String,
        onSuccess: ((newPost: Post) -> Unit),
        onFailure: ((code: Int, body: String) -> Unit),
        onThrowable: ((Throwable) -> Unit)
    ) = withContext(Dispatchers.IO) {
        try {
            // 创建 MultipartBody
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("thread_id", threadsId)
                .addFormDataPart("message", message)
                //.addFormDataPart("attachment_key", "")
                .build()
            // 创建 Request
            val request = Request.Builder()
                .url("$baseUrl/api/posts")
                .addHeader("XF-Api-Key", BuildConfig.xfApiKey)
                .addHeader("XF-Api-User", Cookies["xf_user"] ?:"")
                .post(requestBody)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                if (!response.isSuccessful) {
                    onFailure(response.code, response.body?.string() ?: "")
                    return@withContext
                }
                responseBody?.let {
                    val postJson = JSONObject(it).getString("post")
                    val newPost = Gson().fromJson(postJson, Post::class.java)
                    // API返回业务
                    withContext(Dispatchers.Main) {
                        onSuccess(newPost)
                    }
                }
            }
        } catch (throwable: Throwable) {
            withContext(Dispatchers.Main) {
                onThrowable(throwable)
            }
        }
    }

}
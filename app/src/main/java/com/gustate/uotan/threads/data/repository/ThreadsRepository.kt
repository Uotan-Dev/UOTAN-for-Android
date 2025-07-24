package com.gustate.uotan.threads.data.repository

import com.gustate.uotan.threads.data.api.ThreadsApiService
import com.gustate.uotan.threads.data.model.NoApiPostInfo
import com.gustate.uotan.threads.data.model.Post
import com.gustate.uotan.threads.data.model.PostList
import com.gustate.uotan.threads.data.model.Thread
import com.gustate.uotan.threads.data.parse.ThreadsParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ThreadsRepository {

    private val tas = ThreadsApiService()
    private val tp = ThreadsParse()


    suspend fun getThread(
        thread: String,
        onSuccess: (Thread) -> Unit,
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        tas.getThread(thread, onSuccess, onException)
    }

    suspend fun getThreadsAllPosts(
        posts: String, page: Int,
        onSuccess: (PostList) -> Unit,
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        tas.getThreadsAllPosts(
            posts = posts,
            page = page,
            onSuccess = {
                onSuccess(it)
            },
            onException = {
                onException(it)
            }
        )
    }

    suspend fun getOtherThreadsInfo(
        threads: String,
        onSuccess: (NoApiPostInfo) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) {
        tp.parseThreadsInfo(
            threads = threads,
            onSuccess = {
                onSuccess(it)
            },
            onThrowable = {
                onThrowable(it)
            }
        )
    }

    suspend fun reactPosts(
        postsId: String,
        onSuccess: ((isReact: Boolean) -> Unit),
        onFailure: ((code: Int, body: String) -> Unit),
        onThrowable: ((Throwable) -> Unit)
    ) = withContext(Dispatchers.IO) {
        tas.reactPosts(
            postsId,
            { onSuccess(it == "insert") },
            onFailure,
            onThrowable
        )
    }

    suspend fun addPostOrReplyThreads(
        threadsId: Long,
        message: String,
        onSuccess: ((newPost: Post) -> Unit),
        onFailure: ((code: Int, body: String) -> Unit),
        onThrowable: ((Throwable) -> Unit)
    ) = withContext(Dispatchers.IO) {
        tas.replyThreads(
            threadsId = threadsId.toString(),
            message = message,
            onSuccess = {
                onSuccess(it)
            },
            onFailure = { code, body ->
                onFailure(code, body)
            },
            onThrowable = {
                onThrowable(it)
            }
        )
    }

    suspend fun getIp (
        url: String,
        onSuccess: (String) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        tp.parseIp(url, onSuccess, onThrowable)
    }
}
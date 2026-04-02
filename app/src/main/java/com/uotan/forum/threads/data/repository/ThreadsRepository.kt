package com.uotan.forum.threads.data.repository

import com.uotan.forum.threads.data.api.ThreadsApiService
import com.uotan.forum.threads.data.model.NoApiPostInfo
import com.uotan.forum.threads.data.model.Post
import com.uotan.forum.threads.data.model.PostList
import com.uotan.forum.threads.data.model.post.PostResponse
import com.uotan.forum.threads.data.parse.ThreadsParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ThreadsRepository {

    private val tas = ThreadsApiService()
    private val tp = ThreadsParse()

    suspend fun getThreadsAllPosts(
        posts: String, page: Int,
        onSuccess: (PostList) -> Unit,
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        tas.getThreadsAllPosts(
            threadId = posts,
            page = page,
            onSuccess,
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

    suspend fun getPostAllReply(
        postList: List<Post>,
        onSuccess: (MutableList<PostResponse>) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        tas.getPostAllReply(postList, onSuccess, onThrowable)
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
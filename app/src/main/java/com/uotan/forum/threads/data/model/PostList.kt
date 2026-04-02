package com.uotan.forum.threads.data.model

import com.uotan.forum.utils.data.model.Pagination

data class PostList (
    val posts: List<Post>,
    val pagination: Pagination
)

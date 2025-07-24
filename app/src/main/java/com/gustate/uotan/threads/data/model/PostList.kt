package com.gustate.uotan.threads.data.model

import com.gustate.uotan.utils.data.model.Pagination

data class PostList (
    val posts: List<Post>,
    val pagination: Pagination
)

package com.uotan.forum.utils.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResp(
    val status: String,
    val errors: List<String>?,
    val errorHtml: ErrorHtml?
)

@Serializable
data class ErrorHtml(
    val content: String,
    val title: String
)
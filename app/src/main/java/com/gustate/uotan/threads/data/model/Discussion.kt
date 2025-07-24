package com.gustate.uotan.threads.data.model


import com.google.gson.annotations.SerializedName

data class Discussion(
    @SerializedName("allow_answer_downvote")
    val allowAnswerDownvote: Boolean,
    @SerializedName("allow_answer_voting")
    val allowAnswerVoting: Boolean,
    @SerializedName("allowed_thread_types")
    val allowedThreadTypes: List<String>
)
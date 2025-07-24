package com.gustate.uotan.utils.data.model

import com.google.gson.annotations.SerializedName

data class Attachment (
    @SerializedName("attach_date")
    val attachDate: Long,
    @SerializedName("attachment_id")
    val attachmentID: Long,
    @SerializedName("content_id")
    val contentID: Long,
    @SerializedName("content_type")
    val contentType: String,
    @SerializedName("direct_url")
    val directURL: String,
    @SerializedName("file_size")
    val fileSize: Long,
    val filename: String,
    val height: Long,
    @SerializedName("is_audio")
    val isAudio: Boolean,
    @SerializedName("is_video")
    val isVideo: Boolean,
    @SerializedName("thumbnail_url")
    val thumbnailURL: String,
    @SerializedName("view_count")
    val viewCount: Long,
    val width: Long
)
package com.uotan.forum.utils

import com.bumptech.glide.request.RequestOptions
import com.uotan.forum.R

object Helpers {
    val avatarOptions = RequestOptions()
        .placeholder(R.drawable.avatar_account)
        .error(R.drawable.avatar_account)
        .centerCrop()
        .circleCrop()
}
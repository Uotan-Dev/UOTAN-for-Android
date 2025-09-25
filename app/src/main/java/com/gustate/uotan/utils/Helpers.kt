package com.gustate.uotan.utils

import com.bumptech.glide.request.RequestOptions
import com.gustate.uotan.R

object Helpers {
    val avatarOptions = RequestOptions()
        .placeholder(R.drawable.avatar_account)
        .error(R.drawable.avatar_account)
        .centerCrop()
        .circleCrop()
}
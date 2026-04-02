package com.uotan.forum.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.uotan.forum.R

enum class Navigate(
    @field:StringRes
    val label: Int,
    @field:DrawableRes
    val icon: Int
) {
    HOME(
        label = R.string.home,
        icon = R.drawable.ic_nav_home_selected
    ),
    SECTION(
        label = R.string.section,
        icon = R.drawable.ic_nav_plate_selected
    ),
    MESSAGE(
        label = R.string.message,
        icon = R.drawable.ic_nav_notice_selected
    ),
    RESOURCE(
        label = R.string.resource,
        icon = R.drawable.ic_nav_res_selected
    ),
    ME(
        label = R.string.me,
        icon = R.drawable.ic_nav_me_selected
    )
}
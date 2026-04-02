package com.uotan.forum.welcome.ui.model

import com.uotan.forum.utils.Utils.baseUrl

sealed class LoginSealed(val url: String) {
    object Xiaomi: LoginSealed("$baseUrl/register/connected-accounts/xiaomi/?setup=1")
    object QQ: LoginSealed("$baseUrl/register/connected-accounts/gp_cap_qq/?setup=1")
    object Weibo: LoginSealed("$baseUrl/register/connected-accounts/gp_cap_weibo/?setup=1")
}
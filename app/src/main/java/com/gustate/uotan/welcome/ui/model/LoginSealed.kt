package com.gustate.uotan.welcome.ui.model

import com.gustate.uotan.utils.Utils.baseUrl

sealed class LoginSealed(val url: String) {
    object Xiaomi: LoginSealed("$baseUrl/register/connected-accounts/xiaomi/?setup=1")
    object QQ: LoginSealed("$baseUrl/register/connected-accounts/gp_cap_qq/?setup=1")
    object Weibo: LoginSealed("$baseUrl/register/connected-accounts/gp_cap_weibo/?setup=1")
}
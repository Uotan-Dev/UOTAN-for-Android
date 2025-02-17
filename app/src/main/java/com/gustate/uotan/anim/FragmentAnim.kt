package com.gustate.uotan.anim

import com.gustate.uotan.R

data class TransitionAnimConfig(
    val enterAnim: Int = R.anim.slide_in_right,      // 新Fragment进入
    val exitAnim: Int = R.anim.slide_out_left,       // 旧Fragment退出
    val popEnterAnim: Int = R.anim.slide_in_left,    // 返回栈时旧Fragment重新进入
    val popExitAnim: Int = R.anim.slide_out_right    // 返回栈时当前Fragment退出
)

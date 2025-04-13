package com.gustate.uotan.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.anim.TransitionAnimConfig
import com.gustate.uotan.fragment.login.FirstFragment
import com.gustate.uotan.utils.Utils.Companion.openImmersion

/**
 * 登录页面 (Activity)
 * JiaGuZhuangZhi Miles
 * Gustate 02/23/2025
 * I Love Jiang’Xun
 */

class LoginActivity : BaseActivity(), FragmentNavigation {

    /**
     * 加载视图后
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 启用边到边设计
        enableEdgeToEdge()
        // 链接布局
        setContentView(R.layout.activity_login)
        // 针对部分系统的系统栏沉浸
        openImmersion(window)
        // 启动 Fragment
        supportFragmentManager.commit {
            replace(R.id.main, FirstFragment())
            setReorderingAllowed(true)
        }
    }

    /**
     * 重写 Fragment 切换接口
     */
    override fun switchFragment(
        targetFragment: Fragment,
        addToBackStack: Boolean,
        data: Bundle?,
        animConfig: TransitionAnimConfig?
    ) {
        supportFragmentManager.commit {
            // 设置动画
            animConfig?.let { config ->
                setCustomAnimations(
                    config.enterAnim,
                    config.exitAnim,
                    config.popEnterAnim,
                    config.popExitAnim
                )
            }
            // 切换 Fragment
            replace(R.id.main, targetFragment)
            // 加入返回栈
            if (addToBackStack) addToBackStack(null)
            // 优化事务
            setReorderingAllowed(true)
        }
    }
}
// 导航接口
interface FragmentNavigation {
    fun switchFragment(
        targetFragment: Fragment,
        addToBackStack: Boolean = true,
        data: Bundle? = null,
        animConfig: TransitionAnimConfig? = TransitionAnimConfig()
    )
}


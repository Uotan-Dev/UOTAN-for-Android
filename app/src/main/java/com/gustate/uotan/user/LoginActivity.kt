package com.gustate.uotan.user

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.gustate.uotan.R
import com.gustate.uotan.anim.TransitionAnimConfig
import com.gustate.uotan.utils.Utils.Companion.openImmersion

class LoginActivity : AppCompatActivity(), FirstFragment.FragmentSwitchListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        openImmersion(window)
        supportFragmentManager.commit {
            replace(R.id.main, FirstFragment()) // 替换 Fragment
            addToBackStack("tag") // 可选：加入返回栈
            setReorderingAllowed(true) // 优化事务执行
        }
    }

    override fun switchToFragment(targetFragment: Fragment, addToBackStack: Boolean, animConfig: TransitionAnimConfig?) {
        supportFragmentManager.commit {
            // 设置动画（如果配置不为空）
            animConfig?.let { config ->
                setCustomAnimations(
                    config.enterAnim,
                    config.exitAnim,
                    config.popEnterAnim,
                    config.popExitAnim
                )
            }
            replace(R.id.main, targetFragment)
            if (addToBackStack) addToBackStack(null) // 加入返回栈
            setReorderingAllowed(true) // 优化事务
        }
    }

}

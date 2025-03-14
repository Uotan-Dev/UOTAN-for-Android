package com.gustate.uotan.fragment.login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.gustate.uotan.activity.MainActivity
import com.gustate.uotan.R
import com.gustate.uotan.activity.LoginActivity

/**
 * 欢迎页面 (Fragment)
 * JiaGuZhuangZhi Miles
 * Gustate 02/23/2025
 * I Love Jiang’Xun
 */

class FirstFragment : Fragment() {

    /**
     * 加载视图时
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 加载布局
        return inflater.inflate(
            R.layout.fragment_first,
            container,
            false
        )
    }

    /**
     * 视图加载完成时
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** 从布局中取出需要调用的视图 **/
        // 系统通知栏占位布局
        val statusBarView = view.findViewById<View>(R.id.statusBarView)
        // 系统导航栏占位布局
        val gestureView = view.findViewById<View>(R.id.gestureView)
        // 返回按钮
        val back = view.findViewById<View>(R.id.back)
        // 注册按钮
        val register = view.findViewById<View>(R.id.registerCard)
        // 登录按钮
        val login = view.findViewById<View>(R.id.loginCard)
        // 先看看按钮
        val lookFirst = view.findViewById<View>(R.id.noLogin)

        /** 获取系统栏高度并同步到占位布局 **/
        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            // 获取系统栏高度 (包含 top, bottom, left 和 right)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 同步通知栏占位布局高度
            statusBarView
                .updateLayoutParams<ViewGroup.LayoutParams> {
                    height = systemBars.top
                }
            // 同步导航栏占位布局高度
            gestureView
                .updateLayoutParams<ViewGroup.LayoutParams> {
                    height = systemBars.bottom
                }
            // 返回 insets
            insets
        }

        /** 设置监听 **/
        // 为返回按钮设置监听
        back.setOnClickListener {
            // 结束当前 Activity
            activity?.finish()
        }
        // 为注册按钮添加点击监听
        register.setOnClickListener {
            // 开发中...
            Toast.makeText(context, R.string.developing, Toast.LENGTH_SHORT).show()
        }
        // 为登录按钮添加点击监听
        login.setOnClickListener {
            // 切换到登录页面 (Fragment)
            (activity as? LoginActivity)?.switchFragment(LoginFragment())
        }
        // 为先看看按钮添加监听
        lookFirst.setOnClickListener {
            // 启动 MainActivity
            startActivity(Intent(context, MainActivity::class.java))
            // 结束当前 Activity
            activity?.finish()
        }
    }
}
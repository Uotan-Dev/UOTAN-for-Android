package com.gustate.uotan.fragment.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.gustate.uotan.R
import com.gustate.uotan.databinding.FragmentFirstBinding
import com.gustate.uotan.ui.activity.LoginActivity
import com.gustate.uotan.main.ui.MainActivity

/**
 * 欢迎页面 (Fragment)
 * JiaGuZhuangZhi Miles
 * Gustate 02/23/2025
 */

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    /**
     * 加载视图时
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(layoutInflater, container, false)
        // 加载布局
        return binding.root
    }

    /**
     * 视图加载完成时
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** 获取系统栏高度并同步到占位布局 **/
        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /** 设置监听 **/
        // 为返回按钮设置监听
        binding.btnBack.setOnClickListener {
            // 结束当前 Activity
            activity?.finish()
        }
        // 为注册按钮添加点击监听
        binding.btnRegister.setOnClickListener {
            // 开发中...
            Toast.makeText(context, R.string.developing, Toast.LENGTH_SHORT).show()
        }
        // 为登录按钮添加点击监听
        binding.btnLogin.setOnClickListener {
            Toast.makeText(context, R.string.developing, Toast.LENGTH_SHORT).show()
        }
        // 为先看看按钮添加监听
        binding.tvLookFirst.setOnClickListener {
            // 启动 MainActivity
            startActivity(Intent(context, MainActivity::class.java))
            // 结束当前 Activity
            activity?.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
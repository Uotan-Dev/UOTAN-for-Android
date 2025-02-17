package com.gustate.uotan.article

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gustate.uotan.databinding.ActivityArticleBinding
import com.gustate.uotan.utils.Utils

class ArticleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val binding = ActivityArticleBinding.inflate(layoutInflater)
        setContentView(binding.rootLayout)

        /*
         * 修改各个占位布局的高度
         * 以实现小白条与状态栏的沉浸
         */
        // 使用 ViewCompat 类设置一个窗口监听器（这是一个回调函数），需要传入当前页面的根布局
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLayout) { _, insets ->
            // systemBars 是一个 insets 对象
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 设置状态栏占位布局高度
            binding.statusBarView.layoutParams.height = systemBars.top
            // 修改滚动布局的边距
            binding.rootScrollView.setPadding(0,systemBars.top + Utils.dp2Px(44, this).toInt(), 0, systemBars.bottom + Utils.dp2Px(52, this).toInt())
            // 设置小白条占位布局高度
            binding.gestureView.layoutParams.height = systemBars.bottom
            // 返回 insets
            insets
        }

        Toast.makeText(this, Build.USER, Toast.LENGTH_SHORT).show()
    }
}
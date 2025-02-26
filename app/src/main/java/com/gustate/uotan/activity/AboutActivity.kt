package com.gustate.uotan.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gustate.uotan.anim.TitleAnim
import com.gustate.uotan.databinding.ActivityAboutBinding
import com.gustate.uotan.utils.Utils
import com.gustate.uotan.utils.Utils.Companion.getVersionCode
import com.gustate.uotan.utils.Utils.Companion.getVersionName
import com.gustate.uotan.utils.Utils.Companion.openImmersion

/**
 * 关于页面 (Activity)
 * JiaGuZhuangZhi Miles
 * Gustate 02/23/2025
 * I Love Jiang’Xun
 */

class AboutActivity : AppCompatActivity() {

    /** 全类变量 **/
    // 初始化视图绑定
    private lateinit var binding: ActivityAboutBinding

    /**
     * 视图被创建
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** 窗体设置 **/
        // 启用边到边设计
        enableEdgeToEdge()
        // 针对部分系统的系统栏沉浸
        openImmersion(window)
        // 实例化 binding
        binding = ActivityAboutBinding.inflate(layoutInflater)
        // 绑定视图
        setContentView(binding.main)

        /** 获取系统栏高度并同步到占位布局 **/
        // 使用 ViewCompat 的回调函数
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            // 获取系统栏高度 (包含 top, bottom, left 和 right)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 设置状态栏占位布局高度
            binding.statusBarView.layoutParams.height = systemBars.top
            // 设置滚动布局内的根布局的边距
            binding.refreshLayout.setPadding(
                systemBars.left,
                Utils.dp2Px(60, this).toInt() + systemBars.top,
                systemBars.right,
                Utils.dp2Px(10, this).toInt() + systemBars.bottom
            )
            // 创建 TitleAnim 实例
            TitleAnim(
                binding.title,
                binding.bigTitle,
                Utils.dp2Px(60, this) + systemBars.top.toFloat(),
                systemBars.top.toFloat()
            )
            // 返回 insets
            insets
        }

        binding.appInfoVerName.text = getVersionName(this)
        binding.appInfoVerCode.text = getVersionCode(this)

        /** 设置监听 **/
        openUrl(binding.coreteam1, "https://www.uotan.cn/members/2/")
        openUrl(binding.coreteam2, "https://www.uotan.cn/members/47/")
        openUrl(binding.coreteam3, "https://www.uotan.cn/members/256/")
        openUrl(binding.coreteam4, "https://www.uotan.cn/members/yuzh.2414/")
        openUrl(binding.coreteam5, "https://www.uotan.cn/members/lemo.1042/")
        openUrl(binding.coreteam6, "https://www.uotan.cn/members/779/")
        openUrl(binding.coreteam7, "https://www.uotan.cn/members/zach.1219/")
        openUrl(binding.coreteam8, "https://www.uotan.cn/members/haoyang.2377/")
        openUrl(binding.specialthanks1, "https://www.uotan.cn/pages/about/")
        openUrl(binding.specialthanks2, "https://www.uotan.cn/members/3059/")
        openUrl(binding.openSourceOption, "https://github.com/Uotan-Dev/UOTAN-for-Android/")

        binding.back.setOnClickListener{
            finish()
        }
    }

    private fun openUrl(view: View, url: String) {
        view.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }

}
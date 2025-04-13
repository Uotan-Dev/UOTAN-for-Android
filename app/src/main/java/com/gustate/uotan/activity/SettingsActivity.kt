package com.gustate.uotan.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gustate.uotan.AgreementPolicyActivity
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.ThemeActivity
import com.gustate.uotan.anim.TitleAnim
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.openImmersion
import com.gustate.uotan.utils.Utils.Companion.openUrlInBrowser
import kotlin.math.roundToInt

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        // 针对部分系统的小白条沉浸
        openImmersion(window)

        // 链接首页窗口布局文件
        setContentView(R.layout.activity_settings)

        /*
         * 修改状态栏和底栏占位布局的高度
         */
        // 取当前页面状态栏占位布局
        val statusBarView: View = findViewById(R.id.statusBarView)
        // 取当前页面相对根布局
        val rootView: View = findViewById(R.id.rootView)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarView.layoutParams.height = systemBars.top
            val topPadding = (systemBars.top + 60f.dpToPx(this)).roundToInt()
            val bottomPadding = systemBars.bottom
            rootView.setPadding(0, topPadding, 0, bottomPadding)

            // 初始化视图
            val title: View = findViewById(R.id.title)
            val bigTitle: View = findViewById(R.id.bigTitle)
            // 创建 SimonEdgeIllusion 实例
            TitleAnim(
                title,
                bigTitle,
                (systemBars.top + 60f.dpToPx(this)),
                systemBars.top.toFloat()
            )
            insets
        }

        /** 设置监听 **/
        // 为关于选项卡设置点击监听
        findViewById<View>(R.id.aboutOption).setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.setThemeOption).setOnClickListener {
            val intent = Intent(this, ThemeActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.updaterOption).setOnClickListener {
            val intent = Intent(this, UpdaterActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.privacyOption).setOnClickListener {
            val intent = Intent(this, AgreementPolicyActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.setUserOption).setOnClickListener {
            openUrlInBrowser(this, "$BASE_URL/account/account-details")
        }

        val backButton: View = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }
}
package com.gustate.uotan.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gustate.uotan.R
import com.gustate.uotan.anim.TitleAnim
import com.gustate.uotan.gustatex.dialog.InputDialog
import com.gustate.uotan.utils.Utils
import com.gustate.uotan.utils.Utils.Companion.openImmersion

class SettingsActivity : AppCompatActivity(), InputDialog.InputDialogListener {

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
            val topPadding = Utils.dp2Px(60, this).toInt() + systemBars.top
            val bottomPadding = systemBars.bottom
            rootView.setPadding(0, topPadding, 0, bottomPadding)

            // 初始化视图
            val title: View = findViewById(R.id.tittle)
            val bigTitle: View = findViewById(R.id.bigTittle)
            // 创建 SimonEdgeIllusion 实例
            TitleAnim(title, bigTitle, Utils.dp2Px(60, this) + systemBars.top.toFloat(), systemBars.top.toFloat())

            insets
        }

        /** 设置监听 **/
        // 为关于选项卡设置点击监听
        findViewById<View?>(R.id.aboutOption).setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        /*findViewById<View?>(R.id.updaterOption).setOnClickListener {
            val dialog = InputDialog(this, 1, this)
            dialog.show()
            dialog.updateContent("1", "1")
        }

        findViewById<View?>(R.id.setInterfaceOption).setOnClickListener {
            val intent = Intent(this, InterfaceSettingsActivity::class.java)
            startActivity(intent)
        }*/


        val backButton: View = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    override fun onInputSubmitted(dialogId: Int, input: String) {
        when(dialogId){
            1 -> Toast.makeText(this, input, Toast.LENGTH_LONG).show()
        }
    }
}
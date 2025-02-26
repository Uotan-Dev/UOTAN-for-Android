package com.gustate.uotan.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gustate.uotan.R
import com.gustate.uotan.utils.parse.data.CookiesManager
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import com.gustate.uotan.utils.Utils.Companion.isLogin
import com.gustate.uotan.utils.Utils.Companion.openImmersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

/**
 * 启动页面 (Activity)
 * JiaGuZhuangZhi Miles
 * Gustate 02/23/2025
 * I Love Jiang’Xun
 */

class StartupActivity : AppCompatActivity() {

    // 开屏等待
    private val splashDelay: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // 启用边到边设计 把界面拓展到状态栏和导航栏
        enableEdgeToEdge()

        // 针对部分系统的小白条沉浸
        openImmersion(window)

        // 绑定视图
        setContentView(R.layout.activity_startup)

        // 设置 Paddings
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            insets

        }

        // 使用 Handler 延迟跳转
        Handler(Looper.getMainLooper()).postDelayed({
            executeApp(this)
        }, splashDelay)

        // 获取跳过按钮
        val skipCard: View = findViewById(R.id.skipCard)

        // 点击跳过直接跳转
        skipCard.setOnClickListener {
            executeApp(this)
        }

    }

    private fun executeApp(context: Context) {

        // 实例化 cookiesManager
        val cookiesManager = CookiesManager(context)

        // 开启协程
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cookiesManager.cookiesFlow.collect { cookies ->

                    // 将数据库中的 Cookies 赋值到全局变量中
                    Cookies = cookies

                    // 修改全局变量 isLogin
                    isLogin = cookies != mapOf<String,String>()

                    // 判断是否登录
                    if (!isLogin) {

                        // 未登录 启动登录窗口
                        startActivity(Intent(context, LoginActivity::class.java))

                        // 结束当前窗口
                        finish()

                    } else {

                        // 已登录

                        withContext(Dispatchers.IO) { // 切换到 IO 线程执行网络请求
                            try {
                                val response = Jsoup.connect(BASE_URL)
                                    .userAgent(USER_AGENT)
                                    .timeout(TIMEOUT_MS)
                                    .cookies(Cookies)
                                    .execute() // 这就是原来的109行

                                val document = response.parse()

                                val title = document
                                    .select("#main-header > div > div > div > h1")
                                    .first()
                                    ?.text()
                                    ?: ""

                                // 提取CSRF令牌（关键安全参数）
                                val xfToken = document
                                    .select("input[name=_xfToken]")
                                    .first()
                                    ?.attr("value") ?: throw Exception("CSRF token not found")

                                val url = response.url().toString()
                                val updatePolicyActivityIntent =
                                    Intent(context, UpdatePolicyActivity::class.java)
                                        .putExtra("xfToken", xfToken)
                                        .putExtra("url", url)
                                withContext(Dispatchers.Main) { // 切回主线程处理UI
                                    if (title == "隐私政策") {
                                        startActivity(updatePolicyActivityIntent)
                                        finish()
                                    } else {
                                        startActivity(Intent(context, MainActivity::class.java))
                                        finish()
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    // 处理网络错误
                                    Toast.makeText(context, "网络请求失败", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
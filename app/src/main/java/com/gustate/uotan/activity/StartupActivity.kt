package com.gustate.uotan.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.gustate.uotan.R
import com.gustate.uotan.utils.parse.data.CookiesManager
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import com.gustate.uotan.utils.Utils.Companion.isLogin
import com.gustate.uotan.utils.Utils.Companion.openImmersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

/**
 * 启动页面 (Activity)
 * JiaGuZhuangZhi Miles
 * Gustate 03/03/2025
 * I Love Jiang’Xun
 */

class StartupActivity : AppCompatActivity() {

    data class StartupTypeData(
        val isLogin: Boolean = false,
        val isSmsVerify: Boolean = false,
        val isAgreement: Boolean = false,
        val updatePolicyActivityIntent: Intent? = null,
        val cookies: Map<String, String>? = null
    )

    // 开屏等待
    private lateinit var countDownTimer: CountDownTimer
    // 是否已经点击
    private var isClick = false

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
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }
        val skip: TextView = findViewById(R.id.skip)
        countDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // 更新剩余时间显示（可选）
                val sec = getString(R.string.skip) + " (${millisUntilFinished / 1000}s)"
                skip.text = sec
            }
            override fun onFinish() {
                lifecycleScope.launch {
                    startupApp(this@StartupActivity)
                }
            }
        }.start()

        // 获取跳过按钮
        val skipCard: View = findViewById(R.id.skipCard)

        // 点击跳过直接跳转
        skipCard.setOnClickListener {
            lifecycleScope.launch {
                if (!isClick) {
                    startupApp(this@StartupActivity)
                    isClick = true
                    countDownTimer.cancel()
                }

            }
        }

    }

    private suspend fun startupApp(context: Context) = withContext(Dispatchers.IO) {
        try {
            val startupTypeData = startupTypeParse()
            withContext(Dispatchers.Main) {
                if (!startupTypeData.isLogin) {
                    // 未登录 启动登录窗口
                    startActivity(Intent(context, LoginActivity::class.java))
                    // 结束当前窗口
                    this@StartupActivity.finish()
                } else {
                    if (startupTypeData.isAgreement) {
                        startActivity(startupTypeData.updatePolicyActivityIntent)
                        this@StartupActivity.finish()
                    } else if (startupTypeData.isSmsVerify) {
                        Toast.makeText(
                            context,
                            R.string.china_mainland_verify,
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(context, BindPhoneActivity::class.java))
                        this@StartupActivity.finish()
                    } else {
                        startActivity(Intent(context, MainActivity::class.java))
                        this@StartupActivity.finish()
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                // 处理网络错误
                Toast.makeText(
                    context,
                    R.string.network_request_failed,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private suspend fun startupTypeParse(): StartupTypeData = withContext(Dispatchers.IO) {
        val cookiesManager = CookiesManager(this@StartupActivity)
        // 将数据库中的 Cookies 赋值到全局变量中
        Cookies = cookiesManager.cookiesFlow.first()
        // 修改全局变量 isLogin
        isLogin = Cookies != mapOf<String, String>()
        if (isLogin) {
            // 解析网页, document 返回的就是网页 Document 对象
            val response = Jsoup.connect(BASE_URL)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .cookies(Cookies)
                .execute()
            val document = response
                .parse()
            val noticeTitle = document
                .getElementsByClass("notice-content")
                .first()
                ?.ownText()
                ?: ""
            val pageTitle = document
                .select("#main-header > div > div > div > h1")
                .first()
                ?.text()
                ?: ""
            val xfToken = document
                .select("input[name=_xfToken]")
                .first()
                ?.attr("value") ?: throw Exception("CSRF token not found")
            val isAgreement = pageTitle == "隐私政策" || pageTitle == "服务协议"
            val isSmsVerify = noticeTitle == "您需要验证手机号才能使用全部功能（仅限中国大陆）"
            val updatePolicyActivityIntent =
                Intent(this@StartupActivity, UpdatePolicyActivity::class.java)
                    .putExtra("xfToken", xfToken)
                    .putExtra("url", response.url().toString())
            return@withContext StartupTypeData(
                true,
                isSmsVerify,
                isAgreement,
                updatePolicyActivityIntent,
                Cookies
            )
        } else {
            return@withContext StartupTypeData()
        }
    }
}
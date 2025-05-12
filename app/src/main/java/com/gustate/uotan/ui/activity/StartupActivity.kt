package com.gustate.uotan.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import com.gustate.uotan.utils.Utils.Companion.errorDialog
import com.gustate.uotan.utils.Utils.Companion.isLogin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

/**
 * 启动页面 (Activity)
 * JiaGuZhuangZhi Miles
 * Gustate 03/03/2025
 * I Love Jiang’Xun
 */

class StartupActivity : BaseActivity() {

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
    private var isStarting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                val sec = getString(R.string.skip) + " (${millisUntilFinished / 1000}s)"
                skip.text = sec
            }
            override fun onFinish() {
                if (!isStarting) startupApp(this@StartupActivity)
                else return
            }
        }.start()

        skip.setOnClickListener {
            if (!isStarting) {
                isStarting = true
                startupApp(this@StartupActivity)
                countDownTimer.cancel()
            } else return@setOnClickListener
        }
    }

    private fun startupApp(context: Context) {
        lifecycleScope.launch {
            try {
                val startupTypeData = startupTypeParse()
                withContext(Dispatchers.Main) {
                    if (!startupTypeData.isLogin) {
                        startActivity(
                            Intent(
                                context,
                                LoginActivity::class.java
                            )
                        )
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
                isStarting = false
                withContext(Dispatchers.Main) {
                    errorDialog(this@StartupActivity, "ERROR", e.message)
                }
            } finally {
                isStarting = false
            }
        }
    }

    private suspend fun startupTypeParse(): StartupTypeData = withContext(Dispatchers.IO) {
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
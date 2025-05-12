package com.gustate.uotan.fragment.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gustate.uotan.R
import com.gustate.uotan.ui.activity.BindPhoneActivity
import com.gustate.uotan.ui.activity.MainActivity
import com.gustate.uotan.ui.activity.UpdatePolicyActivity
import com.gustate.uotan.gustatex.dialog.LoadingDialog
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import com.gustate.uotan.utils.Utils.Companion.isLogin
import com.gustate.uotan.utils.parse.data.CookiesManager
import com.gustate.uotan.utils.parse.user.LoginParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

/**
 * 两步验证页面 (Fragment)
 * JiaGuZhuangZhi Miles
 * Gustate 02/23/2025
 * I Love Jiang’Xun
 */

class TwoStepFragment : Fragment() {

    data class StartupTypeData(
        val isLogin: Boolean = false,
        val isSmsVerify: Boolean = false,
        val isAgreement: Boolean = false,
        val updatePolicyActivityIntent: Intent? = null,
        val cookies: Map<String, String>? = null
    )

    /**
     * 加载视图时
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 加载布局
        return inflater.inflate(
            R.layout.fragment_two_step,
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
        // 一次性代码编辑框
        val twoStepEdit = view.findViewById<EditText>(R.id.two_step_edittext)
        // 登录按钮
        val verifyButton = view.findViewById<View>(R.id.verifyButton)

        /** 变量 **/
        // 创建一个变量判断是否正在进行两步验证操作
        var isTwoStepOperation = false

        /** 常量 **/
        // 实例化 CookiesManager
        val cookiesManager = CookiesManager(requireContext())
        // 实例化 LoadingDialog
        val loadingDialog = LoadingDialog(requireContext())
        // 自定义编辑框监听
        val textWatcher = object: TextWatcher {
            // 文本变化前
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 无操作
            }
            // 文本变化时
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 获取一次性代码编辑框的内容
                val code = twoStepEdit.text.toString()
                // 编辑框不为空
                if (code.isNotEmpty()) {
                    // 设置登录按钮透明度为 1.0 (Float)
                    verifyButton.alpha = 1.0f
                }
                // 编辑框为空
                else {
                    // 设置登录按钮透明度为 0.15 (Float)
                    verifyButton.alpha = 0.15f
                }
            }
            // 文本变化后
            override fun afterTextChanged(s: Editable?) {
                // 无操作
            }
        }

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

        /** 获取上一个 Fragment 传递的数据 **/
        // 获取主 Bundle
        val bundle = arguments
        // 获取二次验证 url
        val url = bundle?.getString("url") ?: ""
        // 获取登录时的 xfToken
        val xfToken = bundle?.getString("xfToken") ?: ""

        /** 设置监听 **/
        // 为一次性代码编辑框添加监听 (textWatcher)
        twoStepEdit.addTextChangedListener(textWatcher)
        // 为验证按钮添加点击监听
        verifyButton.setOnClickListener {
            // 获取一次性代码编辑框的内容
            val code = twoStepEdit.text.toString()
            when {
                // 当用户输入的一次性代码格式不正确时
                code.length != 6 ->
                    Toast.makeText(
                        context,
                        R.string.code_format_doesnt_match,
                        Toast.LENGTH_LONG).show()
                // 当没有进行另一个验证操作时
                else -> if (!isTwoStepOperation) {
                    // 启动协程
                    lifecycleScope.launch {
                        // 声明已有验证操作
                        isTwoStepOperation = true
                        // 显示 Loading Dialog
                        loadingDialog.show()
                        try {
                            // 进行验证尝试, 并储存结果
                            val verifyResult = LoginParse.twoStep(url, xfToken, twoStepEdit.text.toString())
                            // 如果结果不为空, 则登录成功
                            if (verifyResult != mapOf<String, String>()) {
                                // 使用 CookiesManager 将结果保存到数据库
                                cookiesManager.saveCookies(verifyResult)
                                // 设置全局 Cookies
                                Cookies = verifyResult
                                // 修改登录状态
                                isLogin = true
                                // 回到 UI 线程
                                withContext(Dispatchers.Main) {
                                    // 弹出登录成功提示
                                    Toast.makeText(
                                        context,
                                        R.string.login_successful,
                                        Toast.LENGTH_LONG
                                    ).show()
                                    // 关闭 LoadingDialog
                                    loadingDialog.cancel()
                                    // 开启
                                    startupApp(requireContext())
                                    // 结束当前 Activity
                                    activity?.finish()
                                }
                            }
                            // 如果以上均不是, 则一次性代码不正确
                            else {
                                // 结束正在登录状态
                                isTwoStepOperation = false
                                // 回到 UI 线程
                                withContext(Dispatchers.Main) {
                                    // 弹出账密不正确或未注册提示
                                    Toast.makeText(
                                        context,
                                        R.string.code_incorrect,
                                        Toast.LENGTH_LONG
                                    ).show()
                                    // 关闭 LoadingDialog
                                    loadingDialog.cancel()
                                }
                            }
                        }
                        // 捕获异常
                        catch (e: Exception) {
                            // 结束正在登录状态
                            isTwoStepOperation = false
                            // 回到 UI 线程
                            withContext(Dispatchers.Main) {
                                // 弹出登录失败提示
                                Toast.makeText(
                                    context,
                                    R.string.login_failed,
                                    Toast.LENGTH_SHORT
                                ).show()
                                // 关闭 LoadingDialog
                                loadingDialog.cancel()
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun startupApp(context: Context) = withContext(Dispatchers.IO) {
        try {
            val startupTypeData = startupTypeParse()
            withContext(Dispatchers.Main) {
                if (startupTypeData.isAgreement) {
                    startActivity(startupTypeData.updatePolicyActivityIntent!!)
                    activity?.finish()
                } else if (startupTypeData.isSmsVerify) {
                    Toast.makeText(
                        context,
                        R.string.china_mainland_verify,
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(context, BindPhoneActivity::class.java))
                    activity?.finish()
                } else {
                    startActivity(Intent(context, MainActivity::class.java))
                    activity?.finish()
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
            Intent(requireContext(), UpdatePolicyActivity::class.java)
                .putExtra("xfToken", xfToken)
                .putExtra("url", response.url().toString())
        return@withContext StartupTypeData(
            true,
            isSmsVerify,
            isAgreement,
            updatePolicyActivityIntent,
            Cookies
        )
    }

}
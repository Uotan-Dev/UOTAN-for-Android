package com.gustate.uotan.fragment.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.gustate.uotan.R
import com.gustate.uotan.ui.activity.BindPhoneActivity
import com.gustate.uotan.ui.activity.LoginActivity
import com.gustate.uotan.ui.activity.MainActivity
import com.gustate.uotan.ui.activity.UpdatePolicyActivity
import com.gustate.uotan.gustatex.dialog.LoadingDialog
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import com.gustate.uotan.utils.Utils.Companion.isLogin
import com.gustate.uotan.utils.Utils.Companion.saveToExternalPrivateDir
import com.gustate.uotan.utils.parse.data.CookiesManager
import com.gustate.uotan.utils.parse.user.LoginParse
import com.gustate.uotan.utils.parse.user.MeParse.Companion.fetchMeData
import com.gustate.uotan.utils.room.User
import com.gustate.uotan.utils.room.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

/**
 * 登录页面 (Fragment)
 * JiaGuZhuangZhi Miles
 * Gustate 02/23/2025
 * I Love Jiang’Xun
 */

class LoginFragment : Fragment() {

    private lateinit var viewModel: UserViewModel

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
            R.layout.fragment_login,
            container,
            false
        )
    }

    data class StartupTypeData(
        val isLogin: Boolean = false,
        val isSmsVerify: Boolean = false,
        val isAgreement: Boolean = false,
        val updatePolicyActivityIntent: Intent? = null,
        val cookies: Map<String, String>? = null
    )

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
        // 登录按钮
        val loginButton = view.findViewById<View>(R.id.loginButton)
        // 用户名/邮箱编辑框
        val usernameEdit = view.findViewById<EditText>(R.id.username_email)
        // 密码编辑框
        val passwordEdit = view.findViewById<EditText>(R.id.password)
        // 同意用户协议按钮
        val checkBox = view.findViewById<CheckBox>(R.id.argumentsCheckBox)

        /** 变量 **/
        // 创建一个变量判断是否正在进行登录操作
        var isLoggingIn = false
        viewModel = ViewModelProvider(this)[UserViewModel::class.java]

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
                // 获取用户名/邮箱编辑框的内容
                val username = usernameEdit.text.toString()
                // 获取密码编辑框的内容
                val password = passwordEdit.text.toString()
                // 两个编辑框都不为空
                if (username.isNotEmpty() && password.isNotEmpty()) {
                    // 设置登录按钮透明度为 1.0 (Float)
                    loginButton.alpha = 1.0f
                }
                // 两个编辑框其一为空
                else {
                    // 设置登录按钮透明度为 0.15 (Float)
                    loginButton.alpha = 0.15f
                }
            }
            // 文本变化后
            override fun afterTextChanged(s: Editable?) {
                // 无操作
            }
        }

        /** 获取系统栏高度并同步到占位布局 **/
        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /** 设置监听 **/
        // 为用户名/邮箱编辑框添加监听 (textWatcher)
        usernameEdit.addTextChangedListener(textWatcher)
        // 为密码编辑框添加监听 (textWatcher)
        passwordEdit.addTextChangedListener(textWatcher)
        // 为登录按钮添加点击监听
        loginButton.setOnClickListener {
            // 获取用户名/邮箱编辑框的内容
            val username = usernameEdit.text.toString()
            // 获取密码编辑框的内容
            val password = passwordEdit.text.toString()
            when {
                // 当用户没有输入用户名和密码时
                username.isEmpty() && password.isEmpty() ->
                    Toast.makeText(
                        context,
                        R.string.please_press_username_password,
                        Toast.LENGTH_SHORT
                    ).show()
                // 当用户没有输入用户名时
                username.isEmpty() ->
                    Toast.makeText(
                        context,
                        R.string.please_press_username,
                        Toast.LENGTH_SHORT
                    ).show()
                // 当用户没有输入密码时
                password.isEmpty() ->
                    Toast.makeText(
                        context,
                        R.string.please_press_password,
                        Toast.LENGTH_SHORT
                    ).show()
                // 当用户没有同意用户协议时
                !checkBox.isChecked ->
                    Toast.makeText(
                        context,
                        R.string.allow_arguments,
                        Toast.LENGTH_SHORT
                    ).show()
                // 当没有进行另一个登录操作时
                else -> if (!isLoggingIn){
                    // 开启协程
                    lifecycleScope.launch {
                        // 声明已有登录操作
                        isLoggingIn = true
                        // 显示 Loading Dialog
                        loadingDialog.show()
                        try {
                            // 进行登录尝试, 并储存结果
                            val firstLoginResult = LoginParse.login(username, password)
                            // 从结果中获取 Cookies
                            val cookies = firstLoginResult.cookies
                            // 从结果中获取是否需要两步验证
                            val isTwoStep = firstLoginResult.isTwoStep
                            // 从结果中获取两步验证的 url
                            val twoStepUrl = firstLoginResult.url
                            // 从结果中获取两步验证所需的 xfToken
                            val xfToken = firstLoginResult.xfToken
                            // 将两步验证所需的内容储存到 bundle 中备用
                            val bundle = Bundle().apply {
                                // 存储两步验证的 url
                                putString("url", twoStepUrl)
                                // 存储 xfToken
                                putString("xfToken", xfToken)
                            }
                            // 如果 Cookies 不为空, 则登录成功
                            if (cookies != mapOf<String, String>()) {
                                // 使用 CookiesManager 将 cookies 保存到数据库
                                cookiesManager.saveCookies(cookies)
                                // 设置全局 Cookies
                                Cookies = cookies
                                // 修改登录状态
                                isLogin = true
                                // 结束正在登录状态
                                isLoggingIn = false
                                // 获取当前用户的基本信息
                                val userData = fetchMeData()
                                // 缓存基本信息链接
                                viewModel.insert(User(0, userData.userName, userData.cover,
                                    userData.avatar, userData.signature, userData.auth,
                                    userData.postCount, userData.resCount, userData.userId,
                                    userData.points, userData.uCoin, userData.ipAddress)
                                )
                                // 缓存头像文件
                                saveToExternalPrivateDir(requireContext(),
                                    BASE_URL + userData.avatar, "user/", "avatar.jpg")
                                // 缓存封面文件
                                saveToExternalPrivateDir(requireContext(),
                                    BASE_URL + userData.cover, "user/", "cover.jpg")
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
                            // 如果是两步验证, 则进行两步验证
                            else if (isTwoStep) {
                                // 结束正在登录状态
                                isLoggingIn = false
                                // 回到 UI 线程
                                withContext(Dispatchers.Main) {
                                    // 弹出两步验证提示
                                    Toast.makeText(
                                        context,
                                        R.string.two_step_verification_toast,
                                        Toast.LENGTH_LONG
                                    ).show()
                                    // 关闭 LoadingDialog
                                    loadingDialog.cancel()
                                    // 保存传递 bundle 并启动 TwoStepFragment
                                    TwoStepFragment().apply {
                                        // 保存 bundle
                                        arguments = bundle
                                    }.let {
                                        // 传递 bundle 并启动 TwoStepFragment
                                        (activity as? LoginActivity)?.switchFragment(it, data = bundle)
                                    }
                                }
                            }
                            // 如果以上均不是, 则登账密不正确或未注册
                            else {
                                // 结束正在登录状态
                                isLoggingIn = false
                                // 回到 UI 线程
                                withContext(Dispatchers.Main) {
                                    // 弹出账密不正确或未注册提示
                                    Toast.makeText(
                                        context,
                                        R.string.password_incorrect,
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
                            isLoggingIn = false
                            Log.e("err", e.toString())
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
        } catch (_: Exception) {
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
package com.uotan.forum.welcome.ui.composable

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.uotan.forum.R
import com.uotan.forum.ui.composable.Input
import com.uotan.forum.ui.composable.RoundedCheckBox
import com.uotan.forum.ui.theme.button.filledButtonColors
import com.uotan.forum.ui.theme.text.buttonBasicTextStyle
import com.uotan.forum.ui.theme.uotanColors
import com.uotan.forum.welcome.ui.LoginViewModel
import com.uotan.forum.utils.Utils.showToast
import com.kyant.capsule.ContinuousRoundedRectangle
import com.uotan.forum.ui.composable.liquid.RoundedIconButton
import com.uotan.forum.ui.composable.pwKeyboardOptions
import com.uotan.forum.ui.theme.text.describeTextStyle
import com.uotan.forum.welcome.ui.NavSealed
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.uotan.forum.settings.ui.PolicyActivity
import com.uotan.forum.startup.ui.StartupState
import com.uotan.forum.startup.ui.composeable.startApp
import com.uotan.forum.ui.composable.UotanLogo
import com.uotan.forum.ui.composable.dialog.BaseDialog
import com.uotan.forum.ui.dialog.LoadingDialog
import com.uotan.forum.utils.Utils.baseUrl
import com.uotan.forum.utils.Utils.errorDialog
import com.uotan.forum.welcome.ui.model.LoginEffect
import com.uotan.forum.welcome.ui.model.LoginUiState
import dev.chrisbanes.haze.HazeState

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel,
    navController: NavController,
    hazeState: HazeState,
    loading: LoadingDialog
) {

    val context = LocalContext.current

    val loginState by viewModel.uiState.collectAsState()
    val startupState by viewModel.startupUiState.collectAsState()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(
            modifier = Modifier
                .height(height = 60.dp)
        )
        UotanLogo(
            modifier = Modifier
                .padding(
                    top = 48.dp,
                    bottom = 60.dp
                ),
            height = 22.dp
        )
        LoginForm(
            navController = navController,
            onLoginClick = { account, password ->
                viewModel.login(account, password)
            }
        )
    }
    HandleLoginUiState(
        viewModel = viewModel,
        uiState = loginState,
        hazeState = hazeState,
        loading = loading
    )
    HandleLoginEffect(
        viewModel = viewModel,
        navController = navController
    )
    HandleStartupEffect(
        context = context,
        uiState = startupState,
        hazeState = hazeState
    )
}

@Composable
fun LoginForm(
    navController: NavController,
    onLoginClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val backdrop = rememberLayerBackdrop()

    var account by remember { mutableStateOf(value = "") }
    var password by remember { mutableStateOf(value = "") }
    var isShowPassword by remember { mutableStateOf(value = false) }
    var isAgree by remember { mutableStateOf(value = false) }

    Column(
        modifier = modifier
            .verticalScroll(state = rememberScrollState())
            .fillMaxWidth()
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Input(
            value = account,
            modifier = Modifier,
            hint = stringResource(id = R.string.username_or_email),
            contentMargin = 18.dp,
            onValueChange = {
                account = it
            }
        )
        Spacer(
            modifier = Modifier
                .height(height = 20.dp)
        )
        Input(
            value = password,
            modifier = Modifier,
            hint = stringResource(id = R.string.password),
            endIcon = painterResource(
                id =
                    if (isShowPassword) R.drawable.input_icon_visibility
                    else R.drawable.input_icon_visibility_off
            ),
            onEndIconClick = { isShowPassword = !isShowPassword },
            contentMargin = 18.dp,
            keyboardOptions = pwKeyboardOptions(),
            visualTransformation =
                if (isShowPassword) VisualTransformation.None
                else PasswordVisualTransformation(),
            onValueChange = {
                password = it
            }
        )
        Spacer(modifier = Modifier.height(40.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundedCheckBox(
                checked = isAgree,
                onCheckedChange = {
                    isAgree = it
                }
            )
            val annotatedText = buildAnnotatedString {
                val serviceAgreement = stringResource(id = R.string.service_agreement)
                val privacyPolicy = stringResource(id = R.string.privacy_policy)
                val userAgreement = stringResource(id = R.string.user_agreement)
                val disclaimer = stringResource(id = R.string.disclaimers)

                val fullText = stringResource(
                    id = R.string.agreement_text,
                    serviceAgreement,
                    privacyPolicy,
                    userAgreement,
                    disclaimer
                )
                append(fullText)

                @Composable
                fun addTag(tag: String, keyword: String) {
                    val start = fullText.indexOf(keyword)
                    val end = start + keyword.length
                    addStyle(
                        style = SpanStyle(
                            color = MaterialTheme.uotanColors.onFilledTonalButton, // 使用主题色
                            textDecoration = TextDecoration.Underline // 添加下划线
                        ),
                        start = start,
                        end = end
                    )
                    addStringAnnotation(
                        tag = tag,
                        annotation = keyword,
                        start = start,
                        end = end
                    )
                }

                addTag(tag = "service", keyword = serviceAgreement)
                addTag(tag = "privacy", keyword = privacyPolicy)
                addTag(tag = "user", keyword = userAgreement)
                addTag(tag = "disclaimer", keyword = disclaimer)
            }
            var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
            Text(
                text = annotatedText,
                color = MaterialTheme.uotanColors.onBackgroundPrimary,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                onTextLayout = { layoutResult = it },
                modifier = Modifier
                    .pointerInput(key1 = Unit) {
                        detectTapGestures { offset ->
                            layoutResult?.let { layout ->
                                val position = layout.getOffsetForPosition(offset)
                                annotatedText.getStringAnnotations(position, position)
                                    .firstOrNull()?.let { annotation ->
                                        openPolicyPage(
                                            context = context,
                                            annotation = annotation
                                        )
                                    }
                            }
                        }
                    }
                    .padding(start = 6.dp)

            )
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp)
                .height(48.dp),
            shape = ContinuousRoundedRectangle(16.dp),
            colors = filledButtonColors(),
            onClick = {
                handleLogin(
                    account = account,
                    password = password,
                    isAgree = isAgree,
                    context = context,
                    onLoginClick = onLoginClick
                )
            }
        ) {
            Text(
                text = stringResource(id = R.string.login),
                style = buttonBasicTextStyle()
            )
        }
        Row(
            modifier = Modifier
                .padding(top = 84.dp)
        ) {
            RoundedIconButton(
                modifier = Modifier.padding(horizontal = 8.dp),
                backdrop = backdrop,
                painter = painterResource(R.drawable.ic_qq),
                onClick = {
                    if (!isAgree) {
                        showToast(context, R.string.allow_arguments)
                        return@RoundedIconButton
                    }
                    navController.navigate(NavSealed.QQLogin.route)
                }
            )
            RoundedIconButton(
                modifier = Modifier.padding(horizontal = 8.dp),
                backdrop = backdrop,
                painter = painterResource(R.drawable.ic_xiaomi),
                onClick = {
                    if (!isAgree) {
                        showToast(context, R.string.allow_arguments)
                        return@RoundedIconButton
                    }
                    navController.navigate(NavSealed.XiaomiLogin.route)
                }
            )
            RoundedIconButton(
                modifier = Modifier.padding(horizontal = 8.dp),
                backdrop = backdrop,
                painter = painterResource(R.drawable.ic_weibo),
                onClick = {
                    if (!isAgree) {
                        showToast(context, R.string.allow_arguments)
                        return@RoundedIconButton
                    }
                    navController.navigate(NavSealed.WeiboLogin.route)
                }
            )
        }
        Text(
            text = stringResource(id = R.string.third_party_login),
            modifier = Modifier
                .padding(top = 12.dp),
            style = describeTextStyle()
        )
    }
}

/**
 * 处理登录操作
 * @param account 账户名/邮箱
 * @param password 密码
 * @param isAgree 是否同意一系列文档
 * @param context 安卓上下文
 * @param onLoginClick 登录回调
 */
private fun handleLogin(
    account: String,
    password: String,
    isAgree: Boolean,
    context: Context,
    onLoginClick: (String, String) -> Unit
) {
    when {
        // 账户名和密码都没输入
        account.isEmpty() && password.isEmpty() ->
            showToast(
                context = context,
                resId = R.string.please_press_username_password
            )
        // 仅仅没输入账户名
        account.isEmpty() ->
            showToast(
                context = context,
                resId = R.string.please_press_username
            )
        // 仅仅没输入密码
        password.isEmpty() ->
            showToast(
                context = context,
                resId = R.string.please_press_password
            )
        // 没同意一系列协议
        !isAgree ->
            showToast(
                context = context,
                resId = R.string.allow_arguments
            )
        // 都办了执行登录操作
        else -> onLoginClick(account, password)
    }
}

/**
 * 打开一些文档页面
 * @param context 安卓上下文
 * @param annotation 超链接文本
 */
private fun openPolicyPage(
    context: Context,
    annotation: AnnotatedString.Range<String>
) {
    context.startActivity(
        Intent(
            context,
            PolicyActivity::class.java
        ).apply {
            when (annotation.tag) {
                "service" -> {
                    putExtra("type", 2)
                    putExtra(
                        "url",
                        "$baseUrl/help/terms/"
                    )
                }
                "privacy" -> {
                    putExtra("type", 4)
                    putExtra(
                        "url",
                        "$baseUrl/help/privacy-policy/"
                    )
                }
                "user" -> {
                    putExtra("type", 1)
                    putExtra(
                        "url",
                        "$baseUrl/help/yhgy/"
                    )
                }
                "disclaimer" -> {
                    putExtra("type", 5)
                    putExtra(
                        "url",
                        "$baseUrl/help/mzsm/"
                    )
                }
            }
        }
    )
}

/**
 * 处理登录状态
 * @param uiState 登录界面状态
 * @param hazeState 模糊云雾状态
 * @param loading 加载弹窗实例
 */
@Composable
fun HandleLoginUiState(
    viewModel: LoginViewModel,
    uiState: LoginUiState,
    hazeState: HazeState,
    loading: LoadingDialog
) {
    if (uiState.loading) loading.show() else loading.dismiss()
    if (uiState.errorDialog != null) {
        errorDialog(
            context = LocalContext.current,
            title = uiState.errorDialog.title,
            message = uiState.errorDialog.message
        )
        viewModel.clearError()
    }
}

/**
 * 处理登录情况
 * @param viewModel 登录 ViewModel
 * @param navController 导航控制器
 */
@Composable
fun HandleLoginEffect(
    viewModel: LoginViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToHome -> {
                    showToast(context = context, resId = R.string.login_successful)
                    viewModel.startup()
                }
                is LoginEffect.NavigateToTwoStep ->
                    navController.navigate(route = NavSealed.LoginTwoStep.route)
            }
        }
    }
}

/**
 * 处理启动状态
 * @param uiState 登录界面状态
 * @param hazeState 模糊云雾状态
 */
@Composable
fun HandleStartupEffect(
    context: Context,
    uiState: StartupState,
    hazeState: HazeState
) {
    var isStarting by remember { mutableStateOf(value = false) }
    LaunchedEffect(key1 = uiState) {
        // 当状态变为非Loading/Idle时，尝试启动
        if (uiState !is StartupState.Loading && uiState !is StartupState.Idle) {
            if (!isStarting) {
                isStarting = true
                startApp(context = context, startupState = uiState)
            }
        }
    }
}
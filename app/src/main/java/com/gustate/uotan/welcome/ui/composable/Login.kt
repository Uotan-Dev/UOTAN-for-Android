package com.gustate.uotan.welcome.ui.composable

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gustate.uotan.R
import com.gustate.uotan.settings.ui.PolicyActivity
import com.gustate.uotan.ui.composable.Input
import com.gustate.uotan.ui.composable.RoundedCheckBox
import com.gustate.uotan.ui.theme.button.filledButtonColors
import com.gustate.uotan.ui.theme.text.buttonBasicTextStyle
import com.gustate.uotan.ui.theme.uotanColors
import com.gustate.uotan.user.login.ui.LoginState
import com.gustate.uotan.welcome.ui.LoginViewModel
import com.gustate.uotan.utils.Utils.baseUrl
import com.gustate.uotan.utils.Utils.showToast
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gustate.uotan.dialog.LoadingDialog
import com.gustate.uotan.main.ui.MainActivity
import com.gustate.uotan.ui.composable.Logo
import com.gustate.uotan.ui.composable.liquid.RoundedIconButton
import com.gustate.uotan.ui.composable.pwKeyboardOptions
import com.gustate.uotan.ui.theme.text.describeTextStyle
import com.gustate.uotan.utils.Utils.errorDialog
import com.gustate.uotan.welcome.ui.NavSealed
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@Composable
fun LoginForm(
    navController: NavController,
    onLoginClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val backdrop = rememberLayerBackdrop()
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isAgree by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Input(
            value = account,
            modifier = Modifier,
            hint = stringResource(R.string.username_or_email),
            contentMargin = 18.dp,
            onValueChange = { account = it }
        )
        Spacer(modifier = Modifier.height(20.dp))
        Input(
            value = password,
            modifier = Modifier,
            hint = stringResource(R.string.password),
            contentMargin = 18.dp,
            keyboardOptions = pwKeyboardOptions(),
            onValueChange = { password = it }
        )
        Spacer(modifier = Modifier.height(40.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RoundedCheckBox(
                checked = isAgree,
                onCheckedChange = {
                    isAgree = it
                }
            )
            val annotatedText = buildAnnotatedString {
                val serviceAgreement = stringResource(R.string.service_agreement)
                val privacyPolicy = stringResource(R.string.privacy_policy)
                val userAgreement = stringResource(R.string.user_agreement)
                val disclaimer = stringResource(R.string.disclaimers)

                val fullText = stringResource(
                    R.string.agreement_text,
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
                            textDecoration = TextDecoration.Underline  // 添加下划线
                        ),
                        start = start,
                        end = end
                    )
                    addStringAnnotation(tag, keyword, start, end)
                }

                addTag("service", serviceAgreement)
                addTag("privacy", privacyPolicy)
                addTag("user", userAgreement)
                addTag("disclaimer", disclaimer)
            }
            var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
            Text(
                text = annotatedText,
                color = MaterialTheme.uotanColors.onBackgroundPrimary,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                onTextLayout = { layoutResult = it },
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            layoutResult?.let { layout ->
                                val position = layout.getOffsetForPosition(offset)
                                annotatedText.getStringAnnotations(position, position)
                                    .firstOrNull()?.let { annotation ->
                                        openPolicy(context, annotation)
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
                when {
                    account.isEmpty() && password.isEmpty() -> {
                        showToast(context, R.string.please_press_username_password)
                    }
                    account.isEmpty() -> {
                        showToast(context, R.string.please_press_username)
                    }
                    password.isEmpty() -> {
                        showToast(context, R.string.please_press_password)
                    }
                    !isAgree -> {
                        showToast(context, R.string.allow_arguments)
                    }
                    else -> {
                        onLoginClick(account, password)
                    }
                }
            }
        ) {
            Text(
                text = stringResource(R.string.login),
                style = buttonBasicTextStyle()
            )
        }
        Row(modifier = Modifier.padding(top = 84.dp)) {
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
            text = "第三方账号登录",
            style = describeTextStyle(),
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

private fun openPolicy(
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

@Composable
fun LoginScreen(navController: NavController) {
    val viewModel: LoginViewModel = viewModel()
    val loginState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.height(60.dp)) {

        }
        Logo(
            modifier = Modifier
                .padding(top = 48.dp, bottom = 60.dp)
        )
        LoginForm(
            navController,
            { account, password ->
                viewModel.login(account, password)
            }
        )
    }
    if (loginState is LoginState.Loading) {
        LoadingDialog(context).show()
    }
    if (loginState is LoginState.Success) {
        showToast(context, R.string.login_successful)
        context.startActivity(Intent(context, MainActivity::class.java))
        (context as Activity).finish()
    }
    if (loginState is LoginState.Error) {
        errorDialog(context, stringResource(R.string.error), (loginState as LoginState.Error).message)
    }
}
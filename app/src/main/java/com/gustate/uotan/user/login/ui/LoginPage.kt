package com.gustate.uotan.user.login.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gustate.uotan.welcome.ui.composable.LoginForm

/*@Composable
fun LoginScreen(
    onLoginSuccess: (Map<String, String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: LoginViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // 根据状态显示不同的UI
    when (val state = uiState) {
        is LoginState.Idle -> LoginForm(
            onLoginClick = { account, password ->
                viewModel.login(account, password)
            },
            modifier = modifier
        )
        is LoginState.Loading -> LoadingScreen(modifier = modifier)
        is LoginState.TwoFactorRequired -> TwoFactorScreen(
            url = state.url,
            xfToken = state.xfToken,
            onSubmitClick = { code ->
                viewModel.submitTwoFactorCode(state.url, state.xfToken, code)
            },
            onBackClick = { viewModel.resetState() },
            modifier = modifier
        )
        is LoginState.Success -> {
            // 登录成功，回调并可能导航到其他屏幕
            LaunchedEffect(state.cookies) {
                onLoginSuccess(state.cookies)
            }
            // 可以显示成功消息或加载指示器
            LoadingScreen("登录成功，正在跳转...", modifier)
        }
        is LoginState.Error -> ErrorScreen(
            message = state.message,
            onRetryClick = { viewModel.resetState() },
            modifier = modifier
        )
    }
}*/

@Preview(locale = "zh")
@Composable
fun LoginFormPreview() {
    LoginForm(
        {i,b ->},
        Modifier
    )
}

/*@Composable
fun TwoFactorScreen(
    url: String,
    xfToken: String,
    onSubmitClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var code by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "两步验证",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "请输入发送到您邮箱的验证码",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("验证码") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onBackClick
            ) {
                Text("返回")
            }

            Button(
                onClick = { onSubmitClick(code) }
            ) {
                Text("提交")
            }
        }
    }
}

@Composable
fun LoadingScreen(
    message: String = "加载中...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(message)
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "错误",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetryClick
        ) {
            Text("重试")
        }
    }
}*/
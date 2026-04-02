package com.uotan.forum.startup.ui.composeable

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uotan.forum.R
import com.uotan.forum.main.ui.MainActivity
import com.uotan.forum.startup.ui.StartupState
import com.uotan.forum.startup.viewmodel.StartupViewModel
import com.uotan.forum.ui.activity.BindPhoneActivity
import com.uotan.forum.ui.activity.UpdatePolicyActivity
import com.uotan.forum.ui.composable.Logo
import com.uotan.forum.ui.theme.button.filledTonalButtonColors
import com.uotan.forum.utils.Utils.baseUrl
import com.uotan.forum.utils.Utils.showToast
import com.kyant.capsule.ContinuousRoundedRectangle

@Preview
@Composable
fun StartupScreen() {
    val context = LocalContext.current
    val viewModel: StartupViewModel = viewModel()
    val countDown by viewModel.countdown.collectAsStateWithLifecycle()
    val startupState by viewModel.uiState.collectAsStateWithLifecycle()
    var isStarting by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                modifier = Modifier.padding(top = 10.dp, end = 16.dp),
                colors = filledTonalButtonColors(),
                shape = ContinuousRoundedRectangle(16.dp),
                onClick = {
                    startApp(context, startupState)
                }
            ) {
                Text(text = stringResource(R.string.skip))
            }
        }
        Logo(modifier = Modifier
            .padding(bottom = 60.dp)
            .height(22.dp))
    }
    LaunchedEffect(countDown, startupState) {
        // 当倒计时结束或状态变为非Loading/Idle时，尝试启动
        if (countDown <= 0 && startupState !is StartupState.Loading && startupState !is StartupState.Idle) {
            if (!isStarting) {
                isStarting = true
                startApp(context, startupState)
            }
        }
    }
}


private fun startApp(context: Context, startupState: StartupState) {
    when(startupState) {
        is StartupState.Loading -> {
            showToast(context, "客服娘正在加紧获取您的登录信息")
        }
        is StartupState.Success -> {
            context.startActivity(Intent(context, MainActivity::class.java))
            (context as? Activity)?.finish() // 启动后结束当前Activity
        }
        is StartupState.NeedAgreePrivacy -> {
            context.startActivity(
                Intent(context, UpdatePolicyActivity::class.java)
                    .putExtra("url", baseUrl)
                    .putExtra("xfToken", startupState.xfToken)
            )
            (context as? Activity)?.finish()
        }
        is StartupState.NeedSmsVerify -> {
            showToast(context, R.string.china_mainland_verify)
            context.startActivity(Intent(context, BindPhoneActivity::class.java))
            (context as? Activity)?.finish()
        }
        is StartupState.Error -> {
            showToast(context, R.string.intent_error)
        }
        is StartupState.Idle -> {
            showToast(context, "客服娘正在加紧获取您的登录信息")
        }
    }
}
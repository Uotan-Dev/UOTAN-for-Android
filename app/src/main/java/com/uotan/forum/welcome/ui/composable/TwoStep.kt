package com.uotan.forum.welcome.ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kyant.capsule.ContinuousRoundedRectangle
import com.uotan.forum.R
import com.uotan.forum.ui.composable.Input
import com.uotan.forum.ui.composable.UotanLogo
import com.uotan.forum.ui.dialog.LoadingDialog
import com.uotan.forum.ui.theme.button.filledButtonColors
import com.uotan.forum.ui.theme.button.textButtonColors
import com.uotan.forum.ui.theme.text.buttonBasicTextStyle
import com.uotan.forum.welcome.ui.LoginViewModel
import dev.chrisbanes.haze.HazeState

@Composable
fun TwoStepPage(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel,
    navController: NavController,
    hazeState: HazeState,
    loading: LoadingDialog
) {

    val loginState by viewModel.uiState.collectAsState()
    val startupState by viewModel.startupUiState.collectAsState()

    val context = LocalContext.current
    val provider = loginState.twoFactor?.provider

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
                .padding(top = 48.dp),
            height = 22.dp
        )
        Text(
            modifier = Modifier
                .padding(
                    horizontal = 30.dp,
                    vertical = 36.dp
                ),
            text = stringResource(
                id =
                    when(provider) {
                        "email" -> R.string.two_step_hint_email
                        "totp" -> R.string.two_step_hint_totp
                        "backup" -> R.string.two_step_hint_backup
                        else -> R.string.error
                    }
            )
        )
        TwoStepForm(
            viewModel = viewModel,
            onNextClick = { code ->
                viewModel.submitTwoFactorCode(code = code)
            }
        )
    }
    HandleLoginUiState(
        uiState = loginState,
        hazeState = hazeState,
        loading = loading,
        viewModel = viewModel
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
fun TwoStepForm(
    viewModel: LoginViewModel,
    onNextClick: (code: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val loginState by viewModel.uiState.collectAsState()
    val provider = loginState.twoFactor?.provider
    var code by rememberSaveable { mutableStateOf(value = "") }
    Column(
        modifier = modifier
            .verticalScroll(state = rememberScrollState())
            .fillMaxWidth()
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Input(
            value = code,
            modifier = Modifier,
            hint = stringResource(
                id =
                    if (provider == "backup") R.string.backup_code
                    else R.string.one_time_code
            ),
            contentMargin = 18.dp,
            onValueChange = { code = it }
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp)
                .height(48.dp),
            shape = ContinuousRoundedRectangle(16.dp),
            colors = filledButtonColors(),
            onClick = {
                onNextClick(code)
            }
        ) {
            Text(
                text = stringResource(id = R.string.next),
                style = buttonBasicTextStyle()
            )
        }
        if (provider == "totp" || provider == "backup") {
            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 18.dp),
                shape = ContinuousRoundedRectangle(size = 16.dp),
                colors = textButtonColors(),
                onClick = {
                    viewModel.changeTotpProvider()
                }
            ) {
                Text(
                    text = stringResource(
                        id =
                            when (provider) {
                                "totp" -> R.string.auth_app_unavailable
                                "backup" -> R.string.continue_auth_app
                                else -> R.string.error
                            }
                    )
                )
            }
        }
    }
}


package com.gustate.uotan.welcome.ui.composable

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.gustate.uotan.R
import com.gustate.uotan.main.ui.MainActivity
import com.gustate.uotan.settings.ui.PolicyActivity
import com.gustate.uotan.ui.composable.dialog.BaseDialog
import com.gustate.uotan.ui.theme.button.filledButtonColors
import com.gustate.uotan.ui.theme.button.filledTonalButtonColors
import com.gustate.uotan.ui.theme.button.textButtonColors
import com.gustate.uotan.ui.theme.text.TypewriterText
import com.gustate.uotan.ui.theme.text.buttonBasicTextStyle
import com.gustate.uotan.ui.theme.uotanColors
import com.gustate.uotan.utils.Utils.baseUrl
import com.gustate.uotan.utils.Utils.openUrlInBrowser
import com.gustate.uotan.utils.mode.AppMode
import com.gustate.uotan.utils.mode.AppModeViewModel
import com.gustate.uotan.welcome.ui.NavSealed.Login
import com.kyant.capsule.ContinuousRoundedRectangle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun WelcomeScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel = hiltViewModel<AppModeViewModel>()
    val hazeState = rememberHazeState()
    val agreement = buildAgreementText()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(hazeState)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        UotanSlogan(
            modifier = Modifier.padding(top = 156.dp)
        )
        InitButton(
            modifier = Modifier.padding(bottom = 36.dp),
            navController = navController,
            viewModel = viewModel
        )
    }
    SelectModeDialog(
        viewModel = viewModel,
        hazeState = hazeState,
        agreement = agreement,
        context = context
    )
}

@Composable
private fun buildAgreementText(): AnnotatedString = buildAnnotatedString {
    val userAgreement = stringResource(R.string.user_agreement)
    val privacyPolicy = stringResource(R.string.privacy_policy)
    val disclaimer = stringResource(R.string.disclaimers)
    val agreementNotice = stringResource(
        id = R.string.agreement_notice,
        userAgreement, privacyPolicy, disclaimer
    )
    append(agreementNotice)
    @Composable
    fun addTag(tag: String, keyword: String) {
        val start = agreementNotice.indexOf(keyword)
        val end = start + keyword.length
        addStyle(
            style = SpanStyle(
                color = MaterialTheme.uotanColors.onFilledTonalButton,
                textDecoration = TextDecoration.Underline
            ),
            start = start,
            end = end
        )
        addStringAnnotation(tag, keyword, start, end)
    }
    addTag("user", userAgreement)
    addTag("privacy", privacyPolicy)
    addTag("disclaimer", disclaimer)
}

@Composable
fun UotanSlogan(modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TypewriterText(
            text = stringResource(R.string.welcome),
            style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.W500),
            color = MaterialTheme.uotanColors.onBackgroundPrimary,
            duration = 4000,
            cursorColor = MaterialTheme.uotanColors.onFilledTonalButton
        )
        TypewriterText(
            modifier = Modifier.padding(top = 5.dp),
            text = stringResource(R.string.welcome_describe),
            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.W400),
            color = MaterialTheme.uotanColors.onBackgroundSecondary,
            duration = 4000,
            cursorColor = MaterialTheme.uotanColors.onFilledTonalButton
        )
    }
}

@Composable
fun InitButton(
    modifier: Modifier,
    navController: NavController,
    viewModel: AppModeViewModel
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = ContinuousRoundedRectangle(16.dp),
            colors = filledButtonColors(),
            onClick = {
                openUrlInBrowser(context, "$baseUrl/register/")
            }
        ) {
            Text(
                text = stringResource(R.string.first_register),
                style = buttonBasicTextStyle()
            )
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .height(height = 48.dp),
            shape = ContinuousRoundedRectangle(16.dp),
            colors = filledTonalButtonColors(),
            onClick = {
                navController.navigate(Login.route)
            }
        ) {
            Text(
                text = stringResource(R.string.first_login),
                style = buttonBasicTextStyle()
            )
        }
        Button(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 60.dp),
            shape = ContinuousRoundedRectangle(16.dp),
            colors = textButtonColors(),
            onClick = {
                directStart(context)
                viewModel.updateMode(newMode = AppMode.NO_LOGIN)
            }
        ) {
            Text(text = stringResource(R.string.no_login))
        }
    }
}

@Composable
private fun SelectModeDialog(
    viewModel: AppModeViewModel,
    hazeState: HazeState,
    agreement: AnnotatedString,
    context: Context
) {
    var showPrivacy by remember { mutableStateOf(value = viewModel.appMode.value == AppMode.NONE) }
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(value = null) }
    if (showPrivacy) {
        BaseDialog(
            hazeState = hazeState,
            title = stringResource(id = R.string.welcome),
            content = {
                Text(
                    text = agreement,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(key1 = Unit) {
                            detectTapGestures { offset ->
                                layoutResult?.let { layout ->
                                    val p = layout.getOffsetForPosition(position = offset)
                                    agreement.getStringAnnotations(p, p).firstOrNull()?.let { a ->
                                            openPolicy(context, annotation = a)
                                        }
                                }
                            }
                        },
                    color = MaterialTheme.uotanColors.onBackgroundSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400,
                    onTextLayout = { layoutResult = it }
                )
            },
            leftButtonText = stringResource(R.string.basic_browsing_mode),
            rightButtonText = stringResource(id = R.string.agree),
            onLeftButtonClick = {
                viewModel.updateMode(newMode = AppMode.BASIC)
                directStart(context)
            },
            onRightButtonClick = {
                viewModel.updateMode(newMode = AppMode.ALL)
            }
        )
    }
}

private fun openPolicy(
    context: Context,
    annotation: AnnotatedString.Range<String>
) {
    context.startActivity(
        Intent(context, PolicyActivity::class.java).apply {
            when (annotation.tag) {
                "user" -> {
                    putExtra("type", 1)
                    putExtra("url", "$baseUrl/help/yhgy/")
                }
                "privacy" -> {
                    putExtra("type", 4)
                    putExtra("url", "$baseUrl/help/privacy-policy/")
                }
                "disclaimer" -> {
                    putExtra("type", 5)
                    putExtra("url", "$baseUrl/help/mzsm/")
                }
            }
        }
    )
}

private fun directStart(context: Context) {
    context.startActivity(
        Intent(
            context,
            MainActivity::class.java
        )
    )
    (context as Activity).finish()
}
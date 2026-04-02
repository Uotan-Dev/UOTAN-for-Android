package com.uotan.forum.welcome.ui.composable

import android.content.Intent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.uotan.forum.R
import com.uotan.forum.main.ui.MainActivity
import com.uotan.forum.ui.theme.button.filledButtonColors
import com.uotan.forum.ui.theme.button.filledTonalButtonColors
import com.uotan.forum.ui.theme.button.textButtonColors
import com.uotan.forum.ui.theme.text.TypewriterText
import com.uotan.forum.ui.theme.text.buttonBasicTextStyle
import com.uotan.forum.ui.theme.uotanColors
import com.uotan.forum.utils.Utils.baseUrl
import com.uotan.forum.utils.Utils.openUrlInBrowser
import com.uotan.forum.welcome.ui.NavSealed.Login
import com.kyant.capsule.ContinuousRoundedRectangle

@Composable
fun UotanSlogan(modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxWidth()
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
fun InitButton(modifier: Modifier, navController: NavController) {
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Button(
            modifier = Modifier.fillMaxWidth()
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
            modifier = Modifier.fillMaxWidth()
                .padding(top = 20.dp)
                .height(48.dp),
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
                context.startActivity(
                    Intent(
                        context,
                        MainActivity::class.java
                    )
                )
            }
        ) { Text(text = stringResource(R.string.no_login)) }
    }
}

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        UotanSlogan(Modifier.padding(top = 156.dp))
        InitButton(Modifier.padding(bottom = 36.dp), navController)
    }
}

@Preview(locale = "ja")
@Composable
fun WelcomePagePreview() {
    val navController = rememberNavController()
    WelcomeNavHost(navController)
}
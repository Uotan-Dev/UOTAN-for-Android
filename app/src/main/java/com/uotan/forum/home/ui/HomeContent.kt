package com.uotan.forum.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.uotan.forum.ui.theme.UotanTheme
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.rememberHazeState

@Preview
@Composable
fun HomeScreen() {
    val hazeState = rememberHazeState()
    UotanTheme {
        Box(
            modifier = Modifier
                .hazeEffect()
                .hazeEffect(hazeState) {
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 1f, endIntensity = 0f)
                }
        ) {

        }
    }
}
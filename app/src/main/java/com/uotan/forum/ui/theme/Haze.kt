package com.uotan.forum.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint

@Composable
fun topBarHazeStyle(): HazeStyle = baseHazeStyle(
    blurRadius = 18.dp,
    containerColor = MaterialTheme.uotanColors.background,
    lightAlpha = 0.36f,
    darkAlpha = 0.24f
)

@Composable
fun cardHazeStyle(): HazeStyle = baseHazeStyle(
    containerColor = MaterialTheme.uotanColors.card,
    lightAlpha = 0.64f,
    darkAlpha = 0.58f
)

@Composable
fun baseHazeStyle(
    blurRadius: Dp = 24.dp,
    containerColor: Color =
        MaterialTheme.uotanColors.dialog,
    lightAlpha: Float= 0.36f,
    darkAlpha: Float = 0.48f,
): HazeStyle = HazeStyle(
    blurRadius = blurRadius,
    backgroundColor = containerColor,
    tint = HazeTint(
        color = containerColor.copy(
            alpha =
                if (isSystemInDarkTheme()) lightAlpha
                else darkAlpha
        )
    ),
)
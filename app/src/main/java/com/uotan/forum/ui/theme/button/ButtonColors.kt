package com.uotan.forum.ui.theme.button

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.uotan.forum.ui.theme.uotanColors

@Composable
fun filledButtonColors() = ButtonColors(
    containerColor = MaterialTheme.uotanColors.filledButton,
    contentColor = MaterialTheme.uotanColors.onFilledButton,
    disabledContainerColor = MaterialTheme.uotanColors.filledButton.copy(alpha = 0.4f),
    disabledContentColor = MaterialTheme.uotanColors.onFilledButton.copy(alpha = 0.4f)
)

@Composable
fun filledTonalButtonColors() = ButtonColors(
    containerColor = MaterialTheme.uotanColors.filledTonalButton,
    contentColor = MaterialTheme.uotanColors.onFilledTonalButton,
    disabledContainerColor = MaterialTheme.uotanColors.filledTonalButton.copy(alpha = 0.4f),
    disabledContentColor = MaterialTheme.uotanColors.onFilledTonalButton.copy(alpha = 0.4f)
)

@Composable
fun textButtonColors() = ButtonColors(
    containerColor = Color.Transparent,
    contentColor = MaterialTheme.uotanColors.onFilledTonalButton,
    disabledContainerColor = Color.Transparent,
    disabledContentColor = MaterialTheme.uotanColors.onFilledTonalButton.copy(alpha = 0.4f)
)
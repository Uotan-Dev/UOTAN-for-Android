package com.gustate.uotan.ui.theme.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gustate.uotan.ui.theme.uotanColors

@Composable
fun buttonBasicTextStyle() = TextStyle(
    fontSize = 16.sp,
    fontWeight = FontWeight.W600
)

@Composable
fun inputTextStyle() = TextStyle(
    color = MaterialTheme.uotanColors.onCardPrimary,
    fontSize = 16.sp,
    fontWeight = FontWeight.W600
)
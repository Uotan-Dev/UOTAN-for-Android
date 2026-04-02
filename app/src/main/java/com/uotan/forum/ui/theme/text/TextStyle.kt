package com.uotan.forum.ui.theme.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.uotan.forum.ui.theme.uotanColors

@Composable
fun collapsedHeaderTextStyle() = TextStyle(
    color = MaterialTheme.uotanColors.onBackgroundPrimary,
    fontSize = 18.sp,
    fontWeight = FontWeight.W600
)

@Composable
fun expandedHeaderTextStyle() = TextStyle(
    color = MaterialTheme.uotanColors.onBackgroundPrimary,
    fontSize = 28.sp,
    fontWeight = FontWeight.W800
)

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

@Composable
fun describeTextStyle() = TextStyle(
    color = MaterialTheme.uotanColors.onBackgroundSecondary,
    fontSize = 12.sp,
    fontWeight = FontWeight.W400
)
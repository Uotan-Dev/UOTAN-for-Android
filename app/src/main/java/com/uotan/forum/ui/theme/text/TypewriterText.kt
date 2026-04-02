package com.uotan.forum.ui.theme.text

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Black,
    duration: Long = 4000,
    textVisibleDuration: Long = 8000,
    showCursor: Boolean = true,
    cursorColor: Color = Color.Black,
    cursorBlinkDuration: Long = 360,
    repeatCount: Int = Int.MAX_VALUE // 默认无限循环
) {
    var currentText by remember { mutableStateOf("") }
    var isCursorVisible by remember { mutableStateOf(true) }

    // 文本动画
    LaunchedEffect(text, repeatCount) {
        if (text.isEmpty()) return@LaunchedEffect
        var currentLoop = 0
        while (currentLoop < repeatCount) {
            text.forEachIndexed { index, _ ->
                currentText = text.substring(0, index + 1)
                delay(duration / text.length)
            }
            delay(textVisibleDuration)
            currentLoop++
        }
    }

    // 光标闪烁（仅在showCursor时启用）
    LaunchedEffect(showCursor) {
        if (!showCursor) return@LaunchedEffect
        while (true) {
            isCursorVisible = !isCursorVisible
            delay(cursorBlinkDuration)
        }
    }

    Text(
        text = buildAnnotatedString {
            append(currentText)
            if (showCursor && isCursorVisible) {
                withStyle(SpanStyle(color = cursorColor)) {
                    append("|")
                }
            }
        },
        style = style,
        color = color,
        modifier = modifier
    )
}

@Preview
@Composable
fun TypewriterTextPreview() {
    TypewriterText("1111111111111111111111111111111111")
}
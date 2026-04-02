package com.uotan.forum.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.uotan.forum.ui.theme.text.inputTextStyle

import com.uotan.forum.ui.theme.uotanColors
import com.kyant.capsule.ContinuousRoundedRectangle

/**
 * @param hint: 空字符时的提示
 * @param startIcon: 左侧图标;
 */
@Composable
fun Input(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    hint: String,
    hintColor: Color = MaterialTheme.uotanColors.onCardSecondary,
    startIcon: Painter? = null,
    iconMargin: Dp = 16.dp,
    contentMargin: Dp = 12.dp,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = inputTextStyle(),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    cursorBrush: Brush = SolidColor(MaterialTheme.uotanColors.onFilledTonalButton)
) {
    // 是否有焦点（控制右侧按钮）
    var hasFocus by remember { mutableStateOf(false) }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.onFocusChanged { hasFocus = it.isFocused },
        singleLine = true,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        cursorBrush = cursorBrush,
        decorationBox = @Composable { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .height(60.dp)
                    .background(
                        color = MaterialTheme.uotanColors.card,
                        shape = ContinuousRoundedRectangle(16.dp)
                    ).border(
                        width = if (hasFocus) 2.dp else 0.dp,
                        color = if (hasFocus) MaterialTheme.uotanColors.onFilledTonalButton
                        else Color.Transparent,
                        shape = ContinuousRoundedRectangle(16.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧 Icon
                if(startIcon != null){
                    Spacer(modifier = Modifier.width(iconMargin))
                    Image(painter = startIcon, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(contentMargin))
                Box(modifier = Modifier.weight(1f)){
                    // 当空字符时, 显示hint
                    if(value.isEmpty())
                        Text(text = hint, color = hintColor, style = textStyle)
                    // 原本输入框的内容
                    innerTextField()
                }
                Spacer(modifier = Modifier.width(contentMargin))
                // 存在焦点 且 有输入内容时. 显示叉号
                if(hasFocus && value.isNotEmpty()) {
                    Image(
                        imageVector = Icons.Rounded.Clear, // 清除图标
                        contentDescription = null,
                        modifier = Modifier.clickable { onValueChange("") }
                    )
                }
                Spacer(modifier = Modifier.width(iconMargin))
            }
        }
    )
}

fun pwKeyboardOptions() = KeyboardOptions.Default.copy(
    capitalization = KeyboardCapitalization.None,
    keyboardType = KeyboardType.Password
)


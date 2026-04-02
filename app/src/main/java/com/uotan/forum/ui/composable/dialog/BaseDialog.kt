package com.uotan.forum.ui.composable.dialog

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.uotan.forum.R
import com.uotan.forum.ui.theme.baseHazeStyle
import com.uotan.forum.ui.theme.uotanColors
import com.kyant.capsule.ContinuousRoundedRectangle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.rememberHazeState

// 背景圆角半径
val bkgCornerRadius = 36.dp
// 按钮圆角半径
val btnCornerRadius = 99.dp

@Composable
fun BaseDialog(
    title: String = stringResource(id = R.string.title),
    description: String? = null,
    content: (@Composable () -> Unit)? = null,
    leftButtonText: String = stringResource(id = R.string.cancel),
    rightButtonText: String = stringResource(id = R.string.ok),
    hazeState: HazeState,
    onDismissRequest: () -> Unit = {},
    onLeftButtonClick: () -> Unit = {},
    onRightButtonClick: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            modifier = Modifier
                .clip(shape = ContinuousRoundedRectangle(size = bkgCornerRadius))
                .border(
                    width = 0.6.dp,
                    color = MaterialTheme.uotanColors.dialog.copy(alpha = 0.8f),
                    shape = ContinuousRoundedRectangle(size = bkgCornerRadius)
                )
                .hazeEffect(state = hazeState, style = baseHazeStyle())
                .fillMaxWidth(),
            colors = CardColors(
                containerColor = Color.Transparent,
                contentColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.Transparent
            )
        ) {
            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 26.dp, end = 26.dp, top = 24.dp),
                color = MaterialTheme.uotanColors.onBackgroundPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.W600
            )
            description?.let { description ->
                Text(
                    text = description,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 26.dp, end = 26.dp, top = 10.dp),
                    color = MaterialTheme.uotanColors.onBackgroundSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400
                )
            }
            content?.let { content ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 26.dp, end = 26.dp, top = 10.dp),
                ) {
                    content()
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 16.dp)
            ) {
                DialogButton(
                    modifier = Modifier.weight(weight = 1f),
                    text = leftButtonText,
                    containerColor = MaterialTheme.uotanColors.filledTonalButton,
                    contentColor = MaterialTheme.uotanColors.onFilledTonalButton,
                    onClick = onLeftButtonClick
                )
                Spacer(modifier = Modifier.width(width = 8.dp))
                DialogButton(
                    modifier = Modifier.weight(weight = 1f),
                    text = rightButtonText,
                    containerColor = MaterialTheme.uotanColors.filledButton,
                    contentColor = MaterialTheme.uotanColors.onFilledButton,
                    onClick = onRightButtonClick
                )
            }
        }
    }
}

@Composable
private fun DialogButton(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit = {},
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = ContinuousRoundedRectangle(size = btnCornerRadius),
        colors = ButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        )
    ) {
        Text(
            text = text,
            fontSize = 14.sp
        )
    }
}

@Preview
@Composable
fun BaseDialogPreview() {
    BaseDialog(
        title = "tit",
        description = "des",
        hazeState = rememberHazeState(),
        onDismissRequest = {

        }
    )
}
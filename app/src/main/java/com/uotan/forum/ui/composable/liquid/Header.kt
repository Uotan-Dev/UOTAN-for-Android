package com.uotan.forum.ui.composable.liquid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.uotan.forum.R
import com.uotan.forum.ui.theme.text.collapsedHeaderTextStyle
import com.uotan.forum.ui.theme.uotanColors
import com.uotan.forum.utils.Utils.pxToDp
import com.kyant.backdrop.Backdrop
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect

@Composable
fun ClassicHeader(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    backdrop: Backdrop,
    buttenSize: Dp = 42.dp,
    enableLeftButton: Boolean = true,
    onLeftButtonClick: (() -> Unit)? = null,
    title: String = stringResource(R.string.title),
    enableRightButton: Boolean = false,
    onRightButtonClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp +
                        (WindowInsets.systemBars.getTop(density).toFloat().pxToDp(context)).dp)
                .hazeEffect(hazeState) {
                    progressive = HazeProgressive
                        .verticalGradient(startIntensity = 1f, endIntensity = 0f)
                }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = 60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CircleIconButton(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .alpha(alpha = if (enableLeftButton) 1f else 0f),
                backdrop = backdrop,
                size = buttenSize,
                imageVector = Icons.Rounded.ArrowBackIosNew,
                tint = MaterialTheme.uotanColors.onBackgroundPrimary,
                onClick = { onLeftButtonClick?.invoke() }
            )
            Text(
                text = title,
                style = collapsedHeaderTextStyle()
            )
            CircleIconButton(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .alpha(alpha = if (enableRightButton) 1f else 0f),
                backdrop = backdrop,
                size = buttenSize,
                imageVector = Icons.Rounded.IosShare,
                tint = MaterialTheme.uotanColors.onBackgroundPrimary,
                onClick = { onRightButtonClick?.invoke() }
            )
        }
    }
}
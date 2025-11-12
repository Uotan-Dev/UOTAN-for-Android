package com.gustate.uotan.ui.composable.liquid

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.gustate.uotan.R
import com.gustate.uotan.ui.theme.uotanColors
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.refraction
import com.kyant.backdrop.effects.vibrancy
import com.kyant.capsule.ContinuousRoundedRectangle
import kotlinx.coroutines.launch

@Composable
fun RoundedIconButton(
    modifier: Modifier = Modifier,
    backdrop: Backdrop,
    size: Dp = 48.dp,
    radius: Float = 16f,
    painter: Painter? = painterResource(R.drawable.ic_qq),
    tint: Color = MaterialTheme.uotanColors.onFilledTonalButton,
    onClick: () -> Unit,
) {
    RoundedIconButton(
        modifier, backdrop, size, radius, painter, imageVector = null, tint, onClick)
}

@Composable
fun RoundedIconButton(
    modifier: Modifier = Modifier,
    backdrop: Backdrop,
    size: Dp = 48.dp,
    radius: Float = 16f,
    imageVector: ImageVector? = Icons.Rounded.ArrowBackIosNew,
    tint: Color = MaterialTheme.uotanColors.onFilledTonalButton,
    onClick: () -> Unit,
) {
    RoundedIconButton(
        modifier, backdrop, size, radius, painter = null, imageVector, tint, onClick)
}

@Composable
private fun RoundedIconButton(
    modifier: Modifier = Modifier,
    backdrop: Backdrop,
    size: Dp = 48.dp,
    radius: Float = 16f,
    painter: Painter? = painterResource(R.drawable.ic_qq),
    imageVector: ImageVector? = Icons.Rounded.ArrowBackIosNew,
    tint: Color = MaterialTheme.uotanColors.onFilledTonalButton,
    onClick: () -> Unit,
) {
    val animationScope = rememberCoroutineScope()
    val progressAnimation = remember { Animatable(0f) }
    Box(
        modifier
            .graphicsLayer {
                val progress = progressAnimation.value
                val scale = lerp(1f, 1.1f, progress)
                scaleX = scale
                scaleY = scale
            }.drawBackdrop(
                backdrop = backdrop,
                shape = { ContinuousRoundedRectangle(radius.dp) },
                effects = {
                    vibrancy()
                    blur(16f.dp.toPx())
                    refraction(
                        height = 24f.dp.toPx(),
                        amount = 48f.dp.toPx(),
                        hasDepthEffect = true
                    )
                }, layerBlock = {
                    val progress = progressAnimation.value
                    val scale = lerp(1f, 1.1f, progress)
                    scaleX = scale
                    scaleY = scale
                }, contentEffects = {
                    blur(4f.dp.toPx())
                    refraction(8f.dp.toPx(), 24f.dp.toPx())
                }
            ).clickable { onClick() }.pointerInput(animationScope) {
                val animationSpec = spring(0.5f, 300f, 0.001f)
                awaitEachGesture {
                    // press
                    awaitFirstDown()
                    animationScope.launch {
                        progressAnimation.animateTo(1f, animationSpec)
                    }
                    // release
                    waitForUpOrCancellation()
                    animationScope.launch {
                        progressAnimation.animateTo(0f, animationSpec)
                    }
                }
            }.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (imageVector != null) {
            Icon(
                modifier = Modifier.size(size * 0.6f).alpha(0.3f),
                imageVector = imageVector,
                contentDescription = null,
                tint = tint
            )
        } else if (painter != null) {
            Icon(
                modifier = Modifier.size(size * 0.6f).alpha(0.3f),
                painter = painter,
                contentDescription = null,
                tint = tint
            )
        }
    }
}

@Preview
@Composable
fun RoundedIconButtonPreview() {
    val backdrop = rememberLayerBackdrop()
    RoundedIconButton(
        backdrop = backdrop,
        painter = painterResource(R.drawable.ic_qq),
        onClick = {}
    )
}
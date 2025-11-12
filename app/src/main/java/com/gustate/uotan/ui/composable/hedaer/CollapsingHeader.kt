package com.gustate.uotan.ui.composable.hedaer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gustate.uotan.R
import com.gustate.uotan.ui.composable.liquid.CircleIconButton
import com.gustate.uotan.ui.theme.text.collapsedHeaderTextStyle
import com.gustate.uotan.ui.theme.uotanColors
import com.gustate.uotan.utils.Utils.pxToDp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.capsule.ContinuousRoundedRectangle
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun CollapsingTabHeader(
    modifier: Modifier = Modifier,
    hazeState: HazeState = rememberHazeState(),
    backdrop: Backdrop = rememberLayerBackdrop(),
    collapseHeight: Dp = 60.dp,
    expandedHeight: Dp = 116.dp,
    buttonSize: Dp = 42.dp,
    enableLeftButton: Boolean = true,
    onLeftButtonClick: (() -> Unit)? = null,
    enableRightButton: Boolean = false,
    onRightButtonClick: (() -> Unit)? = null,
    tabs: List<String> = listOf(stringResource(R.string.app_name), stringResource(R.string.title)),
    selectedTabIndex: Int = 0,
    onTabChange: ((Int) -> Unit)? = null,
    tabsCollapsedAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    tabsExpandedAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    Box(modifier = modifier) {
        // 渐进模糊背景
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    60.dp +
                            (WindowInsets.systemBars.getTop(density).toFloat().pxToDp(context)).dp
                )
                .hazeEffect(hazeState) {
                    progressive = HazeProgressive
                        .verticalGradient(startIntensity = 1f, endIntensity = 0f)
                }
        )
        // 标题栏内容
        Column {
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
                    size = buttonSize,
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    tint = MaterialTheme.uotanColors.onBackgroundPrimary,
                    onClick = { onLeftButtonClick?.invoke() }
                )

                CircleIconButton(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .alpha(alpha = if (enableRightButton) 1f else 0f),
                    backdrop = backdrop,
                    size = buttonSize,
                    imageVector = Icons.Rounded.IosShare,
                    tint = MaterialTheme.uotanColors.onBackgroundPrimary,
                    onClick = { onRightButtonClick?.invoke() }
                )
            }
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                edgePadding = 12.dp,
                indicator = {
                    Spacer(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.uotanColors.onFilledTonalButton,
                                shape = ContinuousRoundedRectangle(99.dp)
                            )
                            .height(2f.dp)
                            .fillMaxWidth()
                    )
                },
                divider = { },
                minTabWidth = 60.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = index == selectedTabIndex,
                        onClick = { onTabChange?.invoke(index) },
                        selectedContentColor = MaterialTheme.uotanColors.onBackgroundPrimary,
                        unselectedContentColor = MaterialTheme.uotanColors.onBackgroundSecondary
                    ) {
                        Text(
                            text = tab,
                            style = collapsedHeaderTextStyle()
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CollapsingTabHeaderPreview() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(100) {
            Text(it.toString())
        }
    }
    CollapsingTabHeader()
}
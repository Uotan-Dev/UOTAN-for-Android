package com.uotan.forum.main.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uotan.forum.R
import com.uotan.forum.main.Navigate
import com.uotan.forum.ui.theme.baseHazeStyle
import com.uotan.forum.ui.theme.uotanColors
import com.kyant.capsule.ContinuousRoundedRectangle
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch

@Preview
@Composable
fun MainScreen() {
    val pagerState =
        rememberPagerState(initialPage = 0) { Navigate.entries.size }
    val scope = rememberCoroutineScope()
    val hazeState = rememberHazeState()
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        HorizontalPager(
            state = pagerState
        ) {

        }
        Image(
            painter = painterResource(id = R.drawable.mjw),
            contentDescription = "MJW",
            modifier = Modifier
                .hazeSource(hazeState)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.FillWidth
        )
        NavigationBar(
            modifier = Modifier
                .hazeEffect(
                    state = hazeState,
                    style = baseHazeStyle()
                ) {
                    progressive = HazeProgressive
                        .verticalGradient(startIntensity = 0f, endIntensity = 1f)
                }
                .align(Alignment.BottomCenter),
            paddingValues = PaddingValues(bottom = 18.dp)
        ) {
            Navigate.entries.forEachIndexed { index, navigate ->
                val isSelected = pagerState.currentPage == index
                NavigationBarItem(
                    selected = isSelected,
                    icon = painterResource(id = navigate.icon),
                    contentDestination = stringResource(id = navigate.label),
                    label = stringResource(id = navigate.label)
                ) {
                    scope.launch {
                        pagerState.animateScrollToPage(page = index)
                    }
                }
            }
        }
    }
}

@Composable
fun NavigationBar(
    modifier: Modifier,
    paddingValues: PaddingValues,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = paddingValues.calculateBottomPadding())
                .fillMaxWidth()
                .height(height = 65.dp)
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Composable
fun RowScope.NavigationBarItem(
    selected: Boolean = false,
    icon: Painter,
    contentDestination: String,
    label: String,
    onClick: () -> Unit
) {
    val itemTint =
        if (selected) MaterialTheme.uotanColors.onBackgroundPrimary
        else MaterialTheme.uotanColors.onBackgroundSecondary
    Box(
        modifier = Modifier
            .weight(weight = 1f)
            .padding(vertical = 6.dp)
            .clip(shape = ContinuousRoundedRectangle(size = 12.dp))
            .clickable(
                enabled = true,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = contentDestination,
                tint = itemTint
            )
            Text(
                text = label,
                modifier = Modifier
                    .padding(top = 2.dp),
                fontSize = 10.sp,
                color = itemTint
            )
        }
    }
}
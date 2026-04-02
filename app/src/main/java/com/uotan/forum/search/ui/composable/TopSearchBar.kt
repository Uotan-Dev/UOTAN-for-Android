package com.uotan.forum.search.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uotan.forum.R
import com.uotan.forum.ui.theme.cardHazeStyle
import com.uotan.forum.ui.theme.topBarHazeStyle
import com.uotan.forum.ui.theme.uotanColors
import com.kyant.capsule.ContinuousRoundedRectangle
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun TopSearchBar(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    paddingValues: PaddingValues,
    searchContent: String = "",
    selectedTabIndex: Int = 0,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .hazeSource(state = hazeState, zIndex = 1f)
            .hazeEffect(state = hazeState, style = topBarHazeStyle()) {
                progressive = HazeProgressive.verticalGradient(
                    startIntensity = 1f,
                    endIntensity = 0f
                )
            }
    ) {
        Column(
            modifier = Modifier
                .padding(
                    vertical = paddingValues.calculateTopPadding(),
                    horizontal = 12.dp
                )
        ) {
            Row(
                modifier = Modifier
                    .height(height = 48.dp)
                    .fillMaxWidth()
                    .clip(shape = ContinuousRoundedRectangle(size = 16.dp))
                    .hazeEffect(state = hazeState, style = cardHazeStyle())
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    modifier = Modifier
                        .padding(end = 12.dp),
                    contentDescription = stringResource(id = R.string.search),
                    tint = MaterialTheme.uotanColors.onBackgroundSecondary
                )
                Text(
                    text = searchContent.ifEmpty {
                        stringResource(id = R.string.search_all)
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    color =
                        if (searchContent.isNotEmpty())
                            MaterialTheme.uotanColors.onBackgroundPrimary
                        else
                            MaterialTheme.uotanColors.onBackgroundSecondary,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            SecondaryScrollableTabRow(
                selectedTabIndex = selectedTabIndex
            ) {

            }
        }
    }
}

@Preview
@Composable
fun TopSearchBarPreview() {
    val hazeState = rememberHazeState()
    val paddingValues = PaddingValues(top = 12.dp)
    Image(
        painter = painterResource(id = R.drawable.mjw),
        contentDescription = "MJW",
        modifier = Modifier
            .fillMaxWidth()
            .hazeSource(
                state = hazeState,
                zIndex = 0f
            ),
        contentScale = ContentScale.FillWidth
    )
    TopSearchBar(
        modifier = Modifier,
        hazeState = hazeState,
        paddingValues = paddingValues,
        searchContent = "MJW 是猫娘！我是流浪者的狗！我是基尼奇的狗！"
    )
}
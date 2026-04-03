package com.uotan.forum.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.uotan.forum.R
import com.uotan.forum.ui.theme.uotanColors

@Composable
fun UotanLogo(
    modifier: Modifier = Modifier,
    height: Dp = 24.dp,
) {
    val progress = height / 20.83.dp
    val gap = progress * 3.dp
    Row(
        modifier = modifier
            .height(height = height)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_uo),
            contentDescription = stringResource(id = R.string.app_name),
            modifier = Modifier
                .fillMaxHeight(),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(color = MaterialTheme.uotanColors.filledButton)
        )
        Spacer(
            modifier = Modifier
                .width(width = gap)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_tan),
            contentDescription = stringResource(id = R.string.app_name),
            modifier = Modifier
                .fillMaxHeight(),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(color = MaterialTheme.uotanColors.onBackgroundPrimary)
        )
    }
}
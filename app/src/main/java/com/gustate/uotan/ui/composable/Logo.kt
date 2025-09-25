package com.gustate.uotan.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gustate.uotan.R


@Composable
fun Logo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.ic_uotan),
        contentDescription = stringResource(R.string.app_name),
        modifier = modifier.height(24.dp)
    )
}
package com.gustate.uotan.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gustate.uotan.ui.theme.uotanColors

@Composable
fun RoundedCheckBox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    uncheckedColor: Color = MaterialTheme.uotanColors.filledTonalButton,
    checkedColor: Color = MaterialTheme.uotanColors.filledButton,
    checkmarkColor: Color = MaterialTheme.uotanColors.onFilledButton
) {
    Box(
        modifier = modifier
            .size(size)
            .padding(size * 0.12f)
            .clip(CircleShape)
            .background(if (checked) checkedColor else uncheckedColor)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = checkmarkColor,
            modifier = Modifier.size(size * 0.64f)
        )
    }
}

@Preview
@Composable
fun RoundedCheckBoxPreview() {
    var isChecked by remember { mutableStateOf(false) }
    RoundedCheckBox(
        checked = isChecked,
        onCheckedChange = {
            isChecked = it
        }
    )
}
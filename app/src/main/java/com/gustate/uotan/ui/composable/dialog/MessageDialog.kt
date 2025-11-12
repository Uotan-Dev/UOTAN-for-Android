package com.gustate.uotan.ui.composable.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.hazeEffect

/*@Composable
fun DialogSample() {
    var showDialog = false
    Dialog(onDismissRequest = { showDialog = false }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = .5f),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Box(
                Modifier.hazeEffect(state = hazeState, style = HazeMaterials.regular())
            ) {
            // empty
            }
        }
    }
}*/
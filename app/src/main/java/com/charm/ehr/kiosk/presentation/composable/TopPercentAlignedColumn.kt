package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TopPercentAlignedColumn(
    topPercent: Float = 0.4f,
    modifier: Modifier = Modifier,
    composeContent: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.weight(topPercent))
        composeContent()
        Spacer(modifier = Modifier.weight(1f - topPercent))
    }
}
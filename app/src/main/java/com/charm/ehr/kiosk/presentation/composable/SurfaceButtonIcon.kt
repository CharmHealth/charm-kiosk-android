package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.presentation.theme.MdColors

@Composable
fun SurfaceButtonIcon(modifier: Modifier, iconPainter: Painter, iconSize: Dp = 24.dp) {
    Box(
        modifier = modifier
            .background(color = MdColors.BrandLightPinkVariant, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = "Add Icon",
            tint = MdColors.BrandPink,
            modifier = Modifier.size(iconSize)
        )
    }
}


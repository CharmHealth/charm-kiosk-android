package com.charm.ehr.kiosk.presentation.composable.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.presentation.theme.MdColors

@Composable
fun MdCircularImageView(
    modifier: Modifier = Modifier.padding(0.dp),
    imageResource: Int, // Use resource ID for simplicity
    contentDescription: String? = null,
    size: Dp = 50.dp, // Customizable size
    border: Dp = 0.dp, // Optional border size
    borderColor: androidx.compose.ui.graphics.Color = MdColors.Transparent, // Customizable border color
) {
    Image(
        painter = painterResource(id = imageResource),
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(width = border, color = borderColor)
            .aspectRatio(1f)
    )
}
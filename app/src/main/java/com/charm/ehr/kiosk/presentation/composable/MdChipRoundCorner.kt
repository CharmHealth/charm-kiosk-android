package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun MdChipRoundCorner(
    text: String,
    isSelected: Boolean,
    onChipClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = if (isSelected) MdColors.BrandLightPinkVariant else Color.Transparent,
        contentColor = if (isSelected) MdColors.BrandPink else Color.Black,
        border = BorderStroke(
            width = 0.5.dp,
            color = if (isSelected) MdColors.BrandPink else MdColors.Black10
        ),
        modifier = Modifier.clip(CircleShape).clickable(onClick = onChipClick),

        ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MdTheme.typography.regular14TextStyle,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MdChipRoundCornerPreview() {
    MdChipRoundCorner(
        text = "Sample Chip",
        isSelected = true,
        onChipClick = {}
    )
}


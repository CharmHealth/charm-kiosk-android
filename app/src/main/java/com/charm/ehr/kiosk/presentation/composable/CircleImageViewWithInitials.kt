package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun CircleImageViewWithInitials(
    initial: String,
    size: Dp = 88.dp,
    style: TextStyle = MdTheme.typography.medium18TextStyle
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(color = MdColors.MildPinkishGrey), contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial, style = style, color = MdColors.DeepGrey
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CircleImageViewWithInitialsPreview() {
    MdTheme {
        CircleImageViewWithInitials(
            initial = "AB",
            size = 50.dp,
            style = MdTheme.typography.bold24TextStyle
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CircleImageViewWithInitialsMultiSizePreview() {
    MdTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircleImageViewWithInitials(initial = "JP", size = 60.dp, style = MdTheme.typography.medium20TextStyle)
            CircleImageViewWithInitials(initial = "QA", size = 48.dp, style = MdTheme.typography.regular20TextStyle)
            CircleImageViewWithInitials(initial = "MC", size = 48.dp, style = MdTheme.typography.bold22TextStyle)
        }
    }
}

package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun CharmKioskLogoHeader(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    showBackPress: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (showBackPress) {
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_down),
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier.rotate(90f)
                )
            }
        } else {
            // Empty space to keep logo centered if back button is not shown
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier
                    .size(48.dp)
                    .padding(top = 32.dp)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .height(48.dp),
                painter = painterResource(id = R.drawable.ic_charm_health),
                contentDescription = "Charm logo",
                tint = Color.Unspecified
            )
        }
        androidx.compose.foundation.layout.Spacer(
            modifier = Modifier
                .size(48.dp)
                .padding(top = 32.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewCharmKioskLogoHeaderWithBack() {
    MdTheme{
        CharmKioskLogoHeader(
            modifier = Modifier,
            onBackPressed = {},
            showBackPress = true
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewCharmKioskLogoHeaderWithoutBack() {
    MdTheme() {
        CharmKioskLogoHeader(
            modifier = Modifier,
            onBackPressed = {},
            showBackPress = false
        )
    }
}

package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun BottomBarWithPrimarySecondaryButton(
    modifier: Modifier = Modifier,
    onPrimaryClick: () -> Unit,
    primaryButtonText: String,
    secondaryButtonText: String = "",
    onSecondaryClick: () -> Unit = {},
    showSecondary: Boolean = false
) {
    MdCardWithoutTonalElevation(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White), elevation = 16.dp, shape = RectangleShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showSecondary) {
                // Secondary Button (left)
                OutlinedButton(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(32.dp),
                    onClick = onSecondaryClick
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = secondaryButtonText,
                        style = MdTheme.typography.semiBold16TextStyle.copy(
                            color = MdColors.BrandPink
                        )
                    )
                }
            } else {
                // To keep primary button right-aligned when secondary is hidden
                Spacer(modifier = Modifier)
            }
            // Primary Button (right)
            MdCommonPrimaryButton(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    text = primaryButtonText,
                onClick = onPrimaryClick
                    )
        }
    }
}


@Preview(showBackground = true, widthDp = 400)
@Composable
fun PreviewBottomBarWithPrimarySecondaryButton() {
    MdTheme {
        BottomBarWithPrimarySecondaryButton(
            primaryButtonText = "Primary",
            onPrimaryClick = {},
            secondaryButtonText = "Secondary",
            onSecondaryClick = {},
            showSecondary = false
        )
    }
}

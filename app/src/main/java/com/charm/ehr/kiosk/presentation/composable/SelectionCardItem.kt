package com.charm.ehr.kiosk.presentation.composable


import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun SelectionCardItem(
    selected: Boolean,
    heading: String,
    message: String?,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MdColors.BrandPink else MdColors.LightBlueGray
    val backgroundColor = if (selected) MdColors.BrandLightPinkVariant else MdColors.White

    MdCardWithoutTonalElevation(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                1.dp, borderColor,
                RoundedCornerShape(MdTheme.dimension.cardCornerRadius)
            ),
        shape = RoundedCornerShape(MdTheme.dimension.cardCornerRadius),
        colors = backgroundColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(18.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = heading,
                    style = MdTheme.typography.semiBold16TextStyle
                )
                if (!message.isNullOrBlank()) {
                    Text(
                        text = message,
                        style = MdTheme.typography.regular14TextStyle,
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewSelectionCardItemSelected() {
    MdTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            SelectionCardItem(
                selected = true,
                heading = "Selected Option",
                message = "This is a description for the selected option",
                onClick = {}
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewSelectionCardItemUnselected() {
    MdTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            SelectionCardItem(
                selected = false,
                heading = "Unselected Option",
                message = "This is a description for the unselected option",
                onClick = {}
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewSelectionCardItemWithoutMessage() {
    MdTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            SelectionCardItem(
                selected = true,
                heading = "Option without message",
                message = null,
                onClick = {}
            )
        }
    }
}


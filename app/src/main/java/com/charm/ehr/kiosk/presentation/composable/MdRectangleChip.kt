package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun MdRectangleChipWithClose(
    text: String,
    onChipClick: () -> Unit,
    cornerRadius: Dp = 6.dp,
    textStyle: TextStyle,
    chipColor: Color = MdColors.BrandLightPinkVariant,
    contentColor: Color = MdColors.Black90,
    closeIconTint: Color = MdColors.Black60,
    verticalPadding: Dp = 4.dp,
    horizontalPadding: Dp = 8.dp,
    closeIconSize: Dp = 16.dp,
    closeIconStartPadding: Dp = 8.dp,
    hideIcon: Boolean
) {
    val modifier = if (hideIcon) Modifier.wrapContentWidth() else Modifier
        .wrapContentWidth()
        .clickable(onClick = onChipClick)
    Surface(
        modifier = modifier,
        color = chipColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(cornerRadius),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(
                    vertical = verticalPadding,
                    horizontal = horizontalPadding
                )
                .wrapContentWidth(),
        ) {
            Text(
                modifier = Modifier,
                maxLines = Int.MAX_VALUE,
                softWrap = true,
                text = text,
                textAlign = TextAlign.Left,
                style = textStyle
            )

            if (!hideIcon) {
                Icon(
                    modifier = Modifier
                        .padding(start = closeIconStartPadding)
                        .size(closeIconSize)
                        .clickable(onClick = onChipClick),
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Close",
                    tint = closeIconTint
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MdRectangleChipWithClosePreview() {
    Row(Modifier.width(2000.dp)) {
        MdRectangleChipWithClose(
            text = "Sample Chip asdfasdf asdfasdf asdfasdf asdfasdf",
            onChipClick = {},
            textStyle = MdTheme.typography.medium16TextStyle,
            chipColor = MdColors.BrandLightPinkVariant,
            contentColor = MdColors.Black90,
            closeIconTint = MdColors.Black60,
            verticalPadding = 4.dp,
            horizontalPadding = 8.dp,
            closeIconSize = 16.dp,
            closeIconStartPadding = 8.dp,
            hideIcon = true
        )
    }
}


package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices.PIXEL_4
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun MdCommonPrimaryButton(
    text: String,
    startIcon: @Composable (() -> Unit)? = null,
    endIcon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier,
    buttonColor: Color = MdColors.BrandPink,
    textColor: Color = Color.White,
    textStyle: TextStyle = MdTheme.typography.semiBold16TextStyle,
) {

    Button(
        modifier = modifier.heightIn(min= 46.dp),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (startIcon != null) {
                startIcon()
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Spacer(modifier = Modifier.size(10.dp))
            }
            Text(
                modifier = Modifier.padding(0.dp),
                text = text, style = textStyle,
                color = textColor, textAlign = TextAlign.Center
            )
            if (endIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                endIcon()
            } else {
                Spacer(modifier = Modifier.size(10.dp))
            }
        }
    }
}

@Preview(showBackground = true, device = PIXEL_4)
@Composable
fun GenericButtonPreview() {
    MdTheme {
        Row(modifier = Modifier.padding(8.dp)) {
            MdCommonPrimaryButton(
                text = "Click Me",
                startIcon = null,
                endIcon = {
                    Icon(
                        modifier = Modifier.rotate(270f),
                        painter = painterResource(id = R.drawable.ic_arrow_down),
                        contentDescription = "Add Icon"
                    )
                },
                onClick = { /* Do something */ },
                modifier = Modifier.wrapContentWidth()
            )
        }
    }
}

@Preview(showBackground = true, device = PIXEL_4)
@Composable
fun GenericFrontIconButtonPreview() {
    MdTheme {
        Row(modifier = Modifier.padding(8.dp)) {
            MdCommonPrimaryButton(
                text = "Click Me",
                startIcon = {
                    Icon(
                        modifier = Modifier.rotate(90f),
                        painter = painterResource(id = R.drawable.ic_arrow_down),
                        contentDescription = "Add Icon"
                    )
                },
                onClick = { /* Do something */ },
                modifier = Modifier.wrapContentWidth()
            )
        }
    }
}

@Preview(showBackground = true, device = PIXEL_4)
@Composable
fun GenericButtonWithoutIconPreview() {
    MdTheme {
        Row(modifier = Modifier.padding(8.dp)) {
            MdCommonPrimaryButton(
                text = "Click Me",
                onClick = { /* Do something */ },
                modifier = Modifier.wrapContentWidth().width(300.dp)
            )
        }
    }
}
package com.charm.ehr.kiosk.presentation.tele_health

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charm.ehr.kiosk.presentation.composable.MdHorizontalSpace
import com.charm.ehr.kiosk.presentation.composable.MdVerticalSpace
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun SideMenuItem(
    text: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                getBackgroundColor(isSelected)
            )
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = getTextStyle(isSelected),
        )
    }
}

@Composable
private fun getBackgroundColor(isSelected: Boolean): Color = if (isSelected) {
    MdColors.LightOrange
} else {
    MdColors.White
}

@Composable
private fun getTextStyle(isSelected: Boolean): TextStyle {
    return if (isSelected) {
        MdTheme.typography.semiBold16TextStyle.copy(color = MdColors.BrandPink)
    } else {
        MdTheme.typography.medium16TextStyle.copy(color = MdColors.Black86)
    }
}

@Composable
fun SideMenuHeading(text: String, textSize: TextUnit = 20.sp, showIcon: Boolean=false,
                    @DrawableRes iconResId: Int?=null) {
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .padding(start = 16.dp, end = 16.dp)
            .heightIn(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Row( verticalAlignment = Alignment.CenterVertically){
            if (showIcon && iconResId!=null) {
                Icon(
                    painter = painterResource(iconResId), contentDescription = "Edit", tint = MdColors.Black70
                )

                MdHorizontalSpace()
            }
            Text(
                text = text,
                modifier = Modifier
                    .wrapContentWidth(),
                style = MdTheme.typography.semiBold18TextStyle.copy(fontSize = textSize),
            )
        }
    }
}

@Composable
fun HeadingText(modifier:Modifier= Modifier, text: String, textSize: TextUnit = 16.sp){
 Column {
     SideMenuDetailHeading(modifier,text, textSize)
     MdVerticalSpace()
 }
}

@Composable
fun SideMenuDetailHeading(modifier:Modifier= Modifier, text: String, textSize: TextUnit = 16.sp) {
    Box(
        modifier = modifier
            .wrapContentWidth(), contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            modifier = Modifier
                .wrapContentWidth(),
            style = MdTheme.typography.semiBold18TextStyle.copy(fontSize = textSize),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
@Preview(showBackground = true)
fun EditProfileScreenPreview() {
    MdTheme {

        Column(modifier = Modifier.fillMaxWidth()) {

            SideMenuHeading(text = "Profile")
            SideMenuItem(
                "Contact information",
                isSelected = true,
                onClick = {}
            )

            SideMenuItem(
                "Personal information",
                isSelected = false,
                onClick = {}
            )
            SideMenuItem(
                "Caregiver information",
                isSelected = false,
                onClick = {}
            )

        }
    }
}







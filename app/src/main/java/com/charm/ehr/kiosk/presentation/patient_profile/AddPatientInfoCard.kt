package com.charm.ehr.kiosk.presentation.patient_profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdVerticalSpace
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun AddMissingPatientInfoCard(@DrawableRes iconResId: Int, label: String, onClick: () -> Unit) {

    // Use the theme card corner radius to keep shape consistent and clip the clickable ripple
    val cardShape = RoundedCornerShape(MdTheme.dimension.cardCornerRadius)

    MdCardWithoutTonalElevation(
        elevation = 0.dp,
        shape = cardShape,
        modifier = Modifier
            .clip(cardShape)
            .clickable {
                onClick()
            }
    ) {
        Column(
            modifier = Modifier.widthIn(min = 200.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            MdVerticalSpace(3f)

            Icon(
                painter = painterResource(iconResId),
                contentDescription = "", tint = MdColors.Black86
            )

            MdVerticalSpace(2f)

            Text(
                text = label, style = MdTheme.typography.medium16TextStyle,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            MdVerticalSpace(3.5f)

            TextButton(
                modifier = Modifier.defaultMinSize(minWidth = 96.dp),
                onClick = {
                    onClick()
                },
                colors = ButtonDefaults.textButtonColors(containerColor = MdColors.BrandLightPink)
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = stringResource(R.string.add),
                    style = MdTheme.typography.semiBold16TextStyle.copy(color = MdColors.BrandPink)
                )
            }

            MdVerticalSpace(3f)

        }
    }

}

@Composable
@Preview(showBackground = true, widthDp = 500, heightDp = 500)
fun AddPatientInfoCardPreview() {
    MdTheme {
        Box(
            modifier = Modifier
                .background(MdColors.MdSurfaceColor)
                .padding(12.dp)
                .size(500.dp)
        ) {
            AddMissingPatientInfoCard(R.drawable.ic_care, label = "Caregiver", onClick = {})
        }
    }

}
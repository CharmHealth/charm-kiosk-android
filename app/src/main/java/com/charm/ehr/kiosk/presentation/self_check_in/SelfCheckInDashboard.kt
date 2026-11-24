package com.charm.ehr.kiosk.presentation.self_check_in

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import com.charm.ehr.kiosk.domain.Facility
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.composable.MDHorizontalLine
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun SelfCheckInDashboardScreen(
    selectedFacility: Facility?,
    onStart: () -> Unit
) {
    val facilityName = selectedFacility?.facilityName ?: ""
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .clickable(onClick = onStart)
            .windowInsetsPadding(
                WindowInsets.systemBars.union(WindowInsets.displayCutout)
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_selfcheck_in_bg),
            contentDescription = "Background Image",
            modifier = Modifier
                .fillMaxSize(),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = (LocalConfiguration.current.screenHeightDp.dp * 0.07f))
        ) {
            Text(
                text = stringResource(R.string.self_check_in_here),
                style = MdTheme.typography.bold24TextStyle.copy(fontSize = 64.sp, color = MdColors.BrandPink)
            )
            Spacer(Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.welcome_to),
                style = MdTheme.typography.semiBold24TextStyle.copy(fontSize = 44.sp)
            )
            Text(
                text = facilityName,
                style = MdTheme.typography.semiBold24TextStyle.copy(fontSize = 44.sp)
            )

            Spacer(Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text =stringResource(R.string.touch_to_start),
                    style = MdTheme.typography.bold24TextStyle.copy(
                        color = MdColors.BrandPink
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.width(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_touch_icon),
                    contentDescription = "Tap Icon",
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.weight(0.15f))
            MDHorizontalLine(color = MdColors.Grey30, thickness = 1.dp, modifier = Modifier)
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.powered_by),
                    style = MdTheme.typography.regular12TextStyle.copy(
                        color = Color.Gray,
                    )
                )
                Spacer(Modifier.height(4.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_charm_health),
                    contentDescription = "charmHealth",
                    modifier = Modifier.height(20.dp)
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 700, heightDp = 1000)
fun SelfCheckInDashboardScreenPreview() {
    MdTheme {
        SelfCheckInDashboardScreen(
            selectedFacility = Facility(
                facilityId = "1",
                facilityName = "Sample Facility"
            )
        ) {}
    }
}

@Composable
fun getPreviewImageLoader(): ImageLoader {
    return ImageLoader.Builder(LocalContext.current).build()
}
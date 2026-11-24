package com.charm.ehr.kiosk.presentation.thank_you

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.charm.ehr.kiosk.domain.Facility
import com.charm.ehr.kiosk.common.util.MDAccountsRelatedUtil
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.util.UIUtil
import com.charm.ehr.kiosk.presentation.composable.MDHorizontalLine
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ThankYouScreen(
    selectedFacility: Facility?,
    imageLoader: ImageLoader,
    onFinish: () -> Unit
) {
    val facilityId = selectedFacility?.facilityId ?: ""
    val facilityName = selectedFacility?.facilityName ?: ""
    val facilityLogoUrl = MDAccountsRelatedUtil.getFacilityLogoUrl(facilityId)
    val context = LocalContext.current
    BackHandler(enabled = true) {
        onFinish()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .clickable(onClick = onFinish)
            .windowInsetsPadding(
                WindowInsets.systemBars.union(WindowInsets.displayCutout)
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f),
                contentAlignment = Alignment.TopCenter
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_thank_you_card),
                    contentDescription = "Thank You Card",
                    modifier = Modifier
                        .fillMaxSize(),
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(top = (LocalConfiguration.current.screenHeightDp.dp * 0.05f))
                ) {
                    if (facilityLogoUrl.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(facilityLogoUrl)
                                .size(UIUtil.dpToPx(context = context, dp = 64.dp))
                                .build(),
                            imageLoader = imageLoader,
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .heightIn(max = 64.dp)
                        ) {
                            val painterState = painter.state
                            if (painterState is AsyncImagePainter.State.Success) {
                                SubcomposeAsyncImageContent()
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_facility_placeholder),
                                    null,
                                    modifier = Modifier.heightIn(max = 64.dp)
                                )
                            }
                        }
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_facility_placeholder),
                            null,
                            modifier = Modifier.heightIn(max = 64.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = facilityName,
                        style = MdTheme.typography.medium20TextStyle
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.tap_anywhere_finish),
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
            Spacer(Modifier.weight(1f))
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
fun ThankYouScreenPreview() {
    MdTheme {
        ThankYouScreen(
            selectedFacility = Facility(
                facilityId = "1",
                facilityName = "Sample Facility"
            ),
            imageLoader = getPreviewImageLoader()
        ) {}
    }
}

@Composable
fun getPreviewImageLoader(): ImageLoader {
    return ImageLoader.Builder(LocalContext.current).build()
}
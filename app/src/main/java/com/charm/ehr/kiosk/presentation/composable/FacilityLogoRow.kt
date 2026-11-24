package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.util.UIUtil
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun FacilityLogoRow(
    imageUrl: String,
    imageLoader: ImageLoader,
    imgSize: Dp,
    facilityName: String,
    initialTextStyle: TextStyle = MdTheme.typography.semiBold22TextStyle,
) {
    Column() {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {

            val context = LocalContext.current
            if (imageUrl.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(imageUrl).size(UIUtil.dpToPx(context = context, dp = imgSize))
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(imgSize)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    val painterState = painter.state
                    if (painterState is AsyncImagePainter.State.Success) {
                        SubcomposeAsyncImageContent()
                    } else {
                        CircleImageViewWithInitials(
                            initial = UIUtil.formInitials(facilityName), size = imgSize, style = initialTextStyle
                        )
                    }
                }
            } else {
                Image(painter = painterResource(id = R.drawable.ic_facility_placeholder), null, modifier = Modifier.size(imgSize))

            }
        }

        MdVerticalSpace(2f)

        Text(
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = facilityName,
            style = MdTheme.typography.medium16TextStyle
        )

    }

}

@Preview(showBackground = true)
@Composable
fun FacilityLogoRowPreview() {
    MdTheme {
        FacilityLogoRow(
            imageUrl = "",
            imageLoader = ImageLoader.Builder(LocalContext.current).build(),
            imgSize = 64.dp,
            facilityName = "Sample Facility",
        )
    }
}
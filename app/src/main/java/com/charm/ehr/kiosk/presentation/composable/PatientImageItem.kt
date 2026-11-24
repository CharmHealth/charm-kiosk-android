package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.common.util.UIUtil
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun PatientImageItem(
    hasImage: Boolean,
    imageUrl: String,
    imageLoader: ImageLoader,
    imgSize: Dp,
    selectedPatient: Patient,
    textStyle: TextStyle,
) {
    val context = LocalContext.current
    if (hasImage) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(imageUrl).size(UIUtil.dpToPx(context = context, dp = imgSize)).build(),
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
                    initial = UIUtil.formInitials(selectedPatient.fullName ?: ""), size = imgSize, style = textStyle
                )
            }
        }
    } else {
        CircleImageViewWithInitials(
            initial = UIUtil.formInitials(selectedPatient.fullName ?: ""), size = imgSize, style = textStyle
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PatientImageItemPreview() {
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()
    val patient = Patient(fullName = "John Doe")
    val imageUrl = "https://example.com/image.jpg"

    PatientImageItem(
        hasImage = true,
        imageUrl = imageUrl,
        imageLoader = imageLoader,
        imgSize = 50.dp,
        selectedPatient = patient,
        textStyle = MdTheme.typography.semiBold24TextStyle.copy(letterSpacing = 3.sp)
    )
}

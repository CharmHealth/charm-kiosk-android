package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.common.util.FileUtil

@Composable
fun MdCircularImageView(
    modifier: Modifier = Modifier.padding(0.dp),
    imageResource: Int,
    contentDescription: String? = null,
    size: Dp = 50.dp,
    border: Dp = 0.dp,
    borderColor: Color = MdColors.Transparent,
    shape: Shape = CircleShape
) {
    Image(
        painter = painterResource(id = imageResource),
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .clip(shape)
            .border(width = border, color = borderColor)
            .aspectRatio(1f)
    )
}

@Composable
fun CircularImageViewWithPlaceholder(
    imageSize: Dp,
    imageFilePath: String?,
    imageUrl: String? = null,
    placeholderIcon: Painter,
    placeholderColor: Color,
    placeHolderIconSize: Dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = MdColors.Black10,
    backgroundColor: Color = MdColors.Transparent,
    imageLoader: ImageLoader
) {

    val context = LocalContext.current
    val imageUri = remember(imageFilePath) { FileUtil.getUriFromFilePath(context, imageFilePath) }
    val imageData = if (!imageFilePath.isNullOrBlank()) imageUri else imageUrl

    Box(
        modifier = Modifier
            .size(imageSize)
            .aspectRatio(1f)
            .background(color = backgroundColor, shape = CircleShape)
            .border(borderWidth, borderColor, CircleShape)
    ) {

        IconWithPlaceholder(
            placeholderIcon,
            placeholderColor,
            placeHolderIconSize,
            modifier = Modifier.align(Alignment.Center)
        )

        if (!imageFilePath.isNullOrBlank() || !imageUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageData)
                    .size(UIUtil.dpToPx(context, imageSize))
                    .build(),
                imageLoader = imageLoader,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
            ) {
                if (painter.state is AsyncImagePainter.State.Success) {
                    SubcomposeAsyncImageContent()
                }
            }
        }
    }
}

@Composable
private fun IconWithPlaceholder(
    placeholderIcon: Painter,
    placeholderColor: Color,
    placeHolderIconSize: Dp,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = placeholderIcon,
        contentDescription = "Placeholder Icon",
        tint = placeholderColor,
        modifier = modifier
            .size(placeHolderIconSize)

    )
}

@Composable
private fun IconWithPlaceholder() {

}

@Preview(showBackground = true)
@Composable
fun PreviewCircularImageViewWithPlaceholder() {
    CircularImageViewWithPlaceholder(
        placeholderIcon = painterResource(id = R.drawable.ic_user),
        borderWidth = 1.dp,
        borderColor = MdColors.Black10,
        placeholderColor = MdColors.Black50,
        placeHolderIconSize = 20.dp,
        imageSize = 50.dp,
        imageFilePath = "",
        imageLoader = ImageLoader.Builder(LocalContext.current).build()
    )
}

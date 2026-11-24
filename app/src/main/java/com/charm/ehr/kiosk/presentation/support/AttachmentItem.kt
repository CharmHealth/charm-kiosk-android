package com.charm.ehr.kiosk.presentation.support

import android.webkit.MimeTypeMap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.Coil.imageLoader
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.util.UIUtil
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdCircularProgressBar
import com.charm.ehr.kiosk.presentation.composable.MdHorizontalSpace
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import com.charm.ehr.kiosk.common.util.FileUtil
import java.io.File

@Composable
fun AttachmentItem(
    filePath: String,
    onViewAttachment: (String) -> Unit,
    onRemove: (String) -> Unit,
    imageLoader: ImageLoader,
    isLoading: Boolean = false,
) {
    val file = remember(filePath) { File(filePath) }
    val context = LocalContext.current
    var fileSizeText by remember { mutableStateOf("") }
    val fileExtension = remember(filePath) {
        MimeTypeMap.getFileExtensionFromUrl(filePath)
    }
    LaunchedEffect(filePath) {
        try {
            fileSizeText = FileUtil.formatFileSize(file.length())
        } catch (e: Exception) {
            fileSizeText = ""
        }
    }
    MdCardWithoutTonalElevation(
        shape = RoundedCornerShape(8.dp),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MdColors.MdSurfaceColor)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isLoading) {
                Box(modifier = Modifier.height(50.dp), contentAlignment = Alignment.Center) {
                    MdCircularProgressBar(size = 30.dp)
                }
                return@Row
            }
            Box {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(file)
                        .size(UIUtil.dpToPx(context = LocalContext.current, dp = 50.dp)).build(),
                    imageLoader = imageLoader,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MdColors.Black40)
                ) {
                    val painterState = painter.state
                    if (painterState is AsyncImagePainter.State.Success) {
                        SubcomposeAsyncImageContent()
                    } else {
                        MdCircularProgressBar(size = 25.dp)
                    }
                }
            }
            MdHorizontalSpace()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MdTheme.typography.medium14TextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$fileSizeText $fileExtension",
                    style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Black30)
                )
            }
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clickable { onRemove(filePath) }, contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "Delete Attachment",
                    colorFilter = ColorFilter.tint(MdColors.Black30)
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewAttachmentItem() {
    MdTheme {
        Column() {
            Box(
                modifier = Modifier.padding(8.dp)
            ) {
                AttachmentItem(
                    onRemove = {},
                    imageLoader = imageLoader(LocalContext.current),
                    filePath = "/tmp/sample.jpg",
                    onViewAttachment = {},
                    isLoading = true
                )
            }
            Box(
                modifier = Modifier.padding(8.dp)
            ) {
                AttachmentItem(
                    onRemove = {},
                    imageLoader = imageLoader(LocalContext.current),
                    filePath = "/tmp/sample.jpg",
                    onViewAttachment = {}
                )
            }
        }
    }
}

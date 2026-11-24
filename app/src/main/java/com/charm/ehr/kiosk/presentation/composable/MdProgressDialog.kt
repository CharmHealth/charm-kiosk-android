package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun MdProgressDialog(
    lottieResId: Int = R.raw.loading,
    messageStrId:Int = R.string.loading,
    addThreeDotToTitle: Boolean = true,
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = {
    }) {
        Box(
            Modifier
                .size(height = 248.dp, width = 248.dp)
                .background(MdColors.MdSurfaceColor, shape = MaterialTheme.shapes.medium)
                .padding(24.dp)
                .graphicsLayer(clip = false)
        ) {

            val composition = rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId)).value
            if (composition != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {

                    LottieAnimation(
                        composition = composition,
                        modifier = Modifier.size(300.dp),
                        iterations = Int.MAX_VALUE
                    )
                }
            }

            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally,) {

                val message = context.getString(messageStrId)
                if (message.isNotEmpty()) {

                    val finalTitle = if (addThreeDotToTitle) {
                        "   $message..."
                    } else {
                        message
                    }
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                        text = finalTitle,
                        style = MdTheme.typography.medium16TextStyle,
                        textAlign = TextAlign.Center
                    )
                }

            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomProgressDialogPreview() {
    MdTheme {
        Box {
            MdProgressDialog(
                lottieResId = R.raw.loading,
            )
        }
    }
}



//@Composable
//fun ConstraintLayoutPercentageExample() {
//    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
//        val (text40, text60) = createRefs()
//
//        Text(
//            text = "Text at 40%",
//            modifier = Modifier.constrainAs(text40) {
//                top.linkTo(parent.top)
//                bottom.linkTo(parent.bottom)
//                start.linkTo(parent.start)
//                end.linkTo(parent.end)
//                verticalBias = 0.4f
//            }
//        )
//
//        Text(
//            text = "Text at 60%",
//            modifier = Modifier.constrainAs(text60) {
//                top.linkTo(parent.top)
//                bottom.linkTo(parent.bottom)
//                start.linkTo(parent.start)
//                end.linkTo(parent.end)
//                verticalBias = 0.6f
//            }
//        )
//    }
//}
//

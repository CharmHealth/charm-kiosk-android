package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun MDSlider(
    modifier: Modifier,
    sliderPosition: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int,
    maxValue: Int,
    unit: String? = null
) {
    val selectedStyle = MdTheme.typography.semiBold16TextStyle.copy(MdColors.BrandPink)
    val unselectedStyle = MdTheme.typography.medium14TextStyle.copy(MdColors.LightGrey)
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        StepSlider(
            value = sliderPosition,
            minvalue = minValue,
            maxValue = maxValue,
            onValueChange = { newValue ->
                onValueChange(newValue)
            },
            selectedTextStyle = selectedStyle,
            unselectedTextStyle = unselectedStyle
        )
        //As now Unit support from server
//        if (unit != null) {
//            Row {
//                MDTextView(
//                    modifier = Modifier
//                        .padding(start = 16.dp, end = 4.dp)
//                        .border(1.dp, MdColors.Black40, RoundedCornerShape(8.dp))
//                        .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
//                        .widthIn(18.dp),
//                    value = sliderPosition.toString(),
//                    textAlign = TextAlign.Center,
//                    style = MdTheme.typography.semiBold16TextStyle
//                )
//
//                MDTextView(
//                    modifier = Modifier
//                        .padding(start = 4.dp, end = 4.dp)
//                        .align(Alignment.CenterVertically),
//                    value = unit,
//                    style = MdTheme.typography.regular12TextStyle.copy(color = MdColors.Black86),
//                )
//            }
//        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MDCustomSlider(
    sliderPosition: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    minValue: Float, maxValue: Float,
    colors: SliderColors = SliderDefaults.colors(),
) {
    Slider(
        value = sliderPosition,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource,
        steps = ((maxValue - minValue).toInt() - 1),
        thumb = {
            SliderDefaults.Thumb(
                interactionSource = interactionSource,
                colors = colors,
                enabled = enabled, thumbSize = DpSize(6.dp, 32.dp)
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                colors = colors,
                modifier = Modifier.height(10.dp)
            )
        },
        valueRange = minValue..maxValue // Define the range of values
    )
}

@Preview(showSystemUi = true)
@Composable
fun SliderComponentPreview() {
    MdTheme {
        var sliderPosition by remember { mutableIntStateOf(1) }
        Box(modifier = Modifier.padding(28.dp)) {
            MDSlider(Modifier, sliderPosition, { sliderPosition = it }, 1, 15, "cm")
        }
    }
}

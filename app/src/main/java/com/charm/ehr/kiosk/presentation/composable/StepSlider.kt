package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import com.tbuonomo.viewpagerdotsindicator.dpToPx
import kotlin.math.roundToInt

@Composable
fun StepSlider(
    value: Int,
    minvalue: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,

    activeColor: Color = MdColors.BrandPink,
    labelBackgroundColor: Color = MdColors.BrandLightPink,
    inactiveColor: Color = MdColors.BrandLightPink,
    backgroundColor: Color = Color.White,

    selectedTextStyle: TextStyle = MdTheme.typography.semiBold16TextStyle.copy(MdColors.BrandPink),
    unselectedTextStyle: TextStyle = MdTheme.typography.medium14TextStyle.copy(MdColors.LightGrey),

    dotRadius: Dp = 3.dp,
    selectedDotRadius: Dp = 5.dp,
    strokeWidth: Dp = 2.dp,
    selectorStrokeWidth: Dp = 2.dp

) {

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val steps = (maxValue - minvalue).toInt()

    // Convert Dp to Px
    val dotRadiusPx = with(density) { dotRadius.toPx() }
    val selectedDotRadiusPx = with(density) { selectedDotRadius.toPx() }
    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    val selectorStrokeWidthPx = with(density) { selectorStrokeWidth.toPx() }

    // Height calculation:
    // 1. Measure the maximum possible text height (using the bigger selected style)
    // 2. Calculate the canvas height based on max text height + slider track
    val maxTextLayout = textMeasurer.measure(
        text = maxValue.toString(), // Use max value to estimate max width/height
        style = selectedTextStyle
    )
    val maxTextHeight = maxTextLayout.size.height.toFloat()

    // Total Canvas Height = Max Text Height + Gap + Track height
    val textGap = 8.dp.dpToPx()
    val trackHeight = 25.dp.dpToPx()

    val canvasHeight = with(density) { (maxTextHeight + textGap + trackHeight).toDp() }

    BoxWithConstraints(modifier = modifier.padding(horizontal = 16.dp)) {
        val width = constraints.maxWidth.toFloat()
        val stepSpacing = width / steps

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(canvasHeight)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newStep = (offset.x / stepSpacing).roundToInt().coerceIn(0, steps)
                        onValueChange(minvalue + newStep)
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        val newStep = (change.position.x / stepSpacing).roundToInt().coerceIn(0, steps)
                        onValueChange(minvalue + newStep)
                    }
                }
        ) {
            val centerY = maxTextHeight + textGap + (trackHeight / 2) // Center of the slider track

            // 1. Draw Track Lines
            drawLine(
                color = inactiveColor,
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = strokeWidthPx
            )

            val currentX = (value - minvalue) * stepSpacing
            drawLine(
                color = activeColor,
                start = Offset(0f, centerY),
                end = Offset(currentX, centerY),
                strokeWidth = strokeWidthPx
            )

            // 2. Loop through steps
            for (i in 0..steps) {
                val stepX = i * stepSpacing
                val stepValue = minvalue + i
                val isSelected = stepValue == value
                val isActive = stepValue <= value

                // --- TEXT DRAWING ---
                val styleToUse = if (isSelected) selectedTextStyle else unselectedTextStyle

                // Measure text using its specific style
                val textLayoutResult = textMeasurer.measure(
                    text = stepValue.toString(),
                    style = styleToUse
                )

                // Calculate Y position to vertically center the current text within the max text height
                // textY = (max height / 2) - (current text height / 2)
                val textYCenterOffset = (maxTextHeight - textLayoutResult.size.height) / 2

                if (isSelected) {
                    val circleRadius = (maxOf(
                        textLayoutResult.size.width,
                        textLayoutResult.size.height
                    ) / 2f) + 4.dp.toPx()
                    drawCircle(
                        color = labelBackgroundColor,
                        radius = circleRadius,
                        center = Offset(
                            x = stepX,
                            y = textYCenterOffset + textLayoutResult.size.height / 2f
                        )
                    )
                }

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = stepX - (textLayoutResult.size.width / 2),
                        y = textYCenterOffset // Aligns all text vertically
                    )
                )

                // --- NODE DRAWING ---
                if (isSelected) {
                    // SELECTED NODE CODE (Now: Solid Filled Circle)
                    // 1. Fill with active color to hide the line

                    drawCircle(
                        color = activeColor,
                        radius = selectedDotRadiusPx,
                        center = Offset(stepX, centerY)
                    )

                    // 2. Inner white circle to create the ring effect
                    drawCircle(
                        color = backgroundColor,
                        radius = selectorStrokeWidthPx,
                        center = Offset(stepX, centerY)
                    )

                    // 3. Outer stroke(to increase the size)
                    drawCircle(
                        color = if (isActive) activeColor else inactiveColor,
                        radius = selectedDotRadiusPx,
                        center = Offset(stepX, centerY),
                        style = Stroke(width = strokeWidthPx)
                    )

                } else {
                    // UNSELECTED NODE CODE
                    // Doted nodes
                    drawCircle(
                        color = if (isActive) activeColor else inactiveColor,
                        radius = dotRadiusPx,
                        center = Offset(stepX, centerY)
                    )

                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
fun PreviewStyledSlider() {
    var sliderValue by remember { mutableStateOf(5) }
    MdTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Define simple text styles for the preview:
            val selectedStyle = MdTheme.typography.semiBold16TextStyle.copy(MdColors.BrandPink)
            val unselectedStyle = MdTheme.typography.medium14TextStyle.copy(MdColors.LightGrey)

            StepSlider(
                value = sliderValue,
                minvalue = 1,
                maxValue = 10,
                onValueChange = { sliderValue = it },
                selectedTextStyle = selectedStyle,
                unselectedTextStyle = unselectedStyle
            )
        }
    }
}
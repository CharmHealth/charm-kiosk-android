package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.withSave
import com.charm.ehr.kiosk.presentation.theme.MdColors


@Composable
fun MdCardWithoutTonalElevation(
    modifier: Modifier = Modifier,
    shape: Shape =  RoundedCornerShape(24.dp),
    colors: Color = MdColors.White,
    border: BorderStroke? = null,
    elevation: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit = {}
)  {
    Surface(
        modifier = modifier,
        shape = shape,
        color = colors,
        tonalElevation = 0.dp,
        shadowElevation = elevation,
        border = border,
    ) {
        Column(content = content)
    }
}


@Preview(showBackground = true)
@Composable
private fun MdCardWithoutTonalElevationPreview() {
    Box(modifier = Modifier.background(MdColors.MdSurfaceColor)){

        MdCardWithoutTonalElevation(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                modifier = Modifier.padding(24.dp),
                text = "Card without tonal elevation"
            )
        }
    }
}



@Composable
fun MDCardSmudgeBackground(
    modifier: Modifier = Modifier,
    shape: Shape =  RoundedCornerShape(24.dp),
    colors: Color = MdColors.White,
    border: BorderStroke? = null,
    elevation: Dp = 4.dp,
    // keep the exact color value requested (transparent alpha)
    shadowColor: Color = Color(0x005E6366).copy(alpha = 0.05f),
    shadowBlur: Dp = 8.dp,
    // number of layered rings used to create the falloff (higher = smoother but costlier)
    shadowLayers: Int =2,
    // maximum outward spread of the shadow in Dp
    shadowSpread: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit = {}
)  {
    // Draw a layered soft shadow behind the Surface so the shadow starts strong near the card
    // and then loses its stretch and disperses before halfway.
    val shadowModifier = Modifier.drawBehind {
        val w = size.width
        val h = size.height

        // Convert Dp to px
        val baseBlurPx = shadowBlur.toPx()
        val maxSpreadPx = shadowSpread.toPx()
        val elevationPx = elevation.toPx()

        // Use a fallback base alpha when the provided color has 0 alpha, so the default color
        // (0x005E6366) still produces a subtle visible shadow.
        val baseInputAlpha = if (shadowColor.alpha <= 0f) 0.06f else shadowColor.alpha

        // Use elevation to increase inner strength and slightly increase overall blur
        val elevationStrengthMultiplier = (1f + (elevation.value / 10f)).coerceAtMost(2f)

        // Ensure at least 2 layers to compute falloff correctly
        val layers = kotlin.math.max(2, shadowLayers)

        // We'll draw layers from inner (i=0) to outer (i=layers-1)
        for (i in 0 until layers) {
            val t = i.toFloat() / (layers - 1).toFloat() // 0..1

            // Falloff: strong near the card, then drop rapidly before halfway, then small residual outer haze
            val falloff = if (t <= 0.5f) {
                // faster quadratic drop in the first half
                1f - (t * t * 3.0f)
            } else {
                // quick taper to a small residual beyond halfway
                (1f - t) * 0.6f
            }
            val clampedFalloff = falloff.coerceAtLeast(0f)

            // Base alpha uses fallback input alpha, scaled by falloff and elevation strength
            var layerAlpha = baseInputAlpha * clampedFalloff * elevationStrengthMultiplier
            layerAlpha = layerAlpha.coerceIn(0f, 1f)
            if (layerAlpha <= 0f) continue // skip fully transparent layers

            val layerColor = shadowColor.copy(alpha = layerAlpha)

            // Blur: increase with t to make outer layers more diffuse; small elevation contribution
            val blurPx = baseBlurPx + (t * baseBlurPx * 1.5f) + (elevationPx * 0.25f)

            // Spread: grow quickly until halfway, then plateau (loses stretch before halfway)
            val spreadFactor = kotlin.math.min(t * 2f, 1f) // 0..1, hits 1 at t=0.5
            val spread = spreadFactor * maxSpreadPx

            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
                color = layerColor.toArgb()
                // MaskFilter creates a smooth gradient-like blur
                maskFilter = android.graphics.BlurMaskFilter(blurPx, android.graphics.BlurMaskFilter.Blur.NORMAL)
            }

            // Determine corner radius from shape when it's a RoundedCornerShape
            val cornerRadius = when (shape) {
                is RoundedCornerShape -> {
                    val topStart = shape.topStart
                    try {
                        topStart.toPx(size, this)
                    } catch (_: Exception) {
                        0f
                    }
                }
                else -> 0f
            }

            val left = -spread
            val top = -spread
            val right = w + spread
            val bottom = h + spread
            val rectF = android.graphics.RectF(left, top, right, bottom)

            val canvas = drawContext.canvas.nativeCanvas
            canvas.withSave {
                drawRoundRect(rectF, cornerRadius + spread, cornerRadius + spread, paint)
            }
        }
    }

    Surface(
        modifier = modifier.then(shadowModifier),
        shape = shape,
        color = colors,
        tonalElevation = 0.dp, // keep tonal elevation zero to avoid tint
        shadowElevation = 0.dp, // we draw our own soft shadow instead
        border = border,
    ) {
        Column(content = content)
    }
}

package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChipList(
    modifier: Modifier,
    items: List<T>,
    displayString: (T) -> String,
    onRemoveItem: (T) -> Unit,
    textStyle: TextStyle,
    horizontal: Arrangement.Horizontal,
    vertical: Arrangement.Vertical,
    hideCloseIcon: Boolean,
    chipColor: Color,
    onMultipleRows: ((Boolean) -> Unit)? = null
) {
    var firstChipHeight by remember { mutableStateOf(0f) }
    var hasMultipleRows by remember { mutableStateOf(false) }

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                if (items.isNotEmpty() && firstChipHeight > 0) {
                    val totalHeight = coordinates.size.height
                    // Check if total height is significantly more than single chip height
                    // Allow small tolerance for padding/spacing variations
                    val isMultipleRows = totalHeight > (firstChipHeight * 1.5f)

                    if (hasMultipleRows != isMultipleRows) {
                        hasMultipleRows = isMultipleRows
                        onMultipleRows?.invoke(isMultipleRows)
                    }
                }
            },
        horizontalArrangement = horizontal,
        verticalArrangement = vertical
    ) {
        items.forEachIndexed { index, item ->
            if (index == 0) {
                // Wrap first chip in Box to measure its height
                Box(
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        val measuredHeight = coordinates.size.height.toFloat()
                        if (firstChipHeight != measuredHeight) {
                            firstChipHeight = measuredHeight
                        }
                    }
                ) {
                    MdRectangleChipWithClose(
                        text = displayString(item),
                        onChipClick = { onRemoveItem(item) },
                        textStyle = textStyle,
                        hideIcon = hideCloseIcon,
                        chipColor = chipColor
                    )
                }
            } else {
                MdRectangleChipWithClose(
                    text = displayString(item),
                    onChipClick = { onRemoveItem(item) },
                    textStyle = textStyle,
                    hideIcon = hideCloseIcon,
                    chipColor = chipColor
                )
            }
        }
    }
}
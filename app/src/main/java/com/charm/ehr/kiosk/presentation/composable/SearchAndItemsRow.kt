package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun <T> ChipFlowLayoutWithButton(
    selectedItems: List<T>,
    labelString: String,
    labelDetailString: String,
    displayString: (T) -> String,
    onRemoveItem: (T) -> Unit,
    onClickAdd: () -> Unit,
    onMultipleRows: ((Boolean) -> Unit)? = null
) {
    var hasMultipleRows by remember { mutableStateOf(false) }
    val rowModifier = if (selectedItems.isEmpty()) {
        Modifier.clickable {
            onClickAdd()
        }
    } else {
        Modifier
    }
    Row(
        modifier = Modifier.then(
            if(hasMultipleRows) {
                Modifier.padding(vertical = MdTheme.dimension.cardPadding)
            } else {
                Modifier.padding(0.dp)
            }
        )
            .heightIn(min = 60.dp, max = 250.dp),
        verticalAlignment = if (hasMultipleRows) {
            Alignment.Top
        } else {
            Alignment.CenterVertically
        },
    ) {
        Text(
            modifier = Modifier
                .then(
                    if (hasMultipleRows) {
                        Modifier.padding(top = 10.dp)
                    } else {
                        Modifier.align(Alignment.CenterVertically)
                    }
                ),
            text = labelString,
            style = MdTheme.typography.semiBold16TextStyle
        )

        Row(
            modifier = rowModifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            MdHorizontalSpace(1.5f)

            if (selectedItems.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                ) {
                    ChipList(
                        modifier = Modifier,
                        items = selectedItems,
                        displayString = displayString,
                        onRemoveItem = onRemoveItem,
                        textStyle = MdTheme.typography.medium14TextStyle,
                        vertical = Arrangement.spacedBy(8.dp),
                        horizontal = Arrangement.spacedBy(8.dp),
                        hideCloseIcon = true,
                        chipColor = MdColors.MildPinkishGrey,
                        onMultipleRows = { isMultiple ->
                            hasMultipleRows = isMultiple
                            onMultipleRows?.invoke(isMultiple)
                        }
                    )
                }
            } else {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .padding(start = MdTheme.dimension.cardPadding),
                    text = labelDetailString,
                    style = MdTheme.typography.regular16TextStyle
                )
            }

            MdRoundAddButton(
                modifier = Modifier
                    .heightIn(min = 64.dp)
                    .width(72.dp).padding(end=8.dp)
                ,
                onClick = {
                    onClickAdd()
                },
                buttonSize = 26.dp,
                isAdd = selectedItems.isEmpty()
            )

        }
    }

}

@Preview(showBackground = true)
@Composable
fun SearchAndItemsRowPreview() {
    val sampleItems =
        remember {
            mutableStateListOf(
                "Item 1",
                "Item 2",
                "Item 3",
                "Item 4",
                "Item 5",
                "Item 6",
                "Item 7"
            )
        }
    MdTheme {
        ChipFlowLayoutWithButton(
            selectedItems = sampleItems,
            labelString = "Selected Items",
            labelDetailString = "No items selected",
            displayString = { it },
            onRemoveItem = { item -> sampleItems.remove(item) },
            onClickAdd = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchAndItemsRowEmptyPreview() {
    MdTheme {
        ChipFlowLayoutWithButton(
            selectedItems = emptyList<String>(),
            labelString = "Selected Items",
            labelDetailString = "No items selected",
            displayString = { it },
            onRemoveItem = { },
            onClickAdd = {},

            )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchAndItemsRowSingleItemPreview() {
    val sampleItems =
        remember { mutableStateListOf("Item 1", "Item 2") }

    MdTheme {

        ChipFlowLayoutWithButton(
            selectedItems = sampleItems,
            labelString = "Selected Items",
            labelDetailString = "No items selected",
            displayString = { it },
            onRemoveItem = { item -> sampleItems.remove(item) },
            onClickAdd = {},

            )
    }
}



package com.charm.ehr.kiosk.presentation.composable

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.edit_profile.OutlinedTextFieldWithFlowRow
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun <T> MultiSelectChipDialogField(
    modifier: Modifier = Modifier,
    dialogTitle: String,
    fieldLabelText:String,
    selectedItems: List<T>,
    allOptions: List<T>,
    itemToString: (T) -> String,
    onSelectionChanged: (List<T>) -> Unit,
    onRemoveItem: (T) -> Unit = {},
    isMultiSelect: Boolean = true,
    isDialogEnabled: Boolean = true,
    isMandatory: Boolean=false
) {
    var isDialogOpen by remember(isDialogEnabled) { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isDialogEnabled) {
        if (!isDialogEnabled) isDialogOpen = false
    }

    OutlinedTextFieldWithFlowRow(
        modifier = modifier.focusRequester(focusRequester),
        fieldLabel = {
            MdCustomLabel(
                text=fieldLabelText,
                isMandatory=isMandatory,
                enabled = true
            )
        },
        list = selectedItems,
        displayString = itemToString,
        onRemoveItem = { item ->
            onRemoveItem(item)
            onSelectionChanged(selectedItems.filterNot { it == item })
        },
        isFocused = {
            if (isDialogEnabled) {
                isDialogOpen = it
                Log.d("isDialogEnabled", "isDialogOpen value set $isDialogOpen")
            }
        },
        onClick = {
            if (isDialogEnabled) {
                isDialogOpen = true
                Log.d("isDialogEnabled", "isDialogOpen value set $isDialogOpen")
            }
        }
    )

    if (isDialogOpen) {
        Log.d("isDialogEnabled", "Inside isDialogOpen")
        MdSelectionDialogWithSearch(
            data = allOptions,
            dialogTitle = dialogTitle,
            isMultiSelect = isMultiSelect,
            itemToString = itemToString,
            onDismissRequest = {
                isDialogOpen = false
                focusManager.clearFocus(force = true)
            },
            onResult = {
                onSelectionChanged(it)
                focusManager.clearFocus(force = true)
            },
            preSelectedItems = selectedItems
        )
    }
}

@Composable
fun <T> MdSelectionDialogWithSearch(
    data: List<T>,
    dialogTitle: String,
    isMultiSelect: Boolean = false,
    onResult: (List<T>) -> Unit,
    preSelectedItems: List<T>,
    itemToString: (T) -> String,
    onDismissRequest: () -> Unit
) {

    var query by remember { mutableStateOf("") }
    val filteredData = remember(query, data) {
        data.filter { itemToString(it).contains(query, ignoreCase = true) }
    }
    var selectedItems by remember(preSelectedItems) { mutableStateOf(preSelectedItems.toSet()) }

    val configuration = LocalConfiguration.current
    val maxHeight = (configuration.screenHeightDp * 0.65).dp
    val maxWidth = (configuration.screenWidthDp * 0.8).dp

    Dialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true,
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        ),
        onDismissRequest = {
            onResult(selectedItems.toList())
            onDismissRequest()
        },
    ) {
        Column(
            modifier = Modifier
                .height(maxHeight)
                .widthIn(max = maxWidth)
                .clip(RoundedCornerShape(24.dp)) // Set rounded corners
                .background(Color.White)
                .padding(16.dp) // Set entire background to white
        ) {

            MdVerticalSpace(1f)
            Text(
                text = dialogTitle,
                style = MdTheme.typography.semiBold20TextStyle,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp)
                    .border(2.dp, Color.Transparent, RoundedCornerShape(24.dp))
                    .background(MdColors.LightGray, RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.Transparent, RoundedCornerShape(20.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (query.isEmpty()) {
                                        Text(
                                            text = "Search...",
                                            style = MdTheme.typography.regular18TextStyle.copy(color = Color.Gray),
                                            modifier = Modifier.padding(horizontal = 8.dp).align(Alignment.CenterStart)
                                        )
                                    }
                                    BasicTextField(
                                        value = query,
                                        onValueChange = { query = it },
                                        singleLine = true,
                                        textStyle = MdTheme.typography.regular18TextStyle,
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).align(Alignment.CenterStart)
                                    )
                                }
                            }

                            if (query.isNotBlank()) {
                                Icon(
                                    tint = MdColors.Black30,
                                    painter = painterResource(R.drawable.ic_close),
                                    contentDescription = "Clear Text",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { query = "" }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp) // Same size as the Icon
                                )
                            }
                        }
                    }
                }
            }

            MdVerticalSpace()

            LazyColumn {
                items(filteredData.size) { index ->
                    if (isMultiSelect) {
                        MultiSelectItem(
                            text = itemToString(filteredData.getOrNull(index) ?: return@items),
                            query = query,
                            isChecked = selectedItems.contains(
                                filteredData.getOrNull(index) ?: return@items
                            ),
                            onClick = {
                                val item = filteredData.getOrNull(index) ?: return@MultiSelectItem
                                if (selectedItems.contains(item)) {
                                    selectedItems = selectedItems - item
                                } else {
                                    selectedItems = selectedItems + item
                                }
                            }
                        )
                    } else {
                        SingleSelectItem(
                            text = itemToString(filteredData.getOrNull(index) ?: return@items),
                            onClick = {
                                val selectedItem =
                                    filteredData.getOrNull(index) ?: return@SingleSelectItem
                                onResult(listOf(selectedItem))
                                onDismissRequest()
                            },
                            query = query,
                            isChecked = selectedItems.contains(
                                filteredData.getOrNull(index) ?: return@items
                            )
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun SingleSelectItem(text: String, query: String, onClick: () -> Unit, isChecked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {

        RadioButton(
            selected = isChecked,
            onClick = {
                onClick()
            },
        )

        MdHighlightedText(
            text = text,
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 8.dp),
            style = MdTheme.typography.medium16TextStyle,
            searchQuery = query,
        )
    }
}

@Composable
private fun MultiSelectItem(
    text: String,
    query: String,
    onClick: () -> Unit,
    isChecked: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {

        Checkbox(checked = isChecked, onCheckedChange = {
            onClick()
        })

        MdHighlightedText(
            text = text,
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 8.dp),
            style = MdTheme.typography.medium14TextStyle,
            searchQuery = query,
        )
    }
}

@Composable
private fun PreviewMultiSelectAlertDialog() {

    data class Book(val id: Int, val name: String)

    val bookList = listOf(
        Book(1, "To Kill a Mockingbird"),
        Book(2, "1984"),
        Book(3, "The Great Gatsby"),
        Book(4, "The Catcher in the Rye"),
        Book(5, "Pride and Prejudice"),
        Book(6, "The Lord of the Rings"),
        Book(7, "Harry Potter and the Sorcerer's Stone"),
        Book(8, "The Hobbit"),
        Book(9, "Fahrenheit 451"),
        Book(10, "Jane Eyre")
    )

    MdTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            MultiSelectChipDialogField(
                modifier = Modifier.padding(16.dp),
                dialogTitle = "Select Book",
                fieldLabelText = "Books",
                isMultiSelect = false,
                selectedItems = listOf(bookList[1], bookList[2]),
                allOptions = bookList,
                itemToString = { it.name },
                onSelectionChanged = { selectedBooks ->
                    // Handle selection change
                },
                onRemoveItem = { removedBook ->
                    // Handle item removal
                }
            )
        }
    }
}

@Composable
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,dpi=420")
private fun MobilePreview() {
    PreviewMultiSelectAlertDialog()
}

@Composable
@Preview(showBackground = true, device = "spec:width=600dp,height=1000dp,dpi=420")
private fun TabletPreview() {
    PreviewMultiSelectAlertDialog()
}

@Composable
@Preview(showBackground = true)
fun PreviewSingleSelectItem() {
    MdTheme {
        SingleSelectItem(
            text = "Sample Item",
            query = "Sample",
            onClick = {
                // Handle click action
            },
            isChecked = false,
        )
    }
}


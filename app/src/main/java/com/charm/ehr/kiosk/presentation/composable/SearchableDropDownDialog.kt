package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdFieldColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun <T> SearchableDropDownDialog(
    modifier: Modifier = Modifier,
    label: String,
    onItemSelected: (T) -> Unit,
    onSearch: (String) -> Unit,
    searchResults: List<T>,
    isLoading: Boolean,
    dialogTitle: String,
    selectedOptions: List<T>,
    optionToString: (T) -> String,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = MdTheme.typography.medium16TextStyle,
    isMandatory: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    OutlinedTextField(
        textStyle = textStyle,
        value = selectedOptions.firstOrNull()?.let(optionToString) ?: "",
        onValueChange = {},
        readOnly = true,
        maxLines = 1,
        isError = isError,
        supportingText = supportingText,
        label = {
            MdCustomLabel(
                text = label,
                isError = isError,
                isMandatory = isMandatory,
                enabled = true
            )
        },
        trailingIcon = {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_down),
                contentDescription = null
            )
        },
        modifier = modifier
            .focusRequester(remember { FocusRequester() })
            .clickable {
                showDialog = true
            },
        colors = MdFieldColors(),
        shape = RoundedCornerShape(8.dp),
        interactionSource = remember { MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        if (it is PressInteraction.Release) {
                            showDialog = true
                        }
                    }
                }
            }
    )

    if (showDialog) {
        MdSelectionDialogWithSearch(
            dialogTitle = dialogTitle,
            onResult = { selectedList ->
                val selected = selectedList.firstOrNull()
                if (selected != null) onItemSelected(selected)
                showDialog = false
            },
            preSelectedItems = selectedOptions,
            itemToString = optionToString,
            onDismissRequest = { showDialog = false },
            isLoading = isLoading,
            onSearch = { query ->
                searchQuery = query
                if (query.length >= 3) onSearch(query)
            },
            minSearchLength = 3,
            searchQuery = searchQuery,
            searchResults = searchResults as List<T>
        )
    }
}

@Composable
fun <T> MdSelectionDialogWithSearch(
    dialogTitle: String,
    onResult: (List<T>) -> Unit,
    preSelectedItems: List<T>,
    itemToString: (T) -> String,
    onDismissRequest: () -> Unit,
    isLoading: Boolean = false,
    onSearch: ((String) -> Unit)? = null,
    minSearchLength: Int = 3,
    searchQuery: String,
    searchResults: List<T>
) {
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
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(16.dp)
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
                    .background(MdColors.Black10, RoundedCornerShape(24.dp))
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
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search...",
                                            style = TextStyle(color = Color.Gray)
                                        )
                                    }
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = {
                                            onSearch?.invoke(it)
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(color = Color.Black),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            if (searchQuery.isNotBlank()) {
                                Icon(
                                    tint = MdColors.Black30,
                                    painter = painterResource(R.drawable.ic_close),
                                    contentDescription = "Clear Text",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { onSearch?.invoke("") }
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

            if (searchQuery.length < minSearchLength) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(id = R.string.search_min_length, minSearchLength))
                }
            } else if (isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn {
                    items(searchResults.size) { index ->
                        SingleSelectItem(
                            text = itemToString(searchResults.getOrNull(index) ?: return@items),
                            onClick = {
                                val selectedItem =
                                    searchResults.getOrNull(index) ?: return@SingleSelectItem
                                onResult(listOf(selectedItem))
                                onDismissRequest()
                            },
                            query = searchQuery,
                            isChecked = selectedItems.contains(
                                searchResults.getOrNull(index) ?: return@items
                            )
                        )
                    }
                }
                if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(id = R.string.no_results_found))
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

@Preview(showSystemUi = true)
@Composable
fun PreviewSearchableDropDownDialog() {
    val sampleResults = listOf("Maharashtra", "Karnataka", "Tamil Nadu", "Kerala")
    var selected by remember { mutableStateOf<String?>(null) }
    SearchableDropDownDialog(
        label = "State",
        selectedOptions = selected?.let { listOf(it) } ?: emptyList(),
        onItemSelected = { selected = it },
        onSearch = {},
        searchResults = sampleResults,
        isLoading = false,
        dialogTitle = "Select State",
        optionToString = { it }
    )
}

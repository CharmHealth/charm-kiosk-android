package com.charm.ehr.kiosk.presentation.composable

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@Composable
fun <OPTION> SearchBarWithDropDown(
    options: List<OPTION>,
    getLabelFromOptions: (OPTION) -> String,
    onOptionSelected: (OPTION) -> Unit,
    onSearch: (String) -> Unit,
    isLoading: Boolean,
    searchResult: List<Any>,
    errorMessage: String? = null,
    pojoToString: (Any) -> String,
    onItemSelected: (Any) -> Unit,
    onItemRemoved: (Any) -> Unit,
    uniqueValueForKey: (Any) -> String,
    selectedItems: Set<Any>,
    isSelected: (Any) -> Boolean,
    isEnabled: (Any) -> Boolean,

    ) {
    var query by remember { mutableStateOf("") }

    LaunchedEffect(query) {
        onSearch(query)
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            keyboardController?.hide()
        }
    }

    Column {
        // Search input and dropdown
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 14.dp, end = 14.dp)
                .border(2.dp, Color.Transparent, RoundedCornerShape(24.dp))
                .background(MdColors.LightGray, RoundedCornerShape(24.dp))
                .padding(4.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (options.size > 1) {
                        DropdownMenuComponent(
                            options = options,
                            optionLabel = getLabelFromOptions,
                            onOptionSelected = onOptionSelected
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                    }

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
                                    //TODO:Need to show Search questionnaire, Search consent form
                                    Text(
                                        modifier = Modifier.align(Alignment.CenterStart),
                                        text = stringResource(
                                            R.string.search_string,
                                            getLabelFromOptions(options.first())
                                        ), style = TextStyle(color = Color.Gray)
                                    )
                                }
                                BasicTextField(
                                    value = query,
                                    onValueChange = { query = it },
                                    singleLine = true,
                                    textStyle =MdTheme.typography.regular20TextStyle,
                                    modifier = Modifier.fillMaxWidth().align(Alignment.CenterStart)
                                )
                            }
                        }

                        if (query.isNotBlank()) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = "Clear Text",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { query = "" }
                            )
                        }
                    }
                }
            }
        }

        // Content based on UiState

        if (errorMessage != null) {
            TopPercentAlignedColumn(
                topPercent = 0.4f,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

        } else if (isLoading) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopPercentAlignedColumn(
                    topPercent = 0.4f,
                    modifier = Modifier.fillMaxSize()
                ) {
                    MdCircularProgressBar()
                }
            }

        } else if (searchResult.isNotEmpty()) {
            val results =
                searchResult.filter { pojoToString(it).contains(query, ignoreCase = true) }
            LazyColumn(state = listState) {
                items(results.size, key = { index -> uniqueValueForKey(results[index]) }) { index ->
                    val item = results[index]
                    SearchResultItem(
                        item = item,
                        itemLabel = pojoToString,
                        isSelected = isSelected(item),
                        onItemClick = {
                            if (isSelected(item)) {
                                onItemRemoved(item)
                            } else {
                                onItemSelected(item)
                            }
                        },
                        isLast = index == results.lastIndex,
                        isEnabled = isEnabled(item)
                    )
                }
            }
        } else {
            if (query.isNotBlank()) {
                TopPercentAlignedColumn(
                    topPercent = 0.4f,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = stringResource(R.string.no_such_questionnaire),
                        style = MdTheme.typography.semiBold16TextStyle,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            } else {
                TopPercentAlignedColumn(
                    topPercent = 0.4f,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = stringResource(R.string.enter_search_keys_to_perform_search),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = MdTheme.typography.semiBold16TextStyle
                    )
                }
            }
        }

    }
}

@Composable
fun <T> DropdownMenuComponent(
    options: List<T>,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(optionLabel(options.first())) }

    Box(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(24.dp))
            .clickable { expanded = true }
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = selected)
            Icon(
                painter = painterResource(R.drawable.ic_arrow_down),
                contentDescription = "Dropdown Arrow"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        selected = optionLabel(option)
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun <T> SearchResultItem(
    item: T,
    itemLabel: (T) -> String,
    isSelected: Boolean,
    onItemClick: (T) -> Unit,
    isLast: Boolean = false,
    isEnabled: Boolean = true
) {
    val modifier = if (isEnabled) {
        Modifier.clickable { onItemClick(item) }
    } else {
        Modifier
    }
    Column(
        modifier = modifier.padding(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onItemClick(item) },
                enabled = isEnabled
            )

            Text(
                text = itemLabel(item),
                modifier = Modifier
                    .weight(1f)
            )
        }
        if (!isLast) {
            Spacer(
                modifier = Modifier
                    .height(0.5.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(MdColors.Black10)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSearchBarWithDropDown() {
    val mockOptions = listOf("Option 1", "Option 2", "Option 3")
    var mockResults = listOf("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
    val selectedItems = remember { mutableStateOf(setOf<Any>()) }

    MdTheme {
        SearchBarWithDropDown(
            options = mockOptions,
            getLabelFromOptions = { it },
            onOptionSelected = {

            },
            onSearch = {
                // Example: Filter results containing the query (case-insensitive)
                mockResults = mockResults.filter { it.contains(it, ignoreCase = true) }
            },
            isLoading = false,
            searchResult = mockResults,
            errorMessage = null,
            pojoToString = { it.toString() },
            onItemSelected = { item ->
                selectedItems.value = selectedItems.value + item
            },
            onItemRemoved = { item ->
                selectedItems.value = selectedItems.value - item
            },
            uniqueValueForKey = { it.toString() },
            selectedItems = selectedItems.value,
            isSelected = { item -> selectedItems.value.contains(item) },
            isEnabled = { item -> selectedItems.value.contains(item) }
        )
    }
}

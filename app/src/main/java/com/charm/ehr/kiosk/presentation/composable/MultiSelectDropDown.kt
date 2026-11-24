package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
@Deprecated("Use MultiSelectChipDialogField instead")
fun MultiSelectDropdown(
    modifier: Modifier = Modifier,
    label: String,
    allOptions: List<String>,
    selectedOptions: List<String>,
    onSelectionChanged: (List<String>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var parentWidth by remember { mutableStateOf(0) }

    val filteredOptions = remember(query, allOptions) {
        if (query.isBlank()) allOptions
        else allOptions.filter { it.contains(query, ignoreCase = true) }
    }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.onGloballyPositioned { parentWidth = it.size.width }
    ) {
        // This triggers the dropdown
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(8.dp)
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .border(1.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            if (selectedOptions.isEmpty()) {
                Text(label, color = Color.Gray)
            } else {
                SelectedChips(
                    selectedOptions = selectedOptions,
                    onRemove = { onSelectionChanged(selectedOptions - it) }
                )
            }
        }

        if (expanded) {
            SearchableDropdownMenu(
                query = query,
                onQueryChanged = { query = it },
                filteredOptions = filteredOptions,
                selectedOptions = selectedOptions,
                onOptionToggled = { option ->
                    val updated = if (selectedOptions.contains(option)) {
                        selectedOptions - option
                    } else {
                        selectedOptions + option
                    }
                    onSelectionChanged(updated)
                },
                onDismiss = {
                    expanded = false
                    focusManager.clearFocus()
                },
                parentWidth = parentWidth
            )
        }
    }
}

@Composable
fun SelectedChips(
    selectedOptions: List<String>,
    onRemove: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        selectedOptions.forEach { option ->
            AssistChip(
                onClick = {

                },
                label = { Text(option) },
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Remove",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onRemove(option) }
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MdColors.BrandLightPinkVariant,
                    labelColor = MdColors.Black86,
                    trailingIconContentColor = MdColors.Black60
                ),
                border = BorderStroke(1.dp, Color.Transparent)
            )
        }
    }
}

@Composable
fun SearchableDropdownMenu(
    query: String,
    onQueryChanged: (String) -> Unit,
    filteredOptions: List<String>,
    selectedOptions: List<String>,
    onOptionToggled: (String) -> Unit,
    onDismiss: () -> Unit,
    parentWidth: Int
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(with(LocalDensity.current) { parentWidth.toDp() })
            .heightIn(max = 400.dp)
            .background(MdColors.LightOrange, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        // Search field
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search", style = MdTheme.typography.medium14TextStyle) },
            leadingIcon = { Icon(
                painter = painterResource(R.drawable.ic_search), contentDescription = "Search") },
            trailingIcon = {
                if (query.isNotBlank()) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Clear",
                        modifier = Modifier.clickable { onQueryChanged("") }
                    )
                }
            },
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Options
        if (filteredOptions.isEmpty()) {
            DropdownMenuItem(
                text = { Text("No results found", color = Color.Gray) },
                onClick = {},
                enabled = false
            )
        } else {
            filteredOptions.forEach { option ->
                DropdownMenuItem(
                    onClick = { onOptionToggled(option) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedOptions.contains(option),
                                onCheckedChange = null, // handled by row click
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF80004D),
                                    uncheckedColor = Color.Gray
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(option)
                        }
                    }
                )
            }
        }
    }
}



@Preview(showBackground = true, widthDp = 700, heightDp = 1000)
@Composable
fun RaceSelectorPreview() {
    var selected by remember { mutableStateOf(listOf("Black Or African American", "Blackfeet", "Bois Forte")) }
    val options = listOf(
        "Black Or African American conte for legnth extra ", "Blackfeet", "Blackfoot Sioux", "Bois Forte", "Botswanan"
    )

    MdTheme {
        Box(
            modifier = Modifier
                .size(700.dp)
                .padding(50.dp)
        ) {
            MultiSelectDropdown(
                modifier = Modifier,
                allOptions = options,
                selectedOptions = selected,
                onSelectionChanged = { selected = it },
                label = "Ethinicity",
            )

        }

    }
}




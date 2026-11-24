package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdFieldColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> MdDropDownMenu(
    modifier: Modifier,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    labelText: String,
    optionToString: (T) -> String,
    isError: Boolean,
    expanded: Boolean? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    textStyle: TextStyle,
    itemTextStyle: TextStyle,
    maxLines: Int = 1,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    isMandatory: Boolean = false
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    var internalExpandedState by remember { mutableStateOf(false) }

    val expandedState = expanded ?: internalExpandedState
    val updateExpandedState: (Boolean) -> Unit = { newValue ->
        if (onExpandedChange != null) {
            onExpandedChange(newValue)
        } else {
            internalExpandedState = newValue
        }
    }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expandedState,
        onExpandedChange = {
            if (enabled) {
                updateExpandedState(!expandedState)
            }
        }
    ) {
        OutlinedTextField(
            textStyle = textStyle,
            value = selectedOption?.let(optionToString) ?: "",
            onValueChange = {},
            readOnly = true,
            maxLines = maxLines,
            isError = isError,
            supportingText = supportingText,
            label = {
                MdCustomLabel(
                    text=labelText,
                    isError=isError,
                    isFocused=isFocused,
                    isMandatory = isMandatory,
                    enabled = enabled
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_down),
                    contentDescription = null
                )
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = enabled)
                .fillMaxWidth()
                .clickable(enabled = enabled) {
                    if (enabled) onClick()
                }
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                },
            enabled = enabled,
            colors = MdFieldColors(),
            shape = RoundedCornerShape(8.dp),
            interactionSource = remember { MutableInteractionSource() }
                .also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect {
                            if (enabled && it is PressInteraction.Release) {
                                onClick()
                            }
                        }
                    }
                }
        )

        ExposedDropdownMenu(
            modifier = Modifier.background(MdColors.MdSurfaceColor),
            expanded = expandedState,
            onDismissRequest = { updateExpandedState(false) }
        ) {
            if (enabled) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = optionToString(option), style = itemTextStyle) },
                        onClick = {
                            onOptionSelected(option)
                            updateExpandedState(false)
                            focusManager.clearFocus()
                        }
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewMdDropDownMenu() {
    val options = listOf("Option 1", "Option 2", "Option 3")
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    MdTheme {
        Row(modifier = Modifier.padding(10.dp)) {
            MdDropDownMenu(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                options = options,
                selectedOption = selectedOption,
                onOptionSelected = { selectedOption = it },
                labelText = "Select an option",
                isError = false,
                textStyle = MdTheme.typography.medium16TextStyle,
                itemTextStyle = MdTheme.typography.medium16TextStyle,
                optionToString = { it },
                expanded = expanded,
                onExpandedChange = { expanded = it }
            )

            Spacer(modifier = Modifier.height(300.dp))
        }
    }
}

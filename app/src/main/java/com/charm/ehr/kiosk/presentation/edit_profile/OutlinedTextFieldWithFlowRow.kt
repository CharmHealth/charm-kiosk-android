package com.charm.ehr.kiosk.presentation.edit_profile

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.presentation.composable.ChipList
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

object EmptyTextToolbar : TextToolbar {
    override val status: TextToolbarStatus = TextToolbarStatus.Hidden

    override fun hide() {}

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?,
    ) {
    }
}

val TransparentTextSelectionColors = TextSelectionColors(
    handleColor = MdColors.Transparent,
    backgroundColor = MdColors.Transparent
)

@Composable
fun <T> OutlinedTextFieldWithFlowRow(
    modifier: Modifier,
    fieldLabel: @Composable (() -> Unit)? = null,
    list: List<T>,
    displayString: (T) -> String,
    onRemoveItem: (T) -> Unit,
    isFocused: (Boolean) -> Unit,
    onClick: () -> Unit,
) {

    val focusManager = LocalFocusManager.current

    CompositionLocalProvider(
        LocalTextSelectionColors provides TransparentTextSelectionColors,
        LocalTextToolbar provides EmptyTextToolbar
    ) {
        OutlinedTextField(
            modifier = modifier
                .wrapContentHeight()
                .onFocusChanged {
                    isFocused(it.isFocused)
                },
            readOnly = true,
            value = if (list.isNotEmpty()) " " else "",
            onValueChange = {
                // Handle text change if needed
            },
            trailingIcon = {
                if (list.isNotEmpty()) {
                    ChipList(
                        modifier = Modifier
                            .padding(start = 12.dp, end = 34.dp, top = 12.dp, bottom = 12.dp),
                        items = list,
                        displayString = displayString,
                        onRemoveItem = {
                            onRemoveItem(it)
                            if (list.isEmpty()) {
                                focusManager.clearFocus()
                            }
                        },
                        textStyle = MdTheme.typography.medium14TextStyle,
                        horizontal = Arrangement.spacedBy(16.dp),
                        vertical = Arrangement.spacedBy(8.dp),
                        hideCloseIcon = false,
                        chipColor = MdColors.BrandLightPinkVariant
                    )
                }
            },
            label = fieldLabel,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.None
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MdColors.LightBlueGray,
                unfocusedBorderColor =  MdColors.LightBlueGray,
                focusedLabelColor =  MdColors.BrandPink,
                unfocusedLabelColor =  MdColors.Black86,
                errorBorderColor = MdColors.RedMatt,
                disabledContainerColor = MdColors.LightGray,
                disabledBorderColor = MdColors.blackishGreyLight
            ),
            shape = RoundedCornerShape(8.dp),
            interactionSource = remember { MutableInteractionSource() }
                .also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect {
                            if (it is PressInteraction.Release) {
                                onClick()
                            }
                        }
                    }
                }

        )
    }

}

@Composable
@Preview(showBackground = true)
fun PreviewFlowRowInTextField() {

    val stringList = remember { mutableStateOf(listOf<String>()) }

    MdTheme {
        Column(modifier = Modifier.size(400.dp)) {
            OutlinedTextFieldWithFlowRow(
                modifier = Modifier,
                fieldLabel = {
                    Text("Enter items", style = MdTheme.typography.medium14TextStyle)
                },
                list = stringList.value,
                displayString = { it },
                onRemoveItem = { item ->
                    stringList.value = stringList.value.filterNot { it == item }
                },
                isFocused = {

                },
                onClick = {

                }
            )
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 600)
fun PreviewFlowRowInTextFieldWithItems() {

    val stringList = remember { mutableStateOf((1..10).map { "Item $it" }) }

    MdTheme {
        Column(modifier = Modifier.size(400.dp)) {
            OutlinedTextFieldWithFlowRow(
                modifier = Modifier,
                fieldLabel = {
                    Text("Enter items")
                },
                list = stringList.value,
                displayString = { it },
                onRemoveItem = { item ->
                    stringList.value = stringList.value.filterNot { it == item }
                },
                isFocused = {

                },
                onClick = {

                }
            )
        }
    }
}
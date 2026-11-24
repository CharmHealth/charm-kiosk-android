package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdFieldColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MdOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = RoundedCornerShape(8.dp),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(MdColors.BrandPink),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = if (label != null) {
            modifier
                .semantics(mergeDescendants = true) {}
                .padding(0.dp)
        } else {
            modifier
        }
            .defaultMinSize(
                minWidth = OutlinedTextFieldDefaults.MinWidth,
                minHeight = OutlinedTextFieldDefaults.MinHeight
            ),
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors
    )
}


@Composable
fun MdCustomOutlineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    labelText: String,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int =1,//Set maxLines to Int.MAX_VALUE by default to allow multiline input
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = RoundedCornerShape(8.dp),
    isMandatory: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }
    MdOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.onFocusChanged { focusState ->
            isFocused = focusState.isFocused
        },
        enabled = enabled,
        readOnly = readOnly,
        textStyle = if (isFocused || isError) {
            LocalTextStyle.current.copy(color = Color.Unspecified)
        } else {
            MdTheme.typography.medium14TextStyle.copy(color = Color.Unspecified)
        },
        label = {
            MdCustomLabel(
                text = labelText,
                isError = isError,
                isFocused = isFocused,
                isMandatory = isMandatory,
                enabled = enabled
            )
        },
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource,
        shape = shape,
        colors = MdFieldColors()
    )
}

//DO not remove - for future use
//Material 3 TextField with TextFieldState
//@Composable
//fun MdOutlineTextFieldWithState(
//    state: TextFieldState,
//    modifier: Modifier = Modifier,
//    enabled: Boolean = true,
//    readOnly: Boolean = false,
//    textStyle: TextStyle = LocalTextStyle.current,
//    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Attached(),
//    labelText: String,
//    placeholder: @Composable (() -> Unit)? = null,
//    leadingIcon: @Composable (() -> Unit)? = null,
//    trailingIcon: @Composable (() -> Unit)? = null,
//    prefix: @Composable (() -> Unit)? = null,
//    suffix: @Composable (() -> Unit)? = null,
//    supportingText: @Composable (() -> Unit)? = null,
//    isError: Boolean = false,
//    inputTransformation: InputTransformation? = null,
//    outputTransformation: OutputTransformation? = null,
//    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
//    onKeyboardAction: KeyboardActionHandler? = null,
//    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
//    onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
//    scrollState: ScrollState = rememberScrollState(),
//    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
//    contentPadding: PaddingValues = OutlinedTextFieldDefaults.contentPadding(),
//    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
//    shape: Shape = RoundedCornerShape(8.dp),
//    isMandatory: Boolean = false
//) {
//    var isFocused by remember { mutableStateOf(false) }
//    OutlinedTextField(
//        state = state,
//        modifier = modifier.onFocusChanged { focusState ->
//            isFocused = focusState.isFocused
//        },
//        enabled = enabled,
//        readOnly = readOnly,
//        textStyle = if (isFocused || isError) {
//            LocalTextStyle.current.copy(color = Color.Unspecified)
//        } else {
//            MdTheme.typography.medium14TextStyle.copy(color = Color.Unspecified)
//        },
//        label = {
//            MdCustomLabel(
//                text = labelText,
//                isError = isError,
//                isFocused = isFocused,
//                isMandatory = isMandatory,
//                enabled = enabled
//            )
//        },
//        placeholder = placeholder,
//        leadingIcon = leadingIcon,
//        trailingIcon = trailingIcon,
//        prefix = prefix,
//        suffix = suffix,
//        supportingText = supportingText,
//        isError = isError,
//        inputTransformation = inputTransformation,
//        outputTransformation = outputTransformation,
//        keyboardOptions = keyboardOptions,
//        onKeyboardAction = onKeyboardAction,
//        lineLimits = lineLimits,
//        onTextLayout = onTextLayout,
//        scrollState = scrollState,
//        colors = MdFieldColors(),
//        contentPadding = contentPadding,
//        interactionSource = interactionSource,
//        shape = shape
//    )
//}


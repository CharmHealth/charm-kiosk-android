package com.charm.ehr.kiosk.presentation.composable

import UiText
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.domain.APIResponseError
import kotlinx.coroutines.launch

@Composable
fun MdVerticalSpace(numberOfSpaces: Float = 1f, verticalSpacerSize: Dp = 8.dp) {
    Spacer(modifier = Modifier.height(verticalSpacerSize * numberOfSpaces))
}

@Composable
fun MdHorizontalSpace( //TODO: Check and change not readable in the code
    numberOfSpaces: Float = 1f,
    spacerSize: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    Spacer(modifier = modifier.width(spacerSize * numberOfSpaces))
}

@Composable
fun AddVerticalSpace(verticalSpacerSize: Dp = 8.dp, numberOfSpaces: Int = 1) {
    Spacer(modifier = Modifier.height(verticalSpacerSize * numberOfSpaces))
}

@Composable
fun MdHalfVerticalSpaceBetweenField() {
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun CardMargin() {
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun CardMarginLeft() {
    Spacer(modifier = Modifier.width(8.dp))
}

@Composable
fun CardMarginRight() {
    Spacer(modifier = Modifier.height(8.dp))
}

val mdBasicFieldModifier = Modifier
    .fillMaxWidth(1f)
    .padding(0.dp, 0.dp, 0.dp, 0.dp)


//This is common UI state class for whole project
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    object Idle : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val apiResponseError: APIResponseError) : UiState<Nothing>()
}

//This is common UI state class for whole project
sealed class APIState<out T> {
    object Loading : APIState<Nothing>()
    data class Success<out T>(val data: T) : APIState<T>()
    data class Error(val apiResponseError: APIResponseError) : APIState<Nothing>()
}

data class MdFieldState(
    val state: TextFieldState = TextFieldState(),
    val error: UiText? = null,
    val isMandatory: Boolean = false
)

@Composable
fun Modifier.autoBringIntoView(): Modifier = composed {
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    bringIntoViewRequester(requester)
        .onFocusChanged {
            if (it.isFocused) {
                scope.launch {
                    requester.bringIntoView()
                }
            }
        }
}
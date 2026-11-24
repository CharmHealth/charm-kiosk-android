import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.composable.MdCustomLabel
import com.charm.ehr.kiosk.presentation.composable.MdSelectionDialogWithSearch
import com.charm.ehr.kiosk.presentation.theme.MdFieldColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun <T> DropDownWithSelectionDialog(
    modifier: Modifier = Modifier,
    options: List<T>,
    selectedOptions: List<T>,
    onOptionsSelected: (List<T>) -> Unit,
    labelText: String,
    dialogTitle: String,
    optionToString: (T) -> String,
    isMultiSelect: Boolean = false,
    isError: Boolean = false,
    textStyle: TextStyle = MdTheme.typography.medium16TextStyle,
    isMandatory: Boolean = false,
    enabled: Boolean = true,
) {
    var isDialogOpen by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        textStyle = textStyle,
        value = selectedOptions.firstOrNull()?.let(optionToString) ?: "",
        onValueChange = {},
        readOnly = true,
        maxLines = 1,
        isError = isError,
        supportingText = null,
        label = {
            MdCustomLabel(
                text = labelText,
                isError = isError,
                isFocused = isFocused,
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
        modifier = modifier
            .focusRequester(remember { FocusRequester() })
            .clickable(enabled = enabled) {
                isDialogOpen = true
            },
        colors = MdFieldColors(),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        interactionSource = remember { MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        if (it is PressInteraction.Release) {
                            isDialogOpen = true
                        }
                    }
                }
            }
    )

    if (isDialogOpen && enabled) {
        MdSelectionDialogWithSearch(
            data = options,
            dialogTitle = dialogTitle,
            isMultiSelect = isMultiSelect,
            itemToString = optionToString,
            onDismissRequest = {
                isDialogOpen = false
            },
            onResult = { selected ->
                onOptionsSelected(selected)
                isDialogOpen = false
            },
            preSelectedItems = selectedOptions
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun PreviewDropDownWithDialogSelector() {

    data class Book(val name: String, val id: Int)

    val options = listOf(
        Book("Book 1", 1),
        Book("Book 2", 2),
        Book("Book 3", 3)
    )
    val selectedOptions = remember { mutableStateOf(listOf<Book>()) }

    MdTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            DropDownWithSelectionDialog(
                options = options,
                selectedOptions = selectedOptions.value,
                onOptionsSelected = {
                    selectedOptions.value = it
                },
                labelText = "Select an Option",
                dialogTitle = "Choose Options",
                optionToString = { it.name }
            )
        }
    }

}
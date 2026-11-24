package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.util.DateUtil
import com.charm.ehr.kiosk.common.util.ModelUtil
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdFieldColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MdDateInputTextField(
    value: String,
    textStyle: TextStyle,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    isError: Boolean,
    labelText: String,
    placeholder: @Composable (() -> Unit),
    supportingText: @Composable (() -> Unit)? = null,
    minDate: LocalDate? = null, // format: MM/dd/yyyy
    maxDate: LocalDate? = null, // format: MM/dd/yyyy
    isMandatory: Boolean = false,
    isDobField: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }
    var textFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    var isInputError by remember { mutableStateOf(false) }
    var isMinDateError by remember { mutableStateOf(false) }
    var isMaxDateError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val minDateMillis = remember(minDate) {
        minDate?.toDateTimeAtStartOfDay()?.millis
    }
    val maxDateMillis = remember(maxDate) {
        maxDate?.plusDays(1)?.toDateTimeAtStartOfDay()?.millis
    }

    val datePickerState = rememberDatePickerState(
        initialDisplayMode = DisplayMode.Picker,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val afterMin = minDateMillis == null || utcTimeMillis >= minDateMillis
                val beforeMax = maxDateMillis == null || utcTimeMillis <= maxDateMillis
                return afterMin && beforeMax
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            colors = DatePickerDefaults.colors(
                containerColor = Color.White
            ),
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatted = DateTime(millis).toString(ModelUtil.mothFirstDateFormatWithSlash())
                            textFieldValue = TextFieldValue(
                                text = formatted,
                                selection = TextRange(formatted.length)
                            )
                            isInputError = false
                            isMinDateError = false
                            isMaxDateError = false
                            onValueChange(formatted)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = true,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White
                )
            )
        }
    }

    val localSupportingText: @Composable (() -> Unit)? =
        if (supportingText == null && isInputError) {
            when {
                isMinDateError -> {
                    {
                        Text(
                            stringResource(
                                R.string.min_date_check_msg,
                                minDate?.toString(ModelUtil.mothFirstDateFormatWithSlash()) ?: ""
                            )
                        )
                    }
                }

                isMaxDateError -> {
                    {
                        Text(
                            stringResource(
                                R.string.max_date_check_msg,
                                maxDate?.toString(ModelUtil.mothFirstDateFormatWithSlash()) ?: ""
                            )
                        )
                    }
                }

                else -> {
                    { Text(stringResource(R.string.enter_valid_date)) }
                }
            }
        } else supportingText



    OutlinedTextField(
        modifier = modifier
            .bringIntoViewRequester(requester)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (focusState.isFocused) {
                    scope.launch {
                        requester.bringIntoView()
                    }
                }
            },
        textStyle = textStyle,
        value = textFieldValue,
        isError = isError || isInputError,
        onValueChange = { newValue ->
            val isAdding = newValue.text.length > textFieldValue.text.length
            val result = DateUtil.validateAndFormatDateInput(
                newValue.text,
                minDate,
                maxDate,
                isAdding
            )
            if (result == null) {
                return@OutlinedTextField
            }
            isInputError = result.isInputError
            isMinDateError = result.isMinDateError
            isMaxDateError = result.isMaxDateError
            textFieldValue = TextFieldValue(
                text = result.formatted,
                selection = TextRange(result.formatted.length)
            )
            onValueChange(result.formatted)
        },
        label = {
            MdCustomLabel(
                text = labelText,
                isError = isError || isInputError,
                isMandatory = isMandatory,
                isFocused = isFocused,
                enabled = true
            )
        },
        placeholder = placeholder,
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                VerticalDivider(
                    color = MdColors.Grey30,
                    modifier = Modifier.height(28.dp),
                    thickness = 1.dp
                )
                IconButton(onClick = {
                    if (isDobField && textFieldValue.text.isBlank()) {
                        datePickerState.selectedDateMillis = defaultPickerDateMillis()
                    } else {
                        try {
                            val parsed = LocalDate.parse(
                                textFieldValue.text,
                                ModelUtil.mothFirstDateFormatWithSlash()
                            )
                            datePickerState.selectedDateMillis = parsed.toDateTimeAtStartOfDay().millis
                        } catch (_: Exception) {
                            // ignore if text is not a valid complete date
                        }
                    }
                    showDatePicker = true
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar),
                        tint = MdColors.BrandPink,
                        contentDescription = "Select Date of Birth"
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        supportingText = localSupportingText,
        keyboardActions = KeyboardActions(),
        shape = RoundedCornerShape(8.dp),
        colors = MdFieldColors()
    )
}

private fun defaultPickerDateMillis(): Long {
    val today = LocalDate.now()
    return LocalDate(2000, today.monthOfYear, today.dayOfMonth)
        .toDateTimeAtStartOfDay().millis
}

@Preview(showBackground = true)
@Composable
fun MdDateInputTextFieldPreview() {
    var dob by remember { mutableStateOf("") }
    val isError = false
    Box(modifier = Modifier.fillMaxSize()) {
        MdDateInputTextField(
            value = dob,
            onValueChange = { dob = it },
            labelText = "Date of Birth",
            placeholder = { Text(ModelUtil.displayDateFormateMonthFirst) },
            modifier = Modifier.fillMaxWidth(0.8f),
            textStyle = MdTheme.typography.regular14TextStyle,
            isError = false,
            supportingText = if (isError) {
                { Text("This field is required") }
            } else null,
            minDate = LocalDate.now().minusYears(100),
            maxDate = LocalDate.now()
        )
    }
}

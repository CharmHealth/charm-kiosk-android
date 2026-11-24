package com.charm.ehr.kiosk.presentation.dashboard.add_patient

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.util.ModelUtil
import com.charm.ehr.kiosk.presentation.composable.CircularImageViewWithPlaceholder
import com.charm.ehr.kiosk.presentation.composable.MdAlertDialog
import com.charm.ehr.kiosk.presentation.composable.MdCircularProgressBar
import com.charm.ehr.kiosk.presentation.composable.MdCustomOutlineTextField
import com.charm.ehr.kiosk.presentation.composable.MdDateInputTextField
import com.charm.ehr.kiosk.presentation.composable.MdDropDownMenu
import com.charm.ehr.kiosk.presentation.composable.MdHorizontalSpace
import com.charm.ehr.kiosk.presentation.composable.MdProgressDialog
import com.charm.ehr.kiosk.presentation.composable.MdVerticalSpace
import com.charm.ehr.kiosk.presentation.composable.SurfaceButtonIcon
import com.charm.ehr.kiosk.presentation.composable.autoBringIntoView
import com.charm.ehr.kiosk.presentation.composable.mdBasicFieldModifier
import com.charm.ehr.kiosk.presentation.dashboard.search_patient.AddPatientAction
import com.charm.ehr.kiosk.presentation.edit_profile.FieldState
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import org.joda.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientCard(
    state: AddPatientState,
    onAction: (AddPatientAction) -> Unit,
    imageLoader: ImageLoader,
    navigateToPatientProfile: (patientId: String) -> Unit,
    genderOptionList: List<String>
) {
    val focusManager = LocalFocusManager.current

    val firstNameFocusRequester = remember { FocusRequester() }
    val lastNameFocusRequester = remember { FocusRequester() }
    val birthSexFocusRequester = remember { FocusRequester() }
    val dateOfBirthFocusRequester = remember { FocusRequester() }

    val focusRequesters = remember {
        mapOf(
            AddPatientField.FIRST_NAME to firstNameFocusRequester,
            AddPatientField.LAST_NAME to lastNameFocusRequester,
            AddPatientField.BIRTH_SEX to birthSexFocusRequester,
            AddPatientField.DATE_OF_BIRTH to dateOfBirthFocusRequester
        )
    }

    LaunchedEffect(state.isPatAddedSuccessful) {
        if (state.isPatAddedSuccessful && state.patientId.isNotBlank()) {
            navigateToPatientProfile(state.patientId)
            onAction(AddPatientAction.OnClearState)
        }
    }

    LaunchedEffect(state.focusedField) {
        state.focusedField?.let { field ->
            focusRequesters[field]?.requestFocus()
            onAction(AddPatientAction.ClearFocusedField)
        }
    }

    if (state.isLoading) {
        MdProgressDialog()
    }

    if (state.errorMessage != null) {
        MdAlertDialog(
            heading = stringResource(R.string.api_not_success_error),
            message = state.errorMessage.asString(),
            primaryButtonResId = R.string.retry,
            secondaryButtonResId = R.string.cancel,
            onPrimaryAction = {
                onAction(AddPatientAction.OnClearErrorMessage)
                onAction(AddPatientAction.OnClickAddPatient)
            },
            onSecondaryAction = {
                onAction(AddPatientAction.OnClearErrorMessage)
            }
        )
    }

    if (state.duplicatePatient != null) {
        DuplicatePatientDialogue(
            currentPatient = state.getPatient(),
            existingPatient = state.duplicatePatient,
            imageLoader = imageLoader,
            onSelectPatient = { patient ->
                onAction(AddPatientAction.OnViewExistingPatient(patient.practicePatientId ?: ""))
            },
            addAsDuplicate = {
                onAction(AddPatientAction.OnAddAsDuplicate)
            },
            onDismissRequest = {
                onAction(AddPatientAction.OnDismissDuplicateDialog)
            }
        )
    }

    val valueStyle = MdTheme.typography.medium16TextStyle
    val supportingTextStyle = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)


    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.6f else .9f)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .padding(
                    start = 90.dp,
                    top = 8.dp,
                    end = 90.dp,
                    bottom = 8.dp
                )
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MdVerticalSpace(numberOfSpaces = 4f)
            Column(
                modifier = Modifier.fillMaxWidth(1f),
                verticalArrangement = Arrangement.Center
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (state.isProfilePicUploading) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .aspectRatio(1f)
                                .background(color = MdColors.Transparent, shape = CircleShape)
                                .border(1.dp, MdColors.Black10, CircleShape),
                            Alignment.Center
                        ) {
                            MdCircularProgressBar()
                        }
                    } else {
                        Box(
                            modifier = Modifier.clickable {
                                onAction(AddPatientAction.OnClickProfilePicture)
                            },
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularImageViewWithPlaceholder(
                                imageSize = 120.dp,
                                placeholderIcon = painterResource(R.drawable.ic_user),
                                placeholderColor = MdColors.Black30,
                                placeHolderIconSize = 40.dp,
                                borderWidth = 1.dp,
                                imageFilePath = state.imageFilePath ?: "",
                                imageLoader = imageLoader
                            )
                            SurfaceButtonIcon(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(40.dp),
                                painterResource(R.drawable.ic_camera)
                            )
                        }
                    }
                }
                MdVerticalSpace(numberOfSpaces = 4f)
                MdCustomOutlineTextField(
                    value = state.firstName.value ?: "",
                    modifier = mdBasicFieldModifier
                        .autoBringIntoView()
                        .focusRequester(firstNameFocusRequester),
                    labelText = stringResource(R.string.first_name),
                    isError = state.firstName.error != null,
                    supportingText = {
                        state.firstName.error?.let {
                            Text(it.asString(), style = supportingTextStyle)
                        }

                    },
                    onValueChange = {
                        onAction(AddPatientAction.OnFirstNameChange(it))
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    )
                )
                MdVerticalSpace(numberOfSpaces = 1.5f)
                MdCustomOutlineTextField(
                    value = state.lastName.value ?: "",
                    modifier = mdBasicFieldModifier
                        .autoBringIntoView()
                        .focusRequester(lastNameFocusRequester),
                    labelText = stringResource(R.string.last_name),
                    isError = state.lastName.error != null,
                    supportingText = {
                        state.lastName.error?.let {
                            Text(it.asString(), style = supportingTextStyle)
                        }

                    },
                    onValueChange = {
                        onAction(AddPatientAction.OnLastNameChange(it))
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    )
                )
                MdVerticalSpace(numberOfSpaces = 1.5f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    MdDropDownMenu(
                        textStyle = valueStyle,
                        modifier = Modifier.weight(1f),
                        options = genderOptionList,
                        selectedOption = state.birthSex.value
                            ?: "", //Can add with default gender option
                        onOptionSelected = {
                            onAction(AddPatientAction.OnGenderChange(it))
                        },
                        labelText = stringResource(R.string.birth_sex),
                        optionToString = {
                            it
                        },
                        isError = (state.birthSex.error != null),
                        supportingText = if (state.birthSex.error != null) {
                            {
                                Text(state.birthSex.error.asString(), style = supportingTextStyle)
                            }
                        } else null,
                        itemTextStyle = MdTheme.typography.medium16TextStyle,
                    )

                    MdHorizontalSpace(3f)

                    MdDateInputTextField(

                        modifier = Modifier.weight(1f).focusRequester(dateOfBirthFocusRequester),
                        value = state.dateOfBirth.value ?: "",
                        textStyle = valueStyle,
                        onValueChange = {
                            onAction(AddPatientAction.OnDobChange(it))
                        },
                        isError = (state.dateOfBirth.error != null),
                        labelText = stringResource(R.string.date_of_birth),
                        placeholder = {
                            Text(text = ModelUtil.displayDateFormateMonthFirst)
                        },
                        supportingText = if (state.dateOfBirth.error != null) {
                            {
                                Text(
                                    state.dateOfBirth.error.asString(),
                                    style = supportingTextStyle
                                )
                            }
                        } else null,
                        minDate = LocalDate.now().minusYears(100),
                        maxDate = LocalDate.now(),
                        isDobField = true
                    )

                }
                MdVerticalSpace(numberOfSpaces = 4f)
                Button(
                    onClick = {
                        onAction(AddPatientAction.OnClickAddPatient)
                    }, modifier = Modifier
                        .height(46.dp)
                        .fillMaxWidth(1f)
                ) {
                    Text(
                        text = stringResource(R.string.add_patient),
                        style = MdTheme.typography.semiBold18TextStyle.copy(color = MdColors.White)
                    )
                }
                MdVerticalSpace(numberOfSpaces = 3f)
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

}

@Preview(showBackground = true, widthDp = 600, heightDp = 1000)
@Composable
fun PreviewAddPatientCard() {
    MdTheme {
        AddPatientCard(
            state = AddPatientState(
                firstName = FieldState("John"),
                lastName = FieldState("Doe"),
                dateOfBirth = FieldState("10/02/1990"),
                birthSex = FieldState("Male"),
            ),
            onAction = {},
            imageLoader = ImageLoader.Builder(LocalContext.current).build(),
            navigateToPatientProfile = {}, listOf("Male", "Female")
        )
    }
}

@Preview(showBackground = true, widthDp = 1000, heightDp = 600)
@Composable
fun PreviewAddPatientCardLandscape() {
    MdTheme {
        AddPatientCard(
            state = AddPatientState(
                firstName = FieldState("John"),
                lastName = FieldState("Doe"),
                dateOfBirth = FieldState("10/02/1990"),
                birthSex = FieldState("Male"),
            ),
            onAction = {},
            imageLoader = ImageLoader.Builder(LocalContext.current).build(),
            navigateToPatientProfile = {}, listOf("Male", "Female")
        )
    }
}



//@Preview(showBackground = true, widthDp = 600, heightDp = 1000)
//@Composable
//fun PreviewWithErrorAddPatientCard() {
//    MdTheme {
//        AddPatientCard(
//            state = AddPatientState(
//                firstName = FieldState("John", error = UiText.DynamicString("Please enter first name")),
//                lastName = FieldState("Doe", error = UiText.DynamicString("Please enter last name")),
//                dateOfBirth = FieldState("10/02/1990", error = UiText.DynamicString("Please enter dob")),
//                birthSex = FieldState("male", error = UiText.DynamicString("Please select gender")),
//            ),
//            onAction = {},
//        )
//    }
//}

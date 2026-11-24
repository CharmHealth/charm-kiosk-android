package com.charm.ehr.kiosk.presentation.edit_profile

import DropDownWithSelectionDialog
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.util.FileUtil
import com.charm.ehr.kiosk.common.util.FileUtil.getFileFromFilePath
import com.charm.ehr.kiosk.common.util.ModelUtil
import com.charm.ehr.kiosk.domain.Country
import com.charm.ehr.kiosk.domain.DmCaregiverRelationship
import com.charm.ehr.kiosk.domain.DmEthnicity
import com.charm.ehr.kiosk.domain.PracticePayer
import com.charm.ehr.kiosk.domain.State
import com.charm.ehr.kiosk.presentation.composable.CircularImageViewWithPlaceholder
import com.charm.ehr.kiosk.presentation.composable.MdAlertDialog
import com.charm.ehr.kiosk.presentation.composable.MdBottomBarForForm
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdCircularProgressBar
import com.charm.ehr.kiosk.presentation.composable.MdCustomOutlineTextField
import com.charm.ehr.kiosk.presentation.composable.MdDateInputTextField
import com.charm.ehr.kiosk.presentation.composable.MdDropDownMenu
import com.charm.ehr.kiosk.presentation.composable.MdHorizontalSpace
import com.charm.ehr.kiosk.presentation.composable.MdTopToolBar
import com.charm.ehr.kiosk.presentation.composable.MdVerticalSpace
import com.charm.ehr.kiosk.presentation.composable.MultiSelectChipDialogField
import com.charm.ehr.kiosk.presentation.composable.SearchableDropDownDialog
import com.charm.ehr.kiosk.presentation.composable.SurfaceButtonIcon
import com.charm.ehr.kiosk.presentation.patient_profile.Section
import com.charm.ehr.kiosk.presentation.tele_health.HeadingText
import com.charm.ehr.kiosk.presentation.tele_health.SideMenuDetailHeading
import com.charm.ehr.kiosk.presentation.tele_health.SideMenuHeading
import com.charm.ehr.kiosk.presentation.tele_health.SideMenuItem
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.LocalDate

private val fieldModifier = Modifier
    .fillMaxWidth()
    .padding(vertical = 4.dp)

@Composable
fun EditProfileScreen(
    state: EditProfileState,
    onAction: (EditProfileAction) -> Unit,
    imageLoader: ImageLoader
) {
    val context = LocalContext.current
    if (state.toastMessage != null) {
        LaunchedEffect(state.toastMessage) {
            Toast.makeText(
                context,
                state.toastMessage,
                Toast.LENGTH_SHORT
            ).show()
            onAction(EditProfileAction.ClearToastMessage)
        }
    }

    Column(
        modifier = Modifier
            .background(MdColors.MdSurfaceColor)
            .windowInsetsPadding(
                WindowInsets.systemBars.union(WindowInsets.displayCutout)
            )
    ) {

        MdTopToolBar(
            headingText = stringResource(id = R.string.edit_profile), onBackClick = {
                onAction(EditProfileAction.OnBackPress)
            })
        MdCardWithoutTonalElevation(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row {

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(IntrinsicSize.Max)
                    //Temp make it wrap for content

                ) {

                    if (state.allowEditPatientDetails) {
                        MdVerticalSpace()
                        SideMenuHeading(
                            text = stringResource(id = R.string.profile),
                            showIcon = true,
                            iconResId = R.drawable.ic_profile
                        )
                        //Patient card
                        SideMenuItem(
                            stringResource(id = R.string.personal_information),
                            isSelected = isSectionSelected(
                                Section.PERSONAL_INFO, state.selectedSection
                            ),
                            onClick = {
                                onAction(EditProfileAction.OnSectionChange(Section.PERSONAL_INFO))
                            }
                        )
                        SideMenuItem(
                            stringResource(id = R.string.contact_information),
                            isSelected = isSectionSelected(
                                Section.CONTACT_INFO, state.selectedSection
                            ),
                            onClick = {
                                onAction(EditProfileAction.OnSectionChange(Section.CONTACT_INFO))
                            }
                        )
                        SideMenuItem(
                            stringResource(id = R.string.caregiver), isSelected = isSectionSelected(
                                Section.CAREGIVER_DETAILS, state.selectedSection
                            ),
                            onClick = {
                                onAction(EditProfileAction.OnSectionChange(Section.CAREGIVER_DETAILS))
                            }
                        )
                        MdVerticalSpace()

                    }
                    if (state.allowEditPatientDetails && state.isInsuranceRequired) {
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 0.dp, horizontal = 8.dp), // Optional padding
                            thickness = 1.dp, color = MdColors.Black10
                        )
                    }

                    if (state.isInsuranceRequired) {
                        MdVerticalSpace()
                        SideMenuHeading(
                            text = stringResource(id = R.string.insurance),
                            iconResId = R.drawable.ic_insurance,
                            showIcon = true
                        )

                        SideMenuItem(
                            stringResource(id = R.string.primary_insurance),
                            isSelected = isSectionSelected(
                                Section.PRIMARY_INSURANCE, state.selectedSection
                            ),
                            onClick = {
                                onAction(EditProfileAction.OnSectionChange(Section.PRIMARY_INSURANCE))
                            }
                        )

                        SideMenuItem(
                            stringResource(id = R.string.secondary_insurance),
                            isSelected = isSectionSelected(
                                Section.SECONDARY_INSURANCE, state.selectedSection
                            ),
                            onClick = {
                                onAction(EditProfileAction.OnSectionChange(Section.SECONDARY_INSURANCE))
                            }
                        )
                    }

                }

                VerticalDivider(
                    modifier = Modifier, // Optional padding
                    thickness = 1.dp, color = MdColors.Black10
                )


                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        MdCircularProgressBar()
                    }
                } else if (state.errorMessage != null) {
                    MdAlertDialog(
                        message = state.errorMessage.asString(),
                        primaryButtonResId = R.string.retry,
                        secondaryButtonResId = R.string.cancel,
                        onPrimaryAction = {
                            onAction(EditProfileAction.RetryLastFailedAction)
                        },
                        onSecondaryAction = {
                            onAction(EditProfileAction.ClearError)
                        })
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {

                        when (state.selectedSection) {
                            Section.PERSONAL_INFO -> {
                                PersonalInformationLazyColumn(state, onAction, imageLoader)
                            }

                            Section.CONTACT_INFO, Section.EMERGENCY_CONTACT -> {
                                ContactInformationLazyColumn(state, onAction)
                            }

                            Section.CAREGIVER_DETAILS -> {
                                CaregiverLazyColumn(state, onAction)
                            }

                            Section.PRIMARY_INSURANCE -> {
                                PrimaryInsuranceLazyColumn(state, onAction)
                            }

                            Section.SECONDARY_INSURANCE -> {
                                SecondaryInsuranceLazyColumn(state, onAction)
                            }
                        }

                        MdBottomBarForForm(
                            isSaveFlow = state.isSaveFlow,
                            onClickNext = {
                                onAction(EditProfileAction.OnClickNext(state.selectedSection))
                            },
                            onClickSave = {
                                onAction(EditProfileAction.OnClickSave(state.selectedSection))
                            },
                            showSkip = state.isShowSkip,
                            onClickSkip = {
                                onAction(EditProfileAction.OnClickSkip(state.selectedSection))
                            },
                            showPrevious = state.isShowPrevious,
                            onClickPrevious = {
                                onAction(EditProfileAction.OnClickPrevious(state.selectedSection))
                            }
                        )
                    }
                }

            }
        }

    }

}

@Composable
private fun ColumnScope.PersonalInformationLazyColumn(
    state: EditProfileState, onAction: (EditProfileAction) -> Unit, imageLoader: ImageLoader
) {
    val firstNameFocusRequester = remember { FocusRequester() }
    val lastNameFocusRequester = remember { FocusRequester() }
    val dateOfBirthFocusRequester = remember { FocusRequester() }
    val birthSexFocusRequester = remember { FocusRequester() }
    val genderIdentityFocusRequester = remember { FocusRequester() }
    val bloodGroupFocusRequester = remember { FocusRequester() }
    val raceFocusRequester = remember { FocusRequester() }
    val ethnicityFocusRequester = remember { FocusRequester() }
    val languageFocusRequester = remember { FocusRequester() }
    val smokingStatusFocusRequester = remember { FocusRequester() }
    val maritalStatusFocusRequester = remember { FocusRequester() }
    val employmentStatusFocusRequester = remember { FocusRequester() }

    val focusRequesters = mapOf(
        PersonalField.FIRST_NAME to firstNameFocusRequester,
        PersonalField.LAST_NAME to lastNameFocusRequester,
        PersonalField.DATE_OF_BIRTH to dateOfBirthFocusRequester,
        PersonalField.BIRTH_SEX to birthSexFocusRequester,
        PersonalField.GENDER_IDENTITY to genderIdentityFocusRequester,
        PersonalField.BLOOD_GROUP to bloodGroupFocusRequester,
        PersonalField.RACE to raceFocusRequester,
        PersonalField.ETHNICITY to ethnicityFocusRequester,
        PersonalField.LANGUAGE to languageFocusRequester,
        PersonalField.SMOKING_STATUS to smokingStatusFocusRequester,
        PersonalField.MARITAL_STATUS to maritalStatusFocusRequester,
        PersonalField.EMPLOYMENT_STATUS to employmentStatusFocusRequester
    )

    val listState = rememberLazyListState()


    val fieldIndexMap = mapOf(
        PersonalField.FIRST_NAME to 1,
        PersonalField.LAST_NAME to 1,
        PersonalField.DATE_OF_BIRTH to 2,
        PersonalField.BIRTH_SEX to 2,
        PersonalField.GENDER_IDENTITY to 3,
        PersonalField.BLOOD_GROUP to 5,
        PersonalField.RACE to 6,
        PersonalField.ETHNICITY to 7,
        PersonalField.LANGUAGE to 8,
        PersonalField.SMOKING_STATUS to 8,
        PersonalField.MARITAL_STATUS to 9,
        PersonalField.EMPLOYMENT_STATUS to 9
    )

    LaunchedEffect(state.focusedPersonalField) {
        state.focusedPersonalField?.let { field ->
            fieldIndexMap[field]?.let { index ->
                listState.animateScrollToItem(index)
            }
            focusRequesters[field]?.requestFocus()
            onAction(EditProfileAction.ClearFocusedPersonalField)
        }
    }

    val supportingTextStyle = MdTheme.typography.regular12TextStyle.copy(color = MdColors.Red)

    LazyColumn(
        state = listState,
        modifier = lazyColumnModifier()
    ) {
        item {
            HeadingText(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(id = R.string.personal_information)
            )
        }
        item {
            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(),
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
                            onAction(EditProfileAction.OnClickProfilePicture)
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularImageViewWithPlaceholder(
                            imageSize = 120.dp,
                            placeholderIcon = painterResource(R.drawable.ic_user),
                            placeholderColor = MdColors.Black30,
                            placeHolderIconSize = 40.dp,
                            borderWidth = 1.dp,
                            imageFilePath = state.personalInformation.profileImageFilePath.cacheFilePath,
                            imageUrl = state.personalInformation.profileImageFilePath.filePath,
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
        }
        item {
            Row {
                MdCustomOutlineTextField(
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(firstNameFocusRequester),
                    labelText = stringResource(id = R.string.first_name),
                    isMandatory = state.personalInformation.firstName.isMandatory,
                    value = state.personalInformation.firstName.value ?: "",
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdatePersonalInformation(firstName = it))
                    },
                    maxLines = 1,
                    isError = state.personalInformation.firstName.error != null,
                    supportingText = if (state.personalInformation.firstName.error != null) {
                        {
                            Text(
                                state.personalInformation.firstName.error.asString(),
                                style = supportingTextStyle
                            )
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    )
                )
                MdHorizontalSpace(2f)
                MdCustomOutlineTextField(
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(lastNameFocusRequester),
                    labelText = stringResource(id = R.string.last_name),
                    isMandatory = state.personalInformation.lastName.isMandatory,
                    value = state.personalInformation.lastName.value ?: "",
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdatePersonalInformation(lastName = it))
                    },
                    maxLines = 1,
                    isError = state.personalInformation.lastName.error != null,
                    supportingText = if (state.personalInformation.lastName.error != null) {
                        {
                            Text(
                                state.personalInformation.lastName.error.asString(),
                                style = supportingTextStyle
                            )
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    )
                )
            }
        }
        item {
            Row {
                MdDateInputTextField(
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(dateOfBirthFocusRequester),
                    labelText = stringResource(id = R.string.date_of_birth),
                    isMandatory = state.personalInformation.dateOfBirth.isMandatory,
                    value = state.personalInformation.dateOfBirth.value ?: "",
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdatePersonalInformation(dateOfBirth = it))
                    },
                    textStyle = MdTheme.typography.medium14TextStyle,
                    isError = state.personalInformation.dateOfBirth.error != null,
                    placeholder = {
                        Text(text = ModelUtil.displayDateFormateMonthFirst)
                    },
                    supportingText = if (state.personalInformation.dateOfBirth.error != null) {
                        val error = state.personalInformation.dateOfBirth.error
                        {
                            Text(
                                error.asString(),
                                style = supportingTextStyle
                            )
                        }
                    } else null,
                    minDate = LocalDate.now().minusYears(100),
                    maxDate = LocalDate.now(),
                    isDobField = true
                )
                MdHorizontalSpace(2f)
                MdDropDownMenu(
                    textStyle = MdTheme.typography.medium14TextStyle,
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(birthSexFocusRequester),
                    options = state.demographicsMetrics?.birthSex ?: emptyList(),
                    selectedOption = state.personalInformation.birthSex.value,
                    onOptionSelected = {
                        onAction(EditProfileAction.OnUpdatePersonalInformation(birthSex = it))
                    },
                    labelText = stringResource(id = R.string.birth_sex),
                    isMandatory = state.personalInformation.birthSex.isMandatory,
                    optionToString = {
                        it
                    },
                    isError = state.personalInformation.birthSex.error != null,
                    maxLines = 1,
                    itemTextStyle = MdTheme.typography.medium14TextStyle,
                    supportingText = if (state.personalInformation.birthSex.error != null) {
                        {
                            Text(
                                state.personalInformation.birthSex.error.asString(),
                                style = supportingTextStyle
                            )

                        }
                    } else null
                )
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                MdDropDownMenu(
                    textStyle = MdTheme.typography.medium14TextStyle,
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(genderIdentityFocusRequester),
                    options = state.demographicsMetrics?.genderIdentity ?: emptyList(),
                    selectedOption = state.personalInformation.genderIdentity.value,
                    onOptionSelected = {
                        onAction(EditProfileAction.OnUpdatePersonalInformation(genderIdentity = it))
                    },
                    labelText = stringResource(id = R.string.gender_identity),
                    isMandatory = state.personalInformation.genderIdentity.isMandatory,
                    optionToString = {
                        it.value
                    },
                    isError = (state.personalInformation.genderIdentity.error != null),
                    itemTextStyle = MdTheme.typography.medium14TextStyle
                )
                MdHorizontalSpace(2f)
                Spacer(modifier = Modifier.weight(0.5f))
            }
        }
        item {
            HeadingText(
                modifier = Modifier.padding(top = 24.dp),
                text = stringResource(id = R.string.additional_information)
            )
        }
        item {
            Row {
                MdDropDownMenu(
                    textStyle = MdTheme.typography.medium14TextStyle,
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(bloodGroupFocusRequester),
                    options = state.demographicsMetrics?.bloodGroup ?: emptyList(),
                    selectedOption = state.personalInformation.bloodGroup.value,
                    onOptionSelected = {
                        onAction(EditProfileAction.OnUpdatePersonalInformation(bloodGroup = it))
                    },
                    labelText = stringResource(id = R.string.blood_group),
                    isMandatory = state.personalInformation.bloodGroup.isMandatory,

                    optionToString = {
                        it
                    },
                    isError = (state.personalInformation.bloodGroup.error != null),
                    itemTextStyle = MdTheme.typography.medium14TextStyle,
                )
                MdHorizontalSpace(2f)
                Spacer(modifier = Modifier.weight(0.5f))
            }
        }
        item {
            MultiSelectChipDialogField(
                modifier = fieldModifier
                    .fillMaxWidth()
                    .focusRequester(raceFocusRequester),
                fieldLabelText = stringResource(id = R.string.race),
                isMandatory = state.personalInformation.race.isMandatory,
                dialogTitle = stringResource(id = R.string.select_race),
                allOptions = state.demographicsMetrics?.race ?: emptyList(),
                selectedItems = state.personalInformation.race.value ?: emptyList(),
                itemToString = { it.value },
                onSelectionChanged = { selectedItems ->
                    onAction(EditProfileAction.OnUpdatePersonalInformation(race = selectedItems))
                },
                isDialogEnabled = state.personalInformation.race.value?.none { it.value == "Declined to specify" }
                    ?: true,
            )
        }
        item {
            MultiSelectChipDialogField(
                modifier = fieldModifier
                    .fillMaxWidth()
                    .focusRequester(ethnicityFocusRequester),
                fieldLabelText = stringResource(id = R.string.ethnicity),
                isMandatory = state.personalInformation.ethnicity.isMandatory,
                dialogTitle = stringResource(id = R.string.select_ethnicity),
                allOptions = state.demographicsMetrics?.ethnicity ?: emptyList(),
                selectedItems = state.personalInformation.ethnicity.value ?: emptyList(),
                itemToString = { it.value },
                onSelectionChanged = { selectedItems ->
                    onAction(EditProfileAction.OnUpdatePersonalInformation(ethnicity = selectedItems))
                },
                isDialogEnabled = (state.personalInformation.ethnicity.value?.none { it.value == "Declined to specify" })
                    ?: true,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DropDownWithSelectionDialog(
                    textStyle = MdTheme.typography.medium14TextStyle,
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(languageFocusRequester),
                    options = state.demographicsMetrics?.languages ?: emptyList(),
                    selectedOptions = listOf(state.personalInformation.language.value),
                    onOptionsSelected = {
                        onAction(EditProfileAction.OnUpdatePersonalInformation(language = it.firstOrNull()))
                    },
                    labelText = stringResource(id = R.string.language),
                    isMandatory = state.personalInformation.language.isMandatory,
                    optionToString = {
                        it?.value ?: ""
                    },
                    isMultiSelect = false,
                    isError = (state.personalInformation.language.error != null),
                    dialogTitle = stringResource(id = R.string.select_language_heading),
                )
                MdHorizontalSpace(2f)
                MdDropDownMenu(
                    textStyle = MdTheme.typography.medium14TextStyle,
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(smokingStatusFocusRequester),
                    options = state.demographicsMetrics?.smokingStatus ?: emptyList(),
                    selectedOption = state.personalInformation.smokingStatus.value,
                    onOptionSelected = {
                        onAction(EditProfileAction.OnUpdatePersonalInformation(smokingStatus = it))
                    },
                    labelText = stringResource(id = R.string.smoking_status),
                    isMandatory = state.personalInformation.smokingStatus.isMandatory,
                    optionToString = {
                        it
                    },
                    isError = (state.personalInformation.smokingStatus.error != null),
                    itemTextStyle = MdTheme.typography.medium14TextStyle,
                )
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                MdDropDownMenu(
                    textStyle = MdTheme.typography.medium14TextStyle,
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(maritalStatusFocusRequester),
                    options = state.demographicsMetrics?.maritalStatus ?: emptyList(),
                    selectedOption = state.personalInformation.maritalStatus.value,
                    onOptionSelected = {
                        onAction(EditProfileAction.OnUpdatePersonalInformation(maritalStatus = it))
                    },
                    labelText = stringResource(id = R.string.marital_status),
                    isMandatory = state.personalInformation.maritalStatus.isMandatory,
                    optionToString = {
                        it
                    },
                    isError = (state.personalInformation.maritalStatus.error != null),
                    itemTextStyle = MdTheme.typography.medium14TextStyle,
                )
                MdHorizontalSpace(2f)
                MdDropDownMenu(
                    textStyle = MdTheme.typography.medium14TextStyle,
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(employmentStatusFocusRequester),
                    options = state.demographicsMetrics?.employmentStatus ?: emptyList(),
                    selectedOption = state.personalInformation.employmentStatus.value,
                    onOptionSelected = {
                        onAction(EditProfileAction.OnUpdatePersonalInformation(employmentStatus = it))
                    },
                    labelText = stringResource(id = R.string.employment_status),
                    isMandatory = state.personalInformation.employmentStatus.isMandatory,

                    optionToString = {
                        it
                    },
                    maxLines = 1,
                    isError = (state.personalInformation.employmentStatus.error != null),
                    itemTextStyle = MdTheme.typography.medium14TextStyle,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.ContactInformationLazyColumn(
    state: EditProfileState, onAction: (EditProfileAction) -> Unit
) {
    val emailFocusRequester = remember { FocusRequester() }
    val primaryPhoneFocusRequester = remember { FocusRequester() }
    val mobilePhoneFocusRequester = remember { FocusRequester() }
    val homePhoneFocusRequester = remember { FocusRequester() }
    val workPhoneFocusRequester = remember { FocusRequester() }
    val addressLine1FocusRequester = remember { FocusRequester() }
    val focusRequesters = mapOf(
        ContactField.EMAIL to emailFocusRequester,
        ContactField.PRIMARY_PHONE to primaryPhoneFocusRequester,
        ContactField.MOBILE_PHONE to mobilePhoneFocusRequester,
        ContactField.HOME_PHONE to homePhoneFocusRequester,
        ContactField.WORK_PHONE to workPhoneFocusRequester,
        ContactField.ADDRESS_LINE_1 to addressLine1FocusRequester
    )

    val listState = rememberLazyListState()
    val emailErrorMsg = stringResource(R.string.invalid_email_error)

    val fieldIndexMap = mapOf(
        ContactField.EMAIL to 2,
        ContactField.PRIMARY_PHONE to 3,
        ContactField.MOBILE_PHONE to 3,
        ContactField.HOME_PHONE to 4,
        ContactField.WORK_PHONE to 5,
        ContactField.ADDRESS_LINE_1 to 8
    )

    val supportingTextStyle = MdTheme.typography.regular12TextStyle.copy(color = MdColors.Red)

    LaunchedEffect(state.focusedContactField) {
        state.focusedContactField?.let { field ->
            fieldIndexMap[field]?.let { index ->
                listState.animateScrollToItem(index)
            }
            focusRequesters[field]?.requestFocus()
            onAction(EditProfileAction.ClearFocusedContactField)
        }
    }
    LazyColumn(
        modifier = lazyColumnModifier(),
        state = listState
    ) {
        item {
            HeadingText(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(id = R.string.contact_details)
            )
        }
        item {
            MdDropDownMenu(
                textStyle = MdTheme.typography.medium14TextStyle,
                modifier = fieldModifier,
                maxLines = 1,
                options = state.demographicsMetrics?.preferredCommunication ?: emptyList(),
                selectedOption = state.contactInformation.preferredCommunication.value,
                onOptionSelected = {
                    onAction(EditProfileAction.OnUpdateContactInformation(preferredCommunication = it))
                },
                labelText = stringResource(id = R.string.preferred_communication),
                optionToString = {
                    it
                },
                isError = (state.contactInformation.preferredCommunication.error != null),
                itemTextStyle = MdTheme.typography.medium14TextStyle
            )
        }

        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier.focusRequester(emailFocusRequester),
                labelText = stringResource(id = R.string.email),
                isMandatory = state.contactInformation.email.isMandatory,

                value = state.contactInformation.email.value ?: "",
                onValueChange = {
                    onAction(EditProfileAction.OnUpdateContactInformation(email = it))
                    val isValid = ModelUtil.isValidEmail(it)
                    val errorMsg = if (it.isNotEmpty() && !isValid) emailErrorMsg else null
                    onAction(EditProfileAction.OnUpdateContactInformation(emailError = errorMsg))
                },
                isError = state.contactInformation.email.error != null,
                supportingText = if (state.contactInformation.email.error != null) {
                    {
                        Text(
                            state.contactInformation.email.error.asString(),
                            style = supportingTextStyle
                        )
                    }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
        }


        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                MdDropDownMenu(
                    textStyle = MdTheme.typography.medium14TextStyle,
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(primaryPhoneFocusRequester),
                    options = ModelUtil.primaryPhoneOptions,
                    selectedOption = state.contactInformation.primaryPhone.value,
                    onOptionSelected = {
                        onAction(EditProfileAction.OnUpdateContactInformation(primaryPhone = it))
                    },
                    labelText = stringResource(id = R.string.primary_phone),
                    isMandatory = state.contactInformation.primaryPhone.isMandatory,
                    optionToString = {
                        it
                    },
                    isError = state.contactInformation.primaryPhone.error != null,
                    supportingText = if (state.contactInformation.primaryPhone.error != null) {
                        {
                            Text(
                                state.contactInformation.primaryPhone.error.asString(),
                                style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                            )
                        }
                    } else null,
                    itemTextStyle = MdTheme.typography.medium14TextStyle
                )

                MdHorizontalSpace(2f)

                MdCustomOutlineTextField(
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(mobilePhoneFocusRequester),
                    labelText = stringResource(id = R.string.mobile_phone),
                    isMandatory = state.contactInformation.mobilePhone.isMandatory,
                    value = state.contactInformation.mobilePhone.value ?: "",
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdateContactInformation(mobilePhone = it))
                    },
                    isError = state.contactInformation.mobilePhone.error != null,
                    supportingText =
                        if (state.contactInformation.mobilePhone.error != null) {
                            {
                                Text(
                                    state.contactInformation.mobilePhone.error.asString(),
                                    style = supportingTextStyle
                                )
                            }
                        } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        }

        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier.focusRequester(homePhoneFocusRequester), labelText =
                    stringResource(id = R.string.home_phone),
                isMandatory = state.contactInformation.homePhone.isMandatory,
                value = state.contactInformation.homePhone.value ?: "", onValueChange = {
                    onAction(EditProfileAction.OnUpdateContactInformation(homePhone = it))
                }, isError = state.contactInformation.homePhone.error != null,
                supportingText = if (state.contactInformation.homePhone.error != null) {
                    {
                        Text(
                            state.contactInformation.homePhone.error.asString(),
                            style = supportingTextStyle
                        )
                    }
                } else null, keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                )
            )
        }

        item {
            Row {
                MdCustomOutlineTextField(
                    modifier = fieldModifier
                        .weight(1f)
                        .focusRequester(workPhoneFocusRequester),
                    labelText =
                        stringResource(id = R.string.work_phone),
                    isMandatory = state.contactInformation.workPhone.isMandatory,
                    value = state.contactInformation.workPhone.value ?: "",
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdateContactInformation(workPhone = it))
                    },
                    isError = state.contactInformation.workPhone.error != null,
                    supportingText = if (state.contactInformation.workPhone.error != null) {
                        {
                            Text(
                                state.contactInformation.workPhone.error.asString(),
                                style = supportingTextStyle
                            )
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                MdHorizontalSpace(2f)

                MdCustomOutlineTextField(
                    modifier = fieldModifier.weight(0.45f),
                    labelText = stringResource(R.string.extn),
                    value = state.contactInformation.workPhoneExtn.value ?: "",
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdateContactInformation(workPhoneExtn = it))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }

        }

        item {
            HeadingText(
                modifier = Modifier.padding(top = 24.dp),
                text = stringResource(id = R.string.address_detail)
            )
        }

        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier.focusRequester(addressLine1FocusRequester), labelText =
                    stringResource(id = R.string.address_line_1),
                isMandatory = state.contactInformation.addressLine1.isMandatory,
                value = state.contactInformation.addressLine1.value ?: "", onValueChange = {
                    onAction(EditProfileAction.OnUpdateContactInformation(addressLine1 = it))
                }, isError = state.contactInformation.addressLine1.error != null,
                supportingText = if (state.contactInformation.addressLine1.error != null) {
                    {
                        Text(
                            state.contactInformation.addressLine1.error.asString(),
                            style = supportingTextStyle
                        )
                    }
                } else null, maxLines = Int.MAX_VALUE)
        }

        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier, labelText = stringResource(R.string.address_line_2),
                value = state.contactInformation.addressLine2.value ?: "", onValueChange = {
                    onAction(EditProfileAction.OnUpdateContactInformation(addressLine2 = it))
                }, maxLines = Int.MAX_VALUE
            )
        }

        item {
            Row {
                MdCustomOutlineTextField(
                    modifier = fieldModifier.weight(0.5f), labelText =
                        stringResource(R.string.cityStr),
                    value = state.contactInformation.city.value ?: "", onValueChange = {
                        onAction(EditProfileAction.OnUpdateContactInformation(city = it))
                    })

                MdHorizontalSpace(2f)

                if (state.contactInformation.country.value?.value in ModelUtil.supportedStateCountries) {
                    val stateOptions = state.contactInformation.stateList
                    DropDownWithSelectionDialog(
                        textStyle = MdTheme.typography.medium14TextStyle,
                        modifier = fieldModifier.weight(0.5f),
                        options = stateOptions,
                        selectedOptions = listOf(stateOptions.find { it.value == state.contactInformation.state.value?.value }),
                        onOptionsSelected = {
                            onAction(EditProfileAction.OnUpdateContactInformation(state = it.firstOrNull()))
                        },
                        labelText =
                            stringResource(R.string.stateStr),
                        optionToString = {
                            it?.label ?: ""
                        },
                        isError = (state.contactInformation.country.error != null),
                        dialogTitle = stringResource(R.string.select_state_heading),
                        isMultiSelect = false,
                    )
                } else {
                    MdCustomOutlineTextField(
                        modifier = fieldModifier.weight(0.5f),
                        labelText =
                            stringResource(R.string.stateStr),

                        value = state.contactInformation.state.value?.label ?: "",
                        onValueChange = {
                            if (it.length <= 50) {
                                onAction(
                                    EditProfileAction.OnUpdateContactInformation(
                                        state = State(label = it, value = it)
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }

        item {
            Row {
                DropDownWithSelectionDialog(
                    textStyle = MdTheme.typography.medium14TextStyle,
                    modifier = fieldModifier.weight(0.5f),
                    options = state.countryList,
                    selectedOptions = listOf(state.contactInformation.country.value),
                    onOptionsSelected = {
                        if (it.firstOrNull() != null && it.firstOrNull()?.value != state.contactInformation.country.value?.value) {
                            onAction(EditProfileAction.OnUpdateContactInformation(country = it.firstOrNull()))
                            onAction(EditProfileAction.OnRequestStatesContactInfo(it.firstOrNull()))
                        }
                    },
                    labelText = stringResource(R.string.country),
                    optionToString = {
                        it?.label ?: ""
                    },
                    isError = (state.contactInformation.country.error != null),
                    dialogTitle = stringResource(id = R.string.select_country_heading),
                    isMultiSelect = false
                )


                MdHorizontalSpace(2f)

                MdCustomOutlineTextField(
                    modifier = fieldModifier.weight(0.5f),
                    labelText = stringResource(R.string.postal_code),
                    value = state.contactInformation.postalCode.value ?: "",
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdateContactInformation(postalCode = it))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

            }
        }

        item {
            HeadingText(
                modifier = Modifier.padding(top = 24.dp),
                text = stringResource(id = R.string.emergency_contact)
            )
        }

        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier,
                labelText = stringResource(R.string.contact_name),
                value = state.contactInformation.emergencyContactName.value ?: "",
                onValueChange = {
                    onAction(EditProfileAction.OnUpdateContactInformation(emergencyContactName = it))
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                )
            )
        }

        item {
            Row {
                MdCustomOutlineTextField(
                    modifier = fieldModifier.weight(1f),
                    labelText = stringResource(R.string.contact_number),
                    value = state.contactInformation.emergencyContactNumber.value ?: "",
                    onValueChange = {
                        onAction(
                            EditProfileAction.OnUpdateContactInformation(
                                emergencyContactNumber = it
                            )
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                MdHorizontalSpace(2f)

                MdCustomOutlineTextField(
                    modifier = fieldModifier.weight(0.45f),
                    labelText = stringResource(R.string.extn),
                    value = state.contactInformation.emergencyContactExtn.value ?: "",
                    onValueChange = {
                        onAction(
                            EditProfileAction.OnUpdateContactInformation(
                                emergencyContactExtn = it
                            )
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

        }

        item {
            MdVerticalSpace(4f)
        }

    }
}

@Composable
private fun ColumnScope.CaregiverLazyColumn(
    state: EditProfileState,
    onAction: (EditProfileAction) -> Unit
) {
    val deleteCaregiverType = remember { mutableStateOf<String?>(null) }

    LazyColumn(modifier = lazyColumnModifier()) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CaregiverColumn(
                    caregiverDetails = state.primaryCaregiverDetails ?: PersonDetail(),
                    state = state,
                    onAction = { field, value ->
                        onAction(
                            when (field) {
                                CaregiverField.RELATIONSHIP -> EditProfileAction.OnUpdatePrimaryCaregiverDetails(
                                    relationship = value as DmCaregiverRelationship?
                                )

                                CaregiverField.FIRST_NAME -> EditProfileAction.OnUpdatePrimaryCaregiverDetails(
                                    firstName = value as String?
                                )

                                CaregiverField.LAST_NAME -> EditProfileAction.OnUpdatePrimaryCaregiverDetails(
                                    lastName = value as String?
                                )

                                CaregiverField.DATE_OF_BIRTH -> EditProfileAction.OnUpdatePrimaryCaregiverDetails(
                                    dateOfBirth = value as String?
                                )

                                CaregiverField.BIRTH_SEX -> EditProfileAction.OnUpdatePrimaryCaregiverDetails(
                                    birthSex = value as String?
                                )

                                CaregiverField.ADDRESS_LINE_1 -> EditProfileAction.OnUpdatePrimaryCaregiverDetails(
                                    addressLine1 = value as String?
                                )

                                CaregiverField.ADDRESS_LINE_2 -> EditProfileAction.OnUpdatePrimaryCaregiverDetails(
                                    addressLine2 = value as String?
                                )

                                CaregiverField.CITY -> EditProfileAction.OnUpdatePrimaryCaregiverDetails(
                                    city = value as String?
                                )

                                CaregiverField.STATE -> EditProfileAction.OnUpdatePrimaryCaregiverDetails(
                                    state = value as State?
                                )

                                CaregiverField.COUNTRY -> EditProfileAction.OnUpdatePrimaryCaregiverDetails(
                                    country = value as Country?
                                )

                                CaregiverField.POSTAL_CODE -> EditProfileAction.OnUpdatePrimaryCaregiverDetails(
                                    postalCode = value as String?
                                )

                                CaregiverField.MOBILE_PHONE -> EditProfileAction.OnUpdatePrimaryCaregiverDetails(
                                    mobilePhone = value as String?
                                )

                                CaregiverField.HOME_PHONE -> EditProfileAction.OnUpdatePrimaryCaregiverDetails(
                                    homePhone = value as String?
                                )

                                CaregiverField.IS_COPY_PATIENT_ADDRESS -> EditProfileAction.OnUpdatePrimaryCaregiverDetails(
                                    isCopyPatientAddress = value as Boolean?
                                )
                            }
                        )
                    },
                    state.focusedPCaregiverField,
                    { country ->
                        onAction(EditProfileAction.OnRequestStatesPrimaryCaregiver(country))
                    },
                    caregiverText = stringResource(id = R.string.primary_caregiver),
                    onActionDelete = {
                        deleteCaregiverType.value = "PRIMARY"
                    },
                    allowDelete = state.primaryCaregiverDetails != null,
                    clearFocusedField = {
                        onAction(EditProfileAction.ClearFocusedPCaregiverField)
                    }
                )
                if (state.primaryCaregiverDetails != null) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 1.dp,
                        color = MdColors.Black10
                    )
                }
            }
        }
        if (state.primaryCaregiverDetails != null) {
            if (state.secondaryCaregiverDetails == null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedButton(
                            onClick = { onAction(EditProfileAction.OnClickAddSecondaryCaregiver) },
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text(text = stringResource(id = R.string.add_caregiver))
                        }
                    }
                }
            } else {
                item {
                    CaregiverColumn(
                        caregiverDetails = state.secondaryCaregiverDetails,
                        state = state,
                        onAction = { field, value ->
                            onAction(
                                when (field) {
                                    CaregiverField.RELATIONSHIP -> EditProfileAction.OnUpdateSecondaryCaregiverDetails(
                                        relationship = value as DmCaregiverRelationship?
                                    )

                                    CaregiverField.FIRST_NAME -> EditProfileAction.OnUpdateSecondaryCaregiverDetails(
                                        firstName = value as String?
                                    )

                                    CaregiverField.LAST_NAME -> EditProfileAction.OnUpdateSecondaryCaregiverDetails(
                                        lastName = value as String?
                                    )

                                    CaregiverField.DATE_OF_BIRTH -> EditProfileAction.OnUpdateSecondaryCaregiverDetails(
                                        dateOfBirth = value as String?
                                    )

                                    CaregiverField.BIRTH_SEX -> EditProfileAction.OnUpdateSecondaryCaregiverDetails(
                                        birthSex = value as String?
                                    )

                                    CaregiverField.ADDRESS_LINE_1 -> EditProfileAction.OnUpdateSecondaryCaregiverDetails(
                                        addressLine1 = value as String?
                                    )

                                    CaregiverField.ADDRESS_LINE_2 -> EditProfileAction.OnUpdateSecondaryCaregiverDetails(
                                        addressLine2 = value as String?
                                    )

                                    CaregiverField.CITY -> EditProfileAction.OnUpdateSecondaryCaregiverDetails(
                                        city = value as String?
                                    )

                                    CaregiverField.STATE -> EditProfileAction.OnUpdateSecondaryCaregiverDetails(
                                        state = value as State?
                                    )

                                    CaregiverField.COUNTRY -> EditProfileAction.OnUpdateSecondaryCaregiverDetails(
                                        country = value as Country?
                                    )

                                    CaregiverField.POSTAL_CODE -> EditProfileAction.OnUpdateSecondaryCaregiverDetails(
                                        postalCode = value as String?
                                    )

                                    CaregiverField.MOBILE_PHONE -> EditProfileAction.OnUpdateSecondaryCaregiverDetails(
                                        mobilePhone = value as String?
                                    )

                                    CaregiverField.HOME_PHONE -> EditProfileAction.OnUpdateSecondaryCaregiverDetails(
                                        homePhone = value as String?
                                    )

                                    CaregiverField.IS_COPY_PATIENT_ADDRESS -> EditProfileAction.OnUpdateSecondaryCaregiverDetails(
                                        isCopyPatientAddress = value as Boolean?
                                    )
                                }
                            )
                        },
                        state.focusedSCaregiverField,
                        { country ->
                            onAction(EditProfileAction.OnRequestStatesSecondaryCaregiver(country))
                        },
                        caregiverText = stringResource(id = R.string.secondary_caregiver),
                        onActionDelete = {
                            deleteCaregiverType.value = "SECONDARY"
                        }, clearFocusedField = {
                            onAction(EditProfileAction.ClearFocusedSCaregiverField)
                        })
                }
            }
        }
    }
    if (deleteCaregiverType.value != null) {
        MdAlertDialog(
            message = stringResource(id = R.string.delete_caregiver_confirm),
            primaryButtonResId = R.string.alertDialogYesTextNoI18N,
            secondaryButtonResId = R.string.alertDialogNoTextNoI18N,
            onPrimaryAction = {
                when (deleteCaregiverType.value) {
                    "PRIMARY" -> onAction(EditProfileAction.OnDeletePrimaryCaregiver)
                    "SECONDARY" -> onAction(EditProfileAction.OnDeleteSecondaryCaregiver)
                }
                deleteCaregiverType.value = null
            },
            onSecondaryAction = {
                deleteCaregiverType.value = null
            }
        )
    }
}

@Composable
private fun CaregiverColumn(
    caregiverDetails: PersonDetail,
    state: EditProfileState,
    onAction: (CaregiverField, Any?) -> Unit,
    focusedCaregiverField: CaregiverField? = null,
    onStateUpdateRequest: (Country?) -> Unit,
    caregiverText: String,
    onActionDelete: () -> Unit,
    allowDelete: Boolean = true,
    clearFocusedField: () -> Unit
) {
    val relationshipFocusRequester = remember { FocusRequester() }
    val firstNameFocusRequester = remember { FocusRequester() }
    val lastNameFocusRequester = remember { FocusRequester() }
    val dobFocusRequester = remember { FocusRequester() }

    val focusRequesters = mapOf(
        CaregiverField.RELATIONSHIP to relationshipFocusRequester,
        CaregiverField.FIRST_NAME to firstNameFocusRequester,
        CaregiverField.LAST_NAME to lastNameFocusRequester,
        CaregiverField.DATE_OF_BIRTH to dobFocusRequester
    )

    val supportingTextStyle = MdTheme.typography.regular12TextStyle.copy(color = MdColors.Red)

    LaunchedEffect(focusedCaregiverField) {
        focusedCaregiverField?.let { field ->
            focusRequesters[field]?.requestFocus()
            clearFocusedField()
        }
    }

    Column {

        Box(
            modifier = Modifier
                .wrapContentWidth(), contentAlignment = Alignment.Center
        ) {
            val caregiverTextModifier =
                if (allowDelete) Modifier else Modifier.padding(top = 16.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = caregiverText,
                    modifier = caregiverTextModifier
                        .weight(1f),
                    style = MdTheme.typography.semiBold18TextStyle.copy(fontSize = 16.sp),
                    textAlign = TextAlign.Start
                )
                if (allowDelete) {
                    IconButton(
                        onClick = { onActionDelete() },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = null,
                            tint = MdColors.BrandPink
                        )
                    }
                }
            }
        }

        MdVerticalSpace()

        MdDropDownMenu(
            textStyle = MdTheme.typography.medium14TextStyle,
            modifier = fieldModifier.focusRequester(relationshipFocusRequester),
            options = state.demographicsMetrics?.caregiverRelationship ?: emptyList(),
            selectedOption = caregiverDetails.relationship.value,
            onOptionSelected = {
                onAction(CaregiverField.RELATIONSHIP, it)
            },
            labelText = stringResource(id = R.string.relationship),
            isMandatory = caregiverDetails.relationship.isMandatory,
            optionToString = {
                it.value
            },
            isError = (caregiverDetails.relationship.error != null),
            supportingText = if (caregiverDetails.relationship.error != null) {
                {
                    Text(
                        caregiverDetails.relationship.error.asString(),
                        style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                    )
                }
            } else null,
            itemTextStyle = MdTheme.typography.medium14TextStyle,
        )

        Row {
            MdCustomOutlineTextField(
                modifier = fieldModifier
                    .weight(0.5f)
                    .focusRequester(firstNameFocusRequester),
                labelText = stringResource(id = R.string.first_name),
                isMandatory = caregiverDetails.firstName.isMandatory,
                value = caregiverDetails.firstName.value ?: "",
                onValueChange = {
                    onAction(CaregiverField.FIRST_NAME, it)
                },
                isError = (caregiverDetails.firstName.error != null),
                supportingText = if (caregiverDetails.firstName.error != null) {
                    {
                        Text(
                            caregiverDetails.firstName.error.asString(),
                            style = supportingTextStyle
                        )
                    }
                } else null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                )
            )

            MdHorizontalSpace(2f)

            MdCustomOutlineTextField(
                modifier = fieldModifier
                    .weight(0.5f)
                    .focusRequester(lastNameFocusRequester),
                labelText = stringResource(id = R.string.last_name),
                isMandatory = caregiverDetails.lastName.isMandatory,
                value = caregiverDetails.lastName.value ?: "",
                onValueChange = {
                    onAction(CaregiverField.LAST_NAME, it)
                },
                isError = (caregiverDetails.lastName.error != null),
                supportingText = if (caregiverDetails.lastName.error != null) {
                    {
                        Text(
                            caregiverDetails.lastName.error.asString(),
                            style = supportingTextStyle
                        )
                    }
                } else null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                )
            )
        }



        Row {
            MdDateInputTextField(
                value = caregiverDetails.dateOfBirth.value ?: "",
                textStyle = MdTheme.typography.medium14TextStyle,
                onValueChange = {
                    onAction(CaregiverField.DATE_OF_BIRTH, it)
                },
                modifier = fieldModifier
                    .weight(0.5f)
                    .focusRequester(dobFocusRequester),
                isError = caregiverDetails.dateOfBirth.error != null,
                labelText =
                    stringResource(id = R.string.date_of_birth),
                placeholder = {
                    Text(
                        text = ModelUtil.displayDateFormateMonthFirst,
                        style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Black40)
                    )
                }, supportingText = if (caregiverDetails.dateOfBirth.error != null) {
                    val error = caregiverDetails.dateOfBirth.error
                    {
                        Text(
                            error.asString(),
                            style = supportingTextStyle
                        )
                    }
                } else null, minDate = LocalDate.now().minusYears(100),
                maxDate = LocalDate.now(),
                isDobField = true)

            MdHorizontalSpace(2f)

            MdDropDownMenu(
                textStyle = MdTheme.typography.medium14TextStyle,
                modifier = fieldModifier.weight(0.5f),
                options = state.demographicsMetrics?.birthSex ?: emptyList(),
                selectedOption = caregiverDetails.birthSex.value,
                onOptionSelected = {
                    onAction(CaregiverField.BIRTH_SEX, it)
                },
                labelText = stringResource(R.string.birth_sex),
                optionToString = {
                    it
                },
                isError = (caregiverDetails.birthSex.error != null),
                itemTextStyle = MdTheme.typography.medium14TextStyle,
            )

        }



        MdVerticalSpace(1f)
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SideMenuDetailHeading(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(id = R.string.contact_details)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onAction(
                            CaregiverField.IS_COPY_PATIENT_ADDRESS,
                            !caregiverDetails.isCopyPatientAddress
                        )
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Checkbox(
                    checked = caregiverDetails.isCopyPatientAddress,
                    onCheckedChange = {
                        onAction(CaregiverField.IS_COPY_PATIENT_ADDRESS, it)
                    },
                    modifier = Modifier,
                )
                Text(
                    text = stringResource(id = R.string.same_patient_contact),
                    style = MdTheme.typography.medium14TextStyle,
                    modifier = Modifier.padding(start = 0.dp)
                )
            }
        }



        MdCustomOutlineTextField(
            modifier = fieldModifier,
            labelText = stringResource(R.string.address_line_1),
            value = caregiverDetails.addressLine1.value ?: "",
            onValueChange = {
                onAction(CaregiverField.ADDRESS_LINE_1, it)
            },
            enabled = !caregiverDetails.isCopyPatientAddress,
            maxLines = Int.MAX_VALUE
        )



        MdCustomOutlineTextField(
            modifier = fieldModifier,
            labelText = stringResource(R.string.address_line_2),
            value = caregiverDetails.addressLine2.value ?: "",
            onValueChange = {
                onAction(CaregiverField.ADDRESS_LINE_2, it)
            },
            enabled = !caregiverDetails.isCopyPatientAddress,
            maxLines = Int.MAX_VALUE
        )



        Row {
            MdCustomOutlineTextField(
                modifier = fieldModifier.weight(0.5f),
                labelText =
                    stringResource(R.string.cityStr),

                value = caregiverDetails.city.value ?: "",
                onValueChange = {
                    onAction(CaregiverField.CITY, it)
                },
                enabled = !caregiverDetails.isCopyPatientAddress
            )

            MdHorizontalSpace(2f)

            if (caregiverDetails.country.value?.value in ModelUtil.supportedStateCountries) {
                DropDownWithSelectionDialog(
                    textStyle = MdTheme.typography.medium14TextStyle.copy(color = Color.Unspecified),
                    modifier = fieldModifier.weight(0.5f),
                    options = state.countryStateMap[caregiverDetails.country.value?.value]
                        ?: emptyList(),
                    selectedOptions = listOf(caregiverDetails.state.value),
                    onOptionsSelected = {
                        onAction(CaregiverField.STATE, it.firstOrNull())
                    },
                    labelText = stringResource(R.string.stateStr),
                    optionToString = {
                        it?.value ?: ""
                    },
                    isError = (caregiverDetails.state.error != null),
                    enabled = !caregiverDetails.isCopyPatientAddress,
                    dialogTitle = stringResource(R.string.select_state_heading)
                )
            } else {
                MdCustomOutlineTextField(
                    modifier = fieldModifier.weight(0.5f),
                    labelText = stringResource(R.string.stateStr),
                    value = caregiverDetails.state.value?.label ?: "",
                    onValueChange = {
                        if (it.length <= 50) {
                            onAction(CaregiverField.STATE, State(label = it, value = it))
                        }
                    },
                    enabled = !caregiverDetails.isCopyPatientAddress
                )
            }
        }



        Row {
            DropDownWithSelectionDialog(
                textStyle = MdTheme.typography.medium14TextStyle.copy(color = Color.Unspecified),//hack used to apply grey color similar to other field when disabled
                modifier = fieldModifier.weight(0.5f),
                options = state.countryList,
                selectedOptions = listOf(caregiverDetails.country.value),
                onOptionsSelected = {
                    if (it.firstOrNull() != null && caregiverDetails.country.value?.value != it.firstOrNull()?.value) {
                        onAction(CaregiverField.COUNTRY, it.firstOrNull())
                        onStateUpdateRequest(it.firstOrNull())
                    }
                },
                labelText = stringResource(R.string.country),
                optionToString = {
                    it?.label ?: ""
                },
                isError = (caregiverDetails.country.error != null),
                enabled = !caregiverDetails.isCopyPatientAddress,
                dialogTitle = stringResource(R.string.select_country_heading)
            )


            MdHorizontalSpace(2f)

            MdCustomOutlineTextField(
                modifier = fieldModifier.weight(0.5f),
                labelText =
                    stringResource(R.string.postal_code),
                value = caregiverDetails.postalCode.value ?: "",
                onValueChange = {
                    onAction(CaregiverField.POSTAL_CODE, it)
                },
                enabled = !caregiverDetails.isCopyPatientAddress,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }



        Row {
            MdCustomOutlineTextField(
                modifier = fieldModifier.weight(0.5f),
                labelText =
                    stringResource(R.string.mobile_phone),
                value = caregiverDetails.mobilePhone.value ?: "",
                onValueChange = {
                    onAction(CaregiverField.MOBILE_PHONE, it)
                },
                enabled = !caregiverDetails.isCopyPatientAddress,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            MdHorizontalSpace(2f)

            MdCustomOutlineTextField(
                modifier = fieldModifier.weight(0.5f),
                labelText = stringResource(R.string.home_phone),
                value = caregiverDetails.homePhone.value ?: "",
                onValueChange = {
                    onAction(CaregiverField.HOME_PHONE, it)
                },
                enabled = !caregiverDetails.isCopyPatientAddress,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        MdVerticalSpace(2f)

    }
}

@Composable
private fun ColumnScope.modifierte(): Modifier = Modifier
    .weight(.65f)
    .padding(horizontal = 16.dp)

@Composable
private fun ColumnScope.PrimaryInsuranceLazyColumn(
    state: EditProfileState, onAction: (EditProfileAction) -> Unit
) {
    val insuranceTypeFocusRequester = remember { FocusRequester() }
    val planOrProgramNameFocusRequester = remember { FocusRequester() }
    val planIdFocusRequester = remember { FocusRequester() }
    val selectedPayerFocusRequester = remember { FocusRequester() }
    val relationshipFocusRequester = remember { FocusRequester() }
    val insuredFirstNameFocusRequester = remember { FocusRequester() }
    val insuredLastNameFocusRequester = remember { FocusRequester() }
    val insuredDOBFocusRequester = remember { FocusRequester() }
    val validToFocusRequester = remember { FocusRequester() }
    val validFromFocusRequester = remember { FocusRequester() }

    val focusRequesters = mapOf(
        InsuranceField.INSURANCE_TYPE to insuranceTypeFocusRequester,
        InsuranceField.PLAN_OR_PROGRAM_NAME to planOrProgramNameFocusRequester,
        InsuranceField.PLAN_ID to planIdFocusRequester,
        InsuranceField.PAYER_NAME to selectedPayerFocusRequester,
        InsuranceField.RELATIONSHIP to relationshipFocusRequester,
        InsuranceField.FIRST_NAME to insuredFirstNameFocusRequester,
        InsuranceField.LAST_NAME to insuredLastNameFocusRequester,
        InsuranceField.DATE_OF_BIRTH to insuredDOBFocusRequester,
        InsuranceField.VALID_FROM to validFromFocusRequester,
        InsuranceField.VALID_UNTIL to validToFocusRequester,
    )

    val listState = rememberLazyListState()

    val fieldIndexMap = mapOf(
        InsuranceField.INSURANCE_TYPE to 1,
        InsuranceField.PLAN_OR_PROGRAM_NAME to 2,
        InsuranceField.PLAN_ID to 3,
        InsuranceField.PAYER_NAME to 5,
        InsuranceField.RELATIONSHIP to 28,
        InsuranceField.FIRST_NAME to 29,
        InsuranceField.LAST_NAME to 29,
        InsuranceField.DATE_OF_BIRTH to 30,
        InsuranceField.VALID_FROM to 16,
        InsuranceField.VALID_UNTIL to 16,
    )

    val deleteInsuranceCardFront = remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(state.focusedInsuranceField) {
        state.focusedInsuranceField?.let { field ->
            fieldIndexMap[field]?.let { index ->
                listState.animateScrollToItem(index)
            }
            focusRequesters[field]?.requestFocus()
            onAction(EditProfileAction.ClearFocusedInsuranceField)
        }
    }

    LazyColumn(
        modifier = lazyColumnModifier(),
        state = listState
    ) {
        item {
            HeadingText(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(id = R.string.basic_information)
            )
        }

        item {
            MdDropDownMenu(
                textStyle = MdTheme.typography.medium14TextStyle,
                modifier = fieldModifier.focusRequester(insuranceTypeFocusRequester),
                options = ModelUtil.insuranceTypeList, //TODO check with server team for API
                selectedOption = state.primaryInsurance.insuranceType.value,
                onOptionSelected = {
                    onAction(EditProfileAction.OnUpdatePrimaryInsurance(insuranceType = it))
                    if (it == ModelUtil.insuranceTypeList[0] || it == ModelUtil.insuranceTypeList[1]) {
                        onAction(EditProfileAction.OnUpdatePrimaryInsurance(planOrProgram = it))
                    }
                },
                labelText = stringResource(id = R.string.insurance_type),
                isMandatory = state.primaryInsurance.insuranceType.isMandatory,
                optionToString = {
                    it
                },
                isError = (state.primaryInsurance.insuranceType.error != null),
                supportingText = if (state.primaryInsurance.insuranceType.error != null) {
                    {
                        Text(
                            state.primaryInsurance.insuranceType.error.asString(),
                            style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                        )
                    }
                } else null,
                itemTextStyle = MdTheme.typography.medium14TextStyle,
            )
        }
        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier.focusRequester(planOrProgramNameFocusRequester),
                labelText = stringResource(id = R.string.plan_or_program_name),
                isMandatory = state.primaryInsurance.planOrProgramName.isMandatory,
                value = state.primaryInsurance.planOrProgramName.value ?: "",
                onValueChange = {
                    onAction(EditProfileAction.OnUpdatePrimaryInsurance(planOrProgram = it))
                },
                isError = state.primaryInsurance.planOrProgramName.error != null,
                supportingText = if (state.primaryInsurance.planOrProgramName.error != null) {
                    {
                        Text(
                            state.primaryInsurance.planOrProgramName.error.asString(),
                            style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                        )
                    }
                } else null
            )
        }
        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier.focusRequester(planIdFocusRequester),
                labelText = stringResource(id = R.string.insurance_id),

                isMandatory = state.primaryInsurance.planId.isMandatory,
                value = state.primaryInsurance.planId.value ?: "",
                onValueChange = {
                    onAction(EditProfileAction.OnUpdatePrimaryInsurance(planId = it))
                },
                isError = state.primaryInsurance.planId.error != null,
                supportingText = if (state.primaryInsurance.planId.error != null) {
                    {
                        Text(
                            state.primaryInsurance.planId.error.asString(),
                            style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                        )
                    }
                } else null)
        }
        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier,
                labelText = stringResource(R.string.policy_group_or_feca),
                value = state.primaryInsurance.policyGroupOrFECA.value ?: "",
                onValueChange = {
                    onAction(EditProfileAction.OnUpdatePrimaryInsurance(policyGroup = it))
                })
        }
        item {
            SearchableDropDownDialog(
                textStyle = MdTheme.typography.medium14TextStyle,
                modifier = fieldModifier.focusRequester(selectedPayerFocusRequester),
                selectedOptions = listOf(state.primaryInsurance.selectedPayer.value),
                onItemSelected = {
                    onAction(EditProfileAction.OnUpdatePrimaryInsurance(selectedPayer = it))
                },
                label = stringResource(id = R.string.payer_name),
                isMandatory = state.primaryInsurance.selectedPayer.isMandatory,
                optionToString = { it?.payerName ?: "" },
                isError = (state.primaryInsurance.selectedPayer.error != null),
                dialogTitle = stringResource(id = R.string.select_payer),
                onSearch = { query -> onAction(EditProfileAction.OnSearchPayer(query, true)) },
                searchResults = state.primaryInsurance.payerNameOptions,
                isLoading = false,
                supportingText = if (state.primaryInsurance.selectedPayer.error != null) {
                    {
                        Text(
                            state.primaryInsurance.selectedPayer.error.asString(),
                            style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                        )
                    }
                } else null
            )
        }


        item {
            HeadingText(
                modifier = Modifier.padding(top = 24.dp),
                text = stringResource(id = R.string.additional_information)
            )
        }


        item {
            Row {
                MdCustomOutlineTextField(
                    modifier = fieldModifier.weight(0.5f),
                    labelText = stringResource(R.string.copay),
                    value = state.primaryInsurance.copay.value ?: "",
                    onValueChange = {
                        // Only allow valid float numbers (empty, or matches float regex)
                        val floatRegex = Regex("^\\d*(\\.\\d*)?")
                        if (it.isEmpty() || floatRegex.matches(it)) {
                            onAction(EditProfileAction.OnUpdatePrimaryInsurance(copay = it))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                MdHorizontalSpace(2f)

                MdCustomOutlineTextField(
                    modifier = fieldModifier.weight(0.5f),
                    labelText = stringResource(R.string.deductible),
                    value = state.primaryInsurance.deductible.value ?: "",
                    onValueChange = {
                        // Only allow valid float numbers (empty, or matches float regex)
                        val floatRegex = Regex("^\\d*(\\.\\d*)?")
                        if (it.isEmpty() || floatRegex.matches(it)) {
                            onAction(EditProfileAction.OnUpdatePrimaryInsurance(deductible = it))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier,
                labelText = stringResource(R.string.coinsurance),
                value = state.primaryInsurance.coinsurance.value ?: "",
                onValueChange = {
                    onAction(EditProfileAction.OnUpdatePrimaryInsurance(coinsurance = it))
                })
        }

        item {
            Row {

                MdDateInputTextField(
                    value = state.primaryInsurance.validFrom.value ?: "",
                    textStyle = MdTheme.typography.regular14TextStyle,
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdatePrimaryInsurance(validFrom = it))
                    },
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(validFromFocusRequester),
                    isError = state.primaryInsurance.validFrom.error != null,
                    labelText = stringResource(R.string.valid_from),
                    placeholder = {
                        Text(
                            text = ModelUtil.displayDateFormateMonthFirst,
                            style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Black40)
                        )
                    }, supportingText = if (state.primaryInsurance.validFrom.error != null) {
                        val error = state.primaryInsurance.validFrom.error
                        {
                            Text(
                                error.asString(),
                                style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                            )
                        }
                    } else null)


                MdHorizontalSpace(2f)

                MdDateInputTextField(
                    value = state.primaryInsurance.validUntil.value ?: "",
                    textStyle = MdTheme.typography.regular14TextStyle,
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdatePrimaryInsurance(validUntil = it))
                    },
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(validToFocusRequester),
                    isError = state.primaryInsurance.validUntil.error != null,
                    labelText = stringResource(R.string.valid_until),
                    placeholder = {
                        Text(
                            text = ModelUtil.displayDateFormateMonthFirst,
                            style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Black40)
                        )
                    }, supportingText = if (state.primaryInsurance.validUntil.error != null) {
                        val error = state.primaryInsurance.validUntil.error
                        {
                            Text(
                                error.asString(),
                                style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                            )
                        }
                    } else null)
            }
        }

        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier,
                labelText = stringResource(R.string.employer_or_school_name),
                value = state.primaryInsurance.employerOrSchoolName.value ?: "",
                onValueChange = {
                    onAction(EditProfileAction.OnUpdatePrimaryInsurance(employerOrSchoolName = it))
                })
        }

        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier,
                labelText = stringResource(R.string.comment_str),
                value = state.primaryInsurance.comment.value ?: "",
                onValueChange = {
                    onAction(EditProfileAction.OnUpdatePrimaryInsurance(comment = it))
                }, maxLines = Int.MAX_VALUE
            )
        }

        item {
            HeadingText(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(id = R.string.insurance_card)
            )
        }
        item {
            InsuranceCardView(
                insuranceCardFront = {
                    var insuranceCardFrontBitmap by remember { mutableStateOf<Bitmap?>(null) }

                    LaunchedEffect(
                        state.primaryInsurance.insuranceCardFront.filePath,
                        state.primaryInsurance.insuranceCardFront.cacheFilePath
                    ) {
                        withContext(Dispatchers.IO) {
                            try {
                                val cacheFilePathNotBlank =
                                    !state.primaryInsurance.insuranceCardFront.cacheFilePath.isNullOrBlank()
                                val insuranceCardFrontFilePathNotBlank =
                                    !state.primaryInsurance.insuranceCardFront.filePath.isNullOrBlank()
                                val bitmap = when {
                                    cacheFilePathNotBlank -> {
                                        state.primaryInsurance.insuranceCardFront.cacheFilePath
                                            .getFileFromFilePath()
                                            ?.let { FileUtil.fileToBitmap(it) }
                                    }

                                    insuranceCardFrontFilePathNotBlank -> {
                                        state.primaryInsurance.insuranceCardFront.filePath
                                            .getFileFromFilePath()
                                            ?.let { FileUtil.fileToBitmap(it) }
                                    }

                                    else -> null
                                }
                                withContext(Dispatchers.Main) {
                                    insuranceCardFrontBitmap = bitmap
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    insuranceCardFrontBitmap = null
                                }
                            }
                        }
                    }

                    DisposableEffect(Unit) {
                        onDispose {
                            insuranceCardFrontBitmap?.recycle()
                            insuranceCardFrontBitmap = null
                        }
                    }
                    InsuranceCardLayout(
                        heading = stringResource(R.string.front_side),
                        imageBitmap = insuranceCardFrontBitmap,
                        onCameraClick = {
                            onAction(EditProfileAction.OnClickInsuranceCardFront)
                        },
                        onDeleteClick = {
                            deleteInsuranceCardFront.value = true
                        },
                        onRetryClick = {
                            onAction(EditProfileAction.OnRetryInsuranceCardFront)
                        },
                        isServerUploaded = state.primaryInsurance.insuranceCardFront.isServerUploaded,
                        isLoading = state.primaryInsurance.insuranceCardFront.isUploading,
                        onDeleteCache = {
                            onAction(EditProfileAction.OnDeleteInsuranceCardCacheFront)
                        },
                        isInsuranceAdded = state.primaryInsurance.insuranceID.value?.isNotBlank()
                    )
                },
                insuranceCardBack = {
                    var insuranceCardBackBitmap by remember { mutableStateOf<Bitmap?>(null) }

                    LaunchedEffect(
                        state.primaryInsurance.insuranceCardBack.filePath,
                        state.primaryInsurance.insuranceCardBack.cacheFilePath
                    ) {
                        withContext(Dispatchers.IO) {
                            try {
                                val cacheFilePathNotBlank =
                                    !state.primaryInsurance.insuranceCardBack.cacheFilePath.isNullOrBlank()
                                val insuranceCardBackFilePathNotBlank =
                                    !state.primaryInsurance.insuranceCardBack.filePath.isNullOrBlank()
                                val bitmap = when {
                                    cacheFilePathNotBlank -> {
                                        state.primaryInsurance.insuranceCardBack.cacheFilePath
                                            .getFileFromFilePath()
                                            ?.let { FileUtil.fileToBitmap(it) }
                                    }

                                    insuranceCardBackFilePathNotBlank -> {
                                        state.primaryInsurance.insuranceCardBack.filePath
                                            .getFileFromFilePath()
                                            ?.let { FileUtil.fileToBitmap(it) }
                                    }

                                    else -> null
                                }
                                withContext(Dispatchers.Main) {
                                    insuranceCardBackBitmap = bitmap
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    insuranceCardBackBitmap = null
                                }
                            }
                        }
                    }

                    DisposableEffect(Unit) {
                        onDispose {
                            insuranceCardBackBitmap?.recycle()
                            insuranceCardBackBitmap = null
                        }
                    }
                    InsuranceCardLayout(
                        heading = stringResource(R.string.back_side),
                        imageBitmap = insuranceCardBackBitmap,
                        onCameraClick = {
                            onAction(EditProfileAction.OnClickInsuranceCardBack)
                        },
                        onDeleteClick = {
                            deleteInsuranceCardFront.value = false
                        },
                        onRetryClick = {
                            onAction(EditProfileAction.OnRetryInsuranceCardBack)
                        },
                        isServerUploaded = state.primaryInsurance.insuranceCardBack.isServerUploaded,
                        isLoading = state.primaryInsurance.insuranceCardBack.isUploading,
                        onDeleteCache = {
                            onAction(EditProfileAction.OnDeleteInsuranceCardCacheBack)
                        },
                        isInsuranceAdded = state.primaryInsurance.insuranceID.value?.isNotBlank()
                    )
                }
            )
        }

        item {
            HeadingText(
                modifier = Modifier.padding(top = 24.dp),
                text = stringResource(id = R.string.insured_person_details)
            )
        }

        item {
            MdDropDownMenu(
                textStyle = MdTheme.typography.medium14TextStyle,
                modifier = fieldModifier.focusRequester(relationshipFocusRequester),
                options = ModelUtil.relationshipList,
                selectedOption = state.primaryInsurance.insuredPersonDetail?.relationship?.value?.value
                    ?: "",
                onOptionSelected = { selected ->
                    onAction(EditProfileAction.OnUpdatePrimaryInsurance(relationship = selected))
                },
                labelText = stringResource(id = R.string.relationship),
                isMandatory = state.primaryInsurance.insuredPersonDetail?.relationship?.isMandatory == true,
                optionToString = { it },
                itemTextStyle = MdTheme.typography.medium14TextStyle,
                isError = state.primaryInsurance.insuredPersonDetail?.relationship?.error != null,
                supportingText = if (state.primaryInsurance.insuredPersonDetail?.relationship?.error != null) {
                    {
                        Text(
                            state.primaryInsurance.insuredPersonDetail.relationship.error.asString(),
                            style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                        )
                    }
                } else null
            )
        }
        item {
            Row {
                MdCustomOutlineTextField(
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(insuredFirstNameFocusRequester),
                    labelText = stringResource(id = R.string.first_name),
                    isMandatory = state.primaryInsurance.insuredPersonDetail?.firstName?.isMandatory == true,
                    value = state.primaryInsurance.insuredPersonDetail?.firstName?.value ?: "",
                    onValueChange = { newValue ->
                        onAction(EditProfileAction.OnUpdatePrimaryInsurance(firstName = newValue))
                    },
                    isError = state.primaryInsurance.insuredPersonDetail?.firstName?.error != null,
                    supportingText = if (state.primaryInsurance.insuredPersonDetail?.firstName?.error != null) {
                        {
                            Text(
                                state.primaryInsurance.insuredPersonDetail.firstName.error.asString(),
                                style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                            )
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    )
                )

                MdHorizontalSpace(2f)

                MdCustomOutlineTextField(
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(insuredLastNameFocusRequester),
                    labelText = stringResource(id = R.string.last_name),
                    isMandatory = state.primaryInsurance.insuredPersonDetail?.lastName?.isMandatory == true,
                    value = state.primaryInsurance.insuredPersonDetail?.lastName?.value ?: "",
                    onValueChange = { newValue ->
                        onAction(EditProfileAction.OnUpdatePrimaryInsurance(lastName = newValue))
                    },
                    isError = state.primaryInsurance.insuredPersonDetail?.lastName?.error != null,
                    supportingText = if (state.primaryInsurance.insuredPersonDetail?.lastName?.error != null) {
                        {
                            Text(
                                state.primaryInsurance.insuredPersonDetail.lastName.error.asString(),
                                style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                            )
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    )
                )
            }
        }
        item {
            Row {
                MdDateInputTextField(
                    value = state.primaryInsurance.insuredPersonDetail?.dateOfBirth?.value
                        ?: "",
                    textStyle = MdTheme.typography.regular14TextStyle,
                    onValueChange = { newValue ->
                        onAction(EditProfileAction.OnUpdatePrimaryInsurance(dateOfBirth = newValue))
                    },
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(insuredDOBFocusRequester),
                    labelText = stringResource(id = R.string.date_of_birth),
                    isMandatory = state.primaryInsurance.insuredPersonDetail?.dateOfBirth?.isMandatory == true,
                    placeholder = {
                        Text(
                            text = ModelUtil.displayDateFormateMonthFirst,
                            style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Black40)
                        )
                    },
                    isError = state.primaryInsurance.insuredPersonDetail?.dateOfBirth?.error != null,
                    supportingText = if (state.primaryInsurance.insuredPersonDetail?.dateOfBirth?.error != null) {
                        val error = state.primaryInsurance.insuredPersonDetail.dateOfBirth.error
                        {
                            Text(
                                error.asString(),
                                style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                            )
                        }
                    } else null, minDate = LocalDate.now().minusYears(100),
                    maxDate = LocalDate.now(),
                    isDobField = true)
                MdHorizontalSpace(2f)
                MdDropDownMenu(
                    textStyle = MdTheme.typography.medium14TextStyle,
                    modifier = fieldModifier.weight(0.5f),
                    options = state.demographicsMetrics?.birthSex ?: emptyList(),
                    selectedOption = state.primaryInsurance.insuredPersonDetail?.birthSex?.value
                        ?: "",
                    onOptionSelected = { selected ->
                        onAction(EditProfileAction.OnUpdatePrimaryInsurance(birthSex = selected))
                    },
                    labelText = stringResource(R.string.birth_sex),
                    optionToString = { it },
                    isError = (state.primaryInsurance.insuredPersonDetail?.birthSex?.error != null),
                    itemTextStyle = MdTheme.typography.medium14TextStyle,
                )
            }
        }
        item {
            MdVerticalSpace(1f)
            Row(
                modifier = Modifier
                    .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SideMenuDetailHeading(
                    modifier = Modifier.padding(top = 16.dp),
                    text = stringResource(id = R.string.contact_details)
                )
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .clickable {
                            onAction(
                                EditProfileAction.OnUpdatePrimaryInsurance(
                                    isCopyPatientAddress = !(state.primaryInsurance.insuredPersonDetail?.isCopyPatientAddress
                                        ?: false)
                                )
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    Checkbox(
                        checked = state.primaryInsurance.insuredPersonDetail?.isCopyPatientAddress == true,
                        onCheckedChange = {
                            onAction(
                                EditProfileAction.OnUpdatePrimaryInsurance(
                                    isCopyPatientAddress = it
                                )
                            )
                        },
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    Text(
                        text = stringResource(id = R.string.copy_patient_address),
                        style = MdTheme.typography.medium14TextStyle,
                        modifier = Modifier.padding(start = 0.dp)
                    )
                }
            }
        }

        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier,
                labelText = stringResource(R.string.address_line_1),
                value = state.primaryInsurance.insuredPersonDetail?.addressLine1?.value ?: "",
                onValueChange = {
                    onAction(EditProfileAction.OnUpdatePrimaryInsurance(addressLine1 = it))
                }, maxLines = Int.MAX_VALUE
            )
        }

        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier,
                labelText = stringResource(R.string.address_line_2),
                value = state.primaryInsurance.insuredPersonDetail?.addressLine2?.value ?: "",
                onValueChange = {
                    onAction(EditProfileAction.OnUpdatePrimaryInsurance(addressLine2 = it))
                }, maxLines = Int.MAX_VALUE
            )
        }

        item {
            Row {
                MdCustomOutlineTextField(
                    modifier = fieldModifier.weight(0.5f),
                    labelText = stringResource(R.string.cityStr),
                    value = state.primaryInsurance.insuredPersonDetail?.city?.value ?: "",
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdatePrimaryInsurance(city = it))
                    })

                MdHorizontalSpace(2f)

                if (state.primaryInsurance.insuredPersonDetail?.country?.value?.value in ModelUtil.supportedStateCountries) {
                    DropDownWithSelectionDialog(
                        textStyle = MdTheme.typography.medium14TextStyle,
                        modifier = fieldModifier.weight(0.5f),
                        options = state.countryStateMap[state.primaryInsurance.insuredPersonDetail?.country?.value?.value]
                            ?: emptyList(),
                        selectedOptions = listOf(state.primaryInsurance.insuredPersonDetail?.state?.value),
                        onOptionsSelected = {
                            onAction(EditProfileAction.OnUpdatePrimaryInsurance(state = it.firstOrNull()))
                        },
                        labelText = stringResource(R.string.stateStr),
                        optionToString = {
                            it?.value ?: ""
                        },
                        isError = (state.primaryInsurance.insuredPersonDetail?.state?.error != null),
                        dialogTitle = stringResource(R.string.select_state_heading)
                    )
                } else {
                    MdCustomOutlineTextField(
                        modifier = fieldModifier.weight(0.5f),
                        labelText = stringResource(R.string.stateStr),
                        value = state.primaryInsurance.insuredPersonDetail?.state?.value?.label
                            ?: "",
                        onValueChange = {
                            if (it.length <= 50) {
                                onAction(
                                    EditProfileAction.OnUpdatePrimaryInsurance(
                                        state = State(label = it, value = it)
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }

        item {
            Row {
                DropDownWithSelectionDialog(
                    textStyle = MdTheme.typography.medium14TextStyle,
                    modifier = fieldModifier.weight(0.5f),
                    options = state.countryList,
                    selectedOptions = listOf(state.primaryInsurance.insuredPersonDetail?.country?.value),
                    onOptionsSelected = {
                        if (it.firstOrNull() != null && state.primaryInsurance.insuredPersonDetail?.country?.value != it.firstOrNull()) {
                            onAction(EditProfileAction.OnUpdatePrimaryInsurance(country = it.firstOrNull()))
                            onAction(EditProfileAction.OnRequestStatesPrimaryInsurance(country = it.firstOrNull()))
                        }
                    },
                    labelText = stringResource(R.string.country),
                    optionToString = {
                        it?.label ?: ""
                    },
                    isError = (state.primaryInsurance.insuredPersonDetail?.country?.error != null),
                    dialogTitle = stringResource(R.string.select_country_heading)
                )

                MdHorizontalSpace(2f)

                MdCustomOutlineTextField(
                    modifier = fieldModifier.weight(0.5f),
                    labelText = stringResource(R.string.postal_code),
                    value = state.primaryInsurance.insuredPersonDetail?.postalCode?.value ?: "",
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdatePrimaryInsurance(postalCode = it))
                    }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        item {
            Row {
                MdCustomOutlineTextField(
                    modifier = fieldModifier.weight(0.5f),
                    labelText = stringResource(R.string.mobile_phone),
                    value = state.primaryInsurance.insuredPersonDetail?.mobilePhone?.value
                        ?: "",
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdatePrimaryInsurance(mobilePhone = it))
                    }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                MdHorizontalSpace(2f)

                MdCustomOutlineTextField(
                    modifier = fieldModifier.weight(0.5f),
                    labelText = stringResource(R.string.home_phone),
                    value = state.primaryInsurance.insuredPersonDetail?.homePhone?.value ?: "",
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdatePrimaryInsurance(homePhone = it))
                    }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        item {
            MdVerticalSpace(4f)
        }

    }

    if (deleteInsuranceCardFront.value != null) {
        MdAlertDialog(
            message = if (deleteInsuranceCardFront.value == true) stringResource(
                id = R.string.insurance_card_front_page_delete_msg
            ) else stringResource(
                id = R.string.insurance_card_back_page_delete_msg
            ),
            primaryButtonResId = R.string.alertDialogYesTextNoI18N,
            secondaryButtonResId = R.string.alertDialogNoTextNoI18N,
            onPrimaryAction = {
                if (deleteInsuranceCardFront.value == true) {
                    onAction(EditProfileAction.DeleteInsuranceCardFront)
                } else {
                    onAction(EditProfileAction.DeleteInsuranceCardBack)
                }
                deleteInsuranceCardFront.value = null
            },
            onSecondaryAction = {
                deleteInsuranceCardFront.value = null
            }
        )
    }
}

@Composable
private fun ColumnScope.SecondaryInsuranceLazyColumn(
    state: EditProfileState, onAction: (EditProfileAction) -> Unit
) {
    val insuranceTypeFocusRequester = remember { FocusRequester() }
    val planOrProgramNameFocusRequester = remember { FocusRequester() }
    val planIdFocusRequester = remember { FocusRequester() }
    val selectedPayerFocusRequester = remember { FocusRequester() }
    val relationshipFocusRequester = remember { FocusRequester() }
    val insuredFirstNameFocusRequester = remember { FocusRequester() }
    val insuredLastNameFocusRequester = remember { FocusRequester() }
    val insuredDOBFocusRequester = remember { FocusRequester() }
    val validFromFocusRequester = remember { FocusRequester() }
    val validToFocusRequester = remember { FocusRequester() }

    val focusRequesters = mapOf(
        InsuranceField.INSURANCE_TYPE to insuranceTypeFocusRequester,
        InsuranceField.PLAN_OR_PROGRAM_NAME to planOrProgramNameFocusRequester,
        InsuranceField.PLAN_ID to planIdFocusRequester,
        InsuranceField.PAYER_NAME to selectedPayerFocusRequester,
        InsuranceField.RELATIONSHIP to relationshipFocusRequester,
        InsuranceField.FIRST_NAME to insuredFirstNameFocusRequester,
        InsuranceField.LAST_NAME to insuredLastNameFocusRequester,
        InsuranceField.DATE_OF_BIRTH to insuredDOBFocusRequester,
        InsuranceField.VALID_FROM to validFromFocusRequester,
        InsuranceField.VALID_UNTIL to validToFocusRequester,
    )

    val listState = rememberLazyListState()

    val fieldIndexMap = mapOf(
        InsuranceField.INSURANCE_TYPE to 1,
        InsuranceField.PLAN_OR_PROGRAM_NAME to 2,
        InsuranceField.PLAN_ID to 3,
        InsuranceField.PAYER_NAME to 5,
        InsuranceField.RELATIONSHIP to 28,
        InsuranceField.FIRST_NAME to 29,
        InsuranceField.LAST_NAME to 29,
        InsuranceField.DATE_OF_BIRTH to 30,
        InsuranceField.VALID_FROM to 16,
        InsuranceField.VALID_UNTIL to 16,
    )

    val deleteInsuranceCardFront = remember { mutableStateOf<Boolean?>(null) }


    LaunchedEffect(state.focusedInsuranceField) {
        state.focusedInsuranceField?.let { field ->
            fieldIndexMap[field]?.let { index ->
                listState.animateScrollToItem(index)
            }
            focusRequesters[field]?.requestFocus()
        }
    }
    LazyColumn(
        modifier = lazyColumnModifier(),
        state = listState
    ) {
        item {
            HeadingText(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(id = R.string.basic_information)
            )
        }

        item {
            MdDropDownMenu(
                textStyle = MdTheme.typography.medium14TextStyle,
                modifier = fieldModifier.focusRequester(insuranceTypeFocusRequester),
                options = ModelUtil.insuranceTypeList,
                selectedOption = state.secondaryInsurance.insuranceType.value,
                onOptionSelected = {
                    onAction(EditProfileAction.OnUpdateSecondaryInsurance(insuranceType = it))
                },
                labelText = stringResource(id = R.string.insurance_type),
                isMandatory = state.secondaryInsurance.insuranceType.isMandatory,
                optionToString = {
                    it
                },
                itemTextStyle = MdTheme.typography.medium14TextStyle,
                isError = state.secondaryInsurance.insuranceType.error != null,
                supportingText = if (state.secondaryInsurance.insuranceType.error != null) {
                    {
                        Text(
                            state.secondaryInsurance.insuranceType.error.asString(),
                            style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                        )
                    }
                } else null
            )
        }
        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier.focusRequester(planOrProgramNameFocusRequester),
                labelText = stringResource(id = R.string.plan_or_program_name),
                isMandatory = state.secondaryInsurance.planOrProgramName.isMandatory,
                value = state.secondaryInsurance.planOrProgramName.value ?: "",
                onValueChange = {
                    onAction(EditProfileAction.OnUpdateSecondaryInsurance(planOrProgram = it))
                },
                isError = state.secondaryInsurance.planOrProgramName.error != null,
                supportingText = if (state.secondaryInsurance.planOrProgramName.error != null) {
                    {
                        Text(
                            state.secondaryInsurance.planOrProgramName.error.asString(),
                            style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                        )
                    }
                } else null
            )
        }
        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier.focusRequester(planIdFocusRequester), labelText =
                    stringResource(id = R.string.insurance_id),
                isMandatory = state.secondaryInsurance.planId.isMandatory,
                value = state.secondaryInsurance.planId.value ?: "", onValueChange = {
                    onAction(EditProfileAction.OnUpdateSecondaryInsurance(planId = it))
                }, isError = state.secondaryInsurance.planId.error != null,
                supportingText = if (state.secondaryInsurance.planId.error != null) {
                    {
                        Text(
                            state.secondaryInsurance.planId.error.asString(),
                            style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                        )
                    }
                } else null)

        }

        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier,
                labelText = stringResource(R.string.policy_group_or_feca),
                value = state.secondaryInsurance.policyGroupOrFECA.value ?: "",
                onValueChange = {
                    onAction(EditProfileAction.OnUpdateSecondaryInsurance(policyGroup = it))
                })
        }

        item {
            SearchableDropDownDialog(
                textStyle = MdTheme.typography.medium14TextStyle,
                modifier = fieldModifier.focusRequester(selectedPayerFocusRequester),
                selectedOptions = listOf(state.secondaryInsurance.selectedPayer.value),
                onItemSelected = {
                    onAction(EditProfileAction.OnUpdateSecondaryInsurance(selectedPayer = it as PracticePayer))
                },
                label = stringResource(id = R.string.payer_name),
                isMandatory = state.secondaryInsurance.selectedPayer.isMandatory,
                optionToString = { it?.payerName ?: "" },
                isError = (state.secondaryInsurance.selectedPayer.error != null),
                dialogTitle = stringResource(id = R.string.select_payer),
                onSearch = { query -> onAction(EditProfileAction.OnSearchPayer(query, false)) },
                searchResults = state.secondaryInsurance.payerNameOptions,
                isLoading = false,
                supportingText = if (state.secondaryInsurance.selectedPayer.error != null) {
                    {
                        Text(
                            state.secondaryInsurance.selectedPayer.error.asString(),
                            style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                        )
                    }
                } else null
            )
        }


        item {
            HeadingText(
                modifier = Modifier.padding(top = 24.dp),
                text = stringResource(id = R.string.additional_information)
            )
        }


        item {
            Row {
                MdCustomOutlineTextField(
                    modifier = fieldModifier.weight(0.5f),
                    labelText =
                        stringResource(R.string.copay),
                    value = state.secondaryInsurance.copay.value ?: "",
                    onValueChange = {
                        // Only allow up to 10 characters
                        if (it.length <= 10) {
                            // Only allow valid float numbers (empty, or matches float regex)
                            val floatRegex = Regex("^\\d*(\\.\\d*)?")
                            if (it.isEmpty() || floatRegex.matches(it)) {
                                onAction(EditProfileAction.OnUpdateSecondaryInsurance(copay = it))
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                MdHorizontalSpace(2f)

                MdCustomOutlineTextField(
                    modifier = fieldModifier.weight(0.5f),
                    labelText = stringResource(R.string.deductible),
                    value = state.secondaryInsurance.deductible.value ?: "",
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdateSecondaryInsurance(deductible = it))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

            }
        }

        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier,
                labelText = stringResource(R.string.coinsurance),
                value = state.secondaryInsurance.coinsurance.value ?: "",
                onValueChange = {
                    onAction(EditProfileAction.OnUpdateSecondaryInsurance(coinsurance = it))
                })
        }

        item {

            Row {

                MdDateInputTextField(
                    value = state.secondaryInsurance.validFrom.value ?: "",
                    textStyle = MdTheme.typography.regular14TextStyle,
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdateSecondaryInsurance(validFrom = it))
                    },
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(validFromFocusRequester),
                    isError = state.secondaryInsurance.validFrom.error != null,
                    labelText = stringResource(R.string.valid_from),
                    placeholder = {
                        Text(
                            text = ModelUtil.displayDateFormateMonthFirst,
                            style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Black40)
                        )
                    }, supportingText = if (state.secondaryInsurance.validFrom.error != null) {
                        val error = state.secondaryInsurance.validFrom.error
                        {
                            Text(
                                error.asString(),
                                style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                            )
                        }
                    } else null)


                MdHorizontalSpace(2f)

                MdDateInputTextField(
                    value = state.secondaryInsurance.validUntil.value ?: "",
                    textStyle = MdTheme.typography.regular14TextStyle,
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdateSecondaryInsurance(validUntil = it))
                    },
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(validToFocusRequester),
                    isError = state.secondaryInsurance.validUntil.error != null,
                    labelText = stringResource(R.string.valid_until),
                    placeholder = {
                        Text(
                            text = ModelUtil.displayDateFormateMonthFirst,
                            style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Black40)
                        )
                    }, supportingText = if (state.secondaryInsurance.validUntil.error != null) {
                        val error = state.secondaryInsurance.validUntil.error
                        {
                            Text(
                                error.asString(),
                                style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                            )
                        }
                    } else null)
            }
        }

        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier,
                labelText = stringResource(R.string.employer_or_school_name),
                value = state.secondaryInsurance.employerOrSchoolName.value ?: "",
                onValueChange = {
                    onAction(EditProfileAction.OnUpdateSecondaryInsurance(employerOrSchoolName = it))
                })
        }

        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier,
                labelText = stringResource(R.string.comment_str),
                value = state.secondaryInsurance.comment.value ?: "",
                onValueChange = {
                    onAction(EditProfileAction.OnUpdateSecondaryInsurance(comment = it))
                }, maxLines = Int.MAX_VALUE
            )
        }

        item {
            HeadingText(
                modifier = Modifier.padding(top = 24.dp),
                text = stringResource(id = R.string.insurance_card)
            )
        }

        item {
            InsuranceCardView(
                insuranceCardFront = {
                    var insuranceCardFrontBitmap by remember { mutableStateOf<Bitmap?>(null) }

                    LaunchedEffect(
                        state.secondaryInsurance.insuranceCardFront.filePath,
                        state.secondaryInsurance.insuranceCardFront.cacheFilePath
                    ) {
                        withContext(Dispatchers.IO) {
                            try {
                                val bitmap = when {
                                    !state.secondaryInsurance.insuranceCardFront.cacheFilePath.isNullOrBlank() -> {
                                        state.secondaryInsurance.insuranceCardFront.cacheFilePath
                                            .getFileFromFilePath()
                                            ?.let { FileUtil.fileToBitmap(it) }
                                    }

                                    !state.secondaryInsurance.insuranceCardFront.filePath.isNullOrBlank() -> {
                                        state.secondaryInsurance.insuranceCardFront.filePath
                                            .getFileFromFilePath()
                                            ?.let { FileUtil.fileToBitmap(it) }
                                    }

                                    else -> null
                                }
                                withContext(Dispatchers.Main) {
                                    insuranceCardFrontBitmap = bitmap
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    insuranceCardFrontBitmap = null
                                }
                            }
                        }
                    }

                    DisposableEffect(Unit) {
                        onDispose {
                            insuranceCardFrontBitmap?.recycle()
                            insuranceCardFrontBitmap = null
                        }
                    }
                    InsuranceCardLayout(
                        heading = stringResource(R.string.front_side),
                        imageBitmap = insuranceCardFrontBitmap,
                        onCameraClick = {
                            onAction(EditProfileAction.OnClickInsuranceCardFront)
                        },
                        onDeleteClick = {
                            deleteInsuranceCardFront.value = true
                        },
                        onRetryClick = {
                            onAction(EditProfileAction.OnRetryInsuranceCardFront)
                        },
                        isServerUploaded = state.secondaryInsurance.insuranceCardFront.isServerUploaded,
                        isLoading = state.secondaryInsurance.insuranceCardFront.isUploading,
                        onDeleteCache = {
                            onAction(EditProfileAction.OnDeleteInsuranceCardCacheFront)
                        },
                        isInsuranceAdded = state.secondaryInsurance.insuranceID.value?.isNotBlank()
                    )
                },
                insuranceCardBack = {
                    var insuranceCardBackBitmap by remember { mutableStateOf<Bitmap?>(null) }

                    LaunchedEffect(
                        state.secondaryInsurance.insuranceCardBack.filePath,
                        state.secondaryInsurance.insuranceCardBack.cacheFilePath
                    ) {
                        withContext(Dispatchers.IO) {
                            try {
                                val cachedFilePathNotBlank =
                                    !state.secondaryInsurance.insuranceCardBack.cacheFilePath.isNullOrBlank()
                                val insuranceCardBackFilePathNotBlank =
                                    !state.secondaryInsurance.insuranceCardBack.filePath.isNullOrBlank()
                                val bitmap = when {
                                    cachedFilePathNotBlank -> {
                                        state.secondaryInsurance.insuranceCardBack.cacheFilePath
                                            .getFileFromFilePath()
                                            ?.let { FileUtil.fileToBitmap(it) }
                                    }

                                    insuranceCardBackFilePathNotBlank -> {
                                        state.secondaryInsurance.insuranceCardBack.filePath
                                            .getFileFromFilePath()
                                            ?.let { FileUtil.fileToBitmap(it) }
                                    }

                                    else -> null
                                }
                                withContext(Dispatchers.Main) {
                                    insuranceCardBackBitmap = bitmap
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    insuranceCardBackBitmap = null
                                }
                            }
                        }
                    }

                    DisposableEffect(Unit) {
                        onDispose {
                            insuranceCardBackBitmap?.recycle()
                            insuranceCardBackBitmap = null
                        }
                    }
                    InsuranceCardLayout(
                        heading = stringResource(R.string.back_side),
                        imageBitmap = insuranceCardBackBitmap,
                        onCameraClick = {
                            onAction(EditProfileAction.OnClickInsuranceCardBack)
                        },
                        onDeleteClick = {
                            deleteInsuranceCardFront.value = false
                        },
                        onRetryClick = {
                            onAction(EditProfileAction.OnRetryInsuranceCardBack)
                        },
                        isServerUploaded = state.secondaryInsurance.insuranceCardBack.isServerUploaded,
                        isLoading = state.secondaryInsurance.insuranceCardBack.isUploading,
                        onDeleteCache = {
                            onAction(EditProfileAction.OnDeleteInsuranceCardCacheBack)
                        },
                        isInsuranceAdded = state.secondaryInsurance.insuranceID.value?.isNotBlank()
                    )
                }
            )
        }

        item {
            HeadingText(
                modifier = Modifier.padding(top = 24.dp),
                text = stringResource(id = R.string.insured_person_details)
            )
        }

        item {
            MdDropDownMenu(
                textStyle = MdTheme.typography.medium14TextStyle,
                modifier = fieldModifier.focusRequester(relationshipFocusRequester),
                options = ModelUtil.relationshipList,
                selectedOption = state.secondaryInsurance.insuredPersonDetail?.relationship?.value?.value
                    ?: "",
                onOptionSelected = { selected ->
                    onAction(EditProfileAction.OnUpdateSecondaryInsurance(relationship = selected))
                },
                labelText = stringResource(id = R.string.relationship),
                isMandatory = state.secondaryInsurance.insuredPersonDetail?.relationship?.isMandatory == true,
                optionToString = { it },
                itemTextStyle = MdTheme.typography.medium14TextStyle,
                isError = state.secondaryInsurance.insuredPersonDetail?.relationship?.error != null,
                supportingText = if (state.secondaryInsurance.insuredPersonDetail?.relationship?.error != null) {
                    {
                        Text(
                            state.secondaryInsurance.insuredPersonDetail.relationship.error.asString(),
                            style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                        )
                    }
                } else null
            )
        }
        item {
            Row {
                MdCustomOutlineTextField(
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(insuredFirstNameFocusRequester),
                    labelText = stringResource(id = R.string.first_name),
                    isMandatory = state.secondaryInsurance.insuredPersonDetail?.firstName?.isMandatory == true,
                    value = state.secondaryInsurance.insuredPersonDetail?.firstName?.value
                        ?: "",
                    onValueChange = { newValue ->
                        onAction(EditProfileAction.OnUpdateSecondaryInsurance(firstName = newValue))
                    },
                    isError = state.secondaryInsurance.insuredPersonDetail?.firstName?.error != null,
                    supportingText = if (state.secondaryInsurance.insuredPersonDetail?.firstName?.error != null) {
                        {
                            Text(
                                state.secondaryInsurance.insuredPersonDetail.firstName.error.asString(),
                                style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                            )
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    )
                )

                MdHorizontalSpace(2f)

                MdCustomOutlineTextField(
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(insuredLastNameFocusRequester),
                    labelText = stringResource(id = R.string.last_name),
                    isMandatory = state.secondaryInsurance.insuredPersonDetail?.lastName?.isMandatory == true,
                    value = state.secondaryInsurance.insuredPersonDetail?.lastName?.value ?: "",
                    onValueChange = { newValue ->
                        onAction(EditProfileAction.OnUpdateSecondaryInsurance(lastName = newValue))
                    },
                    isError = state.secondaryInsurance.insuredPersonDetail?.lastName?.error != null,
                    supportingText = if (state.secondaryInsurance.insuredPersonDetail?.lastName?.error != null) {
                        {
                            Text(
                                state.secondaryInsurance.insuredPersonDetail.lastName.error.asString(),
                                style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                            )
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    )
                )
            }
        }
        item {
            Row {
                MdDateInputTextField(
                    value = state.secondaryInsurance.insuredPersonDetail?.dateOfBirth?.value
                        ?: "",
                    textStyle = MdTheme.typography.regular14TextStyle,
                    onValueChange = { newValue ->
                        onAction(EditProfileAction.OnUpdateSecondaryInsurance(dateOfBirth = newValue))
                    },
                    modifier = fieldModifier
                        .weight(0.5f)
                        .focusRequester(insuredDOBFocusRequester),
                    labelText = stringResource(id = R.string.date_of_birth),
                    isMandatory = state.secondaryInsurance.insuredPersonDetail?.dateOfBirth?.isMandatory == true,
                    placeholder = {
                        Text(
                            text = ModelUtil.displayDateFormateMonthFirst,
                            style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Black40)
                        )
                    },
                    isError = state.secondaryInsurance.insuredPersonDetail?.dateOfBirth?.error != null,
                    supportingText = if (state.secondaryInsurance.insuredPersonDetail?.dateOfBirth?.error != null) {
                        val error =
                            state.secondaryInsurance.insuredPersonDetail.dateOfBirth.error
                        {
                            Text(
                                error.asString(),
                                style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Red)
                            )
                        }
                    } else null, minDate = LocalDate.now().minusYears(100),
                    maxDate = LocalDate.now(),
                    isDobField = true)
                MdHorizontalSpace(2f)
                MdDropDownMenu(
                    textStyle = MdTheme.typography.medium14TextStyle,
                    modifier = fieldModifier.weight(0.5f),
                    options = state.demographicsMetrics?.birthSex ?: emptyList(),
                    selectedOption = state.secondaryInsurance.insuredPersonDetail?.birthSex?.value
                        ?: "",
                    onOptionSelected = { selected ->
                        onAction(EditProfileAction.OnUpdateSecondaryInsurance(birthSex = selected))
                    },
                    labelText = stringResource(R.string.birth_sex),
                    optionToString = { it },
                    isError = (state.secondaryInsurance.insuredPersonDetail?.birthSex?.error != null),
                    itemTextStyle = MdTheme.typography.medium14TextStyle,
                )
            }
        }
        item {
            MdVerticalSpace(1f)
            Row(
                modifier = Modifier
                    .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SideMenuDetailHeading(
                    modifier = Modifier.padding(top = 16.dp),
                    text = stringResource(id = R.string.contact_details)
                )
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .clickable {
                            onAction(
                                EditProfileAction.OnUpdateSecondaryInsurance(
                                    isCopyPatientAddress = !(state.secondaryInsurance.insuredPersonDetail?.isCopyPatientAddress
                                        ?: false)
                                )
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Checkbox(
                        checked = state.secondaryInsurance.insuredPersonDetail?.isCopyPatientAddress == true,
                        onCheckedChange = {
                            onAction(
                                EditProfileAction.OnUpdateSecondaryInsurance(
                                    isCopyPatientAddress = it
                                )
                            )
                        },
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    Text(
                        text = stringResource(id = R.string.copy_patient_address),
                        style = MdTheme.typography.medium14TextStyle,
                        modifier = Modifier.padding(start = 0.dp)
                    )
                }
            }
        }

        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier,
                labelText = stringResource(R.string.address_line_1),
                value = state.secondaryInsurance.insuredPersonDetail?.addressLine1?.value ?: "",
                onValueChange = {
                    onAction(EditProfileAction.OnUpdateSecondaryInsurance(addressLine1 = it))
                }, maxLines = Int.MAX_VALUE
            )
        }

        item {
            MdCustomOutlineTextField(
                modifier = fieldModifier,
                labelText = stringResource(R.string.address_line_2),
                value = state.secondaryInsurance.insuredPersonDetail?.addressLine2?.value ?: "",
                onValueChange = {
                    onAction(EditProfileAction.OnUpdateSecondaryInsurance(addressLine2 = it))
                }, maxLines = Int.MAX_VALUE
            )
        }

        item {
            Row {
                MdCustomOutlineTextField(
                    modifier = fieldModifier.weight(0.5f),
                    labelText = stringResource(R.string.cityStr),
                    value = state.secondaryInsurance.insuredPersonDetail?.city?.value ?: "",
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdateSecondaryInsurance(city = it))
                    })

                MdHorizontalSpace(2f)

                if (state.secondaryInsurance.insuredPersonDetail?.country?.value?.value in ModelUtil.supportedStateCountries) {
                    DropDownWithSelectionDialog(
                        textStyle = MdTheme.typography.medium14TextStyle,
                        modifier = fieldModifier.weight(0.5f),
                        options = state.countryStateMap[state.secondaryInsurance.insuredPersonDetail?.country?.value?.value]
                            ?: emptyList(),
                        selectedOptions = listOf(state.secondaryInsurance.insuredPersonDetail?.state?.value),
                        onOptionsSelected = {
                            onAction(EditProfileAction.OnUpdateSecondaryInsurance(state = it.firstOrNull()))
                        },
                        labelText = stringResource(R.string.stateStr),
                        optionToString = {
                            it?.value ?: ""
                        },
                        isError = (state.secondaryInsurance.insuredPersonDetail?.state?.error != null),
                        dialogTitle = stringResource(R.string.select_state_heading)
                    )
                } else {
                    MdCustomOutlineTextField(
                        modifier = fieldModifier.weight(0.5f),
                        labelText = stringResource(R.string.stateStr),
                        value = state.secondaryInsurance.insuredPersonDetail?.state?.value?.label
                            ?: "",
                        onValueChange = {
                            if (it.length <= 50) {
                                onAction(
                                    EditProfileAction.OnUpdateSecondaryInsurance(
                                        state = State(label = it, value = it)
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }

        item {
            Row {
                DropDownWithSelectionDialog(
                    textStyle = MdTheme.typography.medium14TextStyle,
                    modifier = fieldModifier.weight(0.5f),
                    options = state.countryList,
                    selectedOptions = listOf(state.secondaryInsurance.insuredPersonDetail?.country?.value),
                    onOptionsSelected = {
                        if (it.firstOrNull() != null && it.firstOrNull() != state.secondaryInsurance.insuredPersonDetail?.country?.value) {
                            onAction(EditProfileAction.OnUpdateSecondaryInsurance(country = it.firstOrNull()))
                            onAction(EditProfileAction.OnRequestStatesSecondaryInsurance(it.firstOrNull()))
                        }
                    },
                    labelText = stringResource(R.string.country),
                    optionToString = {
                        it?.label ?: ""
                    },
                    isError = (state.secondaryInsurance.insuredPersonDetail?.country?.error != null),
                    dialogTitle = stringResource(R.string.select_country_heading)
                )

                MdHorizontalSpace(2f)

                MdCustomOutlineTextField(
                    modifier = fieldModifier.weight(0.5f),
                    labelText = stringResource(R.string.postal_code),
                    value = state.secondaryInsurance.insuredPersonDetail?.postalCode?.value
                        ?: "",
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdateSecondaryInsurance(postalCode = it))
                    }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        item {
            Row {
                MdCustomOutlineTextField(
                    modifier = fieldModifier.weight(0.5f),
                    labelText = stringResource(R.string.mobile_phone),
                    value = state.secondaryInsurance.insuredPersonDetail?.mobilePhone?.value
                        ?: "",
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdateSecondaryInsurance(mobilePhone = it))
                    }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                MdHorizontalSpace(2f)

                MdCustomOutlineTextField(
                    modifier = fieldModifier.weight(0.5f),
                    labelText = stringResource(R.string.home_phone),
                    value = state.secondaryInsurance.insuredPersonDetail?.homePhone?.value
                        ?: "",
                    onValueChange = {
                        onAction(EditProfileAction.OnUpdateSecondaryInsurance(homePhone = it))
                    }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        item {
            MdVerticalSpace(4f)
        }

    }

    if (deleteInsuranceCardFront.value != null) {
        MdAlertDialog(
            message = if (deleteInsuranceCardFront.value == true) stringResource(
                id = R.string.insurance_card_front_page_delete_msg
            ) else stringResource(
                id = R.string.insurance_card_back_page_delete_msg
            ),
            primaryButtonResId = R.string.alertDialogYesTextNoI18N,
            secondaryButtonResId = R.string.alertDialogNoTextNoI18N,
            onPrimaryAction = {
                if (deleteInsuranceCardFront.value == true) {
                    onAction(EditProfileAction.DeleteInsuranceCardFront)
                } else {
                    onAction(EditProfileAction.DeleteInsuranceCardBack)
                }
                deleteInsuranceCardFront.value = null
            },
            onSecondaryAction = {
                deleteInsuranceCardFront.value = null
            }
        )
    }
}


@Composable
fun InsuranceCardView(
    insuranceCardFront: @Composable () -> Unit,
    insuranceCardBack: @Composable () -> Unit
) {
    Row(Modifier.fillMaxWidth()) {
        Box(Modifier.weight(1f)) { insuranceCardFront() }
        Box(Modifier.weight(1f)) { insuranceCardBack() }
    }
}

@Composable
fun InsuranceCardLayout(
    heading: String,
    imageBitmap: Bitmap?,
    onCameraClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    isServerUploaded: Boolean = false,
    isLoading: Boolean = false,
    onDeleteCache: () -> Unit,
    isInsuranceAdded: Boolean?
) {
    Column(modifier.padding(8.dp)) {
        Text(text = heading, style = MdTheme.typography.medium16TextStyle)
        Spacer(Modifier.height(8.dp))
        if (isLoading) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                MdCircularProgressBar()
            }
        } else if (imageBitmap != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .border(1.dp, MdColors.Black10, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Image(
                    bitmap = imageBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                if (!isServerUploaded && !isLoading && isInsuranceAdded == true) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .background(Color.White.copy(alpha = 0.7f))
                    )
                    Text(
                        text = stringResource(R.string.server_upload_failed_msg),
                        color = Color.Red,
                        style = MdTheme.typography.medium14TextStyle,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }
            }
            if (!isLoading) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!isServerUploaded && isInsuranceAdded == true) {
                        TextButton(onClick = onRetryClick) {
                            Icon(
                                painterResource(R.drawable.ic_retry),
                                contentDescription = null
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.retry))
                        }
                        TextButton(onClick = onDeleteCache) {
                            Icon(
                                painterResource(R.drawable.ic_delete),
                                contentDescription = null
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.delete))
                        }
                    } else {
                        TextButton(onClick = onCameraClick) {
                            Icon(
                                painterResource(R.drawable.ic_camera),
                                contentDescription = null
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.retake))
                        }
                        TextButton(onClick = onDeleteClick) {
                            Icon(
                                painterResource(R.drawable.ic_delete),
                                contentDescription = null
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.delete))
                        }
                    }
                }
            }
        } else {
            Image(
                painterResource(R.drawable.insurance_card_place_holder_image),
                contentDescription = null,
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .clickable {
                        onCameraClick()
                    }
            )
        }
    }
}


@Composable
private fun ColumnScope.lazyColumnModifier(): Modifier =
    Modifier
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .imePadding()
        .weight(1f)
        .padding(horizontal = 16.dp)

fun isSectionSelected(
    section: Section, selectedSection: Section
): Boolean {
    return section == selectedSection
}

@Composable
fun previewImageLoader(): ImageLoader {
    val context = LocalContext.current
    return ImageLoader.Builder(context).build()
}

@Composable
@Preview(showBackground = true, widthDp = 700, heightDp = 1000)
fun EditProfileScreenPersonalInfoPreview() {
    MdTheme {
        EditProfileScreen(
            state = EditProfileState(
                isLoading = false,
                selectedSection = Section.PERSONAL_INFO,
                personalInformation = PersonalInformation(
                    ethnicity = FieldState(
                        listOf(
                            DmEthnicity("Asian"),
                            DmEthnicity("Black"),
                            DmEthnicity("White"),
                            DmEthnicity("Hispanic"),
                            DmEthnicity("Other")
                        )
                    ),
                ),
                isInsuranceRequired = true,
                allowEditPatientDetails = true
            ),
            {},
            previewImageLoader()
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 700, heightDp = 1000)
fun EditProfileScreenContactInfoPreview() {
    MdTheme {
        EditProfileScreen(
            state = EditProfileState(
                selectedSection = Section.CONTACT_INFO,
                isLoading = false, isInsuranceRequired = true,
                allowEditPatientDetails = true
            ),
            {},
            previewImageLoader()
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 700, heightDp = 1000)
fun EditProfileScreenCaregiverPreview() {
    MdTheme {
        EditProfileScreen(
            state = EditProfileState(
                selectedSection = Section.CAREGIVER_DETAILS,
                isLoading = false,
                isInsuranceRequired = true,
                allowEditPatientDetails = true
            ),
            {},
            previewImageLoader()
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 700, heightDp = 1000)
fun EditProfileScreenPrimaryInsurancePreview() {
    MdTheme {
        EditProfileScreen(
            state = EditProfileState(
                selectedSection = Section.PRIMARY_INSURANCE,
                isLoading = false,
                isInsuranceRequired = true,
                allowEditPatientDetails = true
            ),
            {},
            previewImageLoader()
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 700, heightDp = 1000)
fun EditProfileScreenSecondaryInsurancePreview() {
    MdTheme {
        EditProfileScreen(
            state = EditProfileState(
                selectedSection = Section.SECONDARY_INSURANCE,
                isLoading = false,
                isInsuranceRequired = true,
                allowEditPatientDetails = true
            ),
            {},
            previewImageLoader()
        )
    }
}


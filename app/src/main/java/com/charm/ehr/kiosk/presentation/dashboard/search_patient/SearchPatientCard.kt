package com.charm.ehr.kiosk.presentation.dashboard.search_patient

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.util.DateUtil
import com.charm.ehr.kiosk.common.util.MDAccountsRelatedUtil
import com.charm.ehr.kiosk.common.util.ModelUtil
import com.charm.ehr.kiosk.domain.ContactDetails
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.presentation.composable.MdChipRoundCorner
import com.charm.ehr.kiosk.presentation.composable.MdCircularProgressBar
import com.charm.ehr.kiosk.presentation.composable.MdHighlightedText
import com.charm.ehr.kiosk.presentation.composable.MdHorizontalSpace
import com.charm.ehr.kiosk.presentation.composable.MdVerticalSpace
import com.charm.ehr.kiosk.presentation.composable.PatientImageItem
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import org.joda.time.DateTime
import org.joda.time.LocalDate

enum class SearchType(val displayName: Int) {
    NAME_OR_MOBILE(R.string.name_or_mobile),
    RECORD_ID(
        R.string.recordId
    ),
    DATE_OF_BIRTH(R.string.date_of_birth),
    EMAIL(R.string.email)
}

@Composable
fun SearchPatientCard(
    onClickPatient: (Patient) -> Unit,
    imageLoader: ImageLoader,
    onUpdateSearchType: (SearchType) -> Unit,
    state: SearchPatientState,
    onChangeSearchQuery: (String) -> Unit,
    onAddPatient: () -> Unit
) {

    val keyboardType = when (state.selectedSearchType) {
        SearchType.NAME_OR_MOBILE -> KeyboardType.Unspecified
        SearchType.EMAIL -> KeyboardType.Email
        SearchType.DATE_OF_BIRTH -> KeyboardType.Number
        SearchType.RECORD_ID -> KeyboardType.Unspecified
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 0.dp)
        ) {
            val selectedSearchTypeDisplayName =
                if (state.selectedSearchType.displayName == SearchType.DATE_OF_BIRTH.displayName) {
                    stringResource(state.selectedSearchType.displayName) + " (MM/DD/YYYY)"
                } else {
                    stringResource(state.selectedSearchType.displayName)
                }
            SearchPatientTextField(
                searchQuery = state.searchQuery,
                selectedSearchTypeDisplayName = selectedSearchTypeDisplayName,
                onChangeSearchQuery = onChangeSearchQuery,
                keyboardType,
                isDateType = state.selectedSearchType.displayName == SearchType.DATE_OF_BIRTH.displayName
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(12.dp)
            ) {
                SearchType.entries.forEach { searchType ->
                    MdChipRoundCorner(
                        text = stringResource(searchType.displayName),
                        isSelected = state.selectedSearchType == searchType,
                        onChipClick = {
                            onUpdateSearchType(searchType)
                        })
                }
            }

            MdVerticalSpace(2f)

            if (state.isLoading) {
                Spacer(modifier = Modifier.height(200.dp))
                MdCircularProgressBar(size = 50.dp)
            } else if (state.errorMessage != null) {

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Spacer(modifier = Modifier.height(200.dp))
                    Text(state.errorMessage.asString())

                }
            } else {
                SearchResultContent(state, onClickPatient, imageLoader, onAddPatient)
            }
        }
    }
}



@Composable
private fun SearchResultContent(
    state: SearchPatientState,
    onClickPatient: (Patient) -> Unit,
    imageLoader: ImageLoader,
    onAddPatient: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            keyboardController?.hide()
        }
    }
    if (state.selectedSearchType == SearchType.DATE_OF_BIRTH && state.searchQuery.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Spacer(modifier = Modifier.height(200.dp))
            Text(stringResource(R.string.enter_date_search))
        }
    } else if (state.selectedSearchType == SearchType.DATE_OF_BIRTH && state.searchQuery.length != 10) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Spacer(modifier = Modifier.height(200.dp))
            Text(stringResource(R.string.enter_valid_date_search))
        }
    } else if (state.searchQuery.length < 3) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Spacer(modifier = Modifier.height(200.dp))
            Text(stringResource(R.string.search_min_length, 3))
        }
    } else {
        if (state.searchResultList.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(start = 2.dp),
                text = stringResource(R.string.result),
                style = MdTheme.typography.semiBold20TextStyle,
                color = MdColors.Black60
            )
            MdVerticalSpace(2f)
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                state = listState
            ) {
                items(state.searchResultList) {
                    PatientSearchResultItem(
                        searchQuery = state.searchQuery,
                        patient = it,
                        selectedSearchType = state.selectedSearchType,
                        onClickPatient = {
                            onClickPatient(it)
                        }, imageLoader = imageLoader
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {

                Spacer(modifier = Modifier.height(300.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(
                            R.string.no_patients_found_for, state.searchQuery
                        ) + " with ${stringResource(state.selectedSearchType.displayName)} filter"
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    TextButton(
                        modifier = Modifier.defaultMinSize(minWidth = 96.dp),
                        onClick = { onAddPatient() },
                        colors = ButtonDefaults.textButtonColors(containerColor = MdColors.BrandLightPinkVariant)
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                            text = stringResource(R.string.add_patient),
                            style = MdTheme.typography.medium18TextStyle.copy(color = MdColors.BrandPink)
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun PatientSearchResultItem(
    searchQuery: String,
    patient: Patient,
    imageSize: Dp = 60.dp,
    textStyle: TextStyle = MdTheme.typography.semiBold16TextStyle,
    detailTextStyle: TextStyle = MdTheme.typography.regular14TextStyle,
    detailTextColor: Color = MdColors.BlackText,
    onClickPatient: (Patient) -> Unit,
    imageLoader: ImageLoader,
    isMinimizedView: Boolean = false,
    selectedSearchType: SearchType
) {

    val patientName = patient.fullName.orEmpty()
    val patientRecordId = patient.patientRecordId.orEmpty()
    val patientDOB = patient.dateOfBirth?.toString(ModelUtil.charmApiDateFormat()).orEmpty()
    val patientPhoneNumber = ModelUtil.getPhoneNumberFromPatient(patient)
    val emailId = patient.contactDetails?.emailId.orEmpty()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = { onClickPatient(patient) })
            .padding(10.dp)
    ) {

        val photoPath = MDAccountsRelatedUtil.getPatientPhotoUrl(patient.practicePatientId)
        PatientImageItem(
            hasImage = patient.isPhotoAvailableOnline,
            imageUrl = photoPath,
            imageLoader = imageLoader,
            imgSize = imageSize,
            selectedPatient = patient,
            textStyle = MdTheme.typography.medium20TextStyle.copy(
                letterSpacing = 1.sp,
                fontSize = 24.sp
            )
        )

        MdHorizontalSpace(numberOfSpaces = 2f)
        Column(verticalArrangement = Arrangement.Center) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                MdHighlightedText(
                    text = patientName,
                    style = textStyle,
                    modifier = Modifier.padding(vertical = 4.dp),
                    searchQuery = searchQuery.canShowHighlight(
                        SearchType.NAME_OR_MOBILE,
                        selectedSearchType
                    ),
                    highlightColor = MdColors.PaleYellow
                )

                if (patientRecordId.isNotBlank()) {

                    MdHorizontalSpace()

                    MdHighlightedText(
                        modifier = Modifier.padding(top = 2.dp),
                        text = " $patientRecordId ",
                        style = detailTextStyle.copy(color = detailTextColor),
                        searchQuery = searchQuery.canShowHighlight(
                            SearchType.RECORD_ID,
                            selectedSearchType
                        ),
                        highlightColor = MdColors.PaleYellow
                    )
                    MdHorizontalSpace(2.5f)
                }


            }

            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {

                if (patientDOB.isNotBlank()) {
                    ResultDetailIcon(R.drawable.ic_outline_cake_24)
                    MdHighlightedText(
                        text = patientDOB,
                        style = detailTextStyle.copy(color = detailTextColor),
                        searchQuery = searchQuery.canShowHighlight(
                            SearchType.DATE_OF_BIRTH,
                            selectedSearchType
                        ),
                        highlightColor = MdColors.PaleYellow
                    )
                    MdHorizontalSpace(2.5f)
                }

                if (patientPhoneNumber.isNotBlank()) {
                    ResultDetailIcon(R.drawable.ic_outline_phone_24)
                    MdHighlightedText(
                        text = patientPhoneNumber,
                        style = detailTextStyle.copy(color = detailTextColor),
                        searchQuery = searchQuery.canShowHighlight(
                            SearchType.NAME_OR_MOBILE,
                            selectedSearchType
                        ),
                        highlightColor = MdColors.PaleYellow
                    )
                    MdHorizontalSpace(2.5f)
                }

                if (emailId.isNotBlank()) {
                    ResultDetailIcon(R.drawable.ic_outline_email_24)
                    MdHighlightedText(
                        text = emailId,
                        style = detailTextStyle.copy(color = detailTextColor),
                        searchQuery = searchQuery.canShowHighlight(
                            SearchType.EMAIL,
                            selectedSearchType
                        ),
                        highlightColor = MdColors.PaleYellow,
                        middleEllipsis = true,
                    )
                    MdHorizontalSpace(2.5f)
                }

            }
        }
    }
}

fun String.canShowHighlight(fieldSearchType: SearchType, selectedSearchType: SearchType): String {
    if (fieldSearchType == selectedSearchType) {
        return this
    } else return ""
}

@Composable
fun ResultDetailIcon(@DrawableRes iconDrawable: Int) {
    Icon(
        painter = painterResource(id = iconDrawable),
        contentDescription = null,
        tint = MdColors.Black70,
        modifier = Modifier.size(16.dp)
    )
    MdHorizontalSpace()
}

@Composable
fun ResultDetailText(
    text: String, style: TextStyle, color: Color, applyEllipsis: Boolean = false
) {
    if (applyEllipsis) {
        Text(
            text = text,
            style = style,
            color = color,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    } else {
        Text(
            text = text, style = style, color = color
        )
    }
    MdHorizontalSpace(numberOfSpaces = 3f)
}

@Preview(showBackground = true, device = "spec:width=700dp,height=1200dp")
@Composable
fun PatientSearchCardPreview() {
    val mockPatient = Patient(
        fullName = "Jane Doe",
        patientRecordId = "12345",
        dateOfBirth = DateTime.now(),
        contactDetails = ContactDetails(
            mobile = "1234567890", emailId = "jane.doe@example.com"
        ),
        isPhotoAvailableOnline = false,
        practicePatientId = "P123",
    )
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()

    Row {

        PatientSearchResultItem(
            searchQuery = "2025",
            patient = mockPatient,
            imageLoader = imageLoader,
            onClickPatient = { },
            selectedSearchType = SearchType.NAME_OR_MOBILE
        )
    }
}

@Composable
fun SearchPatientTextField(
    searchQuery: String,
    selectedSearchTypeDisplayName: String,
    onChangeSearchQuery: (String) -> Unit,
    keyboardType: KeyboardType,
    isDateType: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }

    var textFieldValue by remember(searchQuery) {
        mutableStateOf(
            TextFieldValue(
                text = searchQuery,
                selection = TextRange(searchQuery.length)
            )
        )
    }
    val dateTypeText = stringResource(SearchType.DATE_OF_BIRTH.displayName)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    TextField(
        value = textFieldValue,
        onValueChange = {
            if (isDateType) {
                val isAdding = it.text.length > textFieldValue.text.length
                val result = DateUtil.validateAndFormatDateInput(
                    it.text,
                    minDate = null,
                    maxDate = LocalDate.now(),
                    isAdding
                )
                if (result != null) {
                    textFieldValue = TextFieldValue(
                        text = result.formatted,
                        selection = TextRange(result.formatted.length)
                    )
                    onChangeSearchQuery(result.formatted)
                }
            } else {
                textFieldValue = it
                onChangeSearchQuery(it.text)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        placeholder = {
            Text(stringResource(R.string.search_patients_by, selectedSearchTypeDisplayName))
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MdColors.LightGray, shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 16.dp)
            .focusRequester(focusRequester),
        shape = RoundedCornerShape(50),
        singleLine = true,
        textStyle = MdTheme.typography.regular20TextStyle,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent
        ),
        trailingIcon = {
            if (textFieldValue.text.isNotEmpty()) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "Clear Icon",
                    modifier = Modifier.clickable {
                        textFieldValue = TextFieldValue("")
                        onChangeSearchQuery("")
                    }
                )
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 600)
@Composable
fun SearchPatientTextFieldPreview() {
    MdTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            // Preview with empty search
            SearchPatientTextField(
                searchQuery = "",
                selectedSearchTypeDisplayName = "Name or Mobile",
                onChangeSearchQuery = {},
                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Preview with search query
            SearchPatientTextField(
                searchQuery = "Smith",
                selectedSearchTypeDisplayName = "Name or Mobile",
                onChangeSearchQuery = {},
                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Preview with different search type
            SearchPatientTextField(
                searchQuery = "example@email.com",
                selectedSearchTypeDisplayName = stringResource(R.string.date_of_birth),
                onChangeSearchQuery = {},
                keyboardType = KeyboardType.Number
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1000)
@Composable
fun SearchPatientCardWithResultsPreview() {
    val mockPatients = listOf(
        Patient(
            fullName = "John Smith",
            patientRecordId = "P12345",
            dateOfBirth = DateTime.now().minusYears(35),
            contactDetails = ContactDetails(
                mobile = "5551234567",
                emailId = "john.smith@example.com"
            ),
            isPhotoAvailableOnline = false,
            practicePatientId = "P001"
        ),
        Patient(
            fullName = "Jane Doe",
            patientRecordId = "P67890",
            dateOfBirth = DateTime.now().minusYears(28),
            contactDetails = ContactDetails(
                mobile = "5559876543",
                emailId = "jane.doe@example.com"
            ),
            isPhotoAvailableOnline = false,
            practicePatientId = "P002"
        ),
        Patient(
            fullName = "Robert Johnson",
            patientRecordId = "P11223",
            dateOfBirth = DateTime.now().minusYears(42),
            contactDetails = ContactDetails(
                mobile = "5555551234",
                emailId = "robert.j@example.com"
            ),
            isPhotoAvailableOnline = false,
            practicePatientId = "P003"
        )
    )

    val imageLoader = ImageLoader.Builder(LocalContext.current).build()

    MdTheme {
        SearchPatientCard(
            onClickPatient = {},
            imageLoader = imageLoader,
            onUpdateSearchType = {},
            state = SearchPatientState(
                isLoading = false,
                errorMessage = null,
                searchResultList = mockPatients,
                searchQuery = "John",
                selectedSearchType = SearchType.NAME_OR_MOBILE
            ),
            onChangeSearchQuery = {},
            onAddPatient = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 1000)
@Composable
fun SearchPatientCardWithResultsPreviewLandscape() {
    val mockPatients = listOf(
        Patient(
            fullName = "John Smith",
            patientRecordId = "P12345",
            dateOfBirth = DateTime.now().minusYears(35),
            contactDetails = ContactDetails(
                mobile = "5551234567",
                emailId = "john.smith@example.com"
            ),
            isPhotoAvailableOnline = false,
            practicePatientId = "P001"
        ),
        Patient(
            fullName = "Jane Doe",
            patientRecordId = "P67890",
            dateOfBirth = DateTime.now().minusYears(28),
            contactDetails = ContactDetails(
                mobile = "5559876543",
                emailId = "jane.doe@example.com"
            ),
            isPhotoAvailableOnline = false,
            practicePatientId = "P002"
        ),
        Patient(
            fullName = "Robert Johnson",
            patientRecordId = "P11223",
            dateOfBirth = DateTime.now().minusYears(42),
            contactDetails = ContactDetails(
                mobile = "5555551234",
                emailId = "robert.j@example.com"
            ),
            isPhotoAvailableOnline = false,
            practicePatientId = "P003"
        )
    )

    val imageLoader = ImageLoader.Builder(LocalContext.current).build()

    MdTheme {
        SearchPatientCard(
            onClickPatient = {},
            imageLoader = imageLoader,
            onUpdateSearchType = {},
            state = SearchPatientState(
                isLoading = false,
                errorMessage = null,
                searchResultList = mockPatients,
                searchQuery = "John",
                selectedSearchType = SearchType.NAME_OR_MOBILE
            ),
            onChangeSearchQuery = {},
            onAddPatient = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 600, heightDp = 1000)
@Composable
fun SearchPatientCardLoadingPreview() {
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()

    MdTheme {
        SearchPatientCard(
            onClickPatient = {},
            imageLoader = imageLoader,
            onUpdateSearchType = {},
            state = SearchPatientState(
                isLoading = true,
                errorMessage = null,
                searchResultList = emptyList(),
                searchQuery = "Smith",
                selectedSearchType = SearchType.NAME_OR_MOBILE
            ),
            onChangeSearchQuery = {},
            onAddPatient = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 600, heightDp = 1000)
@Composable
fun SearchPatientCardNoResultsPreview() {
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()

    MdTheme {
        SearchPatientCard(
            onClickPatient = {},
            imageLoader = imageLoader,
            onUpdateSearchType = {},
            state = SearchPatientState(
                isLoading = false,
                errorMessage = null,
                searchResultList = emptyList(),
                searchQuery = "NonExistentPatient",
                selectedSearchType = SearchType.NAME_OR_MOBILE
            ),
            onChangeSearchQuery = {},
            onAddPatient = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 500, heightDp = 1000)
@Composable
fun SearchPatientCardDateOfBirthPreview() {
    val mockPatients = listOf(
        Patient(
            fullName = "Sarah Williams",
            patientRecordId = "P44556",
            dateOfBirth = DateTime.parse("1990-05-15"),
            contactDetails = ContactDetails(
                mobile = "5554445555",
                emailId = "sarahasdfasdfasdfasdfasdfasdfasdf.w@example.com"
            ),
            isPhotoAvailableOnline = false,
            practicePatientId = "P004"
        )
    )

    val imageLoader = ImageLoader.Builder(LocalContext.current).build()

    MdTheme {
        SearchPatientCard(
            onClickPatient = {},
            imageLoader = imageLoader,
            onUpdateSearchType = {},
            state = SearchPatientState(
                isLoading = false,
                errorMessage = null,
                searchResultList = mockPatients,
                searchQuery = "05/15/1990",
                selectedSearchType = SearchType.DATE_OF_BIRTH
            ),
            onChangeSearchQuery = {},
            onAddPatient = {}
        )
    }
}


package com.charm.ehr.kiosk.presentation.select_patient

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.util.MDAccountsRelatedUtil
import com.charm.ehr.kiosk.common.util.ModelUtil
import com.charm.ehr.kiosk.domain.ContactDetails
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.presentation.composable.CharmKioskLogoHeader
import com.charm.ehr.kiosk.presentation.composable.FacilityLogoRow
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdCommonPrimaryButton
import com.charm.ehr.kiosk.presentation.composable.MdHorizontalSpace
import com.charm.ehr.kiosk.presentation.composable.MdVerticalSpace
import com.charm.ehr.kiosk.presentation.composable.PatientImageItem
import com.charm.ehr.kiosk.presentation.dashboard.search_patient.ResultDetailIcon
import com.charm.ehr.kiosk.presentation.dashboard.search_patient.ResultDetailText
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import org.joda.time.DateTime

@Composable
fun SelectPatientScreen(
    state: SelectPatientState,
    onAction: (SelectPatientAction) -> Unit,
    imageLoader: ImageLoader,
) {

    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MdColors.MdSurfaceColor)
            .windowInsetsPadding(
                WindowInsets.systemBars.union(WindowInsets.displayCutout)
            ),
        contentAlignment = Alignment.Center
    ) {

        CharmKioskLogoHeader(modifier = Modifier.align(Alignment.TopCenter))

        MdCardWithoutTonalElevation(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(if (isPortrait) 0.75f else 0.5f)
                .padding(16.dp)
        ) {

            Column(
                modifier = Modifier.padding(38.dp)
            ) {

                FacilityLogoRow(
                    imageUrl = state.facilityLogoUrl,
                    imageLoader = imageLoader,
                    imgSize = 64.dp,
                    facilityName = state.facilityName,
                )

                MdVerticalSpace(4f)

                Text(
                    text = stringResource(R.string.select_a_patient_account),
                    style = MdTheme.typography.bold24TextStyle,
                )

                MdVerticalSpace(2f)

                LazyColumn {
                    items(state.patientList) { patient ->
                        PatientItem(
                            patient = patient,
                            onClickPatient = {
                                onAction(SelectPatientAction.OnSelectPatient(it))
                            },
                            imageLoader = imageLoader,
                            selectedPatient = state.selectedPatient
                        )
                    }
                }

                MdVerticalSpace(4f)

                MdCommonPrimaryButton(
                    text = stringResource(R.string.next),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onAction(SelectPatientAction.OnClickDone)
                    },
                )

            }

        }

        Box(
            modifier = Modifier
                .widthIn(min = 48.dp)
                .align(Alignment.BottomEnd)
                .padding(end = 54.dp, bottom = 54.dp)
                .clickable {
                    onAction(SelectPatientAction.OnExitClicked)
                },
            contentAlignment = Alignment.Center
        ) {
            Column {           Icon(
                painter = painterResource(R.drawable.ic_logout),
                contentDescription = null,
                modifier = Modifier.size(34.dp) // Icon size remains the same
            )
                Text(stringResource(R.string.exitStr), style = MdTheme.typography.medium16TextStyle)}
        }
    }

}

@Composable
fun PatientItem(
    patient: Patient,
    imageSize: Dp = 64.dp,
    textStyle: TextStyle = MdTheme.typography.medium18TextStyle,
    detailTextStyle: TextStyle = MdTheme.typography.regular14TextStyle,
    detailTextColor: Color = MdColors.BlackText,
    onClickPatient: (Patient) -> Unit,
    imageLoader: ImageLoader,
    selectedPatient: Patient? = null,
) {

    val patientName = remember(patient) { patient.fullName.orEmpty() }
    val patientRecordId = remember(patient) { patient.patientRecordId.orEmpty() }
    val patientDOB = remember(patient) { patient.dateOfBirth?.toString(ModelUtil.charmApiDateFormat()).orEmpty() }
    val patientPhoneNumber = remember(patient) { ModelUtil.getPhoneNumberFromPatient(patient) }
    val emailId = remember(patient) { patient.contactDetails?.emailId.orEmpty() }

    MdCardWithoutTonalElevation(
        modifier = Modifier.padding(vertical = 8.dp),
        elevation = 0.dp,
        border = BorderStroke(
            width = 1.dp,
            color = MdColors.blackishGrey
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(),
                    onClick = { onClickPatient(patient) })
                .padding(vertical = 8.dp)
        ) {

            MdHorizontalSpace(1.5f)

            val photoPath = MDAccountsRelatedUtil.getPatientPhotoUrl(patient.practicePatientId)
            PatientImageItem(
                hasImage = patient.isPhotoAvailableOnline,
                imageUrl = photoPath,
                imageLoader = imageLoader,
                imgSize = imageSize,
                selectedPatient = patient,
                textStyle = MdTheme.typography.medium20TextStyle.copy(letterSpacing = 1.sp, fontSize = 24.sp)
            )

            MdHorizontalSpace(1.5f)
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = patientName,
                        style = textStyle,
                    )
                    MdHorizontalSpace(1f)

                    patient.gender?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            modifier = Modifier
                                .padding(top = 2.dp), text = "($it)",
                            style = detailTextStyle
                        )
                    }
                }

                MdVerticalSpace(1f)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ResultDetailText(
                        text = patientRecordId, style = detailTextStyle, color = detailTextColor
                    )

                    if (patientDOB.isNotBlank()) {
                        ResultDetailIcon(R.drawable.ic_outline_cake_24)
                        ResultDetailText(
                            text = patientDOB, style = detailTextStyle, color = detailTextColor
                        )
                    }

                }
            }

            RadioButton(
                modifier = Modifier,
                selected = checkForSelectedPatient(patient, selectedPatient),
                onClick = {
                    onClickPatient(patient)
                }
            )

        }
    }

}

fun checkForSelectedPatient(
    patient: Patient,
    selectedPatient: Patient?
): Boolean {
    return selectedPatient?.practicePatientId == patient.practicePatientId
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1200)
@Composable
fun PreviewSelectPatientScreen() {
    val mockState = SelectPatientState(
        facilityName = "Charm health hospital",
        isLoading = false,
        patientList = listOf(
            Patient(
                fullName = "Lewis Hamilton",
                gender = "Male",
                practicePatientId = "110001189",
                patientRecordId = "CH001231",
                dateOfBirth = DateTime.now(),
                contactDetails = ContactDetails(
                    mobile = "1234567890", emailId = "jane.doe@example.com"
                ),
                isPhotoAvailableOnline = false,
            ),
            Patient(
                fullName = "Max Verstappen",
                gender = "Male",
                patientRecordId = "CH001231",
                dateOfBirth = DateTime.now(),
                contactDetails = ContactDetails(
                    mobile = "1234567890", emailId = "jane.doe@example.com"
                ),
                isPhotoAvailableOnline = false,
                practicePatientId = "P123"
            ),
        ),
        selectedPatient =  Patient(
            fullName = "Lewis Hamilton",
            gender = "Male",
            patientRecordId = "CH001231",
            dateOfBirth = DateTime.now(),
            contactDetails = ContactDetails(
                mobile = "1234567890", emailId = "jane.doe@example.com"
            ),
            isPhotoAvailableOnline = false,
            practicePatientId = "110001189",
        )
    )

    val mockOnAction: (SelectPatientAction) -> Unit = {}
    val mockImageLoader = ImageLoader.Builder(LocalContext.current).build()


    MdTheme {
        SelectPatientScreen(
            state = mockState, onAction = mockOnAction, imageLoader = mockImageLoader
        )
    }
}

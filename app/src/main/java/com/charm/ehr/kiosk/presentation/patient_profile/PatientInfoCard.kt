package com.charm.ehr.kiosk.presentation.patient_profile

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import com.charm.ehr.kiosk.domain.ContactDetails
import com.charm.ehr.kiosk.common.util.MDAccountsRelatedUtil
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.util.UIUtil
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdHorizontalSpace
import com.charm.ehr.kiosk.presentation.composable.MdVerticalSpace
import com.charm.ehr.kiosk.presentation.composable.PatientImageItem
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import com.charm.ehr.kiosk.common.util.ModelUtil
import org.joda.time.DateTime

@Composable
fun PatientInfoCard(
    patient: Patient,
    imageLoader: ImageLoader,
    state: PatientProfileState,
    showEdit: Boolean,
    onClickEdit: () -> Unit
) {

    val patientImageSize = 60.dp

    val phoneNumber = remember(patient) {
        ModelUtil.getPhoneNumberFromPatient(patient)
    }

    MdCardWithoutTonalElevation(
        modifier = Modifier.padding(
            horizontal = MdTheme.dimension.contentCardPadding, vertical = MdTheme.dimension.halfCardPadding
        ), elevation = 0.dp
    ) {
        Column(Modifier.padding(MdTheme.dimension.contentCardPadding)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val photoPath = MDAccountsRelatedUtil.getPatientPhotoUrl(patient.practicePatientId)
                PatientImageItem(
                    hasImage = patient.isPhotoAvailableOnline,
                    imageUrl = photoPath,
                    imageLoader = imageLoader,
                    imgSize = patientImageSize,
                    selectedPatient = patient,
                    textStyle = MdTheme.typography.medium20TextStyle.copy(
                        letterSpacing = 1.sp,
                        fontSize = 22.sp
                    )
                )
                Spacer(modifier = Modifier.size(MdTheme.dimension.contentCardPadding))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        patient.fullName?.let {
                            if (it.isNotBlank()) {
                                Text(
                                    it,
                                    style = MdTheme.typography.semiBold16TextStyle.copy(color = MdColors.DeepGrey)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(MdTheme.dimension.halfCardPadding))
                        patient.pronouns?.let {
                            if (it.isNotBlank()) {
                                Text(it, style = MdTheme.typography.regular12TextStyle)
                            }
                        }

                    }
                    MdVerticalSpace(.5f)

                    Row {
                        patient.gender?.let { gender ->
                            Text(gender, style = MdTheme.typography.regular14TextStyle)
                            MdHorizontalSpace(.5f)
                        }
                        MdHorizontalSpace(.5f)
                        Text(
                            UIUtil.getAgeFromDob(patient.dateOfBirth),
                            style = MdTheme.typography.regular14TextStyle
                        )
                    }
                }
                MdHorizontalSpace(modifier = Modifier.weight(1f))
                if (showEdit) {
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            modifier = Modifier.defaultMinSize(minWidth = 96.dp, minHeight = 46.dp),
                            onClick = {
                                onClickEdit()
                            },
                            colors = ButtonDefaults.textButtonColors(containerColor = MdColors.MildPinkishGrey)
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = stringResource(R.string.edit_str),
                                style = MdTheme.typography.semiBold16TextStyle.copy(color = MdColors.BrandPink)
                            )
                        }
                        MdHorizontalSpace()
//                        Icon(
//                            painter = painterResource(R.drawable.ic_outline_edit_24),
//                            contentDescription = "Edit icon",
//                            modifier = Modifier
//                                .size(25.dp)
//                                .clickable { onClickEdit() })
                    }
                }
            }
            Spacer(modifier = Modifier.size(MdTheme.dimension.contentCardPadding))

            val infoItems: List<@Composable () -> Unit> = listOfNotNull(
                patient.patientRecordId?.takeIf { it.isNotBlank() }?.let {
                    @Composable {
                        PatientInfoItem(
                            iconDrawable = R.drawable.ic_patient_id,
                            value = it
                        )
                    }
                },
                patient.dateOfBirth?.let {
                    @Composable {
                        PatientInfoItem(
                            iconDrawable = R.drawable.ic_dob,
                            value = it.toString(ModelUtil.charmDisplayDateFormat())
                        )
                    }
                },
                phoneNumber.takeIf { it.isNotBlank() }?.let {
                    @Composable {
                        PatientInfoItem(
                            iconDrawable = R.drawable.ic_outline_phone_24,
                            value = it
                        )
                    }
                },
                patient.bloodGroup?.takeIf { it.isNotBlank() }?.let {
                    @Composable {
                        PatientInfoItem(
                            iconDrawable = R.drawable.ic_blood_drop,
                            value = it
                        )
                    }
                }
            )

            val configuration = LocalConfiguration.current
            val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

            val itemCount = infoItems.size
            val rowModifier = if (isPortrait) {
                if (itemCount > 2) {
                    Modifier.fillMaxWidth(0.95f)
                } else if (itemCount == 2) {
                    Modifier.fillMaxWidth(0.5f)
                } else {
                    Modifier.wrapContentWidth()
                }
            } else {
                if (itemCount > 2) {
                    Modifier.fillMaxWidth(0.75f)
                } else if (itemCount == 2) {
                    Modifier.fillMaxWidth(0.4f)
                } else {
                    Modifier.wrapContentWidth()
                }

            }

            Row(
                modifier = rowModifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween

            ) {
                infoItems.forEach { patientInfoItem ->
                    Box(
                        modifier = Modifier, contentAlignment = Alignment.Center
                    ) {
                        patientInfoItem()
                    }
                }
            }

        }
    }
}

@Composable
fun PatientInfoItem(@DrawableRes iconDrawable: Int, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(iconDrawable),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(value, style = MdTheme.typography.medium16TextStyle)
    }
}

@Preview(showBackground = true, widthDp = 600, heightDp = 900)
@Composable
fun PreviewPatientInfoCard() {
    val samplePatient = Patient(
        fullName = "John Doe",
        pronouns = "He/Him",
        patientRecordId = "12345",
        gender = "Male",
        dateOfBirth = DateTime("1990-01-20"),
        bloodGroup = "O+",
        language = "English",
        isPhotoAvailableOnline = true,
        practicePatientId = "P12345",
        patientImgFilePath = "",
        contactDetails = ContactDetails(mobile = "9876543210")
    )
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()

    MdTheme {
        PatientInfoCard(
            patient = samplePatient,
            imageLoader = imageLoader,
            state = PatientProfileState(),
            showEdit = true,
            onClickEdit = {})
    }
}


@Preview(showBackground = true, widthDp = 900, heightDp = 600)
@Composable
fun PreviewPatientInfoCardLandscape() {
    val samplePatient = Patient(
        fullName = "John Doe",
        pronouns = "He/Him",
        patientRecordId = "12345",
        gender = "Male",
        dateOfBirth = DateTime("1990-01-20"),
        bloodGroup = "O+",
        language = "English",
        isPhotoAvailableOnline = true,
        practicePatientId = "P12345",
        patientImgFilePath = "",
        contactDetails = ContactDetails(mobile = "9876543210")
    )
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()

    MdTheme {
        PatientInfoCard(
            patient = samplePatient,
            imageLoader = imageLoader,
            state = PatientProfileState(),
            showEdit = true,
            onClickEdit = {})
    }
}

@Preview(showBackground = true, widthDp = 600, heightDp = 900)
@Composable
fun PreviewPatientInfoCardPortraitWithTwoItems() {
    val samplePatient = Patient(
        fullName = "John Doe",
        pronouns = "He/Him",
        patientRecordId = "12345",
        gender = "Male",
        dateOfBirth = DateTime("1990-01-20"),
        language = "English",
        isPhotoAvailableOnline = true,
        practicePatientId = "P12345",

    )
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()

    MdTheme {
        PatientInfoCard(
            patient = samplePatient,
            imageLoader = imageLoader,
            state = PatientProfileState(),
            showEdit = true,
            onClickEdit = {})
    }
}


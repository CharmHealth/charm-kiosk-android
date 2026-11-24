package com.charm.ehr.kiosk.presentation.patient_profile

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.domain.Caregiver
import com.charm.ehr.kiosk.domain.ContactDetails
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.domain.PostalAddress
import com.charm.ehr.kiosk.domain.PracticePayer
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdHorizontalSpace
import com.charm.ehr.kiosk.presentation.composable.MdVerticalSpace
import com.charm.ehr.kiosk.presentation.edit_profile.FieldState
import com.charm.ehr.kiosk.presentation.edit_profile.Insurance
import com.charm.ehr.kiosk.presentation.edit_profile.PersonDetail
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import com.charm.ehr.kiosk.common.util.ModelUtil
import org.joda.time.DateTime

@Composable
fun PatientDetailSection(
    patient: Patient,
    state: PatientProfileState,
    showEdit: Boolean,
    onClickEdit: (Section) -> Unit,
    onAddMissingSection: (Section) -> Unit,
    onRemoveMissingSection: (Section) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {

        MdVerticalSpace(1f)

        val demographicsList = getDemographicsDetails(patient)
        val isDemographicsMissing = demographicsList.all { it.second.isNullOrBlank() }
        LaunchedEffect(isDemographicsMissing) {
            if (isDemographicsMissing) {
                onAddMissingSection(Section.PERSONAL_INFO)
            } else {
                onRemoveMissingSection(Section.PERSONAL_INFO)
            }
        }
        if (!isDemographicsMissing) {
            ProfileSection(
                heading = stringResource(R.string.demographics),
                details = demographicsList,
                iconResId = R.drawable.ic_group,
                showEdit = showEdit && state.allowEditPatientDetails,
                onClickEdit = {
                    onClickEdit(Section.PERSONAL_INFO)
                }
            )
        }

        val contactDetailsList = getContactDetails(patient)
        val isContactDetailsMissing = contactDetailsList.all { it.second.isNullOrBlank() }

        LaunchedEffect(isContactDetailsMissing) {
            if (isContactDetailsMissing) {
                onAddMissingSection(Section.CONTACT_INFO)
            } else {
                onRemoveMissingSection(Section.CONTACT_INFO)
            }
        }

        val emergencyContactDetailsList = getEmergencyContactDetails(patient)
        val isEmergencyContactMissing =
            emergencyContactDetailsList.all { it.second.isNullOrBlank() }

        LaunchedEffect(isEmergencyContactMissing) {
            if (isEmergencyContactMissing) {
                onAddMissingSection(Section.EMERGENCY_CONTACT)
            } else {
                onRemoveMissingSection(Section.EMERGENCY_CONTACT)
            }
        }

        if (!isContactDetailsMissing || !isEmergencyContactMissing) {
            ProfileSection(
                heading = stringResource(R.string.contact_details),
                details = contactDetailsList,
                iconResId = R.drawable.ic_contact,
                showEdit = showEdit && state.allowEditPatientDetails,
                onClickEdit = {
                    onClickEdit(Section.CONTACT_INFO)
                }
            )
        }

        if (!isEmergencyContactMissing) {
            ProfileSection(
                heading = stringResource(R.string.emergency_contact),
                details = emergencyContactDetailsList,
                iconResId = R.drawable.ic_contact,
                showEdit = showEdit && state.allowEditPatientDetails,
                onClickEdit = {
                    onClickEdit(Section.CONTACT_INFO)
                }
            )
        }

        val caregiversDetailsList = patient.caregivers.map { getCaregiverDetails(it) }
        val hasAnyCaregiverData = caregiversDetailsList.any { details ->
            details.any { !it.second.isNullOrBlank() }
        }
        val isCaregiverMissing = patient.caregivers.isEmpty() || !hasAnyCaregiverData

        LaunchedEffect(isCaregiverMissing) {
            if (isCaregiverMissing) {
                Log.d("PatientDetailSection", "Caregiver details missing")
                onAddMissingSection(Section.CAREGIVER_DETAILS)
            } else {
                Log.d("PatientDetailSection", "Caregiver details present")
                onRemoveMissingSection(Section.CAREGIVER_DETAILS)
            }
        }

        if (!isCaregiverMissing) {
            caregiversDetailsList.forEach { details ->
                ProfileSection(
                    heading = stringResource(R.string.caregiver),
                    details = details,
                    iconResId = R.drawable.ic_care,
                    showEdit = showEdit && state.allowEditPatientDetails,
                    onClickEdit = {
                        onClickEdit(Section.CAREGIVER_DETAILS)
                    }
                )
            }
        }

        if (state.isInsuranceRequired) {
            val primaryInsuranceDetails = getInsuranceDetails(patient.primaryInsurance)
            val isPrimaryInsuranceMissing =
                primaryInsuranceDetails.all { it.second.isNullOrBlank() }

            LaunchedEffect(isPrimaryInsuranceMissing) {
                if (isPrimaryInsuranceMissing) {
                    onAddMissingSection(Section.PRIMARY_INSURANCE)
                } else {
                    onRemoveMissingSection(Section.PRIMARY_INSURANCE)
                }
            }

            if (!isPrimaryInsuranceMissing) {
                ProfileSection(
                    heading = stringResource(R.string.primary_insurance),
                    details = primaryInsuranceDetails,
                    iconResId = R.drawable.ic_insurance,
                    showEdit = showEdit,
                    onClickEdit = {
                        onClickEdit(Section.PRIMARY_INSURANCE)
                    }
                )
            }

            val secondaryInsuranceDetails = getInsuranceDetails(patient.secondaryInsurance)
            val isSecondaryInsuranceMissing =
                secondaryInsuranceDetails.all { it.second.isNullOrBlank() }

            LaunchedEffect(isSecondaryInsuranceMissing) {
                if (isSecondaryInsuranceMissing) {
                    onAddMissingSection(Section.SECONDARY_INSURANCE)
                } else {
                    onRemoveMissingSection(Section.SECONDARY_INSURANCE)
                }
            }

            if (!isSecondaryInsuranceMissing) {
                ProfileSection(
                    heading = stringResource(R.string.secondary_insurance),
                    details = secondaryInsuranceDetails,
                    iconResId = R.drawable.ic_insurance,
                    showEdit = showEdit,
                    onClickEdit = {
                        onClickEdit(Section.SECONDARY_INSURANCE)
                    }
                )
            }
        }

    }
//    }
}

@Composable
private fun ProfileSection(
    heading: String,
    details: List<Pair<Int, String?>>,
    @DrawableRes iconResId: Int,
    showEdit: Boolean,
    onClickEdit: () -> Unit
) {
    val mappedDetails = details.map {
        Pair(stringResource(it.first), it.second ?: "")
    }

    if (mappedDetails.any { it.second.isNotBlank() }) {
        RenderProfileDetailItem(
            heading = heading,
            detailList = mappedDetails,
            showEdit = showEdit,
            iconResId = iconResId,
            onClickEdit = onClickEdit
        )
    }
}

private fun getDemographicsDetails(patient: Patient): List<Pair<Int, String?>> {
    return listOf(
        R.string.gender_identity to patient.genderIdentity,
        R.string.language to patient.language,
        R.string.race to patient.race,
        R.string.ethnicity to patient.ethnicity,
        R.string.marital_status to patient.maritalStatus,
        R.string.employment_status to patient.employmentStatus,
        R.string.smoking_status to patient.smokingStatus
    )
}

private fun getContactDetails(patient: Patient): List<Pair<Int, String?>> {
    val workPhone = ModelUtil.getWorkPhoneFromPatient(patient)
    return listOf(
        R.string.address to patient.postalAddress.toString(),
        R.string.primary_phone to (patient.contactDetails?.primaryPhone ?: ""),
        R.string.preferred_communication to (patient.contactDetails?.preferredCommunication ?: ""),
        R.string.email to (patient.contactDetails?.emailId ?: ""),
        R.string.mobile_phone to (patient.contactDetails?.mobilePhone ?: ""),
        R.string.home_phone to (patient.contactDetails?.homePhone ?: ""),
        R.string.work_phone to workPhone
    )
}

private fun getEmergencyContactDetails(patient: Patient): List<Pair<Int, String?>> {
    return listOf(
        R.string.name to (patient.emergencyContactName ?: ""),
        R.string.phone_number to ((patient.emergencyContactExtn
            ?: "") + (patient.emergencyContactNumber ?: ""))
    )
}

private fun getCaregiverDetails(caregiver: Caregiver): List<Pair<Int, String?>> {
    return listOf(
        R.string.relationship to (caregiver.relationship ?: ""),
        R.string.name to ModelUtil.getCaregiverFullName(caregiver),
        R.string.date_of_birth to (caregiver.dateOfBirth ?: ""),
        R.string.birth_sex to (caregiver.gender ?: ""),
        R.string.address to caregiver.postalAddress.toString(),
        R.string.mobile_phone to (caregiver.contactDetails?.mobilePhone ?: ""),
        R.string.home_phone to (caregiver.contactDetails?.homePhone ?: "")
    )
}

private fun getInsuranceDetails(insurance: Insurance?): List<Pair<Int, String?>> {
    val details = ArrayList<Pair<Int, String?>>()
    insurance?.let { ins ->
        ins.insuredPersonDetail?.let { personDetail ->
            details.add(R.string.name to "${personDetail.firstName.value.orEmpty()} ${personDetail.lastName.value.orEmpty()}".trim())
            details.add(R.string.date_of_birth to personDetail.dateOfBirth.value.orEmpty())
            details.add(
                R.string.address to ModelUtil.getFullAddressFromPersonDetail(personDetail).orEmpty()
            )
            details.add(R.string.phone_number to personDetail.mobilePhone.value.orEmpty())
            details.add(R.string.home_phone to personDetail.homePhone.value.orEmpty())
        }

        details.add(R.string.insurance_type to ins.insuranceType.value.orEmpty())
        details.add(R.string.plan_or_program_name to ins.planOrProgramName.value.orEmpty())
        details.add(R.string.payer_name to ins.selectedPayer.value?.payerName.orEmpty())
        details.add(R.string.insurance_id to ins.insuranceID.value.orEmpty())
        details.add(R.string.copay to ins.copay.value.orEmpty())
        details.add(R.string.deductible to ins.deductible.value.orEmpty())
        details.add(R.string.coinsurance to ins.coinsurance.value.orEmpty())
        details.add(R.string.policy_group_or_feca to ins.policyGroupOrFECA.value.orEmpty())
        details.add(R.string.employer_or_school_name to ins.employerOrSchoolName.value.orEmpty())
        details.add(R.string.valid_from to ins.validFrom.value.orEmpty())
        details.add(R.string.valid_until to ins.validUntil.value.orEmpty())
        details.add(R.string.comment_str to ins.comment.value.orEmpty())
        details.add(R.string.relationship to ins.insuredPersonDetail?.relationship?.value?.value.orEmpty())
    }
    return details
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RenderProfileDetailItem(
    heading: String,
    detailList: List<Pair<String, String>> = emptyList(),
    showEdit: Boolean,
    @DrawableRes iconResId: Int,
    onClickEdit: (() -> Unit)
) {

    //TODO:Temp flag
    val showIcon = true


    MdCardWithoutTonalElevation(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MdTheme.dimension.contentCardPadding),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = MdTheme.dimension.contentCardPadding)
                .fillMaxWidth()
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (showIcon) {
                    Icon(
                        painter = painterResource(iconResId),
                        contentDescription = "Edit",
                        tint = MdColors.Black86
                    )

                    MdHorizontalSpace()
                }

                Text(
                    modifier = Modifier.padding(vertical = MdTheme.dimension.contentCardPadding),
                    text = heading,
                    style = MdTheme.typography.semiBold18TextStyle
                )
                Spacer(modifier = Modifier.weight(1f))

                if (showEdit) {
                    TextButton(
                        modifier = Modifier.defaultMinSize(minWidth = 80.dp),
                        onClick = {
                            onClickEdit()
                        },
                        colors = ButtonDefaults.textButtonColors(containerColor = MdColors.Transparent)
                    ) {
                        Text(
                            modifier = Modifier,
                            text = stringResource(R.string.edit_str),
                            style = MdTheme.typography.semiBold16TextStyle.copy(color = MdColors.BrandPink)
                        )
                    }
                }
            }

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MdTheme.dimension.contentCardPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(MdTheme.dimension.contentCardPadding)
            ) {
                detailList.chunked(2).forEach { listOfPair ->
                    if (listOfPair.firstOrNull()?.second?.isNotBlank() == true || listOfPair.lastOrNull()?.second?.isNotBlank() == true) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(32.dp),
                        ) {
                            listOfPair.forEach { (label, value) ->
                                if (value.isNotBlank()) {
                                    ProfileDetailItem(label, value, 1f)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun RowScope.ProfileDetailItem(label: String, value: String, weight: Float) {
    Column(modifier = Modifier.weight(weight), verticalArrangement = Arrangement.Center) {
        Text(label, style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.DeepGrey))
        Spacer(modifier = Modifier.size(2.dp))
        Text(value, style = MdTheme.typography.medium16TextStyle)
        Spacer(modifier = Modifier.size(4.dp))
    }
}

@Composable
@Preview(showBackground = true, widthDp = 600, heightDp = 1000)
fun previewPatientDetailCard() {
    MdTheme {

        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .background(MdColors.MdSurfaceColor)
        ) {

            MdVerticalSpace(2f)

            RenderProfileDetailItem(
                "Contact", listOf(
                    "Address" to "123, Test Street, Test City, Test State, Test Country, 123456",
                    "Primary Phone" to "+919876543210",
                    "Secondary Phone" to "+919876543210",
                    "Email" to "thisistest@gmail.com",
                ),
                showEdit = true,
                iconResId = R.drawable.ic_care, {

                }
            )

            RenderProfileDetailItem(
                "Contact", listOf(
                    "Address" to "123, Test Street, Test City, Test State, Test Country, 123456",
                    "Primary Phone" to "+919876543210",
                    "Secondary Phone" to "+919876543210",
                    "Email" to "thisistest@gmail.com",
                ),
                showEdit = true,
                iconResId = R.drawable.ic_group, {}
            )

            ProfileSection(
                heading = stringResource(R.string.primary_insurance),
                details = getInsuranceDetails(dummyTestPatient.primaryInsurance),
                iconResId = R.drawable.ic_insurance,
                showEdit = true,
                onClickEdit = {}
            )
            ProfileSection(
                heading = stringResource(R.string.secondary_insurance),
                details = getInsuranceDetails(dummyTestPatient.secondaryInsurance),
                iconResId = R.drawable.ic_insurance,
                showEdit = true,
                onClickEdit = {}
            )

        }
    }
}

val dummyTestPatient = Patient(
    fullName = "John Doe",
    patientRecordId = "P123456",
    dateOfBirth = DateTime("1985-05-15"),
    pronouns = "He/Him",
    gender = "Male",
    contactDetails = ContactDetails(
        mobilePhone = "+1234567890",
        homePhone = "+0987654321",
        workPhone = "+1122334455",
        workPhoneExtn = "123",
        emailId = "john.doe@example.com",
        primaryPhone = "+1234567890",
        preferredCommunication = "Email"
    ),
    postalAddress = PostalAddress(
        line1 = "123 Main Street",
        line2 = "Apt 4B",
        city = "New York",
        area = "Manhattan",
        district = "New York County",
        state = "New York",
        country = "USA",
        postalCode = "10001"
    ),
    genderIdentity = "Male",
    language = "English",
    race = "Caucasian",
    ethnicity = "Non-Hispanic",
    maritalStatus = "Married",
    employmentStatus = "Employed",
    smokingStatus = "Non-Smoker",
    isPhotoAvailableOnline = true,
    practicePatientId = "Practice123",
    primaryInsurance = Insurance(
        insuranceType = FieldState("Private"),
        planOrProgramName = FieldState("Gold Plan"),
        selectedPayer = FieldState(
            PracticePayer(
                payerId = "Payer123",
                payerName = "Best Insurance Co."
            )
        ),
        insuranceID = FieldState("INS123456"),
        copay = FieldState("$20"),
        deductible = FieldState("$500"),
        coinsurance = FieldState("10%"),
        policyGroupOrFECA = FieldState("Group123"),
        employerOrSchoolName = FieldState("Tech Corp"),
        validFrom = FieldState("2023-01-01"),
        insuredPersonDetail = PersonDetail(
            firstName = FieldState("John"),
            lastName = FieldState("Doe"),
            dateOfBirth = FieldState("1985-05-15"),
            mobilePhone = FieldState("+1234567890"),
            homePhone = FieldState("+0987654321"),
            addressLine1 = FieldState("123 Main Street, Apt 4B"),
            addressLine2 = FieldState("New York, NY, USA, 10001"),
        ),
    ),
    secondaryInsurance = Insurance(
        insuranceType = FieldState("Medicare"),
        planOrProgramName = FieldState("Silver Plan"),
        selectedPayer = FieldState(
            PracticePayer(
                payerId = "Payer456",
                payerName = "Secondary Insurance Co."
            )
        ),
        insuranceID = FieldState("INS789012"),
        copay = FieldState("$10"),
        deductible = FieldState("$250"),
        coinsurance = FieldState("20%"),
        policyGroupOrFECA = FieldState("Group456"),
        employerOrSchoolName = FieldState("Government"),
        validFrom = FieldState("2023-06-01"),
        insuredPersonDetail = PersonDetail(
            firstName = FieldState("Jane"),
            lastName = FieldState("Doe"),
            dateOfBirth = FieldState("1987-07-20"),
            mobilePhone = FieldState("+1234567891"),
            homePhone = FieldState("+0987654322"),
            addressLine1 = FieldState("456 Oak Avenue"),
            addressLine2 = FieldState("Brooklyn, NY, USA, 11201"),
        ),
    ),
    caregivers = listOf(
        Caregiver(
            firstName = "Jane",
            middleName = "A.",
            lastName = "Doe",
            relationship = "Spouse",
            dateOfBirth = "1987-07-20",
            gender = "Female",
            postalAddress = PostalAddress(
                line1 = "123 Main Street",
                line2 = "Apt 4B",
                city = "New York",
                area = "Manhattan",
                district = "New York County",
                state = "New York",
                country = "USA",
                postalCode = "10001"
            ),
            contactDetails = ContactDetails(
                mobilePhone = "+1234567891",
                homePhone = "+0987654322",

                )
        ),
        /* Caregiver(
             firstName = "Michael",
             middleName = "B.",
             lastName = "Smith",
             relationship = "Brother",
             dateOfBirth = "1990-03-15",
             gender = "Male",
             postalAddress = PostalAddress(
                 line1 = "456 Elm Street",
                 line2 = "Suite 12",
                 city = "Los Angeles",
                 area = "Downtown",
                 district = "Los Angeles County",
                 state = "California",
                 country = "USA",
                 postalCode = "90001"
             ),
             contactDetails = ContactDetails(
                 mobile = "+1234567892",
                 homePhone = "+0987654323"
             )
         )
 */
    )
)

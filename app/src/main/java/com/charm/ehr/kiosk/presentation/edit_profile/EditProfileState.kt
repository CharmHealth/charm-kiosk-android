package com.charm.ehr.kiosk.presentation.edit_profile

import UiText
import com.charm.ehr.kiosk.domain.Country
import com.charm.ehr.kiosk.domain.DemographicsMetrics
import com.charm.ehr.kiosk.domain.DemographicsSetting
import com.charm.ehr.kiosk.domain.DmCaregiverRelationship
import com.charm.ehr.kiosk.domain.DmEthnicity
import com.charm.ehr.kiosk.domain.DmGenderIdentity
import com.charm.ehr.kiosk.domain.DmLanguages
import com.charm.ehr.kiosk.domain.DmRace
import com.charm.ehr.kiosk.domain.PracticePayer
import com.charm.ehr.kiosk.domain.State
import com.charm.ehr.kiosk.presentation.patient_profile.Section

data class EditProfileState(

    val isLoading: Boolean = true,
    val errorMessage: UiText? = null,
    val toastMessage: Int? = null,
    val personalInformation: PersonalInformation = PersonalInformation(),
    val contactInformation: ContactInformation = ContactInformation(),
    val primaryCaregiverDetails: PersonDetail? = null,
    val secondaryCaregiverDetails: PersonDetail? = null,
    //
    val primaryInsurance: Insurance = Insurance(),
    val secondaryInsurance: Insurance = Insurance(),
    val selectedSection: Section = Section.PERSONAL_INFO,
    val demographicsMetrics: DemographicsMetrics? = null,
    val demographicsSetting: ArrayList<DemographicsSetting>? = null,
    val countryList: List<Country> = emptyList(),
    val countryStateMap: Map<String, List<State>> = emptyMap(),
    val isNextFlow: Boolean = true, //Else save flow
    val focusedPersonalField: PersonalField? = null,
    val focusedContactField: ContactField? = null,
    val focusedPCaregiverField: CaregiverField? = null,
    val focusedSCaregiverField: CaregiverField? = null,
    val focusedInsuranceField: InsuranceField? = null,
    val isShowPrevious: Boolean = false,
    val isShowSkip: Boolean = false,
    val isSaveFlow: Boolean = false,
    val isPersonalDetailChanged: Boolean = false,
    val isPrimaryInsuranceChanged: Boolean = false,
    val isSecondaryInsuranceChanged: Boolean = false,
    val payerNameOptions: List<PracticePayer> = emptyList(),
    val allowEditPatientDetails: Boolean =false,
    val isInsuranceRequired: Boolean =false,
    val isProfilePicUploading: Boolean = false,
    val isFromFinalSave: Boolean =false
)

sealed class CustomNavigationEvent {
    object Back : CustomNavigationEvent()
}


enum class CaregiverField {
    RELATIONSHIP,
    FIRST_NAME,
    LAST_NAME,
    DATE_OF_BIRTH,
    BIRTH_SEX,
    ADDRESS_LINE_1,
    ADDRESS_LINE_2,
    CITY,
    STATE,
    COUNTRY,
    POSTAL_CODE,
    MOBILE_PHONE,
    HOME_PHONE,

    IS_COPY_PATIENT_ADDRESS
}

enum class InsuranceField {
    INSURANCE_TYPE,
    PLAN_OR_PROGRAM_NAME,
    PLAN_ID,
    INSURANCE_ID,
    POLICY_GROUP_OR_FECA,
    PAYER_NAME,
    INSURANCE_CATEGORY,
    COPAY,
    DEDUCTIBLE,
    COINSURANCE,
    VALID_FROM,
    VALID_UNTIL,
    EMPLOYER_OR_SCHOOL_NAME,
    COMMENT,
    COPY_AS_PATIENT_ADDRESS,
    RELATIONSHIP,
    FIRST_NAME,
    LAST_NAME,
    DATE_OF_BIRTH
}


enum class ContactField {
    PREFERRED_COMMUNICATION,
    EMAIL,
    PRIMARY_PHONE,
    MOBILE_PHONE,
    HOME_PHONE,
    WORK_PHONE,
    WORK_PHONE_EXTN,
    ADDRESS_LINE_1,
    ADDRESS_LINE_2,
    CITY,
    STATE,
    COUNTRY,
    POSTAL_CODE,
    EMERGENCY_CONTACT_NAME,
    EMERGENCY_CONTACT_NUMBER,
    EMERGENCY_CONTACT_EXTN
}

enum class PersonalField {
    FIRST_NAME,
    LAST_NAME,
    DATE_OF_BIRTH,
    BIRTH_SEX,
    GENDER_IDENTITY,
    BLOOD_GROUP,
    RACE,
    ETHNICITY,
    LANGUAGE,
    SMOKING_STATUS,
    MARITAL_STATUS,
    EMPLOYMENT_STATUS
}

data class FieldState<T>(
    val value: T? = null,
    val error: UiText? = null,
    val isMandatory: Boolean = false
)

data class UploadState(
    val cacheFilePath: String? = null,
    val isServerUploaded: Boolean = true,
    val filePath: String? = null,
    val error: UiText? = null,
    val isMandatory: Boolean = false,
    val isUploading: Boolean = false
)

data class PersonalInformation(
    val firstName: FieldState<String> = FieldState(""),
    val lastName: FieldState<String> = FieldState(""),
    val dateOfBirth: FieldState<String?> = FieldState(null),
    val birthSex: FieldState<String> = FieldState(""),
    val genderIdentity: FieldState<DmGenderIdentity?> = FieldState(null),
    val bloodGroup: FieldState<String> = FieldState(""),
    val race: FieldState<List<DmRace>> = FieldState(emptyList()),
    val ethnicity: FieldState<List<DmEthnicity>> = FieldState(emptyList()),
    val language: FieldState<DmLanguages?> = FieldState(null),
    val smokingStatus: FieldState<String> = FieldState(""),
    val maritalStatus: FieldState<String> = FieldState(""),
    val employmentStatus: FieldState<String> = FieldState(""),
    val profileImageFilePath:UploadState=UploadState()
)

data class ContactInformation(
    val preferredCommunication: FieldState<String> = FieldState(""),
    val email: FieldState<String> = FieldState(""),
    val primaryPhone: FieldState<String> = FieldState(""),
    val mobilePhone: FieldState<String> = FieldState(""),
    val homePhone: FieldState<String> = FieldState(""),
    val workPhone: FieldState<String> = FieldState(""),
    val workPhoneExtn: FieldState<String> = FieldState(""),
    val addressLine1: FieldState<String> = FieldState(""),
    val addressLine2: FieldState<String> = FieldState(""),
    val city: FieldState<String> = FieldState(""),
    val state: FieldState<State?> = FieldState(null),
    val country: FieldState<Country?> = FieldState(null),
    val postalCode: FieldState<String> = FieldState(""),
    val emergencyContactName: FieldState<String> = FieldState(""),
    val emergencyContactNumber: FieldState<String> = FieldState(""),
    val emergencyContactExtn: FieldState<String> = FieldState(""),
    val stateList: List<State> = emptyList()
)


data class PersonDetail(
    val relationship: FieldState<DmCaregiverRelationship?> = FieldState(null),
    val firstName: FieldState<String> = FieldState(""),
    val lastName: FieldState<String> = FieldState(""),
    val dateOfBirth: FieldState<String?> = FieldState(null),
    val birthSex: FieldState<String> = FieldState(""),
    val isCopyPatientAddress: Boolean = false,
    val addressLine1: FieldState<String> = FieldState(""),
    val addressLine2: FieldState<String> = FieldState(""),
    val city: FieldState<String> = FieldState(""),
    val state: FieldState<State?> = FieldState(null),
    val country: FieldState<Country?> = FieldState(null),
    val postalCode: FieldState<String> = FieldState(""),
    val mobilePhone: FieldState<String> = FieldState(""),
    val homePhone: FieldState<String> = FieldState("")
)

data class Insurance(
    val insuranceType: FieldState<String> = FieldState(""),
    val insuranceTypeOptions: List<String> = emptyList(),
    val planOrProgramName: FieldState<String> = FieldState(""),
    val planId: FieldState<String> = FieldState(""),
    val insuranceID: FieldState<String> = FieldState(""),
    val policyGroupOrFECA: FieldState<String> = FieldState(""),
    val selectedPayer: FieldState<PracticePayer?> = FieldState(null),
    val payerNameOptions: List<PracticePayer> = emptyList(),
    val insuranceCategory: FieldState<String> = FieldState(""),
    val copay: FieldState<String> = FieldState(""),
    val deductible: FieldState<String> = FieldState(""),
    val coinsurance: FieldState<String> = FieldState(""),
    val validFrom: FieldState<String> = FieldState(""),
    val validUntil: FieldState<String> = FieldState(""),
    val employerOrSchoolName: FieldState<String> = FieldState(""),
    val comment: FieldState<String> = FieldState(""),
    val fronSide: FieldState<String> = FieldState(""),
    val backSide: FieldState<String> = FieldState(""),
    val copyAsPatientAddress: Boolean = false,
    val insuredPersonDetail: PersonDetail? = PersonDetail(),
    val insuranceCardFront: UploadState= UploadState(),
    val insuranceCardBack: UploadState= UploadState(),
    val patientInsuranceId: String? = ""
)

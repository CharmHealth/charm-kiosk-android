package com.charm.ehr.kiosk.presentation.patient_profile

import UiText
import com.charm.ehr.kiosk.domain.ConsentForm

data class PatientProfileState(
    val patientSharedConsentForms: List<ConsentForm> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val initialCallErrorMessage: UiText? = null,
    val initCallsPerformed: Boolean = false,
    val isLogOutPatient: Boolean = false,
    val allowEditPatientDetails: Boolean = false,
    val isInsuranceRequired: Boolean = false,
    val isQuestionnaireRequired: Boolean = false,
    val isConsentFormRequired: Boolean = false,
    val missingSections: Set<Section> = emptySet()

)


enum class Section { PERSONAL_INFO, CONTACT_INFO, CAREGIVER_DETAILS, PRIMARY_INSURANCE, SECONDARY_INSURANCE,EMERGENCY_CONTACT }
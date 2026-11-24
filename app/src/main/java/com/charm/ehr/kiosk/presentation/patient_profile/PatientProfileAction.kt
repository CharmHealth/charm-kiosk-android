package com.charm.ehr.kiosk.presentation.patient_profile

sealed interface PatientProfileAction {
    data object onAddQuestionnaire : PatientProfileAction
    data object onAddConsentForm : PatientProfileAction
    data object onHandOver : PatientProfileAction
    data class onEditPatient(val patientId: String,val selectedSection: Section?) : PatientProfileAction
    data object onClickFill : PatientProfileAction
    object onBackPress : PatientProfileAction
    data object onFinishClick : PatientProfileAction
    data class onAddMissingSection(val section: Section) : PatientProfileAction
    data class onRemoveMissingSection(val section: Section) : PatientProfileAction
}

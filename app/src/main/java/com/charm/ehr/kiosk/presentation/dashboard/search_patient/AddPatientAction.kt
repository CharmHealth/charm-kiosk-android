package com.charm.ehr.kiosk.presentation.dashboard.search_patient

sealed interface AddPatientAction {
    data class OnFirstNameChange(val firstName: String) : AddPatientAction
    data class OnLastNameChange(val lastName: String) : AddPatientAction
    data class OnDobChange(val dob: String) : AddPatientAction
    data class OnGenderChange(val gender: String) : AddPatientAction
    data object OnClickAddPatient : AddPatientAction
    data object OnClickProfilePicture : AddPatientAction
    data object OnClearState : AddPatientAction
    data object OnClearErrorMessage : AddPatientAction
    data object ClearFocusedField : AddPatientAction
    // Duplicate patient actions
    data object OnDismissDuplicateDialog : AddPatientAction
    data object OnAddAsDuplicate : AddPatientAction
    data class OnViewExistingPatient(val patientId: String) : AddPatientAction
}

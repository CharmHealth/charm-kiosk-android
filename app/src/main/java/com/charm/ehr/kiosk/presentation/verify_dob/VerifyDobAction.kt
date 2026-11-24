package com.charm.ehr.kiosk.presentation.verify_dob

sealed interface VerifyDobAction {
    data object OnVerify : VerifyDobAction
    object OnBackPress : VerifyDobAction
    data class OnDateChange(val dob: String) : VerifyDobAction
}
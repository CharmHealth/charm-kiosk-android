package com.charm.ehr.kiosk.presentation.verify_dob

import com.charm.ehr.kiosk.domain.Patient

data class VerifyDobState(
    val patient: Patient? = null,
    val dob: String = "",
    val isAgeVerified: Boolean = false,
    val verifyDobError: VerifyDobError? = null,
    val attemptsLeft: Int = 3,
)

enum class VerifyDobError{
    DOB_NOT_COMPLETE,
    INCORRECT_DOB
}
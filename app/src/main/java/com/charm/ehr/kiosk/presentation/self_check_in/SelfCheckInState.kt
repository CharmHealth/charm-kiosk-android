package com.charm.ehr.kiosk.presentation.self_check_in

import UiText
import com.charm.ehr.kiosk.domain.Country
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.common.util.ModelUtil

data class SelfCheckInState(
    val mobileNumber: String = "",
    val mobileCountryCode: String = "",
    val otp: String = "",
    val isOtpSent: Boolean = false,
    val isOtpVerified: Boolean = false,
    val facilityLogoUrl: String = "",
    val isLoading: Boolean = false,
    val attemptsLeft: Int = ModelUtil.IncorrectOTPAttempts,
    val facilityName: String = "",
    val apiErrorMessage: UiText? = null,
    val validationErrorMessage: UiText? = null,
    val countryCode: String = "",
    val requestId: String = "",
    val canResendOtp: Boolean = false,
    val resendOtpTimeLeft: Int = 0,
    val resendOtpAttemptLeft: Int = ModelUtil.OTPResendAttempts,
    val patientList: List<Patient> = emptyList(),
    val noPatientAvailableForMobileNumber: Boolean = false,
    val countryList:List<Country> = emptyList()
)
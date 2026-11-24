package com.charm.ehr.kiosk.presentation.self_check_in

sealed interface SelfCheckInAction {
    data object OnVerifyOTP : SelfCheckInAction
    data class OnUpdateMobileNumber(val mobileNumber: String) : SelfCheckInAction
    data class OnUpdateOTPNumber(val otp: String) : SelfCheckInAction
    data object OnSendOTP : SelfCheckInAction
    data object OnResendOTP : SelfCheckInAction
    data object OnClearMobileNumber: SelfCheckInAction
    data object OnClearApiErrorMessage: SelfCheckInAction
    data object OnClearValidationErrorMessage: SelfCheckInAction

    data object OnSettingsClicked : SelfCheckInAction
    data class OnCountryCodeSelected(val code: String) : SelfCheckInAction

    data object ResetOtpSentState : SelfCheckInAction

}
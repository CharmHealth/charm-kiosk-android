package com.charm.ehr.kiosk.presentation.settings

sealed interface SettingsAction {
    data object OnLogout : SettingsAction
    data object OnPrivacyPolicy : SettingsAction

    data object OnAppointmentStatus : SettingsAction
    data object OnTermsOfService : SettingsAction
    data object OnSwitchFacility : SettingsAction
    data object OnContactSupport : SettingsAction

    data object OnUpdateData : SettingsAction
    data object OnSelectGender : SettingsAction

    data object  ClearToastMessage : SettingsAction
    data object  ShowLogoutConfirmDialog : SettingsAction
    data object  DismissLogoutConfirmDialog : SettingsAction
    data object  onAppLockChange : SettingsAction
    data class  onRefreshWorkCompleted(val success: Boolean, val error:String) : SettingsAction
}
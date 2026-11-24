package com.charm.ehr.kiosk.presentation.passcode

sealed interface PasscodeAction {
    data class OnUnlock(val userName: String, val password: String) : PasscodeAction
    data object OnLogOut : PasscodeAction
    data object OnShowPassword : PasscodeAction
    data class OnChangePassword(val password: String) : PasscodeAction

    data object OnBiometricAuthSuccess : PasscodeAction
    data object OnBiometricAuthFailed : PasscodeAction
}
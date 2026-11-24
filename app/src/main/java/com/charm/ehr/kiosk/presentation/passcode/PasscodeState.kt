package com.charm.ehr.kiosk.presentation.passcode

import UiText

data class PasscodeState(
    val loginName: String = "",
    val facilityName: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val isVerified: Boolean = false,
    val showPassword: Boolean = false,
    val isFromIdleState: Boolean = false,
    val wrongPasswordAttempts :Int=0
)

enum class PasscodeVerificationFlow {
    IDLE_TIMEOUT,
    APP_LAUNCH,
    LOCK_TYPE_SELECTION, // Setting up lock type from settings creation
    LOCK_TYPE_CHANGE // Changing lock type from settings verification
}
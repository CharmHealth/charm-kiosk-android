package com.charm.ehr.kiosk.presentation.tele_health

sealed interface TelehealthAction {
    object OnClickCheckIn : TelehealthAction
}


package com.charm.ehr.kiosk.presentation.tele_health

data class TelehealthState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
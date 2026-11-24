package com.charm.ehr.kiosk.presentation.select_patient

import com.charm.ehr.kiosk.domain.Patient

data class SelectPatientState(
    val isLoading: Boolean = false,
    val mobileNumber: String = "",
    val facilityName: String = "",
    val facilityLogoUrl: String = "",
    val patientList: List<Patient> = emptyList(),
    val selectedPatient: Patient? = null,
    val errorMessage: String? = null
)

package com.charm.ehr.kiosk.presentation.select_patient

import com.charm.ehr.kiosk.domain.Patient

sealed interface SelectPatientAction {
    data class OnSelectPatient(val patient: Patient) : SelectPatientAction
    object OnClickDone : SelectPatientAction

    object OnExitClicked : SelectPatientAction
}
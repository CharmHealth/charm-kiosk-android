package com.charm.ehr.kiosk.domain.result

import com.charm.ehr.kiosk.domain.Patient

sealed class AddPatientResult {
    data class Added(val patientId: String) : AddPatientResult()
    data class DuplicateFound(val patient: Patient) : AddPatientResult()
}

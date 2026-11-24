package com.charm.ehr.kiosk.domain.usecase.patient

import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Persists updated patient details to the server.
 *
 * The caller is responsible for any subsequent steps on success — e.g. uploading
 * a profile image or refreshing the patient detail from the server.
 */
class SavePatientUseCase @Inject constructor(
    private val patientRepository: PatientRepository
) {
    suspend operator fun invoke(patient: Patient?): APIState<String> =
        withContext(Dispatchers.IO) {
            patientRepository.editPatient(patient)
        }
}

package com.charm.ehr.kiosk.domain.usecase.patient

import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.presentation.edit_profile.Insurance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Updates an existing insurance record for a patient.
 *
 * Returns a [Boolean] on success indicating whether the update was accepted
 * by the server. The caller handles the subsequent state update and cache refresh.
 */
class UpdatePatientInsurancesUseCase @Inject constructor(
    private val patientRepository: PatientRepository
) {
    suspend operator fun invoke(practicePatientId: String, insurance: Insurance): APIState<Any> =
        withContext(Dispatchers.IO) {
            patientRepository.updatePatientInsurances(practicePatientId, insurance)
        }
}

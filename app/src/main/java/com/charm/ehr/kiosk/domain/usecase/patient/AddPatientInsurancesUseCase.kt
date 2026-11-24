package com.charm.ehr.kiosk.domain.usecase.patient

import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.presentation.edit_profile.Insurance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Adds a new insurance record for a patient.
 *
 * Returns a [Pair] of (success: Boolean, insuranceId: String) on success.
 * The caller is responsible for any subsequent card upload steps using the
 * returned insurance ID.
 */
class AddPatientInsurancesUseCase @Inject constructor(
    private val patientRepository: PatientRepository
) {
    suspend operator fun invoke(practicePatientId: String, insurance: Insurance): APIState<Any> =
        withContext(Dispatchers.IO) {
            patientRepository.addPatientInsurances(practicePatientId, insurance)
        }
}

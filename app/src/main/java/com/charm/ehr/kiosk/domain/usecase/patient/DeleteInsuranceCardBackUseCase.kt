package com.charm.ehr.kiosk.domain.usecase.patient

import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Deletes the back image of a patient's insurance card from the server.
 */
class DeleteInsuranceCardBackUseCase @Inject constructor(
    private val patientRepository: PatientRepository
) {
    suspend operator fun invoke(
        patientId: String,
        insuranceId: String
    ): APIState<Any> =
        withContext(Dispatchers.IO) {
            patientRepository.deletePatientInsuranceCardBack(patientId, insuranceId)
        }
}

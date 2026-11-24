package com.charm.ehr.kiosk.domain.usecase.patient

import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Uploads the back image of a patient's insurance card.
 */
class PostInsuranceCardBackUseCase @Inject constructor(
    private val patientRepository: PatientRepository
) {
    suspend operator fun invoke(
        imageFile: String,
        patientId: String,
        insuranceId: String
    ): APIState<Boolean> =
        withContext(Dispatchers.IO) {
            patientRepository.postPatientInsuranceCardBack(imageFile, patientId, insuranceId)
        }
}

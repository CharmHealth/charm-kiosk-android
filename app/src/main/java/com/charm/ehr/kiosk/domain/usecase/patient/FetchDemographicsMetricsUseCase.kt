package com.charm.ehr.kiosk.domain.usecase.patient

import com.charm.ehr.kiosk.domain.DemographicsMetrics
import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Fetches the patient demographics metrics (gender options, etc.) from the server.
 *
 * Reusable across any screen that needs the demographics configuration — e.g. the
 * dashboard add-patient form or any future patient registration screen.
 */
class FetchDemographicsMetricsUseCase @Inject constructor(
    private val patientRepository: PatientRepository
) {
    suspend operator fun invoke(): APIState<DemographicsMetrics> =
        withContext(Dispatchers.IO) {
            patientRepository.getPatientDemographicsMetrics()
        }
}

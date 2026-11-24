package com.charm.ehr.kiosk.domain.usecase.patient

import com.charm.ehr.kiosk.domain.DemographicsSetting
import com.charm.ehr.kiosk.domain.repository.PatientDetailRepository
import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Fetches patient demographics settings and caches them in [PatientDetailRepository].
 *
 * Part of the reference-data initialisation sequence run in parallel by
 * PatientDetailViewmodel before the patient profile screen becomes interactive.
 * Downstream screens (EditProfile, etc.) read the settings from the cache rather
 * than re-fetching.
 */
class FetchDemographicsSettingsUseCase @Inject constructor(
    private val patientRepository: PatientRepository,
    private val patientDetailRepository: PatientDetailRepository
) {
    suspend operator fun invoke(): APIState<ArrayList<DemographicsSetting>> =
        withContext(Dispatchers.IO) {
            val result = patientRepository.getPatientDemographicsSettings()
            if (result is APIState.Success) {
                patientDetailRepository.setDemographicsSetting(result.data)
            }
            result
        }
}

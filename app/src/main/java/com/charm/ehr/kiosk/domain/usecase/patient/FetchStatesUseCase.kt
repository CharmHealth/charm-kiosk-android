package com.charm.ehr.kiosk.domain.usecase.patient

import com.charm.ehr.kiosk.domain.State
import com.charm.ehr.kiosk.domain.repository.PatientDetailRepository
import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Fetches the state list for a given country and caches it in [PatientDetailRepository].
 *
 * Called once per supported country (India, US, Australia) during the parallel
 * reference-data initialisation in PatientDetailViewmodel. The cached map is
 * used by the insurance and contact sections in EditProfile to resolve state
 * objects without extra network calls.
 *
 * @param countryKey The country short-form key (e.g. [ModelUtil.countryStringIndia]).
 */
class FetchStatesUseCase @Inject constructor(
    private val patientRepository: PatientRepository,
    private val patientDetailRepository: PatientDetailRepository
) {
    suspend operator fun invoke(countryKey: String): APIState<List<State>> =
        withContext(Dispatchers.IO) {
            val result = patientRepository.getStates(countryShortForm = countryKey)
            if (result is APIState.Success) {
                patientDetailRepository.setStateToCountryMap(countryKey, result.data)
            }
            result
        }
}

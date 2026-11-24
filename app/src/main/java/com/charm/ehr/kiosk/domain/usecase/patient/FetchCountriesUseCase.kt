package com.charm.ehr.kiosk.domain.usecase.patient

import com.charm.ehr.kiosk.domain.Country
import com.charm.ehr.kiosk.domain.repository.PatientDetailRepository
import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Fetches the country list and caches it in [PatientDetailRepository].
 *
 * Part of the reference-data initialisation sequence run in parallel by
 * PatientDetailViewmodel. The cached list is used by insurance and contact
 * sections in EditProfile to resolve country objects without extra network calls.
 */
class FetchCountriesUseCase @Inject constructor(
    private val patientRepository: PatientRepository,
    private val patientDetailRepository: PatientDetailRepository
) {
    suspend operator fun invoke(): APIState<ArrayList<Country>> =
        withContext(Dispatchers.IO) {
            val result = patientRepository.getCountries()
            if (result is APIState.Success) {
                patientDetailRepository.setCountryList(result.data)
            }
            result
        }
}

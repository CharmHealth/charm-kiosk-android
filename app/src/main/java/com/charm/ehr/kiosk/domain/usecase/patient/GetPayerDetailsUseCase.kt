package com.charm.ehr.kiosk.domain.usecase.patient

import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Searches for practice payers by an optional query string.
 *
 * Used by the insurance section of EditProfile to populate the payer name
 * dropdown for primary and secondary insurances.
 */
class GetPayerDetailsUseCase @Inject constructor(
    private val patientRepository: PatientRepository
) {
    suspend operator fun invoke(query: String?): APIState<Any> =
        withContext(Dispatchers.IO) {
            patientRepository.getPayerDetails(query)
        }
}

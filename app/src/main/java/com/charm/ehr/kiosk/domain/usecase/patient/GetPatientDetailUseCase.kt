package com.charm.ehr.kiosk.domain.usecase.patient

import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.domain.repository.PatientDetailRepository
import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Fetches a patient's full detail record and caches it in [PatientDetailRepository].
 *
 * Reusable across any screen that needs to load a patient — currently used by
 * PatientDetailViewmodel and EditProfileViewModel, which previously duplicated this logic.
 *
 * On success the patient is stored via [PatientDetailRepository.setPatient] so that
 * downstream screens (EditProfile, insurance, etc.) read from the in-memory cache
 * rather than re-fetching.
 */
class GetPatientDetailUseCase @Inject constructor(
    private val patientRepository: PatientRepository,
    private val patientDetailRepository: PatientDetailRepository
) {
    suspend operator fun invoke(patientId: String): APIState<Patient> =
        withContext(Dispatchers.IO) {
            val result = patientRepository.getPatientDetail(patientId)
            if (result is APIState.Success) {
                patientDetailRepository.setPatient(result.data)
            }
            result
        }
}

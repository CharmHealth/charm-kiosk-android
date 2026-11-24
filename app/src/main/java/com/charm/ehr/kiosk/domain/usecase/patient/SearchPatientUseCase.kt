package com.charm.ehr.kiosk.domain.usecase.patient

import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Searches for patients within the currently selected facility.
 *
 * Reusable across any screen that needs patient search — dashboard, tele-health entry,
 * or any future patient lookup screen.
 *
 * The caller is responsible for passing correctly mapped nullable parameters:
 * - Pass [name] and/or [mobile] for name/phone search
 * - Pass [recordId] for record ID lookup
 * - Pass [email] for email search
 * - Pass [dob] (already formatted for the API) for date-of-birth search
 *
 * Business rule: the facility ID is resolved internally from [AuthManager] so callers
 * do not need to know which facility is currently active.
 */
class SearchPatientUseCase @Inject constructor(
    private val patientRepository: PatientRepository,
    private val authManager: AuthManager
) {
    suspend operator fun invoke(
        name: String?,
        mobile: String?,
        recordId: String?,
        email: String?,
        dob: String?
    ): APIState<ArrayList<Patient>> =
        withContext(Dispatchers.IO) {
            patientRepository.searchPatient(
                facilityId = authManager.selectedFacilityId ?: "",
                name = name,
                mobile = mobile,
                recordId = recordId,
                email = email,
                dob = dob
            )
        }
}

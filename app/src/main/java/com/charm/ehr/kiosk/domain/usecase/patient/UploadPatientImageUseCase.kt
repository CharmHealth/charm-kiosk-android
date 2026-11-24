package com.charm.ehr.kiosk.domain.usecase.patient

import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Uploads a patient profile image to the server.
 *
 * The caller is responsible for updating the local patient cache and
 * refreshing patient detail after a successful upload.
 */
class UploadPatientImageUseCase @Inject constructor(
    private val patientRepository: PatientRepository
) {
    suspend operator fun invoke(
        imageFilePath: String,
        practicePatientId: String
    ): APIState<JsonObject> =
        withContext(Dispatchers.IO) {
            patientRepository.addPatientImage(imageFilePath, practicePatientId)
        }
}

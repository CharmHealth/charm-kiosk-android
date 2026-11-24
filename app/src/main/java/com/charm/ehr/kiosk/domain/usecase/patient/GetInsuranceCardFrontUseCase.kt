package com.charm.ehr.kiosk.domain.usecase.patient

import android.graphics.Bitmap
import com.charm.ehr.kiosk.domain.repository.FileRepository
import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Fetches the front image of a patient's insurance card and converts the
 * response body into a local [File].
 *
 * Returns [APIState.Success] with the local [File] on success, allowing the
 * caller to use [File.absolutePath] directly without needing [FileRepository].
 */
class GetInsuranceCardFrontUseCase @Inject constructor(
    private val patientRepository: PatientRepository,
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(
        patientId: String,
        insuranceId: String
    ): APIState<File?> =
        withContext(Dispatchers.IO) {
            val result = patientRepository.getPatientInsuranceCardFront(patientId, insuranceId)
            when (result) {
                is APIState.Success -> APIState.Success(
                    fileRepository.getResponseBodyToFile(result.data, Bitmap.CompressFormat.PNG)
                )
                is APIState.Error -> APIState.Error(result.apiResponseError)
                else -> APIState.Success(null)
            }
        }
}

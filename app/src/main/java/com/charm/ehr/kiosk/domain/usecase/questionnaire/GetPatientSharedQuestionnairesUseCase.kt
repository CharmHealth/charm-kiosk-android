package com.charm.ehr.kiosk.domain.usecase.questionnaire

import com.charm.ehr.kiosk.domain.QuestionnairePhysicianTemplate
import com.charm.ehr.kiosk.domain.repository.PatientDetailRepository
import com.charm.ehr.kiosk.domain.repository.QuestionnaireRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Fetches the questionnaires shared with a specific patient and caches them
 * in [PatientDetailRepository].
 *
 * Reusable across any screen that needs the patient's shared questionnaire list.
 * The cached list is exposed via [PatientDetailRepository.patientSharedQuestionnaires]
 * so observers react automatically on success.
 */
class GetPatientSharedQuestionnairesUseCase @Inject constructor(
    private val questionnaireRepository: QuestionnaireRepository,
    private val patientDetailRepository: PatientDetailRepository
) {
    suspend operator fun invoke(patientId: String): APIState<List<QuestionnairePhysicianTemplate>> =
        withContext(Dispatchers.IO) {
            val result = questionnaireRepository.getPatientsSharedQuestionnaires(patientId)
            if (result is APIState.Success) {
                patientDetailRepository.setPatientSharedQuestionnaires(result.data)
            }
            result
        }
}

package com.charm.ehr.kiosk.domain.usecase.questionnaire

import com.charm.ehr.kiosk.domain.ConsentForm
import com.charm.ehr.kiosk.domain.repository.QuestionnaireRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Fetches the consent forms shared with a specific patient.
 *
 * Reusable across any screen that needs the patient's shared consent form list.
 * The result is returned to the caller for state management; unlike questionnaires,
 * consent forms are held in ViewModel state rather than a repository cache.
 */
class GetPatientSharedConsentFormsUseCase @Inject constructor(
    private val questionnaireRepository: QuestionnaireRepository
) {
    suspend operator fun invoke(patientId: String): APIState<List<ConsentForm>> =
        withContext(Dispatchers.IO) {
            questionnaireRepository.getPatientsSharedConsentForms(patientId)
        }
}

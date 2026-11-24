package com.charm.ehr.kiosk.domain.usecase.questionnaire

import com.charm.ehr.kiosk.domain.ConsentForm
import com.charm.ehr.kiosk.domain.repository.QuestionnaireRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Fetches all consent form templates available for the practice.
 *
 * The result is returned to the caller for local storage (e.g. [allConsentFormList])
 * and used as the full search pool when the clinician assigns consent forms to a patient.
 */
class GetPracticeConsentFormsUseCase @Inject constructor(
    private val questionnaireRepository: QuestionnaireRepository
) {
    suspend operator fun invoke(): APIState<List<ConsentForm>> =
        withContext(Dispatchers.IO) {
            questionnaireRepository.getPracticeConsentForms()
        }
}

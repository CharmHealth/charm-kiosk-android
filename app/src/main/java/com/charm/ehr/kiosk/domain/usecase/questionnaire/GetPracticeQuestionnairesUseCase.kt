package com.charm.ehr.kiosk.domain.usecase.questionnaire

import com.charm.ehr.kiosk.domain.QuestionnairePhysicianTemplate
import com.charm.ehr.kiosk.domain.repository.QuestionnaireRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Fetches all questionnaire templates available for the practice.
 *
 * The result is returned to the caller for local storage (e.g. [allQuestionnaireList])
 * and used as the full search pool when the clinician assigns questionnaires to a patient.
 */
class GetPracticeQuestionnairesUseCase @Inject constructor(
    private val questionnaireRepository: QuestionnaireRepository
) {
    suspend operator fun invoke(): APIState<List<QuestionnairePhysicianTemplate>> =
        withContext(Dispatchers.IO) {
            questionnaireRepository.getPracticeQuestionnaires()
        }
}

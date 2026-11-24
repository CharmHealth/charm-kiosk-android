package com.charm.ehr.kiosk.domain.usecase.questionnaire

import com.charm.ehr.kiosk.domain.QuestionnairePhysicianTemplate
import com.charm.ehr.kiosk.domain.repository.QuestionnaireRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Updates the set of questionnaires shared with a patient by adding and removing
 * entries in a single operation.
 *
 * The caller is responsible for refreshing the cached questionnaire list after
 * a successful update (e.g. via [GetPatientSharedQuestionnairesUseCase]).
 */
class UpdatePatientSharedQuestionnaireUseCase @Inject constructor(
    private val questionnaireRepository: QuestionnaireRepository
) {
    suspend operator fun invoke(
        patientId: String,
        quesToAdd: List<QuestionnairePhysicianTemplate>,
        quesToRemove: List<QuestionnairePhysicianTemplate>
    ): APIState<Boolean> =
        withContext(Dispatchers.IO) {
            questionnaireRepository.updatePatientSharedQuestionnaire(
                patientId = patientId,
                quesToAdd = quesToAdd,
                quesToRemove = quesToRemove
            )
        }
}

package com.charm.ehr.kiosk.domain.usecase.questionnaire

import javax.inject.Inject

data class QuestionnaireUseCases @Inject constructor(
    val getPatientSharedQuestionnaires: GetPatientSharedQuestionnairesUseCase,
    val getPatientSharedConsentForms: GetPatientSharedConsentFormsUseCase,
    val getPracticeQuestionnaires: GetPracticeQuestionnairesUseCase,
    val getPracticeConsentForms: GetPracticeConsentFormsUseCase,
    val updatePatientSharedQuestionnaire: UpdatePatientSharedQuestionnaireUseCase
)

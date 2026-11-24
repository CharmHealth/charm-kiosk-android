package com.charm.ehr.kiosk.domain.repository

import com.charm.ehr.kiosk.domain.ConsentForm
import com.charm.ehr.kiosk.domain.QuestionnaireMetaData
import com.charm.ehr.kiosk.domain.QuestionnairePhysicianTemplate
import com.charm.ehr.kiosk.presentation.composable.APIState
import okhttp3.ResponseBody

interface QuestionnaireRepository {
    suspend fun getPatientsSharedQuestionnaires(patientId: String): APIState<List<QuestionnairePhysicianTemplate>>
    suspend fun getPatientsSharedConsentForms(patientId: String): APIState<List<ConsentForm>>
    suspend fun getPracticeQuestionnaires(): APIState<List<QuestionnairePhysicianTemplate>>
    suspend fun getPracticeConsentForms(): APIState<List<ConsentForm>>
    suspend fun updatePatientSharedQuestionnaire(
        patientId: String,
        quesToAdd: List<QuestionnairePhysicianTemplate>,
        quesToRemove: List<QuestionnairePhysicianTemplate>
    ): APIState<Boolean>
    suspend fun getQuestionnaireAnswer(quesMapId: String): APIState<QuestionnaireMetaData>
    suspend fun postQuestionnaireAnswers(
        questionnaireMetaData: QuestionnaireMetaData,
        patientId: String
    ): APIState<Boolean>
    suspend fun getSignForQuestionnaire(signatureUrl: String): APIState<ResponseBody>
    suspend fun postSignForQuestionnaire(
        imageFile: String,
        quesMapId: String,
        entryId: String
    ): APIState<Boolean>
}

package com.charm.ehr.kiosk.data.repository
import com.charm.ehr.kiosk.domain.repository.QuestionnaireRepository

import UiText
import android.content.Context
import com.charm.ehr.kiosk.domain.APIResponseError
import com.charm.ehr.kiosk.domain.ConsentForm
import com.charm.ehr.kiosk.common.util.MdJsonBuilder
import com.charm.ehr.kiosk.common.util.MdParsingHelper
import com.charm.ehr.kiosk.domain.QuestionnaireMetaData
import com.charm.ehr.kiosk.domain.QuestionnairePhysicianTemplate
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.data.network.WebService
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.common.util.CharmAPI
import com.charm.ehr.kiosk.common.util.FileUtil
import com.charm.ehr.kiosk.common.util.FileUtil.getFileFromFilePath
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import javax.inject.Inject

class QuestionnaireRepositoryImpl @Inject constructor(
    private val webService: WebService, private val authManager: AuthManager,
    @ApplicationContext   private val appContext: Context, private val charmAPI: CharmAPI
)  : QuestionnaireRepository{

    override suspend fun getPatientsSharedQuestionnaires(patientId: String): APIState<List<QuestionnairePhysicianTemplate>> {
        return charmAPI.executeApiCall(
            call = { webService.getPatientsSharedQuestionnaires(patientId = patientId) },
            onSuccess = {
                MdParsingHelper.parsePatientsSharedQuestionnaires(it)
            },
            ignoreCodeZero = true
        )
    }

    override suspend fun getPatientsSharedConsentForms(patientId: String): APIState<List<ConsentForm>> {
        return charmAPI.executeApiCall(
            call = { webService.getPatientsSharedConsentForms(patientId = patientId) },
            onSuccess = {
                MdParsingHelper.parsePracticeConsentForm(it)
            },
            ignoreCodeZero = true
        )
    }

    override suspend fun getPracticeQuestionnaires(): APIState<List<QuestionnairePhysicianTemplate>> {
        return charmAPI.executeApiCall(
            call = { webService.getPracticeQuestionnaires() },
            onSuccess = {
                MdParsingHelper.parsePracticeQuestionnaire(it)
            },
            ignoreCodeZero = true
        )
    }

    override suspend fun getPracticeConsentForms(): APIState<List<ConsentForm>> {
       return charmAPI.executeApiCall(
            call = {  webService.getPracticeConsentForms() },
            onSuccess = {
                MdParsingHelper.parsePracticeConsentForm(it)
            },
            ignoreCodeZero = true
        )
    }

    override suspend fun updatePatientSharedQuestionnaire(
        patientId: String,
        quesToAdd: List<QuestionnairePhysicianTemplate>,
        quesToRemove: List<QuestionnairePhysicianTemplate>
    ): APIState<Boolean> {

        val params = MdJsonBuilder.formUpdateQuestionnaireParams(
            patientId = patientId,
            quesToAdd = quesToAdd,
            quesToRemove = quesToRemove
        )
        return charmAPI.executeApiCall(
            call = { webService.updatePatientSharedQuestionnaire(params) },
            onSuccess = {
                MdParsingHelper.parseUpdateSharedQuestionnaire(it)
            },
            ignoreCodeZero = true

        )
    }

    override suspend fun getQuestionnaireAnswer(quesMapId: String): APIState<QuestionnaireMetaData> {
        return charmAPI.executeApiCall(
            call = {
                webService.getQuestionnaireAnswer(quesMapId)
            },
            onSuccess = {
                MdParsingHelper.parseQuestionnaireWithAnswers(it)
            },
            ignoreCodeZero = true
        )
    }

    override suspend fun postQuestionnaireAnswers(
        questionnaireMetaData: QuestionnaireMetaData,
        patientId: String
    ): APIState<Boolean> {
        val postObj = MdJsonBuilder.createQuestionnaireAnswers(
            questionnaireMetaData = questionnaireMetaData,
            patientId = patientId
        )
        return charmAPI.executeApiCall(
            call = { webService.postQuestionnaireAnswers(postObj) },
            onSuccess = {
                MdParsingHelper.parseUpdateSharedQuestionnaire(it)
            },
            ignoreCodeZero = true
        )
    }

    override suspend fun getSignForQuestionnaire(signatureUrl: String): APIState<ResponseBody> {
        return charmAPI.executeBinaryApiCall(
            call = { webService.getSignForQuestionnaire(signatureUrl) },
            onSuccess = { responseBody ->
                responseBody
            }
        )
    }

    override suspend fun postSignForQuestionnaire(
        imageFile: String,
        quesMapId: String,
        entryId: String
    ): APIState<Boolean> {
        val file = imageFile.getFileFromFilePath()
        val filePair = FileUtil.createRequestBodyForFile(file)
        val requestBody = filePair?.first

        if (requestBody != null) {
            val multiPartBody = MultipartBody.Part.createFormData("file", file?.name, requestBody)
            return charmAPI.executeApiCall(
                call = { webService.postSignForQuestionnaire(multiPartBody, quesMapId, entryId) },
                onSuccess = { true },
                ignoreCodeZero = true
            )
        } else {
            return APIState.Error(
                APIResponseError(
                    null, null,
                    errorMsg = UiText.StrResId(R.string.the_file_does_not_exist_or_is_not_accessible)
                )
            )
        }
    }
}
package com.charm.ehr.kiosk.presentation.questionnaire

import com.charm.ehr.kiosk.domain.Question
import com.charm.ehr.kiosk.domain.SOAPEntry


sealed interface QuestionnaireDetailAction{
    object ClearToastMessage : QuestionnaireDetailAction
    object ClearError : QuestionnaireDetailAction
    object RetryLastFailedAction : QuestionnaireDetailAction

    //Back Press Action
    object OnBackPress : QuestionnaireDetailAction
    data class  AnswerChanged( val changedQuestion: Question,
                               val answer: String,
                               val soapEntry: SOAPEntry?,
                               val childQuestion: Question? = null): QuestionnaireDetailAction

    data class FetchSignature(val signatureUrl: String, val entryId: String?): QuestionnaireDetailAction
    data class UpdateFocusEntryId(
        val entryId: String?,
        val parentIndex: Int,
        val childFocusIndex: Int,
        val isFromSection: Boolean
    ): QuestionnaireDetailAction

    data class PostQuestionnaire(val isFromNext: Boolean): QuestionnaireDetailAction
}

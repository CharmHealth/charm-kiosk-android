package com.charm.ehr.kiosk.presentation.questionnaire


import UiText
import com.charm.ehr.kiosk.domain.QuestionnaireMetaData

data class QuestionnaireDetailScreenState(
    val questionnaireMetaData: QuestionnaireMetaData? = null,
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val focusEntryId: String? = null,
    val focusIndex: Int = -1,
    val childFocusIndex: Int = -1,
    val isSuccess: Boolean = false,
    val selectedQuestionnaireId: String? = null,
    val selectedQuestionnaireIndex: Int? = null,
    val toastMessage: Int? = null,
    val isSingleQuestionnaire : Boolean = false,
    val isFirstQuestionnaire : Boolean = false,
    val isLastQuestionnaire : Boolean = false,
    val isQuestionnaireValueUpdated : Boolean = false
)

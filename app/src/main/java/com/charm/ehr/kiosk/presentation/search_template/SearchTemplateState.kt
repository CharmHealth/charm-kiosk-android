package com.charm.ehr.kiosk.presentation.search_template

import com.charm.ehr.kiosk.domain.ConsentForm
import com.charm.ehr.kiosk.domain.QuestionnairePhysicianTemplate

data class SearchTemplateState(
    val patientId: String = "",
    val searchQuery: String = "",
    val searchResults: List<Any> = emptyList(),
    val selectedQuestionnaireSet: Set<QuestionnairePhysicianTemplate> = emptySet(),
    val selectedConsentFormSet: Set<ConsentForm> = emptySet(),
    val templateType: SearchTemplateType = SearchTemplateType.Questionnaire,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isChangesMade: Boolean=false
)

package com.charm.ehr.kiosk.presentation.search_template

import com.charm.ehr.kiosk.domain.ConsentForm
import com.charm.ehr.kiosk.domain.QuestionnairePhysicianTemplate

sealed interface SearchTemplateAction {
    data class OnSearchQueryChange(val query: String) : SearchTemplateAction
    data class OnChangeTemplateType(val formType: SearchTemplateType, val query: String) : SearchTemplateAction
    data class OnSelectQuestionnaire(val questionnaire: QuestionnairePhysicianTemplate) : SearchTemplateAction
    data class OnRemoveQuestionnaire(val questionnaire: QuestionnairePhysicianTemplate) : SearchTemplateAction
    data class OnSelectConsentTemplate(val consentForm: ConsentForm) : SearchTemplateAction
    data class OnRemoveConsentTemplate(val consentForm: ConsentForm) : SearchTemplateAction
    data object OnSubmit : SearchTemplateAction
    data object OnBackPress : SearchTemplateAction
    data object OnSuccess : SearchTemplateAction
    data object DismissDiscardDialog : SearchTemplateAction
    data object showDiscardDialog : SearchTemplateAction
}
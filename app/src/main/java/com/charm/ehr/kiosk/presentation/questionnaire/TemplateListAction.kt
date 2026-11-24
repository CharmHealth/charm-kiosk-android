package com.charm.ehr.kiosk.presentation.questionnaire

sealed interface TemplateListAction {
    //Change id to Object when needed
    data class OnClickTemplate(val questionnaireId: String) : TemplateListAction
    data class OnClickConsentForm(val consentFormId: String) : TemplateListAction
    data object onBackPressed : TemplateListAction
    data object onFinishClick: TemplateListAction
}


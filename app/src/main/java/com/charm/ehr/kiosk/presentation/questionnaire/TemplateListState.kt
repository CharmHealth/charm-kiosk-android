package com.charm.ehr.kiosk.presentation.questionnaire

import com.charm.ehr.kiosk.domain.ConsentForm

data class TemplateListState(
    val consentFormList: List<ConsentForm> = emptyList(),
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
)


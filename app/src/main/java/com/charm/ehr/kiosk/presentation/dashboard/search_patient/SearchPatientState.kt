package com.charm.ehr.kiosk.presentation.dashboard.search_patient

import UiText
import com.charm.ehr.kiosk.domain.Patient

enum class DashboardSection {
    ADD, SEARCH, SETTINGS
}

data class SearchPatientState(
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val searchResultList: List<Patient> = emptyList(),
    val searchQuery: String = "",
    val selectedDashboardSection: DashboardSection = DashboardSection.ADD,
    val selectedSearchType: SearchType = SearchType.NAME_OR_MOBILE,
)
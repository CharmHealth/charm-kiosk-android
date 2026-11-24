package com.charm.ehr.kiosk.presentation.dashboard.search_patient

import com.charm.ehr.kiosk.domain.Patient

sealed interface SearchPatientAction {
    data object OnBackPress : SearchPatientAction
    data class OnChangeSearchQuery(val query: String) : SearchPatientAction
    data class OnUpdateSearchType(val selectedSearchType: SearchType) : SearchPatientAction
    data class OnUpdateSelectedItemIndex(val section: DashboardSection) : SearchPatientAction
    data class OnClickPatient(val patient: Patient) : SearchPatientAction
    data object OnAddPatient : SearchPatientAction
}





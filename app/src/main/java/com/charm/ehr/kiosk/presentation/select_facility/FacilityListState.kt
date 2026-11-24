package com.charm.ehr.kiosk.presentation.select_facility

import UiText
import com.charm.ehr.kiosk.domain.Facility

data class FacilityListState(
    val facilityList: List<Facility> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val isSingleFacility: Boolean = false,
)





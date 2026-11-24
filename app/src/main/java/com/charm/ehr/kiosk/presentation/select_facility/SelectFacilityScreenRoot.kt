package com.charm.ehr.kiosk.presentation.select_facility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charm.ehr.kiosk.common.AuthManager

@Composable
fun SelectFacilityScreenRoot(
    authManager: AuthManager,
    onSelectFacility: (String) -> Unit = {},
    viewModel: SelectFacilityViewModel,
    isFromSettings: Boolean,
    onBackPressed: () -> Unit,
) {

    val state by viewModel.facilityListState.collectAsStateWithLifecycle()

    if (state.isSingleFacility) {
        val facility = state.facilityList.firstOrNull()
        authManager.selectedFacilityId = facility?.facilityId ?: ""
        authManager.selectedFacility = facility
        onSelectFacility(facility?.facilityId ?: "")
        return
    }

    SelectFacilityScreen(
        memberName = authManager.kioskSettingsFlow.value?.accountDetails?.loginId ?: "",
        onClickFacility = { facility ->
            val facilityId = facility.facilityId ?: ""
            authManager.selectedFacilityId = facilityId
            authManager.selectedFacility = facility
            onSelectFacility(facilityId)
        },
        state = state,
        onClickRetry = {
            viewModel.getFacilityList()
        },
        isFromSettings = isFromSettings,
        onBackPressed = onBackPressed,
        selectedFacility=authManager.selectedFacility

    )

}
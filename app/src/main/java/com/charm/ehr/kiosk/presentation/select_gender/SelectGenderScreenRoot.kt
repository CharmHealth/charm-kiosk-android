package com.charm.ehr.kiosk.presentation.select_gender


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charm.ehr.kiosk.presentation.dashboard.DashboardViewModel

@Composable
fun SelectGenderScreenRoot(dashboardViewModel: DashboardViewModel,onBackPressed:()-> Unit) {
    val genderList by dashboardViewModel.genderList.collectAsStateWithLifecycle()
    val settingsState by dashboardViewModel.settingState.collectAsStateWithLifecycle()
      SelectGenderScreen(
        genderIdentity = genderList,
      settingsState,
        onGenderSelected = { gender ->
            dashboardViewModel.setDefaultGender(gender)
            onBackPressed()
        },
        onBackPressed = {
            onBackPressed()
        }
    )
}
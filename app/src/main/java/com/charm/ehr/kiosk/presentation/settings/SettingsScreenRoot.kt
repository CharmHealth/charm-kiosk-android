package com.charm.ehr.kiosk.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charm.ehr.kiosk.domain.Facility
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.composable.CharmKioskLogoHeader
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.dashboard.DashboardViewModel
import com.charm.ehr.kiosk.presentation.theme.MdColors
import java.util.UUID

@Composable
fun SettingsScreenRoot(
    dashboardViewModel: DashboardViewModel,
    navigateToSwitchFacility: () -> Unit,
    navigateToSupport: () -> Unit,
    logOutAndNavigateToTourPage: () -> Unit,
    navigateToGenderSelection: () -> Unit,
    navigateToSelectAppLock: () -> Unit,
    navigateToWebView: (String, String) -> Unit,
    popBackStack: () -> Unit,
    isStandAloneUser: Boolean,
) {
    val settingsState by dashboardViewModel.settingState.collectAsStateWithLifecycle()
    val refreshWorkId by dashboardViewModel.refreshWorkId.collectAsStateWithLifecycle()
    val context = LocalContext.current
    SettingsScreenContainer(
        state = settingsState,
        onAction = {

            when (it) {
                is SettingsAction.OnLogout -> {
                    logOutAndNavigateToTourPage()
                }

                is SettingsAction.OnContactSupport -> {
                    navigateToSupport()
                }

                is SettingsAction.OnSelectGender -> {
                    navigateToGenderSelection()
                }

                is SettingsAction.onAppLockChange -> {
                    navigateToSelectAppLock()
                }

                is SettingsAction.OnTermsOfService -> {
                    val url = context.getString(R.string.charm_terms_of_service_url)
                    val heading = context.getString(R.string.terms_of_service)
                    navigateToWebView(url, heading)
                }

                is SettingsAction.OnPrivacyPolicy -> {
                    val url = context.getString(R.string.charm_privacy_policy_url)
                    val heading = context.getString(R.string.privacy_policy)
                    navigateToWebView(url, heading)
                }

                is SettingsAction.OnAppointmentStatus -> {
                    val url = context.getString(R.string.charm_patient_check_in_url)
                    val heading = context.getString(R.string.appointment_status_configure_msg)
                    navigateToWebView(url, heading)
                }

                is SettingsAction.onRefreshWorkCompleted -> {
                    dashboardViewModel.onRefreshWorkCompleted(it.success, it.error)
                }

                else -> {
                    dashboardViewModel.onSettingsAction(it, context)
                    if (it is SettingsAction.OnSwitchFacility) {
                        navigateToSwitchFacility()
                    }
                }
            }

        },
        selectedFacility = dashboardViewModel.getSelectedFacility(),
        onBackPressed = popBackStack,
        isStandAloneUser,
        refreshWorkId
    )
}

@Composable
fun SettingsScreenContainer(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    selectedFacility: Facility?,
    onBackPressed: () -> Unit,
    isStandAloneUser: Boolean,
    refreshWorkId: UUID?,

    ) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MdColors.MdSurfaceColor),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CharmKioskLogoHeader(onBackPressed = onBackPressed, showBackPress = true)
            MdCardWithoutTonalElevation(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(MdColors.MdSurfaceColor)
                    .padding(
                        start = 18.dp, top = 18.dp, end = 18.dp, bottom = 18.dp
                    ),
                shape = RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp
                )
            ) {
                SettingsScreen(
                    state = state,
                    onAction = onAction,
                    selectedFacility = selectedFacility,
                    isStandAloneUser, refreshWorkId

                )
            }
        }
    }
}
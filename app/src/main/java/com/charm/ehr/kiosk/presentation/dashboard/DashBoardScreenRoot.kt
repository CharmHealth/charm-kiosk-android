package com.charm.ehr.kiosk.presentation.dashboard

import android.Manifest
import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.composable.MdAlertDialog
import com.charm.ehr.kiosk.presentation.composable.ShowEducationalDialog
import com.charm.ehr.kiosk.presentation.composable.ShowOpenSettingDialog
import com.charm.ehr.kiosk.presentation.dashboard.search_patient.AddPatientAction
import com.charm.ehr.kiosk.presentation.dashboard.search_patient.SearchPatientAction
import com.charm.ehr.kiosk.presentation.settings.SettingsAction
import com.charm.ehr.kiosk.presentation.tour_page.SessionManager
import com.charm.ehr.kiosk.common.util.MediaUtil

@Composable
fun DashBoardScreenRoot(
    dashboardViewModel: DashboardViewModel,
    imageLoader: ImageLoader,
    navigateToPatientProfile: (String) -> Unit,
    navigateToSwitchFacility: () -> Unit,
    navigateToSupport: () -> Unit,
    logOutAndNavigateToTourPage: () -> Unit,
    navigateToGenderSelection: () -> Unit,
    navigateToSelectAppLock: () -> Unit,
    navigateToWebView: (String, String) -> Unit,
    popBackStack: () -> Unit,
    isStandAloneUser: Boolean,
    sessionManager: SessionManager,
) {

    val searchPatientState by dashboardViewModel.searchPatientState.collectAsStateWithLifecycle()
    val addPatientState by dashboardViewModel.addPatientState.collectAsStateWithLifecycle()
    val settingsState by dashboardViewModel.settingState.collectAsStateWithLifecycle()
    val genderList by dashboardViewModel.genderList.collectAsStateWithLifecycle()
    val generalError by dashboardViewModel.generalErrorState.collectAsStateWithLifecycle()
    val refreshWorkId by dashboardViewModel.refreshWorkId.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val activity = context as? Activity

    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showOpenSettingDialog by remember { mutableStateOf(false) }
    var showEducationalDialog by remember { mutableStateOf(false) }

    if (generalError != null) {
        MdAlertDialog(
            heading = stringResource(R.string.api_not_success_error),
            message = generalError?.asString() ?: "",
            primaryButtonResId = R.string.retry,
            secondaryButtonResId = R.string.cancel,
            onPrimaryAction = {
                dashboardViewModel.setGeneralError(null)
                dashboardViewModel.fetchDemographicsMetrics()
            },
            onSecondaryAction = {
                dashboardViewModel.setGeneralError(null)
            }
        )
    }

    val cameraResultLauncher =
        MediaUtil.rememberActivityResultLauncherWithPasscodeReset(onResult = {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data
//            if (uri != null && activity != null) {
                dashboardViewModel.updatePatientImageUri(
                    capturedImageUri,
                    context.applicationContext
                )
//            }
            }
        }, sessionManager = sessionManager)

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            capturedImageUri = MediaUtil.launchCamera(
                activity, cameraResultLauncher,
                sessionManager = sessionManager
            )
        } else {
            activity?.let {
                MediaUtil.permissionDeniedHandler(
                    activity,
                    Manifest.permission.CAMERA, {
                        showEducationalDialog = true
                    }
                )
            }
        }
    }

    if (showOpenSettingDialog) {
        ShowOpenSettingDialog(
            stringResource(R.string.cameraStr),
            onPrimaryAction = {
                activity?.let {
                    MediaUtil.intentToSettings(it)
                }
                showOpenSettingDialog = false
            },
            onSecondaryAction = {
                showOpenSettingDialog = false
            }
        )
    }

    if (showEducationalDialog) {
        ShowEducationalDialog(
            stringResource(R.string.cameraStr),
            stringResource(R.string.allow_patient_profile),
            onPrimaryAction = {
                activity?.let {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
                showEducationalDialog = false
            },
            onSecondaryAction = {
                showEducationalDialog = false
            }
        )
    }

    DashboardScreen(
        imageLoader = imageLoader,
        searchPatientState = searchPatientState,
        addPatientState = addPatientState,
        settingState = settingsState,
        onSearchPatientAction = {
            when (it) {
                is SearchPatientAction.OnBackPress -> {
                    popBackStack()
                }

                is SearchPatientAction.OnClickPatient -> {
                    navigateToPatientProfile(it.patient.practicePatientId ?: "")
                }

                else -> Unit
            }
            dashboardViewModel.onSearchPatientAction(it)
        },
        onAddPatientAction = {
            if (it is AddPatientAction.OnClickProfilePicture) {
                if (activity != null) {
                    MediaUtil.requestCameraPermission(
                        activity, permissionGrantedCallback = {
                            capturedImageUri = MediaUtil.launchCamera(
                                activity,
                                cameraResultLauncher, sessionManager
                            )
                        },
                        permissionDeniedCallBack = {
                            showOpenSettingDialog = true
                        },
                        requestPermissionCallback = {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    )
                }
            }
            dashboardViewModel.onAddPatientAction(it)
        },
        onSettingsAction = {

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
        navigateToPatientProfile = {
            navigateToPatientProfile(it)
        },
        selectedFacility = dashboardViewModel.getSelectedFacility(),
        loginName = dashboardViewModel.getLoginUserName(),
        genderOptionList = genderList,
        isStandAloneUser = isStandAloneUser,
        refreshWorkId = refreshWorkId
    )

}

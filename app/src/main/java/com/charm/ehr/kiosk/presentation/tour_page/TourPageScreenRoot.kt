package com.charm.ehr.kiosk.presentation.tour_page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkManager
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.composable.MdMessageDialog
import com.charm.ehr.kiosk.presentation.composable.MdProgressDialog
import com.charm.ehr.kiosk.data.worker.UpdateDataFromServerWorker

@Composable
fun TourPageScreenRoot(
    tourPageViewModel: TourPageViewModel,
    onLoginClicked: () -> Unit,
    onUserVerification: () -> Unit,
    navigateToWebView: (url: String, heading: String) -> Unit,
) {

    val state by tourPageViewModel.tourPageState.collectAsStateWithLifecycle()
    val refreshWorkId by tourPageViewModel.refreshWorkId.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.isUserVerified) {
        if (state.isUserVerified) {
            onUserVerification()
        }
    }
    if (state.isLoading && !state.isLoggedIn) {
        MdProgressDialog()
    }

    if (state.updateDataErrorMessage != null) {

        MdMessageDialog(
            message = state.updateDataErrorMessage?.asString() ?: "",
            title = stringResource(R.string.api_not_success_error),
            okButtonText = stringResource(R.string.retry),
            allowDismiss = false,
            onOk = { tourPageViewModel.onRetryUpdateDataFailedCalls() }
        )
    }

    if (state.showKioskTypeMismatchError) {
        MdMessageDialog(
            message = stringResource(R.string.account_type_changed_error_msg),
            title = stringResource(R.string.account_type_changed),
            okButtonText = stringResource(R.string.alertDialogOkTextNoI18N),
            allowDismiss = false,
            onOk = { tourPageViewModel.resetKioskTypeMismatchDialog() }
        )
    }

    LaunchedEffect(refreshWorkId) {
        refreshWorkId?.let { id ->
            WorkManager.getInstance(context.applicationContext)
                .getWorkInfoByIdFlow(id).collect { workInfo ->
                    if (workInfo != null && workInfo.state.isFinished) {
                        val success = workInfo.state == androidx.work.WorkInfo.State.SUCCEEDED
                        val error =
                            workInfo.outputData.getString(UpdateDataFromServerWorker.KEY_ERROR)
                        val failedAPI =
                            workInfo.outputData.getString(UpdateDataFromServerWorker.KEY_FAILED_API_KEYS)
                        tourPageViewModel.onRefreshWorkCompleted(
                            success = success,
                            error = error ?: "", failedAPI
                        )
                    }
                }
        }
    }


    if (!state.isLoggedIn) {
        TourPageScreen(
            state,
            onLoginClicked = { onLoginClicked() },
            resetNoPrevilegeDialog = {
                tourPageViewModel.resetNoPrevilegeDialog()
            },
            hideLoginFailureError = {
                tourPageViewModel.hideLoginFailureError()
            },
            getSettings = {
                tourPageViewModel.getSettingsWithKioskCheck()
            },
            resetErrorState = {
                tourPageViewModel.resetErrorState()
            },
            navigateToKioskAccountCreation = {
                navigateToWebView(
                    context.getString(R.string.setup_charm_kiosk_account_url),
                    context.getString(R.string.create_kiosk_account)
                )
            },
            setKioskAccountDialogShown = {
                tourPageViewModel.setKioskAccountDialogShown()
            }
        )
    } else {
        //TODO get and show illustration screen for setting up Kiosk
        MdProgressDialog(messageStrId = R.string.setting_up_kiosk)
    }
}
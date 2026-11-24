package com.charm.ehr.kiosk.presentation.self_check_in

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.composable.MdAlertDialog
import com.charm.ehr.kiosk.presentation.composable.MdMessageDialog
import com.charm.ehr.kiosk.presentation.composable.MdProgressDialog

@Composable
fun SelfCheckInScreenRoot(
    selfCheckInViewModel: SelfCheckInViewModel,
    imageLoader: ImageLoader,
    navigateToSelectPatient: () -> Unit,
    navigateToPatientDashBoard: (patientId: String) -> Unit,
    navigateToSettingsScreen: () -> Unit,
) {

    val state by selfCheckInViewModel.selfCheckInState.collectAsStateWithLifecycle()
    val noPatientAvailableDialog by selfCheckInViewModel.noPatientAvailableDialog.collectAsStateWithLifecycle()
    val otpMaxAttemptDialog by selfCheckInViewModel.otpMaxAttemptDialog.collectAsStateWithLifecycle()
    val otpReSendMaxAttemptDialog by selfCheckInViewModel.otpReSendMaxAttemptDialog.collectAsStateWithLifecycle()

    if (state.isLoading) {
        MdProgressDialog()
    }

    if (state.apiErrorMessage?.asString()?.isNotBlank() == true) {
        MdAlertDialog(
            onPrimaryAction = {
                selfCheckInViewModel.onRetryFailureCall()
            },
            onSecondaryAction = {
                selfCheckInViewModel.onAction(SelfCheckInAction.OnClearApiErrorMessage)
            },
            message = state.apiErrorMessage?.asString() ?: "",
        )
    }

    if (otpReSendMaxAttemptDialog) {
        MdMessageDialog(
            title = stringResource(R.string.max_limit_reached),
            onOk = {
                selfCheckInViewModel.otpReSendMaxAttemptDialog(false)
                selfCheckInViewModel.updateOtpSentState()

            },
            allowDismiss = false,
            message = stringResource(R.string.max_resend_otp_attempts),
        )
    }
    if (otpMaxAttemptDialog) {
        MdAlertDialog(
            onPrimaryAction = {
                selfCheckInViewModel.resendOtp()
                selfCheckInViewModel.setOtpMaxAttemptDialog(false)
            },
            onSecondaryAction = {
                selfCheckInViewModel.updateOtpSentState()
                selfCheckInViewModel.setOtpMaxAttemptDialog(false)
            },
            message = stringResource(R.string.max_attempt_reached_resend_otp),
        )
    }


    if (state.patientList.size == 1) {
        val patientId = state.patientList.firstOrNull()?.practicePatientId ?: ""
        navigateToPatientDashBoard(patientId)
    }

    if (state.patientList.size > 1) {
        navigateToSelectPatient()
    }

    if (noPatientAvailableDialog) {
        MdMessageDialog(
            message = stringResource(R.string.no_patient_contact_front_desk),
            title = stringResource(R.string.no_patients_found),
            okButtonText = stringResource(R.string.alertDialogOkTextNoI18N),
            allowDismiss = false
        ) {

            selfCheckInViewModel.setNoPatientAvailableDialog(false)
            selfCheckInViewModel.updateOtpSentState()
        }
    }

    SelfCheckInScreen(
        state = state,
        onAction = {
            if (it == SelfCheckInAction.OnSettingsClicked) {
                navigateToSettingsScreen()
            }
            selfCheckInViewModel.onAction(it)
        },
        imageLoader = imageLoader,
    )

}

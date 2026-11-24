package com.charm.ehr.kiosk.presentation.verify_dob

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.util.ModelUtil
import com.charm.ehr.kiosk.presentation.composable.MdAlertDialog

@Composable
fun VerifyAgeScreenRoot(
    viewModel: VerifyDobViewmodel,
    popBackStack: () -> Unit,
    imageLoader: ImageLoader,
    onSuccessVerifyDob: () -> Unit,
    onFailureVerifyDob: () -> Unit,
    navigateToPasscodeScreen: () -> Unit,
) {

    val state by viewModel.verifyAgeState.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }

    if (state.isAgeVerified) {
        onSuccessVerifyDob()
        viewModel.setIsVerifiedAge(false)
    }

    if (state.attemptsLeft <= 0) {
        onFailureVerifyDob()
        viewModel.resetAttempts()
    }

    BackHandler(enabled = true) {
        showExitDialog = true
    }

    if (showExitDialog) {
        MdAlertDialog(
            heading = stringResource(R.string.exit_user),
            message = stringResource(R.string.exit_flow_msg),
            primaryButtonResId = R.string.exitStr,
            onPrimaryAction = {
                showExitDialog = false
                navigateToPasscodeScreen()
            },
            onSecondaryAction = {
                showExitDialog = false
            },
            secondaryButtonResId = R.string.cancel,
        )
    }

    VerifyDobScreen(
        state = state,
        onAction = { action ->
            if (action is VerifyDobAction.OnBackPress) {
                popBackStack()
            } else {
                viewModel.onVerifyAge(action)
            }
        },
        imageLoader = imageLoader,
        displayDateFormate = ModelUtil.displayDateFormateMonthFirst,
        onBackClick = {
            showExitDialog = true
        }
    )
}
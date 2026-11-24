package com.charm.ehr.kiosk.presentation.passcode

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.composable.MdAlertDialog
import com.charm.ehr.kiosk.common.util.SecurityUtil

@Composable
fun PasscodeScreenRoot(
    viewModel: PasscodeViewModel,
    onLogOutClicked: () -> Unit,
    onSuccessUnlock: () -> Unit,
    isFromIdleState: Boolean,
    finishApp: () -> Unit,
    isToCreatePasscode: Boolean,
    passcodeType: SecurityUtil.AuthType?,
    onBackPressed: () -> Boolean,
    isFromSettings: Boolean,
    isFromVerification: Boolean
) {

    val state by viewModel.passcodeState.collectAsStateWithLifecycle()

    var showLogoutDialog by remember { mutableStateOf(false) }

    if (state.isVerified) {
        onSuccessUnlock()
        return
    }

    val context = LocalContext.current
    var lastBackPress by remember { mutableStateOf(0L) }

    // Intercept back press
    BackHandler(enabled = isFromIdleState) {
        if (viewModel.passcodeVerificationFlow == PasscodeVerificationFlow.LOCK_TYPE_CHANGE) {
            onBackPressed()
        } else {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastBackPress < 2000) {
                // Exit app
                finishApp()
            } else {
                lastBackPress = currentTime
                Toast.makeText(
                    context,
                    context.getString(R.string.press_back_again_to_exit),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    if (showLogoutDialog) {
        MdAlertDialog(
            heading = stringResource(R.string.sign_out),
            message = stringResource(R.string.areYouSureWantToLogout),
            primaryButtonResId = R.string.alertDialogTextYes,
            secondaryButtonResId = R.string.alertDialogTextNo,
            onPrimaryAction = {
                showLogoutDialog = false
                onLogOutClicked()
            },
            onSecondaryAction = { showLogoutDialog = false }
        )
    }

    PasscodeScreen(
        state = state,
        onAction = {
            when (it) {
                is PasscodeAction.OnLogOut -> showLogoutDialog = true
                else -> {}
            }
            viewModel.onAction(it)
        }, isToCreatePasscode = isToCreatePasscode, passcodeType = passcodeType,(isFromSettings || isFromVerification)
    )

}

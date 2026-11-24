package com.charm.ehr.kiosk.presentation.support

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.util.AppticsUtil
import com.charm.ehr.kiosk.common.util.MdEvent
import com.charm.ehr.kiosk.common.util.MdEventGroup
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.presentation.composable.MdAlertDialog
import com.charm.ehr.kiosk.presentation.composable.MdProgressDialog

@Composable
fun SupportScreenRoot(
    supportViewModel: SupportViewModel,
    imageLoader: ImageLoader,
    onBackPressed: () -> Unit
) {

    val state by supportViewModel.supportState.collectAsStateWithLifecycle()

    var showOnBackPressDialog by remember { mutableStateOf(false) }

    val photoPickerMultiple = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        supportViewModel.onAction(SupportScreenAction.AddNewFile(uris))
    }

    if (state.sendSupportResult is APIState.Loading || state.uploadFilesResult is APIState.Loading) {
        MdProgressDialog()
    }

    val localContext = LocalContext.current
    state.toastMessage?.let { uiText ->
        val toastMessage = uiText.asString()
        LaunchedEffect(toastMessage) {
            Toast.makeText(localContext, toastMessage, Toast.LENGTH_LONG).show()
            supportViewModel.onAction(SupportScreenAction.ClearToastMessage)
        }
    }

//    MdAlertDialog(heading = R.string.onback)

    state.sendSupportResult.let {
        if (it is APIState.Error) {
            AppticsUtil.addEvent(MdEvent.OnSendSupportFailure, MdEventGroup.Support)
            MdAlertDialog(
                message = it.apiResponseError.errorMsg?.asString()
                    ?: stringResource(R.string.errorOccurred),
                primaryButtonResId = R.string.retry,
                secondaryButtonResId = R.string.cancel,
                onPrimaryAction = {
                    supportViewModel.onAction(SupportScreenAction.RetrySendSupport)
                },
                onSecondaryAction = {
                    supportViewModel.onAction(SupportScreenAction.ClearSendSupportResult)
                })
        } else if (it is APIState.Success && it.data) {
            //Show success dialog for 3 seconds and get back
            AppticsUtil.addEvent(MdEvent.OnSendSupportSuccess, MdEventGroup.Support)
            onBackPressed()
        }
    }

    state.uploadFilesResult.let {
        if (it is APIState.Error) {
            MdAlertDialog(
                message = it.apiResponseError.errorMsg?.asString()
                    ?: stringResource(R.string.errorOccurred),
                primaryButtonResId = R.string.retry,
                secondaryButtonResId = R.string.cancel,
                onPrimaryAction = {
                    supportViewModel.onAction(SupportScreenAction.RetryUploadFiles)
                },
                onSecondaryAction = {
                    supportViewModel.onAction(SupportScreenAction.ClearUploadFilesResult)
                })
        }
    }

    BackHandler(enabled = true) {
        if (state.hasChange) {
            showOnBackPressDialog = true
        } else {
            onBackPressed()
        }
    }


    if (showOnBackPressDialog) {
        MdAlertDialog(
            heading = stringResource(R.string.unsaved_changes),
            message = stringResource(R.string.discard_changes_msg),
            primaryButtonResId = R.string.discard,
            secondaryButtonResId = R.string.cancel,
            onPrimaryAction = {
                onBackPressed()
                showOnBackPressDialog = false
            },
            onSecondaryAction = {
                showOnBackPressDialog = false
            })
    }


    SupportScreen(
        state = state,
        onAction = {
            if (it is SupportScreenAction.OnClickAddAttachment) {
                photoPickerMultiple.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }

            if (it is SupportScreenAction.OnBackPressed) {
                if (state.hasChange) {
                    showOnBackPressDialog = true
                } else {
                    onBackPressed()
                }
            }
            supportViewModel.onAction(it)
        },
        imageLoader = imageLoader
    )

}
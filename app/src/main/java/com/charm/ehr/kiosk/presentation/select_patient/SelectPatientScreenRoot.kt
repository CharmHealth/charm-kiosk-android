package com.charm.ehr.kiosk.presentation.select_patient

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.composable.MdAlertDialog
import com.charm.ehr.kiosk.presentation.composable.MdMessageDialog
import com.charm.ehr.kiosk.presentation.composable.MdProgressDialog

@Composable
fun SelectPatientScreenRoot(
    viewModel: SelectPatientViewModel,
    imageLoader: ImageLoader,
    navigateToDashboard: (patientId: String) -> Unit,
    navigateToEntry: () -> Unit
) {
    val state by viewModel.selectPatientState.collectAsStateWithLifecycle()
    var showNoPatientDialog by remember { mutableStateOf(false) }

    if (state.isLoading) {
        MdProgressDialog()
    }

    if (state.errorMessage != null) {
        MdAlertDialog(
            message = state.errorMessage ?: "",
            onPrimaryAction = {},
            onSecondaryAction = {},
        )
    }

    if (showNoPatientDialog) {
        MdMessageDialog(
            title = stringResource(R.string.select_patient),
            onOk = { showNoPatientDialog = false },
            message = stringResource(R.string.select_patient_to_continue)
        )
    }

    SelectPatientScreen(
        state = state, onAction = {
            viewModel.onAction(it)
            if (it is SelectPatientAction.OnClickDone) {
                val patientId = state.selectedPatient?.practicePatientId
                if (patientId.isNullOrBlank()) {
                    showNoPatientDialog = true
                } else {
                    navigateToDashboard(patientId)
                }
            } else if (it is SelectPatientAction.OnExitClicked) {
                navigateToEntry()
            }
        }, imageLoader = imageLoader
    )
}

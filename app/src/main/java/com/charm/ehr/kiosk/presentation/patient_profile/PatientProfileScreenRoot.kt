package com.charm.ehr.kiosk.presentation.patient_profile


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
import coil.ImageLoader
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.common.PatientProfileFlow
import com.charm.ehr.kiosk.common.util.ModelUtil
import com.charm.ehr.kiosk.presentation.composable.MdAlertDialog

@Composable
fun PatientProfileScreenRoot(
    patientDetailViewmodel: PatientDetailViewmodel,
    imageLoader: ImageLoader,
    popBackStack: () -> Unit,
    navigateToSearchTemplate: () -> Unit,
    navigateToTemplateList: () -> Unit,
    navigateToVerifyDob: (String) -> Unit,
    logOutPatientFlow: () -> Unit,
    navigateToEditProfile: (Section?) -> Unit,
    navigateToThankYouScreen: () -> Unit,
    authManager: AuthManager,
) {

    val state by patientDetailViewmodel.patientProfileState.collectAsStateWithLifecycle()
    val selectedPatient by patientDetailViewmodel.selectedPatient.collectAsStateWithLifecycle()
    val patientSharedQuestionnaires by patientDetailViewmodel.patientSharedQuestionnaires.collectAsStateWithLifecycle()
    val patientProfileFlow =
        authManager.workFlowType.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }

    if (!state.initCallsPerformed) {
        patientDetailViewmodel.initApiCalls()
    }

    if (state.isLogOutPatient) {
        logOutPatientFlow()
        patientDetailViewmodel.setLogoutPatientFlow(false)
    }
    val context = LocalContext.current
    BackHandler(enabled = true) {
        if (patientProfileFlow.value == PatientProfileFlow.AFTER_HANDOVER) {
            Toast.makeText(
                context,
                context.getString(R.string.finish_click_educate_msg),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            popBackStack()
        }
    }

    if (showExitDialog) {
        val pendingQuestionnaireCount = patientSharedQuestionnaires?.filter {
            it.patientQuesMap?.isSubmitted == false
        }?.size
        MdAlertDialog(
            onSecondaryAction = {
                showExitDialog = false
            },
            heading = stringResource(R.string.finish_exit),
            message = if (pendingQuestionnaireCount == 0) stringResource(R.string.finish_exit_msg) else stringResource(
                R.string.exit_non_submit_questionnaire_msg
            ),
            primaryButtonResId = R.string.finishStr,
            onPrimaryAction = {
                showExitDialog = false
                navigateToThankYouScreen()
            },
            secondaryButtonResId = R.string.cancel,
        )
    }

    if (state.initialCallErrorMessage != null) {
        MdAlertDialog(
            onSecondaryAction = {
                patientDetailViewmodel.clearError()
                popBackStack()
            },
            heading = stringResource(R.string.api_not_success_error),
            message = state.initialCallErrorMessage?.asString() ?: "",
            primaryButtonResId = R.string.retry,
            onPrimaryAction = {
                patientDetailViewmodel.initialCallRetry()
            },
            secondaryButtonResId = R.string.cancel,
        )
    }

    if (state.errorMessage != null) {
        MdAlertDialog(
            onSecondaryAction = {
                patientDetailViewmodel.clearError()
                if (patientDetailViewmodel.isInitialLoadFailed) {
                    popBackStack()
                }
            },
            heading = stringResource(R.string.api_not_success_error),
            message = state.errorMessage?.asString() ?: "",
            primaryButtonResId = R.string.retry,
            onPrimaryAction = {
                patientDetailViewmodel.retryLastFailedAction()
            },
            secondaryButtonResId = R.string.cancel,
        )
    }

    PatientProfileScreen(
        state = state,
        imageLoader = imageLoader,
        popBackStack = {
            popBackStack()
        },
        selectedPatient,
        patientSharedQuestionnaires,
        onAction = {
            when (it) {
                is PatientProfileAction.onAddQuestionnaire -> {
                    patientDetailViewmodel.setPatSharedQuesToSelectedQuesState()
                    navigateToSearchTemplate()
                }

                is PatientProfileAction.onHandOver -> {
                    val date =
                        selectedPatient?.dateOfBirth?.toString(ModelUtil.charmApiDateFormat())
                            ?: ""
                    navigateToVerifyDob(date)
                }

                is PatientProfileAction.onClickFill -> {
                    navigateToTemplateList()
                }

                is PatientProfileAction.onEditPatient -> {
                    navigateToEditProfile(it.selectedSection)
                }

                is PatientProfileAction.onAddMissingSection -> {
                    patientDetailViewmodel.addMissingSection(it.section)
                }

                is PatientProfileAction.onRemoveMissingSection -> {
                    patientDetailViewmodel.removeMissingSection(it.section)
                }

                PatientProfileAction.onAddConsentForm -> {}
                PatientProfileAction.onBackPress -> {}
                PatientProfileAction.onFinishClick -> {
                    showExitDialog = true
                }
            }
        },
        patientProfileFlow.value
    )
}

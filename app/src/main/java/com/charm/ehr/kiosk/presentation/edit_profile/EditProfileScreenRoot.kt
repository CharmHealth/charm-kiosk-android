package com.charm.ehr.kiosk.presentation.edit_profile

import android.Manifest
import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
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
import coil.annotation.ExperimentalCoilApi
import coil.memory.MemoryCache
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.composable.MdAlertDialog
import com.charm.ehr.kiosk.presentation.composable.ShowEducationalDialog
import com.charm.ehr.kiosk.presentation.composable.ShowOpenSettingDialog
import com.charm.ehr.kiosk.presentation.patient_profile.Section
import com.charm.ehr.kiosk.presentation.tour_page.SessionManager
import com.charm.ehr.kiosk.common.util.MediaUtil
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalCoilApi::class)
@Composable
fun EditProfileScreenRoot(
    editProfileViewModel: EditProfileViewModel,
    popBackStack: () -> Unit,
    imageLoader: ImageLoader,
    sessionManager: SessionManager
) {

    val state by editProfileViewModel.editProfileState.collectAsStateWithLifecycle()
    val showDiscardDialog by editProfileViewModel.showDiscardDialog.collectAsStateWithLifecycle()
    val saveInsuranceDialog by editProfileViewModel.saveInsuranceDialog.collectAsStateWithLifecycle()

    LaunchedEffect(editProfileViewModel.navigationEvents) {
        editProfileViewModel.navigationEvents.collectLatest { event ->
            when (event) {
                CustomNavigationEvent.Back -> popBackStack()
            }
        }
    }

    BackHandler(enabled = true) {
        editProfileViewModel.onChangeAction(EditProfileAction.OnBackPress)
    }

    if (showDiscardDialog) {
        if (state.selectedSection == Section.PERSONAL_INFO || state.selectedSection == Section.CONTACT_INFO || state.selectedSection == Section.CAREGIVER_DETAILS) {

            MdAlertDialog(
                message = stringResource(R.string.save_changes_msg),
                primaryButtonResId = R.string.save,
                secondaryButtonResId = R.string.discard,
                tertiaryButtonResId = R.string.cancel,
                onPrimaryAction = {
                    editProfileViewModel.onChangeAction(EditProfileAction.DismissDiscardDialog)
                    editProfileViewModel.onChangeAction(
                        EditProfileAction.OnClickDialogSave
                    )
                },
                onSecondaryAction = {
                    editProfileViewModel.onChangeAction(EditProfileAction.DismissDiscardDialog)
                    popBackStack()
                },
                onTertiaryAction = {
                    editProfileViewModel.onChangeAction(EditProfileAction.DismissDiscardDialog)
                }
            )
        } else {
            MdAlertDialog(
                message = stringResource(R.string.save_changes_msg),
                primaryButtonResId = R.string.save,
                secondaryButtonResId = R.string.skip,
                tertiaryButtonResId = R.string.cancel,
                onPrimaryAction = {
                    editProfileViewModel.onChangeAction(EditProfileAction.DismissDiscardDialog)
                    editProfileViewModel.onChangeAction(
                        EditProfileAction.OnClickDialogSave
                    )
                },
                onSecondaryAction = {
                    editProfileViewModel.onChangeAction(EditProfileAction.DismissDiscardDialog)
                    popBackStack()
                },
                onTertiaryAction = {
                    editProfileViewModel.onChangeAction(EditProfileAction.DismissDiscardDialog)
                }
            )
        }
    }

    if (saveInsuranceDialog) {
        MdAlertDialog(
            message = stringResource(R.string.save_insurance_detail),
            primaryButtonResId = R.string.save,
            secondaryButtonResId = R.string.discard,
            onPrimaryAction = {
                if (editProfileViewModel.editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE) {
                    editProfileViewModel.onChangeAction(EditProfileAction.OnClickSave(Section.PRIMARY_INSURANCE))
                } else {
                    editProfileViewModel.onChangeAction(EditProfileAction.OnClickSave(Section.SECONDARY_INSURANCE))
                }
            },
            onSecondaryAction = {
                editProfileViewModel.proceedPendingSectionChange()
            }
        )
    }


    val context = LocalContext.current
    val activity = context as? Activity

    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isFront by remember { mutableStateOf(false) }
    var showOpenSettingDialog by remember { mutableStateOf(false) }
    var showEducationalDialog by remember { mutableStateOf(false) }
    var isInsuranceUpload by remember { mutableStateOf(false) }

    val insuranceCardUploadLauncher =
        MediaUtil.rememberActivityResultLauncherWithPasscodeReset(
        onResult = {
            if (it.resultCode == Activity.RESULT_OK) {
                editProfileViewModel.updateInsuranceCard(
                    capturedImageUri,
                    context.applicationContext, isFront
                )
            }
        },
        sessionManager = sessionManager
    )

    val profileImageLauncher =
        MediaUtil.rememberActivityResultLauncherWithPasscodeReset(onResult = {

            if (it.resultCode == Activity.RESULT_OK) {
                state.personalInformation.profileImageFilePath.filePath?.let { filePath ->
                    val key = MemoryCache.Key(filePath)
                    imageLoader.memoryCache?.remove(key)
                    imageLoader.diskCache?.remove(filePath)
                }
                editProfileViewModel.updatePatientProfile(
                    capturedImageUri,
                    context.applicationContext
                )
            }
        }, sessionManager = sessionManager)


    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            capturedImageUri = MediaUtil.launchCamera(
                activity,
                if (isInsuranceUpload) insuranceCardUploadLauncher else profileImageLauncher,
                sessionManager
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



    EditProfileScreen(
        state = state,
        onAction = {
            if (activity != null) {
                if (it is EditProfileAction.OnClickInsuranceCardFront || it is EditProfileAction.OnClickInsuranceCardBack) {
                    isFront = it is EditProfileAction.OnClickInsuranceCardFront
                    isInsuranceUpload = true
                    MediaUtil.requestCameraPermission(
                        activity, permissionGrantedCallback = {
                            capturedImageUri =
                                MediaUtil.launchCamera(
                                    activity,
                                    insuranceCardUploadLauncher,
                                    sessionManager
                                )
                        },
                        permissionDeniedCallBack = {
                            showOpenSettingDialog = true
                        },
                        requestPermissionCallback = {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    )
                } else if (it is EditProfileAction.OnClickProfilePicture) {
                    isInsuranceUpload = false
                    MediaUtil.requestCameraPermission(
                        activity, permissionGrantedCallback = {
                            capturedImageUri =
                                MediaUtil.launchCamera(
                                    activity,
                                    profileImageLauncher,
                                    sessionManager
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
            editProfileViewModel.onChangeAction(it)
        },
        imageLoader
    )
}


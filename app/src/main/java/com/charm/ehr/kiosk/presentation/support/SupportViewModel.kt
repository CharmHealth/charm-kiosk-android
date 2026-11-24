package com.charm.ehr.kiosk.presentation.support

import UiText
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.domain.repository.FileRepository
import com.charm.ehr.kiosk.domain.repository.MemberRepository
import com.charm.ehr.kiosk.domain.repository.MiscRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.common.util.ModelUtil.Companion.updateWithCharacterLimit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SupportViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
    private val miscRepository: MiscRepository,
    private val fileRepository: FileRepository,
) : ViewModel() {

    private val TAG = "SupportViewModel"

    val allowedAttachmentCount = 5

    private fun update(block: SupportState.() -> SupportState) {
        _supportState.update(block)
    }

    var messageMaxLimit = 250
    var subjectMaxLimit = 80

    val deviceInfo = miscRepository.getAppDataForSupport()

    private val _supportState: MutableStateFlow<SupportState> = MutableStateFlow(SupportState())
    val supportState: StateFlow<SupportState> = _supportState.onStart {
        setMessageMaxLimitData()
        updateSubjectCountDisplay()
        updateMessageCountDisplay()
    }.stateIn(
        scope = viewModelScope, started = WhileSubscribed(5000L), initialValue = _supportState.value
    )

    fun onAction(action: SupportScreenAction) {
        when (action) {
            is SupportScreenAction.UpdateSupportField -> {

                if (!supportState.value.hasChange) {
                    update {
                        copy(
                            hasChange = true
                        )
                    }
                }

                update {
                    copy(category = action.category?.let {
                        category.copy(value = it, error = null)
                    } ?: category, subject = action.subject?.let {
                        subject.updateWithCharacterLimit(
                            newValue = it,
                            maxLength = subjectMaxLimit,
                            errorMessageResId = R.string.subject_heading_max_length_reached,
                            errorArgs = arrayListOf(subjectMaxLimit.toString()).toArray()
                        )
                    } ?: subject, message = action.message?.let {
                        message.updateWithCharacterLimit(
                            newValue = it,
                            maxLength = messageMaxLimit,
                            errorMessageResId = R.string.message_max_length_reached,
                            errorArgs = arrayListOf(messageMaxLimit.toString()).toArray()
                        )
                    } ?: message)
                }

                if (action.message != null) {
                    updateMessageCountDisplay()
                }
                if (action.subject != null) {
                    updateSubjectCountDisplay()
                }
            }

            is SupportScreenAction.AddNewFile -> {
                viewModelScope.launch {

                    val limitExceeded = action.uris.size + supportState.value.filePathAndStatus.size > allowedAttachmentCount
                    if (limitExceeded) {
                        update {
                            copy(
                                toastMessage = UiText.StrResId(R.string.max_file_limit_reached)
                            )
                        }
                    }

                    val availableSlots =
                        allowedAttachmentCount - supportState.value.filePathAndStatus.size
                    if (availableSlots <= 0) {
                        update {
                            copy(
                                toastMessage = UiText.StrResId(R.string.max_file_limit_reached)
                            )
                        }
                        return@launch
                    }

                    val uris = action.uris.take(availableSlots)

                    update {
                        copy(
                            filePathAndStatus = HashMap<String, Boolean>(filePathAndStatus).apply {
                                uris.forEach { put(it.toString(), true) }
                            })
                    }
                    // Convert each uri to file, compress if needed, and update state as each file is ready
                    uris.forEach { uri ->
                        viewModelScope.launch {
                            val size = fileRepository.getUriSize(uri)
                            val file = if (size > 2 * 1024 * 1024) {
                                fileRepository.compressImageUriAndWriteToFile(
                                    imageUri = uri, maxSizeBytes = 2000 * 1024L, //2Mb
                                    quality = 80
                                )
                            } else {
                                fileRepository.copyUriToFile(uri)
                            }
                            update {
                                val newMap = HashMap<String, Boolean>(filePathAndStatus)
                                newMap.remove(uri.toString())
                                if (file != null) newMap[file.absolutePath] = false
                                copy(
                                    filePathAndStatus = newMap
                                )
                            }
                        }
                    }
                }
            }

            is SupportScreenAction.RemoveFile -> {
                update {
                    val key = action.file.absolutePath
                    val newMap = HashMap<String, Boolean>(filePathAndStatus).apply { remove(key) }
                    copy(
                        filePathAndStatus = newMap
                    )
                }

            }

            SupportScreenAction.SendSupport -> {

                val isFormValid = isSupportFormValid()
                Log.d(TAG, "onAction: isFormValid=$isFormValid")

                if (!isFormValid) {
                    return
                }

                val fileHashMap = supportState.value.filePathAndStatus
                if (fileHashMap.count() > 0) {
                    uploadAttachments(fileHashMap.map { it.key })
                } else {
                    sendSupport()
                }

            }

            SupportScreenAction.ClearToastMessage -> {
                update {
                    copy(
                        toastMessage = null
                    )
                }
            }

            SupportScreenAction.ClearUploadFilesResult -> {
                update {
                    copy(uploadFilesResult = null)
                }
            }

            SupportScreenAction.ClearSendSupportResult -> {
                update {
                    copy(sendSupportResult = null)
                }
            }

            else -> {}
        }
    }

    private fun updateSubjectCountDisplay() {
        update {
            val currentSubjectLength = subject.value?.length ?: 0
            val displayString = "$currentSubjectLength/$subjectMaxLimit"
            copy(
                subjectCharacterDisplay = subjectCharacterDisplay.copy(value = displayString)
            )
        }
    }

    private fun updateMessageCountDisplay() {
        update {
            val currentMessageLength = message.value?.length ?: 0
            val displayString = "$currentMessageLength/$messageMaxLimit"
            copy(
                messageCharacterDisplay = messageCharacterDisplay.copy(value = displayString)
            )
        }
    }

    fun validateField() {
        _supportState.update { state ->

            val categoryError = when {
                state.category.value.isNullOrEmpty() -> {
                    UiText.StrResId(R.string.support_category_required)
                }

                else -> null
            }

            val subjectError = when {
                state.subject.value.isNullOrEmpty() -> {
                    UiText.StrResId(R.string.support_subject_empty_error)
                }

                else -> null
            }

            val descriptionError = when {
                state.message.value.isNullOrEmpty() -> {
                    UiText.StrResId(R.string.support_message_empty_error)
                }

                else -> null
            }

            val focusRequest = when {
                categoryError != null -> SupportState.FocusField.CATEGORY
                subjectError != null -> SupportState.FocusField.SUBJECT
                descriptionError != null -> SupportState.FocusField.MESSAGE
                else -> null
            }

            state.copy(
                category = state.category.copy(error = categoryError),
                subject = state.subject.copy(error = subjectError),
                message = state.message.copy(error = descriptionError),
                focusRequest = focusRequest
            )

        }
    }

    fun isSupportFormValid(): Boolean {
        validateField()
        return supportState.value.let {
            it.category.error == null && it.subject.error == null && it.message.error == null
        }
    }

    fun setMessageMaxLimitData() {
        messageMaxLimit -= deviceInfo.length
    }

    fun uploadAttachments(filePathList: List<String>) {
        viewModelScope.launch {

            update { copy(uploadFilesResult = APIState.Loading) }

            try {
                val response = withContext(Dispatchers.IO) {
                    val uriList = filePathList.map {
                        fileRepository.getUriFromFilePath(it)
                    }
                    memberRepository.uploadSupportAttachmentsFromUris(uriList)
                }

                update { copy(uploadFilesResult = response) }

                when (response) {
                    is APIState.Success -> {
                        val fileIds = response.data.joinToString(",")
                        sendSupport(fileIds)
                    }
                    is APIState.Error -> {
                        update {
                            copy(toastMessage = response.apiResponseError.errorMsg)
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "uploadAttachments failed", e)
                update {
                    copy(
                        uploadFilesResult = null,
                        toastMessage = UiText.StrResId(R.string.error_occurred_while_uploading_attachment)
                    )
                }
            }
        }
    }


    fun sendSupport(fileIds: String? = null) {

        viewModelScope.launch(Dispatchers.IO) {
            val finalMessage = supportState.value.message.value + "\n" + deviceInfo
            val result = memberRepository.sendSupport(
                categoryTxt = supportState.value.category.value ?: "",
                subjectTxt = supportState.value.subject.value ?: "",
                messageTxt = finalMessage,
                fileIds = fileIds
            )

            withContext(Dispatchers.Main) {

                update {
                    copy(sendSupportResult = result)
                }

                if (result is APIState.Success) {
                    update {
                        copy(
                            toastMessage = UiText.StrResId(R.string.support_request_sent_successfully),
                            hasChange = false
                        )
                    }
                }

            }

        }
    }

    fun updateHasChange(hasChange: Boolean) {
        update {
            copy(
                hasChange = hasChange
            )
        }
    }

}
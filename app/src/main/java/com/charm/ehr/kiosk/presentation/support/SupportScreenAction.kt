package com.charm.ehr.kiosk.presentation.support

import android.net.Uri
import java.io.File

sealed interface SupportScreenAction {

    data class UpdateSupportField(
        val category: String? = null,
        val subject: String? = null,
        val message: String? = null,
    ) : SupportScreenAction

    object OnClickAddAttachment : SupportScreenAction

    data class AddNewFile(
        val uris: List<Uri>
    ) : SupportScreenAction

    data class RemoveFile(
        val file: File
    ) : SupportScreenAction

    object SendSupport : SupportScreenAction

    object OnBackPressed : SupportScreenAction
    object ClearToastMessage : SupportScreenAction

    object ClearUploadFilesResult : SupportScreenAction
    object ClearSendSupportResult : SupportScreenAction

    object ClearFocusRequest : SupportScreenAction

    object RetrySendSupport : SupportScreenAction
    object RetryUploadFiles : SupportScreenAction



}
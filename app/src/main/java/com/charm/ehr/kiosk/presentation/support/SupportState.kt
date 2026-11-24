package com.charm.ehr.kiosk.presentation.support

import UiText
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.presentation.edit_profile.FieldState
import com.charm.ehr.kiosk.common.util.ModelUtil

data class SupportState(
    val fromAddress: FieldState<String> = FieldState(),
    val toAddress: String = "",
    val categoryOptions: FieldState<List<String>> = FieldState(value = ModelUtil.getSupportCategory()),
    val category: FieldState<String> = FieldState(),
    val subject: FieldState<String> = FieldState(),
    val subjectCharacterDisplay: FieldState<String> = FieldState(), /* like 15/80 */
    val message: FieldState<String> = FieldState(),
    val messageCharacterDisplay: FieldState<String> = FieldState(), /* like 15/300 */
    val uploadFilesResult: APIState<List<String>>? = null, // uploaded file urls
    val sendSupportResult: APIState<Boolean>? = null,
    val filePathAndStatus: HashMap<String, Boolean> = hashMapOf(), // file path to status
    val toastMessage : UiText? = null,
    val hasChange: Boolean= false, //To show dialogue on back press
    val focusRequest: FocusField? = null
) {
    enum class FocusField {
        CATEGORY,
        SUBJECT,
        MESSAGE
    }
}
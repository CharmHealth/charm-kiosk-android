package com.charm.ehr.kiosk.presentation.dashboard.add_patient

import UiText
import android.net.Uri
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.common.util.DateUtil
import com.charm.ehr.kiosk.presentation.edit_profile.FieldState
import com.charm.ehr.kiosk.common.util.ModelUtil


data class AddPatientState(
    val firstName: FieldState<String> = FieldState(),
    val lastName: FieldState<String> = FieldState(),
    val birthSex: FieldState<String> = FieldState(),
    val dateOfBirth: FieldState<String> = FieldState(),
    val uri: Uri? = null, //Use uri only to get form intent like "camera" or "gallery"
    val imageFilePath: String? = null,
    val isLoading: Boolean = false,
    val isPatAddedSuccessful: Boolean = false,
    val patientId: String = "",
    val errorMessage: UiText? = null,
    val isProfilePicUploading: Boolean = false,
    val focusedField: AddPatientField? = null,
    // Set when server returns HTTP 400 / code 48 (duplicate patient found)
    val duplicatePatient: Patient? = null
) {
    fun getPatient(): Patient {
        return Patient(
            practicePatientId = patientId,
            firstName = firstName.value,
            lastName = lastName.value,
            gender = birthSex.value,
            patientImgFilePath = imageFilePath,
            dateOfBirth = dateOfBirth.value?.let {
                DateUtil.getDate(it, ModelUtil.mothFirstDateFormatWithSlash())
            }
        )
    }

    fun clear() = AddPatientState(
        firstName = FieldState(),
        lastName = FieldState(),
        birthSex = FieldState(),
        dateOfBirth = FieldState(),
        uri = null,
        isLoading = false,
        isPatAddedSuccessful = false
    )
}

enum class AddPatientField {
    FIRST_NAME,
    LAST_NAME,
    BIRTH_SEX,
    DATE_OF_BIRTH
}


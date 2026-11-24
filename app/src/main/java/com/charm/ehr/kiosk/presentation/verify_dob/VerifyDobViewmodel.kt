package com.charm.ehr.kiosk.presentation.verify_dob

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.common.util.ModelUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class VerifyDobViewmodel @Inject constructor(val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _verifyAgeState: MutableStateFlow<VerifyDobState> = MutableStateFlow(VerifyDobState())
    val verifyAgeState: StateFlow<VerifyDobState> = _verifyAgeState
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000L),
            initialValue = _verifyAgeState.value
        )

    fun resetAttempts() {
        _verifyAgeState.update {
            it.copy(attemptsLeft = 3)
        }
    }

    fun onVerifyAge(action: VerifyDobAction) {

        when (action) {

            is VerifyDobAction.OnDateChange -> {
                _verifyAgeState.update {
                    it.copy(dob = action.dob, verifyDobError = null)
                }
            }

            is VerifyDobAction.OnVerify -> {

                val patientDob = _verifyAgeState.value.patient?.dateOfBirth?.toString(ModelUtil.mothFirstDateFormatWithSlash())
                val enteredDob = _verifyAgeState.value.dob

                if (enteredDob.length == 10) { // "mm/dd/yyyy" - 10 char
                    if (patientDob == enteredDob) {
                        setIsVerifiedAge(true)
                    } else {
                        _verifyAgeState.update {
                            it.copy(
                                verifyDobError = VerifyDobError.INCORRECT_DOB,
                                dob = "",
                                attemptsLeft = it.attemptsLeft - 1
                            )
                        }
                    }
                } else {
                    _verifyAgeState.update {
                        it.copy(
                            verifyDobError = VerifyDobError.DOB_NOT_COMPLETE,
                        )
                    }
                }
            }

            else -> {

            }

        }

    }

    fun updateSelectedPatient(patient: Patient?) {
        patient ?: return
        _verifyAgeState.update {
            it.copy(patient = patient)
        }
    }

    fun setIsVerifiedAge(isAgeVerified: Boolean) {
        _verifyAgeState.update {
            it.copy(isAgeVerified = isAgeVerified)
        }
    }

}
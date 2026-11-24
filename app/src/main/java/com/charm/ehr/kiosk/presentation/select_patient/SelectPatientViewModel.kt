package com.charm.ehr.kiosk.presentation.select_patient

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charm.ehr.kiosk.common.util.MDAccountsRelatedUtil
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.domain.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SelectPatientViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val patientRepository: PatientRepository,
    val authManager: AuthManager
) : ViewModel() {

    private val _selectPatientState: MutableStateFlow<SelectPatientState> =
        MutableStateFlow(SelectPatientState())
    val selectPatientState: StateFlow<SelectPatientState> = _selectPatientState.onStart {
        _selectPatientState.update {
            it.copy(
                facilityLogoUrl = MDAccountsRelatedUtil.getFacilityLogoUrl(
                    authManager.selectedFacilityId ?: ""
                ),
                facilityName = authManager.selectedFacility?.facilityName ?: ""
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000L),
        initialValue = _selectPatientState.value

    )

    suspend fun updateLoadingState(isLoading: Boolean) {
        withContext(Dispatchers.Main) {
            _selectPatientState.update {
                it.copy(isLoading = isLoading)
            }
        }
    }

    fun onAction(action: SelectPatientAction) {
        when (action) {
            SelectPatientAction.OnClickDone -> {

            }

            is SelectPatientAction.OnSelectPatient -> {
                _selectPatientState.update {
                    it.copy(selectedPatient = action.patient)
                }
            }

            is SelectPatientAction.OnExitClicked -> {

            }
        }
    }

    fun setPatientList(patientList: List<Patient>) {
        _selectPatientState.update {
            it.copy(patientList = patientList)
        }
    }

}
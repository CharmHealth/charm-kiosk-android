package com.charm.ehr.kiosk.presentation.select_facility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.domain.repository.MemberRepository
import com.charm.ehr.kiosk.domain.usecase.facility.GetFacilityListUseCase
import com.charm.ehr.kiosk.presentation.composable.APIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectFacilityViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val getFacilityListUseCase: GetFacilityListUseCase,
    private val memberRepository: MemberRepository
) : ViewModel() {

    private var isFromSettings: Boolean = false
    fun setIsFromSettings(value: Boolean) {
        isFromSettings = value
    }
    private val _facilityListState: MutableStateFlow<FacilityListState> =
        MutableStateFlow(FacilityListState())
    val facilityListState: StateFlow<FacilityListState> = _facilityListState.onStart {
        getFacilityList()
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000L),
        initialValue = _facilityListState.value
    )

    fun getFacilityList() {
        viewModelScope.launch {
            _facilityListState.update { it.copy(isLoading = true) }
            val result = getFacilityListUseCase(isFromSettings)
            _facilityListState.update {
                when (result) {
                    is APIState.Success -> it.copy(
                        isLoading = false,
                        facilityList = result.data.facilities,
                        isSingleFacility = result.data.autoSelectSingle
                    )
                    is APIState.Error -> it.copy(
                        isLoading = false,
                        errorMessage = result.apiResponseError.errorMsg
                    )
                    else -> it.copy(isLoading = false)
                }
            }
        }
    }

}
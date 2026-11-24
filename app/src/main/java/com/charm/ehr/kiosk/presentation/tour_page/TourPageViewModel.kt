package com.charm.ehr.kiosk.presentation.tour_page

import UiText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.common.SignInFailure
import com.charm.ehr.kiosk.domain.repository.MemberRepository
import com.charm.ehr.kiosk.domain.repository.PreferenceRepository
import com.charm.ehr.kiosk.domain.repository.WorkerRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TourPageViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
    private val preferenceRepository: PreferenceRepository,
    private val authManager: AuthManager,
    private val workerRepository: WorkerRepository
) : ViewModel() {

    private val _tourPageState: MutableStateFlow<TourPageState> = MutableStateFlow(TourPageState())
    val tourPageState: StateFlow<TourPageState> = _tourPageState.onStart {
        _tourPageState.update {
            it.copy(
                showEducationDialog = preferenceRepository.getKioskAccountDialogShown()
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000L),
        initialValue = _tourPageState.value
    )

    private val _refreshWorkId = MutableStateFlow<UUID?>(null)
    val refreshWorkId: StateFlow<UUID?> = _refreshWorkId

    fun getSettingsWithKioskCheck() {
        _tourPageState.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = memberRepository.fetchAndCacheSettings()
            if (result is APIState.Success) {
                if (result.data?.accountDetails?.accountType != "KIOSK") {
                    authManager.mdLogout()
                    _tourPageState.update {
                        it.copy(
                            isLoading = false,
                            showNoPrivilegeError = true
                        )
                    }
                } else {
                    updateLoadingState(true)
                    triggerRefreshServerWork(false)
                    workerRepository.scheduleDailyUpdateDataWork()
                }
            } else if (result is APIState.Error) {
                _tourPageState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.apiResponseError.errorMsg
                    )
                }
            }
        }
    }

    fun resetErrorState() {
        _tourPageState.update {
            it.copy(
                errorMessage = null
            )
        }
    }

    fun resetNoPrevilegeDialog() {
        _tourPageState.update {
            it.copy(
                showNoPrivilegeError = false
            )
        }
    }

    fun resetKioskTypeMismatchDialog() {
        _tourPageState.update {
            it.copy(
                showKioskTypeMismatchError = false,
                isLoggedIn = false
            )
        }
    }

    fun showLoginFailureError(failure: SignInFailure) {
        _tourPageState.update {
            it.copy(
                loginFailure = failure
            )
        }
    }

    fun hideLoginFailureError() {
        _tourPageState.update {
            it.copy(
                loginFailure = null
            )
        }
    }

    fun setKioskAccountDialogShown() {
        _tourPageState.update {
            it.copy(
                showEducationDialog = false
            )
        }
        preferenceRepository.setKioskAccountDialogShown()
    }

    fun updateLoadingState(isLoading: Boolean) {
        _tourPageState.update {
            it.copy(
                isLoading = isLoading
            )
        }
    }

    fun setIsUserLoggedIn(isLoggedIn: Boolean) {
        _tourPageState.update {
            it.copy(
                isLoggedIn = isLoggedIn
            )
        }
        if (!isLoggedIn) {
            // Back to the login screen: drop any setup still in progress for the old sign-in.
            clearRefreshWorkId()
            _tourPageState.update { it.copy(isLoading = false) }
        }
        if (isLoggedIn) {
//            val lastUpdatedTime =
//                preferenceRepository.getLastUpdatedTime()
//            if (lastUpdatedTime == 0L) {
            validateAndRefreshData()
//            } else {
//                _tourPageState.update {
//                    it.copy(
//                        isUserVerified = true
//                    )
//                }
//            }
        }
    }

    fun validateAndRefreshData() {
        _tourPageState.update {
            it.copy(isLoading = true)
        }
        // Get kioskSettings from preference
        val preferenceKioskSettings = preferenceRepository.getKioskSettings()
        val preferenceKioskType = preferenceKioskSettings?.accountDetails?.kioskType
        viewModelScope.launch(Dispatchers.IO) {
            val result = memberRepository.fetchAndCacheSettings()
            if (result is APIState.Success) {
                val currentKioskType = result.data?.accountDetails?.kioskType
                // Compare kioskType from preference with current response
                if (!preferenceKioskType.isNullOrBlank() && preferenceKioskType != currentKioskType) {
                    // KioskType mismatch - show error dialog and logout
                    authManager.mdLogout()
                    _tourPageState.update {
                        it.copy(
                            isLoading = false,
                            showKioskTypeMismatchError = true,
                        )
                    }
                } else {
                    // KioskType matches - proceed with refresh
                    triggerRefreshServerWork(false)
                }
            } else if (result is APIState.Error) {
                _tourPageState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.apiResponseError.errorMsg
                    )
                }
            }
        }
    }

    fun triggerRefreshServerWork(fetchSettings: Boolean) {
        _tourPageState.update {
            it.copy(
                isLoading = true
            )
        }
        _refreshWorkId.update {
            workerRepository.triggerUpdateDataWork(fetchSettings)
        }
    }

    fun clearRefreshWorkId() {
        _refreshWorkId.update {
            null
        }
    }

    fun onRefreshWorkCompleted(success: Boolean, error: String?, failedAPI: String?) {
        clearRefreshWorkId()
        if (success) {
            _tourPageState.update {
                it.copy(
                    isLoading = false,
                    isUserVerified = true,
                    updateDataErrorMessage = null,
                    updateDataFailedAPIs = null
                )
            }
            if (authManager.isStandAloneUser()) {
                authManager.selectedFacility = preferenceRepository.getSelectedFacility()
                authManager.selectedFacilityId = preferenceRepository.getSelectedFacilityId()
            }
        } else {
            _tourPageState.update {
                it.copy(
                    isLoading = false,
                    updateDataErrorMessage = UiText.DynamicString(error ?: ""),
                    updateDataFailedAPIs = failedAPI
                )
            }
        }
    }

    fun onRetryUpdateDataFailedCalls() {
        val failedAPIs = tourPageState.value.updateDataFailedAPIs
        if (failedAPIs.isNullOrEmpty()) {
            return
        }
        _tourPageState.update {
            it.copy(
                isLoading = true,
                updateDataErrorMessage = null,
                updateDataFailedAPIs = null
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val newWorkId = workerRepository.retryUpdateDataWork(failedAPIs, false)
            _refreshWorkId.emit(newWorkId)
        }
    }
}

package com.charm.ehr.kiosk.presentation.passcode

import UiText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charm.ehr.kiosk.BuildConfig
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.domain.repository.PreferenceRepository
import com.charm.ehr.kiosk.domain.usecase.auth.VerifyLoginPasswordUseCase
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
class PasscodeViewModel @Inject constructor(
    private val verifyLoginPasswordUseCase: VerifyLoginPasswordUseCase,
    private val authManager: AuthManager,
    val patientRepository: PatientRepository,
    val preferenceRepository: PreferenceRepository,
) :
    ViewModel() {

    private val _passcodeState: MutableStateFlow<PasscodeState> = MutableStateFlow(PasscodeState())
    val passcodeState: StateFlow<PasscodeState> = _passcodeState
        .onStart {
            _passcodeState.update {
                it.copy(
                    facilityName = authManager.selectedFacility?.facilityName ?: "",
                    loginName = authManager.kioskSettingsFlow.value?.accountDetails?.loginId ?: ""
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000L),
            initialValue = _passcodeState.value
        )

    var passcodeVerificationFlow: PasscodeVerificationFlow = PasscodeVerificationFlow.APP_LAUNCH

    fun setPasscodeVerification(flow: PasscodeVerificationFlow) {
        passcodeVerificationFlow = flow
    }

    fun onAction(action: PasscodeAction) {
        when (action) {

            is PasscodeAction.OnUnlock -> {

                if (BuildConfig.DEBUG) {
                    _passcodeState.update {
                        it.copy(isVerified = true)
                    }
                    return
                }

                val isValid = validatePasswordAndSetError()
                if (isValid) {
                    verifyLoginPassword(userName = action.userName, password = action.password)
                }
            }

            is PasscodeAction.OnShowPassword -> {
                _passcodeState.update {
                    it.copy(showPassword = !it.showPassword)
                }
            }

            is PasscodeAction.OnChangePassword -> {
                _passcodeState.update {
                    it.copy(password = action.password, errorMessage = null)
                }
            }

            is PasscodeAction.OnBiometricAuthSuccess -> {
                _passcodeState.update {
                    it.copy(isVerified = true)
                }
            }

            is PasscodeAction.OnBiometricAuthFailed -> {
                _passcodeState.update {
                    it.copy(
                        isVerified = false,
                        wrongPasswordAttempts = it.wrongPasswordAttempts + 1
                    )
                }
            }

            else -> {

            }
        }
    }

    fun validatePasswordAndSetError(): Boolean {
        if (passcodeState.value.password.isNotBlank()) {
            return true
        } else {
            _passcodeState.update {
                it.copy(errorMessage = UiText.StrResId(R.string.pleaseEnterPassword))
            }
            return false
        }
    }

    fun verifyLoginPassword(userName: String, password: String) {
        viewModelScope.launch {
            updateLoadingStatus(true)
            val result = verifyLoginPasswordUseCase(userName, password)
            updateLoadingStatus(false)
            _passcodeState.update {
                when {
                    result is APIState.Success && result.data -> it.copy(isVerified = true)
                    result is APIState.Success -> it.copy(
                        errorMessage = UiText.StrResId(R.string.incorrect_password),
                        isVerified = false,
                        wrongPasswordAttempts = it.wrongPasswordAttempts + 1
                    )
                    result is APIState.Error -> it.copy(
                        errorMessage = result.apiResponseError.errorMsg,
                        isVerified = false,
                        wrongPasswordAttempts = it.wrongPasswordAttempts + 1
                    )
                    else -> it
                }
            }
        }
    }

    private fun updateLoadingStatus(status: Boolean) {
        _passcodeState.update {
            it.copy(isLoading = status)
        }
    }

    fun updateIsFromIdleState(value: Boolean) {
        _passcodeState.update {
            it.copy(isFromIdleState = value)
        }
    }

    fun updateIsUserVerified(value: Boolean) {
        _passcodeState.update {
            it.copy(isVerified = value)
        }
    }

}

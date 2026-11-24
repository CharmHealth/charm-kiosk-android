package com.charm.ehr.kiosk.presentation.self_check_in

import UiText
import com.charm.ehr.kiosk.R
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charm.ehr.kiosk.common.util.MDAccountsRelatedUtil
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.domain.repository.OTPRepository
import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.common.util.CountryMobileCodes.countryCodeMap
import com.charm.ehr.kiosk.common.util.ModelUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
class SelfCheckInViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val otpRepository: OTPRepository,
    private val patientRepository: PatientRepository,
    private val authManager: AuthManager
) : ViewModel() {
    private var resendOtpTimerJob: Job? = null
    private var failedAPICall: (() -> Unit)? = null

    private val _selfCheckInState: MutableStateFlow<SelfCheckInState> =
        MutableStateFlow(SelfCheckInState())
    val selfCheckInState: StateFlow<SelfCheckInState> = _selfCheckInState.onStart {
        fetchCountries()
        _selfCheckInState.update {
            it.copy(
                countryCode = authManager.kioskSettingsFlow.value?.accessLocationCountryCode ?: "",
                mobileCountryCode = authManager.kioskSettingsFlow.value?.mobileCode ?: "",
                facilityName = authManager.selectedFacility?.facilityName ?: "",
                facilityLogoUrl = MDAccountsRelatedUtil.getFacilityLogoUrl(facilityId = authManager.selectedFacilityId)
            )
        }

    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000L),
        initialValue = _selfCheckInState.value
    )

    private val _noPatientAvailableDialog = MutableStateFlow(false)
    val noPatientAvailableDialog: StateFlow<Boolean> = _noPatientAvailableDialog

    private val _otpMaxAttemptDialog = MutableStateFlow(false)
    val otpMaxAttemptDialog: StateFlow<Boolean> = _otpMaxAttemptDialog

    private val _otpReSendMaxAttemptDialog = MutableStateFlow(false)
    val otpReSendMaxAttemptDialog: StateFlow<Boolean> = _otpReSendMaxAttemptDialog

    fun onAction(action: SelfCheckInAction) {
        when (action) {
            SelfCheckInAction.OnSendOTP -> {
                if (isValidMobileNumber()) {
                    val mobileNumber = selfCheckInState.value.mobileNumber
                    val messageKey = ModelUtil.selfCheckInMessageKey
                    val countryCode = selfCheckInState.value.countryCode
                    sendOtp(mobileNumber, messageKey, countryCode)
                }
            }

            is SelfCheckInAction.OnUpdateMobileNumber -> {
                _selfCheckInState.update {
                    it.copy(
                        mobileNumber = action.mobileNumber.take(
                            ModelUtil.getPhoneNumberMaxLength(
                                selfCheckInState.value.countryCode
                            )
                        ), validationErrorMessage = null
                    )
                }
            }

            is SelfCheckInAction.OnUpdateOTPNumber -> {
                _selfCheckInState.update {
                    it.copy(
                        otp = action.otp, validationErrorMessage = null
                    )
                }
            }

            SelfCheckInAction.OnVerifyOTP -> {
                if (isValidOtp()) {
                    val requestId = selfCheckInState.value.requestId
                    val otpNumber = selfCheckInState.value.otp
                    verifyOtp(requestId, otpNumber)
                }
            }

            SelfCheckInAction.OnResendOTP -> {
                if (isValidMobileNumber() && isValidTimeForResend()) {
                    resendOtp()
                }
            }

            SelfCheckInAction.OnClearMobileNumber -> {
                _selfCheckInState.update {
                    it.copy(mobileNumber = "", validationErrorMessage = null)
                }
            }

            SelfCheckInAction.OnClearApiErrorMessage -> {
                _selfCheckInState.update {
                    it.copy(apiErrorMessage = null)
                }
            }

            SelfCheckInAction.OnClearValidationErrorMessage -> {
                _selfCheckInState.update {
                    it.copy(validationErrorMessage = null)
                }
            }

            is SelfCheckInAction.OnCountryCodeSelected -> {
                _selfCheckInState.update {
                    it.copy(
                        countryCode = action.code,
                        mobileCountryCode = "+${countryCodeMap[action.code]}"
                    )
                }
            }

            SelfCheckInAction.OnSettingsClicked -> {

            }

            SelfCheckInAction.ResetOtpSentState -> {
                updateOtpSentState()
            }
        }
    }

    suspend fun updateLoadingState(isLoading: Boolean) {
        withContext(Dispatchers.Main) {
            _selfCheckInState.update {
                it.copy(isLoading = isLoading)
            }
        }
    }

    private fun isValidTimeForResend(): Boolean {
        return selfCheckInState.value.canResendOtp
    }

    fun isValidMobileNumber(): Boolean {
        val mobileNumber = selfCheckInState.value.mobileNumber
        //maxlength 30 allowed from API
        return if (ModelUtil.isValidMobileNumber(
                mobileNumber,
                selfCheckInState.value.countryCode
            )
        ) {
            true
        } else {
            _selfCheckInState.update {
                it.copy(validationErrorMessage = UiText.DynamicString("Please enter a valid mobile number."))
            }
            false
        }
    }

    fun isValidOtp(): Boolean {
        val otp = selfCheckInState.value.otp
        return if (otp.length == ModelUtil.charmOTPLength) {
            true
        } else if (otp.isBlank()) {
            _selfCheckInState.update {
                it.copy(validationErrorMessage = UiText.DynamicString("Please enter OTP to verify."))
            }
            false
        } else {
            _selfCheckInState.update {
                it.copy(validationErrorMessage = UiText.DynamicString("Please enter your ${ModelUtil.charmOTPLength} digits OTP."))
            }
            false
        }
    }

    //region API Calls

    fun sendOtp(mobileNumber: String, messageKey: String, countryCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateLoadingState(true)
            val result = otpRepository.sendOTP(mobileNumber, messageKey, countryCode)
            updateLoadingState(false)
            if (result is APIState.Success) {
                val reqId = result.data
                _selfCheckInState.update {
                    it.copy(
                        requestId = reqId,
                        isOtpSent = true,
                        canResendOtp = false
                    )
                }
                startResendTimer()
            } else if (result is APIState.Error) {
                failedAPICall = { sendOtp(mobileNumber, messageKey, countryCode) }
                _selfCheckInState.update {
                    it.copy(apiErrorMessage = result.apiResponseError.errorMsg)
                }
            }
        }
    }

    fun verifyOtp(requestId: String, otpNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateLoadingState(true)
            val result = otpRepository.verifyOTP(requestId, otpNumber)
            updateLoadingState(false)
            if (result is APIState.Success) {
                _selfCheckInState.update {
                    it.copy(isOtpVerified = true, apiErrorMessage = null)
                }
                searchPatientByMobileNumber()
            } else if (result is APIState.Error) {
                if (result.apiResponseError.charmApiCode == 6) {
                    val attemptsRemaining = _selfCheckInState.value.attemptsLeft - 1
                    _selfCheckInState.update {
                        it.copy(
                            validationErrorMessage = UiText.PluralStrResId(
                                id = R.plurals.incorrect_otp_with_remaining_attempt,
                                count = attemptsRemaining,
                                args = arrayOf(attemptsRemaining)
                            ),
                            attemptsLeft = it.attemptsLeft - 1,
                            otp = ""
                        )
                    }
                    if (_selfCheckInState.value.attemptsLeft <= 0) {
                        _otpMaxAttemptDialog.update { true }
                        reSetAttemptsLeft()
                    }
                } else if (result.apiResponseError.charmApiCode == 97) {
                    _selfCheckInState.update {
                        it.copy(validationErrorMessage = UiText.DynamicString("OTP expired. Please request a new OTP."))
                    }
                } else {
                    _selfCheckInState.update {
                        it.copy(apiErrorMessage = result.apiResponseError.errorMsg)
                    }
                }
            }
        }
    }

    fun reSetAttemptsLeft() {
        _selfCheckInState.update {
            it.copy(attemptsLeft = 4)
        }
    }

    fun resendOtp() {
        if (selfCheckInState.value.resendOtpAttemptLeft <= 0) {
            _otpReSendMaxAttemptDialog.update { true }
            return
        }
        _selfCheckInState.update {
            it.copy(
                validationErrorMessage = null,
                otp = "",
                attemptsLeft = 4
            )
        }
        resendOtp(
            requestId = selfCheckInState.value.requestId,
            messageKey = ModelUtil.selfCheckInMessageKey
        )
    }

    fun resendOtp(requestId: String, messageKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateLoadingState(true)
            val result = otpRepository.resendOTP(requestId, messageKey)
            updateLoadingState(false)
            if (result is APIState.Success) {
                _selfCheckInState.update { it.copy(canResendOtp = false) }
                startResendTimer()
            } else if (result is APIState.Error) {
                _selfCheckInState.update {
                    it.copy(apiErrorMessage = result.apiResponseError.errorMsg)
                }
            }
        }
    }

    fun searchPatientByMobileNumber() {
        val facilityId = authManager.selectedFacility?.facilityId ?: ""
        viewModelScope.launch(Dispatchers.IO) {
            updateLoadingState(true)
            val result = patientRepository.searchPatient(
                facilityId = facilityId, mobile = selfCheckInState.value.mobileNumber,
                recordId = null, name = null,
                email = null, dob = null
            )

            updateLoadingState(false)

            if (result is APIState.Success) {
                if (result.data.isNotEmpty()) {
                    _selfCheckInState.update {
                        it.copy(
                            patientList = result.data,
                        )
                    }
                } else {
                    setNoPatientAvailableDialog(true)
                }
            } else if (result is APIState.Error) {
                _selfCheckInState.update {
                    it.copy(
                        apiErrorMessage = result.apiResponseError.errorMsg
                    )
                }
            }
        }
    }

    fun setNoPatientAvailableDialog(isShow: Boolean) {
        _noPatientAvailableDialog.update { isShow }
    }

    fun updateOtpSentState() {
        _selfCheckInState.update {
            it.copy(
                isOtpSent = false,
                mobileNumber = "",
                validationErrorMessage = null,
                otp = "",
                attemptsLeft = ModelUtil.IncorrectOTPAttempts,
                resendOtpAttemptLeft = ModelUtil.OTPResendAttempts
            )
        }
    }

    fun setOtpMaxAttemptDialog(isShow: Boolean) {
        _otpMaxAttemptDialog.update { isShow }
    }

    fun otpReSendMaxAttemptDialog(isShow: Boolean) {
        _otpReSendMaxAttemptDialog.update { isShow }
    }

    fun onRetryFailureCall() {
        _selfCheckInState.update {
            it.copy(apiErrorMessage = null)
        }
        failedAPICall?.invoke()
    }

    //endregion

    fun startResendTimer() {
        resendOtpTimerJob?.cancel()
        _selfCheckInState.update {
            it.copy(
                canResendOtp = false,
                resendOtpTimeLeft = ModelUtil.OTPResendTimeSeconds,
                resendOtpAttemptLeft = it.resendOtpAttemptLeft - 1
            )
        }
        resendOtpTimerJob = viewModelScope.launch(Dispatchers.Default) {
            for (sec in ModelUtil.OTPResendTimeSeconds downTo 0) {
                _selfCheckInState.update { it.copy(resendOtpTimeLeft = sec) }
                delay(1000)
            }
            _selfCheckInState.update { it.copy(canResendOtp = true) }
        }
    }

    fun fetchCountries() {
        viewModelScope.launch(Dispatchers.IO) {
            updateLoadingState(true)
            val result = patientRepository.getCountries()
            updateLoadingState(false)
            if (result is APIState.Success) {
                _selfCheckInState.update {
                    it.copy(
                        countryList = result.data
                    )
                }
            } else if (result is APIState.Error) {
                failedAPICall = { fetchCountries() }
                _selfCheckInState.update {
                    it.copy(apiErrorMessage = result.apiResponseError.errorMsg)
                }
            }
        }
    }

}
package com.charm.ehr.kiosk.domain.repository

import com.charm.ehr.kiosk.presentation.composable.APIState

interface OTPRepository {
    suspend fun sendOTP(mobileNumber: String, messageKey: String,countryCode:String): APIState<String>
    suspend fun resendOTP(requestId: String, messageKey: String): APIState<Boolean>
    suspend fun verifyOTP(requestId: String, otpNumber: String): APIState<Boolean>
}

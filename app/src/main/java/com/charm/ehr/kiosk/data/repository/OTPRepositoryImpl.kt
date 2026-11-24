package com.charm.ehr.kiosk.data.repository
import com.charm.ehr.kiosk.domain.repository.OTPRepository

import com.charm.ehr.kiosk.common.util.MdJsonBuilder
import com.charm.ehr.kiosk.data.network.WebService
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.common.util.CharmAPI
import javax.inject.Inject

class OTPRepositoryImpl @Inject constructor(
    val webService: WebService,
    private val charmAPI: CharmAPI
)  : OTPRepository{

    override suspend fun sendOTP(mobileNumber: String, messageKey: String,countryCode:String): APIState<String> {
        return charmAPI.executeApiCall(
            call = { webService.sendOtp(MdJsonBuilder.formSendOTPObj(mobileNumber, messageKey,countryCode)) },
            onSuccess = {
                val output = it.get("output_string")?.asJsonObject
                output?.get("request_id")?.asString ?: ""
            }
        )
    }

    override suspend fun resendOTP(requestId: String, messageKey: String): APIState<Boolean> {
        return charmAPI.executeApiCall(
            call = { webService.reSendOtp(MdJsonBuilder.formResendOTPObj(requestId, messageKey)) },
            onSuccess = { true }
        )
    }

    override suspend fun verifyOTP(requestId: String, otpNumber: String): APIState<Boolean> {
        return charmAPI.executeApiCall(
            call = { webService.verifyOtp(MdJsonBuilder.formVerifyOTPObj(requestId, otpNumber)) },
            onSuccess = { true }
        )
    }
}
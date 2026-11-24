package com.charm.ehr.kiosk.common.util

import com.charm.ehr.kiosk.BuildConfig

class MDAccountsRelatedUtil {
    companion object {

        const val TAG = "AccountsUtil"

        /** The EHR server the kiosk calls (secure.properties charmAuth.ehrBaseUrl). */
        val baseUrl: String = BuildConfig.EHR_BASE_URL.trimEnd('/')

        val facilityLogoUrl = "$baseUrl/api/ehr/v1/facilities/logo?facility_id="
        val patientPhotoUrlPart1 = "$baseUrl/api/ehr/facilities/patients/photo?patientId="

        const val photoSize120 = "&photoSize=PHOTO_120"
        const val photoSize60 = "&photoSize=PHOTO_60"

        const val IS_USER_SUCCESSFULLY_LOGGED_IN = "isUserSuccessfullyLoggedId"

        const val privacyPolicyUrl = "https://www.charmhealth.com/privacy-policy.html"
        const val termsOfServicesUrl = "https://www.charmhealth.com/ehr/termsofservice.html"

        /** The EHR's "invalid auth token" response code. */
        const val INVALID_AUTHTOKEN_ERROR_CODE = 14
        const val DUPLICATE_PATIENT_ERROR_CODE = "48"

        fun getPatientPhotoUrl(patientId: String?, photoSize:String = photoSize60): String {
            return "$patientPhotoUrlPart1$patientId$photoSize"
        }

        fun getFacilityLogoUrl(facilityId: String?): String {
            return "$facilityLogoUrl$facilityId"
        }

    }
}

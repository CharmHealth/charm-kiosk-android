package com.charm.ehr.kiosk.presentation.navigation

import com.charm.ehr.kiosk.presentation.patient_profile.Section
import com.charm.ehr.kiosk.common.util.SecurityUtil
import kotlinx.serialization.Serializable

sealed interface MdRoute {

    @Serializable
    data object Login : MdRoute

    @Serializable
    data object Tour : MdRoute

    @Serializable
    data class Passcode(
        val fromIdleState: Boolean = false,
        val createPasscode: Boolean = false,
        val selectAuthType: SecurityUtil.AuthType? = null,
        val isFromSetting: Boolean = false,
        val isFromVerification : Boolean = false
    ) : MdRoute

    @Serializable
    data class SelectFacility(val fromSettings: Boolean = false) : MdRoute

    @Serializable
    data object Dashboard : MdRoute

    @Serializable
    data class PatientProfileModuleHost(val patientId: String, val isSelfCheckInFlow: Boolean) :
        MdRoute

    @Serializable
    data class PatientProfile(val patientId: String, val isSelfCheckInFlow: Boolean) : MdRoute

    @Serializable
    data object SearchTemplate : MdRoute

    @Serializable
    data object VerifyDob : MdRoute

    @Serializable
    data object TemplateList : MdRoute

    @Serializable
    data object QuestionnaireDetail : MdRoute

    @Serializable
    data object ThankYouScreen : MdRoute

    @Serializable
    data class Settings(val isFromLogin: Boolean = false) : MdRoute

    @Serializable
    data object SelectPatient : MdRoute

    @Serializable
    data object SelfChekInModuleHost : MdRoute

    @Serializable
    data object SelfChekIn : MdRoute

    @Serializable
    data class EditProfile(val selectedSection: Section?) : MdRoute

    @Serializable
    data object Support : MdRoute

    @Serializable
    data object SelectGender : MdRoute

    @Serializable
    data object DashboardHost : MdRoute

    @Serializable
    data class AuthTypeSelect(val isFromSetting: Boolean = false) : MdRoute

    @Serializable
    data class SimpleWebView(val heading: String, val url: String) : MdRoute

    @Serializable
    data object SelfCheckInDashboard : MdRoute

}

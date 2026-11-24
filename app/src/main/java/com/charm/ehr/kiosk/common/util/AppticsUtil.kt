package com.charm.ehr.kiosk.common.util

import android.app.Application
import com.zoho.apptics.analytics.AppticsEvents
import com.zoho.apptics.common.Apptics
import com.zoho.apptics.logger.AppticsLogger
import com.zoho.apptics.rateus.AppticsInAppRatings

object AppticsUtil {

    const val TAG = "AppticsUtil"

    fun addEvent(mdEvent: MdEvent, mdEventGroup: MdEventGroup) {
        AppticsEvents.addEvent(mdEvent.value, mdEventGroup.value)
    }

    private fun setInAppRatingCustomUiCallback(application: Application) {
//        AppticsInAppRatings.customUiCallback = { _, criteriaId ->
//            val pref = PreferenceManager.getDefaultSharedPreferences(application.applicationContext)
//            pref.edit().putLong(ModelUtil.isInAppRatingTriggered, criteriaId).commit()
//        }
    }

    fun sendStats(
        criteriaId: Long,
        popupAction: AppticsInAppRatings.PopupAction,
        popupSource: AppticsInAppRatings.PopupSource
    ) {
        AppticsInAppRatings.sendStats(criteriaId)
    }

    private fun enableAppticsLogger(isEnable: Boolean) {
        if (AppticsLogger.isEnabled() && !isEnable) {
            AppticsLogger.disable()
        } else if (!AppticsLogger.isEnabled() && isEnable) {
            AppticsLogger.enable()
        }
    }

    fun initApptics(application: Application) {
        setInAppRatingCustomUiCallback(application)
        Apptics.init(application)
        enableAppticsLogger(true)
    }

}

enum class MdEventGroup(val value: String) {
    Account("Account"),
    Support("Support"),
    Patient("Patient")
}

enum class MdEvent(val value: String) {
    LoginOnSuccess("LoginOnSuccess"),
    LoginOnFailure("LoginOnFailure"),

    LogoutOnSuccess("LogoutOnSuccess"),
    LogoutOnFailure("LogoutOnFailure"),

    LogoutOnKeyStoreCorrupt("LogoutOnKeyStoreCorrupt"),

    OnSendSupportSuccess("onSendSupportSuccess"),
    OnSendSupportFailure("onSendSupportFailure"),

    OnAddPatientSuccess("onAddPatientSuccess"),
    OnAddPatientFailure("onAddPatientFailure"),
    OnHandOverToPatient("onHandOverToPatient")
}


package com.charm.ehr.kiosk.common.util

import android.app.Activity
import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.preference.PreferenceManager
import com.charm.ehr.kiosk.R

object SecurityUtil {

    private val TAG = "SecurityUtil"

    private const val NO_LOCAL_AUTHENTICATION_ERROR_CODE = 100013

    enum class AuthType {
        SYSTEM_LOCK, APP_PIN, ACCOUNT_CREDENTIALS
    }

    fun Context.saveAppLockType(lockType: AuthType) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        pref.edit().putString(ModelUtil.applockType, lockType.name).commit()
    }

    fun Context.isLocalAuthConfigured(): Boolean {
        val keyguardManager = this.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isKeyguardSecure
    }

    fun Context.presentLockScreen(
        biometricPrompt: BiometricPrompt,
        title: String = getString(R.string.unlock) + " " + getString(R.string.app_name),
        description: String = ""
    ): Boolean {

        return if (this.isLocalAuthConfigured()) {
            biometricPrompt.authenticate(getPromptInfo(title, description))
            true
        } else {
            NO_LOCAL_AUTHENTICATION_ERROR_CODE
            false
        }
    }

    private fun getPromptInfo(
        title: String,
        description: String = ""
    ): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setDeviceCredentialAllowed(true)
            .build()
    }

    //Methods for forgot PIN
//
//    /**
//     * Called every time user enters wrong password(false from verify user call)
//     */
//    @SuppressLint("ApplySharedPref")
//    fun Context.wrongResetPasswordAttempt() {
//
//        val pref = PreferenceManager.getDefaultSharedPreferences(this)
//        var failedAttempts = pref.getInt(ModelUtil.numberOfWrongAttemptsToReset, 0)
//
//        failedAttempts += 1
//
//        val nowTime = DateTime.now()
//
//        var pauseTime: DateTime? = null
//        when (failedAttempts) {
//
//            0, 1, 2, 3 -> null
//
//            4 -> pauseTime = nowTime.plusMinutes(5)
//
//            5 -> pauseTime = nowTime.plusMinutes(20)
//
//            else -> {
//                val extraDays = ((failedAttempts - 5) * 1.9).toInt()
//                pauseTime = nowTime.plusDays(extraDays)
//            }
//
//        }
//
//        pref.edit().putInt(ModelUtil.numberOfWrongAttemptsToReset, failedAttempts).commit()
//
//        pauseTime ?: return
//        pref.edit().putLong(ModelUtil.pauseTimeInMillisForResetPin, pauseTime.millis).commit()
//
//    }
//
//    /**
//     * Called every time when user clicks forgot password
//     */
//    fun Context.checkForForgetPassword(): String? {
//
//        val pref = PreferenceManager.getDefaultSharedPreferences(this)
//        val millis = pref.getLong(ModelUtil.pauseTimeInMillisForResetPin, -1L)
//
//        if (millis == -1L) return null
//
//        val pauseTime = DateTime(millis)
//
//        var str = this.getString(R.string.too_many_attempts) + " "
//
//        if (pauseTime.isAfterNow) {
//
//            str += if (ModelUtil.isSameYear(pauseTime, DateTime.now())) {
//
//                if (ModelUtil.isSameDate(pauseTime, DateTime.now())) {
//                    pauseTime.toString(ModelUtil.charmDisplayTimeFormat())
//                } else pauseTime.toString(ModelUtil.monthDayTime12HrWithoutYear())
//
//            } else pauseTime.toString(ModelUtil.charmFlowSheetDataDateFormatter())
//
//            return str
//        }
//
//        return null
//
//    }

    //Enable app pinning during kiosk mode
    fun enableAppPinning(activity: Activity) {
        try {
            val applicationContext = activity.applicationContext
            val activityManager =
                applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (activityManager.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
                activity.startLockTask()
            }
        } catch (e: Exception) {
            Log.d(TAG, "enableAppPinning Exception: ${e.toString()}")
        }
    }

    fun disableAppPinning(activity: Activity) {
        try {
            activity.stopLockTask()
        } catch (e: Exception) {
            Log.d(TAG, "disableAppPinning Exception: ${e.toString()}")
        }

    }

    fun Context.getAppLockType(): AuthType? {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val lockTypeString = pref.getString(ModelUtil.applockType, null)
        return if (lockTypeString == null && !this.isLocalAuthConfigured()) {
            try {
                AuthType.ACCOUNT_CREDENTIALS
            } catch (e: IllegalArgumentException) {
                null
            }
        }else if(lockTypeString == null){
            null
        }else {
            AuthType.valueOf(lockTypeString)
        }
    }

}
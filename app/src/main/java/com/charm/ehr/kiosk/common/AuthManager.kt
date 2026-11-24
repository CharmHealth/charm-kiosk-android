package com.charm.ehr.kiosk.common

import UiText
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import androidx.core.content.edit
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.charm.ehr.kiosk.domain.APIResponseError
import com.charm.ehr.kiosk.domain.Facility
import com.charm.ehr.kiosk.domain.KioskSettings
import com.charm.ehr.kiosk.common.util.MDAccountsRelatedUtil.Companion.INVALID_AUTHTOKEN_ERROR_CODE
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.constants.PrefConstants
import com.charm.ehr.kiosk.common.util.AppticsUtil
import com.charm.ehr.kiosk.common.util.MdEvent
import com.charm.ehr.kiosk.common.util.MdEventGroup
import com.charm.ehr.kiosk.dataStore
import com.charm.ehr.kiosk.common.util.MediaUtil
import com.charm.ehr.kiosk.common.util.ModelUtil
import com.charm.ehr.kiosk.common.util.ModelUtil.Companion.isNotJsonNull
import com.charm.ehr.kiosk.common.util.ModelUtil.Companion.parseStringAsJsonObject
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.charm.ehr.kiosk.common.util.AuthenticationUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

enum class UserStatus {
    ONLINE, OFFLINE
}

enum class PatientProfileFlow {
    BEFORE_HANDOVER,
    AFTER_HANDOVER,
    SELF_CHECKIN
}

/** How a sign-in ended, for the login screen. */
sealed interface SignInResult {
    data object Success : SignInResult

    /** The user closed the sign-in page, or one was already open: nothing to report. */
    data object Ignored : SignInResult
    data class Failed(val failure: SignInFailure) : SignInResult
}

enum class SignInFailure {
    /** The account was refused (inactive/blocked user, no access): retrying as them won't help. */
    ACCOUNT_REFUSED,

    /** Network down or server busy: worth retrying. */
    UNREACHABLE,

    /** The sign-in page was left open too long. */
    EXPIRED,
    OTHER
}

enum class KioskUserType(val displayName: String) {
    TABLET_USER("Kiosk Tablet User"),
    STANDALONE_USER("Kiosk Standalone User") //Self check-in kiosk
}

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val preferences: SharedPreferences,
    private val authenticationUtil: AuthenticationUtil,
) {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** The SDK sign-out started by [mdLogout]; a new sign-in waits for it to finish. */
    @Volatile
    private var signOutJob: Job? = null

    /**
     * Set once the "session ended" sign-out starts, so the API calls that fail together (each one
     * reports the ended session) and the SDK's state change sign out and alert once, not once
     * each. Cleared by a successful sign-in.
     */
    private val isEndingSession = AtomicBoolean(false)

    private val _sessionEndedAlert = MutableStateFlow(false)

    /** True while the "Session expired" alert should show (MainActivity). */
    val sessionEndedAlert: StateFlow<Boolean> = _sessionEndedAlert

    private val _userStatusStateFlow: MutableStateFlow<UserStatus> =
        MutableStateFlow(UserStatus.OFFLINE)
    val userStatusStateFlow: StateFlow<UserStatus> = _userStatusStateFlow

    private val _workFlowType: MutableStateFlow<PatientProfileFlow> =
        MutableStateFlow(PatientProfileFlow.BEFORE_HANDOVER)
    val workFlowType: StateFlow<PatientProfileFlow> = _workFlowType

    private val _requireTourNavigation = MutableStateFlow(false)
    val requireTourNavigation: StateFlow<Boolean> = _requireTourNavigation

    val TAG = "AuthManager"

    var reachability: Boolean = false
        set(value) {
            //
            field = value
            updateAuthStatus()
        }

    val kioskSettingsFlow: MutableStateFlow<KioskSettings?> = MutableStateFlow(null)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            kioskSettingsFlow.value = getKioskSettings()
        }
    }

    fun isStandAloneUser(): Boolean {
        return kioskSettingsFlow.value?.accountDetails?.kioskType == KioskUserType.STANDALONE_USER.displayName
    }

    suspend fun getKioskSettings(): KioskSettings? = withContext(Dispatchers.IO) {
        return@withContext try {
            val json = preferences.getString(ModelUtil.kioskSettings, null)
            if (!json.isNullOrBlank()) {
                Gson().fromJson(json, KioskSettings::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
            null
        }
    }

    suspend fun setKioskSettings(value: KioskSettings?) = withContext(Dispatchers.IO) {
        try {
            kioskSettingsFlow.value = value
            val json = if (value != null) {
                Gson().toJson(value)
            } else {
                null
            }
            preferences.edit(commit = true) {
                putString(ModelUtil.kioskSettings, json)
            }
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }

    var selectedFacility: Facility? = null
        set(value) {
            field = value
            val json = if (value != null) Gson().toJson(value) else null
            preferences.edit(commit = true) {
                putString(ModelUtil.selectedFacility, json)
            }
        }

    var selectedFacilityId: String? = null
        set(value) {
            field = value
            preferences.edit(commit = true) {
                putString(ModelUtil.selectedFacilityId, value)
            }
        }

    val networkConnectionsMap = mutableMapOf<String, Boolean>()

    init {
        initNetworkCallback()
        appScope.launch {
            authenticationUtil.sessionEndedByServer.collect {
                Log.d(TAG, "sign-in ended by the server")
                onSessionEnded()
            }
        }

        selectedFacilityId = preferences.getString(ModelUtil.selectedFacilityId, "")
        // Restore selectedFacility from SharedPreferences
        val facilityJson = preferences.getString(ModelUtil.selectedFacility, null)
        selectedFacility = if (!facilityJson.isNullOrBlank()) Gson().fromJson(
            facilityJson,
            Facility::class.java
        ) else null
    }

    fun updateAuthStatus() {
        val status = if (reachability) UserStatus.ONLINE else UserStatus.OFFLINE
        _userStatusStateFlow.value = status
    }

    //region Sign-in (CharmAuth SDK)

    /**
     * Loads the stored sign-in once per process (no network, unless it completes a sign-in the OS
     * interrupted: the app was ended while the sign-in page was open).
     */
    private val restored = appScope.async(start = CoroutineStart.LAZY) {
        try {
            authenticationUtil.restoreSession()
        } catch (e: Exception) {
            Log.d(TAG, "restore failed: ${e::class.simpleName}")
            false
        }
    }

    /** Starts loading the stored sign-in (KioskApplication.onCreate). */
    fun startRestoringSession() {
        restored.start()
    }

    /**
     * Waits until the stored sign-in is loaded; true when signed in. Every API call waits for it,
     * so one made right after the process starts (e.g. by WorkManager) doesn't find no sign-in.
     */
    suspend fun awaitSessionRestored(): Boolean = restored.await()

    private val startupHandled = AtomicBoolean(false)

    /**
     * For the first screen of this process: true when the kiosk is signed in and set up. A sign-in
     * that restore() completed but the kiosk never set up (the process ended during sign-in) is
     * reported through [signInResult] as a Success, so the login screen carries on with it.
     */
    suspend fun checkLoggedInAtStartup(): Boolean {
        val signedIn = awaitSessionRestored()
        val setUp = preferences.getBoolean(PrefConstants.isUserLoggedIn, false)
        if (signedIn && !setUp && startupHandled.compareAndSet(false, true)) {
            onSignInSucceeded()
            _signInResult.value = SignInResult.Success
        }
        return signedIn && setUp
    }

    private val _signInResult = MutableStateFlow<SignInResult?>(null)

    /** The latest sign-in's outcome, until [consumeSignInResult]. */
    val signInResult: StateFlow<SignInResult?> = _signInResult

    fun consumeSignInResult() {
        _signInResult.value = null
    }

    /**
     * Signs in through a Custom Tab; the outcome arrives in [signInResult]. Runs in the app's scope,
     * not the activity's: the activity can be destroyed while the Custom Tab is open.
     */
    fun startSignIn(activity: Activity) {
        appScope.launch {
            val result = signIn(activity)
            if (result != SignInResult.Ignored || _signInResult.value == null) _signInResult.value = result
        }
    }

    private suspend fun signIn(activity: Activity): SignInResult {
        signOutJob?.join()
        awaitSessionRestored()
        val result = authenticationUtil.signIn(activity)
        when (result) {
            SignInResult.Success -> {
                onSignInSucceeded()
                AppticsUtil.addEvent(MdEvent.LoginOnSuccess, MdEventGroup.Account)
            }
            is SignInResult.Failed -> AppticsUtil.addEvent(MdEvent.LoginOnFailure, MdEventGroup.Account)
            SignInResult.Ignored -> {}
        }
        return result
    }

    /** Re-arms the "session ended" sign-out. Also called when restore() completed a sign-in. */
    fun onSignInSucceeded() {
        isEndingSession.set(false)
    }

    /**
     * The sign-in has ended (server ended it, nobody signed in, or the API refuses the user even
     * with a fresh token): an unattended kiosk can't recover, so sign out, alert once and show the
     * login screen - once, however many calls report it together.
     */
    fun onSessionEnded() {
        if (!isEndingSession.compareAndSet(false, true)) return
        mdLogout()
        _sessionEndedAlert.value = true
    }

    fun dismissSessionEndedAlert() {
        _sessionEndedAlert.value = false
    }

    /** Signs out at CHIS (revokes the refresh token, clears the SDK's sign-in) and clears the kiosk's data. */
    fun mdLogout() {
        signOutJob = appScope.launch {
            try {
                authenticationUtil.signOut()
                AppticsUtil.addEvent(MdEvent.LogoutOnSuccess, MdEventGroup.Account)
            } catch (e: Exception) {
                AppticsUtil.addEvent(MdEvent.LogoutOnFailure, MdEventGroup.Account)
                Log.d(TAG, "signOut failed: ${e::class.simpleName}")
            }
        }
        try {
            afterLogoutOperation()
        } catch (e: Exception) {
            Log.d(TAG, Log.getStackTraceString(e))
        }
    }

    //endregion

    fun afterLogoutOperation() {
        clearAllPreference()
        CoroutineScope(Dispatchers.IO).launch {
            clearCachedString()
        }
        _requireTourNavigation.value = true
        _workFlowType.update {
            PatientProfileFlow.BEFORE_HANDOVER
        }
        kioskSettingsFlow.value = null
        selectedFacility = null
        selectedFacilityId = null
    }

    fun setRequireTourNavigation() {
        _requireTourNavigation.value = false
    }

    fun initNetworkCallback() {
        val connectivityManager = appContext.getSystemService(ConnectivityManager::class.java)
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, " initNetworkCallback onAvailable")
                networkConnectionsMap[network.toString()] = true
                checkAndUpdateInternetStatus()
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "initNetworkCallback onLost")
                networkConnectionsMap.remove(network.toString())
                checkAndUpdateInternetStatus()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                Log.d(TAG, "initNetworkCallback onCapabilitiesChanged")
                checkAndUpdateInternetStatus()
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                Log.d(TAG, "initNetworkCallback onLinkPropertiesChanged")
            }
        })
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = appContext.getSystemService(ConnectivityManager::class.java)
            ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun checkAndUpdateInternetStatus() {
        val isAtLeastOneNetworkActive = networkConnectionsMap.values.any { it }
        if (isAtLeastOneNetworkActive && isInternetAvailable()) {
            Log.d(TAG, "updateAuthStatus UserStatus.ONLINE")
            _userStatusStateFlow.value = UserStatus.ONLINE
        } else {
            Log.d(TAG, "updateAuthStatus UserStatus.OFFLINE")
            _userStatusStateFlow.value = UserStatus.OFFLINE
        }
    }

    fun clearAllPreference() {
        preferences.edit().apply {
            remove(ModelUtil.applockType)
            remove(PrefConstants.defaultGender)
            remove(PrefConstants.lastUpdatedDate)
            remove(MediaUtil.isCameraPermissionDenied)
            remove(PrefConstants.isUserLoggedIn)
            remove(ModelUtil.kioskSettings)
            remove(ModelUtil.selectedFacility)
            remove(ModelUtil.selectedFacilityId)
            commit()
        }
    }

    suspend fun clearCachedString() {
        appContext.dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(ModelUtil.demographicsMetrics))
            preferences.remove(stringPreferencesKey(ModelUtil.demographicsSetting))
            preferences.remove(stringPreferencesKey(ModelUtil.countries))
            preferences.remove(stringPreferencesKey(ModelUtil.states + "_" + ModelUtil.countryStringIndia))
            preferences.remove(stringPreferencesKey(ModelUtil.states + "_" + ModelUtil.countryStringUs))
            preferences.remove(stringPreferencesKey(ModelUtil.states + "_" + ModelUtil.countryStringAustralia))
        }
    }

    /**
     * Handles a pre-parsed error body JsonObject so the response stream is only consumed once
     * (by the caller in CharmAPI.executeApiCall). Handles global concerns like auth-token expiry;
     * API-specific error codes (e.g. duplicate patient) are left to the individual repository's
     * onError lambda.
     */
    fun apiErrorResponseHandeler(errorBodyJson: JsonObject?): APIResponseError? {
        return try {
            Log.d("MdCallback", "jsonObject:$errorBodyJson")
            val code = errorBodyJson?.get("code")?.asInt

            if (code == INVALID_AUTHTOKEN_ERROR_CODE) {
                onSessionEnded()
                null
            } else {
                APIResponseError(
                    code,
                    null,
                    UiText.DynamicString(errorBodyJson?.get("message")?.asString ?: "")
                )
            }
        } catch (e: Exception) {
            APIResponseError(null, null, UiText.DynamicString(ModelUtil.unknownError))
        }
    }

    fun getFacilityId(): String {
        return selectedFacilityId ?: ""
    }

//    fun synchronizeAPICall(webCall: Call<JsonElement>): Any? {
//        try {
//            val webResp = webCall.execute()
//            if (webResp.isSuccessful) {
//                val responseBody = webResp.body()?.asJsonObject
//                val code = responseBody?.get("code")?.asInt
//                val success = responseBody?.get("error")?.asBoolean == false
//                return if ((code == 0 || success)) {
//                    responseBody
//                } else {
//                    APIResponseError(
//                        code,
//                        webResp.code(),
//                        responseBody?.get("message")?.asString
//                            ?: responseBody?.get("error_message")?.asString
//                    )
//                }
//            } else {
//
//                return apiErrorResponseHandeler(webResp) ?: APIResponseError(
//                    null, null, appContext.getString(R.string.conversionError)
//                )
//
//            }
//
//        } catch (e: Exception) {
//            return APIResponseError(
//                null, null, when (e) {
//                    is IOException -> appContext.getString(R.string.ioException)
//                    is SocketTimeoutException -> appContext.getString(R.string.socketTimeoutException)
//                    else -> appContext.getString(R.string.conversionError)
//                }
//            )
//        }
//    }

    fun apiErrorResponseHandlerForResponseBody(
        response: retrofit2.Response<ResponseBody>, errorRespCode: Int? = null
    ): APIResponseError {
        //when response is unSuccessful

        var apiResp: APIResponseError

        try {

            if (response.isSuccessful) {

                if (response.raw().header("content-type")
                        ?.contains("application/json", ignoreCase = true) == true
                ) {

                    val responseObj = response.body()?.string().parseStringAsJsonObject()

                    val code = responseObj?.get("code")?.asInt
                    if (code == INVALID_AUTHTOKEN_ERROR_CODE) { //INVALID AUTHTOKEN CODE

                        onSessionEnded()

                        apiResp = APIResponseError(null, null, null)

                    } else apiResp = APIResponseError(
                        code,
                        response.code(),
                        UiText.DynamicString(responseObj?.get("message")?.asString ?: "")
                    )
                } else {
                    apiResp = APIResponseError(null, null, null)
                }

            } else {

                val errorBody = response.errorBody()?.string().parseStringAsJsonObject()
                var jsonObject: JsonObject? = null
                if (errorBody.isNotJsonNull()) {
                    jsonObject = errorBody?.asJsonObject
                }

                val code = if (errorRespCode != null) {
                    errorRespCode
                } else {
                    jsonObject?.get("code")?.asInt

                }

                if (code == INVALID_AUTHTOKEN_ERROR_CODE) { //INVALID AUTHTOKEN CODE

                    onSessionEnded()

                    apiResp = APIResponseError(code, null, null)

                } else apiResp = APIResponseError(code, null, null)

            }

        } catch (e: Exception) {
            apiResp = APIResponseError(null, null, UiText.StrResId(R.string.something_went_wrong))
        }

        return apiResp

    }

    fun showInsuranceModule(): Boolean {
        //hide insurance module for India
        if (kioskSettingsFlow.value?.country == ModelUtil.countryStringIndia) {
            return false
        }
        return (kioskSettingsFlow.value?.accountDetails?.kioskPreferences?.insuranceRequired == true)
    }

    fun showEditPatient(): Boolean {
        return (kioskSettingsFlow.value?.accountDetails?.kioskPreferences?.patientDemographicsEditRequired == true)
    }

    fun showQuestionnaireModule(): Boolean {
        return (kioskSettingsFlow.value?.accountDetails?.kioskPreferences?.questionnairesRequired == true)
    }

    fun showConsentFormsModule(): Boolean {
        return (kioskSettingsFlow.value?.accountDetails?.kioskPreferences?.consentFormsRequired == true) && !ModelUtil.hideConsentForm
    }

    fun setWorkFlowType(type: PatientProfileFlow) {
        if (type == PatientProfileFlow.AFTER_HANDOVER) {
            AppticsUtil.addEvent(MdEvent.OnHandOverToPatient, MdEventGroup.Patient)
        }
        _workFlowType.update {
            type
        }
    }


}

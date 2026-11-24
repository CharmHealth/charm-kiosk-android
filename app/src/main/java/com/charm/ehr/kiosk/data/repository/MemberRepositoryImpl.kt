package com.charm.ehr.kiosk.data.repository
import com.charm.ehr.kiosk.domain.repository.MemberRepository

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.charm.ehr.kiosk.domain.AppointmentStatus
import com.charm.ehr.kiosk.BuildConfig
import com.charm.ehr.kiosk.domain.KioskSettings
import com.charm.ehr.kiosk.common.util.MdJsonBuilder
import com.charm.ehr.kiosk.common.util.MdParsingHelper
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.dataStore
import com.charm.ehr.kiosk.data.network.WebService
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.common.util.CharmAPI
import com.charm.ehr.kiosk.common.util.FileUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MemberRepositoryImpl @Inject constructor(
    private val webService: WebService,
    private val authManager: AuthManager,
    @ApplicationContext   private val appContext: Context,
    private val charmAPI: CharmAPI
)  : MemberRepository{

    private val TAG = "MemberRepository"
    //region Accounts

    override suspend fun fetchAndCacheSettings(): APIState<KioskSettings?> {
        val settingsState = charmAPI.executeApiCall(
            call = {
                webService.fetchSettings()
            },
            onSuccess = { respObj ->
                MdParsingHelper.formKioskSettings(respObj)
            })

        if (settingsState is APIState.Success && settingsState.data != null) {
            val appointmentStatusState = getAppointmentStatus()
            if (appointmentStatusState is APIState.Success) {
                val appointmentStatus =
                    appointmentStatusState.data.firstOrNull { appointmentStatus ->
                        appointmentStatus.statusId == settingsState.data.accountDetails?.kioskPreferences?.appointmentStatus?.statusId
                    }
                settingsState.data.accountDetails?.kioskPreferences?.appointmentStatus =
                    appointmentStatus
            }
            authManager.setKioskSettings(settingsState.data)
        }
        return settingsState
    }

    override suspend fun getAppointmentStatus(): APIState<ArrayList<AppointmentStatus>> {
        return charmAPI.executeApiCall(
            call = { webService.getAppointmentStatus() },
            onSuccess = { respObj ->
                MdParsingHelper.getAppointmentStatusFromResponse(respObj)
            }
        )
    }

//    fun fetchMembersList(onlineAppointmentsEnabled: Boolean? = null): StateFlow<APIState<List<Member>>> {
//        val fetchMemberListState = MutableStateFlow<APIState<List<Member>>>(APIState.Loading)
//
//        val fetchMemberList = webService.fetchMembers(authManager.getFacilityId(), onlineAppointmentsEnabled = onlineAppointmentsEnabled)
//
//        apiUtil.initiateRequest(fetchMemberList, object : MdNetworkUtil.MdCallback(authManager) {
//            fun onMdSuccess(respObj: JsonObject) {
//                val code = respObj.get("code")?.asInt
//                val message = respObj.get("message")?.asString
//                val respMemberArr = respObj.get("members")?.asJsonArray
//                if (respMemberArr != null) {
//                    fetchMemberListState.value = APIState.Success(MdParsingHelper.formMemberList(respMemberArr))
//                } else {
//                    fetchMemberListState.value = APIState.Error(APIResponseError(code, null, message))
//                }
//            }
//
//            fun onMdFailure(error: APIResponseError) {
//                fetchMemberListState.value = APIState.Error(error)
//            }
//        })
//
//        return fetchMemberListState
//    }

//    fun getAvailableSlots(
//        member_id: String, fromDate: DateTime, toDate: DateTime, facilityId: String, timeSlotInMinutes: Int
//    ): StateFlow<APIState<Any>> {
//
//        val availabilitySlotsCall = webService.getAvailableSlots(
//            member_id,
//            fromDate.toString(ModelUtil.charmApiDateFormat()),
//            toDate.toString(ModelUtil.charmApiDateFormat()),
//            facilityId,
//            timeSlotInMinutes.toString()
//        )
//
//        return charmAPI.initiateApiRequest(
//            call = availabilitySlotsCall, onSuccess = { respObj ->
//                val code = respObj.asJsonObject.get("code")?.asInt
//                val message = respObj.asJsonObject.get("message")?.asString
//                val availabilityObj = respObj.asJsonObject.get("availability")?.asJsonObject
//                if (availabilityObj != null) {
//                    val successResponse =
//                        MdParsingHelper.formMembersAvailabilitySlots(availabilityObj) ?: APIResponseError(code, null, message)
//                    APIState.Success(successResponse)
//                } else {
//                    APIState.Error(APIResponseError(code, null, message))
//                }
//            }, onError = { error ->
//                APIState.Error(error)
//            }
//        )
//    }

    override suspend fun verifyPassword(userName: String, password: String): APIState<String> {
        return charmAPI.executeApiCall(
            call = {
                webService.verifyPassword(userName, password)
            },
            onStringSuccess = {
                it
            }
        )
    }

    override suspend fun clearCachedString(key: String) {
        appContext.dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(key))
        }
    }

    //endregion

    override suspend fun sendSupport(
        categoryTxt: String, subjectTxt: String, messageTxt: String, fileIds: String? ): APIState<Boolean> {

        if (BuildConfig.DEBUG) {
            withContext(Dispatchers.Main) {
                Log.d(TAG, subjectTxt + "\n" + messageTxt)
                Toast.makeText(appContext, "Support request sent [Debug mode]", Toast.LENGTH_LONG)
                    .show()
            }
            return APIState.Success(true)
        }

        val postBody = MdJsonBuilder.buildSupportJson(categoryTxt, subjectTxt, messageTxt, fileIds)
        return charmAPI.executeApiCall(
            call = { webService.sendSupport(postBody) },
            onSuccess = {
                true
            })
    }

    override suspend fun uploadSupportAttachmentsFromUris(
        uriList: List<Uri>
    ): APIState<ArrayList<String>> {

        val multipartBody = FileUtil.getMultipartBodyFromUris(uriList, appContext)
        return charmAPI.executeApiCall(
            call = { webService.uploadSupportAttachments(multipartBody) },
            onSuccess = {
                MdParsingHelper.parseFileUploadResponse(it)
            })
    }

}
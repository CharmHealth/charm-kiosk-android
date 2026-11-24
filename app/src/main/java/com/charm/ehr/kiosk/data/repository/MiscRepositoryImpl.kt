package com.charm.ehr.kiosk.data.repository
import com.charm.ehr.kiosk.domain.repository.MiscRepository

import android.content.Context
import android.os.Build
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.data.network.WebService
import com.google.gson.JsonElement
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Call
import javax.inject.Inject

class MiscRepositoryImpl @Inject constructor(
    val webService: WebService,
    val authManager: AuthManager,
    @ApplicationContext  val appContext: Context,
)  : MiscRepository{

    private val TAG = "MiscRepository"

    private var fetchAddressDetailCall: Call<JsonElement>? = null

    private var fetchDistrictFromStateCall: Call<JsonElement>? = null

    override fun getAppDataForSupport(): String {
        return try {
            val androidSdkVersion = Build.VERSION.SDK_INT

            val appVersion = try {
                appContext.packageManager
                    .getPackageInfo(appContext.packageName, 0)
                    .versionName ?: "NA"
            } catch (e: Exception) {
                "NA"
            }

            buildString {
                appendLine("${appContext.getString(R.string.android_sdk_version)} $androidSdkVersion")
                appendLine("${appContext.getString(R.string.app_version)} $appVersion")
            }
        } catch (e: Exception) {
            ""
        }
    }

//    fun getCountries(): LiveData<Any> {
//
//        val countriesLiveData = MutableLiveData<Any>()
//
//        if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {
//
//            val fetchCountriesCall = webService.getCountries()
//
//            apiUtil.initiateRequest(fetchCountriesCall, object : MdNetworkUtil.MdCallback(authManager) {
//                fun onMdSuccess(respObj: JsonObject) {
//                    countriesLiveData.postValue(MdParsingHelper.formCountriesHashMap(respObj.get("country_metrics")?.asJsonObject!!))
//                }
//
//                fun onMdFailure(error: APIResponseError) {
//                    countriesLiveData.postValue(error)
//                }
//            })
//        } else {
//            countriesLiveData.postValue(APIResponseError(ModelUtil.internetErrorCode, null, appContext.getString(R.string.offline_msg)))
//        }
//
//        return countriesLiveData
//    }

//    fun getStates(countryName: String): LiveData<Any> {
//
//        val statesLiveData = MutableLiveData<Any>()
//
//
//
//        if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {
//
//            val fetchCountriesCall = webService.getStates(countryName)
//
//            apiUtil.initiateRequest(fetchCountriesCall, object : MdNetworkUtil.MdCallback(authManager) {
//
//                fun onMdSuccess(respObj: JsonObject) {
//                    statesLiveData.postValue(MdParsingHelper.formStatesArrayList(respObj.get("state_metrics")?.asJsonObject!!))
//                }
//
//                fun onMdFailure(error: APIResponseError) {
//                    statesLiveData.postValue(error)
//                }
//            })
//
//        } else {
//            statesLiveData.postValue(APIResponseError(ModelUtil.internetErrorCode, null, appContext.getString(R.string.offline_msg)))
//
//        }
//
//
//
//
//        return statesLiveData
//    }
//
//    fun fetchCommonQualifiers(): LiveData<Any> {
//        val qualifiersLiveData = MutableLiveData<Any>()
//
//
//        if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {
//
//            val qualifiersCall = webService.fetchCommonQualifiers()
//
//            apiUtil.initiateRequest(qualifiersCall, object : MdNetworkUtil.MdCallback(authManager) {
//                fun onMdSuccess(respObj: JsonObject) {
//                    qualifiersLiveData.postValue(MdParsingHelper.formQualifiersHashMap(respObj))
//                }
//
//                fun onMdFailure(error: APIResponseError) {
//                    qualifiersLiveData.postValue(error)
//                }
//            })
//
//        } else {
//            qualifiersLiveData.postValue(APIResponseError(ModelUtil.internetErrorCode, null, appContext.getString(R.string.offline_msg)))
//        }
//
//
//        return qualifiersLiveData
//    }
//
//    fun getAppointmentStatus(): LiveData<Any> {
//
//        val getAppointmentRespLive = MutableLiveData<Any>()
//
//
//
//        if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {
//
//            val addAppointmentsCall = webService.getAppointmentStatus()
//
//            apiUtil.initiateRequest(addAppointmentsCall, object : MdNetworkUtil.MdCallback(authManager) {
//
//                fun onMdSuccess(respObj: JsonObject) {
//
//                    getAppointmentRespLive.postValue(MdParsingHelper.getAppointmentStatusFromResponse(respObj))
//                }
//
//                fun onMdFailure(error: APIResponseError) {
//                    getAppointmentRespLive.postValue(error)
//                }
//
//            })
//
//        } else {
//            getAppointmentRespLive.postValue(
//                APIResponseError(
//                    ModelUtil.internetErrorCode,
//                    null,
//                    appContext.getString(R.string.offline_msg)
//                )
//            )
//        }
//
//
//
//        return getAppointmentRespLive
//
//    }
//
//    fun fetchFeedbackQuestionnaire(feedbackId: String): LiveData<Any> {
//        val feedbackQuestionnaireLive = MutableLiveData<Any>()
//
//
//
//        if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {
//
//            val feedbackQuestionnaireCall = webService.fetchFeedbackQuestionnaire(feedbackId)
//            apiUtil.initiateRequest(feedbackQuestionnaireCall, object : MdNetworkUtil.MdCallback(authManager) {
//
//                fun onMdSuccess(respObj: JsonObject) {
//
//                    feedbackQuestionnaireLive.postValue(MdParsingHelper.formFeedbackPOJOList(respObj))
//                }
//
//                fun onMdFailure(error: APIResponseError) {
//                    feedbackQuestionnaireLive.postValue(error)
//                }
//
//            })
//
//        } else {
//            feedbackQuestionnaireLive.postValue(
//                APIResponseError(
//                    ModelUtil.internetErrorCode,
//                    null,
//                    appContext.getString(R.string.offline_msg)
//                )
//            )
//        }
//
//
//
//        return feedbackQuestionnaireLive
//    }
//
//    fun submitFeedback(feedbackPOJOList: ArrayList<FeedbackPOJO>): LiveData<Any> {
//        val submitFeedbackLive = MutableLiveData<Any>()
//
//
//        if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {
//
//            /*
//            if (!authManager.selectedMemberFromLocalSettings?.memberId.isNullOrBlank()){
//                feedbackPOJOList.firstOrNull()?.providerId = authManager.selectedMemberFromLocalSettings?.memberId
//            }
//            else if (!authManager.kioskSettings?.selectedDepartmentId.isNullOrBlank()){
//                feedbackPOJOList.firstOrNull()?.departmentId = authManager.kioskSettings?.selectedDepartmentId
//            }*/
//
//            val postObj = MdJsonBuilder.formFeedbackSubmitArr(feedbackPOJOList)
//
//            Log.d("feedbackSubmitReq", postObj.toString())
//
//            val submitFeedbackCall = webService.submitFeedbackQuestionnaire(postObj)
//
//            apiUtil.initiateRequest(submitFeedbackCall, object : MdNetworkUtil.MdCallback(authManager) {
//
//                fun onMdSuccess(respObj: JsonObject) {
//                    submitFeedbackLive.postValue(true)
//                }
//
//                fun onMdFailure(error: APIResponseError) {
//                    submitFeedbackLive.postValue(error)
//                }
//
//            })
//
//        } else {
//            submitFeedbackLive.postValue(APIResponseError(ModelUtil.internetErrorCode, null, appContext.getString(R.string.offline_msg)))
//        }
//
//
//
//        return submitFeedbackLive
//    }
//
//    fun fetchAddressDetailFromPincode(pincode: String): LiveData<Any> {
//        val fetchAddressDetailLive = MutableLiveData<Any>()
//
//
//
//        if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {
//
//            fetchAddressDetailCall?.cancel()
//
//            fetchAddressDetailCall = webService.fetchAddressDetailsFromPincode(pincode)
//
//            apiUtil.initiateRequest(fetchAddressDetailCall!!, object : MdNetworkUtil.MdCallback(authManager) {
//                fun onMdSuccess(respObj: JsonObject) {
//                    val postalAddress = MdParsingHelper.formAddressDetailFromPincode(respObj)
//                    postalAddress?.country = if (pincode.length == 5) {
//                        "United States"
//                    } else {
//                        "India"
//                    }
//
//                    fetchAddressDetailLive.value = postalAddress ?: APIResponseError(ModelUtil.internetErrorCode, null, null)
//                }
//
//                fun onMdFailure(error: APIResponseError) {
//
//                    fetchAddressDetailLive.value = error
//                }
//            })
//        }
//
//
//        return fetchAddressDetailLive
//    }
//
//    fun cancelPincodeSearch() {
//        if (fetchAddressDetailCall != null) {
//            if (fetchAddressDetailCall?.isCanceled == false) {
//                fetchAddressDetailCall?.cancel()
//            }
//        }
//    }
//
//    fun fetchDistrictFromState(state: String): LiveData<Any> {
//        val fetchAddressDetailLive = MutableLiveData<Any>()
//
//
//        if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {
//
//            fetchDistrictFromStateCall?.cancel()
//
//            fetchDistrictFromStateCall = webService.fetchDistrictFromState(state)
//
//            apiUtil.initiateRequest(fetchDistrictFromStateCall!!, object : MdNetworkUtil.MdCallback(authManager) {
//                fun onMdSuccess(respObj: JsonObject) {
//                    fetchAddressDetailLive.value = MdParsingHelper.formDistrictList(respObj)
//                }
//
//                fun onMdFailure(error: APIResponseError) {
//
//                    fetchAddressDetailLive.value = error
//                }
//            })
//        }
//
//        return fetchAddressDetailLive
//    }

//    fun fetchKioskSettings(): LiveData<Any> {
//        val fetchKioskSettingsLive = MutableLiveData<Any>()
//
//
//
//        if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {
//
//            val fetchKioskSettingsCall = webService.fetchKioskSettings()
//
//            apiUtil.initiateRequest(fetchKioskSettingsCall, object : MdNetworkUtil.MdCallback(authManager) {
//                fun onMdFailure(error: APIResponseError) {
//                    fetchKioskSettingsLive.value = error
//                }
//
//                fun onMdSuccess(respObj: JsonObject) {
//                    val kioskSettings = MdParsingHelper.formKioskSettings(respObj)
//
//                    val facilityResp = fetchFacility(kioskSettings.facility?.facilityId!!)
//
//                    if (facilityResp is Facility) {
//                        kioskSettings.facility = facilityResp
//                        authManager.storeKioskSettings(kioskSettings)
//                        authManager.kioskSettings = kioskSettings
//                        fetchKioskSettingsLive.value = kioskSettings
//                    } else {
//                        fetchKioskSettingsLive.value = facilityResp as APIResponseError
//                    }
//
//                }
//
//            })
//        } else {
//            fetchKioskSettingsLive.value = APIResponseError(ModelUtil.internetErrorCode, null, appContext.getString(R.string.offline_msg))
//        }
//
//        return fetchKioskSettingsLive
//    }

//    fun getMappedFacilities(facilityId: String): Any {
//
//        if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {
//
//            val fetchFacilitiesCall = webService.getMappedFacilityList()
//
//            val fetchFacilitiesList = authManager.synchronizeAPICall(fetchFacilitiesCall)
//
//            if (fetchFacilitiesList is JsonObject) {
//                val facilityObjectArr = fetchFacilitiesList.get("facilities").asJsonArray
//                facilityObjectArr?.forEach {
//
//                    val facilityObj = it.asJsonObject
//
//                    val respFacilityId = facilityObj.get("facility_id")?.asString
//
//
//                    if (facilityId == respFacilityId) {
//                        val facility = Facility()
//                        facility.facilityName = facilityObj.get("facility_name")?.asString
//                        facility.facilityId = respFacilityId
//                        val postalAddress = PostalAddress()
//
//                        postalAddress.city = facilityObj.get("city")?.asString
//                        postalAddress.state = facilityObj.get("state")?.asString
//                        postalAddress.country = facilityObj.get("country")?.asString
//                        facility.postalAddress = postalAddress
//
//                        return facility
//                    }
//                }
//
//                return APIResponseError(null, null, appContext.getString(R.string.emptyFacilityList))
//            } else if (fetchFacilitiesList is APIResponseError) {
//                return fetchFacilitiesList
//            }
//        }
//
//        return APIResponseError(ModelUtil.internetErrorCode, null, appContext.getString(R.string.offline_msg))
//    }

}

package com.charm.ehr.kiosk.data.repository
import com.charm.ehr.kiosk.domain.repository.PatientRepository

import UiText
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.charm.ehr.kiosk.domain.result.AddPatientResult
import com.charm.ehr.kiosk.domain.APIResponseError
import com.charm.ehr.kiosk.domain.Country
import com.charm.ehr.kiosk.domain.DemographicsMetrics
import com.charm.ehr.kiosk.domain.DemographicsSetting
import com.charm.ehr.kiosk.common.util.MDAccountsRelatedUtil
import com.charm.ehr.kiosk.common.util.MdJsonBuilder
import com.charm.ehr.kiosk.common.util.MdParsingHelper
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.domain.State
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.dataStore
import com.charm.ehr.kiosk.data.network.WebService
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.presentation.edit_profile.Insurance
import com.charm.ehr.kiosk.common.util.CharmAPI
import com.charm.ehr.kiosk.common.util.FileUtil
import com.charm.ehr.kiosk.common.util.FileUtil.getFileFromFilePath
import com.charm.ehr.kiosk.common.util.ModelUtil
import com.google.gson.JsonObject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import java.io.File
import javax.inject.Inject

class PatientRepositoryImpl @Inject constructor(
    val webService: WebService,
    val authManager: AuthManager,
    val charmAPI: CharmAPI,
    @ApplicationContext  val appContext: Context,
)  : PatientRepository{

    override suspend fun addPatient(patient: Patient, duplicateCheck: Boolean): APIState<AddPatientResult> {

        val addPatPostObj = MdJsonBuilder.formAddPatient(
            patient, authManager.selectedFacilityId ?: "", duplicateCheck
        )
        return charmAPI.executeApiCall(
            call = { webService.addPatient(addPatPostObj) },
            onSuccess = {
                AddPatientResult.Added(MdParsingHelper.parseAddPatientResponse(it))
            },
            onError = { error, errorBody ->
                val code = errorBody?.get("code")?.asString
                if (code == MDAccountsRelatedUtil.DUPLICATE_PATIENT_ERROR_CODE) {
                    val duplicatePatient = MdParsingHelper.parseDuplicatePatientFromErrorInfo(
                        errorBody.getAsJsonObject("error_info")
                    )
                    if (duplicatePatient != null) {
                        APIState.Success(AddPatientResult.DuplicateFound(duplicatePatient))
                    } else APIState.Error(error)
                } else APIState.Error(error)
            }
        )
    }

    override suspend fun editPatient(patient: Patient?): APIState<String> {
        val editPatPostObj = MdJsonBuilder.formEditPatient(patient)
        return charmAPI.executeApiCall(
            call = { webService.editPatient(patient?.practicePatientId ?: "", editPatPostObj) },
            onSuccess = {
                MdParsingHelper.parseAddPatientResponse(it)
            }
        )
    }

//    fun oldAddPatient(patient: Patient): LiveData<Any> {11
//        val patientResp = MutableLiveData<Any>()
//
//        if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {
//
//            if (patient.practicePatientId.isNullOrBlank()) {
//
//                val addPatientPostObj = MdJsonBuilder.formAddPatient(
//                    patient, authManager.selectedFacilityId ?: ""
//                )
//
//                val addPatientCall = webService.oldAddPatient(addPatientPostObj)
//
//                apiUtil.initiateRequest(addPatientCall, object : MdNetworkUtil.MdCallback(authManager) {
//                    fun onMdSuccess(respObj: JsonObject) {
//                        val patientObj = respObj.get("patient")?.asJsonObject
//                        patient.practicePatientId = patientObj?.get("patient_id")?.asString
//                        patient.patientRecordId = patientObj?.get("record_id")?.asString
//                        if (patient.patientImgFilePath != null) {
//                            val addPatientImgResp = addPatientImage(patient)
//                            patientResp.postValue(addPatientImgResp)
//                        } else {
//                            patientResp.postValue(patient)
//                        }
//                    }
//
//                    fun onMdFailure(error: APIResponseError) {
//                        error.errorMsg = ModelUtil.addPatientFailPrefix + error.errorMsg
//                        patientResp.postValue(error)
//                    }
//
//                })
//            } else {
//                if (patient.patientImgFilePath != null) {
//                    patientResp.postValue(addPatientImage(patient))
//                } else {
//                    patientResp.postValue(patient)
//                }
//            }
//
//        } else {
//            patientResp.postValue(
//                APIResponseError(
//                    ModelUtil.internetErrorCode,
//                    null,
//                    ModelUtil.addPatientFailPrefix + appContext.getString(R.string.add_patient_offline_msg)
//                )
//            )
//        }
//
//
//
//
//        return patientResp
//    }

//    fun editPatient(patient: Patient): LiveData<Any> {
//
//        val editPatientResp = MutableLiveData<Any>()
//
//
//        if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {
//
//            apiUtil.getToken(object : MdNetworkUtil.MdCallback(authManager) {
//                fun onMdSuccess(respObj: JsonObject) {
//                    if (!patient.isPatientUpdated) {
//
//                        val resp = fetchPatientWithPatId(patient.practicePatientId ?: "", true)
//
//                        if (resp is JsonObject) {
//                            val editPatientPostObj = MdJsonBuilder.formEditPatient(patient, resp)
//
//                            val editPatientCall = webService.editPatient(patient.practicePatientId ?: "", editPatientPostObj)
//
//                            apiUtil.initiateRequest(editPatientCall, object : MdNetworkUtil.MdCallback(authManager) {
//                                fun onMdSuccess(respObj: JsonObject) {
//
//                                    patient.isPatientUpdated = true
//
//
//
//                                    if (patient.patientImg != null) {
//                                        editPatientResp.postValue(addPatientImage(patient))
//                                    } else if (patient.isPatientImgToBeDeleted) {
//                                        editPatientResp.postValue(deletePatientImage(patient))
//                                    } else {
//                                        editPatientResp.postValue(patient)
//                                    }
//                                }
//
//                                fun onMdFailure(error: APIResponseError) {
//                                    error.errorMsg = ModelUtil.editPatientFailPrefix + error.errorMsg
//                                    editPatientResp.postValue(error)
//                                }
//
//                            })
//                        } else {
//                            editPatientResp.postValue(APIResponseError(null, null, ModelUtil.editPatientFailPrefix))
//                        }
//
//                    } else {
//                        if (patient.patientImg != null) {
//                            editPatientResp.postValue(addPatientImage(patient))
//                        } else {
//                            editPatientResp.postValue(patient)
//                        }
//                    }
//                }
//
//                fun onMdFailure(error: APIResponseError) {
//                }
//            })
//
//        } else {
//            editPatientResp.postValue(
//                APIResponseError(
//                    ModelUtil.internetErrorCode,
//                    null,
//                    ModelUtil.editPatientFailPrefix + appContext.getString(R.string.edit_patient_offline_msg)
//                )
//            )
//        }
//
//
//
//
//        return editPatientResp
//
//    }

    /**
     * NOTE: If you use this method then all the mandatory fields should not be null at this point
     */
//    fun checkPatientIsAdded(patient: Patient): LiveData<Any> {
//        val respLive = MutableLiveData<Any>()
//
//        if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {
//
//            val fetchPatientCall = webService.fetchPatientListWithPatDetails(
//                patient.firstName!!,
//                patient.lastName!!,
//                patient.dateOfBirth!!.toString(ModelUtil.charmApiDateFormat()),
//                patient.contactDetails?.mobile!!,
//                authManager.kioskSettingsFlow.value?.facility?.facilityId!!
//            )
//
//
//
//
//
//            apiUtil.initiateRequest(fetchPatientCall, object : MdNetworkUtil.MdCallback(authManager) {
//                fun onMdSuccess(respObj: JsonObject) {
//                    val patientJsonArray = respObj.getAsJsonArray("patients")
//
//                    val patientSize = patientJsonArray.size()
//                    val resp = when (patientSize) {
//                        0 -> {
//                            false
//                        }
//
//                        else -> true
//                    }
//                    respLive.postValue(resp)
//                }
//
//                fun onMdFailure(error: APIResponseError) {
//                    respLive.postValue(error)
//
//                }
//            })
//        } else {
//            respLive.postValue(
//                APIResponseError(
//                    ModelUtil.internetErrorCode, null, appContext.getString(R.string.fetch_patient_offline_msg)
//                )
//            )
//        }
//
//
//        return respLive
//    }

    override suspend fun searchPatient(
        facilityId: String, name: String?,
        recordId: String?,
        mobile: String?, email: String?, dob: String?
    ): APIState<ArrayList<Patient>> {

        return charmAPI.executeApiCall(
            call = {
                webService.searchPatient(
                    facilityId = facilityId,
                    recordId = recordId,
                    mobile = mobile,
                    email = email,
                    dob = dob,
                    fullName = name,
                )
            },
            onSuccess = {
                MdParsingHelper.getPatientList(it)
            }
        )

    }

    override suspend fun getPatientDetail(patientId: String): APIState<Patient> {
        return charmAPI.executeApiCall(
            call = {
                webService.getPatientDetail(
                    patientId = patientId
                )
            },
            onSuccess = {
                val patientObj = it.get("patient")?.asJsonObject
                MdParsingHelper.formPatientPOJO(patientObj)
            },
        )
    }

    override suspend fun getPatientInsurances(patientId: String): APIState<Any> {
        return charmAPI.executeApiCall(
            call = {
                webService.getPatientInsurances(
                    patientId = patientId
                )
            },
            onSuccess = {
                MdParsingHelper.formInsurancePOJO(it)
            },
        )
    }

    override suspend fun getPatientInsurance(patientId: String,insuranceId: String): APIState<Any> {
        return charmAPI.executeApiCall(
            call = {
                webService.getPatientInsurance(
                    patientId = patientId,insuranceId=insuranceId
                )
            },
            onSuccess = {
                MdParsingHelper.formInsurancePOJO(it)
            },
        )
    }

    override suspend fun updatePatientInsurances(
        patientId: String,
        insurance: Insurance
    ): APIState<Any> {
        val insuranceJson = MdJsonBuilder.formInsuranceJson(insurance)
        return charmAPI.executeApiCall(
            call = {  webService.updatePatientInsurances(
                patientId,
                insurance.insuranceID.value ?: "",
                insuranceJson
            ) },
            onSuccess = { MdParsingHelper.parsePostPatientInsurancesResponse(it) }
        )
    }

    override suspend fun addPatientInsurances(
        patientId: String,
        insurance: Insurance
    ): APIState<Any> {
        val insuranceJson = MdJsonBuilder.formInsuranceJson(insurance)
        return charmAPI.executeApiCall(
            call = { webService.addPatientInsurances(patientId, insuranceJson) },
            onSuccess = { MdParsingHelper.parseAddInsuranceResponse(it) }
        )
    }

    override suspend fun postPatientInsuranceCardFront(
        imageFile: String,
        patientId: String,
        insuranceId: String
    ): APIState<Boolean> {
        val file = imageFile.getFileFromFilePath()
        val filePair = FileUtil.createRequestBodyForFile(file)
        val requestBody = filePair?.first

        if (requestBody != null) {
            val multiPartBody = MultipartBody.Part.createFormData("file", file?.name, requestBody)
           return charmAPI.executeApiCall(
                call = { webService.postPatientInsuranceCardFront(multiPartBody, patientId, insuranceId) },
                onSuccess = { true },
                ignoreCodeZero = true
            )
        } else {
            return APIState.Error(
                APIResponseError(
                    null, null,
                    errorMsg = UiText.StrResId(R.string.the_file_does_not_exist_or_is_not_accessible)
                )
            )
        }
    }

    override suspend fun postPatientInsuranceCardBack(
        imageFile: String,
        patientId: String,
        insuranceId: String
    ): APIState<Boolean> {
        val file = imageFile.getFileFromFilePath()
        val filePair = FileUtil.createRequestBodyForFile(file)
        val requestBody = filePair?.first

        if (requestBody != null) {
            val multiPartBody = MultipartBody.Part.createFormData("file", file?.name, requestBody)
            return charmAPI.executeApiCall(
                call = { webService.postPatientInsuranceCardBack(multiPartBody, patientId, insuranceId) },
                onSuccess = { true },
                ignoreCodeZero = true
            )
        } else {
            return APIState.Error(
                APIResponseError(
                    null, null,
                    errorMsg = UiText.StrResId(R.string.the_file_does_not_exist_or_is_not_accessible)
                )
            )
        }
    }

    override suspend fun getPatientInsuranceCardFront( patientId: String,
                                              insuranceId: String): APIState<ResponseBody> {
        return charmAPI.executeBinaryApiCall(
            call = { webService.getPatientInsuranceCardFront(patientId,insuranceId) },
            onSuccess = { responseBody ->
                responseBody
            }
        )
    }

    override suspend fun getPatientInsuranceCardBack( patientId: String,
                                              insuranceId: String): APIState<ResponseBody> {
        return charmAPI.executeBinaryApiCall(
            call = { webService.getPatientInsuranceCardBack(patientId,insuranceId) },
            onSuccess = { responseBody ->
                responseBody
            }
        )
    }

    override suspend fun deletePatientInsuranceCardBack(
        patientId: String,
        insuranceId: String
    ): APIState<Any> {
        return charmAPI.executeApiCall(
            call = { webService.deletePatientInsuranceCardBack(patientId, insuranceId) },
            onSuccess = { MdParsingHelper.parseDeleteInsuranceCardResponse(it) }
        )
    }

    override suspend fun deletePatientInsuranceCardFront(
        patientId: String,
        insuranceId: String
    ): APIState<Any> {
       return charmAPI.executeApiCall(
            call = {  webService.deletePatientInsuranceCardFront(patientId, insuranceId) },
            onSuccess = { MdParsingHelper.parseDeleteInsuranceCardResponse(it) }
        )
    }




//    fun fetchPatientWithPatId(patientId: String, giveJsonAsResult: Boolean = false): Any {
//
//        val fetchPatientCall = webService.getPatientDetail(patientId)
//
//        val fetchPatientResp = authManager.synchronizeAPICall(fetchPatientCall)
//        return if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {
//
//            if (fetchPatientResp is JsonObject) {
//                val patientObj = fetchPatientResp.get("patient")?.asJsonObject
//                Log.d("patientResp", patientObj.toString())
//                if (giveJsonAsResult) {
//                    patientObj ?: APIResponseError(null, null, null)
//                } else {
//                    MdParsingHelper.formPatientPOJO(patientObj)
//                }
//            } else {
//                fetchPatientResp as APIResponseError
//            }
//        } else {
//            APIResponseError(ModelUtil.internetErrorCode, null, appContext.getString(R.string.fetch_patient_offline_msg))
//        }
//
//        /*
//                return APIResponseError(null,null,appContext.getString(R.string.conversionError))
//        */
//    }

//    fun fetchPatientWithIdLive(patientId: String): LiveData<Any> {
//        val respLive = MutableLiveData<Any>()
//
//        if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {
//
//            val fetchPatientCall = webService.getPatientDetail(patientId)
//
//            apiUtil.initiateRequest(fetchPatientCall, object : MdNetworkUtil.MdCallback(authManager) {
//                fun onMdSuccess(respObj: JsonObject) {
//                    val patientObj = respObj.get("patient")?.asJsonObject
//                    respLive.postValue(MdParsingHelper.formPatientPOJO(patientObj))
//                }
//
//                fun onMdFailure(error: APIResponseError) {
//                    respLive.postValue(error)
//
//                }
//            })
//        } else {
//            respLive.postValue(
//                APIResponseError(
//                    ModelUtil.internetErrorCode, null, appContext.getString(R.string.fetch_patient_offline_msg)
//                )
//            )
//        }
//
//        return respLive
//    }

    override suspend fun addPatientImage(patientImgFilePath: String,practicePatientId:String): APIState<JsonObject> {
        return withContext(Dispatchers.IO) {
            val requestBody =
                File(patientImgFilePath).asRequestBody("image/*".toMediaTypeOrNull())
            charmAPI.executeApiCall(
                call = {
                    webService.addPatientImage(
                        file = requestBody,
                        patientId = practicePatientId
                    )
                },
                onSuccess = {
                    it
                }
            )
        }
    }

    private suspend fun getCachedString(key: String): String? {
        val preferences = appContext.dataStore.data
            .first()
        return preferences[stringPreferencesKey(key)]
    }

    private suspend fun cacheString(key: String, value: String) {
        appContext.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = value
        }
    }

    private suspend fun clearCachedString(key: String) {
        appContext.dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(key))
        }
    }

//    fun addPatientImage(patient: Patient): Any {
//
//        return if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {
//            val patImgbody = RequestBody.create("image/*".toMediaTypeOrNull(), File(patient.patientImgFilePath ?: ""))
//
//            val addPatientImgCall = webService.addPatientImage(patImgbody, patientId = patient.practicePatientId!!)
//
//            val addPatientImgResp = authManager.synchronizeAPICall(addPatientImgCall)
//            if (addPatientImgResp is JsonObject) {
//                patient
//            } else {
//                addPatientImgResp as APIResponseError
//            }
//        } else {
//            APIResponseError(
//                ModelUtil.internetErrorCode,
//                null,
//                ModelUtil.addPatientImgFailPrefix + appContext.getString(R.string.add_patient_img_offline_msg)
//            )
//        }
//    }

//    fun deletePatientImage(patient: Patient): Any {
//
//        if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {
//
//            val deletePatientImgCall = webService.deletePatientImage(patient.practicePatientId!!)
//
//            val deletePatientResp = authManager.synchronizeAPICall(deletePatientImgCall)
//
//            if (deletePatientResp is JsonObject) {
//                val patientObj = deletePatientResp.get("patient")?.asJsonObject
//                Log.d("patientResp", patientObj.toString())
//                return patient
//            } else {
//                return deletePatientResp!!
//            }
//
//        } else {
//            return APIResponseError(ModelUtil.internetErrorCode, null, appContext.getString(R.string.offline_msg))
//        }
//
//    }

    override suspend fun fetchAndCachePatientDemographicsMetrics(): APIState<DemographicsMetrics> {
        var responseJson: JsonObject? = null
        val result = charmAPI.executeApiCall(
            call = { webService.getPatientDemographicsMetrics() },
            onSuccess = {
                responseJson = it
                MdParsingHelper.formDemographicsMetrics(it)
            }
        )
        if (result is APIState.Success && responseJson != null) {
            cacheString(ModelUtil.demographicsMetrics, responseJson.toString())
        }
        return result
    }

    override suspend fun getPatientDemographicsMetrics(): APIState<DemographicsMetrics> {
        val cached = getCachedString(ModelUtil.demographicsMetrics)
        return if (cached != null) {
            val metrics = MdParsingHelper.formDemographicsMetricsFromString(cached)
            APIState.Success(metrics)
        } else {
            fetchAndCachePatientDemographicsMetrics()
        }
    }

    override suspend fun fetchAndCachePatientDemographicsSettings(): APIState<ArrayList<DemographicsSetting>> {
        var responseJson: JsonObject? = null
        val result = charmAPI.executeApiCall(
            call = { webService.getPatientDemographicsSettings() },
            onSuccess = {
                responseJson = it
                MdParsingHelper.formDemographicsSettings(it)
            }
        )
        if (result is APIState.Success && responseJson != null) {
            cacheString(ModelUtil.demographicsSetting, responseJson.toString())
        }
        return result
    }

    override suspend fun getPatientDemographicsSettings(): APIState<ArrayList<DemographicsSetting>> {
        val cached = getCachedString(ModelUtil.demographicsSetting)
        return if (cached != null) {
            val settings = MdParsingHelper.formDemographicsSettingsFromString(cached)
            APIState.Success(settings)
        } else {
            fetchAndCachePatientDemographicsSettings()
        }
    }

    override suspend fun fetchAndCacheCountries(): APIState<ArrayList<Country>> {
        var responseJson: JsonObject? = null
        val result = charmAPI.executeApiCall(
            call = { webService.getCountries() },
            onSuccess = {
                responseJson = it
                MdParsingHelper.formCountryList(it)
            }
        )
        if (result is APIState.Success && responseJson != null) {
            cacheString(ModelUtil.countries, responseJson.toString())
        }
        return result
    }

    override suspend fun getCountries(): APIState<ArrayList<Country>> {
        val cached = getCachedString(ModelUtil.countries)
        return if (cached != null) {
            val countries = MdParsingHelper.formCountryListFromString(cached)
            APIState.Success(countries)
        } else {
            fetchAndCacheCountries()
        }
    }

    override suspend fun fetchAndCacheStates(countryShortForm: String): APIState<List<State>> {
        val cacheKey = ModelUtil.states + "_" + countryShortForm
        var responseJson: JsonObject? = null
        val result = charmAPI.executeApiCall(
            call = { webService.getStates(countryShortForm) },
            onSuccess = {
                responseJson = it
                MdParsingHelper.formStateList(it)
            }
        )
        if (result is APIState.Success && responseJson != null) {
            cacheString(cacheKey, responseJson.toString())
        }
        return result
    }

    override suspend fun getStates(countryShortForm: String): APIState<List<State>> {
        val cacheKey = ModelUtil.states + "_" + countryShortForm
        val cached = getCachedString(cacheKey)
        return if (cached != null) {
            val states = MdParsingHelper.formStateListFromString(cached)
            APIState.Success(states)
        } else {
            fetchAndCacheStates(countryShortForm)
        }
    }


    override suspend fun getPayerDetails(query: String?): APIState<Any> {
        return charmAPI.executeApiCall(
            call = {
                webService.getPayerDetails(query ?: "")
            },
            onSuccess = {
                MdParsingHelper.formPayerDetails(it)
            },
        )
    }

}

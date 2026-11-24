package com.charm.ehr.kiosk.domain.repository

import com.charm.ehr.kiosk.domain.result.AddPatientResult
import com.charm.ehr.kiosk.domain.Country
import com.charm.ehr.kiosk.domain.DemographicsMetrics
import com.charm.ehr.kiosk.domain.DemographicsSetting
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.domain.State
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.presentation.edit_profile.Insurance
import com.google.gson.JsonObject
import okhttp3.ResponseBody

interface PatientRepository {
    suspend fun addPatient(patient: Patient, duplicateCheck: Boolean = true): APIState<AddPatientResult>
    suspend fun editPatient(patient: Patient?): APIState<String>
    suspend fun searchPatient(
        facilityId: String, name: String?,
        recordId: String?,
        mobile: String?, email: String?, dob: String?
    ): APIState<ArrayList<Patient>>
    suspend fun getPatientDetail(patientId: String): APIState<Patient>
    suspend fun getPatientInsurances(patientId: String): APIState<Any>
    suspend fun getPatientInsurance(patientId: String,insuranceId: String): APIState<Any>
    suspend fun updatePatientInsurances(
        patientId: String,
        insurance: Insurance
    ): APIState<Any>
    suspend fun addPatientInsurances(
        patientId: String,
        insurance: Insurance
    ): APIState<Any>
    suspend fun postPatientInsuranceCardFront(
        imageFile: String,
        patientId: String,
        insuranceId: String
    ): APIState<Boolean>
    suspend fun postPatientInsuranceCardBack(
        imageFile: String,
        patientId: String,
        insuranceId: String
    ): APIState<Boolean>
    suspend fun getPatientInsuranceCardFront( patientId: String,
                                              insuranceId: String): APIState<ResponseBody>
    suspend fun getPatientInsuranceCardBack( patientId: String,
                                              insuranceId: String): APIState<ResponseBody>
    suspend fun deletePatientInsuranceCardBack(
        patientId: String,
        insuranceId: String
    ): APIState<Any>
    suspend fun deletePatientInsuranceCardFront(
        patientId: String,
        insuranceId: String
    ): APIState<Any>
    suspend fun addPatientImage(patientImgFilePath: String,practicePatientId:String): APIState<JsonObject>

    suspend fun fetchAndCachePatientDemographicsMetrics(): APIState<DemographicsMetrics>
    suspend fun getPatientDemographicsMetrics(): APIState<DemographicsMetrics>
    suspend fun fetchAndCachePatientDemographicsSettings(): APIState<ArrayList<DemographicsSetting>>
    suspend fun getPatientDemographicsSettings(): APIState<ArrayList<DemographicsSetting>>
    suspend fun fetchAndCacheCountries(): APIState<ArrayList<Country>>
    suspend fun getCountries(): APIState<ArrayList<Country>>
    suspend fun fetchAndCacheStates(countryShortForm: String): APIState<List<State>>
    suspend fun getStates(countryShortForm: String): APIState<List<State>>
    suspend fun getPayerDetails(query: String?): APIState<Any>
}

package com.charm.ehr.kiosk.data.network

import com.charm.ehr.kiosk.common.util.MDAccountsRelatedUtil
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface WebService {

    //region  Patient

    @Multipart
    @POST("api/ehr/v1/patients/{patient_id}/photo")
    suspend fun addPatientImage(
        @Part("file\"; filename=\"patient.jpeg\" ") file: RequestBody,
        @Path("patient_id") patientId: String
    ): Response<JsonElement>


    @POST("api/ehr/v1/patients")
    fun oldAddPatient(@Body postBody: JsonObject): Call<JsonElement>

    @POST("api/ehr/v1/patients")
    suspend fun addPatient(@Body postBody: JsonObject): Response<JsonElement>

    @PUT("api/ehr/v1/patients/{patient_id}")
    suspend fun editPatient(
        @Path("patient_id") patientId: String,
        @Body postBody: JsonObject
    ): Response<JsonElement>

    @DELETE("api/ehr/v1/patients/{patient_id}/photo")
    fun deletePatientImage(@Path("patient_id") patientId: String): Call<JsonElement>

    @GET("api/ehr/v1/patients?filter_by=Status.Active")
    suspend fun searchPatient(
        @Query("facility_id") facilityId: String,
        @Query("full_name_contains") fullName: String?,
        @Query("record_id_contains") recordId: String?,
        @Query("mobile_contains") mobile: String?,
        @Query("email_contains") email: String?,
        @Query("dob") dob: String?,
    ): Response<JsonElement>

    @GET("api/ehr/v1/patients?filter_by=Status.Active")
    fun getPatWithMobileNumber(
        @Query("mobile") mobile: String,
        @Query("facility_id") facilityId: String
    ): Call<JsonElement>

    @GET("api/ehr/v1/patients?filter_by=Status.Active")
    fun fetchPatientListWithPatDetails(
        @Query("first_name") firstName: String,
        @Query("last_name") lastName: String,
        @Query("dob") dob: String,
        @Query("mobile") mobile: String,
        @Query("facility_id") facilityId: String
    ): Call<JsonElement>

    @GET("api/ehr/v1/patients/{patient_id}")
    suspend fun getPatientDetail(@Path("patient_id") patientId: String): Response<JsonElement>

    @GET("api/ehr/v1/payers?is_verified=true")
    suspend fun getPayerDetails(@Query("search_string") searchString: String): Response<JsonElement>

    @GET("api/ehr/v1/patients/{patient_id}/insurances")
    suspend fun getPatientInsurances(@Path("patient_id") patientId: String): Response<JsonElement>

    @GET("api/ehr/v1/patients/{patient_id}/insurances/{insurance_id}")
    suspend fun getPatientInsurance(@Path("patient_id") patientId: String, @Path("insurance_id") insuranceId: String): Response<JsonElement>

    @PUT("api/ehr/v1/patients/{patient_id}/insurance/{insurance_id}")
    suspend fun updatePatientInsurances(
        @Path("patient_id") patientId: String,
        @Path("insurance_id") insuranceId: String,
        @Body postBody: JsonObject
    ): Response<JsonElement>

    @GET("api/ehr/v1/patients/{patient_id}/insurance/{insurance_id}/front_page")
    suspend fun getPatientInsuranceCardFront(
        @Path("patient_id") patientId: String,
        @Path("insurance_id") insuranceId: String
    ): Response<ResponseBody>

    @GET("api/ehr/v1/patients/{patient_id}/insurance/{insurance_id}/back_page")
    suspend fun getPatientInsuranceCardBack(
        @Path("patient_id") patientId: String,
        @Path("insurance_id") insuranceId: String
    ): Response<ResponseBody>

    @DELETE("api/ehr/v1/patients/{patient_id}/insurance/{insurance_id}/front_page")
    suspend fun deletePatientInsuranceCardFront(
        @Path("patient_id") patientId: String,
        @Path("insurance_id") insuranceId: String
    ): Response<JsonElement>

    @DELETE("api/ehr/v1/patients/{patient_id}/insurance/{insurance_id}/back_page")
    suspend fun deletePatientInsuranceCardBack(
        @Path("patient_id") patientId: String,
        @Path("insurance_id") insuranceId: String
    ): Response<JsonElement>

    @Multipart
    @POST("api/ehr/v1/patients/{patient_id}/insurance/{insurance_id}/front_page")
    suspend fun postPatientInsuranceCardFront(
        @Part file: MultipartBody.Part,
        @Path("patient_id") patientId: String,
        @Path("insurance_id") insuranceId: String
    ): Response<JsonElement>

    @Multipart
    @POST("api/ehr/v1/patients/{patient_id}/insurance/{insurance_id}/back_page")
    suspend fun postPatientInsuranceCardBack(
        @Part file: MultipartBody.Part,
        @Path("patient_id") patientId: String,
        @Path("insurance_id") insuranceId: String
    ): Response<JsonElement>


    @POST("api/ehr/v1/patients/{patient_id}/insurance")
    suspend fun addPatientInsurances(
        @Path("patient_id") patientId: String,
        @Body postBody: JsonObject
    ): Response<JsonElement>

    //On Sync
    //https://ehr.charmtracker.com/api/ehr/v1/settings
    //https://ehr.charmtracker.com/api/ehr/v1/mapped_facilities
    //https://ehr.charmtracker.com/api/ehr/v1/patients/demographics/metrics
    //https://ehr.charmtracker.com/api/ehr/v1/countries?
    //https://ehr.charmtracker.com/api/ehr/v1/states?country=in
    //https://ehr.charmtracker.com/api/ehr/v1/states?country=us
    //https://ehr.charmtracker.com/api/ehr/v1/states?country=au
    //https://ehr.charmtracker.com/api/ehr/v1/settings/demographics
    //https://ehr.charmtracker.com/api/ehr/v1/settings/demographics
    //https://ehr.charmtracker.com/kiosk.do?ACTION=PRACTICE_QUESTIONNAIRES
    //https://ehr.charmtracker.com/kiosk.do?ACTION=PRACTICE_CONSENT_FORMS

    @GET("api/ehr/v1/patients/demographics/metrics")
    suspend fun getPatientDemographicsMetrics(): Response<JsonElement>

    @GET("api/ehr/v1/settings/demographics")
    suspend fun getPatientDemographicsSettings(): Response<JsonElement>

    @GET("api/ehr/v1/countries")
    suspend fun getCountries(): Response<JsonElement>

    @GET("api/ehr/v1/states")
    suspend fun getStates(@Query("country") countryName: String): Response<JsonElement>

    //endregion

    //region OTP
    @POST("api/ehr/v1/patients/send/otp")
    suspend fun sendOtp(@Body postBody: JsonObject): Response<JsonElement>

    @POST("api/ehr/v1/patients/resend/otp")
    suspend fun reSendOtp(@Body postBody: JsonObject): Response<JsonElement>

    @POST("api/ehr/v1/patients/verify/otp")
    suspend fun verifyOtp(@Body postBody: JsonObject): Response<JsonElement>

    //endregion

    //region Accounts

    @FormUrlEncoded
    @POST("charmApiHandler.do?MODULE=verifyUser")
    suspend fun verifyPassword(
        @Field("j_username") username: String,
        @Field("j_password") password: String
    ): Response<JsonElement>

    //endregion

    //region misc


    @GET(value = "api/ehr/v1/settings")
    suspend fun fetchSettings(): Response<JsonElement>

    @GET(value = "api/ehr/v1/commonQualifiers")
    fun fetchCommonQualifiers(): Call<JsonElement>

    @GET("api/ehr/v1/search/pincode")
    fun fetchAddressDetailsFromPincode(@Query("code") pincode: String): Call<JsonElement>

    @GET("api/ehr/v1/search/district")
    fun fetchDistrictFromState(@Query("state") state: String): Call<JsonElement>

    @GET("api/ehr/v1/kiosk/props")
    fun fetchKioskSettings(): Call<JsonElement>

    @GET(value = "api/ehr/v1/mapped_facilities")
    suspend fun getMappedFacilityList(): Response<JsonElement>

    //endregion

    //region Appointment

    @GET("api/ehr/v1/members")
    fun fetchMembers(
        @Query("facility_id") facilityId: String,
        @Query("privilege") privilege: String = "view_appointment_booking",
        @Query("is_online_appointments_enabled") onlineAppointmentsEnabled: Boolean? = null
    ): Call<JsonElement>

    @GET("api/ehr/v1/members")
    fun fetchMembersByDepartment(
        @Query("department_id") departmentId: String,
        @Query("facility_id") facilityId: String,
        @Query("is_online_appointments_enabled") onlineAppointmentsEnabled: Boolean,
        @Query("privilege") privilege: String = "view_appointment_booking"
    ): Call<JsonElement>

    @GET("api/ehr/v1/members/{member_id}/timings")
    fun fetchMemberWorkTiming(
        @Path("member_id") memberId: String,
        @Query("facility_id") facilityId: String
    ): Call<JsonElement>

    @GET("api/ehr/v1/appointments?per_page=200")
    suspend  fun fetchAppointments(
        @Query("patient_id") patientId: String,
        @Query("facility_ids") facilityId: String,
        @Query("start_date") start_date: String,
        @Query("end_date") end_date: String = start_date
    ): Response<JsonElement>

    @POST("api/ehr/v1/appointments/{appointment_id}/token")
    fun generateToken(@Path("appointment_id") appointmentId: String): Call<JsonElement>

    @POST("api/ehr/v1/appointments")
    fun addAppointment(@Body postBody: JsonObject): Call<JsonElement>

    @GET("api/ehr/v1/settings/departments")
    fun fetchDepartments(@Query("facility_id") facilityId: String): Call<JsonElement>

    @GET("api/ehr/v1/members/{member_id}/availability")
    fun getAvailableSlots(
        @Path("member_id") member_id: String, @Query("from_date") fromDate: String,
        @Query("to_date") toDate: String, @Query("facility_id") facilityId: String,
        @Query("time_slot") timeSlots: String
    ): Call<JsonElement>

    @GET("api/ehr/v1/appointments/status")
    suspend fun getAppointmentStatus(): Response<JsonElement>

    @GET("api/ehr/v1/appointments/settings")
    fun getAppointmentSettings(
        @Query("member_id") memberId: String,
        @Query("facility_id") facilityId: String
    ): Call<JsonElement>

    @GET("api/ehr/v1/appointments/count")
    fun getConfirmedAppointmentsCount(
        @Query("member_ids") memberId: String,
        @Query("facility_ids") facilityId: String, @Query("start_date_time") startDateTime: String,
        @Query("end_date_time") endDateTime: String
    ): Call<JsonElement>

    @POST("api/ehr/v1/appointments/{appointment_id}/status")
    suspend fun updateAppointmentStatus(
        @Path("appointment_id") appointmentId: String,
        @Query("status_id") statusId: String
    ): Response<JsonElement>

    //endregion

    //region Questionnaires and consent form (document)

    @GET("kiosk.do?ACTION=PATIENTS_SHARED_QUESTIONNAIRES")
    suspend fun getPatientsSharedQuestionnaires(
        @Query("PRACTICE_PATIENT_ID") patientId: String
    ): Response<JsonElement>

    @GET("kiosk.do?ACTION=PATIENTS_SHARED_CONSENTS")
    suspend fun getPatientsSharedConsentForms(
        @Query("PRACTICE_PATIENT_ID") patientId: String
    ): Response<JsonElement>

    @GET("kiosk.do?ACTION=PRACTICE_QUESTIONNAIRES")
    suspend fun getPracticeQuestionnaires(): Response<JsonElement>

    @GET("kiosk.do?ACTION=PRACTICE_CONSENT_FORMS")
    suspend fun getPracticeConsentForms(): Response<JsonElement>

    @POST("kiosk.do?ACTION=UPDATE_SHARED_QUESTIONNAIRES")
    suspend fun updatePatientSharedQuestionnaire(@QueryMap params: Map<String, String>): Response<JsonElement>

    @GET("api/ehr/v1/questionnaire/answer/{ques_map_id}")
    suspend fun getQuestionnaireAnswer(@Path("ques_map_id") quesMapId: String): Response<JsonElement>

    @POST("api/ehr/v1/questionnaire/answers")
    suspend fun postQuestionnaireAnswers(@Body postObj: JsonObject): Response<JsonElement>

    @GET
    suspend fun getSignForQuestionnaire(@Url url: String): Response<ResponseBody>

    @Multipart
    @POST("api/ehr/v1/questionnaire/signature/{ques_map_id}/{entry_id}")
    suspend fun postSignForQuestionnaire(
        @Part file: MultipartBody.Part,
        @Path(value = "ques_map_id") quesMapId: String,
        @Path(value = "entry_id") entryId: String
    ): Response<JsonElement>

    //endregion

    //region feedback

    @GET("api/ehr/v1/questionnaire/{template_id}")
    fun fetchFeedbackQuestionnaire(@Path("template_id") templateId: String): Call<JsonElement>

    @POST("api/ehr/v1/questionnaire/answers")
    fun submitFeedbackQuestionnaire(@Body requestObj: JsonObject): Call<JsonElement>

    //endregion

    //region payment

    @FormUrlEncoded
    @POST("api/ehr/v1/paymentgateway/transactions")
    fun createPaymentTransaction(@Field("data") postObject: String): Call<JsonElement>

    @PUT("api/ehr/v1/paymentgateway/transactions/{transaction_id}")
    fun updatePaymentTransaction(@Path("transaction_id") transactionId: String): Call<JsonElement>

    //endregion

    //region Support

    @Multipart
    @POST("api/ehr/v1/secure_sharing")
    suspend fun uploadSupportAttachments(@Part files: List<MultipartBody.Part>): Response<JsonElement>

    @POST("api/ehr/v1/support")
    suspend fun sendSupport(@Body postBody: JsonObject): Response<JsonElement>

    //endregion

}

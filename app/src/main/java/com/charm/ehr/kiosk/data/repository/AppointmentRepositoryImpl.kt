package com.charm.ehr.kiosk.data.repository
import com.charm.ehr.kiosk.domain.repository.AppointmentRepository

import android.content.Context
import com.charm.ehr.kiosk.domain.Appointment
import com.charm.ehr.kiosk.common.util.MdParsingHelper
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.data.network.WebService
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.common.util.CharmAPI
import com.charm.ehr.kiosk.common.util.ModelUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import org.joda.time.LocalDate
import javax.inject.Inject

class AppointmentRepositoryImpl @Inject constructor(
    private val webService: WebService, private val authManager: AuthManager,
    @ApplicationContext   private val appContext: Context,
    val charmAPI: CharmAPI
)  : AppointmentRepository{

    override suspend fun fetchAppointments(
        patientId: String,
        dateForTheAppointment: LocalDate
    ): APIState<ArrayList<Appointment>> {

        return charmAPI.executeApiCall(
            call = {
                webService.fetchAppointments(
                    patientId, authManager.getFacilityId(),
                    dateForTheAppointment.toString(ModelUtil.charmApiDateFormat())
                )
            },
            onSuccess = {
                MdParsingHelper.formAppointmentList(it)
            },
        )
    }

    /*   fun addAppointments(appointment: Appointment, allowMultipleBooking: Boolean = false): LiveData<Any> {

           val addAppointmentLive = MutableLiveData<Any>()

           if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {

               val postObj = MdJsonBuilder.formAppointmentPostObj(appointment, allowMultipleBooking)
               if (postObj != null) {

                   Log.d("AppointmentRepository", "addAppointments post obj ${postObj}")

                   val addAppointmentsCall = webService.addAppointment(postObj)

                   apiUtil.initiateRequest(addAppointmentsCall, object : MdNetworkUtil.MdCallback(authManager) {

                       fun onMdSuccess(respObj: JsonObject) {

                           val obj = respObj.get("appointment")?.asJsonObject
                           val appointmentId = obj?.get("appointment_id")?.asString

                           if (appointmentId != null) {

                               appointment.appointmentId = appointmentId
                               addAppointmentLive.postValue(appointment)

                           } else addAppointmentLive.postValue(
                               APIResponseError(
                                   null,
                                   null,
                                   ModelUtil.conversionException
                               )
                           ) //charm code:91 already booked

                       }

                       fun onMdFailure(error: APIResponseError) {
                           addAppointmentLive.postValue(error)
                       }

                   })

               } else addAppointmentLive.postValue(APIResponseError(null, null, ModelUtil.conversionException))

           } else addAppointmentLive.postValue(
               APIResponseError(
                   ModelUtil.internetErrorCode,
                   null,
                   appContext.getString(R.string.members_fetch_offline_msg)
               )
           )

           return addAppointmentLive

       }

       fun getDepartmentList(facilityId: String, withParsedResponse: Boolean ): MutableLiveData<Any?> {

           val getDepartmentListRespLive = MutableLiveData<Any?>()


           if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {

               Log.d("AppointmentRepository", "getDepartmentList - facilityId${facilityId}")
               val fetchDepartmentsCall = webService.fetchDepartments(facilityId)

               apiUtil.initiateRequest(fetchDepartmentsCall, object : MdNetworkUtil.MdCallback(authManager) {

                   fun onMdSuccess(respObj: JsonObject) {

                       val jsonArr = respObj.get("output_string")?.asJsonArray
                       val code = respObj?.get("code")?.asInt

                       if (jsonArr != null) {
                           if (withParsedResponse) {
                               getDepartmentListRespLive.postValue(MdParsingHelper.getDepartmentsListFrom(jsonArr))
                           } else {
                               getDepartmentListRespLive.postValue(jsonArr) //parsing will done on appointment activity
                           }
                       } else {
                           getDepartmentListRespLive.postValue(APIResponseError(code, null, null))
                       }
                   }

                   fun onMdFailure(error: APIResponseError) {
                       getDepartmentListRespLive.postValue(error)
                   }

               })

           } else {
               getDepartmentListRespLive.postValue(
                   APIResponseError(
                       ModelUtil.internetErrorCode,
                       null,
                       appContext.getString(R.string.offline_msg)
                   )
               )
           }



           return getDepartmentListRespLive

       }

       fun generateToken(appointmentId: String): LiveData<Any> {

           val generateTokenLive = MutableLiveData<Any>()

           if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {

               val generateTokenCall = webService.generateToken(appointmentId)

               apiUtil.initiateRequest(generateTokenCall, object : MdNetworkUtil.MdCallback(authManager) {
                   fun onMdSuccess(respObj: JsonObject) {
                       generateTokenLive.value = MdParsingHelper.getToken(respObj)
                   }

                   fun onMdFailure(error: APIResponseError) {
                       generateTokenLive.value = error
                   }

               })

           } else {
               generateTokenLive.postValue(
                   APIResponseError(
                       ModelUtil.internetErrorCode,
                       null,
                       appContext.getString(R.string.noInternetConnection)
                   )
               )
           }

           return generateTokenLive

       }

       fun fetchMembersByDepartment(departmentId: String, onlineAppointmentsEnabled: Boolean ): LiveData<Any> {

           val membersListLive = MutableLiveData<Any>()


           if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {

               Log.d(
                   "AppointmentRepository",
                   "fetchMembersByDepartment - departmentId${departmentId} facilityId -${authManager.getFacilityId()}"
               )
               val fetchMembersCall =
                   webService.fetchMembersByDepartment(departmentId, authManager.getFacilityId(), onlineAppointmentsEnabled)
               apiUtil.initiateRequest(fetchMembersCall, object : MdNetworkUtil.MdCallback(authManager) {

                   fun onMdSuccess(respObj: JsonObject) {

                       val respArr = respObj.get("members")?.asJsonArray
                       val code = respObj?.get("code")?.asInt
                       if (respArr != null)
                           membersListLive.postValue(MdParsingHelper.formMembersListFromDepartmentResp(respArr))
                       else
                           membersListLive.postValue(APIResponseError(code, null, null))
                   }

                   fun onMdFailure(error: APIResponseError) {
                       membersListLive.postValue(error)
                   }
               })

           } else {
               membersListLive.postValue(
                   APIResponseError(
                       ModelUtil.internetErrorCode,
                       null,
                       appContext.getString(R.string.fetch_appointment_offine_failure)
                   )
               )
           }

           return membersListLive

       }

       fun fetchMemberWorkTimings(memberId: String): LiveData<Any> {

           val memberWorkTimingsLiveResp = MutableLiveData<Any>()


           if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {

               val fetchMemberWorkTimingCall = webService.fetchMemberWorkTiming(memberId, authManager.getFacilityId())
               apiUtil.initiateRequest(fetchMemberWorkTimingCall, object : MdNetworkUtil.MdCallback(authManager) {

                   fun onMdSuccess(respObj: JsonObject) {

                       val memWorkTimingArray = respObj.get("member_work_timings")?.asJsonArray
                       val code = respObj?.get("code")?.asInt

                       if (memWorkTimingArray != null) {

                           memberWorkTimingsLiveResp.postValue(MdParsingHelper.formMemberTimingList(memWorkTimingArray))

                       } else memberWorkTimingsLiveResp.postValue(APIResponseError(code, null, null))

                   }

                   fun onMdFailure(error: APIResponseError) {
                       memberWorkTimingsLiveResp.postValue(error)
                   }

               })

           } else {
               memberWorkTimingsLiveResp.postValue(
                   APIResponseError(
                       ModelUtil.internetErrorCode,
                       null,
                       appContext.getString(R.string.member_work_timings_offline_msg)
                   )
               )
           }

           return memberWorkTimingsLiveResp
       }

       fun getAppointmentSettings(memberId: String): LiveData<Any> {

           val getAppointmentSettingsLive = MutableLiveData<Any>()

           if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {

               val getAppointmentSettingsCall = webService.getAppointmentSettings(memberId, authManager.getFacilityId())
               apiUtil.initiateRequest(getAppointmentSettingsCall, object : MdNetworkUtil.MdCallback(authManager) {

                   fun onMdSuccess(respObj: JsonObject) {

                       val resArr = respObj.get("appointment_settings")?.asJsonObject
                       val code = respObj?.get("code")?.asInt

                       if (resArr != null) {

                           getAppointmentSettingsLive.postValue(MdParsingHelper.parseAppointmentSettings(resArr))

                       } else getAppointmentSettingsLive.postValue(APIResponseError(code, null, null))

                   }

                   fun onMdFailure(error: APIResponseError) {
                       getAppointmentSettingsLive.postValue(APIResponseError(null, null, null)) //error occurred
                   }
               })

           } else {

               getAppointmentSettingsLive.postValue(
                   APIResponseError(
                       ModelUtil.internetErrorCode,
                       null,
                       appContext.getString(R.string.fetchAppointmentOfflineMsg)
                   )
               )
           }


           return getAppointmentSettingsLive
       }

       fun changeAppointmentStatus(appointmentId: String, memberId: String): LiveData<Any> {
           val changeStatusLive = MutableLiveData<Any>()

           if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {

               val postObj =
                   MdJsonBuilder.formChangeStatusPostObj(appointmentId, authManager.kioskSettingsFlow.value?.checkinStatusId ?: "", memberId)

               Log.d("AppointmentRepository", "changeAppointmentStatus - postObj${postObj}")
               val changeStatusCall = webService.changeStatus(postObj)

               apiUtil.initiateRequest(changeStatusCall, object : MdNetworkUtil.MdCallback(authManager) {
                   fun onMdSuccess(respObj: JsonObject) {
                       changeStatusLive.postValue(true)
                   }

                   fun onMdFailure(error: APIResponseError) {
                       changeStatusLive.postValue(error)
                   }
               })

           } else {
               changeStatusLive.postValue(
                   APIResponseError(
                       ModelUtil.internetErrorCode,
                       null,
                       appContext.getString(R.string.change_status_offline_msg)
                   )
               )
           }

           return changeStatusLive
       }

       fun getConfirmedAppointmentCount(memberId: String, startDateTime: DateTime, endDateTime: DateTime): LiveData<Any> {
           val confirmedAppointmentCountLive = MutableLiveData<Any>()

           if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {
               val confirmedAppointmentCountCall = webService.getConfirmedAppointmentsCount(
                   memberId,
                   authManager.getFacilityId(),
                   startDateTime.toString(ModelUtil.sqlDateTimeFormatForGettingAppointmentCount()),
                   endDateTime.toString(ModelUtil.sqlDateTimeFormatForGettingAppointmentCount())
               )
               apiUtil.initiateRequest(confirmedAppointmentCountCall, object : MdNetworkUtil.MdCallback(authManager) {
                   fun onMdSuccess(respObj: JsonObject) {
                       val appointmentCount = MdParsingHelper.getConfirmedAppointmentCount(respObj)
                       confirmedAppointmentCountLive.value = appointmentCount ?: APIResponseError(null, null, null)
                   }

                   fun onMdFailure(error: APIResponseError) {
                       confirmedAppointmentCountLive.value = error
                   }
               })
           } else {
               confirmedAppointmentCountLive.postValue(
                   APIResponseError(
                       ModelUtil.internetErrorCode,
                       null,
                       appContext.getString(R.string.noInternetConnection)
                   )
               )
           }

           return confirmedAppointmentCountLive
       }

       //region payment

       fun createPaymentTransaction(paymentTransaction: PaymentTransaction): LiveData<Any> {

           //Note: doing all the check for paymentTransaction in activity it self

           val liveResp = MutableLiveData<Any>()

           if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {

               val postObj = MdJsonBuilder.createPostObjForPaymentTransaction(paymentTransaction)

               Log.d("MdCallback", "transaction $postObj")
               val createPaymentTransactionCall = webService.createPaymentTransaction(postObj.toString())

               apiUtil.initiateRequest(createPaymentTransactionCall, object : MdNetworkUtil.MdCallback(authManager) {

                   fun onMdSuccess(respObj: JsonObject) {

                       val code = respObj.get("code")?.asInt
                       val transactionObj = respObj.get("transaction")?.asJsonObject

                       val transactionId = transactionObj?.get("transaction_id")?.asString?.trim()
                       val razorPayOrderId = transactionObj?.get("razorpay_order_id")?.asString?.trim()
                       val clientId = transactionObj?.get("client_id")?.asString
                       val publicToken = transactionObj?.get("public_token")?.asString?.trim()
                       val amount = transactionObj?.get("amount")?.asString

                       if (!publicToken.isNullOrBlank() && !transactionId.isNullOrBlank() && !razorPayOrderId.isNullOrBlank()) {

                           paymentTransaction.razorPayOrderId = razorPayOrderId
                           paymentTransaction.clientId = clientId
                           paymentTransaction.publicToken = publicToken
                           paymentTransaction.transactionId = transactionId

                           liveResp.value = paymentTransaction

                       } else liveResp.value = APIResponseError(code, null, null)

                   }

                   fun onMdFailure(error: APIResponseError) {
                       liveResp.value = APIResponseError(null, null, null)
                   }

               })

           } else liveResp.value = APIResponseError(ModelUtil.internetErrorCode, null, appContext.getString(R.string.errorPaymentOfflineMsg))

           return liveResp

       }

       fun updatePaymentTransaction(transactionId: String): LiveData<Any> {

           val liveResp = MutableLiveData<Any>()

           if (authManager.userStatusStateFlow.value == UserStatus.ONLINE) {

               val createPaymentTransactionCall = webService.updatePaymentTransaction(transactionId)

               apiUtil.initiateRequest(createPaymentTransactionCall, object : MdNetworkUtil.MdCallback(authManager) {

                   fun onMdSuccess(respObj: JsonObject) {

                       val code = respObj.get("code")?.asInt
                       val transactionObj = respObj.get("transaction")?.asJsonObject

                       val facilityId = transactionObj?.get("facility_id")?.asString?.trim()
                       val patientId = transactionObj?.get("patient_id")?.asString?.trim()
                       val receiptId = transactionObj?.get("receipt_id")?.asString?.trim()
                       val transactionId = transactionObj?.get("transaction_id")?.asString?.trim()

                       liveResp.value = if (!receiptId.isNullOrBlank()) {
                           receiptId
                       } else APIResponseError(code, null, null)

                   }

                   fun onMdFailure(error: APIResponseError) {
                       liveResp.value = APIResponseError(null, null, null)
                   }

               })

           } else liveResp.value = APIResponseError(ModelUtil.internetErrorCode, null, appContext.getString(R.string.errorPaymentOfflineMsg))

           return liveResp

       }

       fun getAmountForAppointment() { // need to ask

       }

       //endregion*/
    override suspend fun updateAppointmentStatus(
        appointmentId: String,
        statusId: String
    ): APIState<Boolean> {
        return charmAPI.executeApiCall(
            call = {
                webService.updateAppointmentStatus(
                    appointmentId = appointmentId,
                    statusId = statusId
                )
            },
            onSuccess = {
                true
            }
        )
    }

}
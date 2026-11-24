package com.charm.ehr.kiosk.common.util

import android.util.Log
import com.charm.ehr.kiosk.domain.Appointment
import com.charm.ehr.kiosk.domain.Facility
import com.charm.ehr.kiosk.domain.FeedbackLanguageQuestionnaire
import com.charm.ehr.kiosk.domain.FeedbackPOJO
import com.charm.ehr.kiosk.domain.Member
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.domain.PaymentTransaction
import com.charm.ehr.kiosk.domain.Question
import com.charm.ehr.kiosk.domain.QuestionnaireMetaData
import com.charm.ehr.kiosk.domain.QuestionnairePhysicianTemplate
import com.charm.ehr.kiosk.domain.SOAPEntry
import com.charm.ehr.kiosk.domain.Service
import com.charm.ehr.kiosk.presentation.edit_profile.Insurance
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.joda.time.DateTime

class MdJsonBuilder {

    companion object {

        //region patient

        fun formAddPatient(patient: Patient, facilityId: String, duplicateCheck: Boolean = true): JsonObject {
            val contactDetPOJO =
                patient.contactDetails //todo: aadhar and passport number to be added
            val postObj = JsonObject()
            postObj.addProperty("first_name", patient.firstName)
            postObj.addProperty("last_name", patient.lastName)
            postObj.addProperty("gender", patient.gender?.lowercase())
            postObj.addProperty("gender_identity", patient.genderIdentity?.lowercase())
            postObj.addProperty("duplicate_check", duplicateCheck)

            if (patient.age != null) {
                postObj.addProperty("age", patient.age)
            } else {
                postObj.addProperty(
                    "dob",
                    patient.dateOfBirth?.toString(ModelUtil.charmApiDateFormat())
                )
            }
            postObj.addProperty("mobile", contactDetPOJO?.mobile)
            postObj.addProperty("email", contactDetPOJO?.emailId)

            val facArr = JsonArray()
            //todo: to be uncommented
            /*  val facilityList = arrayListOf<Facility>()
              facilityList.forEach {facility->
                  val facilityObj = JsonObject()
                  facilityObj.addProperty("facility_id" , facility.facilityId)
                  facArr.add(facilityObj)
              }*/
            val facility = JsonObject()
            facility.addProperty("facility_id", facilityId)
            facArr.add(facility)
            postObj.add("facilities", facArr)
            postObj.add("id_qualifiers", patient.idQualifiers)

            /*

                        val contactDetails = patient.contactDetails

                        postObj.addProperty("preferred_communication", contactDetails?.preferredCommunication ?: "")

                        postObj.addProperty("primary_phone", contactDetails?.primaryPhone ?: "")
                        postObj.addProperty("home_phone", contactDetails?.homePhone ?: "")
                        postObj.addProperty("blood_group", patient.bloodGroup ?: "")
                        postObj.addProperty("marital_status", patient.maritalStatus ?: "")
                        postObj.addProperty("smoking_status", patient.smokingStatus ?: "")
                        postObj.addProperty("emergency_contact_name", patient.emergencyContactName ?: "")
                        postObj.addProperty("emergency_contact_number", patient.emergencyContactNumber ?: "")
                        postObj.addProperty("emergency_extn", patient.emergencyContactExtn ?: "")
                        postObj.addProperty("id_qualifier", patient.idQualifier?.toString() ?: "")
            */
            /*
                        postObj.addProperty("id_of_patient", patient.idOfPatient ?: "")
            *//*

            postObj.addProperty("primary_care_provider", patient.primaryCareProvider ?: "")
            postObj.addProperty("secondary_care_provider", patient.secondaryCareProvider ?: "")
            postObj.addProperty("cargiver_first_name", patient.caregiverFirstName ?: "")
            postObj.addProperty("cargiver_last_name", patient.caregiverLastName ?: "")

*/

            val addressObj = JsonObject()

            val postalAddress = patient.postalAddress


            addressObj.addProperty("address_line1", postalAddress?.line1 ?: "")
            addressObj.addProperty("address_line2", postalAddress?.line2 ?: "")

            addressObj.addProperty("area", postalAddress?.area ?: "")
            addressObj.addProperty("city", postalAddress?.city ?: "")
            addressObj.addProperty("state", postalAddress?.state ?: "")
            addressObj.addProperty("district", postalAddress?.district ?: "")
            addressObj.addProperty("county_code", postalAddress?.county ?: "")
            addressObj.addProperty("country", postalAddress?.country ?: "")
            addressObj.addProperty("zip_code", postalAddress?.postalCode ?: "")

            postObj.add("address", addressObj)




            return postObj

        }

        fun formEditPatient(patPOJO: Patient?): JsonObject {

            val postObj = JsonObject()
            val contactDetPOJO = patPOJO?.contactDetails

            postObj.addProperty("update_specific_details", true)
            postObj.addProperty("first_name", patPOJO?.firstName)
            postObj.addProperty("last_name", patPOJO?.lastName)
            postObj.addProperty("gender", patPOJO?.gender?.lowercase())
            postObj.addProperty("blood_group", patPOJO?.bloodGroup)
            postObj.addProperty("race", patPOJO?.race)
            postObj.addProperty("ethnicity", patPOJO?.ethnicity)
            postObj.addProperty("language", patPOJO?.language)
            postObj.addProperty("smoking_status", patPOJO?.smokingStatus)
            postObj.addProperty("employment_status", patPOJO?.employmentStatus)
            postObj.addProperty("marital_status", patPOJO?.maritalStatus)
            postObj.addProperty("gender_identity", patPOJO?.genderIdentity?.lowercase())
            if (patPOJO?.age != null) {
                postObj.addProperty("age", patPOJO.age)
            } else {
                postObj.addProperty(
                    "dob",
                    patPOJO?.dateOfBirth?.toString(ModelUtil.charmApiDateFormat())
                )
            }
            postObj.addProperty("mobile", contactDetPOJO?.mobilePhone)
            postObj.addProperty("record_id", patPOJO?.patientRecordId)
            postObj.addProperty("email", contactDetPOJO?.emailId)
            postObj.addProperty("preferred_communication", contactDetPOJO?.preferredCommunication)
            postObj.addProperty("primary_phone", contactDetPOJO?.primaryPhone)
            postObj.addProperty("work_phone", contactDetPOJO?.workPhone)
            postObj.addProperty("work_phone_extn", contactDetPOJO?.workPhoneExtn)
            postObj.addProperty("home_phone", contactDetPOJO?.homePhone)
            postObj.addProperty("emergency_contact_name", patPOJO?.emergencyContactName)
            postObj.addProperty("emergency_contact_number", patPOJO?.emergencyContactNumber)
            postObj.addProperty("emergency_extn", patPOJO?.emergencyContactExtn)


            val postalAddress = patPOJO?.postalAddress

            val addressObj = JsonObject()
            addressObj.addProperty("address_line1", postalAddress?.line1)
            addressObj.addProperty("address_line2", postalAddress?.line2)
            addressObj.addProperty("area", postalAddress?.area)
            addressObj.addProperty("city", postalAddress?.city)
            addressObj.addProperty("state", postalAddress?.state)
            addressObj.addProperty("county_code", postalAddress?.county)
            addressObj.addProperty("district", postalAddress?.district)
            addressObj.addProperty("country", postalAddress?.country)
            addressObj.addProperty("zip_code", postalAddress?.postalCode)


            postObj.add("address", addressObj)
            postObj.add("id_qualifiers", patPOJO?.idQualifiers)

            // Add facilities array similar to formAddPatient
            val facArr = JsonArray()
            val facility = JsonObject()
            facArr.add(facility)
            postObj.add("facilities", patPOJO?.facilityListForEdit)

            // Add caregivers array
            val caregiversArr = JsonArray()
            patPOJO?.caregivers?.forEach { caregiver ->
                val caregiverObj = JsonObject()
                caregiverObj.addProperty("relationship", caregiver.relationship)
                caregiverObj.addProperty("first_name", caregiver.firstName)
                caregiverObj.addProperty("last_name", caregiver.lastName)
                if (!caregiver.dateOfBirth.isNullOrBlank()) {
                    DateUtil.getDate(caregiver.dateOfBirth, ModelUtil.mothFirstDateFormatWithSlash())
                        ?.let {
                            caregiverObj.addProperty(
                                "dob",
                                it.toString(ModelUtil.charmApiDateFormat())
                            )
                        }
                } else {
                    caregiverObj.addProperty("dob", "")
                }
                caregiverObj.addProperty("gender", caregiver.gender?.lowercase())
                caregiverObj.addProperty("same_as_patient_contact", caregiver.sameAsPatContact)
                if (caregiver.sameAsPatContact != true) {
                    // Address
                    val addressObj = JsonObject()
                    addressObj.addProperty("address_line1", caregiver.postalAddress?.line1 ?: "")
                    addressObj.addProperty("address_line2", caregiver.postalAddress?.line2 ?: "")
                    addressObj.addProperty("city", caregiver.postalAddress?.city ?: "")
                    addressObj.addProperty("state", caregiver.postalAddress?.state ?: "")
                    addressObj.addProperty("country", caregiver.postalAddress?.country ?: "")
                    addressObj.addProperty("zip_code", caregiver.postalAddress?.postalCode ?: "")
                    caregiverObj.add("address", addressObj)
                    // Contact
                    val contactObj = JsonObject()
                    contactObj.addProperty("home_phone", caregiver.contactDetails?.homePhone ?: "")
                    contactObj.addProperty("mobile", caregiver.contactDetails?.mobilePhone ?: "")
                    caregiverObj.add("contact", contactObj)
                }
                caregiversArr.add(caregiverObj)
            }
            postObj.add("caregivers", caregiversArr)

            postObj.addProperty("update_specific_details", true)

            return postObj
        }

        //endregion

        //region otp

        fun formSendOTPObj(
            mobileNumber: String,
            messageKey: String,
            countryCode: String
        ): JsonObject {
            val jsonObject = JsonObject()
            jsonObject.addProperty("mobile_no", mobileNumber)
            jsonObject.addProperty("message_key", messageKey)
            jsonObject.addProperty("country_code", countryCode)

            return jsonObject
        }

        fun formResendOTPObj(
            requestId: String,
            messageKey: String
        ): JsonObject {
            val jsonObject = JsonObject()

            jsonObject.addProperty("request_id", requestId)
            jsonObject.addProperty("message_key", messageKey)

            return jsonObject
        }

        fun formVerifyOTPObj(requestId: String, otp: String): JsonObject {
            val jsonObject = JsonObject()

            jsonObject.addProperty("request_id", requestId)
            jsonObject.addProperty("otp", otp)

            return jsonObject
        }

        //endregion

        //region appointment

        fun formAppointmentPostObj(
            appointment: Appointment,
            allowMultipleBooking: Boolean = false
        ): JsonObject? {

            val postObj = JsonObject()
            val memberId = appointment.member?.memberId?.trim()
            val patientId = appointment.patient?.practicePatientId?.trim()
            val facilityId = appointment.facilityId?.trim()
            val startTime =
                appointment.appointmentStartTime?.toString(ModelUtil.md12HrTimeFormator())
            val startDate =
                appointment.appointmentDate?.toString(ModelUtil.charmApiDateFormat())?.trim()
            val durationInMin = appointment.durationInMinutes?.toString()?.trim()

            if (!memberId.isNullOrBlank() && !patientId.isNullOrBlank() &&
                !facilityId.isNullOrBlank() && !startTime.isNullOrBlank() &&
                !startDate.isNullOrBlank() && !durationInMin.isNullOrBlank()
            ) {

                postObj.addProperty("member_id", memberId)
                postObj.addProperty("patient_id", patientId)
                postObj.addProperty("facility_id", facilityId)
                postObj.addProperty("mode", "In Person")
                postObj.addProperty("repetition", "Single Date")
                postObj.addProperty("start_time", startTime)
                postObj.addProperty("start_date", startDate)
                postObj.addProperty("duration_in_minutes", durationInMin)
                if (allowMultipleBooking) {
                    postObj.addProperty("provider_double_booking", "allow")
                }
            } else {
                return null
            }


            if (!appointment.receiptId.isNullOrBlank()) {
                postObj.addProperty("receipt_id", appointment.receiptId)
            }

            /*      "member_id": "100000000000000161",/////
                  "patient_id": "100000000000000169",////
                  "facility_id": "100000000000000179",///
                  "mode": "In Person/Phone Call",//
                  "repetition": "Single Date/Period",////
                  "start_time": "10:40 AM",
                  "start_date": "2015-01-29",
                  "duration_in_minutes": "20",///
                 */



            return postObj
        }

        //endregion

        //region Appointment

        fun formGenerateTokenObj(appointmentId: String, statusId: String): JsonObject {
            val postObj = JsonObject()

            postObj.addProperty("appointment_id", appointmentId)
            postObj.addProperty("status_id", statusId)

            return postObj
        }

        fun formChangeStatusPostObj(
            appointmentId: String,
            statusId: String,
            memberId: String
        ): JsonObject {
            val postObj = JsonObject()

            postObj.addProperty("appointment_id", appointmentId)
            postObj.addProperty("status_id", statusId)
            postObj.addProperty("member_id", memberId)

            return postObj
        }

        //endregion

        //region Questionnaire

        fun formUpdateQuestionnaireParams(
            patientId: String,
            quesToRemove: List<QuestionnairePhysicianTemplate>,
            quesToAdd: List<QuestionnairePhysicianTemplate>
        ): MutableMap<String, String> {

            val params = mutableMapOf<String, String>()
            params["PRACTICE_PATIENT_ID"] = patientId

            quesToRemove
                .filter {
                    it.patientQuesMap?.patientQuesMapId?.isNotBlank() == true
                }
                .forEachIndexed { index, ques ->
                    params["UNSHARE_PATIENT_QUES_MAP_ID_$index"] =
                        ques.patientQuesMap?.patientQuesMapId ?: ""
                }

            quesToAdd
                .forEachIndexed { index, ques ->
                    params["TEMPLATE_ID_$index"] = ques.templateId ?: ""
                }

            return params

        }

        //endregion

        fun formFeedbackSubmitArr(feedbackPOJOList: ArrayList<FeedbackPOJO>): JsonObject {
            val reqObj = JsonObject()
            reqObj.addProperty("template_id", feedbackPOJOList.first().feedbackEntry.templateId)

            if (!feedbackPOJOList.firstOrNull()?.patientId.isNullOrBlank()) {
                reqObj.addProperty("patient_id", feedbackPOJOList.first().patientId)
            }

            if (!feedbackPOJOList.firstOrNull()?.providerId.isNullOrBlank()) {
                reqObj.addProperty("provider_id", feedbackPOJOList.first().providerId)
            }
            /*  else if(!feedbackPOJOList.firstOrNull()?.departmentId.isNullOrBlank()){
                  reqObj.addProperty("department_id" , feedbackPOJOList.first().departmentId)
              }
  */
            val feedbackArray = JsonArray()
            feedbackPOJOList.forEach { feedbackPOJO ->

                val answerList = feedbackPOJO.feedbackEntry.answersList

                if (answerList != null) {
                    if (answerList.isNotEmpty()) {
                        val entryId = feedbackPOJO.feedbackEntry.entryId
                        if (answerList.size == 1) {
                            val feedbackObj = JsonObject()
                            feedbackObj.addProperty("entry_id", entryId)
                            feedbackObj.addProperty("answer", answerList[0])
                            feedbackArray.add(feedbackObj)
                        } else {
                            answerList.forEach { answer ->
                                val feedbackObj = JsonObject()
                                feedbackObj.addProperty("entry_id", entryId)
                                feedbackObj.addProperty("answer", answer)
                                feedbackObj.addProperty("is_multi_choice", true)
                                feedbackArray.add(feedbackObj)
                            }

                        }
                    }
                }
            }

            reqObj.add("answers", feedbackArray)



            return reqObj
        }

        //region shared pref db parse

        fun formFacilityObj(facility: Facility): String {

            val facilityObj = JsonObject()


            facilityObj.addProperty("facility_id", facility.facilityId ?: "")
            val postalAddress = facility.postalAddress
            facilityObj.addProperty("facility_name", facility.facilityName ?: "")

            facilityObj.addProperty("city", postalAddress?.city ?: "")


            facilityObj.addProperty("state", postalAddress?.state ?: "")
            facilityObj.addProperty("country", postalAddress?.country ?: "")

            return facilityObj.toString()
        }

        fun formServicesList(serviceList: List<Service>): String {

            val serviceArr = JsonArray()

            serviceList.forEach { service ->
                val servicesObj = JsonObject()
                servicesObj.addProperty("service_id", service.serviceId ?: "")
                servicesObj.addProperty("logo", service.logo ?: "")
                servicesObj.addProperty("service_label", service.serviceLabel ?: "")
                servicesObj.addProperty("service_url", service.serviceURL ?: "")
                serviceArr.add(servicesObj)
            }

            return serviceArr.toString()
        }

        fun formFeedbackLanguageQuestionaireList(feedbackLanguageQuestionnaireList: List<FeedbackLanguageQuestionnaire>): String {

            val feedbackLanguageQuestionaireArr = JsonArray()

            feedbackLanguageQuestionnaireList.forEach { feedbackLanguageQuestionnaire ->
                val feedbackObj = JsonObject()
                feedbackObj.addProperty("language", feedbackLanguageQuestionnaire.language)
                feedbackObj.addProperty(
                    "questionnaireId",
                    feedbackLanguageQuestionnaire.questionnaireId ?: ""
                )
                feedbackObj.addProperty(
                    "questionnaireLabel",
                    feedbackLanguageQuestionnaire.questionnaireLabel ?: ""
                )
                feedbackLanguageQuestionaireArr.add(feedbackObj)
            }


            return feedbackLanguageQuestionaireArr.toString()
        }

        fun formSelectedMemberStr(memberList: ArrayList<Member>): String {
            val memberArr = JsonArray()
            memberList.forEach { member ->

                val memberObj = JsonObject()

                memberObj.addProperty("member_id", member.memberId ?: "")
                memberObj.addProperty("prefix", member.prefix ?: "")
                memberObj.addProperty("first_name", member.firstName ?: "")
                memberObj.addProperty("last_name", member.lastName ?: "")
                memberObj.addProperty("full_name", member.fullName ?: "")
                memberObj.addProperty("gender", member.gender ?: "")
                memberObj.addProperty("specialization", member.specialization ?: "")
                memberObj.addProperty("degree", member.degree ?: "")
                if (member.isPhotoAvailable != null)
                    memberObj.addProperty("is_member_photo_available", member.isPhotoAvailable)

                if (member.isMemberOnlineAppEnabled != null)
                    memberObj.addProperty(
                        "is_member_online_app_enabled",
                        member.isMemberOnlineAppEnabled
                    )

                memberObj.addProperty("introduction", member.introduction ?: "")

                val contactDetails = member.contactDetails

                memberObj.addProperty("home_phone", contactDetails?.homePhone ?: "")
                memberObj.addProperty("mobile", contactDetails?.mobile ?: "")
                memberObj.addProperty("email", contactDetails?.emailId ?: "")

                val postalAddress = member.postalAddress

                memberObj.addProperty("address_line1", postalAddress?.line1 ?: "")
                memberObj.addProperty("address_line2", postalAddress?.line2 ?: "")
                memberObj.addProperty("city", postalAddress?.city ?: "")
                memberObj.addProperty("state", postalAddress?.state ?: "")
                memberObj.addProperty("country", postalAddress?.country ?: "")
                memberObj.addProperty("zip_code", postalAddress?.postalCode ?: "")

                memberArr.add(memberObj)
            }

            return memberArr.toString()
        }

        //endregion

        //region

        fun createPostObjForPaymentTransaction(paymentTransaction: PaymentTransaction): JsonObject {

            val postObj = JsonObject()

            val facilityId = paymentTransaction.facilityId
            val patientId = paymentTransaction.patientId
            val merchantAccountId = paymentTransaction.merchantAccountId
            val paymentAmount = paymentTransaction.paymentAmount // 2.00 - is two rupees
            val memberId = paymentTransaction.memberId

            postObj.addProperty("patient_id", patientId)
            postObj.addProperty("facility_id", facilityId)
            postObj.addProperty("member_id", memberId)
            postObj.addProperty("merchant_account_id", merchantAccountId)
            postObj.addProperty("amount", String.format("%.2f", paymentAmount))
            postObj.addProperty("service", "KIOSK_BILLING")

            val formPostObj = JsonObject()

            formPostObj.addProperty("is_patient_payment", true)
            formPostObj.addProperty(
                "receipt_date",
                DateTime.now().toString(ModelUtil.charmApiDateFormat())
            )
            formPostObj.addProperty("receipt_amount", paymentAmount)
            formPostObj.addProperty("payment_method", "Card")
            formPostObj.addProperty("facility_id", facilityId)
            formPostObj.addProperty(
                "description",
                "Consultation fees for ${UIUtil.formMemberName(paymentTransaction.member)}"
            )
            formPostObj.addProperty("patient_id", patientId)
            formPostObj.add("invoices", JsonArray())

            postObj.add("form_data", formPostObj)


            return postObj

        }

        //endregion

        fun getAnswerJson(entryId: String, answer: String): JsonObject {
            val obj = JsonObject()
            obj.addProperty("entry_id", entryId)
            obj.addProperty("answer", answer)
            return obj
        }

        fun getSOAPAnswerJson(entryId: String, answer: String): JsonObject {
            val soapEntryObj = JsonObject()
            soapEntryObj.addProperty("soap_entry_id", entryId)
            soapEntryObj.addProperty("soap_entry_value", answer)

            return soapEntryObj
        }

        fun getAnswerArrJson(entryId: String, answer: JsonArray): JsonObject {
            val obj = JsonObject()
            obj.addProperty("entry_id", entryId)
            obj.add("multiple_answers", answer)
            return obj
        }

        fun getAnswerForSOAPQuestion(entry: SOAPEntry): JsonElement? {
            when (entry) {
                is SOAPEntry.HeadingEntry -> {
                    if (entry.isMandatory) {
                        return getSOAPAnswerJson(entry.soapEntryId ?: "", entry.soapEntryName ?: "")
                    }
                }

                else -> {
                    if (!entry.answer.isNullOrBlank()) {
                        return getSOAPAnswerJson(
                            entry.soapEntryId ?: "",
                            entry.answer ?: ""
                        )
                    }
                }
            }
            return null
        }

        fun getAnswerForQuestion(question: Question): JsonElement? {
            when (question) {
                is Question.SimpleQuestion, is Question.Label -> {
                    return getAnswerJson(
                        question.entryId ?: "",
                        question.answer ?: ""
                    )
                }

                is Question.RatingScaleQuestion, is Question.YesOrNoQuestion -> {
                    if (!question.answer.isNullOrBlank()) {
                        return getAnswerJson(
                            question.entryId ?: "",
                            question.answer ?: ""
                        )
                    }
                }

                is Question.QuestionWithOptions -> {
                    if (question.isMultiChoice) {
                        val multiChoiceArray = JsonArray()
                        if (question.multipleAnswer.isNotEmpty()) {
                            question.multipleAnswer.forEach {
                                val ansObj = JsonObject()
                                ansObj.addProperty("answer", it)
                                multiChoiceArray.add(ansObj)
                            }
                            return getAnswerArrJson(
                                question.entryId ?: "",
                                multiChoiceArray
                            )
                        }

                    } else {
                        if (!question.answer.isNullOrBlank()) {
                            return getAnswerJson(
                                question.entryId ?: "",
                                question.answer ?: ""
                            )
                        }
                    }
                }

                is Question.DateQuestion -> {
                    var answer = question.answer
                    if (answer != null) {
                        if (answer.isNotBlank()) {
                            answer =
                                DateUtil.getDate(answer, ModelUtil.mothFirstDateFormatWithSlash())
                                    ?.toString(ModelUtil.charmDisplayDateFormat())
                        }
                        return getAnswerJson(
                            question.entryId ?: "",
                            answer ?: ""
                        )
                    }

                }

                is Question.SOAPWidgetQuestion -> {
                    val obj = JsonObject()
                    obj.addProperty("entry_id", question.entryId)
                    val soapEntries = JsonObject()
                    soapEntries.addProperty("soap_widget_id", question.soapWidget.soapWidgetId)
                    soapEntries.addProperty(
                        "soap_template_id",
                        question.soapWidget.soapTemplateId
                    )
                    val soapWidgetAnswers = JsonArray()
                    question.soapWidget.entries.forEach { entry ->

                        getAnswerForSOAPQuestion(entry)?.let { answer ->
                            soapWidgetAnswers.add(answer)
                        }
                    }
                    soapEntries.add("soap_widget_answers", soapWidgetAnswers)
                    obj.add("soap_entries", soapEntries)
                    return obj
                }

                is Question.Section -> {
                    val answersArray = JsonArray()
                    question.questions.forEach { subQ ->
                        val answerObj = getAnswerForQuestion(subQ)
                        if (answerObj != null) {
                            answersArray.add(answerObj)
                        }
                    }
                    return answersArray
                }

                is Question.SignatureQuestion -> {}
            }
            return null
        }

        fun createQuestionnaireAnswers(
            questionnaireMetaData: QuestionnaireMetaData,
            patientId: String
        ): JsonObject {
            val root = JsonObject()
            root.addProperty("template_id", questionnaireMetaData.templateId)
            root.addProperty("is_submitted", questionnaireMetaData.isSubmitted.toString())
            root.addProperty("patient_id", patientId)
            root.addProperty("ques_map_id", questionnaireMetaData.quesMapId)

            val answersArray = JsonArray()

            questionnaireMetaData.questions.forEach { question ->
                val answerObj = getAnswerForQuestion(question)
                if (answerObj != null && answerObj.isJsonObject) {
                    answersArray.add(answerObj)
                } else if (answerObj != null && answerObj.isJsonArray) {
                    answersArray.addAll(answerObj as JsonArray?)
                }
            }

            root.add("answers", answersArray)
            return root
        }

        fun formInsuranceJson(insurance: Insurance): JsonObject {
            val insuranceJson = JsonObject()
            val person = insurance.insuredPersonDetail

            return insuranceJson.apply {
                person?.let {
                    addProperty("insured_person_gender", person.birthSex.value?.lowercase() ?: "")
                    addProperty("insured_person_state", person.state.value?.value ?: "")
                    //addProperty("date_of_signature", "")
                    addProperty("insured_person_city", person.city.value ?: "")
                    addProperty("insured_person_mobile_phone", person.mobilePhone.value ?: "")
                    addProperty("insured_person_home_phone", person.homePhone.value ?: "")
                    addProperty("insured_person_country", person.country.value?.value ?: "")
                    addProperty("insured_person_last_name", person.lastName.value ?: "")
                    addProperty(
                        "insured_person_relationship",
                        person.relationship.value?.value ?: ""
                    )
                    addProperty(
                        "insured_person_dob",
                        DateUtil.getDate(
                            person.dateOfBirth.value,
                            ModelUtil.mothFirstDateFormatWithSlash()
                        )
                            ?.toString(ModelUtil.charmApiDateFormat()) ?: ""
                    )
                    addProperty("insured_person_first_name", person.firstName.value ?: "")
                    addProperty("insured_person_address_line_2", person.addressLine2.value ?: "")
                    addProperty("insured_person_zipcode", person.postalCode.value ?: "")
                    addProperty("insured_person_address_line_1", person.addressLine1.value ?: "")
                }

                //addProperty("payer_phone", "")
                addProperty("employer_name", insurance.employerOrSchoolName.value ?: "")
                // addProperty("payer_post_box", "")
                //addProperty("payer_zip_code", "")
                // addProperty("payer_address_line_1", "")
                addProperty(
                    "valid_to",
                    DateUtil.getDate(
                        insurance.validUntil.value,
                        ModelUtil.mothFirstDateFormatWithSlash()
                    )
                        ?.toString(ModelUtil.charmApiDateFormat()) ?: ""
                )
                addProperty("policy_group_number", insurance.policyGroupOrFECA.value ?: "")
                addProperty("payer_name", insurance.selectedPayer.value?.payerName ?: "")
                // addProperty("payer_state", "")
                addProperty("comments", insurance.comment.value ?: "")
                addProperty(
                    "insurance_category",
                    insurance.insuranceCategory.value
                )
                //addProperty("eligibility_payer_id", "")
                addProperty("insurance_plan_id", insurance.planId.value ?: "")
                // addProperty("payer_country", person.country?.value ?: "")
                addProperty(
                    "valid_from",
                    DateUtil.getDate(
                        insurance.validFrom.value,
                        ModelUtil.mothFirstDateFormatWithSlash()
                    )
                        ?.toString(ModelUtil.charmApiDateFormat()) ?: ""
                )
                // addProperty("additional_pr_details", "")
                //addProperty("payer_city", "")
                // addProperty("payer_fax", "")
                addProperty(
                    "deductible",
                    insurance.deductible.value
                )
                // addProperty("release_medical_info", "yes")
                //  addProperty("assign_medical_benefits", "yes")
                addProperty("insurance_plan_name", insurance.planOrProgramName.value ?: "")
                addProperty("practice_payer_id", insurance.selectedPayer.value?.practicePayerId)
                //  addProperty("insured_person_ssn", "")
                addProperty("insurance_type", insurance.insuranceType.value ?: "")
                addProperty("payer_id", insurance.selectedPayer.value?.payerId)
                addProperty("coinsurance", insurance.coinsurance.value ?: "")
                addProperty("copay", (insurance.copay.value))
            }
        }

        fun buildSupportJson(
            categoryTxt: String,
            subjectTxt: String,
            messageTxt: String,
            fileIds: String?
        ): JsonObject {

            val postObject = JsonObject()
            postObject.addProperty("msg_content", messageTxt)
            postObject.addProperty("subject", ModelUtil.kioskSupportHeader + subjectTxt)
            postObject.addProperty("category", categoryTxt)
            postObject.addProperty("msg_content_type", "text")

            //TODO: custom_subject_prefix needs be added

            if (!fileIds.isNullOrBlank()) {
                postObject.addProperty("file_ids", fileIds) //file_ids
            }

            return postObject
        }

    }
}

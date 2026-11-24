package com.charm.ehr.kiosk.common.util

import android.util.Log
import com.charm.ehr.kiosk.common.util.ModelUtil.Companion.parseStringAsJsonArray
import com.charm.ehr.kiosk.common.util.ModelUtil.Companion.toStringList
import com.charm.ehr.kiosk.domain.AccountDetails
import com.charm.ehr.kiosk.domain.Appointment
import com.charm.ehr.kiosk.domain.AppointmentSettings
import com.charm.ehr.kiosk.domain.AppointmentStatus
import com.charm.ehr.kiosk.domain.Caregiver
import com.charm.ehr.kiosk.domain.ConsentForm
import com.charm.ehr.kiosk.domain.ContactDetails
import com.charm.ehr.kiosk.domain.Country
import com.charm.ehr.kiosk.domain.DemographicsMetrics
import com.charm.ehr.kiosk.domain.DemographicsSetting
import com.charm.ehr.kiosk.domain.Department
import com.charm.ehr.kiosk.domain.DmCaregiverRelationship
import com.charm.ehr.kiosk.domain.DmCommonQualifier
import com.charm.ehr.kiosk.domain.DmEthnicity
import com.charm.ehr.kiosk.domain.DmGenderIdentity
import com.charm.ehr.kiosk.domain.DmLanguages
import com.charm.ehr.kiosk.domain.DmRace
import com.charm.ehr.kiosk.domain.DmSexualOrientation
import com.charm.ehr.kiosk.domain.Facility
import com.charm.ehr.kiosk.domain.FeedbackLanguageQuestionnaire
import com.charm.ehr.kiosk.domain.FeedbackPOJO
import com.charm.ehr.kiosk.domain.Guarantor
import com.charm.ehr.kiosk.domain.IntakeQuestTemplateEntry
import com.charm.ehr.kiosk.domain.IntakeQuestTemplateLabels
import com.charm.ehr.kiosk.domain.IntakeQuestTemplateOption
import com.charm.ehr.kiosk.domain.KioskPreferences
import com.charm.ehr.kiosk.domain.KioskSettings
import com.charm.ehr.kiosk.domain.Member
import com.charm.ehr.kiosk.domain.MemberAvailability
import com.charm.ehr.kiosk.domain.MemberWorkTiming
import com.charm.ehr.kiosk.domain.OptionData
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.domain.PatientQuesMap
import com.charm.ehr.kiosk.domain.PostalAddress
import com.charm.ehr.kiosk.domain.PracticePayer
import com.charm.ehr.kiosk.domain.Question
import com.charm.ehr.kiosk.domain.QuestionnaireMetaData
import com.charm.ehr.kiosk.domain.QuestionnairePhysicianTemplate
import com.charm.ehr.kiosk.domain.QuestionnaireType
import com.charm.ehr.kiosk.domain.SOAPEntry
import com.charm.ehr.kiosk.domain.SOAPEntryType
import com.charm.ehr.kiosk.domain.SOAPTemplateTableCell
import com.charm.ehr.kiosk.domain.SOAPWidget
import com.charm.ehr.kiosk.domain.Service
import com.charm.ehr.kiosk.domain.State
import com.charm.ehr.kiosk.presentation.edit_profile.FieldState
import com.charm.ehr.kiosk.presentation.edit_profile.Insurance
import com.charm.ehr.kiosk.presentation.edit_profile.PersonDetail
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.joda.time.DateTime
import org.joda.time.Years
import kotlin.math.roundToInt

class MdParsingHelper {
    companion object {

        //region Patient
        fun getPatientList(respObj: JsonObject): ArrayList<Patient> {
            val patJsonArr = respObj.getAsJsonArray("patients")
            val patientsArrayList = arrayListOf<Patient>()
            for (jsonObj in patJsonArr) {
                val contDet = ContactDetails()

                val patientObj = jsonObj.asJsonObject
                val patient = Patient()
                patient.firstName = patientObj.get("first_name")?.asString
                patient.lastName = patientObj.get("last_name")?.asString
                patient.fullName = patientObj.get("full_name")?.asString

                patient.practicePatientId = patientObj.get("patient_id")?.asString
                patient.gender = ModelUtil.getBirthSex(patientObj.getString("gender"))
                patient.pronouns = patientObj.get("pronouns")?.asString
                patient.genderIdentity = patientObj.get("gender_identity")?.asString
                patient.bloodGroup = patientObj.get("blood_group")?.asString

                val dob = patientObj.get("dob").asString
                if (!dob.isNullOrBlank()) {
                    patient.dateOfBirth = DateUtil.getDate(dob)
                }
                patient.patientRecordId = patientObj.get("record_id")?.asString

                val isSilhoutte = patientObj.get("is_silhouette").asBoolean
                patient.isPhotoAvailableOnline = !isSilhoutte
                contDet.emailId = patientObj.get("email")?.asString
                contDet.mobile = patientObj.get("mobile")?.asString
                contDet.primaryPhone = patientObj.get("primary_phone")?.asString
                contDet.homePhone = patientObj.get("home_phone")?.asString
                contDet.workPhone = patientObj.get("work_phone")?.asString
                contDet.workPhoneExtn = patientObj.get("work_phone_extn")?.asString
                patient.contactDetails = contDet
                patientsArrayList.add(patient)
            }

            return patientsArrayList
        }

        fun formPatientPOJO(patientObj: JsonObject?): Patient {
            val patPOJO = Patient()

            if (patientObj != null) {
                patPOJO.practicePatientId = patientObj.getString("patient_id")
                patPOJO.firstName = patientObj.getString("first_name")
                patPOJO.middleName = patientObj.getString("middle_name")
                patPOJO.lastName = patientObj.getString("last_name")
                patPOJO.fullName = patientObj.getString("full_name")
                patPOJO.gender = ModelUtil.getBirthSex(patientObj.getString("gender"))
                patPOJO.pronouns = patientObj.getString("pronouns")

                patPOJO.patientRecordId = patientObj.getString("record_id")

                val dob = patientObj.getString("dob")
                if (!dob.isNullOrBlank()) {
                    val isAge = patientObj.getBoolean("is_auto_calculated_dob") ?: false
                    patPOJO.dateOfBirth = DateUtil.getDate(patientObj.getString("dob"))
                    if (isAge) {
                        patPOJO.age = Years.yearsBetween(patPOJO.dateOfBirth, DateTime.now()).years
                    }
                }


                patPOJO.genderIdentity = patientObj.getString("gender_identity")
                patPOJO.nickName = patientObj.getString("nick_name")
                patPOJO.suffix = patientObj.getString("suffix")
                patPOJO.maidenName = patientObj.getString("maiden_name")
                patPOJO.deceased = patientObj.getBoolean("deceased")
                patPOJO.dod = patientObj.getString("dod")
                patPOJO.causeOfDeath = patientObj.getString("cause_of_death")

                patPOJO.idQualifiers = patientObj.getAsJsonArray("id_qualifiers")

                val isSilhoutte = patientObj.getBoolean("is_silhouette") ?: true


                patPOJO.isPhotoAvailableOnline = !isSilhoutte

                patPOJO.emergencyContactName = patientObj.getString("emergency_contact_name")
                patPOJO.emergencyContactNumber =
                    patientObj.getString("emergency_contact_number")
                patPOJO.emergencyContactExtn =
                    patientObj.getString("emergency_contact_number_extn")

                patPOJO.facilityListForEdit = patientObj.getAsJsonArray("facilities")

                val caregiverList: ArrayList<Caregiver> = arrayListOf()
                val caregiverArr = patientObj.getAsJsonArray("caregivers")

                caregiverArr.forEach { caregiveEle ->
                    val caregiverObj = caregiveEle.asJsonObject
                    val relationship = caregiverObj.getString("relationship")
                    if (!relationship.isNullOrBlank()) {
                        val caregiver = Caregiver()
                        caregiver.firstName = caregiverObj.getString("first_name")
                        caregiver.middleName = caregiverObj.getString("middle_name")
                        caregiver.lastName = caregiverObj.getString("last_name")
                        caregiver.ssn = caregiverObj.getString("ssn")
                        caregiver.dateOfBirth = caregiverObj.getString("dob")
                        caregiver.gender = ModelUtil.getBirthSex(caregiverObj.getString("gender"))
                        caregiver.relationship = relationship
                        caregiver.sameAsPatContact =
                            caregiverObj.getBoolean("same_as_patient_contact")

                        val caregiverPostalAddress = PostalAddress()
                        val caregiverAddressObj = caregiverObj.getAsJsonObject("address")
                        caregiverPostalAddress.line1 =
                            caregiverAddressObj.get("address_line1")?.asString
                        caregiverPostalAddress.line2 =
                            caregiverAddressObj.get("address_line2")?.asString
                        caregiverPostalAddress.city = caregiverAddressObj.get("city")?.asString
                        caregiverPostalAddress.state = caregiverAddressObj.get("state")?.asString
                        caregiverPostalAddress.country =
                            caregiverAddressObj.get("country")?.asString
                        caregiverPostalAddress.postalCode =
                            caregiverAddressObj.get("zip_code")?.asString

                        val caregiverContactDetails = ContactDetails()
                        val caregiverContactObj = caregiverObj.getAsJsonObject("contact")
                        caregiverContactDetails.mobilePhone =
                            caregiverContactObj.get("mobile")?.asString
                        caregiverContactDetails.homePhone =
                            caregiverContactObj.get("home_phone")?.asString
                        caregiverContactDetails.workPhone =
                            caregiverContactObj.get("work_phone")?.asString
                        caregiverContactDetails.workPhoneExtn =
                            caregiverContactObj.get("work_phone_extn")?.asString
                        caregiverContactDetails.emailId = caregiverContactObj.get("email")?.asString
                        caregiver.type = caregiverObj.getInt("type")

                        caregiver.contactDetails = caregiverContactDetails
                        caregiver.postalAddress = caregiverPostalAddress

                        caregiverList.add(caregiver)

                    }

                }


                patPOJO.caregivers = caregiverList

                val guarantorObj = patientObj.getAsJsonObject("guarantor")

                val guarantor = Guarantor()

                if (guarantorObj != null) {
                    guarantor.firstName = guarantorObj.get("first_name")?.asString
                    guarantor.middleName = guarantorObj.get("middle_name")?.asString
                    guarantor.lastName = guarantorObj.get("last_name")?.asString
                    guarantor.ssn = guarantorObj.get("ssn")?.asString
                    guarantor.dateOfBirth = guarantorObj.get("dob")?.asString
                    guarantor.gender = ModelUtil.getBirthSex(guarantorObj.get("gender")?.asString)
                    guarantor.relationship = guarantorObj.get("relationship")?.asString

                    val guarantorPostalAddress = PostalAddress()
                    val guarantorAddressObj = guarantorObj.getAsJsonObject("address")
                    guarantorPostalAddress.line1 =
                        guarantorAddressObj.get("address_line1")?.asString
                    guarantorPostalAddress.line2 =
                        guarantorAddressObj.get("address_line2")?.asString
                    guarantorPostalAddress.city = guarantorAddressObj.get("city")?.asString
                    guarantorPostalAddress.state = guarantorAddressObj.get("state")?.asString
                    guarantorPostalAddress.country = guarantorAddressObj.get("country")?.asString
                    guarantorPostalAddress.postalCode =
                        guarantorAddressObj.get("zip_code")?.asString

                    val guarantorContactDetails = ContactDetails()
                    val guarantorContactObj = guarantorObj.getAsJsonObject("contact")
                    guarantorContactDetails.mobile = guarantorContactObj.get("mobile")?.asString
                    guarantorContactDetails.homePhone =
                        guarantorContactObj.get("home_phone")?.asString
                    guarantorContactDetails.workPhone =
                        guarantorContactObj.get("work_phone")?.asString
                    guarantorContactDetails.workPhoneExtn =
                        guarantorContactObj.get("work_phone_extn")?.asString
                    guarantorContactDetails.emailId = guarantorContactObj.get("email")?.asString

                    guarantor.contactDetails = guarantorContactDetails
                    guarantor.postalAddress = guarantorPostalAddress
                }


                patPOJO.guarantor = guarantor


                patPOJO.emailNotification = patientObj.get("email_notification")?.asBoolean
                patPOJO.textNotification = patientObj.get("text_notification")?.asBoolean
                patPOJO.voiceNotification = patientObj.get("voice_notification")?.asBoolean

                patPOJO.bloodGroup = patientObj.get("blood_group")?.asString
                patPOJO.language = patientObj.get("language")?.asString
                patPOJO.race = patientObj.get("race")?.asString
                patPOJO.ethnicity = patientObj.get("ethnicity")?.asString
                patPOJO.maritalStatus = patientObj.get("marital_status")?.asString
                patPOJO.smokingStatus = patientObj.get("smoking_status")?.asString
                patPOJO.employmentStatus = patientObj.get("employment_status")?.asString
                patPOJO.sexualOrientation = patientObj.get("sexual_orientation")?.asString
                patPOJO.motherFirstName = patientObj.get("mother_first_name")?.asString
                patPOJO.motherLastName = patientObj.get("mother_last_name")?.asString
                patPOJO.birthOrder = patientObj.get("birth_order")?.asString

                patPOJO.categories = patientObj.getAsJsonArray("categories")

                patPOJO.introduction = patientObj.get("introduction")?.asString
                patPOJO.customField1 = patientObj.get("custom_field_1")?.asString
                patPOJO.customField2 = patientObj.get("custom_field_2")?.asString
                patPOJO.customField3 = patientObj.get("custom_field_3")?.asString
                patPOJO.customField4 = patientObj.get("custom_field_4")?.asString
                patPOJO.customField5 = patientObj.get("custom_field_5")?.asString
                patPOJO.sourceName = patientObj.get("source_name")?.asString
                patPOJO.sourceValue = patientObj.get("source_value")?.asString
                patPOJO.paymentSource = patientObj.get("payment_source")?.asString
                patPOJO.paymentStartDate = patientObj.get("payment_start_date")?.asString
                patPOJO.paymentEndDate = patientObj.get("payment_end_date")?.asString
                patPOJO.externalId = patientObj.get("external_id")?.asString

                patPOJO.isActive = patientObj.get("birth_order")?.asBoolean ?: true


                patPOJO.primaryCareProvider = patientObj.get("primary_care_provider")?.asString
                patPOJO.secondaryCareProvider = patientObj.get("secondary_care_provider")?.asString

                val primaryContactDetails = patientObj.get("primary_contact_details")?.asJsonObject

                patPOJO.caregiverFirstName =
                    primaryContactDetails?.get("cargiver_first_name")?.asString
                patPOJO.caregiverLastName =
                    primaryContactDetails?.get("cargiver_last_name")?.asString

                val contactDetails = ContactDetails()
                contactDetails.preferredCommunication =
                    primaryContactDetails?.get("preferred_communication")?.asString
                contactDetails.emailId = primaryContactDetails?.get("email")?.asString
                contactDetails.primaryPhone = primaryContactDetails?.get("primary_phone")?.asString
                contactDetails.homePhone = primaryContactDetails?.get("home_phone")?.asString
                contactDetails.mobilePhone = primaryContactDetails?.get("mobile")?.asString
                contactDetails.workPhone = primaryContactDetails?.get("work_phone")?.asString
                contactDetails.workPhoneExtn =
                    primaryContactDetails?.get("work_phone_extn")?.asString
                contactDetails.fax = primaryContactDetails?.get("fax")?.asString

                patPOJO.contactDetails = contactDetails

                val postalAddress = PostalAddress()

                postalAddress.line1 = primaryContactDetails?.get("address_line1")?.asString
                postalAddress.line2 = primaryContactDetails?.get("address_line2")?.asString
                postalAddress.area = primaryContactDetails?.get("area")?.asString
                postalAddress.city = primaryContactDetails?.get("city")?.asString
                postalAddress.state = primaryContactDetails?.get("state")?.asString
                postalAddress.county = primaryContactDetails?.get("county")?.asString
                postalAddress.district = primaryContactDetails?.get("district")?.asString
                postalAddress.country = primaryContactDetails?.get("country")?.asString
                postalAddress.postalCode = primaryContactDetails?.get("zip_code")?.asString


                patPOJO.postalAddress = postalAddress

            }

            return patPOJO
        }

        fun formInsurancePOJO(respObj: JsonObject?): List<Insurance> {
            val insuranceList = ArrayList<Insurance>()
            if (respObj == null) return insuranceList
            val insurancesArr = respObj.getAsJsonArray("insurances") ?: return insuranceList
            insurancesArr.forEach { insuranceElem ->
                val insuranceObj = insuranceElem.asJsonObject
                val insuredPersonObj = insuranceObj.getAsJsonObject("insured_person_details")
                val insuredPersonDetail = if (insuredPersonObj != null) {
                    val relationshipValue =
                        insuredPersonObj.getString("insured_person_relationship")
                    val relationship: FieldState<DmCaregiverRelationship?> = FieldState(
                        if (relationshipValue.isNullOrBlank()) null else DmCaregiverRelationship(
                            value = relationshipValue,
                            key = ""
                        ),
                    )
                    val birthSexValue =
                        ModelUtil.getBirthSex(insuredPersonObj.getString("insured_person_gender"))
                    val birthSex = FieldState(birthSexValue)
                    val firstName =
                        FieldState(insuredPersonObj.getString("insured_person_first_name"))
                    val lastName =
                        FieldState(insuredPersonObj.getString("insured_person_last_name"))
                    val dateOfBirth: FieldState<String?> = FieldState(
                        DateUtil.getDate(
                            insuredPersonObj.getString("insured_person_dob")?.trim() ?: "",
                            ModelUtil.commonDateFormatWithIffenReverse()
                        )?.toString(ModelUtil.mothFirstDateFormatWithSlash())
                    )
                    val mobilePhone =
                        FieldState(insuredPersonObj.getString("insured_person_mobile_phone"))
                    val homePhone =
                        FieldState(insuredPersonObj.getString("insured_person_home_phone"))
                    val addressLine1 =
                        FieldState(insuredPersonObj.getString("insured_person_address_line_1"))
                    val addressLine2 =
                        FieldState(insuredPersonObj.getString("insured_person_address_line_2"))
                    val city = FieldState(insuredPersonObj.getString("insured_person_city"))
                    val state: FieldState<State?> =
                        FieldState(State(insuredPersonObj.getString("insured_person_state")))
                    val country: FieldState<Country?> =
                        FieldState(Country(value = insuredPersonObj.getString("insured_person_country")))
                    val postalCode =
                        FieldState(insuredPersonObj.getString("insured_person_zipcode"))
                    PersonDetail(
                        relationship = relationship,
                        birthSex = birthSex,
                        firstName = firstName,
                        lastName = lastName,
                        dateOfBirth = dateOfBirth,
                        mobilePhone = mobilePhone,
                        homePhone = homePhone,
                        addressLine1 = addressLine1,
                        addressLine2 = addressLine2,
                        city = city,
                        state = state,
                        country = country,
                        postalCode = postalCode
                    )
                } else null
                val selectedPayer = if (insuranceObj.getString("practice_payer_id") != null) {
                    PracticePayer().apply {
                        payerName = insuranceObj.getString("payer_name")
                        payerId = insuranceObj.getString("payer_id")
                        practicePayerId = insuranceObj.getString("practice_payer_id")
                    }
                } else null
                val selectedPayerState: FieldState<PracticePayer?> = FieldState(selectedPayer)
                val insuranceID = FieldState(insuranceObj.getString("insurance_id"))
                val planOrProgramName = FieldState(insuranceObj.getString("insurance_plan_name"))
                val planId = FieldState(insuranceObj.getString("insurance_plan_id"))
                val insuranceCategory = FieldState(insuranceObj.getString("type"))
                val policyGroupOrFECA = FieldState(insuranceObj.getString("policy_group_number"))
                val validFrom = FieldState(
                    DateUtil.getDate(
                        insuranceObj.getString("valid_from"),
                        ModelUtil.commonDateFormatWithIffenReverse()
                    )?.toString(ModelUtil.mothFirstDateFormatWithSlash())
                )
                val validUntil = FieldState(
                    DateUtil.getDate(
                        insuranceObj.getString("valid_to"),
                        ModelUtil.commonDateFormatWithIffenReverse()
                    )?.toString(ModelUtil.mothFirstDateFormatWithSlash())
                )
                val insuranceType = FieldState(insuranceObj.getString("insurance_type"))
                val patientInsuranceId = insuranceObj.getString("patient_insurance_id")
                val employerOrSchoolName = FieldState(insuranceObj.getString("employer_name"))
                val copay = FieldState(ModelUtil.stringToDecimal(insuranceObj.getString("copay")))
                val coinsurance = FieldState(insuranceObj.getString("coinsurance"))
                val deductible =
                    FieldState(ModelUtil.stringToDecimal(insuranceObj.getString("deductible")))
                val comment = FieldState(insuranceObj.getString("comments"))
                insuranceList.add(
                    Insurance(
                        insuranceID = insuranceID,
                        patientInsuranceId = patientInsuranceId,
                        planOrProgramName = planOrProgramName,
                        planId = planId,
                        insuranceCategory = insuranceCategory,
                        policyGroupOrFECA = policyGroupOrFECA,
                        validFrom = validFrom,
                        validUntil = validUntil,
                        insuranceType = insuranceType,
                        employerOrSchoolName = employerOrSchoolName,
                        selectedPayer = selectedPayerState,
                        copay = copay,
                        coinsurance = coinsurance,
                        deductible = deductible,
                        comment = comment,
                        insuredPersonDetail = insuredPersonDetail
                    )
                )
            }
            return insuranceList
        }

        fun formDemographicsMetrics(resObject: JsonObject): DemographicsMetrics {

            val dataObj = resObject.get("data")?.asJsonObject
            val smokingStatus =
                dataObj?.get("smoking_status")?.asJsonArray?.toStringList() ?: emptyList()
            val guarantorRelationship =
                dataObj?.get("guarantor_relationship")?.asJsonArray?.toStringList() ?: emptyList()
            val languagesArr = dataObj?.get("languages")?.asJsonArray
            val languages = ArrayList<DmLanguages>()
            languages.add(DmLanguages("Declined to specify", "Declined to specify"))
            languagesArr?.forEach {
                val langObj = it.asJsonObject
                val value = langObj.get("value")?.asString ?: ""
                val key = langObj.get("key")?.asString ?: ""
                languages.add(DmLanguages(value, key))
            }

            val raceArr = dataObj?.get("race")?.asJsonArray
            val race = ArrayList<DmRace>()
            raceArr?.forEach {
                val raceObj = it.asJsonObject
                val value = raceObj.get("value")?.asString ?: ""
                val key = raceObj.get("key")?.asString ?: ""
                race.add(DmRace(value, key))
            }

            val ethnicityArr = dataObj?.get("ethnicity")?.asJsonArray
            val ethnicity = ArrayList<DmEthnicity>()
            ethnicityArr?.forEach {
                val ethnicityObj = it.asJsonObject
                val value = ethnicityObj.get("value")?.asString ?: ""
                val key = ethnicityObj.get("key")?.asString ?: ""
                ethnicity.add(DmEthnicity(value, key))
            }

            val birthSex = dataObj?.get("birth_sex")?.asJsonArray?.toStringList() ?: emptyList()

            val genderIdentityArr = dataObj?.get("gender_Identity")?.asJsonArray
            val genderIdentity = ArrayList<DmGenderIdentity>()
            genderIdentityArr?.forEach {
                val genderObj = it.asJsonObject
                val value = genderObj.get("value")?.asString ?: ""
                val key = genderObj.get("key")?.asString ?: ""
                genderIdentity.add(DmGenderIdentity(value, key))
            }
            genderIdentity.add(DmGenderIdentity("Declined to specify", "Declined to specify"))

            val employmentStatus =
                dataObj?.get("employment_staus")?.asJsonArray?.toStringList() ?: emptyList()
            val caregiverRelationshipArr = dataObj?.get("caregiver_relationship")?.asJsonArray
            val caregiverRelationship = ArrayList<DmCaregiverRelationship>()
            caregiverRelationshipArr?.forEach {
                val caregiverObj = it.asJsonObject
                val value = caregiverObj.get("value")?.asString ?: ""
                val key = caregiverObj.get("key")?.asString ?: ""
                caregiverRelationship.add(DmCaregiverRelationship(value, key))
            }

            val sexualOrientationArr = dataObj?.get("sexual_orientation")?.asJsonArray
            val sexualOrientation = ArrayList<DmSexualOrientation>()
            sexualOrientationArr?.forEach {
                val sexualObj = it.asJsonObject
                val value = sexualObj.get("value")?.asString ?: ""
                val key = sexualObj.get("key")?.asString ?: ""
                sexualOrientation.add(DmSexualOrientation(value, key))
            }

            val maritalStatus =
                dataObj?.get("marital_status")?.asJsonArray?.toStringList() ?: emptyList()
            val commonQualifierArr = dataObj?.get("common_qualifiers")?.asJsonArray
            val commonQualifier = ArrayList<DmCommonQualifier>()
            commonQualifierArr?.forEach {
                val commonObj = it.asJsonObject
                val idQualifier = commonObj.get("id_qualifier")?.asString ?: ""
                val qualifierName = commonObj.get("qualifier_name")?.asString ?: ""
                commonQualifier.add(DmCommonQualifier(idQualifier, qualifierName))
            }

            val bloodGroup = dataObj?.get("blood_group")?.asJsonArray?.toStringList() ?: emptyList()
            val preferredCommunication =
                dataObj?.get("preferred_communication")?.asJsonArray?.toStringList() ?: emptyList()
//            val paymentSource = dataObj?.get("payment_source")?.asJsonArray?.toStringList()

            val demographicsMetrics = DemographicsMetrics(
                smokingStatus = smokingStatus,
                guarantorRelationship = guarantorRelationship,
                languages = languages,
                race = race,
                ethnicity = ethnicity,
                birthSex = birthSex,
                genderIdentity = genderIdentity,
                employmentStatus = employmentStatus,
                caregiverRelationship = caregiverRelationship,
                sexualOrientation = sexualOrientation,
                maritalStatus = maritalStatus,
                commonQualifier = commonQualifier,
                bloodGroup = bloodGroup,
                preferredCommunication = preferredCommunication
            )

            return demographicsMetrics
        }

        fun formCountryList(resObject: JsonObject): ArrayList<Country> {
            val countryMetricsObj = resObject.get("country_metrics")?.asJsonObject
            val countriesJsonArray = countryMetricsObj?.get("countries")?.asJsonArray
            val countryList = ArrayList<Country>()
            countriesJsonArray?.forEach {
                val countryObj = it.asJsonObject
                val label = countryObj.get("label")?.asString ?: ""
                val value = countryObj.get("value")?.asString ?: ""
                val country = Country(value = value, label = label)
                countryList.add(country)
            }
            return countryList
        }

        fun formStateList(resObject: JsonObject): List<State> {
            val stateMetrics = resObject.get("state_metrics")?.asJsonObject
            val statesArray = stateMetrics?.get("states")?.asJsonArray
            val stateList = ArrayList<State>()
            statesArray?.forEach {
                val countryObj = it.asJsonObject
                val label = countryObj.get("label")?.asString ?: ""
                val value = countryObj.get("value")?.asString ?: ""
                val state = State(label, value)
                stateList.add(state)
            }
            return stateList
        }

        fun formDemographicsSettings(resObject: JsonObject): ArrayList<DemographicsSetting> {
            val dataArray = resObject.get("data")?.asJsonArray

            val settingsList = ArrayList<DemographicsSetting>()
            dataArray?.forEach {
                val dataObj = it.asJsonObject

                val label = dataObj.get("label")?.asString ?: ""
                val nodeIdentifier = dataObj.get("node_identifier")?.asString ?: ""
                val type = dataObj.get("type")?.asString ?: ""
                val mandatory = dataObj.getBoolean("mandatory")

                val demographicsSetting = DemographicsSetting(
                    label = label,
                    nodeIdentifier = nodeIdentifier,
                    type = type,
                    mandatory = mandatory
                )
                settingsList.add(demographicsSetting)
            }
            return settingsList
        }


        //endregion

        //region countries and states

        fun formQualifiersHashMap(qualifiersObj: JsonObject): HashMap<String, String> {
            val respMap = HashMap<String, String>()

            val qualifiersArray = qualifiersObj.get("data")?.asJsonArray
            if (qualifiersArray != null) {
                for (qualifierEle in qualifiersArray) {
                    val qualifierObj = qualifierEle.asJsonObject

                    respMap.put(
                        qualifierObj.get("qualifier_name")?.asString ?: "",
                        qualifierObj.get("id_qualifier")?.asString ?: ""
                    )
                }
            }
            return respMap
        }

        fun formCountriesHashMap(countriesObj: JsonObject): HashMap<String, String> {
            val respMap = HashMap<String, String>()

            val countriesArray = countriesObj.get("countries")?.asJsonArray
            if (countriesArray != null) {
                for (countryEle in countriesArray) {
                    val countryObj = countryEle.asJsonObject

                    respMap.put(
                        countryObj.get("label")?.asString ?: "",
                        countryObj.get("value")?.asString ?: ""
                    )
                }
            }
            return respMap
        }

        fun formStatesArrayList(statesObj: JsonObject): ArrayList<String> {
            val respList = ArrayList<String>()

            val statesArray = statesObj.get("states")?.asJsonArray
            if (statesArray != null) {
                for (stateEle in statesArray) {
                    val stateObj = stateEle.asJsonObject
                    respList.add(stateObj.get("label")?.asString ?: "")
                }
            }
            return respList
        }

        //endregion

        //region Member

        fun formMembersAvailabilitySlots(jsonObj: JsonObject): ArrayList<MemberAvailability>? {

            var availabilityArr: ArrayList<MemberAvailability>? = ArrayList()

            val jsonArr = jsonObj.get("available_slots")?.asJsonArray
            val fromDate =
                jsonObj.get("from_Date")?.asString //Note:From date and toDate are same in out case
            val slotObj = jsonArr?.firstOrNull()?.asJsonObject

            if (slotObj != null) {

                val slotArr = slotObj.get(fromDate)?.asJsonArray

                if (slotArr != null) {

                    slotArr.forEach {

                        val obj = it.asJsonObject

                        val endTime = obj.get("end_time")?.asString
                        val startTime = obj.get("start_time")?.asString

                        if (endTime != null && startTime != null && endTime.isNotBlank() && startTime.isNotBlank()) {

                            //temp fix
                            if (!startTime.contains("00:")) { //TODO: temp fix until api arrives

                                val memberAvailability = MemberAvailability()

                                //todo:00:00 AM error case

                                val currentDateStr =
                                    DateTime.now().toString(ModelUtil.charmApiDateFormat())

                                memberAvailability.startTime =
                                    DateUtil.getDate(
                                        "$currentDateStr $startTime",
                                        ModelUtil.sqlDateTimeFormatForNormalTiming()
                                    )

                                memberAvailability.endTime =
                                    DateUtil.getDate(
                                        "$currentDateStr $endTime",
                                        ModelUtil.sqlDateTimeFormatForNormalTiming()
                                    )

                                availabilityArr?.add(memberAvailability)

                            }

                        }

                    }

                } else {

                    availabilityArr = null
                }

            } else {
                //return error
                availabilityArr = null
            }

            return availabilityArr

        }

        fun formMemberList(memberArray: JsonArray?): ArrayList<Member> {

            val tempMemberList = ArrayList<Member>()
            Log.d("memberResponse", "$memberArray")

            memberArray?.forEach { memberEle ->
                val memberObj = memberEle.asJsonObject
                val member = Member()
                member.memberId = memberObj.get("member_id")?.asString
                member.prefix = memberObj.get("prefix")?.asString
                member.firstName = memberObj.get("first_name")?.asString
                member.lastName = memberObj.get("last_name")?.asString
                member.fullName = memberObj.get("full_name")?.asString
                member.gender = memberObj.get("gender")?.asString
                member.degree = memberObj.get("degree")?.asString
                member.isPhotoAvailable = memberObj.get("is_member_photo_available")?.asBoolean

                val isMemberOnline = memberObj.get("is_member_online_app_enabled")?.asString
                if (isMemberOnline?.trim()?.isNotBlank() == true) member.isMemberOnlineAppEnabled =
                    isMemberOnline.toInt() == 1

                member.specialization = memberObj.get("specialization")?.asString

                val createdDate = memberObj.get("created_date")?.asString?.trim()
                member.createdDate = if (createdDate.isNullOrBlank()) {
                    null
                } else {
                    DateUtil.getDate(createdDate, ModelUtil.charmApiDateFormat())
                }

                val contactDetails = ContactDetails()
                contactDetails.mobile = memberObj.get("mobile")?.asString
                contactDetails.emailId = memberObj.get("email")?.asString
                contactDetails.homePhone = memberObj.get("home_phone")?.asString

                val postalAddress = PostalAddress()
                postalAddress.line1 = memberObj.get("address_line1")?.asString
                postalAddress.line2 = memberObj.get("address_line2")?.asString
                postalAddress.city = memberObj.get("city")?.asString
                postalAddress.state = memberObj.get("state")?.asString
                postalAddress.country = memberObj.get("country")?.asString
                postalAddress.postalCode = memberObj.get("zip_code")?.asString

                member.contactDetails = contactDetails
                member.postalAddress = postalAddress

                tempMemberList.add(member)

            }

//
//            val map = HashMap<String, List<Member>>()
//
//            while (tempMemberList.isNotEmpty()) {
//
//                val specialityName = tempMemberList.first().specialization ?:context.getString(R.string.otherSpeciality)
//
//                val specializedMemList = tempMemberList.filter {
//                    it.specialization ?: context.getString(R.string.otherSpeciality) == specialityName
//                }
//
//                map.put(specialityName, specializedMemList)
//
//                tempMemberList = tempMemberList.filter {
//                    it.specialization ?: context.getString(R.string.otherSpeciality) != specialityName
//                } as ArrayList<Member>
//
//            }

            return tempMemberList

        }

        fun formMemberTimingList(jsonArray: JsonArray?): ArrayList<MemberWorkTiming> {

            val resList = ArrayList<MemberWorkTiming>()

            val currentDateTime = DateTime.now()

            jsonArray?.forEach { respEle ->
                val respObj = respEle.asJsonObject
                val memberWorkTiming = MemberWorkTiming()
                memberWorkTiming.memberTimingId = respObj.get("member_timing_id")?.asString
                memberWorkTiming.memberId = respObj.get("member_id")?.asString
                memberWorkTiming.facilityId = respObj.get("facility_id")?.asString
                memberWorkTiming.dayOfWeek = respObj.get("day_of_week")?.asInt

                if (memberWorkTiming.dayOfWeek == 0) {
                    memberWorkTiming.dayOfWeek = 7
                }
                memberWorkTiming.isVisiting = respObj.get("is_visiting")?.asBoolean
                memberWorkTiming.visitNo = respObj.get("visit_no")?.asString
                memberWorkTiming.startTime =
                    respObj.get("start_time")?.asString?.padStart(length = 5, padChar = '0')
                memberWorkTiming.startSession = respObj.get("start_session")?.asString
                memberWorkTiming.endTime =
                    respObj.get("end_time")?.asString?.padStart(length = 5, padChar = '0')
                memberWorkTiming.endSession = respObj.get("end_session")?.asString
                memberWorkTiming.onlineAppSlot = respObj.get("online_app_slot")?.asBoolean
                memberWorkTiming.tokenCount =
                    respObj.get("token_count")?.asString?.trim()?.toIntOrNull()

                val startDateTimeStr =
                    currentDateTime.toString(ModelUtil.charmApiDateFormat()) + " " + memberWorkTiming.startTime + " " + memberWorkTiming.startSession
                val startDateTime =
                    DateUtil.getDate(startDateTimeStr, ModelUtil.sqlDateTimeFormatForNormalTiming())

                val endDateTimeStr =
                    currentDateTime.toString(ModelUtil.charmApiDateFormat()) + " " + memberWorkTiming.endTime + " " + memberWorkTiming.endSession
                val endDateTime =
                    DateUtil.getDate(endDateTimeStr, ModelUtil.sqlDateTimeFormatForNormalTiming())

                memberWorkTiming.startDateTime = startDateTime
                memberWorkTiming.endDateTime = endDateTime
                resList.add(memberWorkTiming)

            }

            return resList
        }

        //endregion

        //region Appointment

        fun formAppointmentList(encounterListResp: JsonObject): ArrayList<Appointment> {

            val appointmentList = ArrayList<Appointment>()
            val appointments = encounterListResp.getAsJsonArray("appointments")
            for (jsonObj in appointments!!) {

                val appointmentJson = jsonObj!!.asJsonObject
                if (!appointmentJson.get("is_signed").asBoolean) {

                    val appointment = Appointment()
                    val apmntDate = appointmentJson.get("appointment_date")?.asString
                    if (!apmntDate.isNullOrBlank()) {
                        appointment.appointmentDate =
                            DateUtil.getDate(apmntDate, ModelUtil.sqlTimeStampFormattor())
                        appointment.appointmentStartTime =
                            DateUtil.getDate(apmntDate, ModelUtil.sqlTimeStampFormattor())
                    }
                    appointment.appointmentId = appointmentJson.get("appointment_id")?.asString
                    appointment.facilityId = appointmentJson.get("facility_id")?.asString
                    val member = Member()
                    member.fullName = appointmentJson.get("physician_name")?.asString
                    member.prefix = appointmentJson.get("prefix")?.asString
                    member.specialization = appointmentJson.get("member_specialization")?.asString
                    member.degree = appointmentJson.get("member_degree")?.asString
                    member.memberId = appointmentJson.get("member_id")?.asString
                    member.isPhotoAvailable =
                        appointmentJson.get("is_member_photo_available")?.asBoolean
                    appointment.member = member

                    val patient = Patient()
                    patient.fullName = appointmentJson.get("patient_name")?.asString
                    patient.practicePatientId = appointmentJson.get("patient_id")?.asString
                    appointment.patient = patient

                    val appointmentStatus = AppointmentStatus()

                    appointmentStatus.statusId = appointmentJson.get("status_id")?.asString
                    appointmentStatus.status = appointmentJson.get("status")?.asString
                    appointmentStatus.statusColor = appointmentJson.get("status_color")?.asString

                    appointment.appointmentStatus = appointmentStatus

                    appointmentList.add(appointment)

                }

            }
            return appointmentList
        }

        fun getAppointmentStatusFromResponse(respObj: JsonObject): ArrayList<AppointmentStatus> {

            val appointmentStatusList = ArrayList<AppointmentStatus>()

            val appointmentStatusArr = respObj.get("appointment_status")?.asJsonArray

            appointmentStatusArr?.forEach { appointmentStatusEle ->
                val appointmentStatusObj = appointmentStatusEle.asJsonObject
                val appointmentStatus = AppointmentStatus()

                appointmentStatus.statusId = appointmentStatusObj.get("status_id")?.asString
                appointmentStatus.status = appointmentStatusObj.get("status")?.asString
                appointmentStatus.statusColor = appointmentStatusObj.get("status_color")?.asString
                appointmentStatus.isSystemGenerated =
                    appointmentStatusObj.get("is_system_generated")?.asInt == 1//todo:check
                appointmentStatus.isDeleted = appointmentStatusObj.get("is_deleted")?.asInt == 1
                appointmentStatus.position =
                    appointmentStatusObj.get("position")?.asInt //todo:check
                appointmentStatus.isDefaultStatus =
                    appointmentStatusObj.get("is_default_status")?.asInt == 1
                appointmentStatusList.add(appointmentStatus)
            }

            return appointmentStatusList

        }

        fun getDepartmentsListFrom(jsonArr: JsonArray): List<Department> {

            val departmentList = ArrayList<Department>()

            jsonArr.forEach { o ->

                val obj = o as JsonObject
                val department = Department()

                department.departmentId = obj.get("department_id")?.asString
                department.departmentName = obj.get("department_name")?.asString
                department.facilityId = obj.get("facility_id")?.asString
                department.memberName = obj.get("member_name")?.asString
                department.facilityName = obj.get("facility_name")?.asString

                departmentList.add(department)
            }

            return departmentList
        }

        fun formMembersListFromDepartmentResp(jsonArr: JsonArray): ArrayList<Member> {
            val membersList: ArrayList<Member> = ArrayList()

            jsonArr.forEach { element ->

                val obj = element as JsonObject
                val member = Member()

                member.memberId = obj.get("member_id")?.asString
                member.prefix = obj.get("prefix")?.asString
                member.firstName = obj.get("first_name")?.asString
                member.lastName = obj.get("last_name")?.asString
                member.fullName = obj.get("full_name")?.asString
                member.gender = obj.get("gender")?.asString
                member.specialization = obj.get("specialization")?.asString
                member.degree = obj.get("degree")?.asString
                member.isPhotoAvailable = obj.get("is_member_photo_available")?.asBoolean

                val isMemberOnline = obj.get("is_member_online_app_enabled")?.asString
                if (isMemberOnline?.trim()?.isNotBlank() == true) member.isMemberOnlineAppEnabled =
                    isMemberOnline.toInt() == 1


                member.introduction = obj.get("introduction")?.asString

                val contactDetails = ContactDetails()

                contactDetails.homePhone = obj.get("home_phone")?.asString
                contactDetails.mobile = obj.get("mobile")?.asString
                contactDetails.emailId = obj.get("email")?.asString

                val postalAddress = PostalAddress()

                postalAddress.line1 = obj.get("address_line1")?.asString
                postalAddress.line2 = obj.get("address_line2")?.asString
                postalAddress.city = obj.get("city")?.asString
                postalAddress.state = obj.get("state")?.asString
                postalAddress.country = obj.get("country")?.asString
                postalAddress.postalCode = obj.get("zip_code")?.asString

                member.contactDetails = contactDetails
                member.postalAddress = postalAddress
                membersList.add(member)

            }

            return membersList
        }

        fun parseAppointmentSettings(jsonArr: JsonObject): AppointmentSettings {

            val appointmentSettings = AppointmentSettings()

            appointmentSettings.appointmentConfigId = jsonArr.get("appointment_config_id")?.asString
            appointmentSettings.providerId = jsonArr.get("provider_id")?.asString
            appointmentSettings.facilityId = jsonArr.get("facility_id")?.asString
            appointmentSettings.appointmentSlotNewPat =
                jsonArr.get("appointment_slot_new_pat")?.asString
            appointmentSettings.appointmentSlotOldPat =
                jsonArr.get("appointment_slot_old_pat")?.asString
            appointmentSettings.appApprovalPreferences =
                jsonArr.get("app_approval_preferences")?.asString
            appointmentSettings.isEnabled = jsonArr.get("is_enabled")?.asBoolean
            appointmentSettings.visitTypeIds = jsonArr.get("visit_type_ids")?.asString
            appointmentSettings.showVisitType = jsonArr.get("show_visit_type")?.asBoolean
            appointmentSettings.isCardProcessingEnabled =
                jsonArr.get("is_card_processing_enabled")?.asBoolean
            appointmentSettings.cardProcessingType = jsonArr.get("card_processing_type")?.asString
            appointmentSettings.chargeType = jsonArr.get("charge_type")?.asString

            val amt = jsonArr.get("charge_amount")?.asString
            if (amt?.trim()?.isNotBlank() == true) {
                appointmentSettings.chargeAmount = amt.toFloatOrNull()
            }

            appointmentSettings.patientNote = jsonArr.get("patient_note")?.asString
            appointmentSettings.allowAddWaitList = jsonArr.get("allow_add_waitlist")?.asBoolean
            appointmentSettings.isTokenBased = jsonArr.get("is_token_based")?.asBoolean

            return appointmentSettings
        }

        fun getConfirmedAppointmentCount(respObj: JsonObject): Int? {

            val outputData = respObj.getAsJsonObject("appointments")
            if (outputData != null) {
                val appointmentObj = outputData.getAsJsonObject("appointment")
                if (appointmentObj != null) {
                    return appointmentObj.get("appointments_count")?.asInt
                }
            }

            return null
        }

        //endregion

        //region check In
        fun getToken(generateTokenResp: JsonObject): Int {
            val appointmentObj = generateTokenResp.get("output_string")?.asJsonObject

            return appointmentObj?.get("token_number")?.asInt ?: -1
        }
        //endregion

        //region others

        fun formFeedbackPOJOList(respObj: JsonObject): ArrayList<FeedbackPOJO> {

            val feedbackPOJOList = arrayListOf<FeedbackPOJO>()

            val feedbackRespArr = respObj.getAsJsonArray("data")

            feedbackRespArr.forEach {
                val feedbackObj = it.asJsonObject

                val feedbackEntry = IntakeQuestTemplateEntry()
                feedbackEntry.notes = feedbackObj.get("notes")?.asString
                feedbackEntry.isMultiChoice =
                    feedbackObj?.get("is_multi_choice")?.asBoolean ?: false
                feedbackEntry.isDeleted = feedbackObj?.get("is_deleted")?.asBoolean ?: false

                feedbackEntry.templateId = feedbackObj?.get("template_id")?.asString ?: ""

                feedbackEntry.isMandatory = feedbackObj?.get("is_mandatory")?.asBoolean ?: false
                feedbackEntry.position = feedbackObj?.get("position")?.asInt
                feedbackEntry.entryId = feedbackObj?.get("entry_id")?.asString ?: ""
                feedbackEntry.notesType = feedbackObj?.get("notes_type")?.asString ?: ""

                val optionsArr = feedbackObj.get("options")?.asJsonArray

                val optionsList = arrayListOf<IntakeQuestTemplateOption>()


                optionsArr?.forEach { option ->
                    val intakeOption = IntakeQuestTemplateOption()
                    intakeOption.option = option?.asString
                    intakeOption.entryId = feedbackEntry.entryId
                    optionsList.add(intakeOption)
                }

                val feedbackPOJO =
                    FeedbackPOJO(feedbackEntry = feedbackEntry, feedbackOptions = optionsList)

                val labelObj = feedbackObj.get("label_style")?.asJsonObject

                if (labelObj != null) {
                    val intakeLabel = IntakeQuestTemplateLabels()

                    intakeLabel.fontWeight = labelObj.get("font_weight")?.asString ?: ""
                    intakeLabel.textDecoration = labelObj.get("text_decoration")?.asString ?: ""
                    intakeLabel.fontSize = labelObj.get("font_size")?.asInt ?: 14
                    intakeLabel.fontStyle = labelObj.get("font_style")?.asString ?: ""
                    intakeLabel.textAlign = labelObj.get("text_align")?.asString ?: ""

                    intakeLabel.entryId = feedbackEntry.entryId

                    feedbackPOJO.feedbackLabel = arrayListOf(intakeLabel)
                }

                val descriptionObj = feedbackObj.get("description")?.asJsonObject

                if (descriptionObj != null) {
                    val descriptionList = arrayListOf<String>()

                    descriptionList.add(descriptionObj.get("1")?.asString ?: "")
                    descriptionList.add(descriptionObj.get("2")?.asString ?: "")
                    descriptionList.add(descriptionObj.get("3")?.asString ?: "")
                    descriptionList.add(descriptionObj.get("4")?.asString ?: "")
                    descriptionList.add(descriptionObj.get("5")?.asString ?: "")

                    feedbackEntry.ratingDescription = descriptionList

                }




                feedbackPOJOList.add(feedbackPOJO)

            }


            return feedbackPOJOList
        }

        fun formAddressDetailFromPincode(respObj: JsonObject): PostalAddress? {
            val addressRespList = respObj.getAsJsonArray("data")
            if (addressRespList != null) {

                if (addressRespList.size() == 0) {
                    return null
                } else {
                    val postalAddress = PostalAddress()
                    val addressResp = addressRespList.get(0).asJsonObject
                    postalAddress.area = addressResp.get("area")?.asString
                    postalAddress.district = addressResp.get("district")?.asString
                    postalAddress.city = addressResp.get("city")?.asString
                    postalAddress.state = addressResp.get("state")?.asString

                    postalAddress.postalCode = addressResp.get("pincode")?.asString
                    return postalAddress
                }
            }
            return null
        }

        fun formDistrictList(respObj: JsonObject): ArrayList<String> {

            val districtArr = respObj.getAsJsonArray("data")?.asJsonArray

            val districtList = arrayListOf<String>()

            districtArr?.forEach {
                districtList.add(it.asString)
            }

            return districtList
        }

        fun formKioskSettings(respObj: JsonObject): KioskSettings? {

            val kioskSettings = KioskSettings()
            val practiceSettingsArr = respObj.getJsonArray("practicesettings") ?: return null

            // Flatten practiceSettings array into a map for quick lookup
            val settingsMap = mutableMapOf<String, String>()
            practiceSettingsArr.forEach { element ->
                val obj = element.asJsonObject
                obj.entrySet().forEach { entry ->
                    settingsMap[entry.key] = if (entry.value.isJsonPrimitive) {
                        entry.value.asString
                    } else {
                        entry.value.toString()
                    }
                }
            }

            kioskSettings.country = settingsMap["country"] ?: ""
            kioskSettings.isTeleFillConsentForm =
                settingsMap["tele_fill_consent_form"].equals("true", ignoreCase = true)
            kioskSettings.isSMSEnabled =
                settingsMap["PRACTICE_SMS_STATUS"].equals("true", ignoreCase = true)
            kioskSettings.isPremiumUser =
                settingsMap["is_premium_group"].equals("true", ignoreCase = true)

            val accessingLocationDetails = settingsMap["accessing_location_details"]
            if (accessingLocationDetails != null) {
                val locationObj = JsonParser.parseString(accessingLocationDetails).asJsonObject
                kioskSettings.mobileCode = locationObj.getString("mobile_code") ?: ""
                kioskSettings.accessLocationCountryCode =
                    locationObj.getString("country_code") ?: ""
            }
            kioskSettings.defaultGender = settingsMap["default_gender_prop"] ?: ""

            val accountDetailsString = settingsMap["accountDetails"]
            if (accountDetailsString != null) {
                val accountDetailsObj = JsonParser.parseString(accountDetailsString).asJsonObject
                val accountDetails = AccountDetails(
                    loginId = accountDetailsObj.getString("login_id") ?: "",
                    accountType = accountDetailsObj.getString("account_type") ?: "",
                    isTelehealthCheckin = accountDetailsObj.getBoolean("is_telehealth_checkin")
                        ?: false,
                    kioskType = accountDetailsObj.getString("kiosk_type") ?: "",
                    practiceId = accountDetailsObj.getString("practice_id") ?: ""
                )
                val kioskPreferenceObj = accountDetailsObj.getAsJsonObject("kiosk_preferences")
                val kioskPreferences = KioskPreferences(
                    isTelehealthCheckIn = kioskPreferenceObj?.getBoolean("is_telehealth_checkin")
                        ?: false,
                    insuranceRequired = kioskPreferenceObj?.getBoolean("insurance_required")
                        ?: false,
                    patientDemographicsEditRequired = kioskPreferenceObj?.getBoolean("patient_demographics_edit_required")
                        ?: false,
                    questionnairesRequired = kioskPreferenceObj?.getBoolean("questionnaires_required")
                        ?: false,
                    consentFormsRequired = kioskPreferenceObj?.getBoolean("consent_forms_required")
                        ?: false,
                    appointmentStatus = AppointmentStatus(
                        statusId = kioskPreferenceObj?.getString("appointment_status_id") ?: "",
                    ),
                )
                accountDetails.kioskPreferences = kioskPreferences
                kioskSettings.accountDetails = accountDetails
            }

            return kioskSettings

        }

        //endregion

        //region shared pref db parse

        fun formFacilityObj(facilityStr: String): Facility {
            val facility = Facility()


            try {
                val facilityObj = JsonParser.parseString(facilityStr).asJsonObject

                facility.facilityId = facilityObj.get("facility_id")?.asString
                facility.facilityName = facilityObj.get("facility_name")?.asString

                val postalAddress = PostalAddress()

                postalAddress.city = facilityObj.get("city")?.asString ?: ""
                postalAddress.state = facilityObj.get("state")?.asString ?: ""
                postalAddress.country = facilityObj.get("country")?.asString ?: ""
                facility.postalAddress = postalAddress
            } catch (e: Exception) {
                Log.d("facilityParseException", e.toString())
            }



            return facility
        }

        fun formServicesList(servicesStr: String): ArrayList<Service> {
            val serviceList = arrayListOf<Service>()

            try {
                val serviceArr = JsonParser.parseString(servicesStr).asJsonArray

                serviceArr.forEach { servicesEle ->
                    val servicesObj = servicesEle.asJsonObject
                    val service = Service()
                    service.serviceId = servicesObj.get("service_id")?.asString ?: ""
                    service.logo = servicesObj.get("logo")?.asString ?: ""
                    service.serviceLabel = servicesObj.get("service_label")?.asString ?: ""
                    service.serviceURL = servicesObj.get("service_url")?.asString ?: ""

                    serviceList.add(service)
                }
            } catch (e: Exception) {
                Log.d("servicesParseException", e.toString())
            }

            return serviceList
        }

        fun formFeedbackQuestionnaireList(feedbackStr: String): ArrayList<FeedbackLanguageQuestionnaire> {
            val feedbackList = arrayListOf<FeedbackLanguageQuestionnaire>()

            try {
                val feedbackArr = JsonParser.parseString(feedbackStr).asJsonArray

                feedbackArr.forEach { feedbackEle ->
                    val feedbackObj = feedbackEle.asJsonObject
                    val feedbackLanguageQuestionnaire = FeedbackLanguageQuestionnaire()
                    feedbackLanguageQuestionnaire.language =
                        feedbackObj.get("language")?.asString ?: ""
                    feedbackLanguageQuestionnaire.questionnaireId =
                        feedbackObj.get("questionnaireId")?.asString ?: ""
                    feedbackLanguageQuestionnaire.questionnaireLabel =
                        feedbackObj.get("questionnaireLabel")?.asString ?: ""

                    feedbackList.add(feedbackLanguageQuestionnaire)
                }
            } catch (e: Exception) {
                Log.d("feedbackParseException", e.toString())
            }

            return feedbackList
        }

        fun formSelectedMember(memberStr: String): ArrayList<Member> {
            val memberList = ArrayList<Member>()
            if (memberStr.isBlank()) return memberList
            try {
                val parser = JsonParser.parseString(memberStr)
                val memberArr = if (parser.isJsonObject) {
                    val jsonarr = JsonArray()
                    jsonarr.add(parser.asJsonObject)
                    jsonarr
                } else {
                    parser.asJsonArray
                }


                memberArr?.forEach { memberEle ->
                    val memberObj = memberEle.asJsonObject
                    if (memberObj != null) {
                        val member = Member()

                        member.memberId = memberObj.get("member_id")?.asString
                        member.prefix = memberObj.get("prefix")?.asString
                        member.firstName = memberObj.get("first_name")?.asString
                        member.lastName = memberObj.get("last_name")?.asString
                        member.fullName = memberObj.get("full_name")?.asString
                        member.gender = memberObj.get("gender")?.asString
                        member.specialization = memberObj.get("specialization")?.asString
                        member.degree = memberObj.get("degree")?.asString
                        member.isPhotoAvailable =
                            memberObj.get("is_member_photo_available")?.asBoolean

                        member.isMemberOnlineAppEnabled = try {
                            memberObj.get("is_member_online_app_enabled")?.asBoolean
                        } catch (e: Exception) {
                            null
                        }
                        member.introduction = memberObj.get("introduction")?.asString ?: ""

                        val contactDetails = ContactDetails()



                        contactDetails.homePhone = memberObj.get("home_phone")?.asString
                        contactDetails.mobile = memberObj.get("mobile")?.asString
                        contactDetails.emailId = memberObj.get("email")?.asString

                        val postalAddress = PostalAddress()

                        postalAddress.line1 = memberObj.get("address_line1")?.asString
                        postalAddress.line2 = memberObj.get("address_line2")?.asString
                        postalAddress.city = memberObj.get("city")?.asString
                        postalAddress.state = memberObj.get("state")?.asString
                        postalAddress.country = memberObj.get("country")?.asString
                        postalAddress.postalCode = memberObj.get("zip_code")?.asString

                        member.contactDetails = contactDetails
                        member.postalAddress = postalAddress

                        memberList.add(member)
                    }
                }

            } catch (e: Exception) {
                Log.d("memberParseException", e.toString())
                return arrayListOf()
            }

            return memberList
        }

        fun getFacilityList(respObj: JsonObject): ArrayList<Facility> {

            val facilityArrList = ArrayList<Facility>()
            val facilityObjectArr = respObj.get("mapped_facilities").asJsonArray
            facilityObjectArr?.forEach {

                val facilityObj = it.asJsonObject
                val facility = Facility()
                facility.facilityId = facilityObj.get("facility_id")?.asString
                facility.facilityName = facilityObj.get("facility_name")?.asString
                facility.facilityCode = facilityObj.get("facility_code")?.asString
                val str = facilityObj.get("facility_status")?.asString
                if (!str.isNullOrBlank()) {
                    facility.status = str
                }
                val postalAddress = PostalAddress()

                postalAddress.city = facilityObj.get("city")?.asString ?: ""
                postalAddress.state = facilityObj.get("state")?.asString ?: ""
                postalAddress.country = facilityObj.get("country")?.asString ?: ""
                facility.postalAddress = postalAddress
                facilityArrList.add(facility)
            }
            return facilityArrList
        }

        //endregion

        //region Questionnaire

        fun parsePracticeQuestionnaire(jsonResponse: JsonObject): List<QuestionnairePhysicianTemplate> {
            val templates = mutableListOf<QuestionnairePhysicianTemplate>()
            val responseBodyArr = jsonResponse.getAsJsonArray("RESPONSE_BODY")

            responseBodyArr.forEach {
                val obj = it.asJsonObject
                val template = QuestionnairePhysicianTemplate()
                template.apply {
                    templateId = obj.getString("TEMPLATE_ID")
                    memberId = obj.getString("MEMBER_ID")
                    name = obj.getString("NAME")
                    type = obj.getString("TYPE")
                    timeOfCreation = obj.getString("TIME_OF_CREATION")
                    comments = obj.getString("COMMENTS")
                    isPracticeTemplate = obj.getBoolean("IS_PRACTICE_TEMPLATE")
                    isDeleted = obj.getBoolean("IS_DELETED")
                    isEditable = obj.getBoolean("IS_EDITABLE")
                }
                //Note: No PatQuesMap because it is practice template list
                templates.add(template)
            }
            return templates
        }

        fun parsePracticeConsentForm(jsonResponse: JsonObject): List<ConsentForm> {
            val consentForms = mutableListOf<ConsentForm>()
            val responseBodyArr = jsonResponse.getAsJsonArray("RESPONSE_BODY")

            responseBodyArr.forEach {
                val obj = it.asJsonObject
                val consentForm = ConsentForm()
                consentForm.apply {
                    fileSize = obj.getString("FILE_SIZE")
                    fileId = obj.getString("FILE_ID")
                    fileType = obj.getString("FILE_TYPE")
                    fileName = obj.getString("FILE_NAME")
                    uploadedBy = obj.getString("UPLOADED_BY")
                }

                consentForms.add(consentForm)
            }
            return consentForms
        }

        fun parsePatientsSharedQuestionnaires(jsonResponse: JsonObject): List<QuestionnairePhysicianTemplate> {

            val responseBody = jsonResponse.getAsJsonArray("RESPONSE_BODY")
            val templates = mutableListOf<QuestionnairePhysicianTemplate>()

            responseBody.forEach { element ->
                val obj = element.asJsonObject
                val questionnaireObj = obj.getAsJsonObject("QUESTIONNAIRE")

                val template = QuestionnairePhysicianTemplate()
                template.apply {
                    templateId = questionnaireObj.getString("TEMPLATE_ID")
                    memberId = questionnaireObj.getString("MEMBER_ID")
                    name = questionnaireObj.getString("NAME")
                    type = questionnaireObj.getString("TYPE")
                    timeOfCreation = questionnaireObj.getString("TIME_OF_CREATION")
                    comments = questionnaireObj.getString("COMMENTS")
                    isPracticeTemplate = questionnaireObj.getBoolean("IS_PRACTICE_TEMPLATE")
                    isDeleted = questionnaireObj.getBoolean("IS_DELETED")
                    isEditable = questionnaireObj.getBoolean("IS_EDITABLE")
                }

                val patientQuesMap = PatientQuesMap()
                patientQuesMap.apply {
                    patientQuesMapId = obj.getString("PATIENT_QUES_MAP_ID")
                    practicePatientId = obj.getString("PRACTICE_PATIENT_ID")
                    isFilled = obj.getBoolean("IS_FILLED") == true
                    isSubmitted = obj.getBoolean("IS_SUBMITTED") == true
                }
                template.patientQuesMap = patientQuesMap

                templates.add(template)
            }
            return templates
        }

        fun parseUpdateSharedQuestionnaire(jsonObject: JsonObject): Boolean {

            return true
        }

        fun parseQuestionnaireWithAnswers(jsonResponse: JsonObject): QuestionnaireMetaData {
            val questionnaireWithAnsObj = jsonResponse.getAsJsonObject("questionnaire_with_answers")
            val questionnaireType = questionnaireWithAnsObj.getString("questionnaire_type")
            val templateName = questionnaireWithAnsObj.getString("template_name")
            val isSubmitted = questionnaireWithAnsObj.getBoolean("is_submitted")
            val patientId = questionnaireWithAnsObj.getString("patient_id")
            val templateId = questionnaireWithAnsObj.getString("template_id")
            val quesMapId = questionnaireWithAnsObj.getString("ques_map_id")
            val questionsArr = questionnaireWithAnsObj.getJsonArray("questions")

            val questions = mutableListOf<Question>()
            var currentSection: Question.Section? = null
            val unSupportedQuestionnaireType = mutableListOf<QuestionnaireType>()
            if (questionsArr != null) {
                for (qElem in questionsArr) {
                    val questionTypeObj = qElem.asJsonObject
                    val entryId = questionTypeObj.getString("entry_id")
                    val notes = questionTypeObj.getString("notes")
                    val notesType = questionTypeObj.getString("notes_type")
                    val isDeleted = questionTypeObj.getBoolean("is_deleted")
                    val isMandatory = questionTypeObj.getBoolean("is_mandatory")
                    val answer = questionTypeObj.getString("answer")
                    val isMultiChoice = questionTypeObj.getBoolean("is_multi_choice")
                    if (isDeleted == false) {
                        val question = when (notesType) {
                            QuestionnaireType.Label.key -> {
                                Question.Label(
                                    entryId, notes, notesType, isDeleted,
                                    isMandatory == true, answer,
                                    fontWeight = questionTypeObj.get("font_weight")?.asString,
                                    textDecoration = questionTypeObj.get("text_decoration")?.asString,
                                    fontSize = questionTypeObj.get("font_size")?.asInt,
                                    fontStyle = questionTypeObj.get("font_style")?.asString,
                                    textAlign = questionTypeObj.get("text_align")?.asString
                                )
                            }

                            QuestionnaireType.QuestionWithOptions.key, QuestionnaireType.QuestionWithFixedOptions.key -> {
                                val optionList = TemplatesUtil.getOptionListFromJson(
                                    questionTypeObj,
                                    isMultiChoice == true,
                                    answer ?: "",
                                    QuestionnaireType.QuestionWithOptions.key == notesType
                                )
                                Question.QuestionWithOptions(
                                    entryId, notes, notesType, isDeleted,
                                    isMandatory == true, answer,
                                    isMultiChoice = isMultiChoice == true,
                                    options = optionList
                                )
                            }

                            QuestionnaireType.RatingScale.key -> {
                                Question.RatingScaleQuestion(
                                    entryId, notes, notesType, isDeleted,
                                    isMandatory == true, answer,
                                    fromScale = questionTypeObj.get("from_scale")?.asInt ?: 0,
                                    toScale = questionTypeObj.get("to_scale")?.asInt ?: 10
                                )
                            }

                            QuestionnaireType.YesOrNoQuestion.key -> {
                                val optionYes = OptionData("Yes")
                                val optionNo = OptionData("No")
                                if (answer.equals("yes", true)) {
                                    optionYes.isChecked = true
                                } else if (answer.equals("no", true)) {
                                    optionNo.isChecked = true
                                }
                                val optionList = arrayListOf<OptionData>()
                                optionList.add(optionYes)
                                optionList.add(optionNo)

                                Question.YesOrNoQuestion(
                                    entryId, notes, notesType, isDeleted,
                                    isMandatory == true, answer, options = optionList
                                )
                            }

                            QuestionnaireType.Date.key -> {
                                var defaultValue:String?=null
                                val newDate = if (!answer.isNullOrBlank()) {
                                    defaultValue= DateUtil.getDate(answer, ModelUtil.charmDisplayDateFormat())
                                        ?.toString(ModelUtil.mothFirstDateFormatWithSlash())
                                    defaultValue
                                } else answer
                                Question.DateQuestion(
                                    entryId, notes, notesType, isDeleted,
                                    isMandatory == true, newDate,defaultValue=defaultValue
                                )
                            }

                            QuestionnaireType.Signature.key -> {
                                Question.SignatureQuestion(
                                    entryId,
                                    notes,
                                    notesType,
                                    isDeleted,
                                    isMandatory == true,
                                    answer,
                                    signatureUrl = questionTypeObj.getString("signature_url")
                                )
                            }

                            QuestionnaireType.Section.key -> {
                                if (currentSection != null) {
                                    questions.add(currentSection)
                                }
                                currentSection = Question.Section(
                                    entryId, notes, notesType, isDeleted, isMandatory == true,
                                    isCollapsible = questionTypeObj.getBoolean("is_collapsible") == true,
                                    expandByDefault = questionTypeObj.getBoolean("expand_by_default") == true,
                                    questions = mutableListOf()
                                )
                                null
                            }

                            QuestionnaireType.SimpleQuestion.key, QuestionnaireType.Question.key -> {
                                Question.SimpleQuestion(
                                    entryId, notes = notes, notesType, isDeleted,
                                    isMandatory == true, answer = answer,
                                    isMultiLine = QuestionnaireType.Question.key == notesType
                                )
                            }

                            QuestionnaireType.ChiefComplaints.key, QuestionnaireType.HistoryOfPresentIllness.key, QuestionnaireType.Symptoms.key, QuestionnaireType.PastMedicalHistory.key,
                            QuestionnaireType.FamilyHistory.key, QuestionnaireType.SocialHistory.key, QuestionnaireType.ReviewOfSystems.key -> {
                                val soapWidgetObj = questionTypeObj.getAsJsonObject("soap_widget")
                                var soapWidget: SOAPWidget? = null
                                if (soapWidgetObj != null) {
                                    val widgetId = soapWidgetObj.getString("soap_widget_id")
                                    val templateId = soapWidgetObj.getString("soap_template_id")
                                    val widgetName = soapWidgetObj.getString("soap_widget_name")
                                    val widgetModule = soapWidgetObj.getString("soap_widget_module")
                                    val entriesArr = soapWidgetObj.getJsonArray("entries")
                                    val entries = mutableListOf<SOAPEntry>()
                                    entriesArr?.forEach { entryElem ->
                                        val entryObj = entryElem.asJsonObject
                                        val soapEntryTypeStr = entryObj.getString("soap_entry_type")
                                        val soapEntryType =
                                            SOAPEntryType.entries.find { it.type == soapEntryTypeStr }
                                        val soapEntry = when (soapEntryType) {
                                            SOAPEntryType.Header -> {
                                                var fontSize = 14
                                                var fontStyle = ""
                                                var fontWeight = ""
                                                var textAlign = ""
                                                var textDecoration = ""
                                                val property = entryObj.getString("PROPERTIES")
                                                val propertyArr =
                                                    property?.split(ModelUtil.swOptionsSeparatorSymbol())
                                                if (!propertyArr.isNullOrEmpty()) {
                                                    propertyArr.forEach { propertyValue ->
                                                        val propertyValueArr =
                                                            propertyValue.split(":")
                                                        if (propertyValueArr.size == 2) {
                                                            when (propertyValueArr[0]) {
                                                                ModelUtil.swfontSize() -> {
                                                                    val len =
                                                                        propertyValueArr[1].length
                                                                    val fontSizeWithPx =
                                                                        propertyValueArr[1].substring(
                                                                            0,
                                                                            len - 2
                                                                        )
                                                                    fontSize =
                                                                        fontSizeWithPx.toFloat()
                                                                            .roundToInt()
                                                                }

                                                                ModelUtil.swtextAlign() -> {
                                                                    textAlign = propertyValueArr[1]
                                                                }

                                                                ModelUtil.swfontWeight() -> {
                                                                    fontWeight = propertyValueArr[1]
                                                                }

                                                                ModelUtil.swfontStyle() -> {
                                                                    fontStyle = propertyValueArr[1]
                                                                }

                                                                ModelUtil.swtextDecoration() -> {
                                                                    textDecoration =
                                                                        propertyValueArr[1]
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                SOAPEntry.HeadingEntry(
                                                    soapEntryId = entryObj.getString("soap_entry_id"),
                                                    soapEntryName = entryObj.getString("soap_entry_name"),
                                                    isMandatory = entryObj.getBoolean("is_mandatory") == true,
                                                    answer = entryObj.getString("soap_entry_answer"),
                                                    fontSize = fontSize,
                                                    fontWeight = fontWeight,
                                                    fontStyle = fontStyle,
                                                    textAlign = textAlign,
                                                    textDecoration = textDecoration
                                                )
                                            }

                                            SOAPEntryType.TextBox -> SOAPEntry.TextBoxEntry(
                                                soapEntryId = entryObj.getString("soap_entry_id"),
                                                soapEntryName = entryObj.getString("soap_entry_name"),
                                                isMandatory = entryObj.getBoolean("is_mandatory") == true,
                                                defaultValue = entryObj.getString("DEFAULT_VALUE"),
                                                answer = entryObj.getString("soap_entry_answer")
                                                    ?: "",
                                            )

                                            SOAPEntryType.CheckList, SOAPEntryType.CheckBox -> {
                                                val response = getOptionListFromJson(
                                                    entryObj,
                                                    soapEntryType == SOAPEntryType.CheckList
                                                )
                                                SOAPEntry.CheckboxEntry(
                                                    soapEntryId = entryObj.getString("soap_entry_id"),
                                                    soapEntryName = entryObj.getString("soap_entry_name"),
                                                    soapEntryType = soapEntryType,
                                                    isMandatory = entryObj.getBoolean("is_mandatory") == true,
                                                    answer = entryObj.getString("answer")
                                                        .toString(),
                                                    optionList = response.first,
                                                    isAdditionalComments = entryObj.getBoolean("ADDITIONAL_COMMENT") == true,
                                                    isIncludeOthers = entryObj.getBoolean("INCLUDE_OTHERS") == true,
                                                    additionalComment = response.second
                                                )
                                            }

                                            SOAPEntryType.Radio -> {
                                                val response = getOptionListFromJson(entryObj)
                                                SOAPEntry.RadioEntry(
                                                    soapEntryId = entryObj.getString("soap_entry_id"),
                                                    soapEntryName = entryObj.getString("soap_entry_name"),
                                                    isMandatory = entryObj.getBoolean("is_mandatory") == true,
                                                    answer = entryObj.getString("answer")
                                                        .toString(),
                                                    additionalComment = response.second,
                                                    includeOthers = entryObj.getString("INCLUDE_OTHERS"),
                                                    defaultValue = entryObj.getString("DEFAULT_VALUE"),
                                                    optionList = response.first,
                                                    isAdditionalComments = entryObj.getBoolean("ADDITIONAL_COMMENT") == true,
                                                    isIncludeOthers = entryObj.getBoolean("INCLUDE_OTHERS") == true
                                                )
                                            }

                                            SOAPEntryType.Table -> {
                                                val soapWidgetTableData =
                                                    if (entryObj.getString("soap_entry_answer")
                                                            .isNullOrBlank()
                                                    ) {
                                                        getTableData(
                                                            entryObj.getString("soap_entry_name")
                                                                ?: ""
                                                        )
                                                    } else {
                                                        getTableData(
                                                            entryObj.getString("soap_entry_answer")
                                                                ?: ""
                                                        )
                                                    }
                                                SOAPEntry.TableEntry(
                                                    soapEntryId = entryObj.getString("soap_entry_id"),
                                                    soapEntryName = entryObj.getString("soap_entry_name"),
                                                    isMandatory = entryObj.getBoolean("is_mandatory") == true,
                                                    defaultValue = entryObj.getString("DEFAULT_VALUE"),
                                                    soapWidgetTableData = soapWidgetTableData,
                                                    answer = ""
                                                )
                                            }

                                            SOAPEntryType.SimpleQuestion -> SOAPEntry.SimpleQuestionEntry(
                                                soapEntryId = entryObj.getString("soap_entry_id"),
                                                soapEntryName = entryObj.getString("soap_entry_name"),
                                                isMandatory = entryObj.getBoolean("is_mandatory") == true,
                                                defaultValue = entryObj.getString("DEFAULT_VALUE"),
                                                isMultiLine = entryObj.getString("OPTIONS")
                                                    ?.contains("MULTILINE:true") == true,
                                                answer = entryObj.getString("soap_entry_answer"),
                                            )

                                            SOAPEntryType.RatingScale -> {
                                                val minMaxValue = entryObj.getString("OPTIONS")
                                                    ?.split(ModelUtil.swOptionsSeparatorSymbol())
                                                val min = minMaxValue?.first()?.toInt() ?: 0
                                                val max = minMaxValue?.last()?.toInt() ?: 10
                                                SOAPEntry.RatingScaleEntry(
                                                    soapEntryId = entryObj.getString("soap_entry_id"),
                                                    soapEntryName = entryObj.getString("soap_entry_name"),
                                                    isMandatory = entryObj.getBoolean("is_mandatory") == true,
                                                    fromScale = min,
                                                    toScale = max,
                                                    answer = entryObj.getString("soap_entry_answer")
                                                )
                                            }

                                            SOAPEntryType.Image -> {
                                                SOAPEntry.ImageEntry(
                                                    soapEntryId = entryObj.getString("soap_entry_id"),
                                                    soapEntryName = entryObj.getString("soap_entry_name"),
                                                    isMandatory = entryObj.getBoolean("is_mandatory") == true,
                                                    answer = entryObj.getString("soap_entry_answer")
                                                )
                                            }

                                            SOAPEntryType.YesOrNoQuestion -> {

                                                val optionYes = OptionData("Yes")
                                                val optionNo = OptionData("No")
                                                val ansComment =
                                                    entryObj.getString("soap_entry_answer")
                                                        ?.split(ModelUtil.swOptionsSeparatorSymbol())
                                                val onlyAnswer =
                                                    ansComment?.first()?.toString() ?: ""
                                                val comments =
                                                    if (ansComment?.size == 2) ansComment[1] else ""
                                                if (onlyAnswer.equals("yes", true)) {
                                                    optionYes.isChecked = true
                                                } else if (onlyAnswer.equals("no", true)) {
                                                    optionNo.isChecked = true
                                                }
                                                val isAdditionalCommentForYes =
                                                    entryObj.getBoolean("ADDITIONAL_COMMENT_FOR_YES") == true
                                                if (isAdditionalCommentForYes) {
                                                    optionYes.isOther = true
                                                    if (comments.contains("COMMENT_FOR_YES")) {
                                                        optionYes.otherValue =
                                                            comments.substring(16)
                                                    }
                                                }
                                                val isAdditionalCommentForNo =
                                                    entryObj.getBoolean("ADDITIONAL_COMMENT_FOR_NO") == true
                                                if (isAdditionalCommentForNo) {
                                                    optionNo.isOther = true
                                                    if (comments.contains("COMMENT_FOR_NO")) {
                                                        optionNo.otherValue = comments.substring(15)
                                                    }
                                                }
                                                val optionList = arrayListOf<OptionData>()
                                                optionList.add(optionYes)
                                                optionList.add(optionNo)

                                                SOAPEntry.YesOrNoEntry(
                                                    soapEntryId = entryObj.getString("soap_entry_id"),
                                                    soapEntryName = entryObj.getString("soap_entry_name"),
                                                    isMandatory = entryObj.getBoolean("is_mandatory") == true,
                                                    answer = entryObj.getString("soap_entry_answer"),
                                                    optionList = optionList,
                                                )
                                            }

                                            else -> null
                                        }
                                        if (soapEntry != null) entries.add(soapEntry)
                                    }
                                    soapWidget = SOAPWidget(
                                        soapWidgetId = widgetId,
                                        soapTemplateId = templateId,
                                        soapWidgetName = widgetName,
                                        soapWidgetModule = widgetModule,
                                        entries = entries
                                    )
                                    Question.SOAPWidgetQuestion(
                                        entryId, notes, notesType, isDeleted,
                                        isMandatory == true, answer,
                                        soapWidget = soapWidget
                                    )
                                } else null
                            }

                            else -> {
                                val type = QuestionnaireType.entries.find { it.key == notesType }
                                if (type != null) {
                                    unSupportedQuestionnaireType.add(
                                        type
                                    )
                                }
                                null
                            }
                        }
                        if (notesType == QuestionnaireType.Section.key) {
                            continue
                        }
                        question?.let {
                            if (currentSection != null) {
                                (currentSection.questions as MutableList).add(it)
                            } else {
                                questions.add(it)
                            }
                        }
                    }
                }
            }
            if (currentSection != null) {
                questions.add(currentSection)
            }
            return QuestionnaireMetaData(
                questionnaireType = questionnaireType,
                templateName = templateName,
                isSubmitted = isSubmitted == true,
                patientId = patientId,
                questions = questions,
                templateId = templateId,
                quesMapId = quesMapId,
                unSupportedQuestionnaireType = unSupportedQuestionnaireType
            )
        }

        fun getOptionListFromJson(
            entryObj: JsonObject,
            isCheckList: Boolean = false
        ): Pair<ArrayList<OptionData>, String?> {
            val optionsAL = if (isCheckList) {
                TemplatesUtil.getOptionsArrayFromString(
                    entryObj.getString("soap_entry_name")
                        ?: ""
                )
            } else {
                TemplatesUtil.getOptionsArrayFromString(
                    entryObj.getString("OPTIONS")
                        ?: ""
                )
            }
            val answerAL = TemplatesUtil.getAnswerMapFromString(
                entryObj.getString("soap_entry_answer") ?: ""
            )
            val optionList =
                TemplatesUtil.getOptionList(
                    optionsAL,
                    answerAL,
                    0
                )
            val isAdditionalComment =
                entryObj.getBoolean("ADDITIONAL_COMMENT") == true
            val isIncludeOthers =
                entryObj.getBoolean("INCLUDE_OTHERS") == true
            var additionalComments: String? = null
            if (isAdditionalComment && answerAL.isNotEmpty()) {
                val comment = answerAL.entries.last().key
                if (comment.contains(ModelUtil.appendStringCommentsValue)) {
                    additionalComments =
                        comment.substring(14, comment.length)
                    answerAL.remove(comment)
                }
            }
            if (isIncludeOthers) {
                val optionData = OptionData()
                optionData.optionString = "Others"
                optionData.viewLevel = 0
                optionData.isOther = true
                if (answerAL.isNotEmpty()) {
                    val otherValue =
                        answerAL.entries.first().key
                    if (otherValue.isNotBlank()) {
                        optionData.isChecked = true
                        optionData.otherValue = otherValue
                    }
                }
                optionList.add(
                    optionData
                )
            }
            return Pair(optionList, additionalComments)
        }

        //endregion

        fun parseFileUploadResponse(jsonObject: JsonObject): ArrayList<String> {
            val fileIdList = ArrayList<String>()
            val attachArr = jsonObject.getAsJsonArray("attachments")

            attachArr.forEach {
                val obj = it as JsonObject
                val fId = obj.get("file_id")?.asString
                if (!fId.isNullOrBlank()) {
                    fileIdList.add(fId)
                }
            }
            return fileIdList
        }

        fun getTableData(tableDataString: String): ArrayList<ArrayList<SOAPTemplateTableCell>> {
            val tableDataArr = tableDataString.parseStringAsJsonArray()
            val tableData: ArrayList<ArrayList<SOAPTemplateTableCell>> = arrayListOf()
            tableDataArr?.forEach { rowData ->
                val rowDataArr = rowData.asJsonArray
                val rowDataList: ArrayList<SOAPTemplateTableCell> = arrayListOf()
                rowDataArr.forEach { celldata ->
                    val cellJson = celldata.asJsonObject
                    val cellData = SOAPTemplateTableCell()
                    cellData.value = cellJson.getString("value") ?: ""
                    cellData.colspan = cellJson.getString("colspan") ?: ""
                    cellData.type = cellJson.getString("type") ?: ""
                    rowDataList.add(cellData)
                }
                tableData.add(rowDataList)
            }
            return tableData
        }

        fun parseAddPatientResponse(jsonObject: JsonObject): String {
            val patientJson = jsonObject.getJsonObject("patient")
            return patientJson?.getString("patient_id") ?: ""
        }


        fun parseDuplicatePatientFromErrorInfo(errorInfo: JsonObject?): Patient? {
            if (errorInfo == null) return null
            return try {
                val dob = errorInfo.getString("dob")?.let {
                    runCatching { DateTime.parse(it) }.getOrNull()
                }
                Patient(
                    practicePatientId = errorInfo.getString("patient_id"),
                    patientRecordId = errorInfo.getString("record_id"),
                    fullName = errorInfo.getString("full_name"),
                    firstName = errorInfo.getString("first_name"),
                    lastName = errorInfo.getString("last_name"),
                    gender = errorInfo.getString("gender"),
                    dateOfBirth = dob,
                    isActive = errorInfo.getString("is_active")?.equals("true", ignoreCase = true) ?: true,
                    isPhotoAvailableOnline = false
                )
            } catch (e: Exception) {
                null
            }
        }

        fun parsePostPatientInsurancesResponse(respObj: JsonObject): Boolean {
            val code = respObj.get("code")?.asInt
            return code == 0
        }

        fun parseAddInsuranceResponse(respObj: JsonObject): Pair<Boolean, String?> {
            val code = respObj.get("code")?.asInt
            val insuranceId = respObj.getAsJsonObject("insurance")?.get("insurance_id")?.asString
            val success = code == 0 && insuranceId != null
            return Pair(success, insuranceId)
        }

        fun formPayerDetails(respObj: JsonObject): List<PracticePayer> {
            val payersList = mutableListOf<PracticePayer>()
            val payersArray = respObj.getAsJsonArray("payers")
            payersArray?.forEach { payerElem ->
                val payerObj = payerElem.asJsonObject
                val practicePayer = PracticePayer(
                    practicePayerId = payerObj.getString("practice_payer_id"),
                    payerId = payerObj.getString("payer_id"),
                    payerName = payerObj.getString("payer_name"),
                    country = payerObj.getString("country"),
                    state = payerObj.getString("state"),
                    city = payerObj.getString("city"),
                    line1 = payerObj.getString("line_1"),
                    postboxNo = payerObj.getString("postbox_no"),
                    zipCode = payerObj.getString("zip_code"),
                    workPhone = payerObj.getString("work_phone"),
                    fax = payerObj.getString("fax"),
                    patientsUsageCount = payerObj.getInt("patients_usage_count"),
                    isActive = payerObj.getBoolean("is_active"),
                    isVerified = payerObj.getBoolean("is_verified")
                )
                payersList.add(practicePayer)
            }
            return payersList
        }

        fun formDemographicsMetricsFromString(cached: String): DemographicsMetrics {
            val jsonObj = JsonParser.parseString(cached).asJsonObject
            return formDemographicsMetrics(jsonObj)
        }

        fun formDemographicsSettingsFromString(cached: String): ArrayList<DemographicsSetting> {
            val jsonArr = JsonParser.parseString(cached).asJsonObject
            return formDemographicsSettings(jsonArr)
        }

        fun formCountryListFromString(cached: String): ArrayList<Country> {
            val jsonArr = JsonParser.parseString(cached).asJsonObject
            return formCountryList(jsonArr)
        }

        fun formStateListFromString(cached: String): List<State> {
            val jsonArr = JsonParser.parseString(cached).asJsonObject
            return formStateList(jsonArr)
        }

        fun formPayerDetailsFromString(cached: String): Any {
            val jsonObj = JsonParser.parseString(cached).asJsonObject
            return formPayerDetails(jsonObj)
        }

        fun parseDeleteInsuranceCardResponse(respObj: JsonObject): Pair<Boolean, String?> {
            val code = respObj.get("code")?.asInt
            val success = code == 0
            return Pair(success, respObj.getString("message"))
        }
        //endregion
    }
}

package com.charm.ehr.kiosk.common.util

import UiText
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import com.charm.ehr.kiosk.domain.Caregiver
import com.charm.ehr.kiosk.domain.Department
import com.charm.ehr.kiosk.domain.Member
import com.charm.ehr.kiosk.domain.MemberAvailability
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.edit_profile.FieldState
import com.charm.ehr.kiosk.presentation.edit_profile.PersonDetail
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.math.BigDecimal
import java.util.Locale
import kotlin.collections.get

class ModelUtil {
    companion object {

        val TAG = "ModelUtil"

        const val kioskSupportHeader = "Charm Kiosk: Android :: "

        const val patientRecordId = "patientRecordId"

        const val patientId = "patientId"

        const val language = "language"

        const val userInActivity = "userInActivity"

        const val applockType = "AppLockType"

        const val selectedFacilityId = "selectedFacilityId"
        const val selectedFacility = "selectedFacility"
        const val kioskSettings = "kioskSettings"

        const val demographicsMetrics = "demographicsMetrics"
        const val demographicsSetting = "demographicsSetting"
        const val countries = "countries"
        const val states = "states"
        const val payerDetails = "payerDetails"

        //region patient fields

        const val patFieldAddressLine1 = "AddressLine1"
        const val patFieldAddressLine2 = "AddressLine2"
        const val patFieldCity = "City"
        const val patFieldArea = "Area"
        const val patFieldDistrict = "District"
        const val patFieldState = "State"
        const val patFieldCountry = "Country"
        const val patFieldPincode = "Pincode"
        const val patFieldEmail = "Email"
        const val patFieldPassport = "Passport Num"
        const val patFieldAadhar = "Aadhar Num"

        const val patFieldMale = "Male"
        const val patFieldFemale = "Female"
        const val patFieldOthers = "others"
        const val patFieldUnknown = "Unknown"
        const val patFieldTransMale = "transgender male"
        const val patFieldTransFemale = "transgender  female"

        const val hideConsentForm = true

        //endregion

        //region otp

        const val allowEditMobile = "allowEditMobile"

        const val messageKey = "messageKey"

        const val selfCheckInMessageKey =
            "selfCheckinKiosk" //Dont change this key, it is used in server to identify the self check-in kiosk
        const val OTPResendTimeSeconds: Int = 6
        const val OTPResendAttempts: Int = 4
        const val IncorrectOTPAttempts: Int = 4
        const val messageKeyAddPatientSuccess = "addpatientsuccess"
        const val messageKeyAddPatient = "addpatient"
        const val messageKeyEditPatient = "editpatient"
        const val messageKeyAppointmentBooking = "appointmentbooking"
        const val messageKeySelfCheckin = "checkin"

        //endregion

        //region patient login page

        const val walkInAppointmentLogin = 1
        const val editPatientLogin = 2
        const val selfCheckInLogin = 3
        const val futureAppointmentLogin = 4

        const val loginCode = "loginCode"

        //endregion

        //region appointment

        const val apiTokenFailed = "apiTokenFailed"
        const val requestFailed = "Request failed"

        const val charmOTPLength = 7

        const val UpdateDataWorkerName="UpdateDataWorker"

        //Whenever members is fetched then it should be filtered by this criteria
        fun filterMembersByCriteria(memberList: ArrayList<Member>): ArrayList<Member> {

            val list = memberList.filter {
                it.fullName?.trim()?.isNotBlank() ?: false
            } as ArrayList<Member>

            return ArrayList(list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) {
                it.fullName ?: ""
            }))
        }

        fun filterAndSortDeparmnetList(departmentArrList: ArrayList<Department>): ArrayList<Department> {
            val list = departmentArrList.filter {
                it.departmentName?.trim()?.isNotBlank() ?: false
            } as ArrayList<Department>

            return ArrayList(list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) {
                it.departmentName ?: ""
            }))
        }

        fun filterByPastSlots(
            availabilitySlotsArrayList: ArrayList<MemberAvailability>,
            parseTime: DateTime? = null
        ): List<MemberAvailability> {

            val nowTime = parseTime ?: DateTime.now()

            return availabilitySlotsArrayList.filter {
                val startTime = it.startTime
                if (startTime != null) {
                    startTime.isAfter(nowTime)
                } else false

            }

        }

        //endregion

        //region date formats

        const val addPatientSoftInputDelayedTiming: Long = 200
        fun charmDisplayDateFormat() =
            DateTimeFormat.forPattern("MMM dd, yyyy").withLocale(Locale.ENGLISH)

        fun yearOnlyDateFormat() = DateTimeFormat.forPattern("yyyy")
        fun dateWithoutYearFormator() =
            DateTimeFormat.forPattern("MMM, d").withLocale(Locale.ENGLISH)

        fun charmPrinterDateTimeFormat() =
            DateTimeFormat.forPattern("MMM d, yyyy h:mm a").withLocale(Locale.ENGLISH)

        fun charmApiDateFormat() =
            DateTimeFormat.forPattern("yyyy-MM-dd").withLocale(Locale.ENGLISH)

        fun commonDateFormatWithSlash() =
            DateTimeFormat.forPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH)

        fun mothFirstDateFormatWithSlash() =
            DateTimeFormat.forPattern("MM/dd/yyyy").withLocale(Locale.ENGLISH)

        fun commonDateFormatWithIffen() =
            DateTimeFormat.forPattern("dd-MM-yyyy").withLocale(Locale.ENGLISH)

        fun commonDateFormatWithSlashReverse() =
            DateTimeFormat.forPattern("yyyy/MM/dd").withLocale(Locale.ENGLISH)

        fun commonDateFormatWithIffenReverse() =
            DateTimeFormat.forPattern("yyyy-MM-dd").withLocale(Locale.ENGLISH)

        fun sqlDateTimeFormatForNormalTiming() =
            DateTimeFormat.forPattern("yyyy-MM-dd hh:mm a").withLocale(Locale.ENGLISH)

        fun sqlDateTimeFormatForGettingAppointmentCount() =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withLocale(Locale.ENGLISH)

        fun md12HrTimeFormator() = DateTimeFormat.forPattern("hh:mm a").withLocale(Locale.ENGLISH)

        fun sqlTimeStampFormattor() =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withLocale(Locale.ENGLISH)

        fun splitDateTimeToIntegrs(dateTime: DateTime): Triple<Int, Int, Int> {

            val dateStr = dateTime.toString(charmApiDateFormat())
            val strArr = dateStr.split("-")

            val year = strArr[0].toInt() //year
            val month = strArr[1].toInt() //month
            val day = strArr[2].toInt() //day

            return Triple(year, month, day)
        }

        fun integersToDateTime(year: Int, month: Int, day: Int): DateTime? {

            val dateStr = year.toString() + "-" + month.toString()
                .padStart(length = 2, padChar = '0') + "-" + day.toString()
                .padStart(length = 2, padChar = '0')

            return DateUtil.getDate(dateStr, charmApiDateFormat())
        }

        //endregion


        //region auth manager

        const val authTokenFailed = "authTokenFailed"

        //endregion

        //region webcall error msg

        const val ioException = "Network Error"
        const val socketException = "Timeout Error"
        const val conversionException = "Conversion Error"
        const val unknownError = "Error occurred"

        //endregion

        //region states and countries

        const val countryStringIndia = "in"
        const val countryStringUs = "us"
        const val countryStringAustralia = "au"

        const val settingsRequest = "settingsRequest"
        const val countriesRequest = "countriesRequest"
        const val inSatesRequest = "inSatesRequest"
        const val usStatesRequest = "usStatesRequest"
        const val qualifierRequest = "qualifierRequest"
        const val appointmentStatusRequest = "appointmentStatusRequest"
        const val kioskSettingsRequest = "kioskSettingsRequest"

        fun inverseMap(map: HashMap<String, String>): HashMap<String, String> {
            val respMap = HashMap<String, String>()
            val keys = map.keys.iterator()
            keys.forEach {

                val value = map.get(it) ?: ""
                respMap.put(value, it)

            }
            return respMap
        }

        //endregion

        //region language settings

        const val langEnglish = "en"
        const val langTamil = "ta"
        const val langHindi = "hi"
        const val langTelugu = "te"

        //endregion

        const val MDMaxImageSizeInBytes = 1048576 // 1Mb in bytes (binary)

        //Permissions
        const val isFirstTimeForCameraPermission = "isFirstTimeForCameraPermission"
        const val isFirstTimeForReadStoragePermission = "isFirstTimeForReadStoragePermission"
        const val isFirstTimeForWriteStoragePermission = "isFirstTimeForWriteStoragePermission"

        //SOAP
        const val questionnaireLabelAlignmentLeft = "left"
        const val questionnaireLabelAlignmentRight = "right"
        const val questionnaireLabelAlignmentCenter = "center"
        const val questionnaireLabelAlignmentJustify = "justify"

        const val questionnaireLabelAlignmentBold = "Bold"
        const val questionnaireLabelAlignmentItalics = "Italic"
        const val questionnaireLabelAlignmentUnderline = "Underline"

        const val displayDateFormateMonthFirst = "MM/DD/YYYY"
        const val displayDateFormateDateFirst = "DD/MM/YYYY"

        fun swOptionsSeparatorSymbol() = "^^"
        fun swHierarchySymbol() = ">>"
        fun swTagConst() = 10000

        fun swfontSize() = "font-size"
        fun swtextAlign() = "text-align"
        fun swfontWeight() = "font-weight"
        fun swfontStyle() = "font-style"
        fun swtextDecoration() = "text-decoration"
        const val appendStringYesComments = "COMMENT_FOR_YES_"
        const val appendStringNoComments = "COMMENT_FOR_NO_"
        const val appendStringCommentsValue = "COMMENT_VALUE_"

        //
        val primaryPhoneOptions = listOf<String>("Mobile Phone", "Home Phone", "Work Phone")
        val relationshipList = listOf("Self", "Spouse", "Child", "Other")
        val insuranceTypeList = listOf(
            "MEDICARE",
            "MEDICAID",
            "TRICARE CHAMPUS",
            "CHAMPVA",
            "GROUP HEALTH PLAN",
            "FECA BLK LUNG",
            "OTHER"
        )

        const val internetErrorCode = -10

        const val primaryInsurance = "PRIMARY"
        const val secondaryInsurance = "SECONDARY"

        val supportedStateCountries = listOf("in", "us", "au")

//        private fun String.limitCharacters(limit: Int): String {
//            return if (limit < 0 || this.isEmpty()) {
//                ""
//            } else if (this.length > limit) {
//                this.substring(0, limit + 1) //Intentionally allowing one extra char to show error
//            } else {
//                this
//            }
//        }

        fun FieldState<String>.updateWithCharacterLimit(
            newValue: String?,
            maxLength: Int,
            errorMessageResId: Int,
            errorArgs: Array<Any> = arrayOf()
        ): FieldState<String> {

            if (newValue == null) return this

            val hasError = newValue.length > maxLength

            val trimmedValue = if (newValue.length > maxLength + 1) {
                this.value // Keep the old value when limit is exceeded
            } else {
                newValue
            }

            return this.copy(
                value = trimmedValue,
                error = if (hasError) UiText.StrResId(
                    id = errorMessageResId,
                    args = errorArgs
                ) else null
            )
        }


        fun JsonElement?.isNotJsonNull(): Boolean {
            return try {
                this != null && !this.isJsonNull
            } catch (e: Exception) {
                Log.e(TAG, Log.getStackTraceString(e))
                false
            }
        }

        fun String?.parseStringAsJsonObject(): JsonObject? {
            if (this.isNullOrBlank()) return null
            try {
                val jsonElement = JsonParser.parseString(this)
                if (jsonElement.isJsonObject && jsonElement.isNotJsonNull()) {
                    return jsonElement.asJsonObject
                }
            } catch (ex: Exception) {
                Log.e(TAG, ex.printStackTrace().toString())
            }
            return null
        }

        fun String?.parseStringAsJsonArray(): JsonArray? {
            if (this.isNullOrBlank()) return null
            try {
                val jsonElement = JsonParser.parseString(this)
                if (jsonElement.isJsonArray && jsonElement.isNotJsonNull()) {
                    return jsonElement.asJsonArray
                }
            } catch (ex: Exception) {
                Log.e(TAG, ex.printStackTrace().toString())
            }
            return null
        }

        fun getPhoneNumberFromPatient(patient: Patient): String {
            val homePhone = patient.contactDetails?.homePhone ?: ""
            val workPhone = patient.contactDetails?.workPhone ?: ""
            val mobile = patient.contactDetails?.mobile ?: ""

            val phoneString = if (mobile.isNotBlank()) {
                mobile
            } else if (homePhone.isNotBlank()) {
                homePhone
            } else if (workPhone.isNotBlank()) {
                workPhone
            } else {
                ""
            }
            return phoneString

        }

        fun getWorkPhoneFromPatient(patient: Patient): String {
            val workPhone = patient.contactDetails?.workPhone ?: ""
            val workExt = patient.contactDetails?.workPhoneExtn ?: ""
            return if (workExt.isNotBlank()) {
                workPhone + " X " + workExt
            } else {
                workPhone
            }
        }

        fun openUrlInExternalBrowser(context: Context, url: String) {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val packageManager = context.packageManager
            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, R.string.no_browser_found, Toast.LENGTH_LONG).show()
            }
        }

        fun getCaregiverFullName(caregiver: Caregiver): String {
            val firstName = caregiver.firstName ?: ""
            val middleName = caregiver.middleName ?: ""
            val lastName = caregiver.lastName ?: ""

            val fullName = StringBuilder()
            if (firstName.isNotBlank()) {
                fullName.append(firstName)
            }
            if (middleName.isNotBlank()) {
                fullName.append(" ").append(middleName)
            }
            if (lastName.isNotBlank()) {
                fullName.append(" ").append(lastName)
            }

            return fullName.toString()
        }

        fun getFullAddressFromPersonDetail(personDetail: PersonDetail?): String? {
            personDetail ?: return null
            personDetail.let {

                val addressLine1 = it.addressLine1.value ?: ""
                val addressLine2 = it.addressLine2.value ?: ""
                val city = it.city.value ?: ""
                val state = it.state.value?.value ?: ""
                val country = it.country.value?.label ?: ""
                val postalCode = it.postalCode.value ?: ""
                val fullAddress = buildString {
                    var hasValue = false
                    if (addressLine1.isNotBlank()) {
                        append(addressLine1)
                        hasValue = true
                    }
                    if (addressLine2.isNotBlank()) {
                        if (hasValue) append(", ")
                        append(addressLine2)
                        hasValue = true
                    }
                    if (city.isNotBlank()) {
                        if (hasValue) append(", ")
                        append(city)
                        hasValue = true
                    }
                    if (state.isNotBlank()) {
                        if (hasValue) append(", ")
                        append(state)
                        hasValue = true
                    }
                    if (country.isNotBlank()) {
                        if (hasValue) append(", ")
                        append(country)
                        hasValue = true
                    }
                    if (postalCode.isNotBlank()) {
                        if (hasValue) append(", ")
                        append(postalCode)
                    }
                }

                return fullAddress
            }
        }

        fun getMobileNumberMinLengthForCountry(countryCode: String): Int {

            return 10
        }

        /**
         * Converts a total number of seconds into a string in "MM:SS" format.
         */
        fun formatSecondsToMmSs(seconds: Int): String {
            val minutes = seconds / 60
            val secs = seconds % 60
            return String.format("%02d:%02d", minutes, secs)
        }

//        fun stringToFormType(type: String): SearchTemplateType? {
//            return SearchTemplateType.entries.find { it.type.equals(type, ignoreCase = true) }
//        }

        // Place this in a suitable file, e.g., Extensions.kt

        fun JsonArray?.toStringList(): List<String> {
            return this?.mapNotNull { it?.asString } ?: emptyList()
        }

        fun getZipCodeMaxLength(country: String): Int {
            val countryMaxDigits = mapOf(
                "us" to 9,   // USA zip codes
                "in" to 6,   // India
                "ca" to 6,   // Canada
                "uk" to 8,   // UK
                "de" to 5,   // Germany
                "au" to 4    // Australia
            )
            return countryMaxDigits[country] ?: 10
        }

        private val phoneLengthMap = mapOf(
            "in" to 10,
            "us" to 10,
            "uk" to 11
        )

        fun isValidMobileNumber(phone: String, country: String): Boolean {
            if (!phone.all { it.isDigit() }) return false

            return when (country.lowercase()) {
                "in" -> phone.length == 10 && phone[0] in '6'..'9'
                "us" -> phone.length == 10
                "uk" -> phone.length in 10..11
                else -> phone.length in 7..15
            }
        }

        fun getPhoneNumberMaxLength(country: String?): Int =
            phoneLengthMap[country?.lowercase()] ?: 15

        fun isValidEmail(email: String): Boolean {
            val emailRegex = Regex("^[\\w.+\\-]+@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$")
            return emailRegex.matches(email)
        }

        fun getBirthSex(value: String?): String? {
            if (value == null) return null
            return when (value.lowercase()) {
                "male" -> patFieldMale
                "female" -> patFieldFemale
                "unknown" -> patFieldUnknown
                else -> patFieldOthers
            }
        }

        fun stringToDecimal(sciStr: String?): String? {
            return try {
                // Convert string to BigDecimal, then to plain string
                BigDecimal(sciStr).toPlainString()
            } catch (e: Exception) {
                // Return original string if conversion fails
                sciStr
            }
        }

        //API key values do not change
        fun getSupportCategory(): ArrayList<String> {
            return arrayListOf("Feedback", "Bug", "Feature Request", "Question")
        }

        const val API_KEY_DEMOGRAPHICS_METRICS = "demographicsMetrics"
        const val API_KEY_DEMOGRAPHICS_SETTINGS = "demographicsSettings"
        const val API_KEY_COUNTRIES = "countries"
        const val API_KEY_STATES_INDIA = "statesIndia"
        const val API_KEY_STATES_US = "statesUs"
        const val API_KEY_STATES_AUSTRALIA = "statesAustralia"
        const val API_KEY_FACILITY_LIST = "facilityList"
        const val API_KEY_SETTINGS = "settings"
        val API_KEYS = listOf(
            API_KEY_DEMOGRAPHICS_METRICS,
            API_KEY_DEMOGRAPHICS_SETTINGS,
            API_KEY_COUNTRIES,
            API_KEY_STATES_INDIA,
            API_KEY_STATES_US,
            API_KEY_STATES_AUSTRALIA,
            API_KEY_FACILITY_LIST,
            API_KEY_SETTINGS
        )

        /**
         * Exit time for asking passcode
         */
        const val exitTimeInSeconds: String = "exitTimeInSeconds"
        const val passcodeScreenRoute="com.charm.ehr.kiosk.presentation.navigation.MdRoute.Passcode"
        const val tourScreenRoute="com.charm.ehr.kiosk.presentation.navigation.MdRoute.Tour"
        const val selfCheckInDashboardRoute="com.charm.ehr.kiosk.presentation.navigation.MdRoute.SelfChekInDashboard"

    }
}
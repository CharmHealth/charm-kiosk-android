package com.charm.ehr.kiosk.domain

import UiText
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.charm.ehr.kiosk.common.util.ModelUtil
import com.charm.ehr.kiosk.presentation.edit_profile.Insurance
import com.google.gson.JsonArray
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import org.joda.time.DateTime

data class APIResponseError(
    var charmApiCode: Int?,
    var httpStatusCode: Int?,
    var errorMsg: UiText?
)



data class APIResponseErrorOld
    (
    var charmApiCode: Int?,
    var httpStatusCode: Int?,
    var errorMsg: String?
)

//@Serializable
//data class PatientSimplePOJO(
//    var practicePatientId: String? = null,
//    var hasImage: Boolean = false,
//    var fullName: String? = null,
//    var firstName: String? = null,
//    var lastName: String? = null,
//    var patientRecordId: String? = null,
//    var dateOfBirth: String? = null,
//    var age: Int? = null
//)

data class Patient
    (
    var practicePatientId: String? = null,
    var fullName: String? = null,
    var firstName: String? = null,
    var middleName: String? = null,
    var lastName: String? = null,
    var patientRecordId: String? = null,
    var createdByPractice: Boolean? = true,
    var gender: String? = null,//allowed values - male, female
    var pronouns: String? = null,
    var dateOfBirth: DateTime? = null,
    var age: Int? = null,
    var bloodGroup: String? = null,
    var dateOfJoining: DateTime? = null,
    var smokingStatus: String? = null,
    var language: String? = null,
    var maritalStatus: String? = null,
    var ssn: String? = null,
    var emergencyContactName: String? = null,
    var emergencyContactNumber: String? = null,
    var emergencyContactExtn: String? = null,
    var isActive: Boolean = true,
    var localUpdateDateTime: DateTime? = null,
    var idIssuingJurisdiction: String? = null,
    var idQualifier: Int? = null,
    var idOfPatient: String? = null,
    var photoURI: String? = null,
    var isPhotoAvailableOnline: Boolean = false,

    var primaryCareProvider: String? = null,
    var secondaryCareProvider: String? = null,

    var caregiverFirstName: String? = null,
    var caregiverLastName: String? = null,
    var contactDetails: ContactDetails? = null,
    var postalAddress: PostalAddress? = null,
    var genderIdentity: String? = null,
    var nickName: String? = null,
    var suffix: String? = null,
    var maidenName: String? = null,
    var deceased: Boolean? = null,
    var dod: String? = null,
    var causeOfDeath: String? = null,
    var idQualifiers: JsonArray = JsonArray(),
    var caregivers: List<Caregiver> = listOf(),
    var guarantor: Guarantor? = null,
    var emailNotification: Boolean? = null,
    var textNotification: Boolean? = null,
    var voiceNotification: Boolean? = null,
    var race: String? = null,
    var ethnicity: String? = null,
    var employmentStatus: String? = null,
    var sexualOrientation: String? = null,
    var motherFirstName: String? = null,
    var motherLastName: String? = null,
    var birthOrder: String? = null,
    var categories: JsonArray = JsonArray(),

    var introduction: String? = null,
    var customField1: String? = null,
    var customField2: String? = null,
    var customField3: String? = null,
    var customField4: String? = null,
    var customField5: String? = null,
    var sourceName: String? = null,
    var sourceValue: String? = null,
    var paymentSource: String? = null,
    var paymentStartDate: String? = null,
    var paymentEndDate: String? = null,
    var externalId: String? = null,

    var facilityListForEdit: JsonArray = JsonArray(),

    var patientImgFilePath: String? = null,
    var isPatientUpdated: Boolean = false,

    var isPatientImgToBeDeleted: Boolean = false,
    var primaryInsurance: Insurance? = null,
    var secondaryInsurance: Insurance? = null,
)

data class ContactDetails(
    //
    var contactDetailsId: String? = null,
    var preferredCommunication: String? = null,
    var emailId: String? = null,
    var landline: String? = null,
    var mobile: String? = null,
    var fax: String? = null,
    var webUrl: String? = null,
    var workPhone: String? = null,
    var workPhoneExtn: String? = null,
    var contactPreference: Char? = null,

    var homePhone: String? = null,
    var primaryPhone: String? = null,
    var mobilePhone: String? = null

)

data class PostalAddress(
    //
    var postalAddressId: String? = null,
    var line1: String? = null,
    var line2: String? = null,
    var area: String? = null,
    var city: String? = null,
    var district: String? = null,
    var state: String? = null,
    var county: String? = null,
    var country: String? = null,
    var postalCode: String? = null,
    var postbox: String? = null
) {
    override fun toString(): String {
        return listOfNotNull(line1, line2, area, city, state, country?.uppercase(), postalCode)
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }
}

//Dm = Demographics Metrics

@Serializable
data class DmLanguages(
    val value: String,
    val key: String
)

@Serializable
data class DmRace(
    val value: String,
    val key: String
)

@Serializable
data class DmEthnicity(
    val value: String = "",
    val key: String = ""
)

@Serializable
data class DmGenderIdentity(
    val value: String,
    val key: String
)

@Serializable
data class DmCaregiverRelationship(
    val value: String,
    val key: String
)

@Serializable
data class DmSexualOrientation(
    val value: String,
    val key: String
)

@Serializable
data class DmCommonQualifier(
    val idQualifier: String,
    val qualifierName: String
)

//data class DmPaymentSource(
//    val value: String,
//    val key: String
//)


@Serializable
data class DemographicsMetrics(
    val smokingStatus: List<String> = emptyList(),
    val guarantorRelationship: List<String> = emptyList(),
    val languages: List<DmLanguages> = emptyList(),
    val race: List<DmRace> = emptyList(),
    val ethnicity: List<DmEthnicity> = emptyList(),
    val birthSex: List<String> = emptyList(),
    val genderIdentity: List<DmGenderIdentity> = emptyList(),
    val employmentStatus: List<String> = emptyList(),
    val caregiverRelationship: List<DmCaregiverRelationship> = emptyList(),
    val sexualOrientation: List<DmSexualOrientation> = emptyList(),
    val maritalStatus: List<String> = emptyList(),
    val commonQualifier: List<DmCommonQualifier> = emptyList(),
    val bloodGroup: List<String> = emptyList(),
    val preferredCommunication: List<String> = emptyList(),
    // val paymentSource: List<String> // Uncomment if needed
)

@Serializable
data class Country(
    val label: String? = null,
    val value: String? = null,
)

@Serializable
data class State(
    val label: String? = null,
    val value: String? = null,
)

@Serializable
data class DemographicsSetting(
    val label: String? = null,
    val nodeIdentifier: String? = null,
    val type: String? = null,
    val mandatory: Boolean? = null
)

data class Facility(
    //
    var facilityId: String? = null,
    var facilityName: String? = null,
    var facilityCode: String? = null,
    var npi: String? = null,
    var postalAddress: PostalAddress? = null,
    var contactDetails: ContactDetails? = null,
    var qualifierId: String? = null,
    var nonNpi: String? = null,
    var facilityLogo: String? = null,
    var status: String = "Enabled",
    var gstin_num: String? = null,
    var facilityTypeCode: Char? = null,
    var timezone: String? = null
)

data class Member(
    //
    var memberId: String? = null,
    var fullName: String? = null,
    var firstName: String? = null,
    var middleName: String? = null,
    var lastName: String? = null,
    var deaNumber: String? = null,
    var degree: String? = null,
    var specialization: String? = null,
    var gender: String? = null,//Allowed values male, female
    var dateOfBirth: DateTime? = null,
    var bloodGroup: String? = null,
    var postalAddress: PostalAddress? = null,
    var contactDetails: ContactDetails? = null,
    var prefix: String? = null,
    var npi: String? = null,
    var memberStatus: String? = "Enabled",
    var createdDate: DateTime? = null,
    var idQualifier: Long? = null,
    var id: String? = null,
    var ssn: String? = null,
    var photoURI: String? = null,
    var isPhotoAvailable: Boolean? = null,
    var isMemberOnlineAppEnabled: Boolean? = null,
    var introduction: String? = null,
    var memberOnlineAppTiming: String? = null,
    var memberAvailability: ArrayList<MemberAvailability> = ArrayList(),
    var memberWorkTiming: ArrayList<MemberWorkTiming> = ArrayList(),

    var isSelected: Boolean = false
)

data class Service(
    var serviceId: String? = null,
    var logo: String? = null,
    var serviceURL: String? = null,
    var serviceLabel: String? = null,
    var isDepartment: Boolean = false
)

data class MemberWorkTiming(
    var memberTimingId: String? = null,
    var memberId: String? = null,
    var facilityId: String? = null,
    var dayOfWeek: Int? = null,
    var isVisiting: Boolean? = null,
    var visitNo: String? = null,
    var startTime: String? = null,
    var startSession: String? = null,
    var endTime: String? = null,
    var endSession: String? = null,
    var onlineAppSlot: Boolean? = null,
    var tokenCount: Int? = null,
    var startDateTime: DateTime? = null,
    var endDateTime: DateTime? = null
)

data class MemberAvailability(

    var startTime: DateTime? = null,
    var endTime: DateTime? = null

)

data class Appointment(
    //
    var appointmentId: String? = null,
    var appointmentDate: DateTime? = null,
    var appointmentStartTime: DateTime? = null,
    var appointmentEndTime: DateTime? = null,
    var timezone: String? = null,
    var appointmentType: String? = null,
    var statusId: Long? = null,
    var reminderType: String? = null,
    var physicianRemark: String? = null,
    var reason: String? = null,
    var physicianId: String? = null,
    var facilityId: String? = null,
    var modifiedDate: DateTime? = null,
    var lastModifiedTime: DateTime? = null,
    //
    var member: Member? = null,
    var patient: Patient? = null,

    var appointmentStatus: AppointmentStatus? = null,

    var durationInMinutes: Int? = null,

    var tokenNo: Int = -1,

    var receiptId: String? = null

)

data class AppointmentStatus(

    //TODO:clean
    var statusId: String? = null,// "status_id": "100001000000000131",
    var status: String? = null, //  "status": "Confirmed",
    var statusColor: String? = null, //"status_color": "#008000:#ffffff",
    var isSystemGenerated: Boolean? = null, // "is_system_generated": "1",
    var isDeleted: Boolean? = null, //"is_deleted": "0",
    var position: Int? = null, // "position": "0",
    var isDefaultStatus: Boolean? = null //"is_default_status": "0"

)

data class Department(

    //TODO:clean
    var departmentId: String? = null, // "department_id": 100001000000000189,
    var departmentName: String? = null, //"department_name": "dep1",
    var facilityId: String? = null,//"facility_id": 100001000000000107,
    var memberName: String? = null, //"member_name": "Dr. Paul Doctor, Dr. tik tik",
    var facilityName: String? = null //"facility_name": "charmadmin"
)

data class PrinterData(

    var facilityName: String? = "",
    var city: String? = "",
    var appointmentDate: String? = null,
    var token: Int? = null,
    var memberSpeciality: String? = "",
    var memberName: String? = "",
    var patientName: String? = "",
    var isSelfCheckin: Boolean = false

)

data class IntakeQuestTemplateEntry(

    var entryId: String = "",
    var templateId: String = "",
    var notes: String? = null,
    var notesType: String? = null,
    var isMultiChoice: Boolean = false,
    var fromScale: Int? = null,
    var ratingDescription: ArrayList<String> = arrayListOf(),
    var toScale: Int? = null,
    var position: Int? = null,
    var isDeleted: Boolean = false,
    var isMandatory: Boolean = false,
    var answersList: ArrayList<String>? = arrayListOf()
)

data class IntakeQuestTemplateOption(
    var optionId: String = "",
    var entryId: String = "",
    var option: String? = null
)

data class IntakeQuestTemplateLabels(
    var labelId: String = "",
    var entryId: String = "",
    var fontSize: Int = 14,
    var fontStyle: String = "",
    var fontWeight: String = "",
    var textAlign: String = "",
    var textDecoration: String = ""

)

data class FeedbackPOJO(
    var feedbackEntry: IntakeQuestTemplateEntry,
    var feedbackOptions: List<IntakeQuestTemplateOption>,
    var feedbackLabel: List<IntakeQuestTemplateLabels> = listOf(),
    var patientId: String? = null,
    var providerId: String? = null,
    var departmentId: String? = null
)

data class Caregiver(
    var firstName: String? = null,
    var middleName: String? = null,
    var lastName: String? = null,
    var gender: String? = null,
    var dateOfBirth: String? = null,
    var ssn: String? = null,
    var relationship: String? = null,
    var sameAsPatContact: Boolean? = null,
    var contactDetails: ContactDetails? = null,
    var postalAddress: PostalAddress? = null,
    var type: Int? = null
)

data class Guarantor(
    var firstName: String? = null,
    var middleName: String? = null,
    var lastName: String? = null,
    var gender: String? = null,
    var dateOfBirth: String? = null,
    var ssn: String? = null,
    var relationship: String? = null,
    var contactDetails: ContactDetails? = null,
    var postalAddress: PostalAddress? = null
)

data class PatientRegistrationSettings(

    var fieldName: String? = null,
    var isShown: Boolean = true,
    var isMandatory: Boolean = false,
    var fieldViewId: List<Int>? = null,
    var fieldViewLayoutId: List<Int>? = null
)

data class KioskSettings(
    var facility: Facility? = null,
    var selectedLanguages: ArrayList<String>? = null,
    var dashboardModules: ArrayList<String>? = null,
    var selectedDepartments: ArrayList<Department>? = null,
    var feedbackQuestionnaireIdList: ArrayList<FeedbackLanguageQuestionnaire> = arrayListOf(),
    var servicesList: ArrayList<Service> = arrayListOf(),
    var checkinStatusId: String? = null,
    var selectedDepartmentId: String? = null,
    var merchantAccountId: String? = null,

    var accountDetails: AccountDetails? = null,
    var country: String? = null,
    var isTeleFillConsentForm: Boolean = false,
    var isSMSEnabled: Boolean = false,
    var isPremiumUser: Boolean = false,

    //accessingLocationDetails
    var mobileCode: String? = null, //like +91
    var accessLocationCountryCode: String? = null,

    var defaultGender: String? = null

)

data class AccountDetails(
    val loginId: String = "",
    val accountType: String = "",
    val isTelehealthCheckin: Boolean = false,
    val practiceId: String = "",
    val kioskType: String = "",
    var kioskPreferences: KioskPreferences? = null,

    )

data class KioskPreferences(
    val isTelehealthCheckIn: Boolean = false,
    val insuranceRequired: Boolean = false,
    val patientDemographicsEditRequired: Boolean = false,
    val questionnairesRequired: Boolean = false,
    val consentFormsRequired: Boolean = false,
    var appointmentStatus: AppointmentStatus? = null
)

data class AppointmentSettings(

    var appointmentConfigId: String? = null,
    var providerId: String? = null,
    var facilityId: String? = null,
    var appointmentSlotNewPat: String? = null,
    var appointmentSlotOldPat: String? = null,
    var appApprovalPreferences: String? = null,
    var isEnabled: Boolean? = null,
    var visitTypeIds: String? = null,
    var showVisitType: Boolean? = null,
    var isCardProcessingEnabled: Boolean? = null,
    var cardProcessingType: String? = null,
    var chargeType: String? = null,
    var chargeAmount: Float? = null,
    var patientNote: String? = null,
    var allowAddWaitList: Boolean? = null,
    var isTokenBased: Boolean? = null
)

data class FeedbackLanguageQuestionnaire(

    var language: String = ModelUtil.langEnglish,
    var questionnaireId: String? = null,
    var questionnaireLabel: String? = null
)

data class ExistingPatModule(

    @StringRes var moduleName: Int = 0,
    @ColorRes var bgColor: Int = 0,
    var moduleCode: Int = 0
)

data class PaymentTransaction(

    var patientId: String? = null,
    var facilityId: String? = null,
    var memberId: String? = null,
    var member: Member? = null,
    var merchantAccountId: String? = null,
    var paymentAmount: Float = 0F,
    var receiptDate: DateTime? = null,
    var transactionId: String? = null,
    var razorPayOrderId: String? = null,
    var razorPayPaymentId: String? = null,
    var clientId: String? = null,
    var publicToken: String? = null

)

data class DataWithBoolean(

    var key: String = "",
    var value: Boolean = false
)

data class MDFile(
    var responseBody: ResponseBody? = null,
    var fileName: String? = null,
    var extension: String? = null
)

data class PatientQuesMap(
    var patientQuesMapId: String? = null,
    var appointmentId: String? = null,
    var practicePatientId: String? = null,
    var templateId: String? = null,
    var isFilled: Boolean = false,
    var allowPhrEditing: Boolean = false,
    var isSubmitted: Boolean = false,
    var memberId: String? = null,
    var facilityId: String? = null,
    var providerId: String? = null,
    var departmentId: String? = null,
    var lastModifiedTime: DateTime? = null,
    var timeOfSharing: String? = null,
    var randomKey: String? = null,
    var appointment: Appointment? = null
)

data class QuestionnaireEntryPOJO(

    var questionnaireEntry: IntakeQuestTemplateEntry = IntakeQuestTemplateEntry(),
    var questionnaireOptions: ArrayList<IntakeQuestTemplateOption> = arrayListOf(),
    var questionnaireLabel: IntakeQuestTemplateLabels = IntakeQuestTemplateLabels(),
    var questionnaireSignatureFilePath: String? = null,
    var signatureUrl: String? = null, // temp added for gramin make arraylist

//    var questionnaireAllergyList: ArrayList<IntakeQuestAllergyEntry> = arrayListOf(),
//    var questionnaireMedicationList: ArrayList<IntakeQuestMedicationEntry> = arrayListOf(),
//    var questionnaireSupplementList: ArrayList<IntakeQuestSupplementEntry> = arrayListOf(),
//    var isNoKnownDrugAllergies: Boolean = false,
//    var isNoKnownEnvironmentalAllergies: Boolean = false,
//    var isNoKnownFoodAllergies: Boolean = false,
//    var isNoActiveMedications: Boolean = false,
//    var soapWidget: SOAPWidgetPOJO = SOAPWidgetPOJO()

)

data class QuestionnaireDetailPOJO(

    var questionnaireEntryPOJOList: ArrayList<QuestionnaireEntryPOJO> = arrayListOf(),
    var patientId: String? = null,
    var departmentId: String? = null,
    var patientQuesMap: PatientQuesMap? = null

)


data class QuestionnairePhysicianTemplate(

    var templateId: String? = null,
    var memberId: String? = null,
    var name: String? = null,
    var type: String? = null,
    var timeOfCreation: String? = null,
    var comments: String? = null,
    var tags: String? = null,
    var isPracticeTemplate: Boolean? = null,
    var isDeleted: Boolean? = null,
    var isEditable: Boolean? = null,
    var customFunction: String? = null,
    var layout: String? = null,
    var patientQuesMap: PatientQuesMap? = null,
    var member: Member? = null,
    var practiceId: String? = null,
    var toString: String = ""

)

data class ConsentForm(
    var fileSize: String? = null,
    var fileId: String? = null,
    var fileType: String? = null,
    var fileName: String? = null,
    var uploadedBy: String? = null,
    //For DB
    var fileLocation: String? = null,
    var timeOfUpload: String? = null,
    var isDeleted: Boolean = false,
    var uploadedDate: String? = null
)

data class QuestionnaireMetaData(
    val questionnaireType: String?,
    val templateName: String?,
    var isSubmitted: Boolean,
    val patientId: String?,
    val questions: List<Question>,
    val templateId: String?,
    val quesMapId: String?,
    val unSupportedQuestionnaireType: List<QuestionnaireType> = listOf()
)

enum class QuestionnaireType(val key: String) {
    Label("Label"),
    QuestionWithOptions("Question with Options"),
    QuestionWithFixedOptions("Question with Fixed Options"),
    YesOrNoQuestion("Yes/No Question"),
    SimpleQuestion("Simple Question"),
    Question("Question"),
    Date("Date"),
    Signature("Signature"),
    RatingScale("Rating Scale"),
    Allergies("Allergies"),
    Medications("Medications"),
    Supplements("Supplements"),
    ChiefComplaints("Chief Complaints"),
    Symptoms("Symptoms"),
    HistoryOfPresentIllness("History of Present Illness"),
    PastMedicalHistory("Past Medical History"),
    FamilyHistory("Family History"),
    SocialHistory("Social History"),
    ReviewOfSystems("Review of Systems"),
    Section("Section"),
    PersonalDetails("Personal Details"),
    PrimaryContactDetails("Primary Contact Details"),
    PrimaryInsuranceDetails("Primary Insurance Details"),
}

enum class SOAPEntryType(val type: String) {
    Header("Header"),
    SimpleQuestion("Simple Question"),
    YesOrNoQuestion("Yes/No Question"),
    RatingScale("Rating Scale"),
    TextBox("Text Box"),
    CheckList("Check List"),
    CheckBox("Checkbox"),
    Radio("Radio"),
    Table("Table"),
    Image("Image");
}

data class SOAPTemplateTableCell(
    var type: String = "",
    var value: String = "",
    var colspan: String = ""
)

sealed class SOAPEntry {
    abstract val soapEntryId: String?
    abstract val soapEntryName: String?
    abstract val soapEntryType: SOAPEntryType
    abstract val isMandatory: Boolean
    abstract val answer: String?

    data class HeadingEntry(
        override val soapEntryId: String?,
        override val soapEntryName: String?,
        override val soapEntryType: SOAPEntryType = SOAPEntryType.Header,
        override val isMandatory: Boolean,
        override val answer: String?,
        val fontWeight: String? = null,
        val textDecoration: String? = null,
        val fontSize: Int? = null,
        val fontStyle: String? = null,
        val textAlign: String? = null
    ) : SOAPEntry()

    data class SimpleQuestionEntry(
        override val soapEntryId: String?,
        override val soapEntryName: String?,
        override val soapEntryType: SOAPEntryType = SOAPEntryType.SimpleQuestion,
        override val isMandatory: Boolean,
        override val answer: String?,
        val defaultValue: String? = null,
        val isMultiLine: Boolean = false,
    ) : SOAPEntry()

    data class YesOrNoEntry(
        override val soapEntryId: String?,
        override val soapEntryName: String?,
        override val soapEntryType: SOAPEntryType = SOAPEntryType.Radio,
        override val isMandatory: Boolean,
        override var answer: String?,
        val additionalComment: String? = null,
        val includeOthers: String? = null,
        val defaultValue: String? = null,
        var optionList: List<OptionData> = ArrayList(),
        var isAdditionalComments: Boolean = false,
        var isIncludeOthers: Boolean = false,
        var updatedValue: Boolean = false,
        var updatedOption: OptionData? = null,
        var updatedOtherValue: String? = null,
    ) : SOAPEntry()

    data class RatingScaleEntry(
        override val soapEntryId: String?,
        override val soapEntryName: String?,
        override val soapEntryType: SOAPEntryType = SOAPEntryType.TextBox,
        override val isMandatory: Boolean,
        override val answer: String?,
        val defaultValue: String? = null,
        var fromScale: Int = 0,
        var toScale: Int = 10,
    ) : SOAPEntry()

    data class TextBoxEntry(
        override val soapEntryId: String?,
        override val soapEntryName: String?,
        override val soapEntryType: SOAPEntryType = SOAPEntryType.TextBox,
        override val isMandatory: Boolean,
        override val answer: String?,
        val defaultValue: String? = null,
    ) : SOAPEntry()


    data class CheckboxEntry(
        override val soapEntryId: String?,
        override val soapEntryName: String?,
        override val soapEntryType: SOAPEntryType = SOAPEntryType.CheckBox,
        override val isMandatory: Boolean,
        override var answer: String?,
        val additionalComment: String? = null,
        val includeOthers: String? = null,
        val defaultValue: String? = null,
        var optionList: List<OptionData> = ArrayList(),
        var isAdditionalComments: Boolean = false,
        var isIncludeOthers: Boolean = false,
        var updatedValue: Boolean = false,
        var updatedOption: OptionData? = null,
        val updatedCommentValue: String? = null
    ) : SOAPEntry()

    data class RadioEntry(
        override val soapEntryId: String?,
        override val soapEntryName: String?,
        override val soapEntryType: SOAPEntryType = SOAPEntryType.Radio,
        override val isMandatory: Boolean,
        override var answer: String?,
        val additionalComment: String? = null,
        val includeOthers: String? = null,
        val defaultValue: String? = null,
        var optionList: List<OptionData> = ArrayList(),
        var isAdditionalComments: Boolean = false,
        var isIncludeOthers: Boolean = false,
        var updatedValue: Boolean = false,
        var updatedOption: OptionData? = null,
        var updatedOtherValue: String? = null,
        var updatedChildrenList: List<OptionData>? = ArrayList(),
        val updatedCommentValue: String? = null
    ) : SOAPEntry()

    data class TableEntry(
        override val soapEntryId: String?,
        override val soapEntryName: String?,
        override val soapEntryType: SOAPEntryType = SOAPEntryType.Table,
        override val isMandatory: Boolean,
        override var answer: String?,
        val defaultValue: String? = null,
        var soapWidgetTableData: ArrayList<ArrayList<SOAPTemplateTableCell>> = ArrayList(),
        var rowIndex: Int = -1,
        var colIndex: Int = -1
    ) : SOAPEntry()

    data class ImageEntry(
        override val soapEntryId: String?,
        override val soapEntryName: String?,
        override val soapEntryType: SOAPEntryType = SOAPEntryType.Table,
        override val isMandatory: Boolean,
        override val answer: String?,
    ) : SOAPEntry()


}

data class SOAPWidget(
    val soapWidgetId: String?,
    val soapTemplateId: String?,
    val soapWidgetName: String?,
    val soapWidgetModule: String?,
    val entries: List<SOAPEntry>
)

sealed class Question(
    open val entryId: String?,
    open val notes: String?,
    open val notesType: String?,
    open val isDeleted: Boolean,
    open val isMandatory: Boolean,
    open val answer: String?,
    open val errorMessage: String?
) {
    data class SimpleQuestion(
        override val entryId: String?,
        override val notes: String?,
        override val notesType: String?,
        override val isDeleted: Boolean = false,
        override val isMandatory: Boolean,
        override var answer: String? = null,
        override var errorMessage: String? = null,
        val isMultiLine: Boolean = false,

        ) : Question(entryId, notes, notesType, isDeleted, isMandatory, answer, errorMessage)

    data class QuestionWithOptions(
        override val entryId: String?,
        override val notes: String?,
        override val notesType: String?,
        override val isDeleted: Boolean = false,
        override val isMandatory: Boolean,
        override var answer: String? = null,
        override var errorMessage: String? = null,
        val isMultiChoice: Boolean = false,
        val options: List<OptionData> = listOf(),
        var multipleAnswer: List<String> = listOf(),
        var updatedValue: Boolean = false,
        var updatedOption: OptionData? = null,
        var updatedOtherValue: String? = null,
    ) : Question(entryId, notes, notesType, isDeleted, isMandatory, answer, errorMessage)

    data class RatingScaleQuestion(
        override val entryId: String?,
        override val notes: String?,
        override val notesType: String?,
        override val isDeleted: Boolean = false,
        override val isMandatory: Boolean,
        override val answer: String? = null,
        override var errorMessage: String? = null,
        val fromScale: Int,
        val toScale: Int,
    ) : Question(entryId, notes, notesType, isDeleted, isMandatory, answer, errorMessage)

    data class YesOrNoQuestion(
        override val entryId: String?,
        override val notes: String?,
        override val notesType: String?,
        override val isDeleted: Boolean = false,
        override val isMandatory: Boolean,
        override var answer: String? = null,
        override var errorMessage: String? = null,
        val options: List<OptionData> = listOf(),
        var updatedValue: Boolean = false,
        var updatedOption: OptionData? = null,
        var updatedOtherValue: String? = null,
    ) : Question(entryId, notes, notesType, isDeleted, isMandatory, answer, errorMessage)

    data class Label(
        override val entryId: String?,
        override val notes: String?,
        override val notesType: String?,
        override val isDeleted: Boolean = false,
        override val isMandatory: Boolean,
        override val answer: String? = null,
        override var errorMessage: String? = null,
        val fontWeight: String? = null,
        val textDecoration: String? = null,
        val fontSize: Int? = null,
        val fontStyle: String? = null,
        val textAlign: String? = null
    ) : Question(entryId, notes, notesType, isDeleted, isMandatory, answer, errorMessage)

    data class DateQuestion(
        override val entryId: String?,
        override val notes: String?,
        override val notesType: String?,
        override val isDeleted: Boolean = false,
        override val isMandatory: Boolean,
        override val answer: String? = null,
        override var errorMessage: String? = null,
        val defaultValue: String? = null,
    ) : Question(entryId, notes, notesType, isDeleted, isMandatory, answer, errorMessage)

    data class SignatureQuestion(
        override val entryId: String?,
        override val notes: String?,
        override val notesType: String?,
        override val isDeleted: Boolean = false,
        override val isMandatory: Boolean,
        override val answer: String? = null,
        override var errorMessage: String? = null,
        var signatureUrl: String? = null,
        var isLoading: Boolean = false,
        var isError: Boolean = false,
        var signatureFilePath: String? = null,
        var isSignatureChanged: Boolean = false
    ) : Question(entryId, notes, notesType, isDeleted, isMandatory, answer, errorMessage)


    data class Section(
        override val entryId: String?,
        override val notes: String?,
        override val notesType: String?,
        override val isDeleted: Boolean = false,
        override val isMandatory: Boolean,
        override val answer: String? = null,
        override var errorMessage: String? = null,
        val isCollapsible: Boolean = false,
        val expandByDefault: Boolean = false,
        val questions: List<Question> = listOf()
    ) : Question(entryId, notes, notesType, isDeleted, isMandatory, answer, errorMessage)

    data class SOAPWidgetQuestion(
        override val entryId: String?,
        override val notes: String?,
        override val notesType: String?,
        override val isDeleted: Boolean = false,
        override val isMandatory: Boolean,
        override val answer: String? = null,
        override var errorMessage: String? = null,
        val soapWidget: SOAPWidget
    ) : Question(entryId, notes, notesType, isDeleted, isMandatory, answer, errorMessage)
}


data class OptionData(
    var optionString: String? = null,
    var isChecked: Boolean = false,
    var childrenOptionList: List<OptionData>? = ArrayList(),
    var viewLevel: Int = 0,
    var isOther: Boolean = false,
    var otherValue: String? = null
)


data class PracticePayer(
    var practicePayerId: String? = null,
    var payerId: String? = null,
    var payerName: String? = null,
    var country: String? = null,
    var state: String? = null,
    var city: String? = null,
    var line1: String? = null,
    var postboxNo: String? = null,
    var zipCode: String? = null,
    var workPhone: String? = null,
    var fax: String? = null,
    var patientsUsageCount: Int? = null,
    var isActive: Boolean? = null,
    var isVerified: Boolean? = null
)


data class DateInputResult(
    val formatted: String,
    val isInputError: Boolean,
    val isMinDateError: Boolean,
    val isMaxDateError: Boolean
)
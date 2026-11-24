package com.charm.ehr.kiosk.presentation.edit_profile

import com.charm.ehr.kiosk.domain.Country
import com.charm.ehr.kiosk.domain.DmCaregiverRelationship
import com.charm.ehr.kiosk.domain.DmEthnicity
import com.charm.ehr.kiosk.domain.DmGenderIdentity
import com.charm.ehr.kiosk.domain.DmLanguages
import com.charm.ehr.kiosk.domain.DmRace
import com.charm.ehr.kiosk.domain.PracticePayer
import com.charm.ehr.kiosk.domain.State
import com.charm.ehr.kiosk.presentation.patient_profile.Section


// Unified action definitions
sealed interface EditProfileAction {

    //Navigation Events
    data class OnSectionChange(val section: Section) : EditProfileAction
    data class OnClickSave(val section: Section) : EditProfileAction
    data class OnClickNext(val fromSection: Section) : EditProfileAction
    data class OnClickPrevious(val fromSection: Section) : EditProfileAction
    data class OnClickSkip(val fromSection: Section) : EditProfileAction

    data object ClearFocusedPersonalField : EditProfileAction
    data object ClearFocusedContactField : EditProfileAction
    data object ClearFocusedPCaregiverField : EditProfileAction
    data object ClearFocusedSCaregiverField : EditProfileAction
    data object ClearFocusedInsuranceField: EditProfileAction

    data object OnClickDialogSave : EditProfileAction

    //Value update events
    data class OnUpdatePersonalInformation(
        val firstName: String? = null,
        val lastName: String? = null,
        val dateOfBirth: String? = null,
        val birthSex: String? = null,
        val genderIdentity: DmGenderIdentity? = null,
        val bloodGroup: String? = null,
        val race: List<DmRace>? = null,
        val ethnicity: List<DmEthnicity>? = null,
        val language: DmLanguages? = null,
        val smokingStatus: String? = null,
        val maritalStatus: String? = null,
        val employmentStatus: String? = null,
    ) : EditProfileAction

    data class OnUpdateContactInformation(
        val preferredCommunication: String? = null,
        val email: String? = null,
        val primaryPhone: String? = null,

        val mobilePhone: String? = null,
        val homePhone: String? = null,
        val workPhone: String? = null,
        val workPhoneExtn: String? = null,
        val addressLine1: String? = null,
        val addressLine2: String? = null,
        val city: String? = null,
        val state: State? = null,
        val country: Country? = null,
        val postalCode: String? = null,
        val emergencyContactName: String? = null,
        val emergencyContactNumber: String? = null,
        val emergencyContactExtn: String? = null,
        val emailError: String? = null,
    ) : EditProfileAction

    data class OnUpdatePrimaryInsurance(
        val planOrProgram: String? = null,
        val planId: String? = null,
        val insuranceId: String? = null,
        val insuranceType: String? = null,
        val policyGroup: String? = null,
        val selectedPayer: PracticePayer? = null,
        val copay: String? = null,
        val deductible: String? = null,
        val coinsurance: String? = null,
        val validFrom: String? = null,
        val validUntil: String? = null,
        val employerOrSchoolName: String? = null,
        val comment: String? = null,
        val insuranceFrontSide: String? = null,
        val insuranceBackSide: String? = null,

        //Personal details of the insured person
        val relationship: String? = null,
        val firstName: String? = null,
        val lastName: String? = null,
        val dateOfBirth: String? = null,
        val birthSex: String? = null,
        val isCopyPatientAddress: Boolean? = null,
        val addressLine1: String? = null,
        val addressLine2: String? = null,
        val city: String? = null,
        val state: State? = null,
        val country: Country? = null,
        val postalCode: String? = null,
        val mobilePhone: String? = null,
        val homePhone: String? = null
    ) : EditProfileAction

    data class OnUpdateSecondaryInsurance(
        val planOrProgram: String? = null,
        val planId: String? = null,
        val insuranceId: String? = null,
        val insuranceType: String? = null,
        val policyGroup: String? = null,
        val selectedPayer: PracticePayer? = null,
        val copay: String? = null,
        val deductible: String? = null,
        val coinsurance: String? = null,
        val validFrom: String? = null,
        val validUntil: String? = null,
        val employerOrSchoolName: String? = null,
        val comment: String? = null,
        val insuranceFrontSide: String? = null,
        val insuranceBackSide: String? = null,

        //Personal details of the insured person
        val relationship: String? = null,
        val firstName: String? = null,
        val lastName: String? = null,
        val dateOfBirth: String? = null,
        val birthSex: String? = null,
        val isCopyPatientAddress: Boolean? = null,
        val addressLine1: String? = null,
        val addressLine2: String? = null,
        val city: String? = null,
        val state: State? = null,
        val country: Country? = null,
        val postalCode: String? = null,
        val mobilePhone: String? = null,
        val homePhone: String? = null
    ) : EditProfileAction

    data class OnUpdatePrimaryCaregiverDetails(
        val relationship: DmCaregiverRelationship? = null,
        val firstName: String? = null,
        val lastName: String? = null,
        val dateOfBirth: String? = null,
        val birthSex: String? = null,
        val isCopyPatientAddress: Boolean? = null,
        val addressLine1: String? = null,
        val addressLine2: String? = null,
        val city: String? = null,
        val state: State? = null,
        val country: Country? = null,
        val postalCode: String? = null,
        val mobilePhone: String? = null,
        val homePhone: String? = null,
        val stateList: List<State>? = null
    ) : EditProfileAction

    data class OnUpdateSecondaryCaregiverDetails(
        val relationship: DmCaregiverRelationship? = null,
        val firstName: String? = null,
        val lastName: String? = null,
        val dateOfBirth: String? = null,
        val birthSex: String? = null,
        val isCopyPatientAddress: Boolean? = null,
        val addressLine1: String? = null,
        val addressLine2: String? = null,
        val city: String? = null,
        val state: State? = null,
        val country: Country? = null,
        val postalCode: String? = null,
        val mobilePhone: String? = null,
        val homePhone: String? = null,
        val stateList: List<State>? = null
    ) : EditProfileAction

    //Handling state based on country selection
    data class OnRequestStatesContactInfo(val country: Country?) : EditProfileAction
    data class OnRequestStatesPrimaryCaregiver(val country: Country?) : EditProfileAction
    data class OnRequestStatesSecondaryCaregiver(val country: Country?) : EditProfileAction
    data class OnRequestStatesPrimaryInsurance(val country: Country?) : EditProfileAction
    data class OnRequestStatesSecondaryInsurance(val country: Country?) : EditProfileAction

    //Payer Search - Insurance
    data class OnSearchPayer(val query: String, val isPrimary: Boolean) : EditProfileAction

    //Caregiver actions
    object OnClickAddSecondaryCaregiver : EditProfileAction
    object OnDeleteSecondaryCaregiver : EditProfileAction
    object OnDeletePrimaryCaregiver : EditProfileAction

    //Insurance Card Actions
    object DeleteInsuranceCardFront : EditProfileAction
    object DeleteInsuranceCardBack : EditProfileAction
    object OnClickInsuranceCardFront : EditProfileAction
    object OnRetryInsuranceCardFront : EditProfileAction
    object OnClickInsuranceCardBack : EditProfileAction
    object OnRetryInsuranceCardBack : EditProfileAction
    object OnDeleteInsuranceCardCacheBack : EditProfileAction
    object OnDeleteInsuranceCardCacheFront : EditProfileAction

    //Profile Picture Actions
    object OnClickProfilePicture : EditProfileAction

    //Back Press Action
    object OnBackPress : EditProfileAction

    //Dialog and Error Handling
    object DismissDiscardDialog : EditProfileAction
    object ClearToastMessage : EditProfileAction
    object ClearError : EditProfileAction
    object RetryLastFailedAction : EditProfileAction

}
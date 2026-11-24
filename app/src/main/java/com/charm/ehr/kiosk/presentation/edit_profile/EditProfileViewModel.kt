package com.charm.ehr.kiosk.presentation.edit_profile


import UiText.DynamicString
import UiText.StrResId
import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charm.ehr.kiosk.domain.Caregiver
import com.charm.ehr.kiosk.domain.ContactDetails
import com.charm.ehr.kiosk.domain.Country
import com.charm.ehr.kiosk.domain.DmCaregiverRelationship
import com.charm.ehr.kiosk.domain.DmEthnicity
import com.charm.ehr.kiosk.domain.DmRace
import com.charm.ehr.kiosk.common.util.MDAccountsRelatedUtil
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.domain.PostalAddress
import com.charm.ehr.kiosk.domain.PracticePayer
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.domain.State
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.common.util.DateUtil
import com.charm.ehr.kiosk.domain.repository.PatientDetailRepository
import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.domain.usecase.patient.GetPatientDetailUseCase
import com.charm.ehr.kiosk.domain.usecase.patient.InsuranceCardUseCases
import com.charm.ehr.kiosk.domain.usecase.patient.PatientInsuranceUseCases
import com.charm.ehr.kiosk.domain.usecase.patient.SavePatientUseCase
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.presentation.patient_profile.Section
import com.charm.ehr.kiosk.presentation.tour_page.SessionManager
import com.charm.ehr.kiosk.common.util.FileUtil
import com.charm.ehr.kiosk.common.util.ModelUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    val authManager: AuthManager,
    val patientRepository: PatientRepository,
    private val patientDetailRepository: PatientDetailRepository,
    private val sessionManager: SessionManager,
    private val getPatientDetailUseCase: GetPatientDetailUseCase,
    private val savePatientUseCase: SavePatientUseCase,
    private val patientInsuranceUseCases: PatientInsuranceUseCases,
    private val insuranceCardUseCases: InsuranceCardUseCases
) : ViewModel() {


    //region State declarations
    private val _editProfileState: MutableStateFlow<EditProfileState> =
        MutableStateFlow(EditProfileState())
    val editProfileState: StateFlow<EditProfileState> = _editProfileState.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000L),
        initialValue = _editProfileState.value
    )

    val selectedPatient: StateFlow<Patient?> = patientDetailRepository.patient

    // Navigation event flow for Compose navigation
    private val _navigationEvents = MutableSharedFlow<CustomNavigationEvent>()
    val navigationEvents = _navigationEvents

    // Dialog state for discard changes
    private val _showDiscardDialog = MutableStateFlow(false)
    val showDiscardDialog: StateFlow<Boolean> = _showDiscardDialog

    private val _saveInsuranceDialog = MutableStateFlow(false)
    val saveInsuranceDialog: StateFlow<Boolean> = _saveInsuranceDialog
    //endregion

    private var lastFailedAction: (() -> Unit)? = null
    private var pendingSectionChange: Section? = null

    init {
        setPreRequiredData {
            setPersonalInformationFromPatient()
            setContactInformationFromPatient()
            setCaregiverDetailsFromPatient()
            setInsuranceFromRepository()
        }
        val defaultSelectedSection = if (authManager.showEditPatient()) {
            Section.PERSONAL_INFO
        } else {
            Section.PRIMARY_INSURANCE
        }
        _editProfileState.update {
            it.copy(
                allowEditPatientDetails = authManager.showEditPatient(),
                isInsuranceRequired = authManager.showInsuranceModule(),
                selectedSection = defaultSelectedSection
            )
        }
    }

    fun setPreRequiredData(onComplete: () -> Unit) {
        _editProfileState.update { it.copy(demographicsSetting = patientDetailRepository.demographicsSetting) }
        _editProfileState.update { it.copy(demographicsMetrics = patientDetailRepository.demographicsMetrics) }
        _editProfileState.update { it.copy(countryList = patientDetailRepository.countryList) }
        _editProfileState.update { it.copy(countryStateMap = patientDetailRepository.countryStateMap) }
        onComplete()
    }

    //region Navigation handling
    fun setSelectedSection(section: Section) {
        val isNextFlow = getFlowForSelectedSection(section)
        var isShowPrevious = section != Section.PERSONAL_INFO
        val isShowSkip =
            (section == Section.PRIMARY_INSURANCE || section == Section.SECONDARY_INSURANCE)
        if (section == Section.PRIMARY_INSURANCE && !editProfileState.value.allowEditPatientDetails) {
            isShowPrevious = false
        }
        _editProfileState.update {
            it.copy(
                selectedSection = section,
                isNextFlow = isNextFlow,
                isSaveFlow = !isNextFlow,
                isShowPrevious = isShowPrevious,
                isShowSkip = isShowSkip
            )
        }
    }

    fun updateSelectedSection(nextSection: Section) {
        _editProfileState.update {
            it.copy(
                selectedSection = nextSection,
                isShowPrevious = (nextSection == Section.PRIMARY_INSURANCE && editProfileState.value.allowEditPatientDetails) || nextSection == Section.SECONDARY_INSURANCE
            )
        }
    }

    fun getFlowForSelectedSection(currentSection: Section): Boolean {
        val isNextFlow = when (currentSection) {
            Section.PERSONAL_INFO -> {
                true
            }

            Section.CONTACT_INFO, Section.EMERGENCY_CONTACT -> {
                true
            }

            Section.CAREGIVER_DETAILS -> {
                false
            }

            Section.PRIMARY_INSURANCE -> {
                false
            }

            Section.SECONDARY_INSURANCE -> {
                false
            }
        }
        return isNextFlow
    }
    //endregion


    fun onChangeAction(action: EditProfileAction) {

        when (action) {

            is EditProfileAction.OnClickSave -> {
                onClickSave(action.section)
            }

            is EditProfileAction.OnSectionChange -> {
                if (editProfileState.value.selectedSection != action.section) {
                    if (editProfileState.value.selectedSection == Section.PERSONAL_INFO && editProfileState.value.isPersonalDetailChanged) {
                        if (!personalDetailValidation()) {
                            return
                        }
                        if (action.section == Section.PRIMARY_INSURANCE || action.section == Section.SECONDARY_INSURANCE) {
                            savePatient()
                        }
                    } else if (editProfileState.value.selectedSection == Section.CONTACT_INFO && editProfileState.value.isPersonalDetailChanged) {
                        if (!validateContactInformation()) {
                            return
                        }
                        if (action.section == Section.PRIMARY_INSURANCE || action.section == Section.SECONDARY_INSURANCE) {
                            savePatient()
                        }
                    } else if (editProfileState.value.selectedSection == Section.CAREGIVER_DETAILS && editProfileState.value.isPersonalDetailChanged) {
                        if (!validateCaregiverDetails()) {
                            return
                        }
                        if (action.section == Section.PRIMARY_INSURANCE || action.section == Section.SECONDARY_INSURANCE) {
                            savePatient()
                        }
                    } else if ((editProfileState.value.selectedSection == Section.SECONDARY_INSURANCE && editProfileState.value.isSecondaryInsuranceChanged)) {
                        pendingSectionChange = action.section
                        _saveInsuranceDialog.update { true }
                    } else if (editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE && editProfileState.value.isPrimaryInsuranceChanged) {
                        pendingSectionChange = action.section
                        if (action.section == Section.SECONDARY_INSURANCE) {
                            savePrimaryInsurance()
                        } else {
                            _saveInsuranceDialog.update { true }
                        }
                        return
                    }

                    val isNextFlow = getFlowForSelectedSection(action.section)
                    var isShowPrevious = action.section != Section.PERSONAL_INFO
                    val isShowSkip =
                        (action.section == Section.PRIMARY_INSURANCE || action.section == Section.SECONDARY_INSURANCE)
                    if (action.section == Section.PRIMARY_INSURANCE && !editProfileState.value.allowEditPatientDetails) {
                        isShowPrevious = false
                    }
                    _editProfileState.update {
                        it.copy(
                            selectedSection = action.section,
                            isNextFlow = isNextFlow,
                            isSaveFlow = !isNextFlow,
                            isShowPrevious = isShowPrevious,
                            isShowSkip = isShowSkip
                        )
                    }
                }
            }

            is EditProfileAction.OnUpdatePersonalInformation -> {
                _editProfileState.update {
                    it.copy(
                        personalInformation = it.personalInformation.copy(
                            firstName = action.firstName?.take(35)?.let { value ->
                                it.personalInformation.firstName.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.personalInformation.firstName,
                            lastName = action.lastName?.take(35)?.let { value ->
                                it.personalInformation.lastName.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.personalInformation.lastName,
                            dateOfBirth = action.dateOfBirth?.let { value ->
                                it.personalInformation.dateOfBirth.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.personalInformation.dateOfBirth,
                            birthSex = action.birthSex?.let { value ->
                                it.personalInformation.birthSex.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.personalInformation.birthSex,
                            genderIdentity = action.genderIdentity?.let { value ->
                                it.personalInformation.genderIdentity.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.personalInformation.genderIdentity,
                            bloodGroup = action.bloodGroup?.let { value ->
                                it.personalInformation.bloodGroup.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.personalInformation.bloodGroup,
                            race = action.race?.let { value ->
                                it.personalInformation.race.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.personalInformation.race,
                            ethnicity = action.ethnicity?.let { value ->
                                it.personalInformation.ethnicity.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.personalInformation.ethnicity,
                            language = action.language?.let { value ->
                                it.personalInformation.language.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.personalInformation.language,
                            smokingStatus = action.smokingStatus?.let { value ->
                                it.personalInformation.smokingStatus.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.personalInformation.smokingStatus,
                            maritalStatus = action.maritalStatus?.let { value ->
                                it.personalInformation.maritalStatus.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.personalInformation.maritalStatus,
                            employmentStatus = action.employmentStatus?.let { value ->
                                it.personalInformation.employmentStatus.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.personalInformation.employmentStatus,
                        ),
                        isPersonalDetailChanged = true
                    )
                }
            }

            is EditProfileAction.OnUpdateContactInformation -> {
                // Handling mandatory based on preferred communication
                val selectedCommunication = action.preferredCommunication
                    ?: editProfileState.value.contactInformation.preferredCommunication.value
                val isEmailMandatory =
                    (selectedCommunication == "Email" || !selectedPatient.value?.contactDetails?.emailId.isNullOrBlank())
                val isMailMandatory =
                    selectedCommunication == "Mail"
                val isPhoneMandatory =
                    selectedCommunication == "Phone"

                // Handling mandatory based on primaryPhone selection
                val primaryPhoneValue = action.primaryPhone
                    ?: editProfileState.value.contactInformation.primaryPhone.value
                val isMobileMandatory = primaryPhoneValue == "Mobile Phone"
                val isHomeMandatory = primaryPhoneValue == "Home Phone"
                val isWorkMandatory = primaryPhoneValue == "Work Phone"

                _editProfileState.update {
                    it.copy(
                        contactInformation = it.contactInformation.copy(
                            preferredCommunication = action.preferredCommunication?.let { value ->
                                it.contactInformation.preferredCommunication.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.contactInformation.preferredCommunication,
                            email = action.email?.take(100)?.let { value ->
                                it.contactInformation.email.copy(
                                    value = value,
                                    error = null, isMandatory = isEmailMandatory
                                )
                            } ?: action.emailError?.let { error ->
                                it.contactInformation.email.copy(
                                    error = DynamicString(error),
                                )
                            } ?: it.contactInformation.email.copy(
                                isMandatory = isEmailMandatory
                            ),
                            primaryPhone = action.primaryPhone?.let { value ->
                                it.contactInformation.primaryPhone.copy(
                                    value = value,
                                    error = null,
                                    isMandatory = isPhoneMandatory
                                )
                            } ?: it.contactInformation.primaryPhone.copy(
                                isMandatory = isPhoneMandatory
                            ),
                            mobilePhone = action.mobilePhone?.filter { newValue -> newValue.isDigit() }
                                ?.take(
                                    ModelUtil.getPhoneNumberMaxLength(
                                        it.contactInformation.country.value?.value
                                            ?: ""
                                    )
                                )?.let { value ->
                                    it.contactInformation.mobilePhone.copy(
                                        value = value,
                                        error = null,
                                        isMandatory = isMobileMandatory
                                    )
                                } ?: it.contactInformation.mobilePhone.copy(
                                isMandatory = isMobileMandatory,
                            ),
                            homePhone = action.homePhone?.filter { newValue -> newValue.isDigit() }
                                ?.take(
                                    ModelUtil.getPhoneNumberMaxLength(
                                        it.contactInformation.country.value?.value
                                            ?: ""
                                    )
                                )?.let { value ->
                                    it.contactInformation.homePhone.copy(
                                        value = value,
                                        error = null,
                                        isMandatory = isHomeMandatory
                                    )
                                } ?: it.contactInformation.homePhone.copy(
                                isMandatory = isHomeMandatory
                            ),
                            workPhone = action.workPhone?.filter { newValue -> newValue.isDigit() }
                                ?.take(
                                    ModelUtil.getPhoneNumberMaxLength(
                                        it.contactInformation.country.value?.value
                                            ?: ""
                                    )
                                )?.let { value ->
                                    it.contactInformation.workPhone.copy(
                                        value = value,
                                        error = null,
                                        isMandatory = isWorkMandatory
                                    )
                                } ?: it.contactInformation.workPhone.copy(
                                isMandatory = isWorkMandatory
                            ),
                            workPhoneExtn = action.workPhoneExtn?.filter { newValue -> newValue.isDigit() }
                                ?.take(4)?.let { value ->
                                    it.contactInformation.workPhoneExtn.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.contactInformation.workPhoneExtn,
                            addressLine1 = action.addressLine1?.take(35)?.let { value ->
                                it.contactInformation.addressLine1.copy(
                                    value = value,
                                    error = null,
                                    isMandatory = isMailMandatory
                                )
                            } ?: it.contactInformation.addressLine1.copy(
                                isMandatory = isMailMandatory
                            ),
                            addressLine2 = action.addressLine2?.take(35)?.let { value ->
                                it.contactInformation.addressLine2.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.contactInformation.addressLine2,
                            city = action.city?.take(35)?.let { value ->
                                it.contactInformation.city.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.contactInformation.city,
                            state = action.state?.let { value ->
                                it.contactInformation.state.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.contactInformation.state,
                            country = action.country?.let { value ->
                                it.contactInformation.country.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.contactInformation.country,
                            postalCode = action.postalCode?.filter { newValue -> newValue.isDigit() }
                                ?.take(
                                    ModelUtil.getZipCodeMaxLength(
                                        it.contactInformation.country.value?.value ?: ""
                                    )
                                )?.let { value ->
                                    it.contactInformation.postalCode.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.contactInformation.postalCode,
                            emergencyContactName = action.emergencyContactName?.take(70)
                                ?.let { value ->
                                    it.contactInformation.emergencyContactName.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.contactInformation.emergencyContactName,
                            emergencyContactNumber = action.emergencyContactNumber?.filter { newValue -> newValue.isDigit() }
                                ?.take(
                                    ModelUtil.getPhoneNumberMaxLength(
                                        it.contactInformation.country.value?.value
                                            ?: ""
                                    )
                                )
                                ?.let { value ->
                                    it.contactInformation.emergencyContactNumber.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.contactInformation.emergencyContactNumber,
                            emergencyContactExtn = action.emergencyContactExtn?.filter { newValue -> newValue.isDigit() }
                                ?.take(4)
                                ?.let { value ->
                                    it.contactInformation.emergencyContactExtn.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.contactInformation.emergencyContactExtn
                        ), isPersonalDetailChanged = true
                    )
                }
            }

            is EditProfileAction.OnUpdatePrimaryInsurance -> {
                _editProfileState.update {
                    it.copy(isPrimaryInsuranceChanged = true)
                }
                if (action.isCopyPatientAddress != null) {
                    if (action.isCopyPatientAddress) {
                        val contactInfo = editProfileState.value.contactInformation
                        _editProfileState.update {
                            it.copy(
                                primaryInsurance = it.primaryInsurance.copy(
                                    insuredPersonDetail = it.primaryInsurance.insuredPersonDetail?.copy(
                                        isCopyPatientAddress = true,
                                        addressLine1 = contactInfo.addressLine1,
                                        addressLine2 = contactInfo.addressLine2,
                                        city = contactInfo.city,
                                        state = contactInfo.state,
                                        country = contactInfo.country,
                                        postalCode = contactInfo.postalCode,
                                        mobilePhone = contactInfo.mobilePhone,
                                        homePhone = contactInfo.homePhone
                                    )
                                )
                            )
                        }
                    } else {
                        _editProfileState.update {
                            it.copy(
                                primaryInsurance = it.primaryInsurance.copy(
                                    insuredPersonDetail = it.primaryInsurance.insuredPersonDetail?.copy(
                                        isCopyPatientAddress = false,
                                        addressLine1 = FieldState(value = ""),
                                        addressLine2 = FieldState(value = ""),
                                        city = FieldState(value = ""),
                                        state = FieldState(value = null),
                                        country = FieldState(value = null),
                                        postalCode = FieldState(value = ""),
                                        mobilePhone = FieldState(value = ""),
                                        homePhone = FieldState(value = "")
                                    )
                                )
                            )
                        }
                    }
                } else if (!action.relationship.isNullOrBlank() && action.relationship == ModelUtil.relationshipList[0] /* self */) {
                    _editProfileState.update {
                        it.copy(
                            primaryInsurance = it.primaryInsurance.copy(
                                insuranceType = action.insuranceType?.let { value ->
                                    it.primaryInsurance.insuranceType.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.primaryInsurance.insuranceType,
                                planOrProgramName = action.planOrProgram?.take(150)?.let { value ->
                                    it.primaryInsurance.planOrProgramName.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.primaryInsurance.planOrProgramName,
                                planId = action.planId?.take(30)?.let { value ->
                                    it.primaryInsurance.planId.copy(value = value, error = null)
                                } ?: it.primaryInsurance.planId,
                                policyGroupOrFECA = action.policyGroup?.take(30)?.let { value ->
                                    it.primaryInsurance.policyGroupOrFECA.copy(value = value)
                                } ?: it.primaryInsurance.policyGroupOrFECA,
                                selectedPayer = action.selectedPayer?.let { value ->
                                    it.primaryInsurance.selectedPayer.copy(
                                        value = value,
                                        error = null
                                    )

                                } ?: it.primaryInsurance.selectedPayer,
                                copay = action.copay?.take(10)?.let { value ->
                                    it.primaryInsurance.copay.copy(value = value)
                                } ?: it.primaryInsurance.copay,
                                deductible = action.deductible?.take(10)?.let { value ->
                                    it.primaryInsurance.deductible.copy(value = value)
                                } ?: it.primaryInsurance.deductible,
                                coinsurance = action.coinsurance?.take(25)?.let { value ->
                                    it.primaryInsurance.coinsurance.copy(value = value)
                                } ?: it.primaryInsurance.coinsurance,
                                validFrom = action.validFrom?.let { value ->
                                    it.primaryInsurance.validFrom.copy(value = value)
                                } ?: it.primaryInsurance.validFrom,
                                validUntil = action.validUntil?.let { value ->
                                    it.primaryInsurance.validUntil.copy(value = value)
                                } ?: it.primaryInsurance.validUntil,
                                employerOrSchoolName = action.employerOrSchoolName?.take(150)
                                    ?.let { value ->
                                        it.primaryInsurance.employerOrSchoolName.copy(value = value)
                                    } ?: it.primaryInsurance.employerOrSchoolName,
                                comment = action.comment?.take(250)?.let { value ->
                                    it.primaryInsurance.comment.copy(value = value)
                                } ?: it.primaryInsurance.comment,
                                insuredPersonDetail = it.primaryInsurance.insuredPersonDetail?.copy(
                                    relationship = FieldState(
                                        value = DmCaregiverRelationship(
                                            value = action.relationship, key = ""
                                        ), error = null
                                    ),
                                    firstName = editProfileState.value.personalInformation.firstName,
                                    lastName = editProfileState.value.personalInformation.lastName,
                                    dateOfBirth = editProfileState.value.personalInformation.dateOfBirth,
                                    birthSex = editProfileState.value.personalInformation.birthSex,
                                    addressLine1 = editProfileState.value.contactInformation.addressLine1,
                                    addressLine2 = editProfileState.value.contactInformation.addressLine2,
                                    city = editProfileState.value.contactInformation.city,
                                    state = editProfileState.value.contactInformation.state,
                                    country = editProfileState.value.contactInformation.country,
                                    postalCode = editProfileState.value.contactInformation.postalCode,
                                    mobilePhone = editProfileState.value.contactInformation.mobilePhone,
                                    homePhone = editProfileState.value.contactInformation.homePhone
                                )
                            )
                        )

                    }
                } else {
                    _editProfileState.update {
                        it.copy(
                            primaryInsurance = it.primaryInsurance.copy(
                                insuranceType = action.insuranceType?.let { value ->
                                    it.primaryInsurance.insuranceType.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.primaryInsurance.insuranceType,
                                planOrProgramName = action.planOrProgram?.take(150)?.let { value ->
                                    it.primaryInsurance.planOrProgramName.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.primaryInsurance.planOrProgramName,
                                planId = action.planId?.take(30)?.let { value ->
                                    it.primaryInsurance.planId.copy(value = value, error = null)
                                } ?: it.primaryInsurance.planId,
                                policyGroupOrFECA = action.policyGroup?.take(30)?.let { value ->
                                    it.primaryInsurance.policyGroupOrFECA.copy(value = value)
                                } ?: it.primaryInsurance.policyGroupOrFECA,
                                selectedPayer = action.selectedPayer?.let { value ->
                                    it.primaryInsurance.selectedPayer.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.primaryInsurance.selectedPayer,
                                copay = action.copay?.take(10)?.let { value ->
                                    it.primaryInsurance.copay.copy(value = value)
                                } ?: it.primaryInsurance.copay,
                                deductible = action.deductible?.take(10)?.let { value ->
                                    it.primaryInsurance.deductible.copy(value = value)
                                } ?: it.primaryInsurance.deductible,
                                coinsurance = action.coinsurance?.take(25)?.let { value ->
                                    it.primaryInsurance.coinsurance.copy(value = value)
                                } ?: it.primaryInsurance.coinsurance,
                                validFrom = action.validFrom?.let { value ->
                                    it.primaryInsurance.validFrom.copy(value = value, error = null)
                                } ?: it.primaryInsurance.validFrom,
                                validUntil = action.validUntil?.let { value ->
                                    it.primaryInsurance.validUntil.copy(value = value, error = null)
                                } ?: it.primaryInsurance.validUntil,
                                employerOrSchoolName = action.employerOrSchoolName?.take(150)
                                    ?.let { value ->
                                        it.primaryInsurance.employerOrSchoolName.copy(value = value)
                                    } ?: it.primaryInsurance.employerOrSchoolName,
                                comment = action.comment?.take(250)?.let { value ->
                                    it.primaryInsurance.comment.copy(value = value)
                                } ?: it.primaryInsurance.comment,
                                insuredPersonDetail = it.primaryInsurance.insuredPersonDetail?.copy(
                                    relationship = action.relationship?.let { rel ->
                                        it.primaryInsurance.insuredPersonDetail.relationship.copy(
                                            value = DmCaregiverRelationship(
                                                value = rel,
                                                key = ""
                                            ), error = null
                                        )
                                    } ?: it.primaryInsurance.insuredPersonDetail.relationship,
                                    firstName = action.firstName?.take(35)?.let { v ->
                                        it.primaryInsurance.insuredPersonDetail.firstName.copy(
                                            value = v,
                                            error = null
                                        )
                                    } ?: it.primaryInsurance.insuredPersonDetail.firstName,
                                    lastName = action.lastName?.take(35)?.let { v ->
                                        it.primaryInsurance.insuredPersonDetail.lastName.copy(
                                            value = v,
                                            error = null
                                        )
                                    } ?: it.primaryInsurance.insuredPersonDetail.lastName,
                                    dateOfBirth = action.dateOfBirth?.let { v ->
                                        it.primaryInsurance.insuredPersonDetail.dateOfBirth.copy(
                                            value = v, error = null
                                        )
                                    } ?: it.primaryInsurance.insuredPersonDetail.dateOfBirth,
                                    birthSex = action.birthSex?.let { v ->
                                        it.primaryInsurance.insuredPersonDetail.birthSex.copy(value = v)
                                    } ?: it.primaryInsurance.insuredPersonDetail.birthSex,
                                    addressLine1 = action.addressLine1?.take(35)?.let { v ->
                                        it.primaryInsurance.insuredPersonDetail.addressLine1.copy(
                                            value = v
                                        )
                                    } ?: it.primaryInsurance.insuredPersonDetail.addressLine1,
                                    addressLine2 = action.addressLine2?.take(35)?.let { v ->
                                        it.primaryInsurance.insuredPersonDetail.addressLine2.copy(
                                            value = v
                                        )
                                    } ?: it.primaryInsurance.insuredPersonDetail.addressLine2,
                                    city = action.city?.take(35)?.let { v ->
                                        it.primaryInsurance.insuredPersonDetail.city.copy(value = v)
                                    } ?: it.primaryInsurance.insuredPersonDetail.city,
                                    state = action.state?.let { v ->
                                        it.primaryInsurance.insuredPersonDetail.state.copy(value = v)
                                    } ?: it.primaryInsurance.insuredPersonDetail.state,
                                    country = action.country?.let { v ->
                                        it.primaryInsurance.insuredPersonDetail.country.copy(value = v)
                                    } ?: it.primaryInsurance.insuredPersonDetail.country,
                                    postalCode = action.postalCode?.filter { newValue -> newValue.isDigit() }
                                        ?.take(
                                            ModelUtil.getZipCodeMaxLength(
                                                it.primaryInsurance.insuredPersonDetail.country.value?.value
                                                    ?: ""
                                            )
                                        )?.let { v ->
                                            it.primaryInsurance.insuredPersonDetail.postalCode.copy(
                                                value = v,
                                                error = null
                                            )

                                        } ?: it.primaryInsurance.insuredPersonDetail.postalCode,
                                    mobilePhone = action.mobilePhone?.filter { newValue -> newValue.isDigit() }
                                        ?.take(
                                            ModelUtil.getPhoneNumberMaxLength(
                                                it.primaryInsurance.insuredPersonDetail.country.value?.value
                                                    ?: ""
                                            )
                                        )?.let { v ->
                                            it.primaryInsurance.insuredPersonDetail.mobilePhone.copy(
                                                value = v
                                            )
                                        } ?: it.primaryInsurance.insuredPersonDetail.mobilePhone,
                                    homePhone = action.homePhone?.filter { newValue -> newValue.isDigit() }
                                        ?.take(
                                            ModelUtil.getPhoneNumberMaxLength(
                                                it.primaryInsurance.insuredPersonDetail.country.value?.value
                                                    ?: ""
                                            )
                                        )?.let { v ->
                                            it.primaryInsurance.insuredPersonDetail.homePhone.copy(
                                                value = v
                                            )
                                        } ?: it.primaryInsurance.insuredPersonDetail.homePhone
                                )
                            )
                        )
                    }
                }
            }

            is EditProfileAction.OnUpdateSecondaryInsurance -> {
                _editProfileState.update {
                    it.copy(isSecondaryInsuranceChanged = true)
                }
                if (action.isCopyPatientAddress != null) {
                    if (action.isCopyPatientAddress) {
                        val contactInfo = editProfileState.value.contactInformation
                        _editProfileState.update {
                            it.copy(
                                secondaryInsurance = it.secondaryInsurance.copy(
                                    insuredPersonDetail = it.secondaryInsurance.insuredPersonDetail?.copy(
                                        isCopyPatientAddress = true,
                                        addressLine1 = contactInfo.addressLine1,
                                        addressLine2 = contactInfo.addressLine2,
                                        city = contactInfo.city,
                                        state = contactInfo.state,
                                        country = contactInfo.country,
                                        postalCode = contactInfo.postalCode,
                                        mobilePhone = contactInfo.mobilePhone,
                                        homePhone = contactInfo.homePhone
                                    )
                                )
                            )
                        }
                    } else {
                        _editProfileState.update {
                            it.copy(
                                secondaryInsurance = it.secondaryInsurance.copy(
                                    insuredPersonDetail = it.secondaryInsurance.insuredPersonDetail?.copy(
                                        isCopyPatientAddress = false,
                                        addressLine1 = FieldState(value = ""),
                                        addressLine2 = FieldState(value = ""),
                                        city = FieldState(value = ""),
                                        state = FieldState(value = null),
                                        country = FieldState(value = null),
                                        postalCode = FieldState(value = ""),
                                        mobilePhone = FieldState(value = ""),
                                        homePhone = FieldState(value = "")
                                    )
                                )
                            )
                        }
                    }
                } else if (!action.relationship.isNullOrBlank() && action.relationship == ModelUtil.relationshipList[0] /* self */) {
                    _editProfileState.update {
                        it.copy(
                            secondaryInsurance = it.secondaryInsurance.copy(
                                insuranceType = action.insuranceType?.let { value ->
                                    it.secondaryInsurance.insuranceType.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.secondaryInsurance.insuranceType,
                                planOrProgramName = action.planOrProgram?.take(150)?.let { value ->
                                    it.secondaryInsurance.planOrProgramName.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.secondaryInsurance.planOrProgramName,
                                planId = action.planId?.take(30)?.let { value ->
                                    it.secondaryInsurance.planId.copy(value = value, error = null)
                                } ?: it.secondaryInsurance.planId,
                                policyGroupOrFECA = action.policyGroup?.take(30)?.let { value ->
                                    it.secondaryInsurance.policyGroupOrFECA.copy(value = value)
                                } ?: it.secondaryInsurance.policyGroupOrFECA,
                                selectedPayer = action.selectedPayer?.let { value ->
                                    it.secondaryInsurance.selectedPayer.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.secondaryInsurance.selectedPayer,
                                copay = action.copay?.take(10)?.let { value ->
                                    it.secondaryInsurance.copay.copy(value = value)
                                } ?: it.secondaryInsurance.copay,
                                deductible = action.deductible?.take(10)?.let { value ->
                                    it.secondaryInsurance.deductible.copy(value = value)
                                } ?: it.secondaryInsurance.deductible,
                                coinsurance = action.coinsurance?.take(25)?.let { value ->
                                    it.secondaryInsurance.coinsurance.copy(value = value)
                                } ?: it.secondaryInsurance.coinsurance,
                                validFrom = action.validFrom?.let { value ->
                                    it.secondaryInsurance.validFrom.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.secondaryInsurance.validFrom,
                                validUntil = action.validUntil?.let { value ->
                                    it.secondaryInsurance.validUntil.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.secondaryInsurance.validUntil,
                                employerOrSchoolName = action.employerOrSchoolName?.take(150)
                                    ?.let { value ->
                                        it.secondaryInsurance.employerOrSchoolName.copy(value = value)
                                    } ?: it.secondaryInsurance.employerOrSchoolName,
                                comment = action.comment?.take(250)?.let { value ->
                                    it.secondaryInsurance.comment.copy(value = value)
                                } ?: it.secondaryInsurance.comment,
                                insuredPersonDetail = it.secondaryInsurance.insuredPersonDetail?.copy(
                                    relationship = FieldState(
                                        value = DmCaregiverRelationship(
                                            value = action.relationship, key = ""
                                        )
                                    ),
                                    firstName = editProfileState.value.personalInformation.firstName,
                                    lastName = editProfileState.value.personalInformation.lastName,
                                    dateOfBirth = editProfileState.value.personalInformation.dateOfBirth,
                                    birthSex = editProfileState.value.personalInformation.birthSex,
                                    addressLine1 = editProfileState.value.contactInformation.addressLine1,
                                    addressLine2 = editProfileState.value.contactInformation.addressLine2,
                                    city = editProfileState.value.contactInformation.city,
                                    state = editProfileState.value.contactInformation.state,
                                    country = editProfileState.value.contactInformation.country,
                                    postalCode = editProfileState.value.contactInformation.postalCode,
                                    mobilePhone = editProfileState.value.contactInformation.mobilePhone,
                                    homePhone = editProfileState.value.contactInformation.homePhone
                                ),
                                insuranceID = action.insuranceId?.let { value ->
                                    it.secondaryInsurance.insuranceID.copy(value = value)
                                } ?: it.secondaryInsurance.insuranceID

                            )
                        )
                    }
                } else {
                    _editProfileState.update {
                        it.copy(
                            secondaryInsurance = it.secondaryInsurance.copy(
                                insuranceType = action.insuranceType?.let { value ->
                                    it.secondaryInsurance.insuranceType.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.secondaryInsurance.insuranceType,
                                planOrProgramName = action.planOrProgram?.take(150)?.let { value ->
                                    it.secondaryInsurance.planOrProgramName.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.secondaryInsurance.planOrProgramName,
                                planId = action.planId?.take(30)?.let { value ->
                                    it.secondaryInsurance.planId.copy(value = value, error = null)
                                } ?: it.secondaryInsurance.planId,
                                policyGroupOrFECA = action.policyGroup?.take(30)?.let { value ->
                                    it.secondaryInsurance.policyGroupOrFECA.copy(value = value)
                                } ?: it.secondaryInsurance.policyGroupOrFECA,
                                selectedPayer = action.selectedPayer?.let { value ->
                                    it.secondaryInsurance.selectedPayer.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.secondaryInsurance.selectedPayer,
                                copay = action.copay?.take(10)?.let { value ->
                                    it.secondaryInsurance.copay.copy(value = value)
                                } ?: it.secondaryInsurance.copay,
                                deductible = action.deductible?.take(10)?.let { value ->
                                    it.secondaryInsurance.deductible.copy(value = value)
                                } ?: it.secondaryInsurance.deductible,
                                coinsurance = action.coinsurance?.take(25)?.let { value ->
                                    it.secondaryInsurance.coinsurance.copy(value = value)
                                } ?: it.secondaryInsurance.coinsurance,
                                validFrom = action.validFrom?.let { value ->
                                    it.secondaryInsurance.validFrom.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.secondaryInsurance.validFrom,
                                validUntil = action.validUntil?.let { value ->
                                    it.secondaryInsurance.validUntil.copy(
                                        value = value,
                                        error = null
                                    )
                                } ?: it.secondaryInsurance.validUntil,
                                employerOrSchoolName = action.employerOrSchoolName?.take(150)
                                    ?.let { value ->
                                        it.secondaryInsurance.employerOrSchoolName.copy(value = value)
                                    } ?: it.secondaryInsurance.employerOrSchoolName,
                                comment = action.comment?.take(250)?.let { value ->
                                    it.secondaryInsurance.comment.copy(value = value)
                                } ?: it.secondaryInsurance.comment,
                                insuredPersonDetail = it.secondaryInsurance.insuredPersonDetail?.copy(
                                    relationship = action.relationship?.let { rel ->
                                        it.secondaryInsurance.insuredPersonDetail.relationship.copy(
                                            value = DmCaregiverRelationship(
                                                value = rel,
                                                key = ""
                                            ), error = null
                                        )
                                    } ?: it.secondaryInsurance.insuredPersonDetail.relationship,
                                    firstName = action.firstName?.take(35)?.let { v ->
                                        it.secondaryInsurance.insuredPersonDetail.firstName.copy(
                                            value = v,
                                            error = null
                                        )
                                    } ?: it.secondaryInsurance.insuredPersonDetail.firstName,
                                    lastName = action.lastName?.take(35)?.let { v ->
                                        it.secondaryInsurance.insuredPersonDetail.lastName.copy(
                                            value = v,
                                            error = null
                                        )
                                    } ?: it.secondaryInsurance.insuredPersonDetail.lastName,
                                    dateOfBirth = action.dateOfBirth?.let { v ->
                                        it.secondaryInsurance.insuredPersonDetail.dateOfBirth.copy(
                                            value = v, error = null
                                        )
                                    } ?: it.secondaryInsurance.insuredPersonDetail.dateOfBirth,
                                    birthSex = action.birthSex?.let { v ->
                                        it.secondaryInsurance.insuredPersonDetail.birthSex.copy(
                                            value = v
                                        )
                                    } ?: it.secondaryInsurance.insuredPersonDetail.birthSex,
                                    addressLine1 = action.addressLine1?.take(35)?.let { v ->
                                        it.secondaryInsurance.insuredPersonDetail.addressLine1.copy(
                                            value = v
                                        )
                                    } ?: it.secondaryInsurance.insuredPersonDetail.addressLine1,
                                    addressLine2 = action.addressLine2?.take(35)?.let { v ->
                                        it.secondaryInsurance.insuredPersonDetail.addressLine2.copy(
                                            value = v
                                        )
                                    } ?: it.secondaryInsurance.insuredPersonDetail.addressLine2,
                                    city = action.city?.take(35)?.let { v ->
                                        it.secondaryInsurance.insuredPersonDetail.city.copy(value = v)
                                    } ?: it.secondaryInsurance.insuredPersonDetail.city,
                                    state = action.state?.let { v ->
                                        it.secondaryInsurance.insuredPersonDetail.state.copy(value = v)
                                    } ?: it.secondaryInsurance.insuredPersonDetail.state,
                                    country = action.country?.let { v ->
                                        it.secondaryInsurance.insuredPersonDetail.country.copy(value = v)
                                    } ?: it.secondaryInsurance.insuredPersonDetail.country,
                                    postalCode = action.postalCode?.filter { newValue -> newValue.isDigit() }
                                        ?.take(
                                            ModelUtil.getZipCodeMaxLength(
                                                it.secondaryInsurance.insuredPersonDetail.country.value?.value
                                                    ?: ""
                                            )
                                        )?.let { v ->
                                            it.secondaryInsurance.insuredPersonDetail.postalCode.copy(
                                                value = v,
                                                error = null
                                            )

                                        } ?: it.secondaryInsurance.insuredPersonDetail.postalCode,
                                    mobilePhone = action.mobilePhone?.filter { newValue -> newValue.isDigit() }
                                        ?.take(
                                            ModelUtil.getPhoneNumberMaxLength(
                                                it.secondaryInsurance.insuredPersonDetail.country.value?.value
                                                    ?: ""
                                            )
                                        )?.let { v ->
                                            it.secondaryInsurance.insuredPersonDetail.mobilePhone.copy(
                                                value = v
                                            )
                                        } ?: it.secondaryInsurance.insuredPersonDetail.mobilePhone,
                                    homePhone = action.homePhone?.filter { newValue -> newValue.isDigit() }
                                        ?.take(
                                            ModelUtil.getPhoneNumberMaxLength(
                                                it.secondaryInsurance.insuredPersonDetail.country.value?.value
                                                    ?: ""
                                            )
                                        )?.let { v ->
                                            it.secondaryInsurance.insuredPersonDetail.homePhone.copy(
                                                value = v
                                            )
                                        } ?: it.secondaryInsurance.insuredPersonDetail.homePhone
                                )
                            )
                        )
                    }
                }
            }

            is EditProfileAction.OnClickPrevious -> {
                when (action.fromSection) {
                    Section.PERSONAL_INFO -> {

                    }

                    Section.CONTACT_INFO, Section.EMERGENCY_CONTACT -> {
                        _editProfileState.update {
                            it.copy(
                                selectedSection = Section.PERSONAL_INFO,
                                isSaveFlow = false,
                                isShowPrevious = false
                            )
                        }
                    }

                    Section.CAREGIVER_DETAILS -> {
                        _editProfileState.update {
                            it.copy(
                                selectedSection = Section.CONTACT_INFO,
                                isSaveFlow = false,
                                isShowPrevious = true,
                            )
                        }
                    }

                    Section.PRIMARY_INSURANCE -> {
                        _editProfileState.update {
                            it.copy(
                                selectedSection = Section.CAREGIVER_DETAILS,
                                isShowPrevious = true,
                                isShowSkip = false,
                            )
                        }
                    }

                    Section.SECONDARY_INSURANCE -> {
                        _editProfileState.update {
                            it.copy(
                                selectedSection = Section.PRIMARY_INSURANCE,
                                isShowPrevious = editProfileState.value.allowEditPatientDetails
                            )
                        }
                    }
                }
            }

            is EditProfileAction.OnClickNext -> {
                when (action.fromSection) {
                    Section.PERSONAL_INFO -> {
                        if (!personalDetailValidation()) {
                            return
                        }
                        _editProfileState.update {
                            it.copy(
                                selectedSection = Section.CONTACT_INFO,
                                isShowPrevious = true
                            )
                        }
                    }

                    Section.CONTACT_INFO, Section.EMERGENCY_CONTACT -> {
                        if (!validateContactInformation()) {
                            return
                        }
                        _editProfileState.update {
                            it.copy(
                                selectedSection = Section.CAREGIVER_DETAILS,
                                isSaveFlow = true
                            )
                        }
                    }

                    Section.CAREGIVER_DETAILS -> {
                        if (!validateCaregiverDetails()) {
                            return
                        }
                        savePatient()
                    }

                    Section.PRIMARY_INSURANCE -> {
                        savePrimaryInsurance()
                    }

                    Section.SECONDARY_INSURANCE -> {
                        saveSecondaryInsurance()
                    }
                }
            }

            is EditProfileAction.OnClickSkip -> {
                when (action.fromSection) {
                    Section.PRIMARY_INSURANCE -> {
                        _editProfileState.update {
                            it.copy(
                                selectedSection = Section.SECONDARY_INSURANCE
                            )
                        }
                    }

                    Section.SECONDARY_INSURANCE -> {
                        viewModelScope.launch {
                            _navigationEvents.emit(CustomNavigationEvent.Back)
                        }
                    }

                    Section.PERSONAL_INFO -> {}
                    Section.CONTACT_INFO, Section.EMERGENCY_CONTACT -> {}
                    Section.CAREGIVER_DETAILS -> {}
                }
            }

            is EditProfileAction.OnRequestStatesContactInfo -> {
                val selectedStateList = editProfileState.value.countryStateMap[
                    editProfileState.value.contactInformation.country.value?.value ?: ""
                ]
                _editProfileState.update {
                    it.copy(
                        contactInformation = it.contactInformation.copy(
                            state = it.contactInformation.state.copy(
                                value = null
                            ), stateList = selectedStateList ?: emptyList()
                        )
                    )
                }
            }

            is EditProfileAction.OnRequestStatesPrimaryCaregiver -> {
                _editProfileState.update {
                    it.copy(
                        primaryCaregiverDetails = it.primaryCaregiverDetails?.copy(
                            state = FieldState(null)
                        )
                    )
                }
            }

            is EditProfileAction.OnRequestStatesSecondaryCaregiver -> {
                _editProfileState.update {
                    it.copy(
                        secondaryCaregiverDetails = it.secondaryCaregiverDetails?.copy(
                            state = FieldState(null)
                        )
                    )
                }
            }

            is EditProfileAction.OnRequestStatesPrimaryInsurance -> {
                _editProfileState.update {
                    it.copy(
                        primaryInsurance = it.primaryInsurance.copy(
                            insuredPersonDetail = it.primaryInsurance.insuredPersonDetail?.copy(
                                state = FieldState(null)
                            )
                        )
                    )
                }
            }

            is EditProfileAction.OnRequestStatesSecondaryInsurance -> {
                _editProfileState.update {
                    it.copy(
                        secondaryInsurance = it.secondaryInsurance.copy(
                            insuredPersonDetail = it.secondaryInsurance.insuredPersonDetail?.copy(
                                state = FieldState(null)
                            )
                        )
                    )
                }
            }

            is EditProfileAction.OnSearchPayer -> {
                getPayerDetails(action.query, action.isPrimary)
            }

            is EditProfileAction.OnUpdatePrimaryCaregiverDetails -> {
                if (editProfileState.value.primaryCaregiverDetails == null) {
                    val demographicsMetrics = editProfileState.value.demographicsMetrics
                    val countryList = editProfileState.value.countryList
                    val defaultCountry = countryList.firstOrNull()
                    val defaultRelationship =
                        demographicsMetrics?.caregiverRelationship?.firstOrNull()
                    _editProfileState.update {
                        it.copy(
                            primaryCaregiverDetails = PersonDetail(
                                firstName = FieldState(value = "", isMandatory = true),
                                lastName = FieldState(value = "", isMandatory = true),
                                birthSex = FieldState(value = ""),
                                dateOfBirth = FieldState(value = ""),
                                relationship = FieldState(
                                    value = defaultRelationship,
                                    isMandatory = true
                                ),
                                addressLine1 = FieldState(value = ""),
                                addressLine2 = FieldState(value = ""),
                                city = FieldState(value = ""),
                                state = FieldState(value = null),
                                country = FieldState(value = defaultCountry),
                                postalCode = FieldState(value = ""),
                                mobilePhone = FieldState(value = ""),
                                homePhone = FieldState(value = ""),
                                isCopyPatientAddress = false
                            )
                        )
                    }
                }

                _editProfileState.update {
                    it.copy(
                        primaryCaregiverDetails = it.primaryCaregiverDetails?.copy(
                            relationship = action.relationship?.let { value ->
                                it.primaryCaregiverDetails.relationship.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.primaryCaregiverDetails.relationship,
                            firstName = action.firstName?.take(35)?.let { value ->
                                it.primaryCaregiverDetails.firstName.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.primaryCaregiverDetails.firstName,
                            lastName = action.lastName?.take(35)?.let { value ->
                                it.primaryCaregiverDetails.lastName.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.primaryCaregiverDetails.lastName,
                            dateOfBirth = action.dateOfBirth?.let { value ->
                                it.primaryCaregiverDetails.dateOfBirth.copy(value = value)
                            } ?: it.primaryCaregiverDetails.dateOfBirth,
                            birthSex = action.birthSex?.let { value ->
                                it.primaryCaregiverDetails.birthSex.copy(value = value)
                            } ?: it.primaryCaregiverDetails.birthSex,
                            isCopyPatientAddress = action.isCopyPatientAddress
                                ?: it.primaryCaregiverDetails.isCopyPatientAddress,
                            addressLine1 = if (action.isCopyPatientAddress == true) {
                                it.contactInformation.addressLine1
                            } else {
                                action.addressLine1?.take(35)?.let { value ->
                                    it.primaryCaregiverDetails.addressLine1.copy(value = value)
                                } ?: it.primaryCaregiverDetails.addressLine1
                            },
                            addressLine2 = if (action.isCopyPatientAddress == true) {
                                it.contactInformation.addressLine2
                            } else {
                                action.addressLine2?.take(35)?.let { value ->
                                    it.primaryCaregiverDetails.addressLine2.copy(value = value)
                                } ?: it.primaryCaregiverDetails.addressLine2
                            },
                            city = if (action.isCopyPatientAddress == true) {
                                it.contactInformation.city
                            } else {
                                action.city?.take(35)?.let { value ->
                                    it.primaryCaregiverDetails.city.copy(value = value)
                                } ?: it.primaryCaregiverDetails.city
                            },
                            state = if (action.isCopyPatientAddress == true) {
                                it.contactInformation.state
                            } else {
                                action.state?.let { value ->
                                    it.primaryCaregiverDetails.state.copy(value = value)
                                } ?: it.primaryCaregiverDetails.state
                            },
                            country = if (action.isCopyPatientAddress == true) {
                                it.contactInformation.country
                            } else {
                                action.country?.let { value ->
                                    it.primaryCaregiverDetails.country.copy(value = value)
                                } ?: it.primaryCaregiverDetails.country
                            },
                            postalCode = if (action.isCopyPatientAddress == true) {
                                it.contactInformation.postalCode
                            } else {
                                action.postalCode?.filter { newValue -> newValue.isDigit() }?.take(
                                    ModelUtil.getZipCodeMaxLength(
                                        it.primaryCaregiverDetails.country.value?.value
                                            ?: ""
                                    )
                                )?.let { value ->
                                    it.primaryCaregiverDetails.postalCode.copy(value = value)
                                } ?: it.primaryCaregiverDetails.postalCode
                            },
                            mobilePhone = if (action.isCopyPatientAddress == true) {
                                it.contactInformation.mobilePhone
                            } else {
                                action.mobilePhone?.filter { newValue -> newValue.isDigit() }?.take(
                                    ModelUtil.getPhoneNumberMaxLength(
                                        it.primaryCaregiverDetails.country.value?.value
                                            ?: ""
                                    )
                                )?.let { value ->
                                    it.primaryCaregiverDetails.mobilePhone.copy(value = value)
                                } ?: it.primaryCaregiverDetails.mobilePhone
                            },
                            homePhone = if (action.isCopyPatientAddress == true) {
                                it.contactInformation.homePhone
                            } else {
                                action.homePhone?.filter { newValue -> newValue.isDigit() }?.take(
                                    ModelUtil.getPhoneNumberMaxLength(
                                        it.primaryCaregiverDetails.country.value?.value
                                            ?: ""
                                    )
                                )?.let { value ->
                                    it.primaryCaregiverDetails.homePhone.copy(value = value)
                                } ?: it.primaryCaregiverDetails.homePhone
                            }
                        ), isPersonalDetailChanged = true
                    )
                }
            }

            is EditProfileAction.OnUpdateSecondaryCaregiverDetails -> {

                _editProfileState.update {
                    it.copy(
                        secondaryCaregiverDetails = it.secondaryCaregiverDetails?.copy(
                            relationship = action.relationship?.let { value ->
                                it.secondaryCaregiverDetails.relationship.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.secondaryCaregiverDetails.relationship,
                            firstName = action.firstName?.take(35)?.let { value ->
                                it.secondaryCaregiverDetails.firstName.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.secondaryCaregiverDetails.firstName,
                            lastName = action.lastName?.take(35)?.let { value ->
                                it.secondaryCaregiverDetails.lastName.copy(
                                    value = value,
                                    error = null
                                )
                            } ?: it.secondaryCaregiverDetails.lastName,
                            dateOfBirth = action.dateOfBirth?.let { value ->
                                it.secondaryCaregiverDetails.dateOfBirth.copy(value = value)
                            } ?: it.secondaryCaregiverDetails.dateOfBirth,
                            birthSex = action.birthSex?.let { value ->
                                it.secondaryCaregiverDetails.birthSex.copy(value = value)
                            } ?: it.secondaryCaregiverDetails.birthSex,
                            isCopyPatientAddress = action.isCopyPatientAddress
                                ?: it.secondaryCaregiverDetails.isCopyPatientAddress,
                            addressLine1 = if (action.isCopyPatientAddress == true) {
                                it.contactInformation.addressLine1
                            } else {
                                action.addressLine1?.take(35)?.let { value ->
                                    it.secondaryCaregiverDetails.addressLine1.copy(value = value)
                                } ?: it.secondaryCaregiverDetails.addressLine1
                            },
                            addressLine2 = if (action.isCopyPatientAddress == true) {
                                it.contactInformation.addressLine2
                            } else {
                                action.addressLine2?.take(35)?.let { value ->
                                    it.secondaryCaregiverDetails.addressLine2.copy(value = value)
                                } ?: it.secondaryCaregiverDetails.addressLine2
                            },
                            city = if (action.isCopyPatientAddress == true) {
                                it.contactInformation.city
                            } else {
                                action.city?.take(35)?.let { value ->
                                    it.secondaryCaregiverDetails.city.copy(value = value)
                                } ?: it.secondaryCaregiverDetails.city
                            },
                            state = if (action.isCopyPatientAddress == true) {
                                it.contactInformation.state
                            } else {
                                action.state?.let { value ->
                                    it.secondaryCaregiverDetails.state.copy(value = value)
                                } ?: it.secondaryCaregiverDetails.state
                            },
                            country = if (action.isCopyPatientAddress == true) {
                                it.contactInformation.country
                            } else {
                                action.country?.let { value ->
                                    it.secondaryCaregiverDetails.country.copy(value = value)
                                } ?: it.secondaryCaregiverDetails.country
                            },
                            postalCode = if (action.isCopyPatientAddress == true) {
                                it.contactInformation.postalCode
                            } else {
                                action.postalCode?.filter { newValue -> newValue.isDigit() }?.take(
                                    ModelUtil.getZipCodeMaxLength(
                                        it.secondaryCaregiverDetails.country.value?.value
                                            ?: ""
                                    )
                                )?.let { value ->
                                    it.secondaryCaregiverDetails.postalCode.copy(value = value)
                                } ?: it.secondaryCaregiverDetails.postalCode
                            },
                            mobilePhone = if (action.isCopyPatientAddress == true) {
                                it.contactInformation.mobilePhone
                            } else {
                                action.mobilePhone?.filter { newValue -> newValue.isDigit() }?.take(
                                    ModelUtil.getPhoneNumberMaxLength(
                                        it.secondaryCaregiverDetails.country.value?.value
                                            ?: ""
                                    )
                                )?.let { value ->
                                    it.secondaryCaregiverDetails.mobilePhone.copy(value = value)
                                } ?: it.secondaryCaregiverDetails.mobilePhone
                            },
                            homePhone = if (action.isCopyPatientAddress == true) {
                                it.contactInformation.homePhone
                            } else {
                                action.homePhone?.filter { newValue -> newValue.isDigit() }?.take(
                                    ModelUtil.getPhoneNumberMaxLength(
                                        it.secondaryCaregiverDetails.country.value?.value
                                            ?: ""
                                    )
                                )?.let { value ->
                                    it.secondaryCaregiverDetails.homePhone.copy(value = value)
                                } ?: it.secondaryCaregiverDetails.homePhone
                            }
                        ), isPersonalDetailChanged = true
                    )
                }
            }

            EditProfileAction.OnClickAddSecondaryCaregiver -> {
                if (editProfileState.value.secondaryCaregiverDetails == null) {
                    _editProfileState.update {
                        it.copy(
                            secondaryCaregiverDetails = PersonDetail(
                                firstName = FieldState(value = "", isMandatory = true),
                                lastName = FieldState(value = "", isMandatory = true),
                                birthSex = FieldState(value = ""),
                                dateOfBirth = FieldState(value = ""),
                                relationship = FieldState(
                                    value = null,
                                    isMandatory = true
                                ),
                                addressLine1 = FieldState(value = ""),
                                addressLine2 = FieldState(value = ""),
                                city = FieldState(value = ""),
                                state = FieldState(value = null),
                                country = FieldState(value = Country("", "")),
                                postalCode = FieldState(value = ""),
                                mobilePhone = FieldState(value = ""),
                                homePhone = FieldState(value = ""),
                                isCopyPatientAddress = false
                            ), isPersonalDetailChanged = true
                        )
                    }
                }
            }

            EditProfileAction.OnDeletePrimaryCaregiver -> {
                if (editProfileState.value.secondaryCaregiverDetails != null) {
                    _editProfileState.update {
                        it.copy(
                            primaryCaregiverDetails = editProfileState.value.secondaryCaregiverDetails,
                            secondaryCaregiverDetails = null, isPersonalDetailChanged = true
                        )
                    }
                } else {
                    _editProfileState.update {
                        it.copy(primaryCaregiverDetails = null, isPersonalDetailChanged = true)
                    }
                }
            }

            EditProfileAction.OnDeleteSecondaryCaregiver -> {
                _editProfileState.update {
                    it.copy(secondaryCaregiverDetails = null, isPersonalDetailChanged = true)
                }
            }

            EditProfileAction.OnBackPress -> {
                if (editProfileState.value.isPersonalDetailChanged || editProfileState.value.isPrimaryInsuranceChanged || editProfileState.value.isSecondaryInsuranceChanged) {
                    _showDiscardDialog.update {
                        true
                    }
                } else {
                    viewModelScope.launch {
                        navigationEvents.emit(CustomNavigationEvent.Back)
                    }
                }
            }

            EditProfileAction.DismissDiscardDialog -> {
                _showDiscardDialog.update {
                    false
                }
            }

            EditProfileAction.ClearToastMessage -> {
                _editProfileState.update {
                    it.copy(toastMessage = null)
                }
            }

            EditProfileAction.ClearError -> {
                clearError()
            }

            EditProfileAction.RetryLastFailedAction -> {
                retryLastFailedAction()
            }

            is EditProfileAction.DeleteInsuranceCardBack -> {
                val insuranceId =
                    if (editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE) editProfileState.value.primaryInsurance.patientInsuranceId else editProfileState.value.secondaryInsurance.patientInsuranceId
                deleteInsuranceCardBack(
                    insuranceId ?: "",
                    isPrimaryInsurance = editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE
                )
            }

            is EditProfileAction.DeleteInsuranceCardFront -> {
                val insuranceId =
                    if (editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE) editProfileState.value.primaryInsurance.patientInsuranceId else editProfileState.value.secondaryInsurance.patientInsuranceId
                deleteInsuranceCardFront(
                    insuranceId ?: "",
                    isPrimaryInsurance = editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE
                )
            }

            EditProfileAction.OnClickInsuranceCardBack -> {}
            EditProfileAction.OnClickInsuranceCardFront -> {}
            EditProfileAction.OnRetryInsuranceCardBack -> {
                var insuranceId: String
                var imageFilePath: String
                if (editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE) {
                    insuranceId = editProfileState.value.primaryInsurance.patientInsuranceId ?: ""
                    imageFilePath =
                        editProfileState.value.primaryInsurance.insuranceCardBack.cacheFilePath
                            ?: ""
                } else {
                    insuranceId = editProfileState.value.secondaryInsurance.patientInsuranceId ?: ""
                    imageFilePath =
                        editProfileState.value.secondaryInsurance.insuranceCardBack.cacheFilePath
                            ?: ""
                }
                postPatientInsuranceCardBack(
                    imageFilePath, insuranceId,
                    isPrimaryInsurance = editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE
                )
            }

            EditProfileAction.OnRetryInsuranceCardFront -> {
                var insuranceId: String
                var imageFilePath: String
                if (editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE) {
                    insuranceId = editProfileState.value.primaryInsurance.patientInsuranceId ?: ""
                    imageFilePath =
                        editProfileState.value.primaryInsurance.insuranceCardFront.cacheFilePath
                            ?: ""
                } else {
                    insuranceId = editProfileState.value.secondaryInsurance.patientInsuranceId ?: ""
                    imageFilePath =
                        editProfileState.value.secondaryInsurance.insuranceCardFront.cacheFilePath
                            ?: ""
                }
                postPatientInsuranceCardFront(
                    imageFilePath, insuranceId,
                    isPrimaryInsurance = editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE
                )
            }

            EditProfileAction.OnDeleteInsuranceCardCacheBack -> {
                if (editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE) {
                    _editProfileState.update {
                        it.copy(
                            primaryInsurance = it.primaryInsurance.copy(
                                insuranceCardBack = it.primaryInsurance.insuranceCardBack.copy(
                                    cacheFilePath = null,
                                    isServerUploaded = true
                                )
                            )
                        )
                    }
                } else {
                    _editProfileState.update {
                        it.copy(
                            secondaryInsurance = it.secondaryInsurance.copy(
                                insuranceCardBack = it.secondaryInsurance.insuranceCardBack.copy(
                                    cacheFilePath = null,
                                    isServerUploaded = true,
                                )
                            )
                        )
                    }
                }
            }

            EditProfileAction.OnDeleteInsuranceCardCacheFront -> {
                if (editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE) {
                    _editProfileState.update {
                        it.copy(
                            primaryInsurance = it.primaryInsurance.copy(
                                insuranceCardFront = it.primaryInsurance.insuranceCardFront.copy(
                                    cacheFilePath = null,
                                    isServerUploaded = true
                                )
                            )
                        )
                    }
                } else {
                    _editProfileState.update {
                        it.copy(
                            secondaryInsurance = it.secondaryInsurance.copy(
                                insuranceCardFront = it.secondaryInsurance.insuranceCardFront.copy(
                                    cacheFilePath = null,
                                    isServerUploaded = true
                                )
                            )
                        )
                    }
                }
            }

            EditProfileAction.OnClickProfilePicture -> {}
            EditProfileAction.OnClickDialogSave -> {
                _editProfileState.update {
                    it.copy(
                        isFromFinalSave = true
                    )
                }
                onSaveSectionData()
            }

            EditProfileAction.ClearFocusedPersonalField -> {
                _editProfileState.update {
                    it.copy(
                        focusedPersonalField = null
                    )
                }
            }

            EditProfileAction.ClearFocusedContactField -> {
                _editProfileState.update {
                    it.copy(
                        focusedContactField = null
                    )
                }
            }

            EditProfileAction.ClearFocusedPCaregiverField -> {
                _editProfileState.update {
                    it.copy(
                        focusedPCaregiverField = null
                    )
                }
            }

            EditProfileAction.ClearFocusedSCaregiverField -> {
                _editProfileState.update {
                    it.copy(
                        focusedSCaregiverField = null
                    )
                }
            }

            EditProfileAction.ClearFocusedInsuranceField -> {
                _editProfileState.update {
                    it.copy(
                        focusedInsuranceField = null
                    )
                }
            }
        }
    }

    private fun onSaveSectionData() {
        if (editProfileState.value.selectedSection == Section.PERSONAL_INFO && editProfileState.value.isPersonalDetailChanged) {
            if (!personalDetailValidation()) {
                _editProfileState.update {
                    it.copy(
                        isFromFinalSave = false
                    )
                }
                return
            }
            savePatient()
        } else if (editProfileState.value.selectedSection == Section.CONTACT_INFO && editProfileState.value.isPersonalDetailChanged) {
            if (!validateContactInformation()) {
                _editProfileState.update {
                    it.copy(
                        isFromFinalSave = false
                    )
                }
                return
            }
            savePatient()
        } else if (editProfileState.value.selectedSection == Section.CAREGIVER_DETAILS && editProfileState.value.isPersonalDetailChanged) {
            if (!validateCaregiverDetails()) {
                _editProfileState.update {
                    it.copy(
                        isFromFinalSave = false
                    )
                }
                return
            }
            savePatient()
        } else if ((editProfileState.value.selectedSection == Section.SECONDARY_INSURANCE && editProfileState.value.isSecondaryInsuranceChanged)) {
            saveSecondaryInsurance()
        } else if (editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE && editProfileState.value.isPrimaryInsuranceChanged) {
            savePrimaryInsurance()
        }
    }


    private fun onClickSave(section: Section) {
        when (section) {
            Section.PERSONAL_INFO -> {

            }

            Section.CONTACT_INFO, Section.EMERGENCY_CONTACT -> {

            }

            Section.CAREGIVER_DETAILS -> {
                if (!validateCaregiverDetails()) {
                    return
                }
                if (editProfileState.value.isPersonalDetailChanged) {
                    savePatient()
                } else {
                    if (editProfileState.value.isInsuranceRequired) {
                        _editProfileState.update {
                            it.copy(
                                selectedSection = Section.PRIMARY_INSURANCE,
                                isShowSkip = true,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    } else {
                        viewModelScope.launch {
                            _navigationEvents.emit(CustomNavigationEvent.Back)
                        }
                    }
                }

            }

            Section.PRIMARY_INSURANCE -> {
                savePrimaryInsurance()
            }

            Section.SECONDARY_INSURANCE -> {
                saveSecondaryInsurance()
            }
        }
    }

    fun getPayerDetails(query: String? = null, isPrimary: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = patientInsuranceUseCases.getPayerDetails(query)) {
                is APIState.Success -> {

                    val payerList = (result.data as? List<*>)?.filterIsInstance<PracticePayer>()
                    if (payerList != null) {
                        _editProfileState.update {
                            if (isPrimary) {
                                it.copy(primaryInsurance = it.primaryInsurance.copy(payerNameOptions = payerList))
                            } else {
                                it.copy(
                                    secondaryInsurance = it.secondaryInsurance.copy(
                                        payerNameOptions = payerList
                                    )
                                )
                            }
                        }
                    } else {
                        // Handle invalid data gracefully
                        _editProfileState.update {
                            it.copy(errorMessage = StrResId(R.string.errorOccurred))
                        }
                    }
                }

                is APIState.Error -> {
                    lastFailedAction = {
                        getPayerDetails(query, isPrimary)
                    }
                    _editProfileState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.apiResponseError.errorMsg
                        )
                    }
                }

                APIState.Loading -> {

                }

            }
        }
    }

    //region Insurance initial data set up
    fun setInsuranceFromRepository() {
        val primaryInsurance = patientDetailRepository.patient.value?.primaryInsurance
        val secondaryInsurance = patientDetailRepository.patient.value?.secondaryInsurance
        primaryInsurance?.let {
            _editProfileState.update {
                it.copy(
                    primaryInsurance = primaryInsurance,
                )
            }
            primaryInsurance.patientInsuranceId?.let {
                getPatientInsuranceCardFront(
                    primaryInsurance.patientInsuranceId,
                    true
                )
                getPatientInsuranceCardBack(
                    primaryInsurance.patientInsuranceId,
                    true
                )
            }

        }
        secondaryInsurance?.let {
            _editProfileState.update {
                it.copy(
                    secondaryInsurance = secondaryInsurance,
                )
            }
            secondaryInsurance.patientInsuranceId?.let {
                getPatientInsuranceCardFront(
                    secondaryInsurance.patientInsuranceId,
                    true
                )
                getPatientInsuranceCardBack(
                    secondaryInsurance.patientInsuranceId,
                    true
                )
            }
        }


        if (editProfileState.value.primaryInsurance.insuredPersonDetail?.relationship == null) {
            // assign self if relationship is null
            // Copy patient details to insuredPersonDetail
            _editProfileState.update {
                it.copy(
                    primaryInsurance = updateInsuredPersonDetailFromPersonalDetails(
                        editProfileState.value.primaryInsurance
                    )
                )
            }
        }

        // assign self if relationship is null
        if (editProfileState.value.secondaryInsurance.insuredPersonDetail?.relationship == null) {
            _editProfileState.update {
                it.copy(
                    secondaryInsurance = updateInsuredPersonDetailFromPersonalDetails(
                        editProfileState.value.secondaryInsurance
                    )
                )
            }
        }
        updateInsuranceMandatoryFields()
        _editProfileState.update {
            it.copy(
                isLoading = false
            )
        }
    }

    fun updateInsuranceMandatoryFields() {
        _editProfileState.update { state ->
            state.copy(
                primaryInsurance = state.primaryInsurance.copy(
                    insuranceType = state.primaryInsurance.insuranceType.copy(isMandatory = true),
                    planOrProgramName = state.primaryInsurance.planOrProgramName.copy(
                        isMandatory = true
                    ),
                    planId = state.primaryInsurance.planId.copy(isMandatory = true),
                    selectedPayer = state.primaryInsurance.selectedPayer.copy(isMandatory = true),
                    insuredPersonDetail = state.primaryInsurance.insuredPersonDetail?.copy(
                        relationship = state.primaryInsurance.insuredPersonDetail.relationship.copy(
                            isMandatory = true
                        ),
                        firstName = state.primaryInsurance.insuredPersonDetail.firstName.copy(
                            isMandatory = true
                        ),
                        lastName = state.primaryInsurance.insuredPersonDetail.lastName.copy(
                            isMandatory = true
                        ),
                        dateOfBirth = state.primaryInsurance.insuredPersonDetail.dateOfBirth.copy(
                            isMandatory = true
                        )
                    )
                ),
                secondaryInsurance = state.secondaryInsurance.copy(
                    insuranceType = state.secondaryInsurance.insuranceType.copy(isMandatory = true),
                    planOrProgramName = state.secondaryInsurance.planOrProgramName.copy(
                        isMandatory = true
                    ),
                    planId = state.secondaryInsurance.planId.copy(isMandatory = true),
                    selectedPayer = state.secondaryInsurance.selectedPayer.copy(isMandatory = true),
                    insuredPersonDetail = state.secondaryInsurance.insuredPersonDetail?.copy(
                        relationship = state.secondaryInsurance.insuredPersonDetail.relationship.copy(
                            isMandatory = true
                        ),
                        firstName = state.secondaryInsurance.insuredPersonDetail.firstName.copy(
                            isMandatory = true
                        ),
                        lastName = state.secondaryInsurance.insuredPersonDetail.lastName.copy(
                            isMandatory = true
                        ),
                        dateOfBirth = state.secondaryInsurance.insuredPersonDetail.dateOfBirth.copy(
                            isMandatory = true
                        )
                    )
                )
            )
        }
    }

    private fun updateInsuredPersonDetailFromPersonalDetails(insurance: Insurance): Insurance {
        val patient = editProfileState.value.personalInformation
        val contactDetails = editProfileState.value.contactInformation
        val insuredPersonDetail = insurance.insuredPersonDetail ?: PersonDetail()
        return insurance.copy(
            insuredPersonDetail = insuredPersonDetail.copy(
                firstName = patient.firstName,
                lastName = patient.lastName,
                dateOfBirth = patient.dateOfBirth,
                birthSex = patient.birthSex,
                addressLine1 = contactDetails.addressLine1,
                addressLine2 = contactDetails.addressLine2,
                city = contactDetails.city,
                state = contactDetails.state,
                country = contactDetails.country,
                postalCode = contactDetails.postalCode,
                mobilePhone = contactDetails.mobilePhone,
                homePhone = contactDetails.homePhone,
                relationship = insuredPersonDetail.relationship
            )
        )
    }
    //endregion


    //region Insurance Save/Update
    fun savePrimaryInsurance() {
        if (!validatePrimaryInsurance()) {
            _editProfileState.update {
                it.copy(
                    isFromFinalSave = false
                )
            }
            return

        }
        if (_editProfileState.value.primaryInsurance.insuranceID.value.isNullOrBlank()) {
            _editProfileState.update {
                it.copy(
                    primaryInsurance = it.primaryInsurance.copy(
                        insuranceCategory = it.primaryInsurance.insuranceCategory.copy(
                            value = ModelUtil.primaryInsurance
                        )
                    )
                )
            }
            addPatientInsurances(
                _editProfileState.value.primaryInsurance,
                isPrimaryInsurance = true
            )
        } else {
            updatePatientInsurances(_editProfileState.value.primaryInsurance, true)
        }
    }

    fun saveSecondaryInsurance() {
        if (!validateSecondaryInsurance()) {
            _editProfileState.update {
                it.copy(
                    isFromFinalSave = false
                )
            }
            return

        }
        if (_editProfileState.value.secondaryInsurance.insuranceID.value.isNullOrBlank()) {
            _editProfileState.update {
                it.copy(
                    secondaryInsurance = it.secondaryInsurance.copy(
                        insuranceCategory = it.secondaryInsurance.insuranceCategory.copy(
                            value = ModelUtil.secondaryInsurance
                        )
                    )
                )
            }
            addPatientInsurances(
                _editProfileState.value.secondaryInsurance,
                isPrimaryInsurance = false
            )
        } else {
            updatePatientInsurances(_editProfileState.value.secondaryInsurance, false)
        }
    }

    fun onSuccessUpdateInsurance(
        isPrimaryInsurance: Boolean,
        insuranceId: String,
        isFromAdd: Boolean,
        insuranceData: Insurance
    ) {
        val frontCard = insuranceData.insuranceCardFront
        val backCard = insuranceData.insuranceCardBack
        getPatientInsurances(isPrimaryInsurance, insuranceId) { insurance ->
            var updatedInsurance = insurance.copy(
                insuranceCardFront = insurance.insuranceCardFront.copy(
                    cacheFilePath = frontCard.cacheFilePath,
                    filePath = frontCard.filePath,
                    isServerUploaded = frontCard.isServerUploaded,
                    isUploading = frontCard.isUploading,
                    error = frontCard.error
                )
            )
            updatedInsurance =
                updatedInsurance.copy(
                    insuranceCardBack = updatedInsurance.insuranceCardBack.copy(
                        cacheFilePath = backCard.cacheFilePath,
                        filePath = backCard.filePath,
                        isServerUploaded = backCard.isServerUploaded,
                        isUploading = backCard.isUploading,
                        error = backCard.error
                    )
                )
            if (isPrimaryInsurance) {
                _editProfileState.update {
                    it.copy(
                        primaryInsurance = updatedInsurance, isLoading = false,
                        errorMessage = null,
                        toastMessage = if (isFromAdd) R.string.primary_insurance_added else R.string.primary_insurance_updated,
                        isPrimaryInsuranceChanged = false,
                        isSecondaryInsuranceChanged = false
                    )
                }
            } else {
                _editProfileState.update {
                    it.copy(
                        secondaryInsurance = updatedInsurance, isLoading = false,
                        errorMessage = null,
                        toastMessage = if (isFromAdd) R.string.secondary_insurance_added else R.string.secondary_insurance_updated,
                        isPrimaryInsuranceChanged = false,
                        isSecondaryInsuranceChanged = false
                    )
                }
            }
            if (editProfileState.value.isFromFinalSave) {
                viewModelScope.launch {
                    _navigationEvents.emit(CustomNavigationEvent.Back)
                }
            } else {
                if (pendingSectionChange != null) {
                    proceedPendingSectionChange()
                } else if (isPrimaryInsurance) {
                    updateSelectedSection(Section.SECONDARY_INSURANCE)
                } else {
                    viewModelScope.launch {
                        _navigationEvents.emit(CustomNavigationEvent.Back)
                    }
                }
            }
        }
    }

    fun addPatientInsurances(
        insurance: Insurance,
        isPrimaryInsurance: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _editProfileState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = patientInsuranceUseCases.addPatientInsurances(
                selectedPatient.value?.practicePatientId ?: "", insurance
            )
            when (result) {
                is APIState.Error -> {
                    lastFailedAction = {
                        addPatientInsurances(insurance, isPrimaryInsurance)
                    }
                    _editProfileState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.apiResponseError.errorMsg
                        )
                    }
                }

                APIState.Loading -> {}
                is APIState.Success<*> -> {
                    val response = result.data as? Pair<*, *>
                    val isSuccess = response?.first as? Boolean
                    val responseData = response?.second as? String
                    if (isSuccess == true && responseData != null) {
                        uploadInsuranceCardsWithRetry(insurance, responseData, isPrimaryInsurance)
                    } else {
                        _editProfileState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = StrResId(R.string.request_failed_try_again)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun uploadInsuranceCardsWithRetry(
        insurance: Insurance,
        insuranceId: String,
        isPrimaryInsurance: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val needsFront =
                !insurance.insuranceCardFront.isServerUploaded && insurance.insuranceCardFront.cacheFilePath != null
            val needsBack =
                !insurance.insuranceCardBack.isServerUploaded && insurance.insuranceCardBack.cacheFilePath != null
            var frontSuccess = true
            var backSuccess = true
            try {
                val frontDeferred = if (needsFront) async {
                    val res = insuranceCardUseCases.postInsuranceCardFront(
                        insurance.insuranceCardFront.cacheFilePath,
                        selectedPatient.value?.practicePatientId ?: "", insuranceId
                    )
                    frontSuccess = res is APIState.Success<*> && (res.data as? Boolean == true)
                } else null
                val backDeferred = if (needsBack) async {
                    val res = insuranceCardUseCases.postInsuranceCardBack(
                        insurance.insuranceCardBack.cacheFilePath,
                        selectedPatient.value?.practicePatientId ?: "", insuranceId
                    )
                    backSuccess = res is APIState.Success<*> && (res.data as? Boolean == true)
                } else null
                frontDeferred?.await()
                backDeferred?.await()
            } catch (e: Exception) {
                frontSuccess = false
                backSuccess = false
            }
            if (frontSuccess && backSuccess) {
                val imageFile = insurance.insuranceCardFront.cacheFilePath
                var updatedInsurance = insurance.copy(
                    insuranceCardFront = insurance.insuranceCardFront.copy(
                        cacheFilePath = null,
                        filePath = imageFile,
                        isServerUploaded = true,
                        isUploading = false,
                    )
                )
                val imageFileBack = insurance.insuranceCardBack.cacheFilePath
                updatedInsurance = updatedInsurance.copy(
                    insuranceCardBack = updatedInsurance.insuranceCardBack.copy(
                        cacheFilePath = null,
                        filePath = imageFileBack,
                        isServerUploaded = true,
                        isUploading = false,
                    )
                )
                onSuccessUpdateInsurance(
                    isPrimaryInsurance,
                    insuranceId,
                    isFromAdd = true,
                    updatedInsurance
                )
            } else {
                lastFailedAction = {
                    uploadInsuranceCardsWithRetry(insurance, insuranceId, isPrimaryInsurance)
                }
                _editProfileState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = StrResId(R.string.request_failed_try_again)
                    )
                }
            }
        }
    }

    fun updatePatientInsurances(insurance: Insurance, isPrimaryInsurance: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = patientInsuranceUseCases.updatePatientInsurances(
                selectedPatient.value?.practicePatientId ?: "", insurance
            )
            when (result) {
                is APIState.Error -> {
                    lastFailedAction = {
                        updatePatientInsurances(insurance, isPrimaryInsurance)
                    }
                    _editProfileState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.apiResponseError.errorMsg
                        )
                    }
                }

                is APIState.Success<*> -> {
                    val isAdded = result.data as Boolean
                    if (isAdded) {
                        onSuccessUpdateInsurance(
                            isPrimaryInsurance = isPrimaryInsurance,
                            insuranceId = insurance.insuranceID.value
                                ?: "",
                            isFromAdd = false,
                            insuranceData = insurance
                        )
                    } else {
                        _editProfileState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = StrResId(R.string.request_failed_try_again)
                            )
                        }
                    }
                }

                else -> {
                }
            }
        }
    }
    //endregion


    //region Initial Data Set Up for Personal Information, Contact Information and Caregiver Details
    fun setPersonalInformationFromPatient() {
        val patient = selectedPatient.value ?: return
        val metrics = _editProfileState.value.demographicsMetrics
        val demographicsSetting = _editProfileState.value.demographicsSetting
        fun isFieldMandatory(fieldName: String): Boolean {
            return demographicsSetting?.firstOrNull { it.nodeIdentifier == fieldName }?.mandatory == true
        }

        val matchedGenderIdentity = metrics?.genderIdentity?.firstOrNull {
            it.value.equals(patient.genderIdentity, ignoreCase = true)
        }
        val patientRaceList =
            patient.race?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }
                ?: emptyList()
        val matchedRaceList = metrics?.race?.filter { metricRace ->
            patientRaceList.any { it.equals(metricRace.value, ignoreCase = true) }
        }
        val finalMatchedRaceList = if (
            patientRaceList.isNotEmpty() &&
            matchedRaceList?.isEmpty() == true &&
            patientRaceList.size == 1 &&
            patientRaceList[0].equals("Declined to specify", ignoreCase = true)
        ) {
            mutableListOf(DmRace(value = "Declined to specify", key = "Declined to specify"))
        } else {
            matchedRaceList
        }
        val patientEthnicityList =
            patient.ethnicity?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }
                ?: emptyList()
        val matchedEthnicityList = metrics?.ethnicity?.filter { metricEthnicity ->
            patientEthnicityList.any { it.equals(metricEthnicity.value, ignoreCase = true) }
        }
        val finalMatchedEthnicityList = if (
            patientEthnicityList.isNotEmpty() &&
            matchedEthnicityList?.isEmpty() == true &&
            patientEthnicityList.size == 1 &&
            patientEthnicityList[0].equals("Declined to specify", ignoreCase = true)
        ) {
            mutableListOf(
                DmEthnicity(
                    value = "Declined to specify",
                    key = "Declined to specify"
                )
            )
        } else {
            matchedEthnicityList
        }
        val matchedLanguage = metrics?.languages?.firstOrNull {
            it.value.equals(patient.language, ignoreCase = true)
        }
        val profileImageUrl = if (patient.isPhotoAvailableOnline) {
            MDAccountsRelatedUtil.getPatientPhotoUrl(patient.practicePatientId)
        } else null
        _editProfileState.update {
            it.copy(
                personalInformation = it.personalInformation.copy(
                    firstName = FieldState(
                        value = patient.firstName ?: "",
                        isMandatory = isFieldMandatory("first_name")
                    ),
                    lastName = FieldState(
                        value = patient.lastName ?: "",
                        isMandatory = isFieldMandatory("last_name")
                    ),
                    dateOfBirth = FieldState(
                        value = patient.dateOfBirth?.toString(ModelUtil.mothFirstDateFormatWithSlash())
                            ?: "",
                        isMandatory = isFieldMandatory("dob")
                    ),
                    birthSex = FieldState(
                        value = patient.gender ?: "",
                        isMandatory = isFieldMandatory("gender")
                    ),
                    genderIdentity = FieldState(
                        value = matchedGenderIdentity,
                        isMandatory = isFieldMandatory("gender_identity")
                    ),
                    bloodGroup = FieldState(
                        value = patient.bloodGroup ?: "",
                        isMandatory = isFieldMandatory("blood_group")
                    ),
                    race = FieldState(
                        value = finalMatchedRaceList,
                        isMandatory = isFieldMandatory("race")
                    ),
                    ethnicity = FieldState(
                        value = finalMatchedEthnicityList,
                        isMandatory = isFieldMandatory("ethnicity")
                    ),
                    language = FieldState(
                        value = matchedLanguage,
                        isMandatory = isFieldMandatory("language")
                    ),
                    smokingStatus = FieldState(
                        value = patient.smokingStatus ?: "",
                        isMandatory = isFieldMandatory("smoking_status")
                    ),
                    maritalStatus = FieldState(
                        value = patient.maritalStatus ?: "",
                        isMandatory = isFieldMandatory("marital_status")
                    ),
                    employmentStatus = FieldState(
                        value = patient.employmentStatus ?: "",
                        isMandatory = isFieldMandatory("employment_status")
                    ),
                    profileImageFilePath = UploadState(
                        filePath = profileImageUrl,
                        isServerUploaded = patient.isPhotoAvailableOnline
                    )
                )
            )
        }
    }

    fun setContactInformationFromPatient() {
        val patient = selectedPatient.value ?: return
        val countryList = _editProfileState.value.countryList
        val demographicsSetting = _editProfileState.value.demographicsSetting
        fun isFieldMandatory(fieldName: String): Boolean {
            return demographicsSetting?.firstOrNull { it.nodeIdentifier == fieldName }?.mandatory == true
        }
        val country = patient.postalAddress?.country
            ?.takeIf { it.isNotBlank() }
            ?.let { countryValue ->
                countryList.firstOrNull { country -> country.value == countryValue }
            }
            ?: authManager.kioskSettingsFlow.value?.country?.let { authCountryValue ->
                countryList.firstOrNull { country -> country.value == authCountryValue }
            }

        _editProfileState.update {
            it.copy(
                contactInformation = it.contactInformation.copy(
                    preferredCommunication = FieldState(
                        value = patient.contactDetails?.preferredCommunication ?: "",
                        isMandatory = isFieldMandatory("preferred_communication")
                    ),
                    email = FieldState(
                        value = patient.contactDetails?.emailId ?: "",
                        isMandatory = isFieldMandatory("email") || (!patient.contactDetails?.emailId.isNullOrBlank())
                    ),
                    primaryPhone = FieldState(
                        value = patient.contactDetails?.primaryPhone ?: "",
                        isMandatory = isFieldMandatory("primary_phone")
                    ),
                    mobilePhone = FieldState(
                        value = patient.contactDetails?.mobilePhone ?: "",
                        isMandatory = isFieldMandatory("mobile_phone") || (patient.contactDetails?.primaryPhone.equals(
                            "Mobile Phone"
                        ))
                    ),
                    homePhone = FieldState(
                        value = patient.contactDetails?.homePhone ?: "",
                        isMandatory = isFieldMandatory("home_phone") || (patient.contactDetails?.primaryPhone.equals(
                            "Home Phone"
                        ))
                    ),
                    workPhone = FieldState(
                        value = patient.contactDetails?.workPhone ?: "",
                        isMandatory = isFieldMandatory("work_phone") || (patient.contactDetails?.primaryPhone.equals(
                            "Work Phone"
                        ))
                    ),
                    workPhoneExtn = FieldState(
                        value = patient.contactDetails?.workPhoneExtn ?: "",
                        isMandatory = isFieldMandatory("work_phone_extn")
                    ),
                    addressLine1 = FieldState(
                        value = patient.postalAddress?.line1 ?: "",
                        isMandatory = isFieldMandatory("address_line1")
                    ),
                    addressLine2 = FieldState(
                        value = patient.postalAddress?.line2 ?: "",
                        isMandatory = isFieldMandatory("address_line2")
                    ),
                    city = FieldState(
                        value = patient.postalAddress?.city ?: "",
                        isMandatory = isFieldMandatory("city")
                    ),
                    country = FieldState(
                        value = country,
                        isMandatory = isFieldMandatory("country")
                    ),
                    postalCode = FieldState(
                        value = patient.postalAddress?.postalCode ?: "",
                        isMandatory = isFieldMandatory("postal_code")
                    ),
                    emergencyContactName = FieldState(
                        value = patient.emergencyContactName ?: "",
                        isMandatory = isFieldMandatory("emergency_contact_name")
                    ),
                    emergencyContactNumber = FieldState(
                        value = patient.emergencyContactNumber ?: "",
                        isMandatory = isFieldMandatory("emergency_contact_number")
                    ),
                    emergencyContactExtn = FieldState(
                        value = patient.emergencyContactExtn ?: "",
                        isMandatory = isFieldMandatory("emergency_contact_extn")
                    )
                )
            )
        }
        val countryKey = country?.value
        val stateMap = _editProfileState.value.countryStateMap
        if (!countryKey.isNullOrBlank() && stateMap.containsKey(countryKey)) {
            val states = stateMap[countryKey] ?: emptyList()
            setStateInContactInformation(states)
        }
    }

    fun setStateInContactInformation(states: List<State>) {
        val selectedStateValue = selectedPatient.value?.postalAddress?.state
        _editProfileState.update {
            it.copy(
                contactInformation = it.contactInformation.copy(
                    state = it.contactInformation.state.copy(
                        value = states.firstOrNull { state -> state.value == selectedStateValue }
                    ),
                    stateList = states
                )
            )
        }
    }

    fun setCaregiverDetailsFromPatient() {
        val patient = selectedPatient.value ?: return
        // Find primary and secondary caregivers
        val primaryCaregiver = patient.caregivers.firstOrNull { it.type == 0 }
        val secondaryCaregiver = patient.caregivers.firstOrNull { it.type == 1 }

        // Helper to build state for a caregiver
        fun buildCaregiverState(caregiver: Caregiver?): PersonDetail? {
            if (caregiver == null) return null
            val countryKey = caregiver.postalAddress?.country
            val stateMap = _editProfileState.value.countryStateMap
            var stateList = emptyList<State>()
            if (!countryKey.isNullOrBlank() && stateMap.containsKey(countryKey)) {
                stateList = stateMap[countryKey] ?: emptyList()
            }
            return PersonDetail(
                firstName = FieldState(caregiver.firstName ?: "", isMandatory = true),
                lastName = FieldState(caregiver.lastName ?: "", isMandatory = true),
                birthSex = FieldState(caregiver.gender ?: ""),
                dateOfBirth = FieldState(caregiver.dateOfBirth),
                relationship = FieldState(
                    getCaregiverRelationship(key = caregiver.relationship),
                    isMandatory = true
                ),
                addressLine1 = FieldState(caregiver.postalAddress?.line1 ?: ""),
                addressLine2 = FieldState(caregiver.postalAddress?.line2 ?: ""),
                city = FieldState(caregiver.postalAddress?.city ?: ""),
                state = FieldState(stateList.firstOrNull { state -> caregiver.postalAddress?.state == state.value }),
                country = FieldState(_editProfileState.value.countryList.firstOrNull { c -> caregiver.postalAddress?.country == c.value }),
                postalCode = FieldState(caregiver.postalAddress?.postalCode ?: ""),
                mobilePhone = FieldState(caregiver.contactDetails?.mobilePhone ?: ""),
                homePhone = FieldState(caregiver.contactDetails?.homePhone ?: ""),
                isCopyPatientAddress = caregiver.sameAsPatContact == true,
            )
        }

        _editProfileState.update {
            it.copy(
                primaryCaregiverDetails = buildCaregiverState(primaryCaregiver),
                secondaryCaregiverDetails = buildCaregiverState(secondaryCaregiver)
            )
        }
    }

    fun getCaregiverRelationship(key: String?): DmCaregiverRelationship? {
        if (key.isNullOrBlank()) return null
        return _editProfileState.value.demographicsMetrics?.caregiverRelationship?.firstOrNull {
            it.key == key
        }

    }
    //endregion


    //region Validate Insurance
    fun <T : Enum<T>> validateInsurance(
        insurance: Insurance,
        setFocusField: (InsuranceField?) -> Unit,
        updateInsuranceState: (Insurance) -> Unit
    ): Boolean {
        var hasError = false
        var firstErrorField: InsuranceField? = null
        val updatedInsurance = insurance.copy(
            insuranceType = if (insurance.insuranceType.isMandatory && insurance.insuranceType.value.isNullOrBlank()) {
                hasError = true
                firstErrorField = InsuranceField.INSURANCE_TYPE
                insurance.insuranceType.copy(error = StrResId(R.string.mandatory_field_msg))
            } else insurance.insuranceType.copy(error = null),
            planOrProgramName = if (insurance.planOrProgramName.isMandatory && insurance.planOrProgramName.value.isNullOrBlank()) {
                hasError = true
                if (firstErrorField == null) firstErrorField =
                    InsuranceField.PLAN_OR_PROGRAM_NAME
                insurance.planOrProgramName.copy(error = StrResId(R.string.mandatory_field_msg))
            } else insurance.planOrProgramName.copy(error = null),
            planId = if (insurance.planId.isMandatory && insurance.planId.value.isNullOrBlank()) {
                hasError = true
                if (firstErrorField == null) firstErrorField = InsuranceField.PLAN_ID
                insurance.planId.copy(error = StrResId(R.string.mandatory_field_msg))
            } else insurance.planId.copy(error = null),
            selectedPayer = if (insurance.selectedPayer.isMandatory && insurance.selectedPayer.value == null) {
                hasError = true
                if (firstErrorField == null) firstErrorField = InsuranceField.PAYER_NAME
                insurance.selectedPayer.copy(error = StrResId(R.string.mandatory_field_msg))
            } else insurance.selectedPayer.copy(error = null),
            validFrom = if (!insurance.validFrom.value.isNullOrBlank() && !insurance.validUntil.value.isNullOrBlank()) {
                val formatter = ModelUtil.mothFirstDateFormatWithSlash()
                val validFrom =
                    DateUtil.getDate(insurance.validFrom.value, formatter)
                val validUntil =
                    DateUtil.getDate(insurance.validUntil.value, formatter)
                if (validFrom == null) {
                    hasError = true
                    if (firstErrorField == null) firstErrorField = InsuranceField.VALID_FROM
                    insurance.validFrom.copy(error = StrResId(R.string.enter_valid_date))
                } else if (validUntil == null) {
                    hasError = true
                    if (firstErrorField == null) firstErrorField = InsuranceField.VALID_UNTIL
                    insurance.validUntil.copy(error = StrResId(R.string.enter_valid_date))
                } else {
                    if (DateUtil.isDateBefore(
                            insurance.validFrom.value,
                            insurance.validUntil.value,
                            formatter
                        )
                    ) {
                        insurance.validFrom.copy(error = null)
                    } else {
                        hasError = true
                        if (firstErrorField == null) firstErrorField = InsuranceField.VALID_FROM
                        insurance.validFrom.copy(error = StrResId(R.string.valid_from_date_error))
                    }
                }
            } else insurance.validFrom.copy(error = null),
            insuredPersonDetail = insurance.insuredPersonDetail?.copy(
                relationship = if (insurance.insuredPersonDetail.relationship.isMandatory && insurance.insuredPersonDetail.relationship.value == null) {
                    hasError = true
                    if (firstErrorField == null) firstErrorField = InsuranceField.RELATIONSHIP
                    insurance.insuredPersonDetail.relationship.copy(error = StrResId(R.string.mandatory_field_msg))
                } else insurance.insuredPersonDetail.relationship.copy(error = null),
                firstName = if (insurance.insuredPersonDetail.firstName.isMandatory && insurance.insuredPersonDetail.firstName.value.isNullOrBlank()) {
                    hasError = true
                    if (firstErrorField == null) firstErrorField = InsuranceField.FIRST_NAME
                    insurance.insuredPersonDetail.firstName.copy(error = StrResId(R.string.mandatory_field_msg))
                } else if (insurance.insuredPersonDetail.firstName.value?.isNotBlank() == true && !insurance.insuredPersonDetail.firstName.value.any { it.isLetter() }) {
                    hasError = true
                    if (firstErrorField == null) firstErrorField = InsuranceField.FIRST_NAME
                    insurance.insuredPersonDetail.firstName.copy(error = StrResId(R.string.one_alphabet_name))
                } else insurance.insuredPersonDetail.firstName.copy(error = null),
                lastName = if (insurance.insuredPersonDetail.lastName.isMandatory && insurance.insuredPersonDetail.lastName.value.isNullOrBlank()) {
                    hasError = true
                    if (firstErrorField == null) firstErrorField = InsuranceField.LAST_NAME
                    insurance.insuredPersonDetail.lastName.copy(error = StrResId(R.string.mandatory_field_msg))
                } else if (insurance.insuredPersonDetail.lastName.value?.isNotBlank() == true && !insurance.insuredPersonDetail.lastName.value.any { it.isLetter() }) {
                    hasError = true
                    if (firstErrorField == null) firstErrorField = InsuranceField.LAST_NAME
                    insurance.insuredPersonDetail.lastName.copy(error = StrResId(R.string.one_alphabet_name))
                } else insurance.insuredPersonDetail.lastName.copy(error = null),
                dateOfBirth = if (insurance.insuredPersonDetail.dateOfBirth.isMandatory && insurance.insuredPersonDetail.dateOfBirth.value.isNullOrBlank()) {
                    hasError = true
                    if (firstErrorField == null) firstErrorField = InsuranceField.DATE_OF_BIRTH
                    insurance.insuredPersonDetail.dateOfBirth.copy(error = StrResId(R.string.mandatory_field_msg))
                } else if (!insurance.insuredPersonDetail.dateOfBirth.value.isNullOrBlank()) {
                    val formatter = ModelUtil.mothFirstDateFormatWithSlash()
                    val date =
                        DateUtil.getDate(insurance.insuredPersonDetail.dateOfBirth.value, formatter)
                    if (date == null || DateUtil.isDateAfterToday(
                            insurance.insuredPersonDetail.dateOfBirth.value,
                            formatter
                        )
                    ) {
                        hasError = true
                        if (firstErrorField == null) firstErrorField =
                            InsuranceField.DATE_OF_BIRTH
                        insurance.insuredPersonDetail.dateOfBirth.copy(error = StrResId(R.string.dob_invalid))
                    } else {
                        insurance.insuredPersonDetail.dateOfBirth.copy(error = null)
                    }
                } else insurance.insuredPersonDetail.dateOfBirth.copy(error = null)
            )
        )
        if (hasError) {
            updateInsuranceState(updatedInsurance)
            setFocusField(firstErrorField)
            return false
        }
        return true
    }

    fun validatePrimaryInsurance(): Boolean {
        return validateInsurance(
            editProfileState.value.primaryInsurance,
            { field: InsuranceField? -> _editProfileState.update { it.copy(focusedInsuranceField = field) } },
            { insurance -> _editProfileState.update { it.copy(primaryInsurance = insurance) } }
        )
    }

    fun validateSecondaryInsurance(): Boolean {
        return validateInsurance(
            editProfileState.value.secondaryInsurance,
            { field: InsuranceField? -> _editProfileState.update { it.copy(focusedInsuranceField = field) } },
            { insurance -> _editProfileState.update { it.copy(secondaryInsurance = insurance) } }
        )
    }
    //endregion


    //region Update Personal information


    fun personalDetailValidation(): Boolean {
        val personalInfo = editProfileState.value.personalInformation
        // Validate mandatory fields and set error messages for personal info only
        var hasError = false
        val updatedPersonalInfo = personalInfo.copy(
            firstName = if (personalInfo.firstName.isMandatory && (personalInfo.firstName.value.isNullOrBlank())) {
                hasError = true
                personalInfo.firstName.copy(error = StrResId(R.string.mandatory_field_msg))
            } else if (personalInfo.firstName.value?.isNotBlank() == true && !personalInfo.firstName.value.any { it.isLetter() }) {
                hasError = true
                personalInfo.firstName.copy(error = StrResId(R.string.one_alphabet_name))
            } else personalInfo.firstName.copy(error = null),
            lastName = if (personalInfo.lastName.isMandatory && (personalInfo.lastName.value.isNullOrBlank())) {
                hasError = true
                personalInfo.lastName.copy(error = StrResId(R.string.mandatory_field_msg))
            } else if (personalInfo.lastName.value?.isNotBlank() == true && !personalInfo.lastName.value.any { it.isLetter() }) {
                hasError = true
                personalInfo.lastName.copy(error = StrResId(R.string.one_alphabet_name))
            } else personalInfo.lastName.copy(error = null),
            dateOfBirth = if (personalInfo.dateOfBirth.isMandatory && (personalInfo.dateOfBirth.value.isNullOrBlank())) {
                hasError = true
                personalInfo.dateOfBirth.copy(error = StrResId(R.string.mandatory_field_msg))
            } else if (!personalInfo.dateOfBirth.value.isNullOrBlank()) {
                val formatter = ModelUtil.mothFirstDateFormatWithSlash()
                val date =
                    DateUtil.getDate(personalInfo.dateOfBirth.value, formatter)
                if (date == null || DateUtil.isDateAfterToday(
                        personalInfo.dateOfBirth.value,
                        formatter
                    )
                ) {
                    hasError = true
                    personalInfo.dateOfBirth.copy(error = StrResId(R.string.dob_invalid))
                } else {
                    personalInfo.dateOfBirth.copy(error = null)
                }
            } else personalInfo.dateOfBirth.copy(error = null),
            birthSex = if (personalInfo.birthSex.isMandatory && (personalInfo.birthSex.value.isNullOrBlank())) {
                hasError = true
                personalInfo.birthSex.copy(error = StrResId(R.string.mandatory_field_msg))
            } else personalInfo.birthSex.copy(error = null),
            genderIdentity = if (personalInfo.genderIdentity.isMandatory && (personalInfo.genderIdentity.value == null)) {
                hasError = true
                personalInfo.genderIdentity.copy(error = StrResId(R.string.mandatory_field_msg))
            } else personalInfo.genderIdentity.copy(error = null),
            bloodGroup = if (personalInfo.bloodGroup.isMandatory && (personalInfo.bloodGroup.value.isNullOrBlank())) {
                hasError = true
                personalInfo.bloodGroup.copy(error = StrResId(R.string.mandatory_field_msg))
            } else personalInfo.bloodGroup.copy(error = null),
            race = if (personalInfo.race.isMandatory && (personalInfo.race.value.isNullOrEmpty())) {
                hasError = true
                personalInfo.race.copy(error = StrResId(R.string.mandatory_field_msg))
            } else personalInfo.race.copy(error = null),
            ethnicity = if (personalInfo.ethnicity.isMandatory && (personalInfo.ethnicity.value.isNullOrEmpty())) {
                hasError = true
                personalInfo.ethnicity.copy(error = StrResId(R.string.mandatory_field_msg))
            } else personalInfo.ethnicity.copy(error = null),
            language = if (personalInfo.language.isMandatory && (personalInfo.language.value == null)) {
                hasError = true
                personalInfo.language.copy(error = StrResId(R.string.mandatory_field_msg))
            } else personalInfo.language.copy(error = null),
            smokingStatus = if (personalInfo.smokingStatus.isMandatory && (personalInfo.smokingStatus.value.isNullOrBlank())) {
                hasError = true
                personalInfo.smokingStatus.copy(error = StrResId(R.string.mandatory_field_msg))
            } else personalInfo.smokingStatus.copy(error = null),
            maritalStatus = if (personalInfo.maritalStatus.isMandatory && (personalInfo.maritalStatus.value.isNullOrBlank())) {
                hasError = true
                personalInfo.maritalStatus.copy(error = StrResId(R.string.mandatory_field_msg))
            } else personalInfo.maritalStatus.copy(error = null),
            employmentStatus = if (personalInfo.employmentStatus.isMandatory && (personalInfo.employmentStatus.value.isNullOrBlank())) {
                hasError = true
                personalInfo.employmentStatus.copy(error = StrResId(R.string.mandatory_field_msg))
            } else personalInfo.employmentStatus.copy(error = null)
        )
        // Find the first field with an error
        val firstErrorField = when {
            updatedPersonalInfo.firstName.error != null -> PersonalField.FIRST_NAME
            updatedPersonalInfo.lastName.error != null -> PersonalField.LAST_NAME
            updatedPersonalInfo.dateOfBirth.error != null -> PersonalField.DATE_OF_BIRTH
            updatedPersonalInfo.birthSex.error != null -> PersonalField.BIRTH_SEX
            updatedPersonalInfo.genderIdentity.error != null -> PersonalField.GENDER_IDENTITY
            updatedPersonalInfo.bloodGroup.error != null -> PersonalField.BLOOD_GROUP
            updatedPersonalInfo.race.error != null -> PersonalField.RACE
            updatedPersonalInfo.ethnicity.error != null -> PersonalField.ETHNICITY
            updatedPersonalInfo.language.error != null -> PersonalField.LANGUAGE
            updatedPersonalInfo.smokingStatus.error != null -> PersonalField.SMOKING_STATUS
            updatedPersonalInfo.maritalStatus.error != null -> PersonalField.MARITAL_STATUS
            updatedPersonalInfo.employmentStatus.error != null -> PersonalField.EMPLOYMENT_STATUS
            else -> null
        }
        if (hasError) {
            _editProfileState.update {
                it.copy(
                    personalInformation = updatedPersonalInfo,
                    focusedPersonalField = firstErrorField
                )
            }
            return false
        }
        return true
    }

    fun validateCaregiverDetails(): Boolean {
        val primary = editProfileState.value.primaryCaregiverDetails
        val secondary = editProfileState.value.secondaryCaregiverDetails

        var hasError = false
        var focusedCaregiverField: CaregiverField? = null

        val updatedPrimary = primary?.copy(
            relationship = if (primary.relationship.isMandatory && primary.relationship.value == null) {
                hasError = true
                focusedCaregiverField =
                    CaregiverField.RELATIONSHIP
                primary.relationship.copy(error = StrResId(R.string.mandatory_field_msg))
            } else primary.relationship.copy(error = null),
            firstName = if (primary.firstName.isMandatory && primary.firstName.value.isNullOrBlank()) {
                hasError = true
                if (focusedCaregiverField == null) focusedCaregiverField =
                    CaregiverField.FIRST_NAME
                primary.firstName.copy(error = StrResId(R.string.mandatory_field_msg))
            } else if (primary.firstName.value?.isNotBlank() == true && !primary.firstName.value.any { it.isLetter() }) {
                hasError = true
                if (focusedCaregiverField == null) focusedCaregiverField =
                    CaregiverField.FIRST_NAME
                primary.firstName.copy(error = StrResId(R.string.one_alphabet_name))
            } else primary.firstName.copy(error = null),
            lastName = if (primary.lastName.isMandatory && primary.lastName.value.isNullOrBlank()) {
                hasError = true
                if (focusedCaregiverField == null) focusedCaregiverField =
                    CaregiverField.LAST_NAME
                primary.lastName.copy(error = StrResId(R.string.mandatory_field_msg))
            } else if (primary.lastName.value?.isNotBlank() == true && !primary.lastName.value.any { it.isLetter() }) {
                hasError = true
                if (focusedCaregiverField == null) focusedCaregiverField =
                    CaregiverField.LAST_NAME
                primary.lastName.copy(error = StrResId(R.string.one_alphabet_name))
            } else primary.lastName.copy(error = null),
            dateOfBirth = if (!primary.dateOfBirth.value.isNullOrBlank()) {
                val formatter = ModelUtil.mothFirstDateFormatWithSlash()
                val date =
                    DateUtil.getDate(primary.dateOfBirth.value, formatter)
                if (date == null || DateUtil.isDateAfterToday(
                        primary.dateOfBirth.value,
                        formatter
                    )
                ) {
                    hasError = true
                    focusedCaregiverField = focusedCaregiverField ?: CaregiverField.DATE_OF_BIRTH
                    primary.dateOfBirth.copy(error = StrResId(R.string.dob_invalid))
                } else {
                    primary.dateOfBirth.copy(error = null)
                }
            } else primary.dateOfBirth.copy(error = null)
        )

        var focusedSecondCaregiverField: CaregiverField? = null

        val updatedSecondary = secondary?.copy(
            relationship = if (secondary.relationship.isMandatory && secondary.relationship.value == null) {
                hasError = true
                if (focusedCaregiverField == null) focusedSecondCaregiverField =
                    CaregiverField.RELATIONSHIP
                secondary.relationship.copy(error = StrResId(R.string.mandatory_field_msg))
            } else secondary.relationship.copy(error = null),
            firstName = if (secondary.firstName.isMandatory && secondary.firstName.value.isNullOrBlank()) {
                hasError = true
                if (focusedCaregiverField == null && focusedSecondCaregiverField == null) focusedSecondCaregiverField =
                    CaregiverField.FIRST_NAME
                secondary.firstName.copy(error = StrResId(R.string.mandatory_field_msg))
            } else if (secondary.firstName.value?.isNotBlank() == true && !secondary.firstName.value.any { it.isLetter() }) {
                hasError = true
                if (focusedCaregiverField == null) focusedCaregiverField =
                    CaregiverField.FIRST_NAME
                secondary.firstName.copy(error = StrResId(R.string.one_alphabet_name))
            } else secondary.firstName.copy(error = null),
            lastName = if (secondary.lastName.isMandatory && secondary.lastName.value.isNullOrBlank()) {
                hasError = true
                if (focusedCaregiverField == null && focusedSecondCaregiverField == null) focusedSecondCaregiverField =
                    CaregiverField.LAST_NAME
                secondary.lastName.copy(error = StrResId(R.string.mandatory_field_msg))
            } else if (secondary.lastName.value?.isNotBlank() == true && !secondary.lastName.value.any { it.isLetter() }) {
                hasError = true
                if (focusedCaregiverField == null) focusedCaregiverField =
                    CaregiverField.LAST_NAME
                secondary.lastName.copy(error = StrResId(R.string.one_alphabet_name))
            } else secondary.lastName.copy(error = null),
            dateOfBirth = if (!secondary.dateOfBirth.value.isNullOrBlank()) {
                val formatter = ModelUtil.mothFirstDateFormatWithSlash()
                val date =
                    DateUtil.getDate(secondary.dateOfBirth.value, formatter)
                if (date == null || DateUtil.isDateAfterToday(
                        secondary.dateOfBirth.value,
                        formatter
                    )
                ) {
                    hasError = true
                    if (focusedCaregiverField == null && focusedSecondCaregiverField == null) {
                        focusedSecondCaregiverField = CaregiverField.DATE_OF_BIRTH
                    }
                    secondary.dateOfBirth.copy(error = StrResId(R.string.dob_invalid))
                } else {
                    secondary.dateOfBirth.copy(error = null)
                }
            } else secondary.dateOfBirth.copy(error = null)
        )

        if (hasError) {
            _editProfileState.update {
                it.copy(
                    primaryCaregiverDetails = updatedPrimary,
                    secondaryCaregiverDetails = updatedSecondary,
                    focusedPCaregiverField = focusedCaregiverField,
                    focusedSCaregiverField = focusedSecondCaregiverField
                )
            }
            return false
        }
        return true
    }

    fun validateContactInformation(): Boolean {
        val contactInfo = editProfileState.value.contactInformation
        var hasError = false
        val updatedContactInfo = contactInfo.copy(
            email = if (contactInfo.email.isMandatory && contactInfo.email.value.isNullOrBlank()) {
                hasError = true
                contactInfo.email.copy(error = StrResId(R.string.mandatory_field_msg))
            } else contactInfo.email.copy(error = null),
            primaryPhone = if (contactInfo.primaryPhone.isMandatory && contactInfo.primaryPhone.value.isNullOrBlank()) {
                hasError = true
                contactInfo.primaryPhone.copy(error = StrResId(R.string.mandatory_field_msg))
            } else contactInfo.primaryPhone.copy(error = null),
            addressLine1 = if (contactInfo.addressLine1.isMandatory && contactInfo.addressLine1.value.isNullOrBlank()) {
                hasError = true
                contactInfo.addressLine1.copy(error = StrResId(R.string.mandatory_field_msg))
            } else contactInfo.addressLine1.copy(error = null),
            mobilePhone = if (contactInfo.mobilePhone.isMandatory && contactInfo.mobilePhone.value.isNullOrBlank()) {
                hasError = true
                contactInfo.mobilePhone.copy(error = StrResId(R.string.mandatory_field_msg))
            } else contactInfo.mobilePhone.copy(error = null),
            homePhone = if (contactInfo.homePhone.isMandatory && contactInfo.homePhone.value.isNullOrBlank()) {
                hasError = true
                contactInfo.homePhone.copy(error = StrResId(R.string.mandatory_field_msg))
            } else contactInfo.homePhone.copy(error = null),
            workPhone = if (contactInfo.workPhone.isMandatory && contactInfo.workPhone.value.isNullOrBlank()) {
                hasError = true
                contactInfo.workPhone.copy(error = StrResId(R.string.mandatory_field_msg))
            } else contactInfo.workPhone.copy(error = null)
        )
        // Find the first error field (email, primaryPhone, addressLine1, mobilePhone, homePhone, workPhone)
        val firstErrorField = when {
            updatedContactInfo.email.error != null -> ContactField.EMAIL
            updatedContactInfo.primaryPhone.error != null -> ContactField.PRIMARY_PHONE
            updatedContactInfo.addressLine1.error != null -> ContactField.ADDRESS_LINE_1
            updatedContactInfo.mobilePhone.error != null -> ContactField.MOBILE_PHONE
            updatedContactInfo.homePhone.error != null -> ContactField.HOME_PHONE
            updatedContactInfo.workPhone.error != null -> ContactField.WORK_PHONE
            else -> null
        }
        if (hasError) {
            _editProfileState.update {
                it.copy(
                    contactInformation = updatedContactInfo,
                    focusedContactField = firstErrorField
                )
            }
            return false
        }
        return true
    }

    fun savePatient() {
        viewModelScope.launch(Dispatchers.IO) {
            _editProfileState.update { it.copy(isLoading = true, errorMessage = null) }
            val patientToEdit = selectedPatient.value?.copy() ?: return@launch
            val personalInfo = editProfileState.value.personalInformation
            val contactInfo = editProfileState.value.contactInformation
            // copy details from personalInformation
            patientToEdit.firstName = personalInfo.firstName.value
            patientToEdit.lastName = personalInfo.lastName.value
            patientToEdit.dateOfBirth =
                DateUtil.getDate(
                    personalInfo.dateOfBirth.value,
                    ModelUtil.mothFirstDateFormatWithSlash()
                )
            patientToEdit.gender = personalInfo.birthSex.value
            patientToEdit.genderIdentity = personalInfo.genderIdentity.value?.value
            patientToEdit.bloodGroup = personalInfo.bloodGroup.value
            patientToEdit.race = personalInfo.race.value?.joinToString(",") { it.value }
            patientToEdit.ethnicity =
                personalInfo.ethnicity.value?.joinToString(",") { it.value }
            patientToEdit.language = personalInfo.language.value?.value
            patientToEdit.smokingStatus = personalInfo.smokingStatus.value
            patientToEdit.maritalStatus = personalInfo.maritalStatus.value
            patientToEdit.employmentStatus = personalInfo.employmentStatus.value
            // Copy contact information
            patientToEdit.contactDetails?.apply {
                preferredCommunication = contactInfo.preferredCommunication.value
                emailId = contactInfo.email.value
                primaryPhone = contactInfo.primaryPhone.value
                mobilePhone = contactInfo.mobilePhone.value
                homePhone = contactInfo.homePhone.value
                workPhone = contactInfo.workPhone.value
                workPhoneExtn = contactInfo.workPhoneExtn.value
            }
            patientToEdit.postalAddress?.apply {
                line1 = contactInfo.addressLine1.value
                line2 = contactInfo.addressLine2.value
                city = contactInfo.city.value
                state = contactInfo.state.value?.value ?: ""
                country = contactInfo.country.value?.value ?: ""
                postalCode = contactInfo.postalCode.value
            }
            patientToEdit.emergencyContactName = contactInfo.emergencyContactName.value
            patientToEdit.emergencyContactNumber = contactInfo.emergencyContactNumber.value
            patientToEdit.emergencyContactExtn = contactInfo.emergencyContactExtn.value
            val caregiverList = mutableListOf<Caregiver>()
            val primaryCaregiver = Caregiver()
            primaryCaregiver.apply {
                relationship =
                    editProfileState.value.primaryCaregiverDetails?.relationship?.value?.value
                        ?: ""
                firstName =
                    editProfileState.value.primaryCaregiverDetails?.firstName?.value ?: ""
                lastName = editProfileState.value.primaryCaregiverDetails?.lastName?.value ?: ""
                dateOfBirth = editProfileState.value.primaryCaregiverDetails?.dateOfBirth?.value
                gender = editProfileState.value.primaryCaregiverDetails?.birthSex?.value ?: ""
                sameAsPatContact =
                    editProfileState.value.primaryCaregiverDetails?.isCopyPatientAddress
                if (sameAsPatContact != true) {
                    val postalAddress = PostalAddress()
                    postalAddress.apply {
                        line1 =
                            editProfileState.value.primaryCaregiverDetails?.addressLine1?.value
                                ?: ""
                        line2 =
                            editProfileState.value.primaryCaregiverDetails?.addressLine2?.value
                                ?: ""
                        city = editProfileState.value.primaryCaregiverDetails?.city?.value ?: ""
                        state =
                            editProfileState.value.primaryCaregiverDetails?.state?.value?.value
                                ?: ""
                        country =
                            editProfileState.value.primaryCaregiverDetails?.country?.value?.value
                                ?: ""
                        postalCode =
                            editProfileState.value.primaryCaregiverDetails?.postalCode?.value
                                ?: ""
                    }
                    val contactDetails = ContactDetails()
                    contactDetails.mobilePhone =
                        editProfileState.value.primaryCaregiverDetails?.mobilePhone?.value ?: ""
                    contactDetails.homePhone =
                        editProfileState.value.primaryCaregiverDetails?.homePhone?.value ?: ""
                    this.postalAddress = postalAddress
                    this.contactDetails = contactDetails
                }
            }
            caregiverList.add(primaryCaregiver)
            val secondaryCaregiver = Caregiver()
            secondaryCaregiver.apply {
                relationship =
                    editProfileState.value.secondaryCaregiverDetails?.relationship?.value?.value
                        ?: ""
                firstName =
                    editProfileState.value.secondaryCaregiverDetails?.firstName?.value ?: ""
                lastName =
                    editProfileState.value.secondaryCaregiverDetails?.lastName?.value ?: ""
                dateOfBirth =
                    editProfileState.value.secondaryCaregiverDetails?.dateOfBirth?.value
                gender = editProfileState.value.secondaryCaregiverDetails?.birthSex?.value ?: ""
                sameAsPatContact =
                    editProfileState.value.secondaryCaregiverDetails?.isCopyPatientAddress
                if (sameAsPatContact != true) {
                    val postalAddress = PostalAddress()
                    postalAddress.apply {
                        line1 =
                            editProfileState.value.secondaryCaregiverDetails?.addressLine1?.value
                                ?: ""
                        line2 =
                            editProfileState.value.secondaryCaregiverDetails?.addressLine2?.value
                                ?: ""
                        city =
                            editProfileState.value.secondaryCaregiverDetails?.city?.value ?: ""
                        state =
                            editProfileState.value.secondaryCaregiverDetails?.state?.value?.value
                                ?: ""
                        country =
                            editProfileState.value.secondaryCaregiverDetails?.country?.value?.value
                                ?: ""
                        postalCode =
                            editProfileState.value.secondaryCaregiverDetails?.postalCode?.value
                                ?: ""
                    }
                    val contactDetails = ContactDetails()
                    contactDetails.mobilePhone =
                        editProfileState.value.secondaryCaregiverDetails?.mobilePhone?.value
                            ?: ""
                    contactDetails.homePhone =
                        editProfileState.value.secondaryCaregiverDetails?.homePhone?.value ?: ""
                    this.postalAddress = postalAddress
                    this.contactDetails = contactDetails
                }
            }
            caregiverList.add(secondaryCaregiver)
            patientToEdit.caregivers = caregiverList
            val result = savePatientUseCase(patientToEdit)
            withContext(Dispatchers.Main) {
                when (result) {
                    is APIState.Success -> {
                        if (personalInfo.profileImageFilePath.cacheFilePath != null) {
                            uploadProfileImage(
                                personalInfo.profileImageFilePath.cacheFilePath,
                                patientToEdit.practicePatientId ?: "",
                                patientToEdit
                            )
                        } else {
                            getPatientDetail(patientToEdit.practicePatientId ?: "")
                        }
                    }

                    is APIState.Error -> {
                        lastFailedAction = { savePatient() }
                        _editProfileState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.apiResponseError.errorMsg
                            )
                        }
                    }

                    else -> {

                    }
                }
            }
        }
    }

    fun uploadProfileImage(
        imageFilePath: String,
        practicePatientId: String,
        patientToEdit: Patient
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = insuranceCardUseCases.uploadPatientImage(imageFilePath, practicePatientId)
            when (result) {
                is APIState.Success -> {
                    patientDetailRepository.updatePatient(patientToEdit)
                    getPatientDetail(patientToEdit.practicePatientId ?: "")
                }

                is APIState.Error -> {
                    lastFailedAction = {
                        uploadProfileImage(
                            imageFilePath,
                            practicePatientId,
                            patientToEdit
                        )
                    }
                    _editProfileState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.apiResponseError.errorMsg
                        )
                    }
                }

                else -> {
                    //Show error and retry
                }
            }
        }
    }

    fun onSuccessSavePatient() {
        if (editProfileState.value.isInsuranceRequired && !editProfileState.value.isFromFinalSave) {
            _editProfileState.update {
                it.copy(
                    selectedSection = Section.PRIMARY_INSURANCE,
                    isShowSkip = true,
                    isLoading = false,
                    errorMessage = null,
                    toastMessage = R.string.patient_info_save_msg,
                    isPersonalDetailChanged = false
                )
            }
        } else {
            viewModelScope.launch {
                _navigationEvents.emit(CustomNavigationEvent.Back)
            }
        }
    }

    fun updatePatientProfile(uri: Uri?, context: Context) {
        uri ?: return
        _editProfileState.update {
            it.copy(
                isProfilePicUploading = true
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val file = FileUtil.compressImageUriAndWriteToFile(
                context = context,
                imageUri = uri,
                maxSizeBytes = 400_000L,
            )
            _editProfileState.update {
                it.copy(
                    isProfilePicUploading = false,
                    personalInformation = it.personalInformation.copy(
                        profileImageFilePath = it.personalInformation.profileImageFilePath.copy(
                            cacheFilePath = file?.path,
                            isServerUploaded = false
                        )
                    ),
                    isPersonalDetailChanged = true
                )
            }
        }
    }
    //endregion

    // Utility method to update insuredPersonDetail with personal and contact details


    fun clearError() {
        _editProfileState.update { it.copy(errorMessage = null) }
        lastFailedAction = null
    }

    fun retryLastFailedAction() {
        val action = lastFailedAction
        clearError()
        action?.invoke()
    }

    fun proceedPendingSectionChange() {
        _saveInsuranceDialog.update {
            false
        }
        pendingSectionChange?.let { section ->
            updateSelectedSection(section)
            pendingSectionChange = null
        }
    }


    //region Insurance Card Server API
    fun postPatientInsuranceCardFront(
        imageFile: String,
        insuranceId: String, isPrimaryInsurance: Boolean
    ) {
        if (isPrimaryInsurance) {
            _editProfileState.update {
                it.copy(
                    primaryInsurance = it.primaryInsurance.copy(
                        insuranceCardFront = it.primaryInsurance.insuranceCardFront.copy(
                            cacheFilePath = imageFile,
                            isUploading = !insuranceId.isBlank(), // if insurance id is not blank we will not upload
                            isServerUploaded = false
                        )
                    )
                )
            }
        } else {
            _editProfileState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null,
                    secondaryInsurance = it.secondaryInsurance.copy(
                        insuranceCardFront = it.secondaryInsurance.insuranceCardFront.copy(
                            cacheFilePath = imageFile,
                            isUploading = !insuranceId.isBlank(),
                            isServerUploaded = false
                        )
                    )
                )
            }
        }
        if (insuranceId.isBlank()) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = insuranceCardUseCases.postInsuranceCardFront(
                imageFile,
                selectedPatient.value?.practicePatientId ?: "", insuranceId
            )
            when (result) {
                is APIState.Error -> {
                    if (isPrimaryInsurance) {
                        _editProfileState.update {
                            it.copy(
                                primaryInsurance = it.primaryInsurance.copy(
                                    insuranceCardFront = it.primaryInsurance.insuranceCardFront.copy(
                                        isUploading = false,
                                        isServerUploaded = false
                                    )
                                )
                            )
                        }
                    } else {
                        _editProfileState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null,
                                secondaryInsurance = it.secondaryInsurance.copy(
                                    insuranceCardFront = it.secondaryInsurance.insuranceCardFront.copy(
                                        isUploading = false,
                                        isServerUploaded = false
                                    )
                                )
                            )
                        }
                    }
                }

                APIState.Loading -> {
                    if (isPrimaryInsurance) {
                        _editProfileState.update {
                            it.copy(
                                primaryInsurance = it.primaryInsurance.copy(
                                    insuranceCardFront = it.primaryInsurance.insuranceCardFront.copy(
                                        isUploading = true
                                    )
                                )
                            )
                        }
                    } else {
                        _editProfileState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null,
                                secondaryInsurance = it.secondaryInsurance.copy(
                                    insuranceCardFront = it.secondaryInsurance.insuranceCardFront.copy(
                                        isUploading = true
                                    )
                                )
                            )
                        }
                    }
                }

                is APIState.Success<*> -> {
                    val isAdded = result.data as Boolean
                    if (isAdded) {
                        if (isPrimaryInsurance) {
                            _editProfileState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = null,
                                    primaryInsurance = it.primaryInsurance.copy(
                                        insuranceCardFront = it.primaryInsurance.insuranceCardFront.copy(
                                            cacheFilePath = null,
                                            filePath = imageFile,
                                            isServerUploaded = true,
                                            isUploading = false,
                                        )
                                    )
                                )
                            }
                        } else {
                            _editProfileState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = null,
                                    secondaryInsurance = it.secondaryInsurance.copy(
                                        insuranceCardFront = it.secondaryInsurance.insuranceCardFront.copy(
                                            cacheFilePath = null,
                                            filePath = imageFile,
                                            isServerUploaded = true,
                                            isUploading = false,
                                        )
                                    )
                                )
                            }
                        }
                    } else {
                        _editProfileState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = StrResId(R.string.request_failed_try_again)
                            )
                        }
                    }
                }
            }
        }

    }

    fun postPatientInsuranceCardBack(
        imageFile: String,
        insuranceId: String, isPrimaryInsurance: Boolean
    ) {
        if (isPrimaryInsurance) {
            _editProfileState.update {
                it.copy(
                    primaryInsurance = it.primaryInsurance.copy(
                        insuranceCardBack = it.primaryInsurance.insuranceCardBack.copy(
                            cacheFilePath = imageFile,
                            isUploading = !insuranceId.isBlank(), // if insurance id is not blank we will not upload
                            isServerUploaded = false
                        )
                    )
                )
            }
        } else {
            _editProfileState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null,
                    secondaryInsurance = it.secondaryInsurance.copy(
                        insuranceCardBack = it.secondaryInsurance.insuranceCardBack.copy(
                            cacheFilePath = imageFile,
                            isUploading = !insuranceId.isBlank(), // if insurance id is not blank we will not upload
                            isServerUploaded = false
                        )
                    )
                )
            }
        }

        if (insuranceId.isBlank()) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = insuranceCardUseCases.postInsuranceCardBack(
                imageFile,
                selectedPatient.value?.practicePatientId ?: "", insuranceId
            )
            when (result) {
                is APIState.Error -> {
                    if (isPrimaryInsurance) {
                        _editProfileState.update {
                            it.copy(
                                primaryInsurance = it.primaryInsurance.copy(
                                    insuranceCardBack = it.primaryInsurance.insuranceCardBack.copy(
                                        isUploading = false,
                                        isServerUploaded = false
                                    )
                                )
                            )
                        }
                    } else {
                        _editProfileState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null,
                                secondaryInsurance = it.secondaryInsurance.copy(
                                    insuranceCardBack = it.secondaryInsurance.insuranceCardBack.copy(
                                        isUploading = false,
                                        isServerUploaded = false
                                    )
                                )
                            )
                        }
                    }
                }

                APIState.Loading -> {
                    if (isPrimaryInsurance) {
                        _editProfileState.update {
                            it.copy(
                                primaryInsurance = it.primaryInsurance.copy(
                                    insuranceCardBack = it.primaryInsurance.insuranceCardBack.copy(
                                        isUploading = true
                                    )
                                )
                            )
                        }
                    } else {
                        _editProfileState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null,
                                secondaryInsurance = it.secondaryInsurance.copy(
                                    insuranceCardBack = it.secondaryInsurance.insuranceCardBack.copy(
                                        isUploading = true
                                    )
                                )
                            )
                        }
                    }
                }

                is APIState.Success<*> -> {
                    val isAdded = result.data as Boolean
                    if (isAdded) {
                        if (isPrimaryInsurance) {
                            _editProfileState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = null,
                                    primaryInsurance = it.primaryInsurance.copy(
                                        insuranceCardBack = it.primaryInsurance.insuranceCardBack.copy(
                                            cacheFilePath = null,
                                            filePath = imageFile,
                                            isServerUploaded = true,
                                            isUploading = false,
                                        )
                                    )
                                )
                            }
                        } else {
                            _editProfileState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = null,
                                    secondaryInsurance = it.secondaryInsurance.copy(
                                        insuranceCardBack = it.secondaryInsurance.insuranceCardBack.copy(
                                            cacheFilePath = null,
                                            filePath = imageFile,
                                            isServerUploaded = true,
                                            isUploading = false,
                                        )
                                    )
                                )
                            }
                        }

                    } else {
                        _editProfileState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = StrResId(R.string.request_failed_try_again)
                            )
                        }
                    }
                }

            }
        }

    }

    fun getPatientInsuranceCardFront(insuranceId: String, isPrimaryInsurance: Boolean) {
        _editProfileState.update {
            it.copy(isLoading = true, errorMessage = null)
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = insuranceCardUseCases.getInsuranceCardFront(
                selectedPatient.value?.practicePatientId ?: "", insuranceId
            )
            when (result) {
                is APIState.Success -> {
                    val file = result.data
                    _editProfileState.update {
                        it.copy(isLoading = false, errorMessage = null)
                    }
                    file?.absolutePath
                    if (isPrimaryInsurance) {
                        _editProfileState.update {
                            it.copy(
                                primaryInsurance = it.primaryInsurance.copy(
                                    insuranceCardFront = it.primaryInsurance.insuranceCardFront.copy(
                                        filePath = file?.absolutePath,
                                        isServerUploaded = true,
                                        cacheFilePath = null
                                    )
                                )
                            )
                        }
                    } else {
                        _editProfileState.update {
                            it.copy(
                                secondaryInsurance = it.secondaryInsurance.copy(
                                    insuranceCardFront = it.secondaryInsurance.insuranceCardFront.copy(
                                        filePath = file?.absolutePath,
                                        isServerUploaded = true,
                                        cacheFilePath = null
                                    )
                                )
                            )
                        }
                    }
                }

                is APIState.Error -> {
                    lastFailedAction = {
                        getPatientInsuranceCardFront(insuranceId, isPrimaryInsurance)
                    }
                    _editProfileState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.apiResponseError.errorMsg
                        )
                    }
                }

                else -> {
                }
            }
        }
    }

    fun getPatientInsuranceCardBack(insuranceId: String, isPrimaryInsurance: Boolean) {
        _editProfileState.update {
            it.copy(isLoading = true, errorMessage = null)
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = insuranceCardUseCases.getInsuranceCardBack(
                selectedPatient.value?.practicePatientId ?: "", insuranceId
            )
            when (result) {
                is APIState.Success -> {
                    val file = result.data
                    _editProfileState.update {
                        it.copy(isLoading = false, errorMessage = null)
                    }
                    file?.absolutePath
                    if (isPrimaryInsurance) {
                        _editProfileState.update {
                            it.copy(
                                primaryInsurance = it.primaryInsurance.copy(
                                    insuranceCardBack = it.primaryInsurance.insuranceCardBack.copy(
                                        filePath = file?.absolutePath
                                    )
                                )
                            )
                        }
                    } else {
                        _editProfileState.update {
                            it.copy(
                                secondaryInsurance = it.secondaryInsurance.copy(
                                    insuranceCardBack = it.secondaryInsurance.insuranceCardBack.copy(
                                        filePath = file?.absolutePath
                                    )
                                )
                            )
                        }
                    }
                }

                is APIState.Error -> {
                    lastFailedAction = {
                        getPatientInsuranceCardBack(insuranceId, isPrimaryInsurance)
                    }
                    _editProfileState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.apiResponseError.errorMsg
                        )
                    }
                }


                else -> {}
            }
        }
    }

    fun updateInsuranceCard(uri: Uri?, context: Context, isFront: Boolean) {
        uri ?: return
        if (isFront) {
            if (editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE) {
                _editProfileState.update {
                    it.copy(
                        primaryInsurance = it.primaryInsurance.copy(
                            insuranceCardFront = it.primaryInsurance.insuranceCardFront.copy(
                                isUploading = true,
                            )
                        )
                    )
                }
            } else {
                _editProfileState.update {
                    it.copy(
                        secondaryInsurance = it.secondaryInsurance.copy(
                            insuranceCardFront = it.secondaryInsurance.insuranceCardFront.copy(
                                isUploading = true,
                            )
                        )
                    )
                }
            }
        } else {
            if (editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE) {
                _editProfileState.update {
                    it.copy(
                        primaryInsurance = it.primaryInsurance.copy(
                            insuranceCardBack = it.primaryInsurance.insuranceCardBack.copy(
                                isUploading = true,
                            )
                        )
                    )
                }
            } else {
                _editProfileState.update {
                    it.copy(
                        secondaryInsurance = it.secondaryInsurance.copy(
                            insuranceCardBack = it.secondaryInsurance.insuranceCardBack.copy(
                                isUploading = true,
                            )
                        )
                    )
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            val file = FileUtil.compressImageUriAndWriteToFile(
                context = context,
                imageUri = uri,
                maxSizeBytes = 400_000L,
            )
            if (isFront) {
                postPatientInsuranceCardFront(
                    file?.path ?: "",
                    if (editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE) {
                        editProfileState.value.primaryInsurance.patientInsuranceId ?: ""
                    } else {
                        editProfileState.value.secondaryInsurance.patientInsuranceId ?: ""
                    },
                    editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE
                )
            } else {
                postPatientInsuranceCardBack(
                    file?.path ?: "",
                    if (editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE) {
                        editProfileState.value.primaryInsurance.patientInsuranceId ?: ""
                    } else {
                        editProfileState.value.secondaryInsurance.patientInsuranceId ?: ""
                    },
                    editProfileState.value.selectedSection == Section.PRIMARY_INSURANCE
                )
            }
        }

    }

    fun deleteInsuranceCardBack(
        insuranceId: String,
        isPrimaryInsurance: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _editProfileState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = insuranceCardUseCases.deleteInsuranceCardBack(
                selectedPatient.value?.practicePatientId ?: "", insuranceId
            )
            when (result) {
                is APIState.Error -> {
                    lastFailedAction = {
                        deleteInsuranceCardBack(insuranceId, isPrimaryInsurance)
                    }
                    _editProfileState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.apiResponseError.errorMsg
                        )
                    }
                }

                is APIState.Success<*> -> {

                    val response = result.data as? Pair<*, *>
                    val isSuccess = response?.first as? Boolean
                    val responseData = response?.second as? String

                    if (isSuccess == true && responseData != null) {
                        if (isPrimaryInsurance) {
                            _editProfileState.update {
                                it.copy(
                                    primaryInsurance = it.primaryInsurance.copy(
                                        insuranceCardBack = it.primaryInsurance.insuranceCardBack.copy(
                                            filePath = null,
                                            cacheFilePath = null,
                                            isServerUploaded = true
                                        )
                                    ),
                                    isLoading = false,
                                    errorMessage = null,
                                    toastMessage = R.string.insurance_card_delete_success
                                )
                            }
                        } else {
                            _editProfileState.update {
                                it.copy(
                                    secondaryInsurance = it.secondaryInsurance.copy(
                                        insuranceCardBack = it.secondaryInsurance.insuranceCardBack.copy(
                                            filePath = null,
                                            cacheFilePath = null,
                                            isServerUploaded = true
                                        )
                                    ),
                                    isLoading = false,
                                    errorMessage = null,
                                    toastMessage = R.string.insurance_card_delete_success
                                )
                            }
                        }
                    } else {
                        _editProfileState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = StrResId(R.string.request_failed_try_again)
                            )
                        }
                    }
                }

                else -> {
                }
            }
        }
    }

    fun deleteInsuranceCardFront(
        insuranceId: String,
        isPrimaryInsurance: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _editProfileState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = insuranceCardUseCases.deleteInsuranceCardFront(
                selectedPatient.value?.practicePatientId ?: "", insuranceId
            )
            when (result) {
                is APIState.Error -> {
                    lastFailedAction = {
                        deleteInsuranceCardFront(insuranceId, isPrimaryInsurance)
                    }
                    _editProfileState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.apiResponseError.errorMsg
                        )
                    }
                }

                is APIState.Success<*> -> {

                    val response = result.data as? Pair<*, *>
                    val isSuccess = response?.first as? Boolean
                    val responseData = response?.second as? String

                    if (isSuccess == true && responseData != null) {
                        if (isPrimaryInsurance) {
                            _editProfileState.update {
                                it.copy(
                                    primaryInsurance = it.primaryInsurance.copy(
                                        insuranceCardFront = it.primaryInsurance.insuranceCardFront.copy(
                                            filePath = null,
                                            cacheFilePath = null,
                                            isServerUploaded = true
                                        )
                                    ),
                                    isLoading = false,
                                    errorMessage = null,
                                    toastMessage = R.string.insurance_card_delete_success
                                )
                            }
                        } else {
                            _editProfileState.update {
                                it.copy(
                                    secondaryInsurance = it.secondaryInsurance.copy(
                                        insuranceCardFront = it.secondaryInsurance.insuranceCardFront.copy(
                                            filePath = null,
                                            cacheFilePath = null,
                                            isServerUploaded = true
                                        )
                                    ),
                                    isLoading = false,
                                    errorMessage = null,
                                    toastMessage = R.string.insurance_card_delete_success
                                )
                            }
                        }
                    } else {
                        _editProfileState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = StrResId(R.string.request_failed_try_again)
                            )
                        }
                    }
                }

                else -> {
                }
            }
        }
    }
    //endregion

    fun getPatientDetail(patientId: String) {
        _editProfileState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }
        // Capture insurance before the use case overwrites the cache, so edits are not lost.
        val existingPrimaryInsurance = selectedPatient.value?.primaryInsurance
        val existingSecondaryInsurance = selectedPatient.value?.secondaryInsurance
        viewModelScope.launch(Dispatchers.IO) {
            val result = getPatientDetailUseCase(patientId)
            when (result) {
                is APIState.Success -> {
                    val patient = result.data
                    patient.primaryInsurance = existingPrimaryInsurance
                    patient.secondaryInsurance = existingSecondaryInsurance
                    patientDetailRepository.setPatient(patient)
                    onSuccessSavePatient()
                }

                is APIState.Error -> {
                    lastFailedAction = { getPatientDetail(patientId) }
                    _editProfileState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.apiResponseError.errorMsg
                        )
                    }
                }

                else -> {}
            }
        }
    }

    fun getPatientInsurances(
        isPrimaryInsurance: Boolean,
        insuranceId: String,
        onSuccess: (Insurance) -> Unit
    ) {
        _editProfileState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = patientRepository.getPatientInsurance(
                selectedPatient.value?.practicePatientId ?: "", insuranceId
            )
            when (result) {
                is APIState.Success -> {
                    val insuranceList = result.data as List<Insurance>
                    val insurance = insuranceList.firstOrNull()
                    val countryKey = insurance?.insuredPersonDetail?.country?.value?.value
                    val stateMap = patientDetailRepository.countryStateMap
                    var stateList = emptyList<State>()
                    if (!countryKey.isNullOrBlank() && stateMap.containsKey(countryKey)) {
                        stateList = stateMap[countryKey] ?: emptyList()
                    }
                    val country =
                        patientDetailRepository.countryList.firstOrNull { it.value == countryKey }
                    val state = stateList.firstOrNull {
                        it.label == (insurance?.insuredPersonDetail?.state?.value?.label
                            ?: "")
                    } ?: insurance?.insuredPersonDetail?.state?.value
                    insurance?.copy(
                        insuredPersonDetail = insurance.insuredPersonDetail?.copy(
                            state = FieldState(value = state),
                            country = FieldState(value = country)
                        ),
                        selectedPayer = FieldState(insurance.selectedPayer.value)
                    )
                    insurance?.let {
                        if (isPrimaryInsurance) {
                            patientDetailRepository.setPrimaryInsurance(insurance)
                        } else {
                            patientDetailRepository.setSecondaryInsurance(insurance)
                        }
                        onSuccess(insurance)
                    }
                }

                is APIState.Error -> {
                    lastFailedAction =
                        { getPatientInsurances(isPrimaryInsurance, insuranceId, onSuccess) }
                    _editProfileState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.apiResponseError.errorMsg
                        )
                    }
                }

                else -> {}
            }
        }
    }

}

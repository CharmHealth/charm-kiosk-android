package com.charm.ehr.kiosk.presentation.dashboard

import UiText
import android.content.Context
import android.net.Uri
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.common.util.AppticsUtil
import com.charm.ehr.kiosk.common.util.DateUtil
import com.charm.ehr.kiosk.common.util.FileUtil
import com.charm.ehr.kiosk.common.util.MDAccountsRelatedUtil
import com.charm.ehr.kiosk.common.util.MdEvent
import com.charm.ehr.kiosk.common.util.MdEventGroup
import com.charm.ehr.kiosk.common.util.ModelUtil
import com.charm.ehr.kiosk.common.util.ModelUtil.Companion.updateWithCharacterLimit
import com.charm.ehr.kiosk.domain.Facility
import com.charm.ehr.kiosk.domain.repository.MemberRepository
import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.domain.repository.PreferenceRepository
import com.charm.ehr.kiosk.domain.repository.WorkerRepository
import com.charm.ehr.kiosk.domain.result.AddPatientResult
import com.charm.ehr.kiosk.domain.usecase.patient.FetchDemographicsMetricsUseCase
import com.charm.ehr.kiosk.domain.usecase.patient.SearchPatientUseCase
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.presentation.dashboard.add_patient.AddPatientState
import com.charm.ehr.kiosk.presentation.dashboard.search_patient.AddPatientAction
import com.charm.ehr.kiosk.presentation.dashboard.search_patient.DashboardSection
import com.charm.ehr.kiosk.presentation.dashboard.search_patient.SearchPatientAction
import com.charm.ehr.kiosk.presentation.dashboard.search_patient.SearchPatientState
import com.charm.ehr.kiosk.presentation.dashboard.search_patient.SearchType
import com.charm.ehr.kiosk.presentation.edit_profile.FieldState
import com.charm.ehr.kiosk.presentation.settings.SettingsAction
import com.charm.ehr.kiosk.presentation.settings.SettingsState
import com.charm.ehr.kiosk.presentation.tour_page.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val memberRepository: MemberRepository,
    val authManager: AuthManager,
    private val preferenceRepository: PreferenceRepository,
    private val sessionManager: SessionManager,
    private val workerRepository: WorkerRepository,
    private val fetchDemographicsMetricsUseCase: FetchDemographicsMetricsUseCase,
    private val searchPatientUseCase: SearchPatientUseCase,
) : ViewModel() {

    var searchJob: Job? = null

    private val _searchPatientState: MutableStateFlow<SearchPatientState> =
        MutableStateFlow(SearchPatientState())
    val searchPatientState: StateFlow<SearchPatientState> = _searchPatientState.onStart {

        _searchPatientState.update {
            it.copy(
                selectedSearchType = getValidSearchType(preferenceRepository.getFrequentSearchType())
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000L),
        initialValue = _searchPatientState.value
    )

    private val _addPatientState: MutableStateFlow<AddPatientState> =
        MutableStateFlow(AddPatientState())
    val addPatientState: StateFlow<AddPatientState> = _addPatientState.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000L),
        initialValue = _addPatientState.value
    )

    private val _settingState: MutableStateFlow<SettingsState> = MutableStateFlow(SettingsState())
    val settingState: StateFlow<SettingsState> = _settingState.stateIn(
        scope = viewModelScope, started = WhileSubscribed(0), initialValue = _settingState.value
    )


    private val _genderList =
        MutableStateFlow<List<String>>(emptyList())
    val genderList: StateFlow<List<String>> = _genderList

    private val _generalErrorState = MutableStateFlow<UiText?>(null)
    val generalErrorState: StateFlow<UiText?> = _generalErrorState

    private val _refreshWorkId = MutableStateFlow<UUID?>(null)
    val refreshWorkId: StateFlow<UUID?> = _refreshWorkId

    init {
        fetchDemographicsMetrics()
        observeSearchPatientQuery()

        val defaultGender = preferenceRepository.getDefaultGender()
            ?: authManager.kioskSettingsFlow.value?.defaultGender?.replaceFirstChar { it.uppercaseChar() }
            ?: ""
        val lastUpdatedTime = preferenceRepository.getLastUpdatedTime()
        val appointmentStatus =
            authManager.kioskSettingsFlow.value?.accountDetails?.kioskPreferences?.appointmentStatus?.status
                ?: ""
        _settingState.update {
            it.copy(
                defaultGender = defaultGender,
                lastUpdatedTime = lastUpdatedTime,
                appointmentStatus = appointmentStatus
            )
        }
        _addPatientState.update {
            it.copy(
                birthSex = FieldState(value = defaultGender, error = null)
            )
        }
    }

    fun fetchDemographicsMetrics(): Job {
        return viewModelScope.launch {
            when (val result = fetchDemographicsMetricsUseCase()) {
                is APIState.Success -> _genderList.value = result.data.birthSex
                is APIState.Error -> _generalErrorState.value = result.apiResponseError.errorMsg
                else -> Unit
            }
        }
    }

    fun setDefaultGender(selectedGender: String) {
        preferenceRepository.putDefaultGender(selectedGender)
        _settingState.update {
            it.copy(
                defaultGender = selectedGender
            )
        }
        _addPatientState.update {
            it.copy(birthSex = FieldState(value = selectedGender, error = null))
        }
    }

    fun onAddPatientAction(action: AddPatientAction) {
        when (action) {

            is AddPatientAction.OnClickAddPatient -> {
                val isValid = validateAddPatientFields()
                if (isValid) {
                    addPatient()
                }
            }

            is AddPatientAction.OnFirstNameChange -> {
                _addPatientState.update {
                    it.copy(
                        firstName = it.firstName.updateWithCharacterLimit(
                            action.firstName, 35,
                            errorMessageResId = R.string.firstname_heading_max_length_reached,
                            errorArgs = arrayListOf("35").toArray()
                        )
                    )
                }
            }

            is AddPatientAction.OnLastNameChange -> {
                _addPatientState.update {
                    it.copy(
                        lastName = it.lastName.updateWithCharacterLimit(
                            action.lastName, 35,
                            errorMessageResId = R.string.lastname_heading_max_length_reached,
                            errorArgs = arrayListOf("35").toArray()
                        )
                    )
                }
            }

            is AddPatientAction.OnDobChange -> {
                _addPatientState.update {
                    it.copy(dateOfBirth = FieldState(value = action.dob, error = null))
                }
            }

            is AddPatientAction.OnGenderChange -> {
                _addPatientState.update {
                    it.copy(birthSex = FieldState(value = action.gender, error = null))
                }
            }

            is AddPatientAction.OnClearState -> {
                _addPatientState.update {
                    it.clear()
                }
            }

            is AddPatientAction.OnClearErrorMessage -> {
                _addPatientState.update {
                    it.copy(
                        errorMessage = null
                    )
                }
            }

            AddPatientAction.ClearFocusedField -> {
                _addPatientState.update {
                    it.copy(
                        focusedField = null
                    )
                }
            }

            is AddPatientAction.OnDismissDuplicateDialog -> {
                _addPatientState.update {
                    it.copy(duplicatePatient = null)
                }
            }

            is AddPatientAction.OnAddAsDuplicate -> {
                _addPatientState.update {
                    it.copy(duplicatePatient = null)
                }
                addPatient(duplicateCheck = false)
            }

            is AddPatientAction.OnViewExistingPatient -> {
                // Clear duplicate dialog and navigate to the existing patient's profile
                _addPatientState.update {
                    it.copy(
                        duplicatePatient = null,
                        isPatAddedSuccessful = true,
                        patientId = action.patientId
                    )
                }
            }

            else -> {

            }
        }
    }


    fun getSelectedFacility(): Facility? {
        return authManager.selectedFacility
    }

    fun getLoginUserName(): String {
        return authManager.kioskSettingsFlow.value?.accountDetails?.loginId ?: ""
    }

    fun validateAddPatientFields(): Boolean {

        var isValid = true
        var firstErrorField: com.charm.ehr.kiosk.presentation.dashboard.add_patient.AddPatientField? =
            null

        if (addPatientState.value.firstName.value.isNullOrBlank()) {
            _addPatientState.update {
                it.copy(firstName = it.firstName.copy(error = UiText.StrResId(R.string.pleaseEnterFirstName)))
            }
            isValid = false
            if (firstErrorField == null) firstErrorField =
                com.charm.ehr.kiosk.presentation.dashboard.add_patient.AddPatientField.FIRST_NAME
        }

        if (addPatientState.value.firstName.value?.any { it.isLetter() } == false) {
            _addPatientState.update {
                it.copy(firstName = it.firstName.copy(error = UiText.StrResId(R.string.one_alphabet_name)))
            }
            isValid = false
            if (firstErrorField == null) firstErrorField =
                com.charm.ehr.kiosk.presentation.dashboard.add_patient.AddPatientField.FIRST_NAME
        }

        if ((addPatientState.value.firstName.value?.length ?: 0) > 35) {
            isValid = false
            if (firstErrorField == null) firstErrorField =
                com.charm.ehr.kiosk.presentation.dashboard.add_patient.AddPatientField.FIRST_NAME
        }

        if (addPatientState.value.lastName.value.isNullOrBlank()) {
            _addPatientState.update {
                it.copy(lastName = it.lastName.copy(error = UiText.StrResId(R.string.pleaseEnterLastName)))
            }
            isValid = false
            if (firstErrorField == null) firstErrorField =
                com.charm.ehr.kiosk.presentation.dashboard.add_patient.AddPatientField.LAST_NAME
        }

        if (addPatientState.value.lastName.value?.any { it.isLetter() } == false) {
            _addPatientState.update {
                it.copy(lastName = it.lastName.copy(error = UiText.StrResId(R.string.one_alphabet_name)))
            }
            isValid = false
            if (firstErrorField == null) firstErrorField =
                com.charm.ehr.kiosk.presentation.dashboard.add_patient.AddPatientField.LAST_NAME
        }


        if ((addPatientState.value.lastName.value?.length ?: 0) > 35) {
            isValid = false
            if (firstErrorField == null) firstErrorField =
                com.charm.ehr.kiosk.presentation.dashboard.add_patient.AddPatientField.LAST_NAME
        }

        if (addPatientState.value.dateOfBirth.value.isNullOrBlank()) {
            _addPatientState.update {
                it.copy(dateOfBirth = it.dateOfBirth.copy(error = UiText.StrResId(R.string.pleaseEnterDob)))
            }
            isValid = false
            if (firstErrorField == null) firstErrorField =
                com.charm.ehr.kiosk.presentation.dashboard.add_patient.AddPatientField.DATE_OF_BIRTH
        } else {
            val dob = addPatientState.value.dateOfBirth.value ?: ""
            val formatter = ModelUtil.mothFirstDateFormatWithSlash()
            val date = DateUtil.getDate(dob, formatter)
            if (date == null || DateUtil.isDateAfterToday(dob, formatter)) {
                _addPatientState.update {
                    it.copy(dateOfBirth = it.dateOfBirth.copy(error = UiText.StrResId(R.string.enter_valid_date)))
                }
                isValid = false
                if (firstErrorField == null) firstErrorField =
                    com.charm.ehr.kiosk.presentation.dashboard.add_patient.AddPatientField.DATE_OF_BIRTH
            }
        }

        if (addPatientState.value.birthSex.value.isNullOrBlank()) {
            _addPatientState.update {
                it.copy(birthSex = it.birthSex.copy(error = UiText.StrResId(R.string.pleaseEnterBirthSex)))
            }
            isValid = false
            if (firstErrorField == null) firstErrorField =
                com.charm.ehr.kiosk.presentation.dashboard.add_patient.AddPatientField.BIRTH_SEX
        }

        if (!isValid && firstErrorField != null) {
            _addPatientState.update {
                it.copy(focusedField = firstErrorField)
            }
        }

        return isValid

    }

    fun onSearchPatientAction(action: SearchPatientAction) {

        when (action) {
            is SearchPatientAction.OnChangeSearchQuery -> {

                _searchPatientState.update {
                    it.copy(
                        searchQuery = action.query
                    )
                }
            }

            is SearchPatientAction.OnUpdateSearchType -> {
                preferenceRepository.setFrequentSearchType(action.selectedSearchType)
                _searchPatientState.update {
                    it.copy(
                        selectedSearchType = action.selectedSearchType,
                        searchQuery = "",
                        searchResultList = emptyList()
                    )
                }
            }

            is SearchPatientAction.OnUpdateSelectedItemIndex -> {
                _searchPatientState.update {
                    it.copy(
                        selectedDashboardSection = action.section
                    )
                }
            }

            is SearchPatientAction.OnAddPatient -> {
                val searchQuery = searchPatientState.value.searchQuery
                val selectedSearchType = searchPatientState.value.selectedSearchType
                if (selectedSearchType == SearchType.NAME_OR_MOBILE && !searchQuery.isDigitsOnly()) {
                    val nameParts = searchQuery.trim().split(" ")
                    val firstName = nameParts.getOrNull(0) ?: ""
                    val lastName = nameParts.drop(1).joinToString(" ")
                    _addPatientState.update {
                        it.copy(
                            firstName = it.firstName.copy(value = firstName),
                            lastName = it.lastName.copy(value = lastName)
                        )
                    }
                } else if (selectedSearchType == SearchType.DATE_OF_BIRTH) {
                    val formatter = ModelUtil.mothFirstDateFormatWithSlash()
                    if (!DateUtil.isDateAfterToday(searchQuery, formatter)) {
                        _addPatientState.update {
                            it.copy(
                                dateOfBirth = it.dateOfBirth.copy(value = searchQuery)
                            )
                        }
                    }
                }
                _searchPatientState.update {
                    it.copy(
                        selectedDashboardSection = DashboardSection.ADD,
                        searchQuery = "",
                        searchResultList = emptyList()
                    )
                }
            }

            else -> Unit
        }

    }

    @OptIn(FlowPreview::class)
    fun observeSearchPatientQuery() {
        searchPatientState.map { it.searchQuery.trim() }.distinctUntilChanged().debounce(500L)
            .onEach { query ->
                if (searchPatientState.value.selectedSearchType == SearchType.DATE_OF_BIRTH && query.length != 10) {
                    _searchPatientState.value = _searchPatientState.value.copy(
                        errorMessage = null,
                    )
                } else if (query.length < 3) {
                    _searchPatientState.value = _searchPatientState.value.copy(
                        errorMessage = null,
                    )
                } else {
                    searchJob?.cancel()
                    _searchPatientState.update {
                        it.copy(
                            isLoading = true,
                            errorMessage = null,
                            searchResultList = emptyList()
                        )
                    }
                    searchJob =
                        searchPatient(query, searchPatientState.value.selectedSearchType)

                }
            }.launchIn(viewModelScope)
    }

    fun searchPatient(searchString: String, selectedSearchType: SearchType): Job {
        return viewModelScope.launch {
            _searchPatientState.update { it.copy(isLoading = true) }

            val (nameSearchString, mobileSearchString) = if (searchString.all { it.isDigit() }) {
                Pair(null, searchString)
            } else {
                Pair(searchString, null)
            }

            val result = searchPatientUseCase(
                name = if (selectedSearchType == SearchType.NAME_OR_MOBILE) nameSearchString else null,
                mobile = if (selectedSearchType == SearchType.NAME_OR_MOBILE) mobileSearchString else null,
                recordId = if (selectedSearchType == SearchType.RECORD_ID) searchString else null,
                email = if (selectedSearchType == SearchType.EMAIL) searchString else null,
                dob = if (selectedSearchType == SearchType.DATE_OF_BIRTH) {
                    val inputDate = LocalDate.parse(searchString, ModelUtil.mothFirstDateFormatWithSlash())
                    inputDate.toString(ModelUtil.charmApiDateFormat())
                } else null,
            )

            if (!isActive) return@launch

            _searchPatientState.update {
                when (result) {
                    is APIState.Success -> it.copy(
                        isLoading = false,
                        errorMessage = null,
                        searchResultList = result.data
                    )
                    is APIState.Error -> it.copy(
                        isLoading = false,
                        errorMessage = result.apiResponseError.errorMsg
                    )
                    else -> it.copy(isLoading = false)
                }
            }
        }
    }

    fun addPatient(duplicateCheck: Boolean = true) {
        updateIsAddPatientLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val response = patientRepository.addPatient(addPatientState.value.getPatient(), duplicateCheck)
            updateIsAddPatientLoading(false)
            when (response) {
                is APIState.Success -> {
                    when (val result = response.data) {
                        is AddPatientResult.Added -> {
                            updatePatientId(result.patientId)
                            if (addPatientState.value.imageFilePath != null) {
                                val patient = addPatientState.value.getPatient()
                                updateIsAddPatientLoading(true)
                                val imgResult = patientRepository.addPatientImage(
                                    patient.patientImgFilePath ?: "",
                                    patient.practicePatientId ?: ""
                                )
                                updateIsAddPatientLoading(false)
                                when (imgResult) {
                                    is APIState.Error -> {
                                        _addPatientState.update {
                                            it.copy(errorMessage = imgResult.apiResponseError.errorMsg)
                                        }
                                    }
                                    else -> {}
                                }
                            } else {
                                updateIsAddPatientLoading(false)
                            }
                            _addPatientState.update { it.copy(isPatAddedSuccessful = true) }
                            AppticsUtil.addEvent(MdEvent.OnAddPatientSuccess, MdEventGroup.Patient)
                        }
                        is AddPatientResult.DuplicateFound -> {
                            _addPatientState.update { it.copy(duplicatePatient = result.patient) }
                        }
                    }
                }

                is APIState.Error -> {
                    _addPatientState.update {
                        it.copy(errorMessage = response.apiResponseError.errorMsg)
                    }
                    AppticsUtil.addEvent(MdEvent.OnAddPatientFailure, MdEventGroup.Patient)
                }

                else -> {}
            }
        }
    }

    fun updateIsAddPatientLoading(isLoading: Boolean) {
        _addPatientState.update {
            it.copy(isLoading = isLoading)
        }
    }

    fun updatePatientImageUri(uri: Uri?, context: Context) {
        uri ?: return
        _addPatientState.update {
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

            _addPatientState.update {
                it.copy(imageFilePath = file?.path ?: "", isProfilePicUploading = false)
            }

        }
        _addPatientState.update {
            it.copy(uri = uri)
        }
    }

    suspend fun addPatientImage() {
        withContext(Dispatchers.IO) {

        }
    }

    private suspend fun updatePatientId(patientId: String) {
        withContext(Dispatchers.Main) {
            _addPatientState.update {
                it.copy(patientId = patientId)
            }
        }
    }

    fun onSettingsAction(action: SettingsAction, context: Context) {
        when (action) {

            SettingsAction.OnPrivacyPolicy -> {
                ModelUtil.openUrlInExternalBrowser(
                    context.applicationContext,
                    MDAccountsRelatedUtil.privacyPolicyUrl
                )
            }

            SettingsAction.OnTermsOfService -> {
                ModelUtil.openUrlInExternalBrowser(
                    context.applicationContext,
                    MDAccountsRelatedUtil.termsOfServicesUrl
                )
            }

            SettingsAction.OnUpdateData -> {
                triggerRefreshServerWork(fetchSettings = true)
            }

            SettingsAction.ClearToastMessage -> {
                _settingState.update {
                    it.copy(
                        toastMessage = null
                    )
                }
            }

            SettingsAction.ShowLogoutConfirmDialog -> {
                _settingState.update {
                    it.copy(
                        showLogoutConfirmDialog = true
                    )
                }
            }

            SettingsAction.DismissLogoutConfirmDialog -> {
                _settingState.update {
                    it.copy(
                        showLogoutConfirmDialog = false
                    )
                }
            }

            SettingsAction.OnSwitchFacility -> {
                _searchPatientState.update {
                    it.copy(
                        searchResultList = emptyList(),
                        searchQuery = ""
                    )
                }
            }

            else -> {}

        }
    }

    fun setGeneralError(error: UiText?) {
        _generalErrorState.value = error
    }

    fun getValidSearchType(rawValue: String?): SearchType {
        return try {
            rawValue?.let { SearchType.valueOf(it) } ?: SearchType.NAME_OR_MOBILE
        } catch (e: IllegalArgumentException) {
            SearchType.NAME_OR_MOBILE
        }
    }

    fun triggerRefreshServerWork(fetchSettings: Boolean) {
        _settingState.update {
            it.copy(
                isLoading = true,
                toastMessage = null // clear previous error
            )
        }
        _refreshWorkId.update {
            workerRepository.triggerUpdateDataWork(fetchSettings)
        }
    }

    fun clearRefreshWorkId() {
        _refreshWorkId.update {
            null
        }
    }

    fun onRefreshWorkCompleted(success: Boolean, error: String?) {
        clearRefreshWorkId()
        if (success) {
            _settingState.update {
                it.copy(
                    isLoading = false,
                    toastMessage = R.string.data_update_success_msg,
                    lastUpdatedTime = System.currentTimeMillis()
                )
            }
            if (authManager.isStandAloneUser()) {
                authManager.selectedFacility = preferenceRepository.getSelectedFacility()
                authManager.selectedFacilityId = preferenceRepository.getSelectedFacilityId()
            }
        } else {
            _settingState.update {
                it.copy(
                    isLoading = false,
                    toastMessage = R.string.data_update_failed_msg
                )
            }
        }
    }

}

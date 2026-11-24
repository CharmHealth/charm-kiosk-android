package com.charm.ehr.kiosk.presentation.patient_profile


import UiText
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.common.PatientProfileFlow
import com.charm.ehr.kiosk.common.util.ModelUtil
import com.charm.ehr.kiosk.domain.Appointment
import com.charm.ehr.kiosk.domain.ConsentForm
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.domain.QuestionnairePhysicianTemplate
import com.charm.ehr.kiosk.domain.repository.PatientDetailRepository
import com.charm.ehr.kiosk.domain.usecase.appointment.AppointmentUseCases
import com.charm.ehr.kiosk.domain.usecase.patient.FetchDemographicsMetricsUseCase
import com.charm.ehr.kiosk.domain.usecase.patient.GetPatientDetailUseCase
import com.charm.ehr.kiosk.domain.usecase.patient.GetPatientInsurancesUseCase
import com.charm.ehr.kiosk.domain.usecase.patient.ReferenceDataUseCases
import com.charm.ehr.kiosk.domain.usecase.questionnaire.QuestionnaireUseCases
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.presentation.navigation.MdRoute
import com.charm.ehr.kiosk.presentation.search_template.SearchTemplateAction
import com.charm.ehr.kiosk.presentation.search_template.SearchTemplateState
import com.charm.ehr.kiosk.presentation.search_template.SearchTemplateType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
import javax.inject.Inject

@HiltViewModel
class PatientDetailViewmodel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val patientDetailRepository: PatientDetailRepository,
    private val getPatientDetailUseCase: GetPatientDetailUseCase,
    private val fetchDemographicsMetricsUseCase: FetchDemographicsMetricsUseCase,
    private val referenceDataUseCases: ReferenceDataUseCases,
    private val getPatientInsurancesUseCase: GetPatientInsurancesUseCase,
    private val questionnaireUseCases: QuestionnaireUseCases,
    private val appointmentUseCases: AppointmentUseCases,
    val authManager: AuthManager
) : ViewModel() {

    val patientId: String by lazy { savedStateHandle.toRoute<MdRoute.PatientProfileModuleHost>().patientId }

    val selectedPatient: StateFlow<Patient?> = patientDetailRepository.patient

    val patientSharedQuestionnaires: StateFlow<List<QuestionnairePhysicianTemplate>?> =
        patientDetailRepository.patientSharedQuestionnaires

    var allQuestionnaireList: List<QuestionnairePhysicianTemplate> = emptyList()
    var allConsentFormList: List<ConsentForm> = emptyList()

    var isInitialLoadFailed: Boolean = false

    var searchFormJob: Job? = null

    // Dialog state for discard changes
    private val _showDiscardDialog = MutableStateFlow(false)
    val showDiscardDialog: StateFlow<Boolean> = _showDiscardDialog

    private val _patientProfileState: MutableStateFlow<PatientProfileState> =
        MutableStateFlow(PatientProfileState())
    val patientProfileState: StateFlow<PatientProfileState> = _patientProfileState.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000L),
        initialValue = _patientProfileState.value
    )

    private var lastFailedAction: (() -> Unit)? = null

    fun clearError() {
        _patientProfileState.update { it.copy(errorMessage = null, initialCallErrorMessage = null) }
        lastFailedAction = null
    }

    fun retryLastFailedAction() {
        val action = lastFailedAction
        clearError()
        action?.invoke()
    }

    fun initialCallRetry() {
        loadInitialData(failedApiCalls as List<String>) {
            onCompleteInitialCalls()
        }
    }

    fun addMissingSection(section: Section) {
        _patientProfileState.update { state ->
            if (section in state.missingSections) {
                state
            } else {
                state.copy(missingSections = state.missingSections + section)
            }
        }
    }

    fun removeMissingSection(section: Section) {
        _patientProfileState.update { state ->
            if (section in state.missingSections) {
                state.copy(missingSections = state.missingSections - section)
            } else {
                state
            }
        }
    }

    fun onCompleteInitialCalls() {
        getPatientDetail(patientId = patientId)
        getPatientSharedTemplates()
        getPracticeTemplates()
        setInitCallsPerformed(true)
        _patientProfileState.update {
            it.copy(
                allowEditPatientDetails = authManager.showEditPatient(),
                isInsuranceRequired = authManager.showInsuranceModule(),
                isQuestionnaireRequired = authManager.showQuestionnaireModule(),
                isConsentFormRequired = authManager.showConsentFormsModule()
            )
        }
    }

    private val _searchTemplateState: MutableStateFlow<SearchTemplateState> =
        MutableStateFlow(SearchTemplateState())
    val searchTemplateState: StateFlow<SearchTemplateState> = _searchTemplateState
        .onStart {
        }
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000L),
            initialValue = _searchTemplateState.value
        )

    init {
        observerSearchFormQuery()
    }

    private val failedApiCalls = mutableSetOf<String>()

    fun initApiCalls() {

        loadInitialData(apiCallList) {
            onCompleteInitialCalls()
        }

    }

    val apiCallList = listOf(
        ModelUtil.API_KEY_DEMOGRAPHICS_METRICS,
        ModelUtil.API_KEY_SETTINGS,
        ModelUtil.API_KEY_COUNTRIES,
        ModelUtil.API_KEY_STATES_INDIA,
        ModelUtil.API_KEY_STATES_US,
        ModelUtil.API_KEY_STATES_AUSTRALIA
    )


    private fun loadInitialData(apiCallList: List<String>, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _patientProfileState.update {
                it.copy(
                    isLoading = true,
                    initialCallErrorMessage = null,
                    errorMessage = null
                )
            }
            var errorMessage: UiText? = null
            val jobs = apiCallList.map { key ->
                async {
                    val result = when (key) {
                        ModelUtil.API_KEY_DEMOGRAPHICS_METRICS -> getPatientDemographicsMetricsSync()
                        ModelUtil.API_KEY_SETTINGS -> referenceDataUseCases.fetchDemographicsSettings()
                        ModelUtil.API_KEY_COUNTRIES -> referenceDataUseCases.fetchCountries()
                        ModelUtil.API_KEY_STATES_INDIA -> referenceDataUseCases.fetchStates(ModelUtil.countryStringIndia)
                        ModelUtil.API_KEY_STATES_US -> referenceDataUseCases.fetchStates(ModelUtil.countryStringUs)
                        ModelUtil.API_KEY_STATES_AUSTRALIA -> referenceDataUseCases.fetchStates(ModelUtil.countryStringAustralia)
                        else -> null
                    }
                    if (result is APIState.Error) {
                        errorMessage = result.apiResponseError.errorMsg
                        key
                    } else null
                }
            }
            val failedKeys = jobs.awaitAll().filterNotNull()
            failedApiCalls.clear()
            failedApiCalls.addAll(failedKeys)

            if (failedApiCalls.isNotEmpty()) {
                _patientProfileState.update {
                    it.copy(
                        initialCallErrorMessage = errorMessage
                            ?: UiText.StrResId(R.string.something_went_wrong)
                    )
                }
            } else {
                onComplete()
            }
        }
    }

    private suspend fun getPatientDemographicsMetricsSync(): APIState<Any> {
        val result = fetchDemographicsMetricsUseCase()
        if (result is APIState.Success) {
            patientDetailRepository.setDemographicsMetrics(result.data)
        }
        return result
    }

    fun getPatientSharedTemplates() {
        getPatientsSharedQuestionnaires(patientId)
        getPatientsSharedConsentForms(patientId)
    }

    fun getPracticeTemplates() {
        getPracticeQuestionnaires()
        getPracticeConsentForms()
    }

    fun getPatientDetail(patientId: String) {
        isInitialLoadFailed = false
        _patientProfileState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = getPatientDetailUseCase(patientId)) {
                is APIState.Success -> {
                    getPatientInsurances()
                }

                is APIState.Error -> {
                    isInitialLoadFailed = true
                    lastFailedAction = { getPatientDetail(patientId) }
                    _patientProfileState.update {
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

    fun getPatientsSharedQuestionnaires(patientId: String) {
        isInitialLoadFailed = false
        viewModelScope.launch(Dispatchers.IO) {
            val result = questionnaireUseCases.getPatientSharedQuestionnaires(patientId)
            when (result) {
                is APIState.Error -> {
                    isInitialLoadFailed = true
                    lastFailedAction = { getPatientsSharedQuestionnaires(patientId) }
                    _patientProfileState.update {
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

    fun getPatientsSharedConsentForms(patientId: String) {
        isInitialLoadFailed = false
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = questionnaireUseCases.getPatientSharedConsentForms(patientId)) {
                is APIState.Success -> {
                    _patientProfileState.update {
                        it.copy(
                            patientSharedConsentForms = result.data,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
                is APIState.Error -> {
                    isInitialLoadFailed = true
                    lastFailedAction = { getPatientsSharedConsentForms(patientId) }
                    _patientProfileState.update {
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

    fun getPracticeConsentForms() {
        isInitialLoadFailed = false
        viewModelScope.launch(Dispatchers.IO) {
            val result = questionnaireUseCases.getPracticeConsentForms()
            when (result) {
                is APIState.Success -> allConsentFormList = result.data
                is APIState.Error -> {
                    isInitialLoadFailed = true
                    lastFailedAction = { getPracticeConsentForms() }
                    _patientProfileState.update {
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

    fun getPracticeQuestionnaires() {
        isInitialLoadFailed = false
        viewModelScope.launch(Dispatchers.IO) {
            val result = questionnaireUseCases.getPracticeQuestionnaires()
            when (result) {
                is APIState.Success -> allQuestionnaireList = result.data
                is APIState.Error -> {
                    isInitialLoadFailed = true
                    lastFailedAction = { getPracticeQuestionnaires() }
                    _patientProfileState.update {
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

    fun onSearchFormAction(action: SearchTemplateAction) {
        when (action) {
            is SearchTemplateAction.OnChangeTemplateType -> {
                _searchTemplateState.update {
                    it.copy(
                        templateType = action.formType,
                        searchQuery = action.query,
                        searchResults = emptyList(),
                        isLoading = true,
                        errorMessage = null
                    )
                }
                performSearchCall(action.query)
            }

            is SearchTemplateAction.OnSearchQueryChange -> {
                _searchTemplateState.update {
                    it.copy(
                        searchQuery = action.query,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }

            is SearchTemplateAction.OnRemoveConsentTemplate -> {
                _searchTemplateState.update {
                    it.copy(
                        isChangesMade = true,
                        selectedConsentFormSet = it.selectedConsentFormSet.filter { form ->
                            form.fileId != action.consentForm.fileId
                        }.toSet(),
                    )
                }
            }

            is SearchTemplateAction.OnRemoveQuestionnaire -> {
                _searchTemplateState.update {
                    it.copy(
                        isChangesMade = true,
                        selectedQuestionnaireSet = it.selectedQuestionnaireSet.filter { ques ->
                            ques.templateId != action.questionnaire.templateId
                        }.toSet(),
                    )
                }
            }

            is SearchTemplateAction.OnSelectConsentTemplate -> {
                _searchTemplateState.update {
                    it.copy(
                        isChangesMade = true,
                        selectedConsentFormSet = (it.selectedConsentFormSet + action.consentForm),
                    )
                }
            }

            is SearchTemplateAction.OnSelectQuestionnaire -> {
                _searchTemplateState.update {
                    it.copy(
                        isChangesMade = true,
                        selectedQuestionnaireSet = (it.selectedQuestionnaireSet + action.questionnaire),
                    )
                }
            }

            is SearchTemplateAction.OnSubmit -> {
                if (_searchTemplateState.value.isChangesMade) {
                    val questionnairePair = patientSharedQuestionnaires.value?.let {
                        filterQuestionForUpdate(
                            patSharedQuesList = it.toSet(),
                            selectedQuesList = searchTemplateState.value.selectedQuestionnaireSet
                        )
                    } ?: Pair(emptyList(), emptyList())
                    val consentFormPair = filterConsentFormForUpdate(
                        patSharedConsentFormList = allConsentFormList.toSet(),
                        selectedConsentFormList = searchTemplateState.value.selectedConsentFormSet
                    )

                    updatePatientSharedQuestionnaire(
                        questionnairePair.first,
                        questionnairePair.second
                    )
                } else {
                    _searchTemplateState.update {
                        it.copy(
                            isSuccess = true,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }

            }

            is SearchTemplateAction.OnSuccess -> {
                _searchTemplateState.update {
                    it.copy(
                        isSuccess = false,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }

            is SearchTemplateAction.showDiscardDialog -> {
                _showDiscardDialog.update {
                    true
                }
            }

            is SearchTemplateAction.DismissDiscardDialog -> {
                _showDiscardDialog.update {
                    false
                }
            }

            else -> {

            }
        }
    }

    fun setInitCallsPerformed(isPerformed: Boolean) {
        _patientProfileState.update {
            it.copy(
                initCallsPerformed = isPerformed
            )
        }
    }

    fun filterQuestionForUpdate(
        patSharedQuesList: Set<QuestionnairePhysicianTemplate>,
        selectedQuesList: Set<QuestionnairePhysicianTemplate>
    ): Pair<List<QuestionnairePhysicianTemplate>, List<QuestionnairePhysicianTemplate>> {

        val addList = selectedQuesList.filter { it !in patSharedQuesList }
        val removeList = patSharedQuesList.filter { it !in selectedQuesList }

        return Pair(addList, removeList)
    }

    fun filterConsentFormForUpdate(
        patSharedConsentFormList: Set<ConsentForm>,
        selectedConsentFormList: Set<ConsentForm>
    ): Pair<List<ConsentForm>, List<ConsentForm>> {

        val addList = selectedConsentFormList.filter { it !in patSharedConsentFormList }
        val removeList = patSharedConsentFormList.filter { it !in selectedConsentFormList }

        return Pair(addList, removeList)
    }

    @OptIn(FlowPreview::class)
    fun observerSearchFormQuery() {
        searchTemplateState.map {
            it.searchQuery
        }
            .distinctUntilChanged()
            .debounce(500L)
            .onEach { query ->
                when {
                    query.isBlank() -> {
                        _searchTemplateState.update {
                            if (it.templateType == SearchTemplateType.Questionnaire) {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = null,
                                    searchResults = allQuestionnaireList.take(50)
                                )

                            } else {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = null,
                                    searchResults = allConsentFormList.take(50)
                                )
                            }
                        }
                    }

                    query.length >= 3 -> {
                        _searchTemplateState.update {
                            it.copy(
                                isLoading = true,
                                errorMessage = null,
                            )
                        }
                        performSearchCall(query)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun performSearchCall(query: String) {
        searchFormJob?.cancel() // Cancel any ongoing filter job
        searchFormJob = viewModelScope.launch {
            if (searchTemplateState.value.templateType == SearchTemplateType.Questionnaire) {
                val result = allQuestionnaireList.filter {
                    it.name?.contains(query, ignoreCase = true) == true
                }.take(20)
                if (!isActive) return@launch
                _searchTemplateState.update {
                    it.copy(
                        searchResults = result,
                        isLoading = false,
                        errorMessage = null
                    )
                }

            } else {
                val result = allConsentFormList.filter {
                    it.fileName?.contains(query, ignoreCase = true) == true
                }.take(20)
                if (!isActive) return@launch
                _searchTemplateState.update {
                    it.copy(
                        searchResults = result,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun updatePatientSharedQuestionnaire(
        questionnairesToAdd: List<QuestionnairePhysicianTemplate>,
        questionnairesToRemove: List<QuestionnairePhysicianTemplate>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _searchTemplateState.update {
                it.copy(isLoading = true, errorMessage = null)
            }
            questionnaireUseCases.updatePatientSharedQuestionnaire(
                patientId = patientId,
                quesToAdd = questionnairesToAdd,
                quesToRemove = questionnairesToRemove
            )
            questionnaireUseCases.getPatientSharedQuestionnaires(patientId)
            _searchTemplateState.update {
                it.copy(
                    isLoading = false,
                    isSuccess = true,
                    errorMessage = null,
                    isChangesMade = false,
                )
            }
            getPatientSharedTemplates()
        }
    }

    fun filterQuestionnaire() {
        //when the selected questionnaire has patientQuesMapId then filter to add api
        //For Remove the questionnaire should have patientQuestionnaireMapId to add to remove list
    }

    fun setPatSharedQuesToSelectedQuesState() {
        patientSharedQuestionnaires.value?.let {
            _searchTemplateState.update { searchTemplateState ->
                searchTemplateState.copy(
                    selectedQuestionnaireSet = it.toSet(),
                    searchResults = allQuestionnaireList.take(50)
                        .sortedBy { questionnaire -> questionnaire.name }
                )
            }
        }
    }

    fun setWorkFlowType(type: PatientProfileFlow) {
        authManager.setWorkFlowType(type)
        //once the workflow type is set, fetch appointments for patient and update the status
        if (type == PatientProfileFlow.AFTER_HANDOVER) {
            viewModelScope.launch {
                if (!authManager.getKioskSettings()?.accountDetails?.kioskPreferences?.appointmentStatus?.statusId.isNullOrBlank()) {
                    fetchAppointmentsForPatient()
                }
            }
        }
    }

    fun setLogoutPatientFlow(status: Boolean) {
        _patientProfileState.update {
            it.copy(isLogOutPatient = status)
        }
    }

    fun getPatientInsurances() {
        isInitialLoadFailed = false
        _patientProfileState.update {
            it.copy(isLoading = true, errorMessage = null)
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = getPatientInsurancesUseCase(
                selectedPatient.value?.practicePatientId ?: ""
            )
            when (result) {
                is APIState.Success -> {
                    _patientProfileState.update {
                        it.copy(isLoading = false, errorMessage = null)
                    }
                }
                is APIState.Error -> {
                    isInitialLoadFailed = true
                    lastFailedAction = { getPatientInsurances() }
                    _patientProfileState.update {
                        it.copy(isLoading = false, errorMessage = result.apiResponseError.errorMsg)
                    }
                }
                else -> {}
            }
        }
    }

    fun fetchAppointmentsForPatient() {
        _patientProfileState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = appointmentUseCases.fetchAppointments(patientId)
            when (result) {
                is APIState.Success -> {
                    if (result.data.isNotEmpty()) {
                        updateMultipleAppointmentStatuses(result.data)
                    } else {
                        _patientProfileState.update {
                            it.copy(isLoading = false, errorMessage = null)
                        }
                    }
                }
                is APIState.Error -> {
                    lastFailedAction = { fetchAppointmentsForPatient() }
                    _patientProfileState.update {
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

    private val _failedAppointmentIds = mutableListOf<String>()
    val failedAppointmentIds: List<String> get() = _failedAppointmentIds

    fun updateMultipleAppointmentStatuses(appointments: ArrayList<Appointment>) {
        viewModelScope.launch(Dispatchers.IO) {
            _patientProfileState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }
            _failedAppointmentIds.clear()
            val statusId =
                authManager.getKioskSettings()?.accountDetails?.kioskPreferences?.appointmentStatus?.statusId
                    ?: ""
            var errorMessage: UiText? = null
            val deferreds = appointments.map { appointment ->
                async {
                    val appointmentId = appointment.appointmentId ?: ""
                    val result =
                        appointmentUseCases.updateAppointmentStatus(appointmentId, statusId)
                    if (result is APIState.Error) {
                        errorMessage = result.apiResponseError.errorMsg
                        appointmentId
                    } else {
                        null
                    }
                }
            }
            val failed = deferreds.awaitAll().filterNotNull()
            _failedAppointmentIds.addAll(failed)
            _patientProfileState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = if (failed.isNotEmpty()) errorMessage else null
                )
            }
            retryFailedAppointmentUpdates()
        }
    }

    fun retryFailedAppointmentUpdates() {
        if (_failedAppointmentIds.isEmpty()) return
        val failedAppointments = _failedAppointmentIds.map { Appointment(appointmentId = it) }
        lastFailedAction = { updateMultipleAppointmentStatuses(ArrayList(failedAppointments)) }

    }

    override fun onCleared() {
        super.onCleared()
        searchFormJob?.cancel()
        patientDetailRepository.clearData()
    }
}

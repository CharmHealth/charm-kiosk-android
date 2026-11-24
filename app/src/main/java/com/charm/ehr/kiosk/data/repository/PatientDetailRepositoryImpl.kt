package com.charm.ehr.kiosk.data.repository
import com.charm.ehr.kiosk.domain.repository.PatientDetailRepository

import com.charm.ehr.kiosk.domain.Country
import com.charm.ehr.kiosk.domain.DemographicsMetrics
import com.charm.ehr.kiosk.domain.DemographicsSetting
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.domain.QuestionnairePhysicianTemplate
import com.charm.ehr.kiosk.domain.State
import com.charm.ehr.kiosk.presentation.edit_profile.Insurance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientDetailRepositoryImpl @Inject constructor()  : PatientDetailRepository{
    private val _patient = MutableStateFlow<Patient?>(null)
    override val patient: StateFlow<Patient?> = _patient.asStateFlow()

    private var _demographicsMetrics: DemographicsMetrics? = null
    override val demographicsMetrics: DemographicsMetrics?
        get() = synchronized(this) { _demographicsMetrics }

    private var _demographicsSetting: ArrayList<DemographicsSetting>? = null
    override val demographicsSetting: ArrayList<DemographicsSetting>?
        get() = synchronized(this) { _demographicsSetting }

    private var _countryList: List<Country> = emptyList()
    override val countryList: List<Country>
        get() = synchronized(this) { _countryList }

    override val countryStateMap: MutableMap<String, List<State>> = mutableMapOf()

    private val _patientSharedQuestionnaires = MutableStateFlow<List<QuestionnairePhysicianTemplate>>(emptyList())
    override val patientSharedQuestionnaires: StateFlow<List<QuestionnairePhysicianTemplate>> = _patientSharedQuestionnaires.asStateFlow()



    override fun setPatient(patient: Patient) {
        _patient.value = patient
    }

    override fun updatePatient(updated: Patient) {
        _patient.value = updated
    }

    override fun setDemographicsMetrics(metrics: DemographicsMetrics) {
        synchronized(this) { _demographicsMetrics = metrics }
    }

    override fun setCountryList(countries: List<Country>) {
        synchronized(this) { _countryList = countries }
    }

    override fun setDemographicsSetting(settings: ArrayList<DemographicsSetting>) {
        synchronized(this) { _demographicsSetting = settings }
    }

    override fun setStateToCountryMap(country: String, stateList: List<State>) {
        countryStateMap[country] = stateList
    }

    override fun setPrimaryInsurance(insurance: Insurance) {
        _patient.value = _patient.value?.copy(primaryInsurance = insurance)
    }

    override fun setSecondaryInsurance(insurance: Insurance) {
        _patient.value = _patient.value?.copy(secondaryInsurance = insurance)
    }

    override fun clearData() {
        _patient.value = null
        synchronized(this) { _demographicsMetrics = null }
        synchronized(this) { _demographicsSetting = null }
        synchronized(this) { _countryList = emptyList() }
        countryStateMap.clear()
    }

    override fun setPatientSharedQuestionnaires(list: List<QuestionnairePhysicianTemplate>) {
        _patientSharedQuestionnaires.value = list
    }

    override fun submitQuestionnaire(questionnaireId: String) {
        val updatedList = _patientSharedQuestionnaires.value.map { questionnaire ->
            if (questionnaire.patientQuesMap?.patientQuesMapId == questionnaireId) {
                questionnaire.copy(
                    patientQuesMap = questionnaire.patientQuesMap?.copy(isSubmitted = true)
                )
            } else {
                questionnaire
            }
        }
        _patientSharedQuestionnaires.value = updatedList
    }

}
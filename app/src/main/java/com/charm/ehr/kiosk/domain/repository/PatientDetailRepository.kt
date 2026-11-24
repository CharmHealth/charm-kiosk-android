package com.charm.ehr.kiosk.domain.repository

import com.charm.ehr.kiosk.domain.Country
import com.charm.ehr.kiosk.domain.DemographicsMetrics
import com.charm.ehr.kiosk.domain.DemographicsSetting
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.domain.QuestionnairePhysicianTemplate
import com.charm.ehr.kiosk.domain.State
import com.charm.ehr.kiosk.presentation.edit_profile.Insurance
import kotlinx.coroutines.flow.StateFlow

interface PatientDetailRepository {
    val patient: StateFlow<Patient?>
    val demographicsMetrics: DemographicsMetrics?
    val demographicsSetting: ArrayList<DemographicsSetting>?
    val countryList: List<Country>
    val countryStateMap: MutableMap<String, List<State>>
    val patientSharedQuestionnaires: StateFlow<List<QuestionnairePhysicianTemplate>>

    fun setPatient(patient: Patient)
    fun updatePatient(updated: Patient)
    fun setDemographicsMetrics(metrics: DemographicsMetrics)
    fun setCountryList(countries: List<Country>)
    fun setDemographicsSetting(settings: ArrayList<DemographicsSetting>)
    fun setStateToCountryMap(country: String, stateList: List<State>)
    fun setPrimaryInsurance(insurance: Insurance)
    fun setSecondaryInsurance(insurance: Insurance)
    fun clearData()
    fun setPatientSharedQuestionnaires(list: List<QuestionnairePhysicianTemplate>)
    fun submitQuestionnaire(questionnaireId: String)
}

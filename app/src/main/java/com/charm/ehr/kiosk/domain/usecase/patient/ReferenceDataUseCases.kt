package com.charm.ehr.kiosk.domain.usecase.patient

import javax.inject.Inject

data class ReferenceDataUseCases @Inject constructor(
    val fetchDemographicsSettings: FetchDemographicsSettingsUseCase,
    val fetchCountries: FetchCountriesUseCase,
    val fetchStates: FetchStatesUseCase
)

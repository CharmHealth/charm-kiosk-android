package com.charm.ehr.kiosk.domain.usecase.patient

import javax.inject.Inject

data class PatientInsuranceUseCases @Inject constructor(
    val getPayerDetails: GetPayerDetailsUseCase,
    val addPatientInsurances: AddPatientInsurancesUseCase,
    val updatePatientInsurances: UpdatePatientInsurancesUseCase
)

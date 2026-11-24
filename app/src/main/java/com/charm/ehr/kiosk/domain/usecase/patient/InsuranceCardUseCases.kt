package com.charm.ehr.kiosk.domain.usecase.patient

import javax.inject.Inject

data class InsuranceCardUseCases @Inject constructor(
    val uploadPatientImage: UploadPatientImageUseCase,
    val postInsuranceCardFront: PostInsuranceCardFrontUseCase,
    val postInsuranceCardBack: PostInsuranceCardBackUseCase,
    val getInsuranceCardFront: GetInsuranceCardFrontUseCase,
    val getInsuranceCardBack: GetInsuranceCardBackUseCase,
    val deleteInsuranceCardFront: DeleteInsuranceCardFrontUseCase,
    val deleteInsuranceCardBack: DeleteInsuranceCardBackUseCase
)

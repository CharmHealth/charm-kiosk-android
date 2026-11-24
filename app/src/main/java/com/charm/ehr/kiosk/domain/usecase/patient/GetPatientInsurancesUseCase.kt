package com.charm.ehr.kiosk.domain.usecase.patient

import com.charm.ehr.kiosk.common.util.ModelUtil
import com.charm.ehr.kiosk.domain.State
import com.charm.ehr.kiosk.domain.repository.PatientDetailRepository
import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.presentation.edit_profile.FieldState
import com.charm.ehr.kiosk.presentation.edit_profile.Insurance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Fetches the insurance list for a patient, resolves state/country objects from the
 * cached reference data, and stores primary/secondary insurances in [PatientDetailRepository].
 *
 * Encapsulates the mapping logic that was previously duplicated in PatientDetailViewmodel.
 * Requires [FetchCountriesUseCase] and [FetchStatesUseCase] to have run first so that
 * [PatientDetailRepository.countryList] and [PatientDetailRepository.countryStateMap] are populated.
 *
 * Returns [APIState.Success] with [Unit] on success — all results are stored in the cache.
 */
class GetPatientInsurancesUseCase @Inject constructor(
    private val patientRepository: PatientRepository,
    private val patientDetailRepository: PatientDetailRepository
) {
    suspend operator fun invoke(practicePatientId: String): APIState<Unit> =
        withContext(Dispatchers.IO) {
            val result = patientRepository.getPatientInsurances(practicePatientId)
            when (result) {
                is APIState.Success -> {
                    val insuranceList = result.data as List<Insurance>
                    val updatedInsuranceList = insuranceList.map { insurance ->
                        if (insurance.insuranceCategory.value == ModelUtil.primaryInsurance ||
                            insurance.insuranceCategory.value == ModelUtil.secondaryInsurance
                        ) {
                            val countryKey = insurance.insuredPersonDetail?.country?.value?.value
                            val stateMap = patientDetailRepository.countryStateMap
                            var stateList = emptyList<State>()
                            if (!countryKey.isNullOrBlank() && stateMap.containsKey(countryKey)) {
                                stateList = stateMap[countryKey] ?: emptyList()
                            }
                            val country = patientDetailRepository.countryList
                                .firstOrNull { it.value == countryKey }
                            val state = stateList.firstOrNull {
                                it.label == (insurance.insuredPersonDetail?.state?.value?.label ?: "")
                            } ?: insurance.insuredPersonDetail?.state?.value
                            insurance.copy(
                                insuredPersonDetail = insurance.insuredPersonDetail?.copy(
                                    state = FieldState(value = state),
                                    country = FieldState(value = country)
                                ),
                                selectedPayer = FieldState(insurance.selectedPayer.value)
                            )
                        } else {
                            insurance
                        }
                    }
                    val primaryInsurance = updatedInsuranceList.firstOrNull {
                        it.insuranceCategory.value == ModelUtil.primaryInsurance
                    }
                    primaryInsurance?.let { patientDetailRepository.setPrimaryInsurance(it) }

                    val secondaryInsurance = updatedInsuranceList.firstOrNull {
                        it.insuranceCategory.value == ModelUtil.secondaryInsurance
                    }
                    secondaryInsurance?.let { patientDetailRepository.setSecondaryInsurance(it) }

                    APIState.Success(Unit)
                }
                is APIState.Error -> APIState.Error(result.apiResponseError)
                else -> APIState.Success(Unit)
            }
        }
}

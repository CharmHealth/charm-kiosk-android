package com.charm.ehr.kiosk.domain.usecase.facility

import com.charm.ehr.kiosk.domain.Facility
import com.charm.ehr.kiosk.domain.repository.FacilityRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Returns the list of facilities mapped to this kiosk account.
 *
 * Business rule: when exactly one facility is returned and the caller is NOT navigating
 * from Settings, [FacilityListData.autoSelectSingle] is true — the UI should select that
 * facility automatically without showing the picker.
 */
class GetFacilityListUseCase @Inject constructor(
    private val facilityRepository: FacilityRepository
) {
    suspend operator fun invoke(isFromSettings: Boolean): APIState<FacilityListData> =
        withContext(Dispatchers.IO) {
            when (val result = facilityRepository.getMappedFacilityList()) {
                is APIState.Success -> APIState.Success(
                    FacilityListData(
                        facilities = result.data,
                        autoSelectSingle = result.data.size == 1 && !isFromSettings
                    )
                )
                is APIState.Error -> APIState.Error(result.apiResponseError)
                is APIState.Loading -> APIState.Loading
            }
        }
}

data class FacilityListData(
    val facilities: ArrayList<Facility>,
    val autoSelectSingle: Boolean
)

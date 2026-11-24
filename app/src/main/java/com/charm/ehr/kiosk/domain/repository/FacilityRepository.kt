package com.charm.ehr.kiosk.domain.repository

import com.charm.ehr.kiosk.domain.Facility
import com.charm.ehr.kiosk.presentation.composable.APIState

interface FacilityRepository {
    suspend fun getMappedFacilityList(): APIState<ArrayList<Facility>>
}

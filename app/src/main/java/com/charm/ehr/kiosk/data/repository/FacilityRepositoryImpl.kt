package com.charm.ehr.kiosk.data.repository
import com.charm.ehr.kiosk.domain.repository.FacilityRepository

import android.content.Context
import com.charm.ehr.kiosk.domain.Facility
import com.charm.ehr.kiosk.common.util.MdParsingHelper
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.data.network.WebService
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.common.util.CharmAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FacilityRepositoryImpl @Inject constructor(
    val webService: WebService,
    val authManager: AuthManager,
    @ApplicationContext val appContext: Context,
    private val charmAPI: CharmAPI
) : FacilityRepository {

    override suspend fun getMappedFacilityList(): APIState<ArrayList<Facility>> {
        return charmAPI.executeApiCall(
            call = { webService.getMappedFacilityList() },
            onSuccess = {
                MdParsingHelper.getFacilityList(it)
            }
        )
    }

}
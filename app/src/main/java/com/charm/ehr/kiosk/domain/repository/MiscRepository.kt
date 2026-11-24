package com.charm.ehr.kiosk.domain.repository

import android.content.Context
import android.os.Build
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.data.network.WebService
import com.google.gson.JsonElement
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Call
import javax.inject.Inject

interface MiscRepository {

    fun getAppDataForSupport(): String
}

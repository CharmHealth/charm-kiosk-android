package com.charm.ehr.kiosk.data.repository
import com.charm.ehr.kiosk.domain.repository.PreferenceRepository

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.charm.ehr.kiosk.domain.KioskSettings
import com.charm.ehr.kiosk.common.constants.PrefConstants
import com.charm.ehr.kiosk.presentation.dashboard.search_patient.SearchType
import com.charm.ehr.kiosk.common.util.ModelUtil
import com.charm.ehr.kiosk.domain.Facility
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferenceRepositoryImpl @Inject constructor(
    @ApplicationContext   appContext: Context,
)  : PreferenceRepository{
    private val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)

    override fun getFrequentSearchType(): String? = prefs.getString(PrefConstants.searchType, null)
    override fun setFrequentSearchType(searchType: SearchType) {
        prefs.edit { putString(PrefConstants.searchType, searchType.name) }
    }

    override fun putLastUpdatedTime(date: Long) {
        prefs.edit { putLong(PrefConstants.lastUpdatedDate, date) }
    }

    override fun getLastUpdatedTime(): Long {
        return prefs.getLong(PrefConstants.lastUpdatedDate, 0L)
    }

    override fun putDefaultGender(selectedGender: String) {
        prefs.edit { putString(PrefConstants.defaultGender, selectedGender) }
    }

    override fun getDefaultGender(): String? {
        return prefs.getString(PrefConstants.defaultGender, null)
    }

    override fun setKioskAccountDialogShown() {
        prefs.edit { putBoolean(PrefConstants.kioskAccountDialogShown, true) }
    }

    override fun getKioskAccountDialogShown(): Boolean {
        return prefs.getBoolean(PrefConstants.kioskAccountDialogShown, false)
    }

    override fun getKioskSettings(): KioskSettings? {
       return try {
            val json = prefs.getString(ModelUtil.kioskSettings, null)
            if (!json.isNullOrBlank()) {
                Gson().fromJson(json, KioskSettings::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.d("PreferenceRepository", e.toString())
            null
        }
    }

    override fun setSelectedFacilityId(facilityId: String) {
        prefs.edit { putString(ModelUtil.selectedFacilityId, facilityId) }
    }

    override fun setSelectedFacility(facility: Facility?) {
        val json = if (facility != null) Gson().toJson(facility) else null
        prefs.edit { putString(ModelUtil.selectedFacility, json) }
    }

    override fun getSelectedFacilityId(): String? {
        return prefs.getString(ModelUtil.selectedFacilityId, null)
    }

    override fun getSelectedFacility(): Facility? {
        val json = prefs.getString(ModelUtil.selectedFacility, null)
        return if (!json.isNullOrBlank()) {
            Gson().fromJson(json, Facility::class.java)
        } else {
            null
        }
    }
}
package com.charm.ehr.kiosk.domain.repository

import com.charm.ehr.kiosk.domain.Facility
import com.charm.ehr.kiosk.domain.KioskSettings
import com.charm.ehr.kiosk.presentation.dashboard.search_patient.SearchType

interface PreferenceRepository {
    fun getFrequentSearchType(): String?
    fun setFrequentSearchType(searchType: SearchType)
    fun putLastUpdatedTime(date: Long)
    fun getLastUpdatedTime(): Long
    fun putDefaultGender(selectedGender: String)
    fun getDefaultGender(): String?
    fun setKioskAccountDialogShown()
    fun getKioskAccountDialogShown(): Boolean
    fun getKioskSettings(): KioskSettings?
    fun setSelectedFacilityId(facilityId: String)
    fun setSelectedFacility(facility: Facility?)
    fun getSelectedFacilityId(): String?
    fun getSelectedFacility(): Facility?
}

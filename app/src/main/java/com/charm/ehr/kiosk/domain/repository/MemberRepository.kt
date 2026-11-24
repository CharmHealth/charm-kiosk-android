package com.charm.ehr.kiosk.domain.repository

import android.net.Uri
import com.charm.ehr.kiosk.domain.AppointmentStatus
import com.charm.ehr.kiosk.domain.KioskSettings
import com.charm.ehr.kiosk.presentation.composable.APIState

interface MemberRepository {
    suspend fun fetchAndCacheSettings(): APIState<KioskSettings?>
    suspend fun getAppointmentStatus(): APIState<ArrayList<AppointmentStatus>>
    suspend fun verifyPassword(userName: String, password: String): APIState<String>
    suspend fun clearCachedString(key: String)
    suspend fun sendSupport(
        categoryTxt: String, subjectTxt: String, messageTxt: String, fileIds: String? ): APIState<Boolean>
    suspend fun uploadSupportAttachmentsFromUris(
        uriList: List<Uri>
    ): APIState<ArrayList<String>>
}

package com.charm.ehr.kiosk.domain.repository

import java.util.UUID

interface WorkerRepository {
    fun triggerUpdateDataWork(fetchSettings: Boolean): UUID
    fun scheduleDailyUpdateDataWork()
    fun retryUpdateDataWork(failedApiKeys: String, fetchSettings: Boolean): UUID
}

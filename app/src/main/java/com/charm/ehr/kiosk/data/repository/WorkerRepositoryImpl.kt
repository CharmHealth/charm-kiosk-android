package com.charm.ehr.kiosk.data.repository
import com.charm.ehr.kiosk.domain.repository.WorkerRepository

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.charm.ehr.kiosk.common.util.ModelUtil
import com.charm.ehr.kiosk.data.worker.UpdateDataFromServerWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WorkerRepository {

    override fun triggerUpdateDataWork(fetchSettings: Boolean): UUID {
        val workManager = WorkManager.getInstance(context)
        val inputData = Data.Builder()
            .putBoolean(UpdateDataFromServerWorker.KEY_SHOULD_FETCH_SETTINGS, fetchSettings)
            .putBoolean(UpdateDataFromServerWorker.ALWAYS_EXECUTE, true)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<UpdateDataFromServerWorker>()
            .setInputData(inputData)
            .build()
        workManager.enqueueUniqueWork(
            uniqueWorkName = ModelUtil.UpdateDataWorkerName + "_one_time",
            existingWorkPolicy = androidx.work.ExistingWorkPolicy.KEEP,
            request = workRequest
        )
        return workRequest.id
    }

    override fun scheduleDailyUpdateDataWork() {
        val workManager = WorkManager.getInstance(context.applicationContext)
        val inputData =
            Data.Builder().putBoolean(UpdateDataFromServerWorker.KEY_SHOULD_FETCH_SETTINGS, false)
                .build()
        val workRequest =
            PeriodicWorkRequestBuilder<UpdateDataFromServerWorker>(12, TimeUnit.HOURS)
                .setInputData(inputData)
                .build()
        workManager.enqueueUniquePeriodicWork(
            ModelUtil.UpdateDataWorkerName + "_periodic",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun retryUpdateDataWork(failedApiKeys: String, fetchSettings: Boolean): UUID {
        val workManager = WorkManager.getInstance(context)
        val inputData = Data.Builder()
            .putString(UpdateDataFromServerWorker.KEY_RETRY_API_KEYS, failedApiKeys)
            .putBoolean(UpdateDataFromServerWorker.KEY_SHOULD_FETCH_SETTINGS, fetchSettings)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<UpdateDataFromServerWorker>()
            .setInputData(inputData)
            .build()
        workManager.enqueueUniqueWork(
            uniqueWorkName = ModelUtil.UpdateDataWorkerName + "_one_time",
            existingWorkPolicy = androidx.work.ExistingWorkPolicy.KEEP,
            request = workRequest
        )
        return workRequest.id
    }
}
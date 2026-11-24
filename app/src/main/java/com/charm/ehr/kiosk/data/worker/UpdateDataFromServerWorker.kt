package com.charm.ehr.kiosk.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.charm.ehr.kiosk.domain.Facility
import com.charm.ehr.kiosk.common.KioskUserType
import com.charm.ehr.kiosk.domain.repository.FacilityRepository
import com.charm.ehr.kiosk.domain.repository.MemberRepository
import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.domain.repository.PreferenceRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.common.util.ModelUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

@HiltWorker
class UpdateDataFromServerWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val memberRepository: MemberRepository,
    private val preferenceRepository: PreferenceRepository,
    private val facilityRepository: FacilityRepository,
    private val patientRepository: PatientRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = coroutineScope {
        val failedApiKeys = mutableListOf<String>()
        val retryApiKeys = inputData.getString(KEY_RETRY_API_KEYS)?.split(",")?.map { it.trim() }
            ?.filter { it.isNotEmpty() }?.toSet()
        val alwaysExecute = inputData.getBoolean(ALWAYS_EXECUTE, false)
        if (!alwaysExecute) {
            // Check if last updated within the last hour
            val lastUpdatedTime = preferenceRepository.getLastUpdatedTime()
            val currentTime = System.currentTimeMillis()
            val oneHourInMillis = 60 * 60 * 1000L

            // If retrying specific APIs, skip this check
            if (retryApiKeys == null && lastUpdatedTime != 0L && (currentTime - lastUpdatedTime) < oneHourInMillis) {
                return@coroutineScope Result.success()
            }
        }

        try {
            val deferredCalls =
                mutableListOf<Pair<String, Deferred<APIState<*>>>>()
            if (retryApiKeys == null || retryApiKeys.contains(ModelUtil.API_KEY_DEMOGRAPHICS_METRICS))
                deferredCalls += ModelUtil.API_KEY_DEMOGRAPHICS_METRICS to async { patientRepository.fetchAndCachePatientDemographicsMetrics() }
            if (retryApiKeys == null || retryApiKeys.contains(ModelUtil.API_KEY_DEMOGRAPHICS_SETTINGS))
                deferredCalls += ModelUtil.API_KEY_DEMOGRAPHICS_SETTINGS to async { patientRepository.fetchAndCachePatientDemographicsSettings() }
            if (retryApiKeys == null || retryApiKeys.contains(ModelUtil.API_KEY_COUNTRIES))
                deferredCalls += ModelUtil.API_KEY_COUNTRIES to async { patientRepository.fetchAndCacheCountries() }
            if (retryApiKeys == null || retryApiKeys.contains(ModelUtil.API_KEY_STATES_INDIA))
                deferredCalls += ModelUtil.API_KEY_STATES_INDIA to async {
                    patientRepository.fetchAndCacheStates(
                        ModelUtil.countryStringIndia
                    )
                }
            if (retryApiKeys == null || retryApiKeys.contains(ModelUtil.API_KEY_STATES_US))
                deferredCalls += ModelUtil.API_KEY_STATES_US to async {
                    patientRepository.fetchAndCacheStates(
                        ModelUtil.countryStringUs
                    )
                }
            if (retryApiKeys == null || retryApiKeys.contains(ModelUtil.API_KEY_STATES_AUSTRALIA))
                deferredCalls += ModelUtil.API_KEY_STATES_AUSTRALIA to async {
                    patientRepository.fetchAndCacheStates(
                        ModelUtil.countryStringAustralia
                    )
                }

            val kioskSettings = preferenceRepository.getKioskSettings()
            val isStandAloneUser =
                kioskSettings?.accountDetails?.kioskType == KioskUserType.STANDALONE_USER.displayName
            if ((retryApiKeys == null || retryApiKeys.contains(ModelUtil.API_KEY_FACILITY_LIST)) && isStandAloneUser) {
                deferredCalls += ModelUtil.API_KEY_FACILITY_LIST to async { facilityRepository.getMappedFacilityList() }
            }
            val shouldFetchSettings = inputData.getBoolean(KEY_SHOULD_FETCH_SETTINGS, false)
            if ((retryApiKeys == null || retryApiKeys.contains(ModelUtil.API_KEY_SETTINGS)) && shouldFetchSettings) {
                deferredCalls += ModelUtil.API_KEY_SETTINGS to async { memberRepository.fetchAndCacheSettings() }
            }

            val results = deferredCalls.map { it.first to it.second.await() }
            for ((key, result) in results) {
                if (result !is APIState.Success) {
                    failedApiKeys.add(key)
                }
            }
            if (failedApiKeys.isNotEmpty()) {
                val firstError =
                    results.firstOrNull { it.second is APIState.Error }?.second as? APIState.Error
                return@coroutineScope Result.failure(
                    workDataOf(
                        KEY_ERROR to (firstError?.apiResponseError?.errorMsg ?: "Unknown error"),
                        KEY_FAILED_API_KEYS to failedApiKeys.joinToString(",")
                    )
                )
            }
            if (isStandAloneUser) {
                val facilityData = results.find { it.first == "facilityList" }?.second
                if (facilityData is APIState.Success) {
                    val facility = (facilityData.data as List<Facility>).firstOrNull()
                    preferenceRepository.setSelectedFacilityId(facility?.facilityId ?: "")
                    preferenceRepository.setSelectedFacility(facility)
                }
            }
            preferenceRepository.putLastUpdatedTime(System.currentTimeMillis())
            Result.success()
        } catch (e: Exception) {
            Result.failure(
                workDataOf(
                    KEY_ERROR to (e.message ?: "Unknown error"),
                    KEY_FAILED_API_KEYS to failedApiKeys.joinToString(",")
                )
            )
        }
    }

    companion object {
        const val KEY_SHOULD_FETCH_SETTINGS = "should_fetch_settings"
        const val KEY_ERROR = "error"
        const val KEY_FAILED_API_KEYS = "failed_api_keys"
        const val KEY_RETRY_API_KEYS = "retry_api_keys"
        const val ALWAYS_EXECUTE = "always_execute"
    }
}

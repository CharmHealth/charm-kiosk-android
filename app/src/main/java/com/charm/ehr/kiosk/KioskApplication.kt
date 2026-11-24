package com.charm.ehr.kiosk

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.common.util.AppticsUtil
import com.charm.ehr.kiosk.common.util.FileUtil
import com.charm.ehr.kiosk.common.util.KioskCrashHandler
import com.charm.ehr.kiosk.presentation.tour_page.SessionManager
import com.zoho.apptics.common.Apptics
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private val DATASTORE_NAME = "kiosk_datastore_preference"
val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

@HiltAndroidApp
class KioskApplication() : Application(), Configuration.Provider {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var authManager: AuthManager

    override val workManagerConfiguration: Configuration
        get() {
            return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
        }


    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler(KioskCrashHandler(this))

        // Load the stored sign-in (CharmAuth) before anything calls the API.
        authManager.startRestoringSession()

        initApptics()

        CoroutineScope(Dispatchers.IO).launch {
            FileUtil.clearTempFiles(applicationContext)
        }
    }


    private fun initApptics() {
        Apptics.resetOnDCSwitch = true
        AppticsUtil.initApptics(this)
    }



}
package com.charm.ehr.kiosk.common.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import androidx.preference.PreferenceManager
import com.charm.ehr.kiosk.presentation.tour_page.MainActivity
import com.zoho.apptics.crash.AppticsNonFatals
import kotlin.system.exitProcess

class KioskCrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    companion object {
        private const val TAG = "KioskCrashHandler"
        private const val PREFS_NAME = "crash_handler_prefs"
        private const val KEY_CRASH_COUNT = "crash_count"
        private const val KEY_LAST_CRASH_TIME = "last_crash_time"
        private const val MAX_CRASHES = 2
        private const val CRASH_RESET_TIME_MS = 60000L // 1 minute
    }

    //Note: If app crashes more than MAX_CRASHES times within CRASH_RESET_TIME_MS, it will not restart automatically.

    @SuppressLint("ApplySharedPref") // commit() is required before killing process
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            Log.d(TAG, "App crashed! Exception: ${throwable.message}", throwable)

            // Record the crash to Apptics
            AppticsNonFatals.recordException(throwable)

            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val currentTime = System.currentTimeMillis()
            val lastCrashTime = prefs.getLong(KEY_LAST_CRASH_TIME, 0L)
            var crashCount = prefs.getInt(KEY_CRASH_COUNT, 0)

            Log.d(TAG, "Current crash count: $crashCount, Last crash: ${currentTime - lastCrashTime}ms ago")

            // Reset crash count if enough time has passed since last crash
            if (currentTime - lastCrashTime > CRASH_RESET_TIME_MS) {
                crashCount = 0
                Log.d(TAG, "Reset crash count to 0")
            }

            crashCount++

            // Save crash information
            prefs.edit()
                .putInt(KEY_CRASH_COUNT, crashCount)
                .putLong(KEY_LAST_CRASH_TIME, currentTime)
                .commit()

            Log.d(TAG, "Saved crash count: $crashCount")

            // If too many crashes in short period, don't restart - use default handler
            if (crashCount > MAX_CRASHES) {
                Log.d(TAG, "Too many crashes ($crashCount), using default handler")
                defaultHandler?.uncaughtException(thread, throwable)
                return
            }

            Log.d(TAG, "Scheduling app restart...")
            restartApp()

        } catch (e: Exception) {
            Log.d(TAG, "Failed in crash handler", e)

            AppticsNonFatals.recordException(throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun restartApp() {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
            Log.d(TAG, "Restart intent sent")
        } catch (e: Exception) {
            AppticsNonFatals.recordException(e)
            Log.d(TAG, "Failed to send restart intent", e)
        } finally {
            // Kill the app process
            Process.killProcess(Process.myPid())
            exitProcess(0)
        }
    }
}
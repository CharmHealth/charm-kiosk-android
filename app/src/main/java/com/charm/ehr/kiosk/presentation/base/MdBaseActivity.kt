package com.charm.ehr.kiosk.presentation.base

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.charm.ehr.kiosk.presentation.tour_page.SessionManager
import com.charm.ehr.kiosk.common.util.ModelUtil
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class MdBaseActivity : AppCompatActivity() {

    val mTAG = "MdBaseActivity"

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(mTAG, "MdBaseActivity onCreate $this")
    }

    override fun onResume() {
        super.onResume()
        Log.d(mTAG, "MdBaseActivity onResume $this")
        if (sessionManager.getIsPasscodeAllowed()) {
            val reEnterTime = System.currentTimeMillis() / 1000
            val exitTime = pref.getLong(ModelUtil.exitTimeInSeconds, 0)
            val timeDifference = (exitTime.minus(reEnterTime)).unaryMinus()
            if (exitTime != 0L && timeDifference !in 0..TimeUnit.SECONDS.toSeconds(30) && timeDifference >= 0) { // time difference should not be in range of 0 to 1800
                sessionManager.lock()
            }
            Log.d(mTAG, "onResume reEnterTime = $reEnterTime , ExitTime $exitTime")
            Log.d(mTAG, "onResume timeDifference = $timeDifference")

        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(mTAG, "MdBaseActivity onPause $this")
        if (sessionManager.getIsPasscodeAllowed()) {

            val exitTime = System.currentTimeMillis() / 1000

            pref.edit(commit = true) { putLong(ModelUtil.exitTimeInSeconds, exitTime) }

        } else pref.edit(commit = true) { putLong(ModelUtil.exitTimeInSeconds, 0) }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(mTAG, "MdBaseActivity onDestroy $this")
    }

}
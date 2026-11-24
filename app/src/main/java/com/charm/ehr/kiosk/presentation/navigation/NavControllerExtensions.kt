package com.charm.ehr.kiosk.presentation.navigation

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

//Note:This logic prevent same screen presenting twice in a row

fun NavController.safeNavigate(
    route: MdRoute,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    val currentRoute = currentDestination?.route ?: ""
    val targetClassName = route::class.simpleName ?: ""

    if (currentRoute.contains(targetClassName)) {
        return
    }

    hideKeyboard()
    navigate(route, builder)
}

fun NavController.hideKeyboard() {
    try {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        (context as? Activity)?.window?.decorView?.windowToken?.let { token ->
            imm.hideSoftInputFromWindow(token, 0)
        }
    } catch (e: Exception) {
        Log.d("NavController.hideKeyboard", "Error hiding keyboard: ${e.message}")
    }
}

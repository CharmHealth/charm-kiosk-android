package com.charm.ehr.kiosk.presentation.tour_page

import UiText
import com.charm.ehr.kiosk.common.SignInFailure

data class TourPageState(
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val showNoPrivilegeError: Boolean = false,
    val loginFailure: SignInFailure? = null,
    val isUserVerified : Boolean = false,
    val isLoggedIn : Boolean = true,
    val showEducationDialog: Boolean = false,
    val updateDataErrorMessage: UiText? = null,
    val updateDataFailedAPIs: String?=null,
    val showKioskTypeMismatchError: Boolean = false
)
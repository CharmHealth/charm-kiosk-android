package com.charm.ehr.kiosk.presentation.tour_page

sealed interface TourPageAction {
    object OnClickLogin : TourPageAction
}
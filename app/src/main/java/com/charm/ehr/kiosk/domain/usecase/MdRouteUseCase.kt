package com.charm.ehr.kiosk.domain.usecase

import com.charm.ehr.kiosk.presentation.navigation.MdRoute
import com.charm.ehr.kiosk.common.util.SecurityUtil
import javax.inject.Inject

class MdRouteUseCase @Inject constructor() {

    fun passCode(): MdRoute.Passcode =
        MdRoute.Passcode(
            fromIdleState = false,
            createPasscode = false,
            selectAuthType = null,
            isFromSetting = false,
            isFromVerification = false
        )

    fun settingsCreatePasscode(authType: SecurityUtil.AuthType?): MdRoute.Passcode =
        MdRoute.Passcode(
            createPasscode = true,
            selectAuthType = authType,
            isFromSetting = true
        )

    fun settingsVerification(): MdRoute.Passcode =
        MdRoute.Passcode(
            isFromSetting = true,
            isFromVerification = true
        )

    fun passcodeVerification(): MdRoute.Passcode =
        MdRoute.Passcode(isFromVerification = true)

    fun createPasscode(authType: SecurityUtil.AuthType?): MdRoute.Passcode =
        MdRoute.Passcode(
            createPasscode = true,
            selectAuthType = authType
        )

    fun passcodeFromSettings(): MdRoute.Passcode =
        MdRoute.Passcode(
            isFromSetting = true
        )

    fun passcodeFromIdleState(): MdRoute.Passcode =
        MdRoute.Passcode(
            fromIdleState = true
        )

    fun settingFromLogin(): MdRoute.Settings =
        MdRoute.Settings(
            isFromLogin = true
        )

    fun settings(): MdRoute.Settings =
        MdRoute.Settings(false)

    fun selectFacilityFromSettings(): MdRoute.SelectFacility =
        MdRoute.SelectFacility(
            fromSettings = true
        )

    fun selectFacility(): MdRoute.SelectFacility =
        MdRoute.SelectFacility(false)

    fun authTypeSelect(): MdRoute.AuthTypeSelect =
        MdRoute.AuthTypeSelect()

    fun authTypeSelectFromSetting(): MdRoute.AuthTypeSelect =
        MdRoute.AuthTypeSelect(isFromSetting = true)

    // Add more as needed for your scenarios
}


package com.charm.ehr.kiosk.presentation.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.util.SecurityUtil.isLocalAuthConfigured
import com.charm.ehr.kiosk.common.util.UIUtil
import com.charm.ehr.kiosk.data.worker.UpdateDataFromServerWorker
import com.charm.ehr.kiosk.domain.Facility
import com.charm.ehr.kiosk.presentation.composable.MdAlertDialog
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdCircularProgressBar
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import java.util.UUID

data class SettingsState(
    val defaultGender: String = "",
    val appointmentStatus: String = "",
    val lastUpdatedTime: Long = 0L,
    val isLoading: Boolean = false,
    val toastMessage: Int? = null,
    val showLogoutConfirmDialog: Boolean = false,
)

@Composable
fun SettingsScreen(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    selectedFacility: Facility?,
    isStandAloneUser: Boolean,
    refreshWorkId: UUID?
) {
    var showAppointmentStatusDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val appContext = context.applicationContext
    val appVersion = remember {
        try {
            appContext.packageManager
                .getPackageInfo(appContext.packageName, 0)
                .versionName ?: "NA"
        } catch (e: Exception) {
            "NA"
        }
    }
    LaunchedEffect(refreshWorkId) {
        refreshWorkId?.let { id ->
            WorkManager.getInstance(context.applicationContext)
                .getWorkInfoByIdFlow(id).collect { workInfo ->
                    if (workInfo != null && workInfo.state.isFinished) {
                        val success = workInfo.state == WorkInfo.State.SUCCEEDED
                        val error =
                            workInfo.outputData.getString(UpdateDataFromServerWorker.KEY_ERROR)
                        onAction(
                            SettingsAction.onRefreshWorkCompleted(
                                success = success,
                                error = error ?: ""
                            )
                        )
                    }
                }
        }
    }

    if (state.toastMessage != null) {
        LaunchedEffect(state.toastMessage) {
            Toast.makeText(
                context,
                state.toastMessage,
                Toast.LENGTH_SHORT
            ).show()
            onAction(SettingsAction.ClearToastMessage)
        }
    }

    if (state.showLogoutConfirmDialog) {
        MdAlertDialog(
            message = stringResource(R.string.areYouSureWantToLogout),
            primaryButtonResId = R.string.alertDialogTextYes,
            secondaryButtonResId = R.string.alertDialogTextNo,
            onPrimaryAction = {
                onAction(SettingsAction.DismissLogoutConfirmDialog)
                onAction(SettingsAction.OnLogout)
            },
            onSecondaryAction = {
                onAction(SettingsAction.DismissLogoutConfirmDialog)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        SettingTitleText(text = stringResource(R.string.facility))
        SettingsCard {
            DetailText(label = selectedFacility?.facilityName ?: "", value = "", onClick = {
                if (!isStandAloneUser) {
                    onAction(SettingsAction.OnSwitchFacility)
                }
            }, showIcon = !isStandAloneUser)
        }
        if (!isStandAloneUser || state.appointmentStatus.isNotEmpty()) {
            SettingTitleText(text = stringResource(R.string.preference_str))
            SettingsCard {
                if (!isStandAloneUser) {
                    DetailText(
                        label = stringResource(R.string.default_gender),
                        value = state.defaultGender.replaceFirstChar { it.uppercase() },
                        onClick = {
                            onAction(SettingsAction.OnSelectGender)
                        }
                    )
                }

                if (showAppointmentStatusDialog) {
                    MdAlertDialog(
                        heading = stringResource(R.string.check_in_appointment_status),
                        message = stringResource(R.string.appointment_status_configure_step_msg),
                        primaryButtonResId = R.string.alertDialogOkTextNoI18N,
                        secondaryButtonResId = R.string.learn_more,
                        onPrimaryAction = { showAppointmentStatusDialog = false },
                        onSecondaryAction = {
                            showAppointmentStatusDialog = false
                            onAction(SettingsAction.OnAppointmentStatus)
                        }
                    )
                }

                if (state.appointmentStatus.isNotEmpty()) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MdColors.White
                    )
                    DetailText(
                        label = stringResource(R.string.check_in_appointment_status),
                        value = state.appointmentStatus,
                        onClick = {
                            showAppointmentStatusDialog = true
                        },
                        iconResource = R.drawable.ic_info,
                        modifier = Modifier
                    )
                }
            }
        }

        if (context.isLocalAuthConfigured()) {
            SettingTitleText(text = stringResource(R.string.security))

            SettingsCard {
                DetailText(
                    label = stringResource(R.string.app_lock),
                    description = stringResource(R.string.app_lock_desc),
                    value = "",
                    onClick = {
                        onAction(SettingsAction.onAppLockChange)
                    }
                )
            }
        }


//        SettingTitleText(text = "Security")
//        SettingsCard {
//            DetailText(
//                text = stringResource(R.string.guided_access),
//                onClick = {
//                    onAction(SettingsAction.OnTermsOfService)
//                },
//                showIcon = true
//            )
//        }


        SettingTitleText(text = stringResource(R.string.more_settings))
        SettingsCard {
            //Removed temporarily will be added later
//            DetailText(
//                label = stringResource(R.string.guided_access),
//                onClick = {
//                    onAction(SettingsAction.OnTermsOfService)
//                }
//            )
//            HorizontalDivider(
//                thickness = 1.dp,
//                color = MdColors.White
//            )
            val time = UIUtil.getTimeAgo(state.lastUpdatedTime)
            DetailText(
                label = stringResource(R.string.refresh_data),
                subLabel = "  ${stringResource(R.string.updated)} $time",
                onClick = {
                    onAction(SettingsAction.OnUpdateData)
                },
                showTextButton = true,
                iconResource = R.drawable.ic_refresh,
                iconColor = MdColors.BrandPink,
                isLoading = state.isLoading,
                modifier = Modifier
            )
        }
        SettingTitleText(text = stringResource(R.string.help))
        SettingsCard {
            DetailText(
                label = stringResource(R.string.contact_support),
                onClick = {
                    onAction(SettingsAction.OnContactSupport)
                }
            )
        }
        SettingTitleText(text = stringResource(R.string.info))
        SettingsCard {
//            DetailText(
//                text = stringResource(R.string.about_us),
//                onClick = { onAction(SettingsAction.) },
//                showIcon = true
//            )
            DetailText(
                label = stringResource(R.string.terms_of_service),
                onClick = {
                    onAction(SettingsAction.OnTermsOfService)
                }
            )
            HorizontalDivider(
                thickness = 1.dp,
                color = MdColors.White
            )
            DetailText(
                label = stringResource(R.string.privacy_policy),
                onClick = {
                    onAction(SettingsAction.OnPrivacyPolicy)
                }
            )
        }

        //TODO: Apptics crash access
        //TODO: Default Gender
        //TODO: Lock time out

        OutlinedButton(
            modifier = Modifier.padding(horizontal =MdTheme.dimension.contentCardPadding, vertical = 16.dp),
            onClick = {
                onAction(SettingsAction.ShowLogoutConfirmDialog)
            },
            border = BorderStroke(1.dp, MdColors.RedMatt)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                text = stringResource(R.string.sign_out),
                color = MdColors.RedMatt,
                style = MdTheme.typography.semiBold16TextStyle
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                painter = painterResource(R.drawable.ic_logout),
                tint = MdColors.RedMatt,
                contentDescription = null
            )
        }


        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.watermark_logo),
                contentDescription = null,
                tint = MdColors.LavenderMist
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = appVersion,
                style = MdTheme.typography.semiBold18TextStyle.copy(color = MdColors.LavenderMist)
            )
        }
    }
}


@Composable
private fun DetailText(
    modifier: Modifier = Modifier.rotate(270f),
    label: String,
    subLabel: String? = null,
    description: String = "",
    value: String = "",
    onClick: () -> Unit,
    showTextButton: Boolean = false,
    iconResource: Int = R.drawable.ic_arrow_down,
    iconColor: Color = Color.Unspecified,
    showIcon: Boolean = true,
    isLoading: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val colModifier =
                if (description.isNotEmpty()) Modifier.padding(vertical = 18.dp) else Modifier
            Column(
                modifier = colModifier
                    .weight(1f)
                    .padding(horizontal = 18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        style = MdTheme.typography.medium16TextStyle.copy(color = MdColors.CharcoalGrey),
                    )
                    subLabel?.let {
                        Text(
                            modifier = Modifier.padding(top = 2.dp),
                            text = subLabel,
                            style = MdTheme.typography.medium14TextStyle.copy(color = MdColors.WarmGrey),
                        )
                    }
                }

                if (description.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MdTheme.typography.regular16TextStyle.copy(color = MdColors.DeepGrey),
                    )
                }
            }
            if (showTextButton) {
                if (isLoading) {
                    Row(
                        modifier = Modifier
                            .padding(end = 32.dp)
                            .wrapContentSize()
                    ) {
                        MdCircularProgressBar(modifier = Modifier.size(24.dp), size = 24.dp)
                    }
                } else
                    TextButton(onClick = onClick, modifier = Modifier.padding(end = 14.dp)) {
//                        Text(
//                            text = stringResource(R.string.refresh),
//                            style = MdTheme.typography.semiBold18TextStyle.copy(
//                                MdColors.BrandPink
//                            )
//                        )
//                        Spacer(Modifier.width(4.dp))
                        Icon(
                            modifier = Modifier.rotate(270f),
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = null
                        )
                    }
            } else {
                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(end = MdTheme.dimension.contentCardPadding)
                ) {
                    if (value.isNotEmpty()) {
                        Text(
                            modifier = Modifier
                                .wrapContentWidth(),
                            text = value,
                            style = MdTheme.typography.regular16TextStyle.copy(color = MdColors.DeepGrey),
                        )
                    }
                    if (showIcon) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            modifier = modifier,
                            tint = iconColor,
                            painter = painterResource(iconResource),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingTitleText(text: String) {
    Text(
        modifier = Modifier.padding(
            start = MdTheme.dimension.contentCardPadding,
            end = MdTheme.dimension.contentCardPadding,
            top = 16.dp,
            bottom = 0.dp
        ),
        text = text,
        style = MdTheme.typography.regular16TextStyle.copy(color = MdColors.DeepGrey)
    )
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    MdCardWithoutTonalElevation(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MdTheme.dimension.contentCardPadding, vertical = 8.dp),
        elevation = 0.dp,
        colors = MdColors.MdSurfaceColor
    ) {
        Column {
            content()
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1200)
@Composable
fun SettingsScreenPreview() {
    MdTheme {
        SettingsScreen(
            state = SettingsState(appointmentStatus = "Checked In", defaultGender = "male"),
            onAction = {},
            selectedFacility = Facility(facilityName = "Charm Health Clinic"),
            isStandAloneUser = true,
            refreshWorkId = null
        )

    }
}
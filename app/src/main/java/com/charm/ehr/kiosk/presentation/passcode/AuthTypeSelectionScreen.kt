package com.charm.ehr.kiosk.presentation.passcode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.composable.CharmKioskLogoHeader
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.SelectionCardItem
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import com.charm.ehr.kiosk.common.util.SecurityUtil.AuthType


@Composable
fun AuthTypeSelectionScreen(
    onAuthTypeSelected: (AuthType) -> Unit,
    selectedType: AuthType? = null,
    onBackPressed: () -> Unit,
    isFromSettings: Boolean
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MdColors.MdSurfaceColor),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CharmKioskLogoHeader(
                onBackPressed = onBackPressed, showBackPress = isFromSettings
            )
            AuthTypeSelectionCard(
                onAuthTypeSelected = onAuthTypeSelected,
                selectedType = selectedType,
                isFromSettings
            )
        }
    }
}

@Composable
fun AuthTypeSelectionCard(
    onAuthTypeSelected: (AuthType) -> Unit,
    selectedType: AuthType? = null,
    isFromSettings: Boolean
) {
    MdCardWithoutTonalElevation(
        modifier = Modifier
            .fillMaxWidth(0.6f) // 70% of the screen width
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(MdTheme.dimension.cardCornerRadius),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            if (!isFromSettings) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lock_with_bg),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.app_lock_required),
                            style = MdTheme.typography.bold18TextStyle
                        )
                        Text(
                            text = stringResource(R.string.select_preferred_unlock_method),
                            style = MdTheme.typography.medium16TextStyle,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MdColors.Black5
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            Text(
                text = stringResource(R.string.select_unlock_method),
                style = MdTheme.typography.semiBold16TextStyle,
            )
            Spacer(modifier = Modifier.height(24.dp))
            SelectionCardItem(
                selected = selectedType == AuthType.SYSTEM_LOCK,
                heading = stringResource(R.string.use_phone_lock),
                message = stringResource(R.string.unlock_using_phone_lock),
                onClick = { onAuthTypeSelected(AuthType.SYSTEM_LOCK) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SelectionCardItem(
                selected = selectedType == AuthType.ACCOUNT_CREDENTIALS,
                heading = stringResource(R.string.use_account_password),
                message = stringResource(R.string.unlock_using_account_password),
                onClick = { onAuthTypeSelected(AuthType.ACCOUNT_CREDENTIALS) }
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 800, heightDp = 1200)
@Composable
fun AuthTypeSelectionScreenPreview() {
    MdTheme {
        AuthTypeSelectionScreen(
            onAuthTypeSelected = {},
            AuthType.SYSTEM_LOCK, isFromSettings = true, onBackPressed = {}
        )
    }
}

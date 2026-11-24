package com.charm.ehr.kiosk.presentation.composable

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.UserStatus
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import kotlinx.coroutines.delay

@Composable
fun ConnectivityStatusBar(
    userStatus: UserStatus,
    modifier: Modifier = Modifier
) {
    var showOnlineBar by rememberSaveable { mutableStateOf(false) }
    var showOfflineBar by rememberSaveable { mutableStateOf(false) }

    val view = LocalView.current
    val window = (view.context as? Activity)?.window

    val onlineText = stringResource(R.string.you_are_online)
    val offlineText = stringResource(R.string.you_are_offline)


    SideEffect {
        window?.let {
            val insetsController = WindowCompat.getInsetsController(it, view)

            val isLightStatusBar = !(showOnlineBar || showOfflineBar)
            insetsController.isAppearanceLightStatusBars = isLightStatusBar
        }
    }

    // Handle online state - show for 5 seconds then hide
    LaunchedEffect(userStatus) {
        when (userStatus) {
            UserStatus.ONLINE -> {
                showOnlineBar = true && showOfflineBar
                showOfflineBar = false
                delay(5000L) // Show for 5 seconds
                showOnlineBar = false
            }

            UserStatus.OFFLINE -> {
                showOnlineBar = false
                showOfflineBar = true
            }
        }
    }

    // Online status bar (green, auto-hide after 5 seconds)
    AnimatedVisibility(
        visible = showOnlineBar,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        )
    ) {
        StatusBar(
            text = onlineText,
            backgroundColor = MdColors.LightGreen,
            modifier = modifier
        )
    }

    // Offline status bar (grey, persistent)
    AnimatedVisibility(
        visible = showOfflineBar,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        )
    ) {
        StatusBar(
            text = offlineText,
            backgroundColor = MdColors.Black50,
            modifier = modifier
        )
    }
}

@Composable
fun StatusBar(
    text: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MdTheme.typography.semiBold16TextStyle.copy(letterSpacing = 0.5.sp),
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.displayCutoutPadding()
        )

    }
}

@Preview(showBackground = true, widthDp = 800)
@Composable
fun OnlineStatusBarPreview() {
    MdTheme {
        StatusBar(
            text = stringResource(R.string.you_are_online),
            backgroundColor = MdColors.LightGreen
        )
    }
}

@Preview(showBackground = true, widthDp = 800)
@Composable
fun OfflineStatusBarPreview() {
    MdTheme {
        StatusBar(
            text = stringResource(R.string.you_are_offline),
            backgroundColor = MdColors.Black50
        )
    }
}

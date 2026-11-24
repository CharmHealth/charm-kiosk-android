package com.charm.ehr.kiosk.presentation.passcode

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import UiText
import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.composable.CharmKioskLogoHeader
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdCommonPrimaryButton
import com.charm.ehr.kiosk.presentation.composable.MdErrorText
import com.charm.ehr.kiosk.presentation.composable.MdOutlinedTextField
import com.charm.ehr.kiosk.presentation.composable.MdVerticalSpace
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import com.charm.ehr.kiosk.common.util.SecurityUtil
import com.charm.ehr.kiosk.common.util.SecurityUtil.presentLockScreen
import java.util.concurrent.Executor

@Composable
fun PasscodeScreen(
    state: PasscodeState, onAction: (PasscodeAction) -> Unit,
    isToCreatePasscode: Boolean,
    passcodeType: SecurityUtil.AuthType?,
    isFromSettings: Boolean
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val isPortrait =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    Column(modifier = Modifier.fillMaxSize()) {
        CharmKioskLogoHeader()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MdColors.MdSurfaceColor)
                .windowInsetsPadding(
                    WindowInsets.systemBars.union(WindowInsets.displayCutout)
                ),
            contentAlignment = BiasAlignment(0f, -0.6f)

        ) {
            val unlockButtonText = if (isFromSettings) {
                stringResource(R.string.verify)
            } else {
                stringResource(R.string.unlock)
            }
            if (passcodeType == SecurityUtil.AuthType.ACCOUNT_CREDENTIALS) {
                MdCardWithoutTonalElevation(
                    modifier = Modifier
                        .fillMaxWidth(if (isPortrait) 0.7f else 0.5f)
                        .padding(38.dp), // Removed manual top padding, alignment handles position
                    shape = RoundedCornerShape(MdTheme.dimension.cardCornerRadius)
                ) {
                    Column(
                        modifier = Modifier.padding(38.dp)
                    ) {
                        val passcodeTypeText = if (isFromSettings) {
                            stringResource(R.string.identity_verification)
                        } else {
                            stringResource(R.string.screen_lock_is_on)
                        }
                        Text(
                            text = passcodeTypeText,
                            style = MdTheme.typography.bold24TextStyle
                        )

                        MdVerticalSpace(1f)

                        Text(
                            text = stringResource(R.string.enter_password_to_unlock),
                            style = MdTheme.typography.regular18TextStyle,
                            color = MdColors.Black60
                        )

                        MdVerticalSpace(4f)
                        LoginInfo(Modifier.fillMaxWidth(), state.loginName, state.facilityName)
                        MdVerticalSpace(3f)

                        PasswordInputField(
                            password = state.password,
                            onPasswordChange = { onAction(PasscodeAction.OnChangePassword(it)) },
                            showPassword = state.showPassword,
                            onShowPassword = { onAction(PasscodeAction.OnShowPassword) },
                            onDone = {
                                if (!state.isLoading) {
                                    onAction(PasscodeAction.OnUnlock(state.loginName, state.password))
                                }
                            }
                        )

                        MdVerticalSpace(1f)

                        (state.errorMessage?.asString() ?: "").takeIf { it.isNotBlank() }?.let {
                            MdErrorText(it)
                        }

                        MdVerticalSpace(4f)

                        if (state.isLoading) {
                            LoadingIndicator()
                        } else {
                            UnlockButton(modifier = Modifier.fillMaxWidth(), onUnlockClicked = {
                                onAction(
                                    PasscodeAction.OnUnlock(
                                        state.loginName,
                                        state.password
                                    )
                                )
                            }, unlockButtonText)
                        }
                    }
                }
                if (!isFromSettings && state.wrongPasswordAttempts >= 3) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Column(
                            modifier = Modifier
                                .clickable {
                                    onAction(PasscodeAction.OnLogOut)
                                }
                                .padding(24.dp)
                                .background(Color.White, shape = RoundedCornerShape(16.dp))
                                .padding(vertical = 16.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LogoutIcon(onClickLogout = { onAction(PasscodeAction.OnLogOut) })
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.sign_out),
                                style = MdTheme.typography.semiBold16TextStyle,
                                color = MdColors.Black90
                            )
                        }
                    }
                }
            } else if (passcodeType == SecurityUtil.AuthType.SYSTEM_LOCK) {
                activity?.let {
                    showBiometricPrompt(activity, isToCreatePasscode, {
                        onAction(PasscodeAction.OnBiometricAuthSuccess)
                    }, isFromSettings)
                }
                Column(
                    modifier = Modifier.fillMaxSize(0.5f),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_lock_white_bg),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .heightIn(80.dp)
                            .widthIn(120.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(16.dp))

                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = if (isToCreatePasscode || isFromSettings) {
                            stringResource(R.string.identity_verification)
                        } else {
                            stringResource(R.string.charm_kiosk_locked)
                        },
                        style = MdTheme.typography.bold24TextStyle
                    )
                    if (!isToCreatePasscode && !isFromSettings) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            text = stringResource(R.string.unlock_charm_kiosk),
                            style = MdTheme.typography.regular18TextStyle,
                            color = MdColors.Black60
                        )
                    }
                    MdVerticalSpace(4f)
                    MdCardWithoutTonalElevation(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Row(
                            Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LoginInfo(
                                modifier = Modifier.weight(1f),
                                state.loginName,
                                state.facilityName
                            )
                            if (!isFromSettings && state.wrongPasswordAttempts >= 3) {
                                Spacer(modifier = Modifier.height(16.dp))

                                LogoutIcon(onClickLogout = {
                                    onAction(PasscodeAction.OnLogOut)
                                })
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        UnlockButton(
                            modifier = Modifier.fillMaxWidth(),
                            onUnlockClicked = {
                                onAction(PasscodeAction.OnBiometricAuthFailed)
                                activity?.let {
                                    showBiometricPrompt(
                                        activity,
                                        isToCreatePasscode,
                                        isFromSettings = isFromSettings,
                                        onSuccess = {
                                            onAction(PasscodeAction.OnBiometricAuthSuccess)
                                        }
                                    )
                                }
                            },
                            unlockButtonText = unlockButtonText
                        )
                    }
                }
            }
        }
    }
}

fun showBiometricPrompt(
    activity: FragmentActivity,
    isToCreatePasscode: Boolean,
    onSuccess: () -> Unit,
    isFromSettings: Boolean
) {
    val biometricPrompt = instanceOfBiometricPrompt(
        activity = activity,
        afterPasscodeSuccess = onSuccess,
        onUserCancel = {

        }

    )
    if (isToCreatePasscode || isFromSettings) {
        biometricPrompt.let {
            activity.presentLockScreen(
                biometricPrompt,
                title = activity.getString(R.string.identity_verification),
            )
        }
    } else {
        biometricPrompt.let {
            activity.presentLockScreen(
                biometricPrompt,
                description = activity.getString(R.string.confirm_screen_lock)
            )
        }
    }
}

@Composable
fun LoginInfo(modifier: Modifier, userDisplayName: String, facilityName: String) {
    Row(
        modifier = modifier, verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.kiosk_icon_dummy),
            contentDescription = null,
            modifier = Modifier.size(56.dp)
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp),
        ) {
            Text(text = userDisplayName, style = MdTheme.typography.semiBold16TextStyle)
            MdVerticalSpace(.5f)
            Text(text = facilityName, style = MdTheme.typography.regular14TextStyle)
        }
    }
}

@Composable
fun PasswordInputField(
    password: String,
    onPasswordChange: (String) -> Unit,
    showPassword: Boolean,
    onShowPassword: () -> Unit,
    onDone: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    val icon = if (showPassword) R.drawable.ic_eye_open else R.drawable.ic_eye_close
    MdOutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        value = password,
        // Single line, so Enter (keyboard Done or a hardware keyboard) unlocks instead of adding a newline.
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        shape = RoundedCornerShape(8.dp),
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            Icon(
                modifier = Modifier.clickable { onShowPassword() },
                painter = painterResource(icon),
                contentDescription = null
            )
        },
        textStyle = MdTheme.typography.regular18TextStyle
    )
}

@Composable
fun LoadingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(48.dp)
                .padding(4.dp)
        )
    }
}

@Composable
fun UnlockButton(modifier: Modifier, onUnlockClicked: () -> Unit, unlockButtonText: String) {

    MdCommonPrimaryButton(
        modifier = modifier
            .heightIn(48.dp),
        text = unlockButtonText,
        startIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_unlock),
                contentDescription = null,
                tint = MdColors.White
            )
        },
        onClick = { onUnlockClicked() },
    )
}

@Composable
fun LogoutIcon(onClickLogout: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable { onClickLogout() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            painter = painterResource(R.drawable.ic_logout),
            contentDescription = null,
            tint = MdColors.Black90
        )
    }
}

private fun instanceOfBiometricPrompt(
    activity: FragmentActivity,
    afterPasscodeSuccess: () -> Unit,
    onUserCancel: () -> Unit
): BiometricPrompt {
    val executor: Executor = ContextCompat.getMainExecutor(activity)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            @SuppressLint("RestrictedApi")
            if (BiometricPrompt.ERROR_USER_CANCELED == errorCode) {
                onUserCancel()
            }
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            //This method is called when biometric is valid but not recognized
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            afterPasscodeSuccess()
        }
    }
    return BiometricPrompt(activity, executor, callback)
}


@Composable
@Preview(showBackground = true, widthDp = 800, heightDp = 1200)
fun PasscodeScreenPreview() {
    MdTheme {
        PasscodeScreen(
            state = PasscodeState(
                loginName = "JohnDoe",
                facilityName = "Main Facility",
                password = "",
                errorMessage = UiText.DynamicString("Please enter your password"),
                isLoading = false,
                showPassword = true
            ),
            onAction = {

            },
            isToCreatePasscode = false,
            passcodeType = SecurityUtil.AuthType.ACCOUNT_CREDENTIALS,
            isFromSettings = false
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 1000, heightDp = 600)
fun PasscodeScreenPreviewHorizontal() {
    MdTheme {
        PasscodeScreen(
            state = PasscodeState(
                loginName = "JohnDoe",
                facilityName = "Main Facility",
                password = "",
                errorMessage = UiText.DynamicString("Please enter your password"),
                isLoading = false,
                showPassword = true
            ),
            onAction = {

            },
            isToCreatePasscode = false,
            passcodeType = SecurityUtil.AuthType.ACCOUNT_CREDENTIALS,
            isFromSettings = false
        )
    }
}
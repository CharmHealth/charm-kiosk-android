package com.charm.ehr.kiosk.presentation.self_check_in

import UiText
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.util.ModelUtil.Companion.formatSecondsToMmSs
import com.charm.ehr.kiosk.presentation.composable.CharmKioskLogoHeader
import com.charm.ehr.kiosk.presentation.composable.FacilityLogoRow
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdCommonPrimaryButton
import com.charm.ehr.kiosk.presentation.composable.MdErrorText
import com.charm.ehr.kiosk.presentation.composable.MdHorizontalSpace
import com.charm.ehr.kiosk.presentation.composable.MdOutlinedTextField
import com.charm.ehr.kiosk.presentation.composable.MdSelectionDialogWithSearch
import com.charm.ehr.kiosk.presentation.composable.MdVerticalSpace
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import kotlinx.coroutines.delay

@Composable
fun SelfCheckInScreen(
    state: SelfCheckInState,
    onAction: (SelfCheckInAction) -> Unit, imageLoader: ImageLoader
) {

    val scrollState = rememberScrollState()
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MdColors.MdSurfaceColor)
            .windowInsetsPadding(
                WindowInsets.systemBars.union(WindowInsets.displayCutout)
            )
            .verticalScroll(scrollState)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {

        CharmKioskLogoHeader(modifier = Modifier.align(Alignment.TopCenter))
        MdCardWithoutTonalElevation(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(if (isPortrait) 0.7f else 0.5f)
                .padding(38.dp),
            shape = RoundedCornerShape(32.dp)
        ) {

            if (state.isOtpSent) {
                OTPSection(
                    state,
                    { onAction(it) },
                    imageLoader
                )
            } else {
                MobileNumberSection(
                    state = state,
                    imageLoader = imageLoader,
                    onAction = {
                        onAction(it)
                    }
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 54.dp, bottom = 54.dp)
                .size(48.dp) // Increased touch area
                .clickable { onAction(SelfCheckInAction.OnSettingsClicked) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_setting),
                contentDescription = null,
                modifier = Modifier.size(34.dp) // Icon size remains the same
            )
        }
    }

}

@Composable
private fun OTPSection(
    state: SelfCheckInState,
    onAction: (SelfCheckInAction) -> Unit,
    imageLoader: ImageLoader
) {
    Column(
        modifier = Modifier.padding(38.dp)
    ) {

        FacilityLogoRow(
            imageUrl = state.facilityLogoUrl,
            imageLoader = imageLoader,
            imgSize = 64.dp,
            facilityName = state.facilityName,
        )

        MdVerticalSpace(3f)

        Text(
            text = stringResource(R.string.enter_your_code),
            style = MdTheme.typography.bold24TextStyle,
        )

        MdVerticalSpace(2f)

        Column {
            Text(
                text = stringResource(R.string.otp_sent_to_mobile),
                style = MdTheme.typography.medium18TextStyle,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = state.mobileNumber,
                    style = MdTheme.typography.medium18TextStyle,
                )
                MdHorizontalSpace(1f)
                TextButton(onClick = { onAction(SelfCheckInAction.ResetOtpSentState) }) {
                    Text(
                        text = stringResource(R.string.change_str),
                        style = MdTheme.typography.regular18TextStyle.copy(color = MdColors.BrandPink)
                    )
                }
            }
        }

        MdVerticalSpace(2f)
        OtpInputField(
            otpLength = 7,
            otp = state.otp,
            onOtpChange = { onAction(SelfCheckInAction.OnUpdateOTPNumber(it)) },
            otpTextStyle = MdTheme.typography.medium20TextStyle,
            )
        MdVerticalSpace(1f)

        state.validationErrorMessage?.asString()?.takeIf {
            it.isNotBlank()
        }?.let {
            MdErrorText(text = it)
        }

        Row(modifier = Modifier.align(Alignment.End)) {
            if (state.canResendOtp) {
                TextButton(onClick = {
                    onAction(SelfCheckInAction.OnResendOTP)
                }) {
                    Text(
                        stringResource(R.string.resend_otp),
                        style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.BrandPink)
                    )
                }
            } else {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 8.dp),
                    text = stringResource(
                        R.string.resend_otp_in_time,
                        formatSecondsToMmSs(state.resendOtpTimeLeft)
                    ),
                    style = MdTheme.typography.regular14TextStyle
                )
            }
        }

        MdVerticalSpace(3f)

        MdCommonPrimaryButton(
            text = stringResource(R.string.verify_otp),
            onClick = {
                onAction(SelfCheckInAction.OnVerifyOTP)
            },
            modifier = Modifier.fillMaxWidth(),
        )

    }
}

@Composable
private fun MobileNumberSection(
    state: SelfCheckInState,
    imageLoader: ImageLoader,
    onAction: (SelfCheckInAction) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }

    val showCountryCodeDialog = remember { mutableStateOf(false) }
    val mobileNumberTextStyle = MdTheme.typography.medium18TextStyle.copy(letterSpacing = 1.5.sp)


    Column(
        modifier = Modifier.padding(38.dp)
    ) {

        FacilityLogoRow(
            imageUrl = state.facilityLogoUrl,
            imageLoader = imageLoader,
            imgSize = 64.dp,
            facilityName = state.facilityName,
        )

        MdVerticalSpace(3f)

        Text(
            text = stringResource(R.string.self_check_in),
            style = MdTheme.typography.bold24TextStyle,
        )

        MdVerticalSpace(2f)

        Text(
            text = stringResource(R.string.enter_mobile_number_instruction),
            style = MdTheme.typography.medium18TextStyle,
        )

        MdVerticalSpace(2f)

        MdOutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            value = state.mobileNumber,
            onValueChange = { input ->
                if (input.all { it.isDigit() }) {
                    onAction(SelfCheckInAction.OnUpdateMobileNumber(input))
                }
            },
            textStyle = mobileNumberTextStyle,
            leadingIcon = {
                Row(
                    modifier = Modifier
                        .clickable { showCountryCodeDialog.value = true }
                        .padding(start = 8.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        state.mobileCountryCode,
                        style = mobileNumberTextStyle
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_down),
                        contentDescription = "Select country code",
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            trailingIcon = {
                if (state.mobileNumber.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onAction(SelfCheckInAction.OnClearMobileNumber) },
                        contentAlignment = Alignment.Center
                    ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = null,
                            modifier = Modifier.size(24.dp)
                    )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        if (showCountryCodeDialog.value) {
            MdSelectionDialogWithSearch(
                data = state.countryList,
                dialogTitle = stringResource(R.string.select_country_code),
                onResult = { selectedList ->
                    val selectedCountry = selectedList.firstOrNull()
                    if (selectedCountry != null) {
                        onAction(
                            SelfCheckInAction.OnCountryCodeSelected(
                                selectedCountry.value ?: ""
                            )
                        )
                    }
                    showCountryCodeDialog.value = false
                },
                preSelectedItems = state.countryList.filter { it.value == state.mobileCountryCode },
                itemToString = { it.label ?: it.value ?: "" },
                onDismissRequest = { showCountryCodeDialog.value = false }
            )
        }

        MdVerticalSpace(1f)

        state.validationErrorMessage?.asString()?.takeIf {
            it.isNotBlank()
        }?.let {
            MdErrorText(text = it)
        }
        MdVerticalSpace(4f)

        MdCommonPrimaryButton(
            text = stringResource(R.string.send_otp),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onAction(SelfCheckInAction.OnSendOTP)
            },
        )

    }
}

@Preview(showBackground = true, widthDp = 700, heightDp = 1000)
@Composable
fun SelfCheckInScreenPreview() {
    val mockState = SelfCheckInState(
        mobileNumber = "987654321",
        mobileCountryCode = "+91",
        otp = "123123",
        isOtpSent = false,
        validationErrorMessage = UiText.DynamicString("Please enter a valid OTP"),
        facilityName = "Test Facility",
    )
    MdTheme {
        SelfCheckInScreen(
            state = mockState,
            onAction = {},
            imageLoader = ImageLoader.Builder(LocalContext.current).build()
        )
    }
}

@Preview(showBackground = true, widthDp = 1700, heightDp = 1000)
@Composable
private fun OTPSectionPreview() {
    val mockState = SelfCheckInState(
        mobileNumber = "987654321",
        mobileCountryCode = "+91",
        otp = "",
        isOtpSent = true,
        validationErrorMessage = UiText.DynamicString("Please enter a valid OTP"),
        canResendOtp = true,
        facilityName = "Test Facility",
    )
    MdTheme {
        SelfCheckInScreen(
            state = mockState,
            onAction = {},
            imageLoader = ImageLoader.Builder(LocalContext.current).build()
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun FacilityLogoRowPreview() {
//    val context = LocalContext.current
//    FacilityLogoRow(
//        imageUrl = "",
//        imageLoader = ImageLoader.Builder(context).build(),
//        imgSize = 64.dp,
//        facilityName = "Test Facility",
//        initials = "TF",
//        textStyle = MdTheme.typography.bold24TextStyle
//    )
//}
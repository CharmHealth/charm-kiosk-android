package com.charm.ehr.kiosk.presentation.self_check_in

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusState

@Composable
fun OtpInputField(
    otpLength: Int = 7,
    otp: String,
    onOtpChange: (String) -> Unit,
    otpTextStyle: TextStyle
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Box(contentAlignment = Alignment.Center) {
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.Transparent)
                .focusRequester(focusRequester)
                .onFocusChanged { state: FocusState ->
                    if (state.isFocused) {
                        keyboardController?.show()
                    }
                },
            value = otp,
            onValueChange = {
                val filtered = it.filter { char -> char.isDigit() }
                if (filtered.length <= otpLength) {
                    onOtpChange(filtered)
                }
            },
            textStyle = TextStyle(
                color = Color.Transparent,
                textAlign = TextAlign.Center,
                fontSize = 10.sp
            ),
            maxLines = 1,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            ),
            cursorBrush = SolidColor(Color.Transparent)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 0 until otpLength) {
                val char = otp.getOrNull(i)?.toString() ?: ""
                OTPItem(
                    text = char,
                    otpTextStyle = otpTextStyle
                )
            }
        }
    }
}

@Composable
fun OTPItem(text: String, otpTextStyle: TextStyle) {
    val bgColor = if (text.isEmpty()) {
        MdColors.Black20
    } else {
        MdColors.BrandPink
    }
    Box(
        modifier = Modifier
            .height(46.dp)
            .width(44.dp)
            .background(
                MdColors.MdSurfaceColor,
                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomEnd = 0.dp, bottomStart = 0.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .height(1.5.dp)
                .fillMaxWidth()
                .background(color = bgColor, shape = RoundedCornerShape(2.dp))
                .align(Alignment.BottomCenter)
        )
        Text(
            text = text,
            style = otpTextStyle,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OTPItemPreview() {
    MdTheme {
        Row(modifier = Modifier.background(MdColors.Black)) {
            OTPItem("7", otpTextStyle = MdTheme.typography.medium18TextStyle)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OtpInputFieldPreview() {
    var otp by remember { mutableStateOf("") }
    MdTheme {
        Box(modifier = Modifier.padding(vertical = 20.dp)) {
            OtpInputField(otp = otp, onOtpChange = { otp = it }, otpTextStyle = MdTheme.typography.medium18TextStyle)
        }
    }
}

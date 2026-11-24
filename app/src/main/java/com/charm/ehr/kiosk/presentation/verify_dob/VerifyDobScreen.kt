package com.charm.ehr.kiosk.presentation.verify_dob

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.util.MDAccountsRelatedUtil
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdCommonPrimaryButton
import com.charm.ehr.kiosk.presentation.composable.MdDateInputTextField
import com.charm.ehr.kiosk.presentation.composable.MdTopToolBar
import com.charm.ehr.kiosk.presentation.composable.MdVerticalSpace
import com.charm.ehr.kiosk.presentation.composable.PatientImageItem
import com.charm.ehr.kiosk.presentation.patient_profile.dummyTestPatient
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyDobScreen(
    state: VerifyDobState,
    onAction: (VerifyDobAction) -> Unit,
    imageLoader: ImageLoader,
    displayDateFormate: String,
    onBackClick:()->Unit
) {
    val photoPath = MDAccountsRelatedUtil.getPatientPhotoUrl(state.patient?.practicePatientId ?: "")
    val patientImageSize = 120.dp

    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.displayCutout
            )
    ) {
        Column {
            MdTopToolBar(
                headingText = stringResource(R.string.verify_dob),
                onBackClick = {
                    onBackClick()
                }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.systemBars.union(WindowInsets.displayCutout)
                    ),
                contentAlignment = Alignment.TopCenter
            ) {

                val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.fillMaxHeight(if (isLandscape) 0.05f else 0.20f))


                    val cardWidthFraction = if (isLandscape) 0.5f else 0.65f
                    MdCardWithoutTonalElevation(
                        modifier = Modifier.fillMaxWidth(cardWidthFraction)
                    ) {

                        Column(Modifier.padding(24.dp)) {

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                PatientImageItem(
                                    hasImage = state.patient?.isPhotoAvailableOnline ?: false,
                                    imageUrl = photoPath,
                                    imageLoader = imageLoader,
                                    imgSize = patientImageSize,
                                    selectedPatient = state.patient ?: Patient(),
                                    textStyle = MdTheme.typography.medium20TextStyle.copy(
                                        letterSpacing = 1.sp,
                                        fontSize = 22.sp
                                    )
                                )
                            }

                            MdVerticalSpace(2f)

                            Text(
                                text = stringResource(R.string.welcome_patient, state.patient?.fullName ?: ""),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.CenterHorizontally),
                                style = MdTheme.typography.semiBold22TextStyle,
                                textAlign = TextAlign.Center
                            )

                            MdVerticalSpace(3f)

                            Text(
                                text = stringResource(R.string.verify_your_date_of_birth),
                                style = MdTheme.typography.semiBold16TextStyle
                            )


                            MdVerticalSpace(1f)

                            MdDateInputTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                textStyle = MdTheme.typography.medium16TextStyle,
                                value = state.dob,
                                isError = state.verifyDobError != null,
                                onValueChange = {
                                    onAction(VerifyDobAction.OnDateChange(it))
                                },
                                labelText = stringResource(R.string.date_of_birth),
                                placeholder = {
                                    Text(
                                        text = displayDateFormate,
                                        style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Black40)
                                    )
                                },
                                isDobField = true
                            )
                            MdVerticalSpace(.5f)

                            if (state.verifyDobError != null) {
                                MdVerticalSpace(.5f)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    val errorText = when (state.verifyDobError) {
                                        VerifyDobError.INCORRECT_DOB -> {
                                            pluralStringResource(
                                                R.plurals.incorrect_dob_attempts_remaining,
                                                state.attemptsLeft,
                                                state.attemptsLeft
                                            )
                                        }

                                        VerifyDobError.DOB_NOT_COMPLETE -> {
                                            stringResource(R.string.please_enter_valid_dob_format, displayDateFormate)
                                        }
                                    }

                                    Text(
                                        text = errorText,
                                        style = MdTheme.typography.regular14TextStyle.copy(
                                            color = MdColors.Red,
                                            textAlign = TextAlign.Start
                                        )
                                    )

                                }
                            }


                            LaunchedEffect(Unit) {
                                delay(200)
                                focusRequester.requestFocus()
                            }

                            MdVerticalSpace(3f)

                            MdCommonPrimaryButton(
                                text = stringResource(R.string.verify),
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    onAction(
                                        VerifyDobAction.OnVerify
                                    )
                                },
                                endIcon = {
                                    ImageVector.vectorResource(id = R.drawable.ic_arrow_down)
                                },
                            )

                        }

                        LaunchedEffect(Unit) {
                            delay(300)
                            focusRequester.requestFocus()
                        }
                    }
                }
            }
        }
    }

}

//VerifyAgeScreen

@Composable
@Preview(showBackground = true, widthDp = 600, heightDp = 1000)
fun PreviewVerifyAgeScreen() {
    val dummyState = VerifyDobState(patient = dummyTestPatient, dob = "", verifyDobError = null, attemptsLeft = 2)
    MdTheme{
        Box(modifier = Modifier.fillMaxSize().background(MdColors.MdSurfaceColor)) {
            VerifyDobScreen(
                state = dummyState,
                onAction = {},
                imageLoader = ImageLoader.Builder(LocalContext.current).build(),
                displayDateFormate = "MM/dd/yyyy",
                onBackClick = {}
            )
        }
    }
}


@Composable
@Preview(showBackground = true, widthDp = 1000, heightDp = 600)
fun PreviewVerifyAgeScreenLandScape() {
    val dummyState = VerifyDobState(patient = dummyTestPatient, dob = "", verifyDobError = null, attemptsLeft = 2)
    MdTheme{
        Box(modifier = Modifier.fillMaxSize().background(MdColors.MdSurfaceColor)) {
            VerifyDobScreen(
                state = dummyState,
                onAction = {},
                imageLoader = ImageLoader.Builder(LocalContext.current).build(),
                displayDateFormate = "MM/dd/yyyy",
                onBackClick = {}
            )
        }
    }
}
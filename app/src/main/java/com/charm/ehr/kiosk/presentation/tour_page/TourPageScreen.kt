package com.charm.ehr.kiosk.presentation.tour_page

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.SignInFailure
import com.charm.ehr.kiosk.presentation.composable.MdAlertDialog
import com.charm.ehr.kiosk.presentation.composable.MdCommonPrimaryButton
import com.charm.ehr.kiosk.presentation.composable.MdMessageDialog
import com.charm.ehr.kiosk.presentation.composable.MdVerticalSpace
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import com.tbuonomo.viewpagerdotsindicator.compose.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.compose.model.DotGraphic
import com.tbuonomo.viewpagerdotsindicator.compose.type.WormIndicatorType
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sign

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TourPageScreen(
    tourPageState: TourPageState,
    onLoginClicked: () -> Unit,
    resetNoPrevilegeDialog: () -> Unit,
    hideLoginFailureError: () -> Unit,
    getSettings: () -> Unit,
    resetErrorState: () -> Unit,
    navigateToKioskAccountCreation: () -> Unit,
    setKioskAccountDialogShown: () -> Unit
) {
    val slideCount = 5
    val pagerState = rememberPagerState { slideCount }
    var showKioskAccountDialog by remember { mutableStateOf(false) }
    val localConfiguration = LocalConfiguration.current
    val isPortrait = localConfiguration.orientation == Configuration.ORIENTATION_PORTRAIT
    val slideAnimationDuration = 800

    LaunchedEffect(pagerState.settledPage) {
        delay(4000) // 4sec
        val nextPage = (pagerState.currentPage + 1) % slideCount
        pagerState.animateScrollToPage(
            page = nextPage,
            animationSpec = tween(
                durationMillis = slideAnimationDuration,
                easing = FastOutSlowInEasing
            )
        )
    }


    if (showKioskAccountDialog) {
        MdAlertDialog(
            message = stringResource(R.string.kiosk_login_msg),
            heading = stringResource(R.string.sign_in_with_kiosk_account),
            primaryButtonResId = R.string.sign_in,
            secondaryButtonResId = R.string.learn_more,
            onPrimaryAction = {
                showKioskAccountDialog = false
                setKioskAccountDialogShown()
                onLoginClicked()
            },
            onSecondaryAction = {
                navigateToKioskAccountCreation()
            },
        )
    }

    //TODO: Commented, bottom sheet appeared screen keeps loading
//    if (tourPageState.isLoading) {
//        MdProgressDialog()
//    }

    if (tourPageState.showNoPrivilegeError) {
        MdMessageDialog(
            message = stringResource(R.string.non_kiosk_account_user_msg),
            title = stringResource(R.string.LoginFailed),
            okButtonText = stringResource(R.string.alertDialogOkTextNoI18N)
        ) {
            resetNoPrevilegeDialog()
        }
    }

    tourPageState.loginFailure?.let { failure ->
        val (title, message) = when (failure) {
            SignInFailure.ACCOUNT_REFUSED -> R.string.login_refused_title to R.string.login_refused_message
            SignInFailure.UNREACHABLE -> R.string.login_unreachable_title to R.string.login_unreachable_message
            SignInFailure.EXPIRED -> R.string.login_expired_title to R.string.login_expired_message
            SignInFailure.OTHER -> R.string.login_failed_title to R.string.login_failed_message
        }
        MdAlertDialog(
            heading = stringResource(title),
            message = stringResource(message),
            // A refused account fails again until an admin acts: offer a fresh sign-in so
            // someone else can use the kiosk.
            primaryButtonResId = if (failure == SignInFailure.ACCOUNT_REFUSED) R.string.login_refused_other_user else R.string.retry,
            onPrimaryAction = {
                hideLoginFailureError()
                onLoginClicked()
            },
            onSecondaryAction = { hideLoginFailureError() },
        )
    }

    if (tourPageState.errorMessage != null) {
        MdAlertDialog(
            message = tourPageState.errorMessage.asString(),
            primaryButtonResId = R.string.retry,
            secondaryButtonResId = R.string.cancel,
            onPrimaryAction = {
                getSettings()
            },
            onSecondaryAction = {
                resetErrorState()
            })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(
                top = WindowInsets.displayCutout
                    .asPaddingValues()
                    .calculateTopPadding(),
                bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
                start = 0.dp,
                end = 0.dp
            )
    ) {
        // Pager in the background, fills the whole screen for swipe gestures
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (isPortrait) 340.dp else 260.dp), // Padding to avoid overlap
                contentAlignment = Alignment.Center
            ) {
                GetImageForIndex(page)
            }
        }

        // UI content in the foreground, aligned to the bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val currentPage = pagerState.currentPage
            val pageOffset = pagerState.currentPageOffsetFraction

            val (currentHeading, currentDescription) = getHeadingTextForIndex(currentPage)
            val (nextHeading, nextDescription) = getHeadingTextForIndex(
                if (pageOffset > 0) (currentPage + 1) % slideCount else (currentPage - 1 + slideCount) % slideCount
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = currentHeading,
                    style = MdTheme.typography.semiBold22TextStyle.copy(color = MdColors.BrandPink),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            // Invert direction for opposite animation
                            translationX = -size.width * pageOffset
                            alpha = 1 - abs(pageOffset)
                        }
                )
                Text(
                    text = nextHeading,
                    style = MdTheme.typography.semiBold22TextStyle.copy(color = MdColors.BrandPink),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationX = -size.width * (pageOffset - sign(pageOffset))
                            alpha = abs(pageOffset)
                        }
                )
            }

            if (isPortrait) {
                MdVerticalSpace(1f)
            } else {
                MdVerticalSpace(1f)
            }

            val textSidePadding = if (isPortrait) {
                44.dp
            } else {
                180.dp
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = textSidePadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentDescription,
                    style = MdTheme.typography.medium18TextStyle.copy(letterSpacing = 0.2.sp),
                    minLines = 3,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            // Invert direction for opposite animation
                            translationX = -size.width * pageOffset
                            alpha = 1 - abs(pageOffset)
                        }
                )
                Text(
                    text = nextDescription,
                    style = MdTheme.typography.medium18TextStyle.copy(letterSpacing = 0.2.sp),
                    minLines = 3,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationX = -size.width * (pageOffset - sign(pageOffset))
                            alpha = abs(pageOffset)
                        }
                )
            }

            if (isPortrait) {
                MdVerticalSpace(5f)
            } else {
                MdVerticalSpace(2f)
            }

            // Dots indicator
            DotsIndicator(
                dotCount = slideCount,
                type = WormIndicatorType(
                    dotsGraphic = DotGraphic(
                        size = 6.dp,
                        color = MdColors.whiteBasedPink,
                    ),
                    wormDotGraphic = DotGraphic(
                        size = 6.dp,
                        color = MdColors.BrandPink,
                    )
                ),
                pagerState = pagerState
            )

            if (isPortrait) {
                MdVerticalSpace(8f)
            } else {
                MdVerticalSpace(4f)
            }

            MdCommonPrimaryButton(
                onClick = {
                    if (tourPageState.showEducationDialog) {
                        onLoginClicked()
                    } else {
                        showKioskAccountDialog = true
                    }
                },
                modifier = Modifier
                    .width(192.dp)
                    .height(46.dp),
                text = stringResource(R.string.sign_in)
            )

            if (isPortrait) {
                MdVerticalSpace(10f)
            } else {
                MdVerticalSpace(5.5f)
            }
        }

    }


}

@Composable
private fun GetImageForIndex(page: Int) {

    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val imageModifier = if (isPortrait) {
        Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
    } else {
        Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp)
    }

    val imageRes = when (page) {
        0 -> R.drawable.ic_charm_health_kiosk
        1 -> R.drawable.ic_patient_registration
        2 -> R.drawable.ic_front_desk_workflow
        3 -> R.drawable.ic_patient_insurance
        4 -> R.drawable.ic_questionnaire_consent_feedback_forms
        5 -> R.drawable.ic_video_consultation
        else -> R.drawable.ic_video_consultation
    }
    Image(
        painter = painterResource(imageRes),
        contentDescription = "",
        modifier = imageModifier
    )

}

@Composable
fun getHeadingTextForIndex(index: Int): Pair<String, String> {
    val headings = listOf(
        Pair(
            stringResource(R.string.charm_health_kiosk),
            stringResource(R.string.charm_health_kiosk_content)
        ),
        Pair(
            stringResource(R.string.patient_registration),
            stringResource(R.string.patient_registration_content)
        ),
        Pair(
            stringResource(R.string.front_desk_workflow),
            stringResource(R.string.front_desk_workflow_content)
        ),
        Pair(
            stringResource(R.string.patient_insurance),
            stringResource(R.string.patient_insurance_content)
        ),
        Pair(
            stringResource(R.string.questionnaire_consent_feedback_forms),
            stringResource(R.string.questionnaire_consent_feedback_forms_content)
        )
    )
    return headings.getOrNull(index) ?: Pair("", "")
}

@Composable
@Preview(showBackground = true, widthDp = 700, heightDp = 1500)
fun TourPageScreenPreview() {
    MdTheme {
        TourPageScreen(
            onLoginClicked = {},
            tourPageState = TourPageState(),
            resetNoPrevilegeDialog = {},
            hideLoginFailureError = {},
            getSettings = {},
            resetErrorState = {},
            navigateToKioskAccountCreation = {}, setKioskAccountDialogShown = {}
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 1000, heightDp = 700)
fun TourPageScreenHorizontalPreview() {
    MdTheme {
        TourPageScreen(
            onLoginClicked = {},
            tourPageState = TourPageState(),
            resetNoPrevilegeDialog = {},
            hideLoginFailureError = {},
            getSettings = {},
            resetErrorState = {
            },
            navigateToKioskAccountCreation = {},
            setKioskAccountDialogShown = {}
        )
    }
}

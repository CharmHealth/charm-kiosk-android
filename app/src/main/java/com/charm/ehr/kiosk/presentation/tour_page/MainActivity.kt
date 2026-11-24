package com.charm.ehr.kiosk.presentation.tour_page

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil.ImageLoader
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.common.PatientProfileFlow
import com.charm.ehr.kiosk.common.constants.PrefConstants
import com.charm.ehr.kiosk.common.util.ModelUtil
import com.charm.ehr.kiosk.common.util.SecurityUtil
import com.charm.ehr.kiosk.common.util.SecurityUtil.getAppLockType
import com.charm.ehr.kiosk.common.util.SecurityUtil.isLocalAuthConfigured
import com.charm.ehr.kiosk.domain.usecase.MdRouteUseCase
import com.charm.ehr.kiosk.presentation.MdInAppUpdateActivity
import com.charm.ehr.kiosk.presentation.composable.ConnectivityStatusBar
import com.charm.ehr.kiosk.presentation.composable.MdMessageDialog
import com.charm.ehr.kiosk.common.SignInResult
import com.charm.ehr.kiosk.presentation.dashboard.DashBoardScreenRoot
import com.charm.ehr.kiosk.presentation.dashboard.DashboardViewModel
import com.charm.ehr.kiosk.presentation.edit_profile.EditProfileScreenRoot
import com.charm.ehr.kiosk.presentation.edit_profile.EditProfileViewModel
import com.charm.ehr.kiosk.presentation.navigation.MdRoute
import com.charm.ehr.kiosk.presentation.navigation.safeNavigate
import com.charm.ehr.kiosk.presentation.passcode.AuthTypeSelectionScreen
import com.charm.ehr.kiosk.presentation.passcode.PasscodeScreenRoot
import com.charm.ehr.kiosk.presentation.passcode.PasscodeVerificationFlow
import com.charm.ehr.kiosk.presentation.passcode.PasscodeViewModel
import com.charm.ehr.kiosk.presentation.patient_profile.PatientDetailViewmodel
import com.charm.ehr.kiosk.presentation.patient_profile.PatientProfileScreenRoot
import com.charm.ehr.kiosk.presentation.questionnaire.QuestionnaireDetailViewModel
import com.charm.ehr.kiosk.presentation.questionnaire.QuestionnaireRootScreen
import com.charm.ehr.kiosk.presentation.questionnaire.TemplateListScreenRoot
import com.charm.ehr.kiosk.presentation.search_template.SearchTemplateScreenRoot
import com.charm.ehr.kiosk.presentation.select_facility.SelectFacilityScreenRoot
import com.charm.ehr.kiosk.presentation.select_facility.SelectFacilityViewModel
import com.charm.ehr.kiosk.presentation.select_gender.SelectGenderScreenRoot
import com.charm.ehr.kiosk.presentation.select_patient.SelectPatientScreenRoot
import com.charm.ehr.kiosk.presentation.select_patient.SelectPatientViewModel
import com.charm.ehr.kiosk.presentation.self_check_in.SelfCheckInDashboardScreen
import com.charm.ehr.kiosk.presentation.self_check_in.SelfCheckInScreenRoot
import com.charm.ehr.kiosk.presentation.self_check_in.SelfCheckInViewModel
import com.charm.ehr.kiosk.presentation.settings.SettingsScreenRoot
import com.charm.ehr.kiosk.presentation.support.SupportScreenRoot
import com.charm.ehr.kiosk.presentation.support.SupportViewModel
import com.charm.ehr.kiosk.presentation.thank_you.ThankYouScreen
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import com.charm.ehr.kiosk.presentation.verify_dob.VerifyAgeScreenRoot
import com.charm.ehr.kiosk.presentation.verify_dob.VerifyDobViewmodel
import com.charm.ehr.kiosk.presentation.web_view.WebViewScreen
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : MdInAppUpdateActivity() {

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var mdRouteUseCase: MdRouteUseCase


    private val TAG = "MainActivity"


    //TODO:imePadding(), imeNestedScroll()
    //.consumeWindowInsets(innerPadding)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MdTheme {
                Box(
                    Modifier.background(MdColors.MdSurfaceColor)
                ) {
                    KioskApp(pref, authManager, imageLoader, mdRouteUseCase)
                }
            }
        }
    }


    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            sessionManager.userInteracted()
        }
        return super.dispatchTouchEvent(event)
    }

}

@Composable
fun KioskApp(
    pref: SharedPreferences,
    authManager: AuthManager,
    imageLoader: ImageLoader,
    mdRouteUseCase: MdRouteUseCase,
) {
    val navController = rememberNavController()
    val userStatus by authManager.userStatusStateFlow.collectAsStateWithLifecycle()
    val sessionEndedAlert by authManager.sessionEndedAlert.collectAsStateWithLifecycle()

    // The stored sign-in (CharmAuth) decides between the login screen and the app, so nothing is
    // shown until it is loaded. Once per process; quick (no network) unless it completes an
    // interrupted sign-in.
    var sessionReady by remember { mutableStateOf(false) }
    val isLoggedIn = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isLoggedIn.value = authManager.checkLoggedInAtStartup()
        sessionReady = true
    }

    Box(Modifier.fillMaxSize()) {
        if (sessionReady) {
            initNavHost(navController, pref, authManager, imageLoader, mdRouteUseCase, isLoggedIn)
        }
        // Connectivity status bar at the root level
        ConnectivityStatusBar(
            userStatus = userStatus
        )
    }

    // The sign-in ended (server ended it, or the API refuses the user): already signed out and on
    // the login screen; said once however many calls failed together.
    if (sessionEndedAlert) {
        MdMessageDialog(
            title = stringResource(R.string.session_expired_title),
            message = stringResource(R.string.session_expired_message),
            okButtonText = stringResource(R.string.alertDialogOkTextNoI18N),
            onOk = { authManager.dismissSessionEndedAlert() }
        )
    }
}

@Composable
fun initNavHost(
    navController: NavHostController,
    pref: SharedPreferences,
    authManager: AuthManager,
    imageLoader: ImageLoader,
    mdRouteUseCase: MdRouteUseCase,
    isLoggedIn: MutableState<Boolean>,
) {

    val context = LocalContext.current
    val appLockType =
        rememberSaveable { mutableStateOf(context.getAppLockType()) }


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var backPressedTime by remember { mutableLongStateOf(0L) }


    val sessionManager = remember {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            SessionManagerEntryPoint::class.java
        )
        entryPoint.sessionManager()
    }

    BackHandler(enabled = navBackStackEntry != null && navController.previousBackStackEntry == null) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime < 4000L) { // 4sec
            val activity = context as? Activity
            if (activity is MainActivity) {
                activity.finish()
            }
        } else {
            backPressedTime = currentTime
            Toast.makeText(
                context,
                context.getString(R.string.press_back_again_to_exit),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val sessionState by sessionManager.state.collectAsStateWithLifecycle()
    val requireTourNavigation by authManager.requireTourNavigation.collectAsStateWithLifecycle()

    // Set up navigation callback from charmAPI on token expiry
    LaunchedEffect(requireTourNavigation) {
        if (requireTourNavigation) {
            //Resetting Logged In before navigating to TOUR page
            isLoggedIn.value = false
            navController.safeNavigate(MdRoute.Tour) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
            authManager.setRequireTourNavigation()
        }
    }

    var showSessionInactiveDialog by rememberSaveable { mutableStateOf(false) }
    var timerSeconds by rememberSaveable { mutableStateOf(60) }
    var timerRunning by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(sessionState.isLocked) {
        val currentScreen = navController.currentBackStackEntry?.destination?.route
        val isPasscodeScreen = currentScreen == ModelUtil.passcodeScreenRoute
        val isTourScreen = currentScreen == ModelUtil.tourScreenRoute
        val isSelfCheckInDashboardScreen = currentScreen == ModelUtil.selfCheckInDashboardRoute
        if (sessionState.isLocked && !isPasscodeScreen && !isTourScreen && !isSelfCheckInDashboardScreen) {
            if (authManager.workFlowType.value == PatientProfileFlow.AFTER_HANDOVER) {
                if (!showSessionInactiveDialog) {
                    showSessionInactiveDialog = true
                    timerSeconds = 60
                    timerRunning = true
                }
            } else {
                onIdleNavigation(authManager, navController, mdRouteUseCase)
            }
        }
    }

    LaunchedEffect(showSessionInactiveDialog, timerRunning) {
        if (showSessionInactiveDialog && timerRunning) {
            while (timerSeconds > 0) {
                delay(1000)
                timerSeconds--
            }
            // Timer expired, execute navigation logic
            if (showSessionInactiveDialog) {
                showSessionInactiveDialog = false
                authManager.setWorkFlowType(PatientProfileFlow.BEFORE_HANDOVER)
                onIdleNavigation(authManager, navController, mdRouteUseCase)
                timerRunning = false
            }
        }
    }

    if (showSessionInactiveDialog) {
        val messageStart = stringResource(R.string.session_inactive_msg)
        val messageEnd = " " + stringResource(R.string.seconds) + "."
        val annotatedString = buildAnnotatedString {
            append(messageStart)
            val start = length
            append(" ")
            append(timerSeconds.toString())
            val end = length
            addStyle(
                style = SpanStyle(color = MdColors.Black86),
                start = start,
                end = end
            )
            append(messageEnd)
        }
        MdMessageDialog(
            onOk = {
                showSessionInactiveDialog = false
                timerRunning = false
                sessionManager.unlock()
            },
            okButtonText = stringResource(R.string.unlock),
            title = stringResource(R.string.session_inactive),
            messageAnnotated = annotatedString
        )
    }


    NavHost(
        navController = navController,
        startDestination = MdRoute.Tour,
        enterTransition = { defaultEnterTransition() },
        exitTransition = { defaultExitTransition() },
        popEnterTransition = { defaultPopEnterTransition() },
        popExitTransition = { defaultPopExitTransition() }) {

        composable<MdRoute.Tour> { backStackEntry ->
            val tourPageViewModel =
                backStackEntry.sharedViewModel<TourPageViewModel>(navController = navController)
            LaunchedEffect(Unit) {
                tourPageViewModel.setIsUserLoggedIn(isLoggedIn.value)
            }
            // Signed out while this screen is already showing (session ended during "Setting up
            // kiosk"): navigating to Tour reuses this entry, so tell the view model directly.
            LaunchedEffect(isLoggedIn.value) {
                if (!isLoggedIn.value) tourPageViewModel.setIsUserLoggedIn(false)
            }
            // The sign-in's outcome (it runs in AuthManager's scope, so it survives this activity
            // being destroyed while the Custom Tab is open). Collected only while this screen is
            // started, so a failure dialog shows once the Custom Tab is gone.
            val signInResult by authManager.signInResult.collectAsStateWithLifecycle()
            LaunchedEffect(signInResult) {
                val result = signInResult ?: return@LaunchedEffect
                authManager.consumeSignInResult()
                tourPageViewModel.updateLoadingState(false)
                when (result) {
                    SignInResult.Success -> tourPageViewModel.getSettingsWithKioskCheck()
                    is SignInResult.Failed -> tourPageViewModel.showLoginFailureError(result.failure)
                    SignInResult.Ignored -> {}
                }
            }
            DisposableEffect(Unit) {
                // Disable passcode when TourPage enters screen
                sessionManager.updateIsPasscodeAllowed(false)
                onDispose {
                    // Re-enable passcode when leaving TourPage
                    sessionManager.updateIsPasscodeAllowed(true)
                }
            }
            TourPageScreenRoot(
                tourPageViewModel = tourPageViewModel,
                onLoginClicked = {
                    (context as? Activity)?.let { authManager.startSignIn(it) }
                },
                onUserVerification = {
                    try {
                        if (isLoggedIn.value) {
                            val routeTo = if (authManager.isStandAloneUser()) {
                                MdRoute.SelfCheckInDashboard
                            } else {
                                if (appLockType.value != null) {
                                    mdRouteUseCase.passCode()
                                } else {
                                    mdRouteUseCase.authTypeSelect()
                                }
                            }
                            navController.safeNavigate(routeTo) {
                                popUpTo(MdRoute.Tour) { inclusive = true }
                            }
                        } else {
                            pref.edit { putBoolean(PrefConstants.isUserLoggedIn, true) }
                            if (authManager.isStandAloneUser()) {
                                navController.safeNavigate(mdRouteUseCase.settingFromLogin()) {
                                    popUpTo(MdRoute.Tour) { inclusive = true }
                                }
                            } else {
                                if (context.isLocalAuthConfigured()) {
                                    navController.safeNavigate(mdRouteUseCase.authTypeSelect()) {
                                        popUpTo(MdRoute.Tour) { inclusive = true }
                                    }
                                } else {
                                    navController.safeNavigate(
                                        mdRouteUseCase.createPasscode(
                                            appLockType.value
                                        )
                                    ) {
                                        popUpTo(MdRoute.Tour) { inclusive = true }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("MainActivity", "Failed to update isUserLoggedIn pref ${e.message}")
                    }
                },
                navigateToWebView = { url, heading ->
                    navController.safeNavigate(
                        MdRoute.SimpleWebView(url = url, heading = heading)
                    )
                }
            )
        }
        composable<MdRoute.AuthTypeSelect> { backStackEntry ->
            val isFromSettings = backStackEntry.toRoute<MdRoute.AuthTypeSelect>().isFromSetting
            val selectedAuthType = when (pref.getString(ModelUtil.applockType, null)) {
                SecurityUtil.AuthType.SYSTEM_LOCK.name -> SecurityUtil.AuthType.SYSTEM_LOCK
                SecurityUtil.AuthType.ACCOUNT_CREDENTIALS.name -> SecurityUtil.AuthType.ACCOUNT_CREDENTIALS
                else -> null
            }
            AuthTypeSelectionScreen(
                selectedType = selectedAuthType,
                onAuthTypeSelected = {
                    if (isFromSettings) {
                        navController.safeNavigate(
                            mdRouteUseCase.settingsCreatePasscode(it)
                        ) {
                            popUpTo(mdRouteUseCase.authTypeSelectFromSetting()) { inclusive = true }
                        }
                    } else {
                        navController.safeNavigate(
                            mdRouteUseCase.createPasscode(it)
                        )
                    }

                }, onBackPressed = {
                    navController.popBackStack()
                }, isFromSettings = isFromSettings
            )
        }
        composable<MdRoute.SelectFacility> { backStackEntry ->
            val isFromSettings = backStackEntry.toRoute<MdRoute.SelectFacility>().fromSettings
            val selectFacilityViewModel =
                backStackEntry.sharedViewModel<SelectFacilityViewModel>(navController = navController)

            LaunchedEffect(isFromSettings) {
                selectFacilityViewModel.setIsFromSettings(isFromSettings)
            }

            SelectFacilityScreenRoot(
                authManager = authManager,
                onSelectFacility = {
                    val fromSettings = backStackEntry.toRoute<MdRoute.SelectFacility>().fromSettings
                    if (fromSettings) {
                        navController.popBackStack()
                    } else {
                        navController.safeNavigate(MdRoute.DashboardHost) {
                            popUpTo(mdRouteUseCase.selectFacility()) { inclusive = true }
                        }
                    }
                },
                viewModel = selectFacilityViewModel,
                isFromSettings = isFromSettings,
                onBackPressed = {
                    navController.popBackStack()
                })
        }
        composable<MdRoute.Passcode> { backStackEntry ->
            val passcodeViewModel =
                backStackEntry.sharedViewModel<PasscodeViewModel>(navController = navController)
            //when user left the app in idle state ask for passcode verification
            val isFromIdleState = backStackEntry.toRoute<MdRoute.Passcode>().fromIdleState
            //if user is setting up passcode from settings or app launch
            val isToCreatePasscode = backStackEntry.toRoute<MdRoute.Passcode>().createPasscode
            val isFromSettings = backStackEntry.toRoute<MdRoute.Passcode>().isFromSetting
            //Self check-in flow verification proceeding to settings
            val isFromVerification = backStackEntry.toRoute<MdRoute.Passcode>().isFromVerification
            val passcodeType = if (isFromVerification) {
                SecurityUtil.AuthType.ACCOUNT_CREDENTIALS
            } else if (!isToCreatePasscode) {
                when (pref.getString(ModelUtil.applockType, null)) {
                    SecurityUtil.AuthType.SYSTEM_LOCK.name -> {
                        if (context.isLocalAuthConfigured()) {
                            SecurityUtil.AuthType.SYSTEM_LOCK
                        } else {
                            try {
                                pref.edit {
                                    putString(
                                        ModelUtil.applockType,
                                        SecurityUtil.AuthType.ACCOUNT_CREDENTIALS.name
                                    )
                                }
                            } catch (e: Exception) {
                                Log.d("MainActivity", "Failed to save applock type: ${e.message}")
                            }
                            SecurityUtil.AuthType.ACCOUNT_CREDENTIALS
                        }
                    }

                    SecurityUtil.AuthType.ACCOUNT_CREDENTIALS.name -> SecurityUtil.AuthType.ACCOUNT_CREDENTIALS
                    else -> null
                }
            } else {
                backStackEntry.toRoute<MdRoute.Passcode>().selectAuthType
            }

            LaunchedEffect(isToCreatePasscode, passcodeType) {
                if (isToCreatePasscode && passcodeType == SecurityUtil.AuthType.ACCOUNT_CREDENTIALS) {
                    passcodeViewModel.updateIsUserVerified(true)
                }
            }

            LaunchedEffect(isFromSettings, isToCreatePasscode) {
                if (isFromSettings) {
                    if (isToCreatePasscode) {
                        // Setting up new lock type from settings
                        passcodeViewModel.setPasscodeVerification(PasscodeVerificationFlow.LOCK_TYPE_SELECTION)
                    } else {
                        // Verifying existing lock type from settings before changing
                        passcodeViewModel.setPasscodeVerification(PasscodeVerificationFlow.LOCK_TYPE_CHANGE)
                    }
                }
            }

            LaunchedEffect(isFromIdleState) {
                passcodeViewModel.updateIsFromIdleState(isFromIdleState)
            }

            PasscodeScreenRoot(
                viewModel = passcodeViewModel,
                onSuccessUnlock = {
                    if (isFromVerification) {
                        navController.safeNavigate(mdRouteUseCase.settings()) {
                            popUpTo(mdRouteUseCase.passcodeVerification()) {
                                inclusive = true
                            }
                        }
                    } else if (passcodeViewModel.passcodeVerificationFlow == PasscodeVerificationFlow.LOCK_TYPE_CHANGE) {
                        navController.safeNavigate(mdRouteUseCase.authTypeSelectFromSetting()) {
                            popUpTo(mdRouteUseCase.passcodeFromSettings()) { inclusive = true }
                        }
                    } else {
                        if (isToCreatePasscode) {
                            try {
                                pref.edit { putString(ModelUtil.applockType, passcodeType?.name) }
                            } catch (e: Exception) {
                                Log.d("MainActivity", "Failed to save applock type: ${e.message}")
                            }
                        }
                        sessionManager.unlock()
                        if (isFromIdleState) {
                            if (authManager.workFlowType.value == PatientProfileFlow.AFTER_HANDOVER) {
                                authManager.setWorkFlowType(PatientProfileFlow.BEFORE_HANDOVER)
                                navController.safeNavigate(MdRoute.DashboardHost) {
                                    popUpTo(mdRouteUseCase.passcodeFromIdleState()) {
                                        inclusive = true
                                    }
                                }
                            } else {
                                navController.popBackStack() // Go back to where user left
                            }
                        } else {
                            if (passcodeViewModel.passcodeVerificationFlow == PasscodeVerificationFlow.LOCK_TYPE_SELECTION) {
                                navController.popBackStack()
                            } else {
                                //Check if the facility already selected then move to dashboard screen
                                if (authManager.selectedFacility == null) {
                                    if (isToCreatePasscode) {
                                        navController.safeNavigate(
                                            mdRouteUseCase.selectFacility()
                                        ) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    } else {
                                        navController.safeNavigate(
                                            mdRouteUseCase.selectFacility()
                                        ) {
                                            popUpTo(
                                                mdRouteUseCase.passCode()
                                            ) { inclusive = true }
                                        }
                                    }
                                } else {
                                    navController.safeNavigate(MdRoute.DashboardHost) {
                                        popUpTo(mdRouteUseCase.passCode()) { inclusive = true }
                                    }
                                }
                            }
                        }
                    }

                },
                onLogOutClicked = {
                    authManager.mdLogout()
                },
                isFromIdleState = isFromIdleState,
                finishApp = {
                    sessionManager.unlock()
                    passcodeViewModel.updateIsFromIdleState(false)
                    val activity = context as? Activity
                    if (activity is MainActivity) {
                        activity.finish()
                    }
                },
                isToCreatePasscode = isToCreatePasscode,
                passcodeType = passcodeType,
                onBackPressed = {
                    navController.popBackStack()
                }, isFromSettings = isFromSettings, isFromVerification = isFromVerification
            )
        }
        composable<MdRoute.Settings> {
            val dashboardViewModel =
                it.sharedViewModel<DashboardViewModel>(navController = navController)
            val isFromLogin = it.toRoute<MdRoute.Settings>().isFromLogin
            SettingsScreenRoot(
                dashboardViewModel = dashboardViewModel,
                navigateToSwitchFacility = {
                    navController.safeNavigate(mdRouteUseCase.selectFacilityFromSettings())
                },
                logOutAndNavigateToTourPage = {
                    authManager.mdLogout()
                },
                navigateToSupport = {
                    navController.safeNavigate(MdRoute.Support)
                },
                navigateToGenderSelection = {
                    navController.safeNavigate(MdRoute.SelectGender)
                },
                popBackStack = {
                    if (isFromLogin) {
                        navController.safeNavigate(MdRoute.SelfChekIn) {
                            popUpTo(mdRouteUseCase.settingFromLogin()) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                }, navigateToSelectAppLock = {
                    navController.safeNavigate(mdRouteUseCase.passcodeFromSettings())
                },
                navigateToWebView = { url, heading ->
                    navController.safeNavigate(
                        MdRoute.SimpleWebView(url = url, heading = heading)
                    )
                },
                isStandAloneUser = authManager.isStandAloneUser()
            )
        }

        dashboardGraph(navController, imageLoader, authManager, mdRouteUseCase, sessionManager)

        patientProfileGraph(
            navController = navController,
            imageLoader = imageLoader,
            authManager = authManager,
            mdRouteUseCase,
            sessionManager = sessionManager
        )

        selfCheckInGraph(
            navController = navController,
            imageLoader = imageLoader,
            authManager = authManager,
            sessionManager = sessionManager,
            mdRouteUseCase
        )

        composable<MdRoute.Support> {

            val supportViewModel = it.sharedViewModel<SupportViewModel>(
                navController = navController
            )

            SupportScreenRoot(
                supportViewModel = supportViewModel, imageLoader = imageLoader, onBackPressed = {
                    navController.popBackStack()
                })

        }

        composable<MdRoute.SimpleWebView> {
            val url = it.toRoute<MdRoute.SimpleWebView>().url
            val heading = it.toRoute<MdRoute.SimpleWebView>().heading
            WebViewScreen(
                url = url,
                heading = heading,
                onRequestClose = {
                    navController.popBackStack()
                })
        }

    }
}

fun NavGraphBuilder.selfCheckInGraph(
    navController: NavController,
    imageLoader: ImageLoader,
    authManager: AuthManager,
    sessionManager: SessionManager,
    mdRouteUseCase: MdRouteUseCase
) {
    navigation<MdRoute.SelfChekInModuleHost>(
        startDestination = MdRoute.SelfCheckInDashboard,
    ) {
        composable<MdRoute.SelfCheckInDashboard> {
            SelfCheckInDashboardScreen(
                selectedFacility = authManager.selectedFacility,
                onStart = {
                    sessionManager.unlock()
                    navController.safeNavigate(MdRoute.SelfChekIn) {
                        popUpTo(MdRoute.SelfCheckInDashboard) { inclusive = true }
                    }
                })
        }
        composable<MdRoute.SelfChekIn> {
            val selfCheckInViewModel = it.sharedViewModel<SelfCheckInViewModel>(
                navController = navController
            )
            SelfCheckInScreenRoot(
                selfCheckInViewModel = selfCheckInViewModel,
                imageLoader = imageLoader,
                navigateToSelectPatient = {
                    navController.safeNavigate(MdRoute.SelectPatient) {
                        popUpTo(MdRoute.SelfChekIn) { inclusive = true }
                    }
                },
                navigateToPatientDashBoard = {
                    authManager.setWorkFlowType(PatientProfileFlow.AFTER_HANDOVER)
                    navController.safeNavigate(
                        MdRoute.PatientProfileModuleHost(
                            patientId = it, isSelfCheckInFlow = true
                        )
                    ) {
                        popUpTo(MdRoute.SelfChekIn) { inclusive = true }
                    }
                }, navigateToSettingsScreen = {
                    navController.safeNavigate(mdRouteUseCase.passcodeVerification())
                })
        }
        composable<MdRoute.SelectPatient> {
            val selfCheckInViewModel = it.sharedViewModel<SelfCheckInViewModel>(
                navController = navController
            )
            val selectPatientViewModel = it.sharedViewModel<SelectPatientViewModel>(
                navController = navController
            )
            LaunchedEffect(Unit) {
                selfCheckInViewModel.selfCheckInState.collect { state ->
                    selectPatientViewModel.setPatientList(state.patientList)
                }
            }

            SelectPatientScreenRoot(
                selectPatientViewModel, imageLoader, navigateToDashboard = { patientId ->
                    authManager.setWorkFlowType(PatientProfileFlow.AFTER_HANDOVER)
                    navController.safeNavigate(
                        MdRoute.PatientProfileModuleHost(
                            patientId = patientId, isSelfCheckInFlow = true
                        )
                    ) {
                        popUpTo(MdRoute.SelectPatient) { inclusive = true }
                    }
                }, navigateToEntry = {
                    navController.safeNavigate(MdRoute.SelfCheckInDashboard) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                })
        }

    }

}

fun NavGraphBuilder.patientProfileGraph(
    navController: NavController,
    imageLoader: ImageLoader,
    authManager: AuthManager,
    mdRouteUseCase: MdRouteUseCase,
    sessionManager: SessionManager
) {
    navigation<MdRoute.PatientProfileModuleHost>(
        startDestination = MdRoute.PatientProfile::class, //android internal working automatically param passed to PatientProfileModuleHost get copied to PatientProfile.This copy only happen for start destination
    ) {
        composable<MdRoute.PatientProfile> {
            val patientDetailViewmodel: PatientDetailViewmodel =
                it.sharedViewModel<PatientDetailViewmodel>(navController)
            /*
             One way to get patientId from PatientProfileModuleHost to PatientProfile
             val parentEntry = remember(it) {
                 navController.getBackStackEntry<MdRoute.PatientProfileModuleHost>()
             }
             parentEntry.toRoute<MdRoute.PatientProfileModuleHost>().patientId
            */

            val isSelfCheckInFlow = it.toRoute<MdRoute.PatientProfile>().isSelfCheckInFlow
            val patientId = it.toRoute<MdRoute.PatientProfile>().patientId


            LaunchedEffect(isSelfCheckInFlow) {
                if (isSelfCheckInFlow) {
                    patientDetailViewmodel.setWorkFlowType(PatientProfileFlow.AFTER_HANDOVER)
                }
            }
            PatientProfileScreenRoot(
                patientDetailViewmodel = patientDetailViewmodel,
                imageLoader = imageLoader,
                popBackStack = {
                    authManager.setWorkFlowType(PatientProfileFlow.BEFORE_HANDOVER)
                    navController.popBackStack()
                },
                navigateToSearchTemplate = {
                    navController.safeNavigate(MdRoute.SearchTemplate)
                },
                navigateToVerifyDob = {
                    navController.safeNavigate(MdRoute.VerifyDob)
                },
                logOutPatientFlow = {
                    authManager.setWorkFlowType(PatientProfileFlow.BEFORE_HANDOVER)
                    //TODO:
                },
                navigateToTemplateList = {
                    navController.safeNavigate(MdRoute.TemplateList)
                },
                navigateToEditProfile = { selectedSection ->
                    navController.safeNavigate(MdRoute.EditProfile(selectedSection = selectedSection))
                },
                navigateToThankYouScreen = {
                    authManager.setWorkFlowType(PatientProfileFlow.BEFORE_HANDOVER)
                    navController.safeNavigate(MdRoute.ThankYouScreen) {
                        popUpTo(
                            MdRoute.PatientProfile(patientId, isSelfCheckInFlow)
                        ) { inclusive = true }
                    }
                }, authManager = authManager
            )
        }

        composable<MdRoute.EditProfile> { backStackEntry ->
            val editProfileViewModel = hiltViewModel<EditProfileViewModel>()
            backStackEntry.toRoute<MdRoute.EditProfile>().selectedSection?.let {
                editProfileViewModel.setSelectedSection(it)
            }
            EditProfileScreenRoot(editProfileViewModel, popBackStack = {
                navController.popBackStack()
            }, imageLoader = imageLoader, sessionManager = sessionManager)
        }

        composable<MdRoute.SearchTemplate> {
            val patientDetailViewmodel: PatientDetailViewmodel =
                it.sharedViewModel<PatientDetailViewmodel>(navController)
            SearchTemplateScreenRoot(
                patientDetailViewmodel, navController, onPopBackstack = {
                    navController.popBackStack()
                })
        }

        composable<MdRoute.VerifyDob> { backStackEntry ->
            val patientDetailViewmodel =
                backStackEntry.sharedViewModel<PatientDetailViewmodel>(navController = navController)
            val verifyDobViewmodel =
                backStackEntry.sharedViewModel<VerifyDobViewmodel>(navController = navController)

            LaunchedEffect(Unit) {
                patientDetailViewmodel.selectedPatient.collect { patient ->
                    verifyDobViewmodel.updateSelectedPatient(patient)
                }
            }

            VerifyAgeScreenRoot(popBackStack = {
                navController.popBackStack()
            }, viewModel = verifyDobViewmodel, imageLoader = imageLoader, onSuccessVerifyDob = {
                patientDetailViewmodel.setWorkFlowType(PatientProfileFlow.AFTER_HANDOVER)
                navController.popBackStack()
            }, onFailureVerifyDob = {
                navController.safeNavigate(mdRouteUseCase.passCode()) {
                    popUpTo(MdRoute.DashboardHost) {
                        inclusive = false
                    }
                }
            }, navigateToPasscodeScreen = {
                navController.safeNavigate(mdRouteUseCase.passCode()) {
                    popUpTo(MdRoute.DashboardHost) {
                        inclusive = true
                    }
                }
            })
        }

        composable<MdRoute.TemplateList> { backStackEntry ->
            val questionnaireViewModel =
                backStackEntry.sharedViewModel<QuestionnaireDetailViewModel>(navController = navController)

            TemplateListScreenRoot(
                onBackPress = {
                    navController.popBackStack()
                },
                questionnaireViewModel = questionnaireViewModel,
                onTemplateClick = {
                    navController.safeNavigate(
                        MdRoute.QuestionnaireDetail
                    )
                })

        }

        composable<MdRoute.QuestionnaireDetail> { backStackEntry ->
            val questionnaireViewModel =
                backStackEntry.sharedViewModel<QuestionnaireDetailViewModel>(navController = navController)

            // Get patientId from the parent PatientProfile route
            val parentPatientEntry = remember(backStackEntry) {
                navController.getBackStackEntry<MdRoute.PatientProfile>()
            }
            val parentRoute = parentPatientEntry.toRoute<MdRoute.PatientProfile>()

            QuestionnaireRootScreen(
                questionnaireViewModel = questionnaireViewModel,
                onFinishClick = {
                    navController.popBackStack(
                        route = MdRoute.PatientProfile(
                            patientId = parentRoute.patientId,
                            isSelfCheckInFlow = parentRoute.isSelfCheckInFlow
                        ),
                        inclusive = false
                    )
                },
                onBackPress = {
                    navController.popBackStack()
                })
        }

        composable<MdRoute.ThankYouScreen> {
            ThankYouScreen(
                selectedFacility = authManager.selectedFacility,
                imageLoader = imageLoader,
                onFinish = {
                    if (authManager.isStandAloneUser()) {
                        navController.safeNavigate(MdRoute.SelfCheckInDashboard) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        navController.safeNavigate(mdRouteUseCase.passCode()) {
                            popUpTo(MdRoute.DashboardHost) {
                                inclusive = true
                            }
                        }
                    }
                })
        }

    }
}

fun NavGraphBuilder.dashboardGraph(
    navController: NavController, imageLoader: ImageLoader,
    authManager: AuthManager,
    mdRouteUseCase: MdRouteUseCase,
    sessionManager: SessionManager,
) {

    navigation<MdRoute.DashboardHost>(
        startDestination = MdRoute.Dashboard,
    ) {
        composable<MdRoute.Dashboard> {
            val dashboardViewModel =
                it.sharedViewModel<DashboardViewModel>(navController = navController)
            DashBoardScreenRoot(
                dashboardViewModel = dashboardViewModel,
                imageLoader = imageLoader,
                navigateToPatientProfile = { patientId ->
                    navController.safeNavigate(
                        MdRoute.PatientProfileModuleHost(
                            patientId = patientId, isSelfCheckInFlow = false
                        )
                    )
                },
                navigateToSwitchFacility = {
                    navController.safeNavigate(mdRouteUseCase.selectFacilityFromSettings())
                },
                logOutAndNavigateToTourPage = {
                    authManager.mdLogout()
                },
                navigateToSupport = {
                    navController.safeNavigate(MdRoute.Support)
                },
                navigateToGenderSelection = {
                    navController.safeNavigate(MdRoute.SelectGender)
                },
                popBackStack = {
                    navController.popBackStack()
                }, navigateToSelectAppLock = {
                    navController.safeNavigate(mdRouteUseCase.passcodeFromSettings())
                },
                navigateToWebView = { url, heading ->
                    navController.safeNavigate(
                        MdRoute.SimpleWebView(url = url, heading = heading)
                    )
                },
                isStandAloneUser = authManager.isStandAloneUser(), sessionManager = sessionManager
            )
        }
        composable<MdRoute.SelectGender> { backStackEntry ->
            val dashboardViewModel =
                backStackEntry.sharedViewModel<DashboardViewModel>(navController = navController)
            SelectGenderScreenRoot(
                dashboardViewModel = dashboardViewModel, onBackPressed = {
                    navController.popBackStack()
                })
        }
    }
}

@Composable
inline fun <reified VM : ViewModel> NavBackStackEntry.sharedViewModel(
    navController: NavController
): VM {

    val navGraphRoute = destination.parent?.route ?: return hiltViewModel<VM>()

    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }

    return hiltViewModel(parentEntry)
}

private const val defaultAnimationMillis: Int = 700

fun defaultEnterTransition(): EnterTransition = slideInHorizontally(
    initialOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(defaultAnimationMillis)
)

fun defaultExitTransition(): ExitTransition = slideOutHorizontally(
    targetOffsetX = { fullWidth -> -fullWidth }, animationSpec = tween(defaultAnimationMillis)
)

fun defaultPopEnterTransition(): EnterTransition = slideInHorizontally(
    initialOffsetX = { fullWidth -> -fullWidth }, animationSpec = tween(defaultAnimationMillis)
)

fun defaultPopExitTransition(): ExitTransition = slideOutHorizontally(
    targetOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(defaultAnimationMillis)
)

fun onIdleNavigation(
    authManager: AuthManager,
    navController: NavController,
    mdRouteUseCase: MdRouteUseCase
) {
    if (authManager.isStandAloneUser()) {
        authManager.setWorkFlowType(PatientProfileFlow.BEFORE_HANDOVER)
        navController.safeNavigate(MdRoute.SelfCheckInDashboard) {
            popUpTo(0)
            launchSingleTop = true
        }
    } else {
        navController.safeNavigate(mdRouteUseCase.passcodeFromIdleState()) {
            launchSingleTop = true
        }
    }
}

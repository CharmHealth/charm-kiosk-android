package com.charm.ehr.kiosk.presentation.passcode

//
//@AndroidEntryPoint
//class PasscodeActivity : MdBaseActivity() {
//
//    companion object {
//        fun Activity.intentToPasscodeActivity() {
//            val intent = Intent(this, PasscodeActivity::class.java)
//            startActivity(intent)
//        }
//    }
//
//    private val viewModel: PasscodeViewModel by viewModels()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            MdTheme {
//
//                LaunchedEffect(Unit) {
//                    viewModel.getSettings()
//                }
//
////                PasscodeScreen(onUnlockClicked = { password ->
////                    viewModel.verifyLoginPassword(emailId, password)
////                }, userDisplayName = userDisplayName, facilityName = "")
//
//                val state = viewModel.passcodeState.value
//                if (state == PasscodeState.Success) {
//                    intentToDashboardActivity()
//                } else {
//
//                }
//
//            }
//        }
//    }
//
//}
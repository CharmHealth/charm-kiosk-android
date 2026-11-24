# Workflow

How a user moves through the app, screen by screen. Two journeys — staff and patient self check-in — share several screens (tour, passcode, questionnaire, thank-you).

All screen folders are under `app/src/main/java/com/charm/ehr/kiosk/presentation/`. Routes are declared in `presentation/navigation/Screen.kt` as `MdRoute` entries.

## Kiosk modes

The app runs in one of two modes. The mode is fixed per device by an admin in **Charm EHR → Settings → Facilities → Kiosk Setup**, delivered to the app as `kioskSettings.accountDetails.kioskType` (see `KioskUserType` in `common/AuthManager.kt:58-61`), and determines the entry route after login.

| Mode | `KioskUserType` | Backend display name | Intended use |
|---|---|---|---|
| **Handover flow** | `TABLET_USER` | `"Kiosk Tablet User"` | Staff logs in on the tablet, finds the patient, then **hands the tablet to the patient** to complete questionnaires / consent forms. |
| **Self check-in kiosk flow** | `STANDALONE_USER` | `"Kiosk Standalone User"` | Patient-facing kiosk with no staff login. Patient enters mobile + OTP, verifies DOB, completes forms. |

Mode-aware routing is decided after sign-in in `MainActivity.kt:350-358`:

- `STANDALONE_USER` → `MdRoute.SelfCheckInDashboard` (skips passcode / auth-type setup)
- otherwise → passcode verification or auth-type selection

`AuthManager.isStandAloneUser()` (`AuthManager.kt:105-107`) is the helper used elsewhere (dashboard, settings, worker) to branch on mode.

### Handover sub-state (`PatientProfileFlow`)

Within the Handover flow, a runtime sub-state tracks whether the staff member or the patient is currently in control of the tablet. It is owned by `AuthManager._workFlowType` and exposed as `workFlowType: StateFlow<PatientProfileFlow>`.

| Value | Meaning | Set at |
|---|---|---|
| `BEFORE_HANDOVER` | Staff is driving (default after login or logout) | Init (`AuthManager.kt:73-74`), on logout (`AuthManager.kt:211`), on profile pop / thank-you nav (`MainActivity.kt:790, 800, 810, 1060`) |
| `AFTER_HANDOVER` | Patient is driving the tablet | When opening the patient profile via staff handover or self check-in entry (`MainActivity.kt:709, 736, 783, 854`) |
| `SELF_CHECKIN` | (Reserved) | **Defined but not currently assigned at runtime.** Read only in `PatientProfileScreen.kt` UI branching at lines 140, 248, 270; also referenced once in a `@Preview` at line 598. The self check-in journey currently sets `AFTER_HANDOVER` instead. Treat this enum value as reserved / dead until reintroduced. |

The sub-state drives UI behavior in `PatientProfileScreen.kt` (header text, edit-button visibility, which sections render) and idle-timeout behavior in `SessionManager` (see [Idle-timeout behavior](#idle-timeout-behavior) below).

## Staff journey

| # | Screen | Folder | What happens |
|---|---|---|---|
| 1 | Tour / Idle | `tour_page/` | Attract screen. First touch starts the session and routes to login or dashboard depending on auth state. Hosts `MainActivity` and `SessionManager` (idle timeout). |
| 2 | Login | `tour_page/` | Sign-in through CHIS / Auth0 with the CharmAuth SDK in a Custom Tab (`AuthManager.startSignIn`). The SDK's own `CharmRedirectActivity` receives the OAuth redirect. Failures show on the Tour page (account refused, can't reach the server, timed out, failed). |
| 4 | Passcode | `passcode/` | PIN or biometric verification. Used at login, on idle re-verification, and from Settings to change the auth method. |
| 5 | Select facility | `select_facility/` | Pick the clinic/practice location. Also reachable from Settings to switch. |
| 6 | Dashboard | `dashboard/` | Main hub: patient search, patient list, add-patient form. Entry point for patient profile and self check-in. |
| 7 | Patient profile | `patient_profile/` | Tabbed view of demographics, insurance, appointments, questionnaire history. Driven by `PatientDetailViewmodel`. |
| 8 | Edit profile | `edit_profile/` | Multi-section form to update demographics and insurance. Opened from patient profile for a specific `Section`. Uses `select_gender/` as a sub-picker. |
| 9 | Questionnaire | `questionnaire/` | Render and submit assigned health forms or consent forms. Supports multi-page forms and signatures. |
| 10 | Search templates | `search_template/` | Search and assign questionnaire / consent templates to a visit. Shares `PatientDetailViewmodel` with patient profile. |
| 11 | Telehealth | `tele_health/` | Telehealth session entry from inside the patient profile. |
| 12 | Settings | `settings/` | Logout, switch facility, change auth method (PIN ↔ biometric), view app/build info. |
| 13 | Support | `support/` | In-app support ticket: subject (max 80 chars), message (max 250 chars), up to 5 attachments, auto-collected device info. |

## Patient self check-in journey

| # | Screen | Folder | What happens |
|---|---|---|---|
| 1 | Tour / Idle | `tour_page/` | Same attract screen. Patient taps to start. |
| 2 | Self check-in | `self_check_in/` | Patient enters mobile number → receives OTP → confirms identity. Independent of staff. |
| 3 | Select patient | `select_patient/` | If the mobile number maps to multiple records, the matching list is shown. |
| 4 | Verify DOB | `verify_dob/` | Patient confirms date of birth. Three attempts allowed before the flow is blocked. |
| 5 | Questionnaire | `questionnaire/` | Patient completes assigned forms and consents (shared with staff journey). |
| 6 | Thank you | `thank_you/` | Stateless confirmation screen. Returns to the attract screen after a delay or tap. |

## Shared / utility screens

| Screen | Folder | Used by |
|---|---|---|
| Select gender | `select_gender/` | Picker opened from `edit_profile` |
| Web view | `web_view/` | Embedded URL viewer for privacy policy, terms, etc. Takes `heading` + `url` as nav args. |

## Idle-timeout behavior

`SessionManager` (singleton, defined in `presentation/tour_page/SessionManager.kt`) monitors touch events forwarded from `MainActivity`. After an inactivity period it either returns to the tour/attract screen or prompts passcode re-verification, depending on the workflow type set in `AuthManager` (`BEFORE_HANDOVER`, `AFTER_HANDOVER`, `SELF_CHECKIN`).

Timeout durations are **hardcoded** in `SessionManager.kt`:

| Kiosk type | Idle timeout |
|---|---|
| `STANDALONE_USER` | 1 minute |
| All other types (e.g. `TABLET_USER`) | 10 minutes |

The kiosk type itself comes from the backend via `AuthManager.kioskSettingsFlow`; only the durations are local constants.

## Last updated

2026-05-28

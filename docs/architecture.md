# Architecture

The app uses **MVI (Model–View–Intent)** with **Clean Architecture** layering.

## Layers

- **domain** — pure Kotlin; repository interfaces, use cases, shared domain models. No Android dependencies.
- **data** — Retrofit services, repository implementations, WorkManager workers.
- **presentation** — Compose screens, ViewModels, navigation, theme.

> **Heads-up on DTOs:** response DTOs are not under `data/` — they are co-located with domain models in `domain/CustomModels.kt`. See [Non-obvious conventions](#non-obvious-conventions).

## Folder layout

```
app/src/main/java/com/charm/ehr/kiosk/
├── KioskApplication.kt       # Application entry point, Hilt setup
├── common/                   # Cross-cutting concerns
│   ├── AuthManager.kt        # Sign-in (CharmAuth) & feature flags
│   ├── constants/            # Shared preference keys
│   └── util/                 # Stateless utility helpers
├── di/                       # Hilt dependency injection modules
│   ├── AppModule.kt          # Singletons: Retrofit, OkHttp, DataStore
│   └── RepositoryModule.kt   # Repository interface → implementation bindings
├── domain/                   # Pure Kotlin; no Android dependencies
│   ├── CustomModels.kt       # Shared domain models + response DTOs (Patient, Facility, …)
│   ├── repository/           # Repository interfaces
│   ├── result/               # Operation result types (e.g. AddPatientResult)
│   └── usecase/              # Business logic use-cases
├── data/                     # Android/network implementations
│   ├── network/              # WebService (Retrofit interface) + MdCallback
│   ├── repository/           # Repository implementations
│   └── worker/               # WorkManager workers (server data sync)
└── presentation/             # UI layer: screens, ViewModels, composables, nav
    ├── InAppUpdateActivity.kt # MdInAppUpdateActivity — Apptics version-alert host
    ├── base/                 # MdBaseActivity (no BaseViewModel currently exists)
    ├── composable/           # Reusable UI components (Md* prefix)
    ├── navigation/           # MdRoute sealed interface, NavController extensions
    ├── theme/                # MdTheme, MdColors, typography, shapes
    ├── tour_page/            # Host shell — MainActivity, SessionManager, login side-effects
    └── <feature>/            # One sub-package per feature module
```

> **`tour_page/` is the host shell, not a single feature folder.**
>
> - **Hosts:** `MainActivity.kt` and the singleton `SessionManager` live here.
> - **Login trigger:** the login button calls `AuthManager.startSignIn(activity)` (CharmAuth SDK, Custom Tab). The sign-in runs in `AuthManager`'s scope and its outcome arrives in `AuthManager.signInResult`, which the Tour route in `MainActivity.kt` collects.
> - **`TourPageViewModel`:** only manages login-failure dialog state, not the login trigger itself.

The top-level `domain/usecase/MdRouteUseCase.kt` sits outside the per-domain subfolders because it resolves global navigation routing and is not bound to any single feature domain.

## Data flow per screen

```
Screen → dispatches Action → ViewModel updates State → Screen renders new state
```

ViewModels expose state as `StateFlow<State>`; screens collect via `collectAsStateWithLifecycle()`. Side effects (navigation, dialogs) are emitted as `SharedFlow` events from the ViewModel.

## Use case pattern

- Location: `domain/usecase/<domain>/`
- Signature: `suspend operator fun invoke(...)` with `@Inject constructor`
- Thread dispatch (`withContext(Dispatchers.IO)`) is owned by the use case, not the ViewModel
- Hilt resolves use cases automatically via `@Inject constructor` — no module changes needed
- ViewModel call site:
  ```kotlin
  val result = useCase(...)
  _state.update { when (result) { ... } }
  ```
- `APIState<T>` sealed class (`Loading`, `Success<T>`, `Error`) is defined in `presentation/composable/CommonComposable.kt`

### Migration status

> This snapshot drifts. The code is the source of truth for which ViewModels have migrated; this list reflects what was true on the **Last updated** date below.

Use cases are the convention for **new** code, but the migration is partial:

- `PatientDetailViewmodel` — fully refactored to use cases
- `DashboardViewModel` — partially refactored
- `EditProfileViewModel` — partially refactored; uses use cases for save/insurance/card uploads, but still reads state flows and reference data directly from `PatientDetailRepository` / `PatientRepository`

When editing a legacy ViewModel, prefer continuing the existing style rather than mixing patterns inside one class.

## Non-obvious conventions

- `domain/repository/PatientDetailRepository.kt` imports the `Insurance` type from the presentation layer. This breaks the "domain has no upward dependencies" rule but is the existing convention — do not refactor without team approval.

## Domain repositories

Interfaces in `domain/repository/`, implementations in `data/repository/`.

| Repository | Responsibility |
|---|---|
| `MemberRepository` | Kiosk settings, member info, settings sync |
| `PatientRepository` | Patient search, listing, creation |
| `PatientDetailRepository` | Full patient details and medical history |
| `FacilityRepository` | Facility information and mappings |
| `QuestionnaireRepository` | Template CRUD, questionnaire submissions |
| `AppointmentRepository` | Appointment data |
| `OTPRepository` | OTP generation and validation |
| `FileRepository` | File upload/download |
| `PreferenceRepository` | Local DataStore preferences persistence |
| `WorkerRepository` | Background WorkManager job scheduling |
| `MiscRepository` | Miscellaneous / support ticket submission |

## Navigation

Routes are declared in `presentation/navigation/Screen.kt` as entries in the `MdRoute` sealed interface. Type-safe argument passing is handled by `kotlinx.serialization`.

## Cross-cutting singletons

- **`AuthManager`** (`common/AuthManager.kt`) — Hilt singleton owning sign-in (CharmAuth SDK: restore, sign-in, sign-out, the once-only "session ended" sign-out), connectivity state, logged-in user, facility selection, feature flags (insurance / questionnaires / consent forms), workflow type (`BEFORE_HANDOVER` / `AFTER_HANDOVER` / `SELF_CHECKIN`), user type (`TABLET_USER` / `STANDALONE_USER`), and kiosk-type validation. Triggers automatic logout + navigation reset on auth failures.
- **`SessionManager`** (`presentation/tour_page/SessionManager.kt`) — Monitors touch events from `MainActivity`, fires idle timeout, navigates back to the attract screen or prompts passcode re-verification. Exposed via `SessionManagerEntryPoint` (Hilt entry point).
- **`MDAccountsRelatedUtil`** (`common/util/MDAccountsRelatedUtil.kt`) — EHR base URL (from `secure.properties`), photo/facility logo URL builders, EHR error codes.

Other helpers in `common/util/` include:

- **Stateless utilities:** `DateUtil`, `FileUtil`, `ModelUtil`, `EncryptionUtil`, `MediaUtil`, `TemplatesUtil` / `TemplateUIUtil`, `SecurityUtil`, `MdJsonBuilder`, `AppticsUtil`, `UIUtil`, `MdParsingHelper`, `Extensions`, `CountryMobileCodes`.
- **Stateful:** `KioskCrashHandler` — installed as the process-wide uncaught-exception handler.

## Technical stack

### UI

| Library | Version |
|---|---|
| Jetpack Compose BOM | 2025.10.01 |
| Material3 + WindowSizeClass | (via BOM) |
| Lottie (animations) | 6.4.0 |
| Coil (image loading) | 2.6.0 |

### Navigation, networking, DI

| Library | Version |
|---|---|
| Navigation Component | 2.9.0 |
| Kotlinx Serialization | 1.7.1 |
| Retrofit | 2.11.0 |
| OkHttp Logging Interceptor | 4.9.3 (3-min connect/read/write timeouts) |
| Gson | 2.11.0 |
| Hilt | 2.57.2 |
| Hilt Navigation Compose | 1.2.0 |
| Hilt WorkManager integration | 1.3.0 |

### Background, persistence, auth, analytics

| Library | Version |
|---|---|
| WorkManager | 2.11.0 |
| DataStore Preferences | 1.1.7 |
| Biometric | 1.2.0-alpha05 |
| CharmAuth SDK (charm-mobile-auth) | submodule `native/charm-mobile-auth-sdk` |
| Zoho Apptics | 0.3.6.2 |

### Language & build

| Item | Version |
|---|---|
| Kotlin | 2.2.20 |
| Android Gradle Plugin (AGP) | 8.10.0 |
| Gradle | 8.11.1 |
| Java / JDK | 17 |

## Last updated

2026-05-28

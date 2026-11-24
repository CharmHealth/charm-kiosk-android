package com.charm.ehr.kiosk.presentation.tour_page

import android.util.Log
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.common.KioskUserType
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.minutes

@OptIn(FlowPreview::class)
@Singleton
class SessionManager @Inject constructor(authManager: AuthManager) {

    private val TAG = "SessionManager"

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            Log.d(TAG, "Unhandled SessionManager coroutine error", throwable)
        }
    }

    private val scope = CoroutineScope(
        SupervisorJob() +
                Dispatchers.Main.immediate +
                exceptionHandler
    )
    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()


    private var timeoutMillis: Long = 10.minutes.inWholeMilliseconds

    private val interactionFlow = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1
    )

    init {
        // Observe user type and update timeoutMillis accordingly
        scope.launch {
            authManager.kioskSettingsFlow.collect { settings ->
                val isStandAlone =
                    settings?.accountDetails?.kioskType == KioskUserType.STANDALONE_USER.displayName
                timeoutMillis =
                    if (isStandAlone) 1.minutes.inWholeMilliseconds else 10.minutes.inWholeMilliseconds
                // Start the session timeout
                startSessionTimeout()
            }
        }

    }

    private fun startSessionTimeout() {
        interactionFlow
            .onStart { emit(Unit) }
            .debounce { timeoutMillis }
            .onEach {
                Log.d(TAG, "Session timed out, locking session")
                if (state.value.isPasscodeAllowed) {
                    lock()
                }
            }
            .launchIn(scope)
    }

    fun userInteracted() {
        interactionFlow.tryEmit(Unit)
    }

    fun unlock() {
        _state.update { it.copy(isLocked = false) }
        interactionFlow.tryEmit(Unit)
    }

    fun lock() {
        if (state.value.isLocked) return
        _state.update { it.copy(isLocked = true) }
    }

    fun updateIsPasscodeAllowed(allowPasscode: Boolean) {
        _state.update { it.copy(isPasscodeAllowed = allowPasscode) }
        if (allowPasscode) {
            interactionFlow.tryEmit(Unit)
        }
    }

    fun getIsPasscodeAllowed(): Boolean {
        return state.value.isPasscodeAllowed
    }

}

data class SessionState(
    val isLocked: Boolean = false,
    val isPasscodeAllowed: Boolean = true
)

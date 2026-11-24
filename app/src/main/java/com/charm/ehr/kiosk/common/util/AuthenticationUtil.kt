package com.charm.ehr.kiosk.common.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.charm.ehr.kiosk.BuildConfig
import com.charm.ehr.kiosk.common.SignInFailure
import com.charm.ehr.kiosk.common.SignInResult
import com.charmhealth.auth.AuthState
import com.charmhealth.auth.CharmAuthAndroid
import com.charmhealth.auth.CharmAuthClient
import com.charmhealth.auth.CharmAuthConfig
import com.charmhealth.auth.CharmAuthException
import com.charmhealth.auth.CharmAuthIOException
import com.charmhealth.auth.CharmAuthInterceptor
import com.charmhealth.auth.CustomTabsAuthBrowser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import javax.inject.Inject
import javax.inject.Singleton

/** How the CharmAuth interceptor refused an API call. */
enum class ApiAuthFailure {
    /** No usable sign-in (ended, missing, or the API refuses the user even with a fresh token). */
    SESSION_ENDED,

    /** Misconfiguration (e.g. a host missing from charmAuth.apiOrigins): never retried. */
    SECURITY_VIOLATION
}

/** The only place the app uses the CharmAuth SDK. */
@Singleton
class AuthenticationUtil @Inject constructor(
    @ApplicationContext appContext: Context,
) {

    private val client: CharmAuthClient = createClient(appContext)

    /** Emits when the server ends the sign-in (user deactivated or blocked, refresh revoked); not on the kiosk's own sign-out. */
    val sessionEndedByServer: Flow<Unit> = client.state
        .filter { it is AuthState.SignedOut && it.sessionEnded }
        .map { }

    /**
     * Loads the stored sign-in; true when signed in. No network, unless it completes a sign-in the
     * OS interrupted (the app was ended while the sign-in page was open).
     */
    suspend fun restoreSession(): Boolean = client.restore() is AuthState.SignedIn

    /** Signs in through a Custom Tab. */
    suspend fun signIn(activity: Activity): SignInResult = try {
        client.signIn(CustomTabsAuthBrowser(activity))
        SignInResult.Success
    } catch (e: CharmAuthException.Cancelled) {
        SignInResult.Ignored
    } catch (e: CharmAuthException.SignInInProgress) {
        SignInResult.Ignored
    } catch (e: CharmAuthException) {
        Log.d(TAG, "sign-in failed: ${e::class.simpleName}")
        SignInResult.Failed(signInFailure(e))
    }

    /** Revokes the refresh token at CHIS and clears the SDK's sign-in. */
    suspend fun signOut() {
        client.signOut()
    }

    /** Adds the access token (only for charmAuth.apiOrigins) and refreshes it once on a 401. */
    fun authInterceptor(): Interceptor = CharmAuthInterceptor(client)

    companion object {
        private const val TAG = "CharmAuth"

        /** The sign-in problem behind a failed API call, or null if [e] isn't one. */
        fun apiAuthFailureOf(e: Exception): ApiAuthFailure? =
            when ((e as? CharmAuthIOException)?.authError) {
                is CharmAuthException.SessionEnded,
                is CharmAuthException.NotSignedIn,
                is CharmAuthException.AccessDenied -> ApiAuthFailure.SESSION_ENDED
                is CharmAuthException.SecurityViolation -> ApiAuthFailure.SECURITY_VIOLATION
                else -> null
            }

        /** The SDK reports the OAuth code; the kiosk decides what it means. */
        private fun signInFailure(e: CharmAuthException): SignInFailure = when (e) {
            // access_denied: refused by a rule; unauthorized: Auth0 has blocked the user (e.g. CHIS
            // deactivated them). Other codes are request problems, not the account.
            is CharmAuthException.AuthorizationDenied ->
                if (e.error == "access_denied" || e.error == "unauthorized") SignInFailure.ACCOUNT_REFUSED
                else SignInFailure.OTHER
            is CharmAuthException.Network,
            is CharmAuthException.TemporarilyUnavailable,
            is CharmAuthException.ServerError -> SignInFailure.UNREACHABLE
            is CharmAuthException.SignInExpired -> SignInFailure.EXPIRED
            else -> SignInFailure.OTHER
        }

        /** The config is per environment, from secure.properties; the access token is sent only to apiOrigins. */
        private fun createClient(appContext: Context): CharmAuthClient {
            fun list(value: String, separator: Char) =
                value.split(separator).map { it.trim() }.filter { it.isNotEmpty() }
            val apiOrigins = list(BuildConfig.AUTH_API_ORIGINS, ',').toSet()
            if (BuildConfig.DEBUG) {
                // Every API URL is EHR_BASE_URL + a path: if its origin isn't allowed, every call is
                // refused. Say so now rather than through a failed request later.
                val baseOrigin = MDAccountsRelatedUtil.baseUrl.toHttpUrlOrNull()?.let { url ->
                    val defaultPort = if (url.scheme == "https") 443 else 80
                    "${url.scheme}://${url.host}" + (if (url.port != defaultPort) ":${url.port}" else "")
                }
                if (baseOrigin == null || baseOrigin !in apiOrigins) {
                    Log.w(TAG, "charmAuth.apiOrigins $apiOrigins doesn't include charmAuth.ehrBaseUrl's origin $baseOrigin: every API call will be refused")
                }
            }
            return CharmAuthClient(
                CharmAuthConfig(
                    issuer = BuildConfig.AUTH_ISSUER,
                    clientId = BuildConfig.AUTH_CLIENT_ID,
                    redirectUri = "charm.kiosk:/oauth2/callback",
                    audience = BuildConfig.AUTH_AUDIENCE.ifEmpty { null },
                    additionalScopes = list(BuildConfig.AUTH_ADDITIONAL_SCOPES, ' '),
                    apiOrigins = apiOrigins
                    ),
                CharmAuthAndroid.platform(appContext),
                // The SDK never logs tokens, codes or keys.
                logger = { level, message -> Log.i(TAG, "[$level] $message") },
            )
        }
    }
}

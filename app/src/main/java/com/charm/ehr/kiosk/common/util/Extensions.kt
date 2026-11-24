package com.charm.ehr.kiosk.common.util

import UiText
import android.content.Context
import android.util.Log
import com.charm.ehr.kiosk.BuildConfig
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.AuthManager
import com.charm.ehr.kiosk.common.UserStatus
import com.charm.ehr.kiosk.common.util.ModelUtil.Companion.isNotJsonNull
import com.charm.ehr.kiosk.domain.APIResponseError
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.joda.time.DateTime
import org.joda.time.ReadableInstant
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

fun JsonObject.getString(key: String): String? {

    val reqEle = get(key)

    if (reqEle != null && reqEle.isNotJsonNull()) {
        return reqEle.asString
    }

    return null
}

fun JsonObject.getInt(key: String): Int? {
    try {
        val reqEle = get(key)
        if (reqEle != null && reqEle.isNotJsonNull()) {
            return try {
                reqEle.asInt
            } catch (e: Exception) {
                val intStr = reqEle.asString ?: ""
                intStr.toIntOrNull()
            }
        }
    } catch (e: Exception) {
        if (BuildConfig.DEBUG) {
            Log.d("getInt", e.toString())
        }
    }
    return null
}

fun JsonObject.getBoolean(key: String): Boolean? {
    try {
        val reqEle = get(key)

        if (reqEle != null && reqEle.isNotJsonNull()) {
            return try {
                reqEle.asBoolean
            } catch (e: Exception) {
                val intStr = reqEle.asString ?: ""
                intStr.toBooleanStrictOrNull()
            }
        }
    } catch (e: Exception) {
        if (BuildConfig.DEBUG) {
            Log.d("getBoolean", e.toString())
        }
    }
    return null
}

fun JsonObject.getJsonArray(key: String): JsonArray? {

    try {
        val reqEle = get(key)

        if (reqEle != null && reqEle.isNotJsonNull()) {
            return reqEle.asJsonArray
        }
    } catch (e: Exception) {
        return null
    }

    return null
}

fun JsonObject.getJsonObject(key: String): JsonObject? {

    try {
        val reqEle = get(key)

        if (reqEle != null && reqEle.isNotJsonNull()) {
            return reqEle.asJsonObject
        }
    } catch (e: Exception) {
        return null
    }

    return null
}

fun DateTime.isBeforeOrEqual(readableInstant: ReadableInstant): Boolean {
    return isBefore(readableInstant) || isEqual(readableInstant)
}

fun DateTime.isAfterOrEqual(readableInstant: ReadableInstant): Boolean {
    return isAfter(readableInstant) || isEqual(readableInstant)
}

class CharmAPI @Inject constructor(
    private val authManager: AuthManager,
    @ApplicationContext private val appContext: Context
) {

    //Note: Use either onSuccess or onStringSuccess, not both
    suspend fun <T> executeApiCall(
        ignoreCodeZero: Boolean = false,
        call: suspend () -> Response<JsonElement>,
        onSuccess: ((JsonObject) -> T)? = null,
        onStringSuccess: ((String) -> T)? = null,
        onError: (error: APIResponseError, errorBody: JsonObject?) -> APIState<T> = { error, _ -> APIState.Error(error) },
    ): APIState<T> {
        return withContext(Dispatchers.IO) {
            try {
                authManager.awaitSessionRestored()
                val response = call()
                if (response.isSuccessful) {

                    if (onStringSuccess != null) {
                        val bodyString = response.body()?.asString
                        if (bodyString != null) {
                            return@withContext APIState.Success(onStringSuccess(bodyString))
                        } else {
                            return@withContext APIState.Error(
                                APIResponseError(
                                    null,
                                    null,
                                    UiText.DynamicString("Empty response received")
                                )
                            )
                        }
                    }

                    val jsonObject = response.body()?.asJsonObject
                    val code = jsonObject?.get("code")?.asInt
                    if ((code == 0 || ignoreCodeZero) && jsonObject != null && onSuccess != null) {
                        APIState.Success(onSuccess.invoke(jsonObject))
                    } else {
                        APIState.Error(
                            APIResponseError(
                                code,
                                response.code(),
                                UiText.DynamicString(jsonObject?.get("message")?.asString ?: "")
                            )
                        )
                    }
                } else {
                    // Parse the error body once here so the stream is not consumed twice.
                    val errorBodyJson = try {
                        val element = com.google.gson.JsonParser.parseString(response.errorBody()?.string())
                        if (!element.isJsonNull) element.asJsonObject else null
                    } catch (e: Exception) { null }

                    val apiError = authManager.apiErrorResponseHandeler(errorBodyJson) ?: APIResponseError(
                        null,
                        response.code(),
                        UiText.DynamicString("Unknown error")
                    )
                    onError(apiError, errorBodyJson)
                }
            } catch (e: Exception) {
                APIState.Error(handleException(e))
            }
        }
    }

    suspend fun <T> executeBinaryApiCall(
        call: suspend () -> Response<ResponseBody>,
        onSuccess: (ResponseBody) -> T,
        onError: (APIResponseError) -> APIState<T> = { APIState.Error(it) },
    ): APIState<T> {
        return withContext(Dispatchers.IO) {
            try {
                authManager.awaitSessionRestored()
                val response = call()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        APIState.Success(onSuccess(body))
                    } else {
                        APIState.Error(APIResponseError(null, null, null))
                    }
                } else {
                    APIState.Error(
                        authManager.apiErrorResponseHandlerForResponseBody(response)
                    )
                }
            } catch (e: Exception) {

                APIState.Error(handleException(e))
            }
        }
    }

    private fun handleException(e: Exception): APIResponseError {

        when (AuthenticationUtil.apiAuthFailureOf(e)) {
            // An unattended kiosk can't recover: sign out.
            ApiAuthFailure.SESSION_ENDED -> {
                authManager.onSessionEnded()
                return APIResponseError(null, null, null)
            }
            ApiAuthFailure.SECURITY_VIOLATION ->
                Log.e("CharmAPI", "request refused by CharmAuth: ${e.message}")
            null -> {}
        }

        if (authManager.userStatusStateFlow.value != UserStatus.ONLINE) {
            return APIResponseError(
                ModelUtil.internetErrorCode, null, UiText.StrResId(R.string.no_internet_connection)
            )
        }

        return when (e) {
            is IOException -> APIResponseError(null, null, UiText.StrResId(R.string.network_error))
            is SocketTimeoutException -> APIResponseError(
                null,
                null,
                UiText.StrResId(R.string.request_timeout)
            )

            is APIResponseError -> e
            else -> APIResponseError(null, null, UiText.StrResId(R.string.something_went_wrong))
        }
    }

}
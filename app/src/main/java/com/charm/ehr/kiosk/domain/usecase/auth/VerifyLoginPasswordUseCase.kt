package com.charm.ehr.kiosk.domain.usecase.auth

import com.charm.ehr.kiosk.domain.repository.MemberRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Verifies the login password against the server.
 *
 * Business rule: the API returns the string "true" or "false" to indicate whether the
 * credentials are valid. This use case converts that string to [APIState<Boolean>] so
 * callers never need to handle the raw string comparison.
 */
class VerifyLoginPasswordUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    suspend operator fun invoke(userName: String, password: String): APIState<Boolean> =
        withContext(Dispatchers.IO) {
            when (val result = memberRepository.verifyPassword(userName, password)) {
                is APIState.Success -> APIState.Success(result.data.equals("true", ignoreCase = true))
                is APIState.Error -> APIState.Error(result.apiResponseError)
                is APIState.Loading -> APIState.Loading
            }
        }
}

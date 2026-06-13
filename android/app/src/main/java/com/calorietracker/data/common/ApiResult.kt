package com.calorietracker.data.common

import retrofit2.HttpException
import java.io.IOException

/**
 * Outcome of a single API call, mapped into a form the UI layer can render
 * without knowing about HTTP or exceptions.
 */
sealed interface ApiResult<out T> {

    /** The call succeeded and produced [data]. */
    data class Success<T>(val data: T) : ApiResult<T>

    /** The call failed; [message] is suitable for display to the user. */
    data class Failure(val message: String) : ApiResult<Nothing>
}

/**
 * Runs [block], translating an [HttpException] or [IOException] into an
 * [ApiResult.Failure] with a user-facing message. Any other throwable propagates.
 */
suspend fun <T> apiCall(block: suspend () -> T): ApiResult<T> =
    try {
        ApiResult.Success(block())
    } catch (e: HttpException) {
        ApiResult.Failure(messageFor(e.code()))
    } catch (e: IOException) {
        ApiResult.Failure("Could not reach the server. Check your connection.")
    }

private fun messageFor(httpCode: Int): String = when (httpCode) {
    401 -> "Your session has expired. Please log in again."
    404 -> "The requested item could not be found."
    else -> "Something went wrong. Please try again."
}

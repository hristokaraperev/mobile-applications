package com.calorietracker.data.auth

/** Authenticated user as used by the app. */
data class User(
    val id: Long,
    val email: String,
    val displayName: String?,
)

/** Outcome of a login/register attempt. */
sealed interface AuthResult {

    /** Authentication succeeded; the token has been persisted. */
    data class Success(val user: User) : AuthResult

    /** Authentication failed; [message] is suitable for display to the user. */
    data class Failure(val message: String) : AuthResult
}

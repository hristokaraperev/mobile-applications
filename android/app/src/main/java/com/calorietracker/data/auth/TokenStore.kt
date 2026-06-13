package com.calorietracker.data.auth

import kotlinx.coroutines.flow.Flow

/**
 * Persistent store for the authenticated user's JWT. Survives process death so a
 * returning user stays logged in.
 */
interface TokenStore {

    /** Emits the current token, or `null` when the user is not authenticated. */
    val token: Flow<String?>

    /** Reads the currently stored token once, or `null` if none is stored. */
    suspend fun currentToken(): String?

    /** Persists [token], replacing any previously stored value. */
    suspend fun saveToken(token: String)

    /** Removes the stored token (e.g. on logout). */
    suspend fun clear()
}

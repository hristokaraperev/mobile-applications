package com.calorietracker.data.auth

import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Coordinates authentication: calls the API, persists the returned JWT, and maps
 * the response (or any error) into a domain [AuthResult] for the UI layer.
 */
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
) {

    suspend fun login(email: String, password: String): AuthResult =
        authenticate { api.login(LoginRequest(email, password)) }

    suspend fun register(email: String, password: String, displayName: String?): AuthResult =
        authenticate { api.register(RegisterRequest(email, password, displayName)) }

    /**
     * Runs an auth [call], persisting the token on success. On an HTTP error the
     * token is left untouched and a user-facing [AuthResult.Failure] is returned.
     */
    private suspend fun authenticate(call: suspend () -> AuthResponse): AuthResult =
        try {
            val response = call()
            tokenStore.saveToken(response.accessToken)

            AuthResult.Success(response.user.toUser())
        } catch (e: HttpException) {
            AuthResult.Failure(messageFor(e.code()))
        } catch (e: IOException) {
            AuthResult.Failure("Could not reach the server. Check your connection.")
        }

    private fun messageFor(httpCode: Int): String = when (httpCode) {
        401 -> "Invalid email or password."
        409 -> "An account with this email already exists."
        else -> "Authentication failed. Please try again."
    }

    private fun UserDto.toUser(): User = User(id = id, email = email, displayName = displayName)
}

package com.calorietracker.data.auth

import kotlinx.serialization.Serializable

/** Request body for `POST /auth/login`. */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

/** Request body for `POST /auth/register`. */
@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String? = null,
)

/** Response payload returned by both auth endpoints. */
@Serializable
data class AuthResponse(
    val accessToken: String,
    val user: UserDto,
)

/** User as returned by the API. */
@Serializable
data class UserDto(
    val id: Long,
    val email: String,
    val displayName: String? = null,
)

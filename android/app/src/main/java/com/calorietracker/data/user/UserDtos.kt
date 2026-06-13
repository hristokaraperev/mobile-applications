package com.calorietracker.data.user

import kotlinx.serialization.Serializable

/** User profile as returned by `GET`/`PUT /users/me`. */
@Serializable
data class UserProfileDto(
    val id: Long,
    val email: String,
    val displayName: String? = null,
    val dailyKcalGoal: Int? = null,
)

/** Request body for `PUT /users/me`. */
@Serializable
data class UpdateUserRequest(
    val dailyKcalGoal: Int,
)

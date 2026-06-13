package com.calorietracker.data.user

import com.calorietracker.data.common.ApiResult
import com.calorietracker.data.common.apiCall
import javax.inject.Inject

/** The authenticated user's profile as used by the app. */
data class UserProfile(
    val id: Long,
    val email: String,
    val displayName: String?,
    val dailyKcalGoal: Int?,
)

/** Reads and updates the authenticated user's profile via the API. */
class UserRepository @Inject constructor(
    private val api: UserApi,
) {

    /** Fetches the current user's profile. */
    suspend fun getProfile(): ApiResult<UserProfile> =
        apiCall { api.me().toDomain() }

    /** Updates the user's daily kcal goal to [goal] and returns the updated profile. */
    suspend fun updateDailyKcalGoal(goal: Int): ApiResult<UserProfile> =
        apiCall { api.updateMe(UpdateUserRequest(goal)).toDomain() }

    private fun UserProfileDto.toDomain(): UserProfile =
        UserProfile(id = id, email = email, displayName = displayName, dailyKcalGoal = dailyKcalGoal)
}

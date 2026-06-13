package com.calorietracker.data.user

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

/** Retrofit binding for the backend user-profile endpoints. */
interface UserApi {

    /** Returns the authenticated user's profile, including their daily kcal goal. */
    @GET("users/me")
    suspend fun me(): UserProfileDto

    /** Updates the authenticated user's profile and returns the updated view. */
    @PUT("users/me")
    suspend fun updateMe(@Body request: UpdateUserRequest): UserProfileDto
}

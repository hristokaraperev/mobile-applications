package com.calorietracker.data.auth

import retrofit2.http.Body
import retrofit2.http.POST

/** Retrofit binding for the backend authentication endpoints. */
interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
}

package com.calorietracker.data.diary

import retrofit2.http.GET
import retrofit2.http.Query

/** Retrofit binding for the backend diary endpoints. */
interface DiaryApi {

    /** Returns all non-deleted entries for the authenticated user on [date] (ISO `yyyy-MM-dd`). */
    @GET("diary")
    suspend fun entries(@Query("date") date: String): List<DiaryEntryDto>

    /** Returns per-meal and daily totals for the authenticated user on [date]. */
    @GET("diary/summary")
    suspend fun summary(@Query("date") date: String): DiarySummaryDto
}

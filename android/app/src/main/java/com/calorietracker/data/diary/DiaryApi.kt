package com.calorietracker.data.diary

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/** Retrofit binding for the backend diary endpoints. */
interface DiaryApi {

    /** Creates a diary entry, snapshotting nutrition server-side from the referenced food. */
    @POST("diary")
    suspend fun create(@Body request: CreateDiaryEntryRequest): DiaryEntryDto

    /** Returns all non-deleted entries for the authenticated user on [date] (ISO `yyyy-MM-dd`). */
    @GET("diary")
    suspend fun entries(@Query("date") date: String): List<DiaryEntryDto>

    /** Returns per-meal and daily totals for the authenticated user on [date]. */
    @GET("diary/summary")
    suspend fun summary(@Query("date") date: String): DiarySummaryDto
}

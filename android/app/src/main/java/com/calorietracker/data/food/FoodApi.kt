package com.calorietracker.data.food

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Retrofit binding for the backend food endpoints. */
interface FoodApi {

    /** Searches foods by name, returning up to 50 results ordered alphabetically. */
    @GET("foods/search")
    suspend fun search(@Query("q") query: String): List<FoodDto>

    /** Returns a single food item by its ID. */
    @GET("foods/{id}")
    suspend fun getById(@Path("id") id: Long): FoodDto
}

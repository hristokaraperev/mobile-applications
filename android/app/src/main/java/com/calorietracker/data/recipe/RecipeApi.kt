package com.calorietracker.data.recipe

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Retrofit binding for the backend recipe endpoints. */
interface RecipeApi {

    /** Returns all non-deleted recipes owned by the authenticated user. */
    @GET("recipes")
    suspend fun list(): List<RecipeDto>

    /** Returns a single recipe with its ingredients and computed nutrition. */
    @GET("recipes/{id}")
    suspend fun getById(@Path("id") id: Long): RecipeDto

    /** Creates a recipe, returning it with computed total and per-portion nutrition. */
    @POST("recipes")
    suspend fun create(@Body request: RecipeRequestDto): RecipeDto

    /** Replaces a recipe's fields and ingredients, returning recomputed nutrition. */
    @PUT("recipes/{id}")
    suspend fun update(@Path("id") id: Long, @Body request: RecipeRequestDto): RecipeDto

    /** Soft-deletes the recipe with [id]. */
    @DELETE("recipes/{id}")
    suspend fun delete(@Path("id") id: Long)
}

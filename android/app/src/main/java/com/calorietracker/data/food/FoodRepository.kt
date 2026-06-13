package com.calorietracker.data.food

import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/** Outcome of a food search. */
sealed interface FoodSearchResult {

    /** The search completed; [foods] may be empty. */
    data class Success(val foods: List<FoodDto>) : FoodSearchResult

    /** The search failed; [message] is suitable for display to the user. */
    data class Failure(val message: String) : FoodSearchResult
}

/** Outcome of looking up a single food by ID. */
sealed interface FoodLookupResult {

    /** The food was found. */
    data class Success(val food: FoodDto) : FoodLookupResult

    /** The lookup failed; [message] is suitable for display to the user. */
    data class Failure(val message: String) : FoodLookupResult
}

/** Looks up foods from the API, mapping transport errors to user-facing results. */
class FoodRepository @Inject constructor(
    private val api: FoodApi,
) {

    /** Searches foods by [query], returning a [FoodSearchResult]. */
    suspend fun search(query: String): FoodSearchResult =
        try {
            FoodSearchResult.Success(api.search(query))
        } catch (e: HttpException) {
            FoodSearchResult.Failure("Search failed. Please try again.")
        } catch (e: IOException) {
            FoodSearchResult.Failure("Could not reach the server. Check your connection.")
        }

    /** Looks up the food with [id], returning a [FoodLookupResult]. */
    suspend fun foodById(id: Long): FoodLookupResult =
        try {
            FoodLookupResult.Success(api.getById(id))
        } catch (e: HttpException) {
            FoodLookupResult.Failure("Could not load this food. Please try again.")
        } catch (e: IOException) {
            FoodLookupResult.Failure("Could not reach the server. Check your connection.")
        }
}

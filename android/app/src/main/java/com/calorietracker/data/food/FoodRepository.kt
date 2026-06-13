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
}

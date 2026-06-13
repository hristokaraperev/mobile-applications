package com.calorietracker.data.recipe

import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/** Outcome of loading the user's recipes. */
sealed interface RecipeListResult {

    /** The list loaded; [recipes] may be empty. */
    data class Success(val recipes: List<RecipeDto>) : RecipeListResult

    /** The load failed; [message] is suitable for display to the user. */
    data class Failure(val message: String) : RecipeListResult
}

/** Outcome of loading a single recipe. */
sealed interface RecipeLoadResult {

    /** The recipe was found. */
    data class Success(val recipe: RecipeDto) : RecipeLoadResult

    /** The load failed; [message] is suitable for display to the user. */
    data class Failure(val message: String) : RecipeLoadResult
}

/** Outcome of creating or updating a recipe. */
sealed interface RecipeSaveResult {

    /** The recipe was saved; [recipe] is the server's view of it. */
    data class Success(val recipe: RecipeDto) : RecipeSaveResult

    /** The save failed; [message] is suitable for display to the user. */
    data class Failure(val message: String) : RecipeSaveResult
}

/** Reads and mutates the user's recipes via the API, mapping transport errors to results. */
class RecipeRepository @Inject constructor(
    private val api: RecipeApi,
) {

    /** Loads all of the user's recipes. */
    suspend fun list(): RecipeListResult =
        try {
            RecipeListResult.Success(api.list())
        } catch (e: HttpException) {
            RecipeListResult.Failure("Could not load your recipes. Please try again.")
        } catch (e: IOException) {
            RecipeListResult.Failure("Could not reach the server. Check your connection.")
        }

    /** Loads a single recipe by [id]. */
    suspend fun getById(id: Long): RecipeLoadResult =
        try {
            RecipeLoadResult.Success(api.getById(id))
        } catch (e: HttpException) {
            RecipeLoadResult.Failure("Could not load this recipe. Please try again.")
        } catch (e: IOException) {
            RecipeLoadResult.Failure("Could not reach the server. Check your connection.")
        }

    /** Creates a recipe from [request]. */
    suspend fun create(request: RecipeRequestDto): RecipeSaveResult =
        save { api.create(request) }

    /** Replaces the recipe [id] with [request]. */
    suspend fun update(id: Long, request: RecipeRequestDto): RecipeSaveResult =
        save { api.update(id, request) }

    private inline fun save(call: () -> RecipeDto): RecipeSaveResult =
        try {
            RecipeSaveResult.Success(call())
        } catch (e: HttpException) {
            RecipeSaveResult.Failure("Could not save this recipe. Please try again.")
        } catch (e: IOException) {
            RecipeSaveResult.Failure("Could not reach the server. Check your connection.")
        }

    /** Soft-deletes the recipe with [id], returning whether the call succeeded. */
    suspend fun delete(id: Long): Boolean =
        try {
            api.delete(id)
            true
        } catch (e: HttpException) {
            false
        } catch (e: IOException) {
            false
        }
}

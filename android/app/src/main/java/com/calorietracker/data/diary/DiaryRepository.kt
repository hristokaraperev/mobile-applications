package com.calorietracker.data.diary

import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

/** A day's diary entries paired with its nutrition summary. */
data class DiaryDay(
    val date: LocalDate,
    val entries: List<DiaryEntryDto>,
    val summary: DiarySummaryDto,
)

/** Outcome of logging a diary entry. */
sealed interface LogEntryResult {

    /** The entry was created. */
    data object Success : LogEntryResult

    /** Logging failed; [message] is suitable for display to the user. */
    data class Failure(val message: String) : LogEntryResult
}

/** Fetches a day's diary entries and totals, and logs new entries, via the API. */
class DiaryRepository @Inject constructor(
    private val api: DiaryApi,
) {

    /** Loads the entries and summary for [date] in a single call. */
    suspend fun loadDay(date: LocalDate): DiaryDay {
        val iso = date.toString()

        return DiaryDay(
            date = date,
            entries = api.entries(iso),
            summary = api.summary(iso),
        )
    }

    /** Logs [quantityGrams] of food [foodId] to [mealType] on [entryDate]. */
    suspend fun logFood(
        entryDate: LocalDate,
        mealType: MealType,
        foodId: Long,
        quantityGrams: Double,
    ): LogEntryResult =
        try {
            api.create(
                CreateDiaryEntryRequest(
                    entryDate = entryDate.toString(),
                    mealType = mealType.name,
                    sourceType = "FOOD",
                    foodId = foodId,
                    quantity = quantityGrams,
                )
            )

            LogEntryResult.Success
        } catch (e: HttpException) {
            LogEntryResult.Failure("Could not save this entry. Please try again.")
        } catch (e: IOException) {
            LogEntryResult.Failure("Could not reach the server. Check your connection.")
        }

    /** Logs [portions] of recipe [recipeId] to [mealType] on [entryDate]. */
    suspend fun logRecipePortion(
        entryDate: LocalDate,
        mealType: MealType,
        recipeId: Long,
        portions: Double,
    ): LogEntryResult =
        try {
            api.create(
                CreateDiaryEntryRequest(
                    entryDate = entryDate.toString(),
                    mealType = mealType.name,
                    sourceType = "RECIPE_PORTION",
                    recipeId = recipeId,
                    quantity = portions,
                )
            )

            LogEntryResult.Success
        } catch (e: HttpException) {
            LogEntryResult.Failure("Could not save this entry. Please try again.")
        } catch (e: IOException) {
            LogEntryResult.Failure("Could not reach the server. Check your connection.")
        }
}

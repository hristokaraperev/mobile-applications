package com.calorietracker.data.diary

import kotlinx.serialization.Serializable

/** A single diary entry as returned by the API; nutrition is the snapshot at write time. */
@Serializable
data class DiaryEntryDto(
    val id: String,
    val entryDate: String,
    val mealType: String,
    val sourceType: String,
    val foodId: Long? = null,
    val recipeId: Long? = null,
    val quantity: Double,
    val kcal: Double,
    val proteinG: Double? = null,
    val carbsG: Double? = null,
    val fatG: Double? = null,
    val deleted: Boolean = false,
    val updatedAt: String? = null,
)

/**
 * Human-readable quantity label for this entry: grams for a food entry, a singular/plural
 * portion count for a recipe-portion entry. The unit is derived from [sourceType], since a
 * food entry's [quantity] is grams while a recipe-portion entry's is a number of portions.
 */
fun DiaryEntryDto.quantityLabel(): String =
    if (sourceType == "RECIPE_PORTION") {
        val portions = quantity.toInt()
        "$portions ${if (portions == 1) "portion" else "portions"}"
    } else {
        "${quantity.toInt()} g"
    }

/** Per-meal and daily nutrition totals for a date, compared against the user's kcal goal. */
@Serializable
data class DiarySummaryDto(
    val date: String,
    val dailyKcalGoal: Int? = null,
    val totalKcal: Double,
    val totalProteinG: Double? = null,
    val totalCarbsG: Double? = null,
    val totalFatG: Double? = null,
    val meals: Map<String, MealTotalsDto> = emptyMap(),
)

/**
 * Request body for `POST /diary`. Nutrition is snapshotted server-side from the
 * referenced food or recipe; [quantity] is grams for foods, portions for recipes.
 */
@Serializable
data class CreateDiaryEntryRequest(
    val entryDate: String,
    val mealType: String,
    val sourceType: String,
    val foodId: Long? = null,
    val recipeId: Long? = null,
    val quantity: Double,
)

/** Nutrition totals for a single meal type within a [DiarySummaryDto]. */
@Serializable
data class MealTotalsDto(
    val kcal: Double,
    val proteinG: Double? = null,
    val carbsG: Double? = null,
    val fatG: Double? = null,
)

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

/** Nutrition totals for a single meal type within a [DiarySummaryDto]. */
@Serializable
data class MealTotalsDto(
    val kcal: Double,
    val proteinG: Double? = null,
    val carbsG: Double? = null,
    val fatG: Double? = null,
)

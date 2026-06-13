package com.calorietracker.data.recipe

import kotlinx.serialization.Serializable

/** Nutrition figures (kcal and macros) for a recipe's total or a single portion. */
@Serializable
data class NutritionDto(
    val kcal: Double? = null,
    val proteinG: Double? = null,
    val carbsG: Double? = null,
    val fatG: Double? = null,
)

/** A single ingredient line: a food and the grams of it used in the recipe. */
@Serializable
data class RecipeIngredientDto(
    val foodId: Long,
    val grams: Double,
)

/** A recipe as returned by the API, including computed total and per-portion nutrition. */
@Serializable
data class RecipeDto(
    val id: Long,
    val name: String,
    val numberOfPortions: Int,
    val totalCookedWeightG: Double? = null,
    val ingredients: List<RecipeIngredientDto> = emptyList(),
    val total: NutritionDto = NutritionDto(),
    val perPortion: NutritionDto = NutritionDto(),
    val deleted: Boolean = false,
    val updatedAt: String? = null,
)

/** Request body for `POST /recipes` and `PUT /recipes/{id}`; nutrition is computed server-side. */
@Serializable
data class RecipeRequestDto(
    val name: String,
    val numberOfPortions: Int,
    val totalCookedWeightG: Double? = null,
    val ingredients: List<RecipeIngredientDto>,
)

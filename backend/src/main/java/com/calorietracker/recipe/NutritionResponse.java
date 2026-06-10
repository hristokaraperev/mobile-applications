package com.calorietracker.recipe;

/**
 * Nutrition figures (kcal and macros) for a recipe's total or a single portion.
 */
public record NutritionResponse(
        Double kcal,
        Double proteinG,
        Double carbsG,
        Double fatG
) {
}

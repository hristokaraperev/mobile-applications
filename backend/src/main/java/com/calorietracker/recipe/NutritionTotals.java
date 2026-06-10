package com.calorietracker.recipe;

/**
 * Summed per-100g nutrition across a recipe's ingredients, scaled by their grams.
 */
public record NutritionTotals(
        double kcal,
        double proteinG,
        double carbsG,
        double fatG
) {
}

package com.calorietracker.recipe;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Response payload for a single recipe, including computed total and per-portion nutrition.
 */
public record RecipeResponse(
        Long id,
        String name,
        Integer numberOfPortions,
        Double totalCookedWeightG,
        List<RecipeIngredientResponse> ingredients,
        NutritionResponse total,
        NutritionResponse perPortion,
        boolean deleted,
        OffsetDateTime updatedAt
) {
}

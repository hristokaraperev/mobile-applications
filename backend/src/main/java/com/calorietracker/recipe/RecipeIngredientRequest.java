package com.calorietracker.recipe;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * A single ingredient line in a recipe create/update request.
 */
public record RecipeIngredientRequest(
        @NotNull Long foodId,
        @NotNull @Positive Double grams
) {
}

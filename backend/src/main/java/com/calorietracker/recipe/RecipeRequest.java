package com.calorietracker.recipe;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Request body for creating or replacing a recipe and its ingredients.
 */
public record RecipeRequest(
        @NotBlank String name,
        @NotNull @Positive Integer numberOfPortions,
        Double totalCookedWeightG,
        @NotEmpty @Valid List<RecipeIngredientRequest> ingredients
) {
}

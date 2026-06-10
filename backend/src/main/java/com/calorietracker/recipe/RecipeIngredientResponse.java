package com.calorietracker.recipe;

/**
 * A single ingredient line in a recipe response.
 */
public record RecipeIngredientResponse(
        Long foodId,
        Double grams
) {

    /** Build a response from a {@link RecipeIngredient} entity. */
    public static RecipeIngredientResponse from(RecipeIngredient ingredient) {
        return new RecipeIngredientResponse(ingredient.getFoodId(), ingredient.getGrams());
    }
}

package com.calorietracker.recipe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Persistence operations for {@link RecipeIngredient}.
 */
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {

    /** Returns all ingredient lines belonging to a recipe. */
    List<RecipeIngredient> findByRecipeId(Long recipeId);

    /** Removes all ingredient lines belonging to a recipe, e.g. before replacing them on update. */
    void deleteByRecipeId(Long recipeId);
}

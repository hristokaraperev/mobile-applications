package com.calorietracker.recipe;

import com.calorietracker.food.Food;
import com.calorietracker.food.FoodRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Computes summed nutrition for a recipe's ingredients (per-100g values scaled by grams).
 */
@Service
public class RecipeNutritionService {

    private final RecipeIngredientRepository recipeIngredientRepository;
    private final FoodRepository foodRepository;

    public RecipeNutritionService(RecipeIngredientRepository recipeIngredientRepository, FoodRepository foodRepository) {
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.foodRepository = foodRepository;
    }

    /** Sums nutrition across all ingredients of the given recipe. */
    public NutritionTotals computeTotal(Long recipeId) {
        return computeTotal(recipeIngredientRepository.findByRecipeId(recipeId));
    }

    /** Sums nutrition across the given ingredient lines. Throws 422 if an ingredient's food is missing. */
    public NutritionTotals computeTotal(List<RecipeIngredient> ingredients) {
        double kcal = 0, protein = 0, carbs = 0, fat = 0;

        for (RecipeIngredient ingredient : ingredients) {
            Food food = foodRepository.findById(ingredient.getFoodId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Ingredient food not found"));
            double grams = ingredient.getGrams();

            kcal += food.getEnergyKcal() * grams / 100.0;
            if (food.getProteinG() != null) protein += food.getProteinG() * grams / 100.0;
            if (food.getCarbsG() != null) carbs += food.getCarbsG() * grams / 100.0;
            if (food.getFatG() != null) fat += food.getFatG() * grams / 100.0;
        }

        return new NutritionTotals(kcal, protein, carbs, fat);
    }
}

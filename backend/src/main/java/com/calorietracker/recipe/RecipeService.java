package com.calorietracker.recipe;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Business logic for recipe CRUD and nutrition computation.
 */
@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeNutritionService recipeNutritionService;

    public RecipeService(
            RecipeRepository recipeRepository,
            RecipeIngredientRepository recipeIngredientRepository,
            RecipeNutritionService recipeNutritionService
    ) {
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.recipeNutritionService = recipeNutritionService;
    }

    /**
     * Creates a recipe with its ingredients, owned by {@code userId}.
     */
    public RecipeResponse create(RecipeRequest req, Long userId) {
        Recipe recipe = new Recipe();
        recipe.setUserId(userId);
        recipe.setName(req.name());
        recipe.setNumberOfPortions(req.numberOfPortions());
        recipe.setTotalCookedWeightG(req.totalCookedWeightG());
        recipe.setUpdatedAt(OffsetDateTime.now());
        recipe.setDeleted(false);
        recipe = recipeRepository.save(recipe);

        List<RecipeIngredient> ingredients = saveIngredients(recipe.getId(), req.ingredients());

        return toResponse(recipe, ingredients);
    }

    /**
     * Returns a recipe owned by {@code userId}, including ingredients and computed nutrition.
     * Throws 404 if not found, soft-deleted, or not owned by {@code userId}.
     */
    public RecipeResponse findById(Long id, Long userId) {
        Recipe recipe = recipeRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<RecipeIngredient> ingredients = recipeIngredientRepository.findByRecipeId(id);

        return toResponse(recipe, ingredients);
    }

    private List<RecipeIngredient> saveIngredients(Long recipeId, List<RecipeIngredientRequest> requests) {
        return requests.stream().map(req -> {
            RecipeIngredient ingredient = new RecipeIngredient();
            ingredient.setRecipeId(recipeId);
            ingredient.setFoodId(req.foodId());
            ingredient.setGrams(req.grams());

            return recipeIngredientRepository.save(ingredient);
        }).toList();
    }

    private RecipeResponse toResponse(Recipe recipe, List<RecipeIngredient> ingredients) {
        NutritionTotals total = recipeNutritionService.computeTotal(ingredients);
        int portions = recipe.getNumberOfPortions();

        NutritionResponse totalResponse = new NutritionResponse(
                round2(total.kcal()), round2(total.proteinG()), round2(total.carbsG()), round2(total.fatG())
        );
        NutritionResponse perPortionResponse = new NutritionResponse(
                round2(total.kcal() / portions), round2(total.proteinG() / portions),
                round2(total.carbsG() / portions), round2(total.fatG() / portions)
        );

        return new RecipeResponse(
                recipe.getId(),
                recipe.getName(),
                recipe.getNumberOfPortions(),
                recipe.getTotalCookedWeightG(),
                ingredients.stream().map(RecipeIngredientResponse::from).toList(),
                totalResponse,
                perPortionResponse,
                recipe.isDeleted(),
                recipe.getUpdatedAt()
        );
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

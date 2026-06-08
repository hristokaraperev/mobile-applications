package com.calorietracker.diary;

import com.calorietracker.food.Food;
import com.calorietracker.food.FoodRepository;
import com.calorietracker.recipe.Recipe;
import com.calorietracker.recipe.RecipeIngredient;
import com.calorietracker.recipe.RecipeIngredientRepository;
import com.calorietracker.recipe.RecipeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Business logic for diary entry creation, retrieval, update, and sync operations.
 */
@Service
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final FoodRepository foodRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;

    public DiaryService(
            DiaryRepository diaryRepository,
            FoodRepository foodRepository,
            RecipeRepository recipeRepository,
            RecipeIngredientRepository recipeIngredientRepository
    ) {
        this.diaryRepository = diaryRepository;
        this.foodRepository = foodRepository;
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
    }

    /**
     * Creates a diary entry, snapshotting nutrition from the referenced food or recipe at write time.
     */
    public DiaryEntryResponse create(CreateDiaryEntryRequest req, Long userId) {
        DiaryEntry entry = new DiaryEntry();
        entry.setId(UUID.randomUUID());
        entry.setUserId(userId);
        entry.setEntryDate(req.entryDate());
        entry.setMealType(req.mealType());
        entry.setSourceType(req.sourceType());
        entry.setQuantity(req.quantity());
        entry.setDeleted(false);
        entry.setUpdatedAt(OffsetDateTime.now());

        if (req.sourceType() == DiarySourceType.FOOD) {
            snapshotFromFood(entry, req.foodId(), req.quantity());
        } else {
            snapshotFromRecipe(entry, req.recipeId(), req.quantity());
        }

        return DiaryEntryResponse.from(diaryRepository.save(entry));
    }

    /**
     * Returns all non-deleted diary entries for the authenticated user on the given date.
     */
    public List<DiaryEntryResponse> findByDate(Long userId, LocalDate date) {
        return diaryRepository.findByUserIdAndEntryDateAndDeletedFalse(userId, date)
                .stream()
                .map(DiaryEntryResponse::from)
                .toList();
    }

    /**
     * Updates the quantity and meal type of a diary entry and recalculates the nutrition snapshot.
     * Throws 404 if the entry is not found or not owned by {@code userId}.
     */
    public DiaryEntryResponse update(UUID id, UpdateDiaryEntryRequest req, Long userId) {
        DiaryEntry entry = diaryRepository.findById(id)
                .filter(e -> e.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        entry.setMealType(req.mealType());
        entry.setQuantity(req.quantity());
        entry.setUpdatedAt(OffsetDateTime.now());

        if (entry.getSourceType() == DiarySourceType.FOOD) {
            snapshotFromFood(entry, entry.getFoodId(), req.quantity());
        } else {
            snapshotFromRecipe(entry, entry.getRecipeId(), req.quantity());
        }

        return DiaryEntryResponse.from(diaryRepository.save(entry));
    }

    /**
     * Soft-deletes the diary entry owned by {@code userId}. Throws 404 if not found or not owned by the user.
     */
    public void softDelete(UUID id, Long userId) {
        DiaryEntry entry = diaryRepository.findById(id)
                .filter(e -> e.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        entry.setDeleted(true);
        entry.setUpdatedAt(OffsetDateTime.now());
        diaryRepository.save(entry);
    }

    private void snapshotFromFood(DiaryEntry entry, Long foodId, double quantityGrams) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found"));

        entry.setFoodId(foodId);
        entry.setKcal(round2(food.getEnergyKcal() * quantityGrams / 100.0));
        entry.setProteinG(food.getProteinG() != null ? round2(food.getProteinG() * quantityGrams / 100.0) : null);
        entry.setCarbsG(food.getCarbsG() != null ? round2(food.getCarbsG() * quantityGrams / 100.0) : null);
        entry.setFatG(food.getFatG() != null ? round2(food.getFatG() * quantityGrams / 100.0) : null);
    }

    private void snapshotFromRecipe(DiaryEntry entry, Long recipeId, double portions) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));

        List<RecipeIngredient> ingredients = recipeIngredientRepository.findByRecipeId(recipeId);

        double totalKcal = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0;
        for (RecipeIngredient ing : ingredients) {
            Food food = foodRepository.findById(ing.getFoodId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Ingredient food not found"));
            double grams = ing.getGrams();
            totalKcal += food.getEnergyKcal() * grams / 100.0;
            if (food.getProteinG() != null) totalProtein += food.getProteinG() * grams / 100.0;
            if (food.getCarbsG() != null) totalCarbs += food.getCarbsG() * grams / 100.0;
            if (food.getFatG() != null) totalFat += food.getFatG() * grams / 100.0;
        }

        int numberOfPortions = recipe.getNumberOfPortions();
        entry.setRecipeId(recipeId);
        entry.setKcal(round2(totalKcal / numberOfPortions * portions));
        entry.setProteinG(round2(totalProtein / numberOfPortions * portions));
        entry.setCarbsG(round2(totalCarbs / numberOfPortions * portions));
        entry.setFatG(round2(totalFat / numberOfPortions * portions));
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

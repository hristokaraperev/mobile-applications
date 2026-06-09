package com.calorietracker.diary;

import com.calorietracker.food.Food;
import com.calorietracker.food.FoodRepository;
import com.calorietracker.recipe.Recipe;
import com.calorietracker.recipe.RecipeIngredient;
import com.calorietracker.recipe.RecipeIngredientRepository;
import com.calorietracker.recipe.RecipeRepository;
import com.calorietracker.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final UserRepository userRepository;

    public DiaryService(
            DiaryRepository diaryRepository,
            FoodRepository foodRepository,
            RecipeRepository recipeRepository,
            RecipeIngredientRepository recipeIngredientRepository,
            UserRepository userRepository
    ) {
        this.diaryRepository = diaryRepository;
        this.foodRepository = foodRepository;
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.userRepository = userRepository;
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
     * Returns per-meal and daily nutrition totals for the authenticated user on the given date.
     */
    public DiarySummaryResponse summary(Long userId, LocalDate date) {
        List<DiaryEntry> entries = diaryRepository.findByUserIdAndEntryDateAndDeletedFalse(userId, date);

        Integer dailyKcalGoal = userRepository.findById(userId)
                .map(u -> u.getDailyKcalGoal())
                .orElse(null);

        Map<String, double[]> mealTotals = new LinkedHashMap<>();
        double totalKcal = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0;

        for (DiaryEntry e : entries) {
            String meal = e.getMealType().name();
            double[] t = mealTotals.computeIfAbsent(meal, k -> new double[4]);
            t[0] += orZero(e.getKcal());
            t[1] += orZero(e.getProteinG());
            t[2] += orZero(e.getCarbsG());
            t[3] += orZero(e.getFatG());
            totalKcal += orZero(e.getKcal());
            totalProtein += orZero(e.getProteinG());
            totalCarbs += orZero(e.getCarbsG());
            totalFat += orZero(e.getFatG());
        }

        Map<String, DiarySummaryResponse.MealTotals> meals = new LinkedHashMap<>();
        mealTotals.forEach((meal, t) ->
                meals.put(meal, new DiarySummaryResponse.MealTotals(
                        round2(t[0]), round2(t[1]), round2(t[2]), round2(t[3])
                ))
        );

        return new DiarySummaryResponse(date, dailyKcalGoal,
                round2(totalKcal), round2(totalProtein), round2(totalCarbs), round2(totalFat),
                meals);
    }

    /**
     * Batch upserts diary entries from an offline sync payload.
     * New entries have their nutrition snapshotted; existing entries are updated in-place.
     */
    public List<DiaryEntryResponse> sync(List<SyncDiaryEntryRequest> items, Long userId) {
        return items.stream().map(req -> {
            DiaryEntry entry = diaryRepository.findById(req.id())
                    .filter(e -> e.getUserId().equals(userId))
                    .orElseGet(DiaryEntry::new);

            boolean isNew = entry.getId() == null;
            entry.setId(req.id());
            entry.setUserId(userId);
            entry.setEntryDate(req.entryDate());
            entry.setMealType(req.mealType());
            entry.setSourceType(req.sourceType());
            entry.setQuantity(req.quantity());
            entry.setDeleted(req.deleted());
            entry.setUpdatedAt(OffsetDateTime.now());

            if (isNew || entry.getSourceType() == DiarySourceType.FOOD) {
                if (req.sourceType() == DiarySourceType.FOOD) {
                    snapshotFromFood(entry, req.foodId(), req.quantity());
                } else {
                    snapshotFromRecipe(entry, req.recipeId(), req.quantity());
                }
            }

            return DiaryEntryResponse.from(diaryRepository.save(entry));
        }).toList();
    }

    /**
     * Returns all entries (including soft-deleted) for the authenticated user modified after {@code since}.
     */
    public List<DiaryEntryResponse> findChanges(Long userId, OffsetDateTime since) {
        return diaryRepository.findByUserIdAndUpdatedAtAfter(userId, since)
                .stream()
                .map(DiaryEntryResponse::from)
                .toList();
    }

    private static double orZero(Double v) {
        return v != null ? v : 0.0;
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

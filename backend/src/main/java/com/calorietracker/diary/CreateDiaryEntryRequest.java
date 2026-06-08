package com.calorietracker.diary;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Request body for creating a diary entry.
 * Nutrition values are snapshotted from the referenced food or recipe at creation time.
 */
public record CreateDiaryEntryRequest(
        @NotNull LocalDate entryDate,
        @NotNull MealType mealType,
        @NotNull DiarySourceType sourceType,
        Long foodId,
        Long recipeId,
        @NotNull @Positive Double quantity
) {
}

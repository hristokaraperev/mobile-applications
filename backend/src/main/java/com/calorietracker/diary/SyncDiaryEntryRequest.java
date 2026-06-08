package com.calorietracker.diary;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a single diary entry in a batch sync payload.
 * The client controls the UUID so that offline-generated IDs are preserved.
 */
public record SyncDiaryEntryRequest(
        @NotNull UUID id,
        @NotNull LocalDate entryDate,
        @NotNull MealType mealType,
        @NotNull DiarySourceType sourceType,
        Long foodId,
        Long recipeId,
        @NotNull Double quantity,
        boolean deleted
) {
}

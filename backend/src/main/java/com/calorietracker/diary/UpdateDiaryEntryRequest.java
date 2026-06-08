package com.calorietracker.diary;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request body for updating a diary entry's quantity and meal type.
 * Nutrition snapshot is recalculated from the updated quantity.
 */
public record UpdateDiaryEntryRequest(
        @NotNull @Positive Double quantity,
        @NotNull MealType mealType
) {
}

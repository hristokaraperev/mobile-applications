package com.calorietracker.diary;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response payload for a single diary entry.
 * Nutrition values reflect the snapshot taken at creation or last update time.
 */
public record DiaryEntryResponse(
        UUID id,
        LocalDate entryDate,
        String mealType,
        String sourceType,
        Long foodId,
        Long recipeId,
        Double quantity,
        String itemName,
        Double kcal,
        Double proteinG,
        Double carbsG,
        Double fatG,
        boolean deleted,
        OffsetDateTime updatedAt
) {

    /** Build a response from a {@link DiaryEntry} entity. */
    public static DiaryEntryResponse from(DiaryEntry e) {
        return new DiaryEntryResponse(
                e.getId(),
                e.getEntryDate(),
                e.getMealType().name(),
                e.getSourceType().name(),
                e.getFoodId(),
                e.getRecipeId(),
                e.getQuantity(),
                e.getItemName(),
                e.getKcal(),
                e.getProteinG(),
                e.getCarbsG(),
                e.getFatG(),
                e.isDeleted(),
                e.getUpdatedAt()
        );
    }
}

package com.calorietracker.food;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code POST /foods} (user-contributed food label).
 * All per-100g nutrition values except {@code energyKcal} are optional.
 */
public record CreateFoodRequest(
        @NotBlank String name,
        String brand,
        String barcode,
        @NotNull Double energyKcal,
        Double proteinG,
        Double carbsG,
        Double sugarsG,
        Double fatG,
        Double satFatG,
        Double fiberG,
        Double saltG,
        Double servingSizeG
) {
}

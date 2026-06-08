package com.calorietracker.food;

/**
 * Response payload for a single food item.
 */
public record FoodResponse(
        Long id,
        String name,
        String brand,
        String barcode,
        String type,
        String source,
        Double energyKcal,
        Double proteinG,
        Double carbsG,
        Double sugarsG,
        Double fatG,
        Double satFatG,
        Double fiberG,
        Double saltG,
        Double servingSizeG
) {

    /** Build a response from a {@link Food} entity. */
    public static FoodResponse from(Food food) {
        return new FoodResponse(
                food.getId(),
                food.getName(),
                food.getBrand(),
                food.getBarcode(),
                food.getType().name(),
                food.getSource().name(),
                food.getEnergyKcal(),
                food.getProteinG(),
                food.getCarbsG(),
                food.getSugarsG(),
                food.getFatG(),
                food.getSatFatG(),
                food.getFiberG(),
                food.getSaltG(),
                food.getServingSizeG()
        );
    }
}

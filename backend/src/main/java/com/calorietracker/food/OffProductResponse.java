package com.calorietracker.food;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Minimal Jackson mapping of the Open Food Facts v2 product response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OffProductResponse(
        int status,
        Product product
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Product(
            @JsonProperty("product_name") String productName,
            String brands,
            String code,
            Nutriments nutriments
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Nutriments(
            @JsonProperty("energy-kcal_100g") Double energyKcal100g,
            @JsonProperty("proteins_100g") Double proteins100g,
            @JsonProperty("carbohydrates_100g") Double carbohydrates100g,
            @JsonProperty("sugars_100g") Double sugars100g,
            @JsonProperty("fat_100g") Double fat100g,
            @JsonProperty("saturated-fat_100g") Double saturatedFat100g,
            @JsonProperty("fiber_100g") Double fiber100g,
            @JsonProperty("salt_100g") Double salt100g
    ) {}
}

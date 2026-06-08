package com.calorietracker.food;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;

/**
 * Calls the Open Food Facts v2 API to resolve product barcodes.
 * Results are licensed under ODbL (https://opendatacommons.org/licenses/odbl/).
 */
@Component
public class OffClientImpl implements OffClient {

    private static final String BASE_URL = "https://world.openfoodfacts.org";
    private static final String USER_AGENT = "CalorieTracker/1.0 (info.karaperevi@gmail.com)";

    private final RestClient restClient;

    public OffClientImpl() {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("User-Agent", USER_AGENT)
                .build();
    }

    @Override
    public Optional<Food> fetchByBarcode(String ean) {
        try {
            OffProductResponse response = restClient.get()
                    .uri("/api/v2/product/{ean}", ean)
                    .retrieve()
                    .body(OffProductResponse.class);

            if (response == null || response.status() != 1 || response.product() == null) {
                return Optional.empty();
            }

            return Optional.of(mapToFood(response.product(), ean));
        } catch (RestClientResponseException ex) {
            // OFF returns 4xx (e.g. 404) for unknown barcodes; treat any error response as not found
            return Optional.empty();
        }
    }

    private Food mapToFood(OffProductResponse.Product product, String ean) {
        Food food = new Food();
        food.setName(product.productName() != null ? product.productName() : ean);
        food.setBrand(product.brands());
        food.setBarcode(ean);
        food.setSource(FoodSource.OFF);
        food.setType(FoodType.PACKAGED);

        OffProductResponse.Nutriments n = product.nutriments();
        if (n != null) {
            food.setEnergyKcal(n.energyKcal100g());
            food.setProteinG(n.proteins100g());
            food.setCarbsG(n.carbohydrates100g());
            food.setSugarsG(n.sugars100g());
            food.setFatG(n.fat100g());
            food.setSatFatG(n.saturatedFat100g());
            food.setFiberG(n.fiber100g());
            food.setSaltG(n.salt100g());
        }

        return food;
    }
}

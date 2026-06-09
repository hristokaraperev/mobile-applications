package com.calorietracker.food;

import java.util.Optional;

/**
 * Client for fetching product data from the Open Food Facts API.
 */
public interface OffClient {

    /**
     * Fetch a product by its EAN barcode.
     * Returns empty if the product is not found in OFF.
     */
    Optional<Food> fetchByBarcode(String ean);
}

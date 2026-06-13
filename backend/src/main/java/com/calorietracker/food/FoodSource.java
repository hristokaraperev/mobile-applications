package com.calorietracker.food;

/**
 * Where a {@link Food} item's data originated from.
 */
public enum FoodSource {
    /** Fetched from Open Food Facts via the barcode proxy. */
    OFF,
    /** Seeded from the CIQUAL French food composition database. */
    CIQUAL,
    /** Created directly by a user. */
    USER
}

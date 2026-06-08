package com.calorietracker.food;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for food lookup and user-contributed food creation.
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/foods")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    /**
     * Search foods by name across all sources.
     * Returns up to 50 results ordered alphabetically.
     */
    @GetMapping("/search")
    public List<FoodResponse> search(@RequestParam String q) {
        return foodService.search(q);
    }

    /** Returns a single food item by ID. */
    @GetMapping("/{id}")
    public FoodResponse getById(@PathVariable Long id) {
        return foodService.findById(id);
    }

    /**
     * Returns food by EAN barcode. Serves from the Postgres cache if available;
     * otherwise proxies Open Food Facts, persists the result, and returns it.
     * Data sourced from OFF is licensed under ODbL (https://opendatacommons.org/licenses/odbl/).
     */
    @GetMapping("/barcode/{ean}")
    public FoodResponse getByBarcode(@PathVariable String ean) {
        return foodService.lookupByBarcode(ean);
    }

    /**
     * Creates a user-contributed food label scoped to the authenticated user.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FoodResponse create(
            @Valid @RequestBody CreateFoodRequest req,
            @AuthenticationPrincipal Long userId
    ) {
        return foodService.create(req, userId);
    }
}

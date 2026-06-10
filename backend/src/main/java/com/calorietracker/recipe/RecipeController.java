package com.calorietracker.recipe;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for recipe management. All endpoints require authentication.
 */
@RestController
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /**
     * Creates a recipe with its ingredients, returning computed total and per-portion nutrition.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecipeResponse create(
            @Valid @RequestBody RecipeRequest req,
            @AuthenticationPrincipal Long userId
    ) {
        return recipeService.create(req, userId);
    }

    /**
     * Returns all non-deleted recipes owned by the authenticated user.
     */
    @GetMapping
    public List<RecipeResponse> list(@AuthenticationPrincipal Long userId) {
        return recipeService.findAll(userId);
    }

    /**
     * Returns a recipe with its ingredients and computed total/per-portion nutrition.
     */
    @GetMapping("/{id}")
    public RecipeResponse getById(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        return recipeService.findById(id, userId);
    }

    /**
     * Replaces a recipe's fields and ingredients, recomputing nutrition totals.
     */
    @PutMapping("/{id}")
    public RecipeResponse update(
            @PathVariable Long id,
            @Valid @RequestBody RecipeRequest req,
            @AuthenticationPrincipal Long userId
    ) {
        return recipeService.update(id, req, userId);
    }

    /** Soft-deletes a recipe owned by the authenticated user. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        recipeService.softDelete(id, userId);
    }
}

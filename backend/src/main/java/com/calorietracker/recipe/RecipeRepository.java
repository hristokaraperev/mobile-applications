package com.calorietracker.recipe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for {@link Recipe}.
 */
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    /** Returns all non-deleted recipes owned by a user. */
    List<Recipe> findByUserIdAndDeletedFalse(Long userId);

    /** Returns a non-deleted recipe by ID, scoped to its owner. */
    Optional<Recipe> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);
}

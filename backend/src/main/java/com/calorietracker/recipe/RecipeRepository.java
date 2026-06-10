package com.calorietracker.recipe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByUserIdAndDeletedFalse(Long userId);

    Optional<Recipe> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);
}

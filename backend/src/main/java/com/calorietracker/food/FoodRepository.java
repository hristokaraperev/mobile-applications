package com.calorietracker.food;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for {@link Food}.
 */
public interface FoodRepository extends JpaRepository<Food, Long> {

    /** Looks up a food by its barcode, e.g. for the OFF barcode scan cache. */
    Optional<Food> findByBarcode(String barcode);

    /** Searches foods by name (case-insensitive, partial match), ordered alphabetically. */
    List<Food> findByNameContainingIgnoreCaseOrderByNameAsc(String query, Pageable pageable);

    /** Checks whether any food from the given source exists, e.g. to skip re-seeding CIQUAL data. */
    boolean existsBySource(FoodSource source);
}

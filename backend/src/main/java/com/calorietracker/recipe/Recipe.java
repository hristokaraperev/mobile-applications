package com.calorietracker.recipe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * A user-owned recipe composed of {@link RecipeIngredient}s, used to compute and
 * log per-portion nutrition via {@link com.calorietracker.diary.DiarySourceType#RECIPE_PORTION}.
 */
@Entity
@Table(name = "recipes")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    /** Number of equal portions the recipe is divided into, used for per-portion nutrition math. */
    @Column(name = "number_of_portions", nullable = false)
    private Integer numberOfPortions;

    /** Total weight of the finished dish in grams, if known; used to refine per-portion weight. */
    @Column(name = "total_cooked_weight_g")
    private Double totalCookedWeightG;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /** Soft-delete flag; deleted recipes are retained so clients can sync the deletion. */
    @Column(nullable = false)
    private boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNumberOfPortions() {
        return numberOfPortions;
    }

    public void setNumberOfPortions(Integer numberOfPortions) {
        this.numberOfPortions = numberOfPortions;
    }

    public Double getTotalCookedWeightG() {
        return totalCookedWeightG;
    }

    public void setTotalCookedWeightG(Double totalCookedWeightG) {
        this.totalCookedWeightG = totalCookedWeightG;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}

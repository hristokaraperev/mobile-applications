package com.calorietracker.diary;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A single logged entry in a user's food diary for a given date and meal.
 *
 * <p>An entry is logged either directly from a {@link com.calorietracker.food.Food}
 * (when {@code sourceType} is {@link DiarySourceType#FOOD}, with {@code foodId} set)
 * or as a portion of a {@link com.calorietracker.recipe.Recipe} (when {@code sourceType}
 * is {@link DiarySourceType#RECIPE_PORTION}, with {@code recipeId} set). The nutrition
 * fields ({@code kcal}, {@code proteinG}, {@code carbsG}, {@code fatG}) are snapshotted
 * at log time so later edits to the source food or recipe don't retroactively change
 * historical diary totals.
 */
@Entity
@Table(name = "diary_entries")
public class DiaryEntry {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private DiarySourceType sourceType;

    /** ID of the logged {@link com.calorietracker.food.Food}; set only when {@code sourceType} is {@code FOOD}. */
    @Column(name = "food_id")
    private Long foodId;

    /** ID of the logged {@link com.calorietracker.recipe.Recipe}; set only when {@code sourceType} is {@code RECIPE_PORTION}. */
    @Column(name = "recipe_id")
    private Long recipeId;

    /** Amount logged: grams for a food entry, number of portions for a recipe entry. */
    @Column(nullable = false)
    private Double quantity;

    /** Energy for this entry, snapshotted at log time. */
    @Column(nullable = false)
    private Double kcal;

    /** Protein in grams, snapshotted at log time. */
    @Column(name = "protein_g")
    private Double proteinG;

    /** Carbohydrates in grams, snapshotted at log time. */
    @Column(name = "carbs_g")
    private Double carbsG;

    /** Fat in grams, snapshotted at log time. */
    @Column(name = "fat_g")
    private Double fatG;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /** Soft-delete flag; deleted entries are retained so clients can sync the deletion. */
    @Column(nullable = false)
    private boolean deleted;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public DiarySourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(DiarySourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public Long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Long recipeId) {
        this.recipeId = recipeId;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getKcal() {
        return kcal;
    }

    public void setKcal(Double kcal) {
        this.kcal = kcal;
    }

    public Double getProteinG() {
        return proteinG;
    }

    public void setProteinG(Double proteinG) {
        this.proteinG = proteinG;
    }

    public Double getCarbsG() {
        return carbsG;
    }

    public void setCarbsG(Double carbsG) {
        this.carbsG = carbsG;
    }

    public Double getFatG() {
        return fatG;
    }

    public void setFatG(Double fatG) {
        this.fatG = fatG;
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

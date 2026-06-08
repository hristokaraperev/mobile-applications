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

    @Column(name = "food_id")
    private Long foodId;

    @Column(name = "recipe_id")
    private Long recipeId;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private Double kcal;

    @Column(name = "protein_g")
    private Double proteinG;

    @Column(name = "carbs_g")
    private Double carbsG;

    @Column(name = "fat_g")
    private Double fatG;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

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

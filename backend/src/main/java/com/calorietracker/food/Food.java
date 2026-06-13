package com.calorietracker.food;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * A food item with per-100g nutrition data, sourced either from an external database
 * (Open Food Facts, CIQUAL) or created by a user.
 */
@Entity
@Table(name = "foods")
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String brand;

    /** EAN/UPC barcode used to look up packaged foods scanned via the Android client. */
    private String barcode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FoodType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FoodSource source;

    /** Energy per 100g, in kcal. */
    @Column(name = "energy_kcal")
    private Double energyKcal;

    /** Protein per 100g, in grams. */
    @Column(name = "protein_g")
    private Double proteinG;

    /** Carbohydrates per 100g, in grams. */
    @Column(name = "carbs_g")
    private Double carbsG;

    /** Sugars per 100g, in grams. */
    @Column(name = "sugars_g")
    private Double sugarsG;

    /** Fat per 100g, in grams. */
    @Column(name = "fat_g")
    private Double fatG;

    /** Saturated fat per 100g, in grams. */
    @Column(name = "sat_fat_g")
    private Double satFatG;

    /** Fiber per 100g, in grams. */
    @Column(name = "fiber_g")
    private Double fiberG;

    /** Salt per 100g, in grams. */
    @Column(name = "salt_g")
    private Double saltG;

    /** Reference serving size in grams, used to suggest a default portion. */
    @Column(name = "serving_size_g")
    private Double servingSizeG;

    /** ID of the user who created this food; {@code null} for foods from external sources. */
    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public FoodType getType() {
        return type;
    }

    public void setType(FoodType type) {
        this.type = type;
    }

    public FoodSource getSource() {
        return source;
    }

    public void setSource(FoodSource source) {
        this.source = source;
    }

    public Double getEnergyKcal() {
        return energyKcal;
    }

    public void setEnergyKcal(Double energyKcal) {
        this.energyKcal = energyKcal;
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

    public Double getSugarsG() {
        return sugarsG;
    }

    public void setSugarsG(Double sugarsG) {
        this.sugarsG = sugarsG;
    }

    public Double getFatG() {
        return fatG;
    }

    public void setFatG(Double fatG) {
        this.fatG = fatG;
    }

    public Double getSatFatG() {
        return satFatG;
    }

    public void setSatFatG(Double satFatG) {
        this.satFatG = satFatG;
    }

    public Double getFiberG() {
        return fiberG;
    }

    public void setFiberG(Double fiberG) {
        this.fiberG = fiberG;
    }

    public Double getSaltG() {
        return saltG;
    }

    public void setSaltG(Double saltG) {
        this.saltG = saltG;
    }

    public Double getServingSizeG() {
        return servingSizeG;
    }

    public void setServingSizeG(Double servingSizeG) {
        this.servingSizeG = servingSizeG;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

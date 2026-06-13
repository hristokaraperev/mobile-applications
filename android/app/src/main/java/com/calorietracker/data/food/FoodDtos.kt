package com.calorietracker.data.food

import kotlinx.serialization.Serializable

/** A food item as returned by the API; nutrition values are per 100 g. */
@Serializable
data class FoodDto(
    val id: Long,
    val name: String,
    val brand: String? = null,
    val barcode: String? = null,
    val type: String,
    val source: String,
    val energyKcal: Double? = null,
    val proteinG: Double? = null,
    val carbsG: Double? = null,
    val sugarsG: Double? = null,
    val fatG: Double? = null,
    val satFatG: Double? = null,
    val fiberG: Double? = null,
    val saltG: Double? = null,
    val servingSizeG: Double? = null,
)

/** Request body for creating a user-contributed food label; nutrition values are per 100 g. */
@Serializable
data class CreateFoodRequest(
    val name: String,
    val brand: String? = null,
    val barcode: String? = null,
    val energyKcal: Double,
    val proteinG: Double? = null,
    val carbsG: Double? = null,
    val sugarsG: Double? = null,
    val fatG: Double? = null,
    val satFatG: Double? = null,
    val fiberG: Double? = null,
    val saltG: Double? = null,
    val servingSizeG: Double? = null,
)

package com.calorietracker.data.diary

/**
 * The four meal sections a diary entry can belong to. Names match the backend
 * [MealType] enum so values round-trip over the wire as plain strings.
 */
enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK,
}

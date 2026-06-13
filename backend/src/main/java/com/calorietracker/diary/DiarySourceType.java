package com.calorietracker.diary;

/**
 * Identifies what a {@link DiaryEntry} was logged from.
 */
public enum DiarySourceType {
    /** Entry was logged directly from a {@link com.calorietracker.food.Food}. */
    FOOD,
    /** Entry was logged as a portion of a {@link com.calorietracker.recipe.Recipe}. */
    RECIPE_PORTION
}

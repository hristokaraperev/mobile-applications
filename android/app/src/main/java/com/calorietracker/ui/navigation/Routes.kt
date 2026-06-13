package com.calorietracker.ui.navigation

import com.calorietracker.data.diary.MealType
import java.time.LocalDate

/** Navigation destinations within the app. */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DIARY = "diary"

    const val RECIPES = "recipes"

    const val ARG_MEAL_TYPE = "mealType"
    const val ARG_DATE = "date"
    const val ARG_FOOD_ID = "foodId"
    const val ARG_RECIPE_ID = "recipeId"

    /** Key under which the reused food-search flow returns a picked ingredient to the editor. */
    const val RESULT_INGREDIENT_FOOD_ID = "ingredientFoodId"
    const val ARG_BARCODE = "barcode"

    /** Food search, scoped to the meal and date the user is adding to. */
    const val FOOD_SEARCH = "foodSearch/{$ARG_MEAL_TYPE}/{$ARG_DATE}"

    /** Food detail, carrying the selected food plus the target meal and date. */
    const val FOOD_DETAIL = "foodDetail/{$ARG_FOOD_ID}/{$ARG_MEAL_TYPE}/{$ARG_DATE}"

    /** Recipe editor; with no recipe id it creates a new recipe, otherwise it edits one. */
    const val RECIPE_EDITOR_NEW = "recipeEditor"
    const val RECIPE_EDITOR_EDIT = "recipeEditor/{$ARG_RECIPE_ID}"

    /** Reused food search for picking a recipe ingredient; returns the food id to the editor. */
    const val INGREDIENT_SEARCH = "ingredientSearch"

    /** Log-a-portion for a given recipe. */
    const val LOG_PORTION = "logPortion/{$ARG_RECIPE_ID}"
    /** Barcode scanner, carrying the meal and date a scanned food will be logged to. */
    const val SCANNER = "scanner/{$ARG_MEAL_TYPE}/{$ARG_DATE}"

    /** Custom-food form; [ARG_BARCODE] is optional and pre-fills the label when present. */
    const val CUSTOM_FOOD = "customFood/{$ARG_MEAL_TYPE}/{$ARG_DATE}?$ARG_BARCODE={$ARG_BARCODE}"

    /** Concrete food-search route for [mealType] on [date]. */
    fun foodSearch(mealType: MealType, date: LocalDate): String =
        "foodSearch/${mealType.name}/$date"

    /** Concrete food-detail route for [foodId], logging to [mealType] on [date]. */
    fun foodDetail(foodId: Long, mealType: MealType, date: LocalDate): String =
        "foodDetail/$foodId/${mealType.name}/$date"

    /** Concrete recipe-editor route for editing [recipeId]. */
    fun recipeEditor(recipeId: Long): String = "recipeEditor/$recipeId"

    /** Concrete log-a-portion route for [recipeId]. */
    fun logPortion(recipeId: Long): String = "logPortion/$recipeId"
    /** Concrete scanner route for [mealType] on [date]. */
    fun scanner(mealType: MealType, date: LocalDate): String =
        "scanner/${mealType.name}/$date"

    /** Concrete custom-food route for [mealType] on [date], optionally pre-filling [barcode]. */
    fun customFood(mealType: MealType, date: LocalDate, barcode: String? = null): String {
        val base = "customFood/${mealType.name}/$date"

        return if (barcode.isNullOrBlank()) base else "$base?$ARG_BARCODE=$barcode"
    }
}

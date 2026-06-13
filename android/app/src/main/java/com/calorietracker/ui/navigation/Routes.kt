package com.calorietracker.ui.navigation

import com.calorietracker.data.diary.MealType
import java.time.LocalDate

/** Navigation destinations within the app. */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DIARY = "diary"

    const val ARG_MEAL_TYPE = "mealType"
    const val ARG_DATE = "date"
    const val ARG_FOOD_ID = "foodId"

    /** Food search, scoped to the meal and date the user is adding to. */
    const val FOOD_SEARCH = "foodSearch/{$ARG_MEAL_TYPE}/{$ARG_DATE}"

    /** Food detail, carrying the selected food plus the target meal and date. */
    const val FOOD_DETAIL = "foodDetail/{$ARG_FOOD_ID}/{$ARG_MEAL_TYPE}/{$ARG_DATE}"

    /** Concrete food-search route for [mealType] on [date]. */
    fun foodSearch(mealType: MealType, date: LocalDate): String =
        "foodSearch/${mealType.name}/$date"

    /** Concrete food-detail route for [foodId], logging to [mealType] on [date]. */
    fun foodDetail(foodId: Long, mealType: MealType, date: LocalDate): String =
        "foodDetail/$foodId/${mealType.name}/$date"
}

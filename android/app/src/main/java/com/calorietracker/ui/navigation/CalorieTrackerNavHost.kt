package com.calorietracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.calorietracker.data.diary.MealType
import com.calorietracker.ui.auth.LoginScreen
import com.calorietracker.ui.auth.RegisterScreen
import com.calorietracker.ui.diary.DiaryScreen
import com.calorietracker.ui.fooddetail.FoodDetailScreen
import com.calorietracker.ui.foodsearch.FoodSearchScreen
import com.calorietracker.ui.profile.ProfileScreen
import java.time.LocalDate

/**
 * Top-level navigation graph. Starts at Login; on successful authentication the
 * back stack is reset to the Diary destination so the user cannot navigate back
 * into the auth flow. From the diary the user drills into food search and detail,
 * carrying the target meal and date forward so a saved entry lands on the right day.
 */
@Composable
fun CalorieTrackerNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onAuthenticated = { navController.toDiary() },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onAuthenticated = { navController.toDiary() },
                onNavigateToLogin = { navController.popBackStack() },
            )
        }
        composable(Routes.DIARY) {
            DiaryScreen(
                onAddFood = { mealType, date ->
                    navController.navigate(Routes.foodSearch(mealType, date))
                },
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLoggedOut = { navController.toLogin() },
            )
        }
        composable(
            route = Routes.FOOD_SEARCH,
            arguments = listOf(
                navArgument(Routes.ARG_MEAL_TYPE) { type = NavType.StringType },
                navArgument(Routes.ARG_DATE) { type = NavType.StringType },
            ),
        ) { entry ->
            val mealType = entry.mealType()
            val date = entry.date()

            FoodSearchScreen(
                onFoodSelected = { foodId ->
                    navController.navigate(Routes.foodDetail(foodId, mealType, date))
                },
                onScanBarcode = { /* Barcode scanner is added in a later slice. */ },
                onAddCustomFood = { /* Custom-food creation is added in a later slice. */ },
            )
        }
        composable(
            route = Routes.FOOD_DETAIL,
            arguments = listOf(
                navArgument(Routes.ARG_FOOD_ID) { type = NavType.LongType },
                navArgument(Routes.ARG_MEAL_TYPE) { type = NavType.StringType },
                navArgument(Routes.ARG_DATE) { type = NavType.StringType },
            ),
        ) { entry ->
            FoodDetailScreen(
                foodId = entry.arguments?.getLong(Routes.ARG_FOOD_ID) ?: 0L,
                mealType = entry.mealType(),
                entryDate = entry.date(),
                onSaved = { navController.popBackStack(Routes.DIARY, inclusive = false) },
            )
        }
    }
}

/** Navigates to Diary, clearing the auth flow from the back stack. */
private fun NavHostController.toDiary() {
    navigate(Routes.DIARY) {
        popUpTo(Routes.LOGIN) { inclusive = true }
        launchSingleTop = true
    }
}

/** Returns to Login on logout, clearing the entire authenticated back stack. */
private fun NavHostController.toLogin() {
    navigate(Routes.LOGIN) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}

private fun androidx.navigation.NavBackStackEntry.mealType(): MealType =
    MealType.valueOf(arguments?.getString(Routes.ARG_MEAL_TYPE) ?: MealType.BREAKFAST.name)

private fun androidx.navigation.NavBackStackEntry.date(): LocalDate =
    LocalDate.parse(arguments?.getString(Routes.ARG_DATE) ?: LocalDate.now().toString())

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
import com.calorietracker.ui.customfood.CustomFoodScreen
import com.calorietracker.ui.diary.DiaryScreen
import com.calorietracker.ui.fooddetail.FoodDetailScreen
import com.calorietracker.ui.foodsearch.FoodSearchScreen
import com.calorietracker.ui.scanner.BarcodeScannerScreen
import java.time.LocalDate

/**
 * Top-level navigation graph. Starts at Login; on successful authentication the
 * back stack is reset to the Diary destination so the user cannot navigate back
 * into the auth flow. From the diary the user drills into food search and detail,
 * carrying the target meal and date forward so a saved entry lands on the right day.
 * Food search can branch to the barcode scanner, which routes a known scan straight to
 * food detail and an unknown scan to the custom-food form, both keeping that meal and date.
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
                onScanBarcode = { navController.navigate(Routes.scanner(mealType, date)) },
                onAddCustomFood = { navController.navigate(Routes.customFood(mealType, date)) },
            )
        }
        composable(
            route = Routes.SCANNER,
            arguments = listOf(
                navArgument(Routes.ARG_MEAL_TYPE) { type = NavType.StringType },
                navArgument(Routes.ARG_DATE) { type = NavType.StringType },
            ),
        ) { entry ->
            val mealType = entry.mealType()
            val date = entry.date()

            BarcodeScannerScreen(
                onFoodFound = { foodId ->
                    navController.navigate(Routes.foodDetail(foodId, mealType, date)) {
                        popUpTo(Routes.SCANNER) { inclusive = true }
                    }
                },
                onUnknownBarcode = { barcode ->
                    navController.navigate(Routes.customFood(mealType, date, barcode)) {
                        popUpTo(Routes.SCANNER) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = Routes.CUSTOM_FOOD,
            arguments = listOf(
                navArgument(Routes.ARG_MEAL_TYPE) { type = NavType.StringType },
                navArgument(Routes.ARG_DATE) { type = NavType.StringType },
                navArgument(Routes.ARG_BARCODE) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            val mealType = entry.mealType()
            val date = entry.date()

            CustomFoodScreen(
                barcode = entry.arguments?.getString(Routes.ARG_BARCODE).orEmpty(),
                onCreated = { foodId ->
                    navController.navigate(Routes.foodDetail(foodId, mealType, date)) {
                        popUpTo(Routes.CUSTOM_FOOD) { inclusive = true }
                    }
                },
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

private fun androidx.navigation.NavBackStackEntry.mealType(): MealType =
    MealType.valueOf(arguments?.getString(Routes.ARG_MEAL_TYPE) ?: MealType.BREAKFAST.name)

private fun androidx.navigation.NavBackStackEntry.date(): LocalDate =
    LocalDate.parse(arguments?.getString(Routes.ARG_DATE) ?: LocalDate.now().toString())
